/* 
 * @author vbarzana
 */

function init(){
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
    
	//Each demo will configure its own callbacks to be passed to the login widget
	var lCallbacks = {
		OnOpen: function(aEvent){
			if(mLog.isDebugEnabled){
				log("Opening jWebSocket");
				log(aEvent);
			}
		},
		OnClose: function(aEvent){
			if(mLog.isDebugEnabled){
				log("Closing jWebSocket");
				log(aEvent);
			}
		},
		OnMessage: function(aEvent){
			if(mLog.isDebugEnabled){
				log("Incoming message from jWebSocket");
				log(aEvent);
			}
		},
		OnWelcome: function(aEvent){
			if(mLog.isDebugEnabled){
				log("Welcome to jWebSocket");
				log(aEvent);
			}
		},
		OnGoodBye: function(aEvent){
			if(mLog.isDebugEnabled){
				log("good bye jWebSocket");
				log(aEvent);
			}
		}
	};
    
	//executing widgets
	$("#log_box").log();
	$("#demo_box").auth();
	$("#demo_box").actions();
	
	//configuring tooltip as we wish
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
	
checkWebSocketSupport();
}

function checkWebSocketSupport(){
	if( jws.browserSupportsWebSockets() ) {
		lWSC = new jws.jWebSocketJSONClient({
			OnWelcome: ""
		});
        
        
		lWSC.setSamplesCallbacks({
			OnSamplesServerTime: getServerTimeCallback
		});
		lWSC.setFileSystemCallbacks({
			OnFileLoaded: onFileLoadedObs,
			OnFileSaved: onFileSavedObs,
			OnFileError: onFileErrorObs
		});
	} else {
		//disable all buttons
		//        $( "#login_button" ).attr( "disabled", "disabled" );
		var lMsg = jws.MSG_WS_NOT_SUPPORTED;
		alert( lMsg );
		if(mLog.isDebugEnabled)
			log( lMsg );
	}
}

$(document).ready(function(){
	init();
});