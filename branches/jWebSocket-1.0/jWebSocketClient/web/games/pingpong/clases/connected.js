$.widget("jws.connected",{
        
    _init:function(){
        eConnected=this;
        $eConnected=this.element;         
        eConnected.onMessage();
        eConnected.captureEvent();
        ePlayerslist=$eConnected.find('#players_list');
        ePopupOk=$eConnected.find('#popup_ok');
    },
    onMessage: function(){
        //crear la lista de los que estan conectados
        $.jws.bind('pingpong:usser', function(ev, aToken){
           ePlayerslist.html("");
            for (i = 0; i < aToken.available.length; i++) {
                eConnected.initConnected(aToken.available[i],aToken.state);
            }
            
            for (i = 0; i < aToken.playing.length; i++) {
               eConnected.initPlaying(aToken.playing[i]);
            }
            
        });
        //recive quien te manda la solicitud de juego
        $.jws.bind('pingpong:sendrequest', function(ev, aToken){
            jConfirm(aToken.username+' wants to start a game. Would you like to proceed?', 'Ping Pong Game', function(r) {
                eConnected.has_accepted_request(r,aToken.username);
            });            
        });
        $.jws.bind('pingpong:deleteusser', function(ev, aToken){
            ePlayerslist.html("");              
        });
        //recive quien te manda la solicitud de juego
        $.jws.bind('pingpong:sendrequest', function(ev, aToken){
            jConfirm(aToken.username+' wants to start a game. Would you like to proceed?', 'Ping Pong Game', function(r) {
               eConnected.has_accepted_request(r,aToken.username);
            });            
        });
        //recive la respuesta donde dice que no asecta la solicitud
        $.jws.bind('pingpong:submitsequestno', function(ev, aToken){
            jAlert(aToken.username+' does not want to start a game');
        });
        
    },
    initConnected:function(text,state){
        if(!state){
            var $text;
            var $O_p   =$('<li class="available">').text(text).click(function(){ 
                $text=$(this).text();
                eConnected.send_request($text);   
                jAlert('Waiting for request confirmation', 'Ping Pong Game', function() {        
                   eConnected.has_accepted_request(false,$text);   
                });
                ePopupOk.attr("value",'Cancel');
            });
        }else{
            $O_p   =$('<li class="available">').text(text);
        }
       ePlayerslist.append($O_p);
    },
    initPlaying:function(text){
        var $O_p   =$('<li class="playing">').text(text);
       ePlayerslist.append($O_p);
    },
    captureEvent:function(){        
        
    },
    //envio la solicitud para jugar
    send_request:function (username){
        var args={
            username:username
        };
        $.jws.submit('pingpong','sendrequest',args);
    },//ha aceptado o no la solicitud
    has_accepted_request:function(accepted,username){
              
        var args={
            username:username,
            accepted:accepted
        };
        $.jws.submit('pingpong','submitsequest',args);        
    }        
});