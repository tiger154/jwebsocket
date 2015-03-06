# use strict;
use warnings;
use Net::STOMP::Client;
use JSON;
use Data::Dumper;

# the gateway topic of the message broker
$jms_gateway = "/topic/org.jwebsocket.jms.gateway";

# exit flag for the message receiver loop
$flag_exit = 0;

# create STOMP client
# $stomp = Net::STOMP::Client->new(uri => "stomp://localhost:61613");
$stomp = Net::STOMP::Client->new(uri => "stomp://127.0.0.1:61613");

# the unique client id (the message selector)
$endPointId = $stomp->uuid();
$sid = $stomp->uuid();

# connect to message broker
$stomp->connect(
	# if we want to authenticate against the message broker, we can do that here
	# 'login' => "username",
	# 'passcode' => "password",
);

# get and display some information about the connection
$peer = $stomp->peer();
printf( "Connected to broker %s (IP %s), port %d\n",
    $peer->host(), $peer->addr(), $peer->port() );
printf( "Speaking STOMP %s with server %s\n",
    $stomp->version(), $stomp->server() || "UNKNOWN" );
printf( "Session %s started\n", $stomp->session() );
printf( "EndPoint-ID: %s, Session-ID: %s\n", $endPointId, $sid );

# this is the incoming message callback handler
sub processMesage ($$) {
	my($self, $frame) = @_;
	my $cmd = $frame->command();

	# extract the STOMP command from the frame
	if ($cmd eq "MESSAGE") {

		# or debug purposes
		# print Dumper($frame);
		my $sourceId = $frame->header("sourceId");
		printf("Sychronously received from %s: %s\n", 
			# here you can access header data too, if required
			# $frame->header("message-id"), 
			$sourceId,
			$frame->body,
		);

		# get the JSON from the message
		$lJSON = $frame->body;
		# generate a token from the received JSON string
		$lReceived = from_json( $lJSON );
		# nice debug output to console, comment if not desired for production
		print Dumper($lReceived);

		$lToBeSent = NULL;
		$lAnswerProcessed = NULL;
		# when the JMS client connects to the topic 
		# the jWebSocket subsystem send a welcome message.
		# this is used to send the authentication against jWebSocket
		if( $lReceived->{'ns'} eq "org.jwebsocket.jms.gateway" ) {
			if( $lReceived->{'type'} eq "welcome" ) {
				printf( "Authenticating against jWebSocket server...\n" );
				$lToBeSent = {
					"ns" => "org.jwebsocket.plugins.system",
					"type" => "login",
					"username" => "root",
					"password" => "root",
				};
			}
		# the login answers with a response token
		# it can be success or failure
		} elsif( $lReceived->{'ns'} eq "org.jwebsocket.plugins.system" ) {
			# check login response
			if( $lReceived->{'reqType'} eq "login" ) {
				# check if login was successful
				if( $lReceived->{'code'} eq 0 ) {
					printf( "Uploading file to jWebSocket server...\n" );

					$lToBeSent = {
						"ns" => "org.jwebsocket.plugins.filesystem",
						"type" => "save",
						"scope" => "private",
						"encoding" => "base64",
						"encode" => JSON::false,
						"notify" => JSON::false,
						# if to be sent as plain text set encoding to empty string
						# "data" => "This is a test file uploaded by the Perl STOMP client.",
						"data" => "VGhpcyBpcyBhIHRlc3QgZmlsZSB1cGxvYWRlZCBieSB0aGUgUGVybCBTVE9NUCBjbGllbnQu",
						"filename" => "test2.txt",
					};

					#$lToBeSent = {
					#	"ns" => "org.jwebsocket.plugins.jdbc",
					#	"type" => "getNextSeqVal",
					#	"sequence" => "sq_pk_system_log",
					#};
				}
			}
		# process responses from the jWebSocket file system
		} elsif( $lReceived->{'ns'} eq "org.jwebsocket.plugins.filesystem" ) {
			# check login response
			if( defined $lReceived->{'reqType'} 
					&& $lReceived->{'reqType'} eq "save" ) {
				$lAnswerProcessed = 1;
				if( $lReceived->{'code'} eq 0 ) {
					printf( "File was uploaded successfully.\n" );
				} else {
					printf( "File could not be uploaded: %s\n", $lReceived->{'msg'} );
				}

				# right now, in this little POC 
				# we can stop the console application here
				$flag_exit = 1;
			}
		}

		# check if something has to be sent or 
		# if the incoming token shall be ignored
		if( $lToBeSent eq NULL ) {
			if( $lAnswerProcessed eq NULL ) {
				printf( "Incoming token '%s' ignored\n", $lJSON );
			}
		} else {
			$lJSON = to_json( $lToBeSent );
			printf( "Sending token to %s: %s\n", $sourceId, $lJSON );
			print Dumper($lToBeSent);
			$stomp->send(
				# we send to the JMS gateway destination
				destination => $jms_gateway,
				# the JSON as message body
				body => $lJSON,

				# don't miss the end point id to identify the publisher!
				"sourceId" => $endPointId,
				# and add the target id
				"targetId" => $sourceId,
			);
		}
	} else {
		## we received a frame different from "message"
		printf("%s frame received\n", $cmd);
	}

	return( $frame );
}

# asynchronous message processing (recommended for UI applications)

#$stomp->message_callback( sub {
#	my($self, $frame) = @_;
#	printf("asynchronously received message id %s\n%s\n", 
#		$frame->header("message-id"), $frame->body);
#	# check if the program shall be terminated (by sending "quit")
#	$flag_exit = 1 if $frame->body =~ /quit/i;
#	return($self);
#});


# this is the listener to the jWebSocket-2-JMS topic
$stomp->subscribe(
	# we listen to the JMS gateway topic
	destination => $jms_gateway,
	# and use our end point id to select the messages for this node
	selector => "targetId='" . $endPointId . "'",
);

# synchronous message processing (recommended for console applications)
# receive loop, wait for incoming messages until we have a break condition
while (0 == $flag_exit) {
	$stomp->wait_for_frames(
		callback => \&processMesage,
		timeout => 1,
	);
}

# un-subscribe from topic and...
$stomp->unsubscribe(
	destination => $jms_gateway
);
# disconnect from the message broker
$stomp->disconnect();
