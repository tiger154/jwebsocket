/* 
 * @author vbarzana
 */

function init(  ) {
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
    
	// Options
	// @maxLogLines: maximum number of lines that will be logged
	// @linesToDelete: quantity of lines that will be deleted from 
	// the log window each time the log exceeds the maxLogLines
	$( "#log_box" ).log({
		maxLogLines: 200, 
		linesToDelete: 20
	});
	
	//starting the widget Streaming
	$( "#demo_box" ).streaming();
	
	//configuring tooltip as we wish
	$( "[title]" ).tooltip( {
		position: "bottom center", 
		onShow: function(  ) {
			var lTip = this.getTip(  );
			var lTop = ( "<div class='top'></div>" );
			var lMiddle = $( "<div class='middle'></div>" ).text( lTip.text(  ) );
			var lBottom = ( "<div class='bottom'></div>" );
			lTip.html( "" ).append( lTop ).append( lMiddle ).append( lBottom );
			lTip.mouseover( function(){
				$( this ).hide();
			});
		}
	} );
}
$( document ).ready( function(  ) {
	init(  );
} );