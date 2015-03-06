#	---------------------------------------------------------------------------
#	jWebSocket - Perl STOMP Client (Community Edition, CE)
#	---------------------------------------------------------------------------
#	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
#	Alexander Schulze, Germany (NRW)
#
#	Licensed under the Apache License, Version 2.0 (the "License");
#	you may not use this file except in compliance with the License.
#	You may obtain a copy of the License at
#
#	http://www.apache.org/licenses/LICENSE-2.0
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS,
#	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#	See the License for the specific language governing permissions and
#	limitations under the License.
#	---------------------------------------------------------------------------

# use strict;
use warnings;
use Net::STOMP::Client;
use JSON;
use Data::Dumper;

print( "Welcome to the jWebSocket Perl STOMP Client Demo\n" );

# the JMS gateway topic of the message broker
$jms_gateway = "/topic/org.jwebsocket.jms.gateway";

# exit flag for the message receiver loop
$flag_exit = 0;

# create STOMP client
my $brokerURI = "stomp://localhost:61613";
printf( "Connecting to %s...\n", $brokerURI );
$stomp = Net::STOMP::Client->new(uri => $brokerURI);

# the unique client id (the message selector)
my $user_name = $ENV{USERNAME};
my $computer_name = $ENV{COMPUTERNAME};
$endPointId = $user_name . "-" . $computer_name . "-" . $stomp->uuid();
$sid = $stomp->uuid();

# connect to message broker
$stomp->connect(
	# if we want to authenticate against the message broker, we can do that here
	# 'login' => "username",
	# 'passcode' => "password",
);
printf( "Connected to %s.\n", $brokerURI );

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
			if( $lReceived->{'type'} eq "welcome" ) {
				printf( "Authenticating against jWebSocket server...\n" );
				$lToBeSent = {
					"ns" => "org.jwebsocket.plugins.system",
					"type" => "login",
					"username" => "root",
					"password" => "root",
				};
			# check login response
			} elsif( defined $lReceived->{'reqType'} 
					&& $lReceived->{'reqType'} eq "login" ) {
				# check if login was successful
				if( $lReceived->{'code'} eq 0 ) {
					# process successful authentication
					# $lAnswerProcessed = 1;

					# indirectly send a message to the target via jWebSocket JMS Gateway
					$lToBeSent = {
						"ns" => "org.jwebsocket.plugins.system",
						"type" => "send",
						"targetId" => "org.jwebsocket.perl.server",
						"message" => "This is a test message",
						"data" => "{\"ns\":\"org.jwebsocket.perl\", \"type\":\"demo\", \"data\":\"demo\"}",
					};
				}
			}
		# process responses from the jWebSocket file system
		} elsif( $lReceived->{'ns'} eq "org.jwebsocket.msgctrl" ) {
			$lAnswerProcessed = 1;
			printf( "Incoming message control token '%s' ignored.\n", $lJSON );
		
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
				printf( "Incoming token '%s' ignored, not yet processed, so quitting...\n", $lJSON );
				# we can stop the console application here
				# this is only a little state machine 
				# in bigger applications this should be processed more intelligent.
				$flag_exit = 1;
			}
		} else {
			$lJSON = to_json( $lToBeSent );
			printf( "Sending token %s\n", $lJSON );
			print Dumper($lToBeSent);
			$stomp->send(
				# we send to the JMS Gateway destination
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
