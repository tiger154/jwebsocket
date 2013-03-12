/* 
 * @author vbarzana
 */

function init( ) {
	w = {};
	mLog = {};
	mLog.isDebugEnabled = false;
	//starting the widget viewer
	$(".container").viewer( );
}

$(document).ready(function( ) {
	init( );
});