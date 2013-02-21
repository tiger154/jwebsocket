/* 
 * @author vbarzana
 */

function init( ) {
	w = "undefined" != typeof( w )?w:{ };
	NS = jws.NS_BASE + ".plugins.sharedcanvas";
	// Setting the styles to the buttons, avoiding to fill the HTML code 
	// with unnecessary data
	$( '.button' ).each( function( ) {
		var lBtn = $( this );
		var lRightClass = lBtn.hasClass( 'download' ) ? 'r_download' : 'btn_right';
		lBtn.attr( "class", "button onmouseup" )
		.attr( "onmouseover", "this.className='button onmouseover'" )
		.attr( "onmousedown", "this.className='button onmousedown'"  )
		.attr( "onmouseup", "this.className='button onmouseup'"  )
		.attr( "onmouseout", "this.className='button onmouseout'"  )
		.attr( "onclick", "this.className='button onmouseover'"  );
		lBtn.html( '<div class="btn_left"/>'+'<div class="btn_center">' +
			lBtn.html( )+ '</div>' + '<div class="' + lRightClass + '"></div>' );
	});
	
	$( "#clients" ).switcher( );
	$( "#image_area" ).image( );
	$( "#paint_area" ).paint( );
}

$( document ).ready( function( ) {
	init( );
});