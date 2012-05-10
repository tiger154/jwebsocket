
window.onload = function(){
    document.getElementById("btnStart").onclick = start;
    document.getElementById("btnClosing").onclick = stopConnection;
    document.getElementById("btnClear").onclick = clearLog;
    document.getElementById("btnSend").onclick = send;
}

function start(){
    var lUrl = "ws://localhost:8787/propio";

    aTokenClient = new jws.jWebSocketJSONClient();

    aTokenClient.open(lUrl, {
        OnOpen: function(aEvent){
            loger("open event fired");
        },
        OnTimeout: function(aEvent){
            loger("jwebsocket ontime our event fired");
        },
        OnMessage:function(aEventResponse){
            var lMessage = "";
            console.log(aEventResponse);
            if (aEventResponse.data != undefined)
                lMessage = JSON.parse(aEventResponse.data).data;
            if (JSON.parse(aEventResponse.data).data == undefined)
                lMessage = JSON.stringify(aEventResponse.data);
            receiveMessage(lMessage);

        },
        OnClose: function(aEvent){
            loger("Closes event fired",aEvent);
        }
    });
}

function send(){
    var lTextMessage = document.getElementById("textToSend").value;
        var lMessage = {
            ns:'jws.py.demo',
            type:'chat',
            sender: 'JSClient',
            message:lTextMessage
        }
        aTokenClient.sendToken(lMessage);
    sendingMessage(lMessage);
    document.getElementById("textToSend").value = "";
}

function stopConnection(){
    aTokenClient.close();
}

function receiveMessage(aMessage){
    lMessage = JSON.parse(aMessage);
    lMessage = JSON.parse(lMessage);
        console.log(lMessage);
    if (lMessage.type == "chat"){
      var Llogger = document.getElementById('chatRom');
      var Llog = document.createElement("div");
      var Ltex = document.createTextNode(lMessage.sender+": "+lMessage.message);
      Llog.appendChild(Ltex);
      Llogger.appendChild(Llog);
      loger(aMessage);
    }
    else{
      loger(aMessage);
    }
}

function sendingMessage(aMessage){
    var Llogger = document.getElementById('chatRom');
    var Llog = document.createElement("div");
    var Ltex = document.createTextNode(aMessage.sender+": "+aMessage.message);
    Llog.appendChild(Ltex);
    Llogger.appendChild(Llog);
}

function loger(aText){

    var Llogger = document.getElementById('LogRom');
    var Llog = document.createElement("div");
    var Ltex = undefined;
    if (aText.type != undefined)
        Ltex = document.createTextNode(aText.replace(/\\/g,""));
    else
        Ltex = document.createTextNode(aText);
    Llog.appendChild(Ltex);
    Llogger.appendChild(Llog);

}

function clearLog(){
    var Llogger = document.getElementById('chatRom');
    Llogger.innerHTML = "";
}
