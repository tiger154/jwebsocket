var lWSC = new jws.jWebSocketJSONClient();
$(document).ready(function(){
	$('#start').click(function(){
		log('Openning client connections...', true);
				
		$('#start').fadeOut(500);
		openConnections(parseInt($('#clients').val()));
	});
});

var lClients = [];
var lActive = 0;
openConnections = function(aClients){
	if (aClients == 0)
		return;
		
	var lC =  new jws.jWebSocketJSONClient();
	lC.open("ws://localhost:8787/jWebSocket/jWebSocket", {
		OnWelcome: function(){
			lC.login('root', 'root');
		},
		OnLogon: function(){
			if ($('#transport').val() == 'channelBroadcast'){
				lC.channelSubscribe("publicA", "access", {
					OnSuccess: function(){
						log('Established. Active: ' + (++lActive), true);
					}
				});
				lC.setChannelCallbacks({
					OnChannelBroadcast: function(aToken){
					}
				});
			} else if ($('#transport').val() == 'broadcast'){
				log('Established. Active: ' + (++lActive), true);
			} else if ($('#transport').val() == 'jmsBroadcast'){
				lC.listenJms( 
					"connectionFactory",	// aConnectionFactoryName, 
					"testQueue",			// aDestinationName, 
					false, {
						OnSuccess: function(aToken){
							log('Established. Active: ' + (++lActive), true);
						}
					}
					);
				lC.setJMSCallbacks({
					OnHandleJmsText: function(aToken){
						console.log(aToken);
					}
				});
			}
			
			lClients.push(lC);
			openConnections(aClients - 1);
		},
		OnClose: function(){
			log('Closed. Active: ' + (--lActive), true);
		},
		OnMessage: function(aMessage){
		}
	});
}