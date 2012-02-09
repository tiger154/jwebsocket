/* 
 * @author vbarzana
 */

function init(){
	NS = jws.NS_BASE + ".plugins.sharedcanvas";
		
	$("#clients").switcher();
	$("#image_area").image();
	$("#paint_area").paint();
}

$(document).ready(function(){
	init();
});