# use strict;
use warnings;
use Net::STOMP::Client;

# create STOMP client
$stomp = Net::STOMP::Client->new(uri => "stomp://127.0.0.1:61613");

# connect to message broker
$stomp->connect(
	# 'login' => "username",
	# 'passcode' => "password",
);

# get and display some information about the connection
$peer = $stomp->peer();
printf("connected to broker %s (IP %s), port %d\n",
    $peer->host(), $peer->addr(), $peer->port());
printf("speaking STOMP %s with server %s\n",
    $stomp->version(), $stomp->server() || "UNKNOWN");
printf("session %s started\n", $stomp->session());

# exit flag for the message receiver loop
$flag_exit = 0;
$correlationId = $stomp->uuid();
$sid = $stomp->uuid();

# this is the message callback
sub msg_cb ($$) {
	my($self, $frame) = @_;
	my $cmd = $frame->command();

	if ($cmd eq "MESSAGE") {
		printf("sychronously received message id %s\n%s\n", 
			$frame->header("message-id"), $frame->body);
	} else {
		printf("%s frame received\n", $cmd);
	}
	return($frame);
}

# asynchronous message processing (better for UI apps)
$stomp->message_callback( sub {
	my($self, $frame) = @_;
	printf("asynchronously received message id %s\n%s\n", 
		$frame->header("message-id"), $frame->body);
	# check if the program shall be terminated (by sending "quit")
	$flag_exit = 1 if $frame->body =~ /quit/i;
	return($self);
});

# asynchronous message processing (better for UI apps)
$stomp->receipt_callback( sub {
	printf("Receipt, logging in...");
	$stomp->send(
		destination => "/topic/org.jwebsocket.jms2jws",
		body        => "{\"ns\":\"org.jwebsocket.plugins.system\",\"type\":\"login\",\"username\":\"root\",\"password\":\"root\"}",
		"correlation-id" => $correlationId,
	);
});

$stomp->subscribe(
	destination => "/topic/org.jwebsocket.jws2jms",
	selector	=> "JMSCorrelationID='" . $correlationId . "'",
);

# synchronous message processing (better for console applications)
# receive loop, wait for incoming messages
while (0 == $flag_exit) {
	$stomp->wait_for_frames(
		callback => \&msg_cb,
		timeout => 1,
	);
}

# un-subscribe from topic
$stomp->unsubscribe(id => $sid);
# disconnect from broker
$stomp->disconnect();
