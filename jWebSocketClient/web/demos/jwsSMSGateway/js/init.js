/* 
 * @author vbarzana
 */

function init(){
    mWSC                = {};
    w                   = {};
    mLog                = {};
    mLog.isDebugEnabled = true;
    
    //Each demo will configure its own callbacks to be passed to the login widget
    // Default callbacks { OnOpen | OnClose | OnMessage | OnWelcome | OnGoodBye}
    var lCallbacks = {
        OnOpen: function(aEvent){
            $("#container").SMSGateway();
        }
    };
    //executing widgets
    $("#log_box").log();
    $("#demo_box").auth(lCallbacks);
    
    startjWebSocketConnection();
}

function startjWebSocketConnection(){
    if( jws.browserSupportsWebSockets() ) {
        mWSC = new jws.jWebSocketJSONClient({
            OnWelcome: ""
        });
    } else {
        //disable all buttons
        var lMsg = jws.MSG_WS_NOT_SUPPORTED;
        alert( lMsg );
        log( lMsg );
    }
}

$(document).ready(function(){
    init();
});