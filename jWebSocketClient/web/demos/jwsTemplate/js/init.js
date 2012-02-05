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
	//executing widgets
	$("#log_box").log();
	$("#demo_box").auth(lCallbacks);
}


$(document).ready(function(){
	init();
});