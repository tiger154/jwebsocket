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
	$("#log_box").log({maxLogLines: 200, linesToDelete: 20});
	
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
}


$(document).ready(function(){
	init();
});