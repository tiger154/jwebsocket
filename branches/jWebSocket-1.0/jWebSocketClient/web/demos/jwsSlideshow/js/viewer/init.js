/* 
 * @author vbarzana
 */

function init( ) {
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = false;
    
	// Options
	// @maxLogLines: maximum number of lines that will be logged
	// @linesToDelete: quantity of lines that will be deleted from 
	// the log window each time the log exceeds the maxLogLines
	// the log window will log only if the parent exists
	if( $( top.document ).find( "#log_box" ).get(0) ) {
		mLog.isDebugEnabled = true;
		$( top.document ).find( "#log_box" ).log( {
			maxLogLines: 200, 
			linesToDelete: 20
		});
	}
	
	//starting the widget viewer
	$(".container").viewer( );
	
	//configuring tooltip as we wish
	$( "[title]" ).tooltip( {
		position: "bottom center", 
		onShow: function(  ) {
			var lTip = this.getTip(  );
			lTip.css({
				"opacity": "1 !important"
			});
			var lTop = ( "<div class='top'></div>" );
			var lMiddle = $( "<div class='middle'></div>" ).text( lTip.text(  ) );
			var lBottom = ( "<div class='bottom'></div>" );
			lTip.html( "" ).append( lTop ).append( lMiddle ).append( lBottom );
			this.getTrigger( ).mouseout( function( ) {
				lTip.hide( ).hide( );
			});
			this.getTrigger( ).mousemove( function( ) {
				lTip.show( );
			});
			
		}
	});
}

$( document ).ready( function( ) {
	init( );
} );