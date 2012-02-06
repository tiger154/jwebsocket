/* 
 * @author vbarzana
 */

function init(){
	w                   = {};
	mLog                = {};
	mLog.isDebugEnabled = true;
	
	var lOptions = {
		OnOpen: function( aEvent ) {
			console.log("successfully connected");
			// start keep alive if user selected that option
			lWSC.startKeepAlive({
				interval: 30000
			});
		},
		OnWelcome: function( aEvent )  {
		},
		OnGoodBye: function( aEvent )  {
		},
		OnMessage: function( aEvent, aToken ) {
		},
		OnClose: function( aEvent ) {
			eStatus.src = "../../images/disconnected.png";
			lIsConnected = false;
			lWSC.stopKeepAlive();
		}, 
		lURL: jws.getDefaultServerURL() + ( frameElement.id ? ";unid=" + frameElement.id : "")
	};
	
//	$("#demo_box").auth(lOptions);
	$("#clients").image();

	//Enabling ToolTip
	$("[title]").tooltip({
		position: "bottom center", 
		onShow: function() {
			var lTip = this.getTip();
			var lTop = ("<div class='top'></div>");
			var lMiddle = $("<div class='middle'></div>").text(lTip.text());
			var lBottom = ("<div class='bottom'></div>");
			lTip.html("").append(lTop).append(lMiddle).append(lBottom);
		}
	});
}

$(document).ready(function(){
	init();
});