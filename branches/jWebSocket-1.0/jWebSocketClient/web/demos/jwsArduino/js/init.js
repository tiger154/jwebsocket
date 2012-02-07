/* 
 * @author xdariel
 */

function init(){
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
	gRcPlugin			= null;
	
	gElements = {
		eLedBlue    : $("#ledblue"),
		eLedRed     : $("#ledred"),
		eLedGreen   : $("#ledgreen"),
		eLedYellow  : $("#ledyellow"),
		ePosition   : $("#point")
	};
	
	var lOptions = {
		lURL: jws.getDefaultServerURL(),
		OnOpen: function( aEvent ) {
			if( !gRcPlugin ) {
				securityFilter = new jws.SecurityFilter();
				securityFilter.OnNotAuthorized = function(aEvent){
					log("Failure: NOT AUTHORIZED to notify an event with id '" + aEvent.type + "'. Logon first!");
				}

				cacheFilter = new jws.CacheFilter();
				cacheFilter.cache = new Cache();
				validatorFiler = new jws.ValidatorFilter();

				//Creating a event notifier
				notifier = new jws.EventsNotifier();
				notifier.ID = "notifier0";
				notifier.NS = "rc";
				notifier.jwsClient = mWSC;
				jws.user = new jws.AppUser();
				notifier.filterChain = [securityFilter, cacheFilter, validatorFiler];
				notifier.initialize();
				//Creating a plugin generator
				generator = new jws.EventsPlugInGenerator();
     
				gRcPlugin = generator.generate("rc", notifier, function(){
				
					startArduinoRemoteControl();
				
					gRcPlugin.ledState = function(aEvent){  
						changeledsStatus(aEvent.blue, aEvent.red, aEvent.green, aEvent.yellow);  
					} 
                    
					gRcPlugin.joystickPosition = function(aEvent){	
						changePosition(aEvent.x, aEvent.y);                       
					}
                    
					gRcPlugin.message = function(aEvent){						
						alert(aEvent.content)
					}
				});
			}
		}
	};
	// Options
	// @maxLogLines: maximum number of lines that will be logged
	// @linesToDelete: quantity of lines that will be deleted from 
	// the log window each time the log exceeds the maxLogLines
	$("#log_box").log({
		maxLogLines: 200, 
		linesToDelete: 20
	});
	
	$("#demo_box").auth(lOptions);
	
	//Configuring tooltip as we wish
	$("[title]").tooltip({
		position: "bottom center",
		onShow: function() {
			var lTip = this.getTip();
			var lTop = ("<div class='top'></div>");
			var lMiddle = $("<div class='middle'></div>").text(lTip.text());
			var lBottom = ("<div class='bottom'></div>");
			lTip.html("").append(lTop).append(lMiddle).append(lBottom);
		}
	});
}

function registerEvents(){
	gElements.eLedBlue.click( function(){       		
		sendCommand(49);                  
	});  
   
	gElements.eLedRed.click( function(){
		sendCommand(50);
	});
	gElements.eLedGreen.click( function(){
		sendCommand(51);
	});
	gElements.eLedYellow.click( function(){
		sendCommand(52);
	});
}

function sendCommand(aCmd){	
	gRcPlugin.command({
		args: {            
			cmd: parseInt(aCmd)
		},        
		OnSuccess: function(aResponse){
			if(aResponse.message != null)
				alert(aResponse.message);
		}
	});
}

function startArduinoRemoteControl(){
	registerEvents();
	
	gRcPlugin.startrc({
		OnSuccess: function(aResponse){
			if(aResponse.message != null)
				alert(aResponse.message);
		}					
	});    
}

$(document).ready(function(){
	init();
});