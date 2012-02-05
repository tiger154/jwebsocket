/* 
 * @author vbarzana
 */

function init(){
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
	var lCallbacks = {
		OnOpen: function( aEvent ) {
			console.log("successfully connected");
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
	//executing widgets
	$("#demo_box").auth(lCallbacks);
	$("#clients").image();
}

$(document).ready(function(){
	init();
});