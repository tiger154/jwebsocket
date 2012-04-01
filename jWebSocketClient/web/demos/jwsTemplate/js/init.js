/* 
 * @author vbarzana
 */

function init(){
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
	var lCallbacks = {
		OnOpen: function( aEvent ) {
		},
		OnWelcome: function( aEvent )  {
		},
		OnGoodBye: function( aEvent )  {
		},
		OnMessage: function( aEvent, aToken ) {
		},
		OnClose: function( aEvent ) {
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
	
	
	//This is the way anyone can use a jwsDialog
	var lButtons = [{
		id: "buttonYes",	
		text: "Yes",
		aFunction: function(){
			//alert("you clicked YES button");
		}
	},{
		id: "buttonNo",
		text: "No",
		aFunction: function(){
			//alert("you clicked button NO");
		}
	}];

//	dialog(aMessage, aTitle, aIsModal, aCloseFunction, aButtons, aIconType);
	dialog("Would you like to work with us in jWebSocket?", "Accept?", false, null, lButtons);
	dialog("This is a simple ALERT message", "ALERT from jWebSocket", false, null, lButtons, "alert");
	dialog("This is a simple IMPORTANT message", "IMPORTANT from jWebSocket", false, null, lButtons, "important");
	dialog("This is a simple WARNING message", "WARNING from jWebSocket", false, null, null, "warning");
	dialog("This is a simple INFORMATION message", "INFORMATION from jWebSocket", false, null, lButtons, "information");
	dialog("This is a simple ERROR message, also this message is MODAL", "ERROR from jWebSocket", true, null, null, "error");
}


$(document).ready(function(){
	init();
});