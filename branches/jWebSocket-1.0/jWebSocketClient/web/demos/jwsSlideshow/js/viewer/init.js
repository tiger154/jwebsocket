/* 
 * @author vbarzana
 */

function init( ) {
	w = {};
	mLog = {};
	mLog.isDebugEnabled = false;

	// Options
	// @maxLogLines: maximum number of lines that will be logged
	// @linesToDelete: quantity of lines that will be deleted from 
	// the log window each time the log exceeds the maxLogLines
	// the log window will log only if the parent exists
	if ($(top.document).find("#log_box").get(0)) {
		mLog.isDebugEnabled = true;
		$(top.document).find("#log_box").log({
			maxLogLines: 200,
			linesToDelete: 20
		});
	}
	//starting the widget viewer
	$(".container").viewer( );
}

$(document).ready(function( ) {
	init( );
});