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
			$.jws.submit("monitoringPlugin.pcinfo", "register", { interest: "computerInfo" });
			updateGauge();
		},
		OnClose: function(){
			if(!mWSC.isConnected()) {
				resetGauges();
			}
		},
		OnMessage: function(aEvent, aToken){
			var lDate = "";
			if( aToken.date_val ) {
				lDate = jws.tools.ISO2Date( aToken.date_val );
			}
			log( "<font style='color:#888'>jWebSocket '" + aToken.type + "' token received, full message: '" + aEvent.data + "' " + lDate + "</font>" );
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