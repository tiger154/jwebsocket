/* 
 * @author vbarzana
 */

function init(){
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
	
	var lCallbacks = {
		OnOpen: function(aEvent){
		},
		OnClose: function(aEvent){
		},
		OnMessage: function(aEvent, aToken){
			log("<font style='color:#888'>" + JSON.stringify(aToken) + "</font>");
		},
		OnWelcome: function(aEvent){
		},
		OnGoodBye: function(aEvent){
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
	
	$("#demo_box").auth(lCallbacks);
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
		lWSC = new jws.jWebSocketJSONClient({});
	} else {
		var lMsg = jws.MSG_WS_NOT_SUPPORTED;
		alert( lMsg );
		if(mLog.isDebugEnabled)
			log( lMsg );
	}
}

$(document).ready(function(){
	init();
});