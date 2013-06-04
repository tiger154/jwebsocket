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

# the listen topic of the message broker
$subscribe_destination = "/topic/org.jwebsocket.jws2jms";
# the send topic of the message broker
$publish_destination = "/topic/org.jwebsocket.jms2jws";

# exit flag for the message receiver loop
$flag_exit = 0;

# create STOMP client
$stomp = Net::STOMP::Client->new(uri => "stomp://127.0.0.1:61613");

# the unique client id (the message selector)
$correlationId = $stomp->uuid();
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
printf( "Correlation-ID: %s, Session-ID: %s\n", $correlationId, $sid );

# this is the incoming message callback handler
sub processMesage ($$) {
	my($self, $frame) = @_;
	my $cmd = $frame->command();

	# extract the STOMP command from the frame
	if ($cmd eq "MESSAGE") {
		printf("Sychronously received: %s\n", 
			# here you can access header data too, if required
			# $frame->header("message-id"), 
			$frame->body
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
		if( $lReceived->{'ns'} eq "org.jwebsocket.jms.bridge" ) {
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
			if( defined $lReceived->{'reqType'} 
					&& $lReceived->{'reqType'} eq "login" ) {
				# check if login was successful
				if( $lReceived->{'code'} eq 0 ) {
					# process successful authentication
					# $lAnswerProcessed = 1;
					$lToBeSent = {
						"ns" => "org.jwebsocket.plugins.system",
						"type" => "send",
						"targetId" => "org.jwebsocket.perl.server",
						"message" => "This is a test message",
						"json" => "{\"ns\":\"org.jwebsocket.perl\", \"type\":\"demo\", \"data\":\"demo\"}",
					};
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
			printf( "Sending token %s\n", $lJSON );
			print Dumper($lToBeSent);
			$stomp->send(
				# we send to the jms->jws destination
				destination => $publish_destination,
				# the JSON as message body
				body => $lJSON,

				# don't miss the correlation id to identify the publisher!
				"correlation-id" => $correlationId,
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
	# we listen to the jws->jms topic
	destination => $subscribe_destination,
	# and use our correlation id to select the messages for this node
	selector => "JMSCorrelationID='" . $correlationId . "'",
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
$stomp->unsubscribe();
# disconnect from the message broker
$stomp->disconnect();
