$.widget("jws.stage",{
        
    _init:function(){
        eStage=this;
        $eStage=this.element;         
        $.jws.submit('pingpong','stage');
        eObjArea=$eStage.find('#obj_area');
        eGameOver=$eStage.find('#game_over');
        eSendPause=$eStage.find('#send_pause');
        eMessagesArea=$eStage.find('#messages_area');
        eBoard=$eStage.find('#board');
        eCounter=$eStage.find('#counter');
        eStage.onMessage();
        eObjArea.hide();
        eGameOver.hide();
                
    },
    //aqui es donde me entrantra todos los msj desde el servidor
    onMessage: function(){
        //pinta el esenario
        $.jws.bind('pingpong:stage', function(ev, aToken){
            eStage.initStage(aToken.width,aToken.height,aToken.gameBorder);           
        });
        $.jws.bind('pingpong:gameover', function(ev, aToken){ 
            eStage.gameOver(aToken.gameover,aToken.message);           
        });
        //activo y desactivo los ObjArea
        $.jws.bind('pingpong:objarea', function(ev, aToken){            
            eStage.objArea(aToken.objarea);   
        }); 
        //activo y desactivo el contador
        $.jws.bind('pingpong:counter', function(ev, aToken){
            eStage.counter(aToken.counter);           
        });
        $.jws.bind('pingpong:sendexit', function(ev, aToken){
            jAlert(aToken.username+' has left the game');           
        });
    },
    initStage:function(width,height,gameBorder){
        // inicializar ancho y largo
        //alert(gameBorder);
        eBoard.css({
            'width' :width - gameBorder * 2+'px', 
            'height' :height- gameBorder * 2+'px'
        });
    },
    gameOver: function(gameover,message){
        if(gameover){  
            eGameOver.text(message);
            eGameOver.show();
            eBallBall.hide();
        }else{
            eGameOver.hide();
        }
    },
    objArea:function(objarea){
        if(objarea){
            eObjArea.show();
            eScenarioMenu.show();
            eBallBall.hide();
            eCounter.hide();
            ePlayer.captureEvent();           
        }else{
            eObjArea.hide();
            eGameOver.hide();
            eScenarioMenu.hide();
            ePlayer.decouplingEvent();
        }
        $.alerts._hide();
        eSendPause.html("");
        eMessagesArea.html("");
        eChat.minimize();
  
        
    },
    counter:function(counter){
        if(counter==0){
            eCounter.fadeOut(200);
            eBallBall.fadeIn(100);
        }else{
            eBallBall.hide();
            eCounter.show();
            eCounter.html(counter);
        }
    }
});
