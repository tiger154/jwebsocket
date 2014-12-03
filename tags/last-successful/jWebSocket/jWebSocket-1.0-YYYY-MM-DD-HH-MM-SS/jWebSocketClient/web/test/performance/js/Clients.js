var lWSC = new jws.jWebSocketJSONClient();
$(document).ready(function() {
	$('#start').click(function() {
		log('Openning client connections...', true);

		$('#start').fadeOut(500);
		openConnections(parseInt($('#clients').val()));
	});
});

var lClients = [];
var lActive = 0;
openConnections = function(aClients) {
	if (aClients == 0)
		return;

	var lWSC = new jws.jWebSocketJSONClient();
	lWSC.open("ws://localhost:8787/jWebSocket/jWebSocket?sessionCookieName=test_client2" + new Date().getTime(), {
		OnWelcome: function(aToken) {
			if (aToken.username != "anonymous") {
				this.fOnLogon();
			} else {
				lWSC.login('root', 'root');
			}
		},
		OnLogon: function() {
			if ($('#transport').val() == 'channelBroadcast') {
				lWSC.channelSubscribe("publicA", "access", {
					OnSuccess: function() {
						log('Established. Active: ' + (++lActive), true);
					}
				});
				lWSC.setChannelCallbacks({
					OnChannelBroadcast: function(aToken) {
					}
				});
			} else if ($('#transport').val() == 'broadcast') {
				log('Established. Active: ' + (++lActive), true);
			} else if ($('#transport').val() == 'jmsBroadcast') {
				lWSC.listenJms(
						"connectionFactory", // aConnectionFactoryName, 
						"testQueue", // aDestinationName, 
						false, {
							OnSuccess: function(aToken) {
								log('Established. Active: ' + (++lActive), true);
							}
						}
				);
				lWSC.setJMSCallbacks({
					OnHandleJmsText: function(aToken) {
					}
				});
			}

			lClients.push(lWSC);
			openConnections(aClients - 1);
		},
		OnClose: function() {
			log('Closed. Active: ' + (--lActive), true);
		},
		OnMessage: function(aMessage) {
		}
	});
}