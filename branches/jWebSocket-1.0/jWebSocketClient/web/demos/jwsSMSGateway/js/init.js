/* 
 * @author vbarzana
 */

function init(){
	mWSC                = {};
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
    
	// Each demo will configure its own callbacks to be passed to the login widget
	// Default callbacks { OnOpen | OnClose | OnMessage | OnWelcome | OnGoodBye}
	var lCallbacks = {
		
		OnOpen: function(aEvent){
			$("#container").SMSGateway();
		}
	};
	
	// Options
	// @maxLogLines: maximum number of lines that will be logged
	// @linesToDelete: quantity of lines that will be deleted from 
	// the log window each time the log exceeds the maxLogLines
	$("#log_box").log({maxLogLines: 200, linesToDelete: 20});
	
	$("#demo_box").auth(lCallbacks);
	
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
	
	startjWebSocketConnection();
}

function startjWebSocketConnection(){

	if( jws.browserSupportsWebSockets() ) {
		mWSC = new jws.jWebSocketJSONClient({
			OnWelcome: ""
		});
	} else {
		//disable all buttons
		var lMsg = jws.MSG_WS_NOT_SUPPORTED;
		alert( lMsg );
		log( lMsg );
	}
}

$(document).ready(function(){
	init();
});