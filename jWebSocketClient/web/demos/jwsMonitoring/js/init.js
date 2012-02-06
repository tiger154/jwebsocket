/* 
 * @author vbarzana
 */

function init(){
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
	
	//Each demo will configure its own callbacks to be passed to the login widget
	var lCallbacks = {
		OnOpen: function(){
			//CONFIGURING JWEBSOCKET CLIENT mWSC defined in widget Auth
			$.jws.setTokenClient(mWSC);
			$.jws.submit("monitoringPlugin.pcinfo", "register");
			updateGauge();
		},
		OnClose: function(){
			if(!mWSC.isConnected()) {
//				resetGauges();
			}
		}
	};
	
	$("#log_box").log({
		maxLogLines: 200, 
		linesToDelete: 20
	});
	$("#demo_box").auth(lCallbacks);
}

$(document).ready(function(){
	init();
});