
window.onload = function(){
    document.getElementById("btnStart").onclick = start;
    document.getElementById("btnClosing").onclick = stopConnection;
    document.getElementById("btnClear").onclick = clearLog;
    document.getElementById("btnSend").onclick = send;
}

/**
 * 
 * 
 * 
 * This is the xhrwebsocket test embed in to jwebsocket client
 */
function start(){
    var lUrl = "http://localhost:8084/longPolling-1.0/ce";

    aTokenClient = new jws.jWebSocketJSONClient();

    aTokenClient.open(lUrl, {
        OnOpen: function(aToken){
            log("Open event was fired");
        },
        OnTimeout: function(aEvent){
            log("jwebsocket ontime our event fired");
        },
        OnMessage:function(aEventResponse){
            var lMessage = "";
            if (aEventResponse.data != undefined)
                lMessage = "message receive: "+JSON.parse(aEventResponse.data).data;
            if (JSON.parse(aEventResponse.data).data == undefined)
                lMessage = "message receive: "+JSON.stringify(aEventResponse.data);
            log(lMessage);
        },
        OnClose: function(aEvent){
            log("Open event was fired");
        }
    });
}

function send(){
	var lRepeatMessage = document.getElementById("repeatMessages").value;
    var lTextMessage = document.getElementById("textToSend").value;
    var lMessage = {
        ns:'jws.comet.sample',
        type:'chat',
        message:lTextMessage
    }

	if (lRepeatMessage == 1){
		aTokenClient.sendToken(lMessage);
	}else{
		for (var i = 0; i < lRepeatMessage; i++){
			aTokenClient.sendToken(lMessage);
		}
	}
	
    document.getElementById("textToSend").value = "";
}

function stopConnection(){
    aTokenClient.close();
}

function log(Text){
    var Llogger = document.getElementById('log');
    var Llog = document.createElement("div");
    var Ltex = document.createTextNode(Text.replace(/\\/g,""));
    Llog.appendChild(Ltex);
    Llogger.appendChild(Llog);
}

function clearLog(){
    var Llogger = document.getElementById('log');
    Llogger.innerHTML = "";
}
