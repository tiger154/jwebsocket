$.widget("jws.menu",{
        
    _init:function(){        
        eMenu=this;
        $eMenu=this.element;        
        eSendPpause=$eMenu.find('#send_pause');
        eNewGame=$eMenu.find('#new_game');
        ePause=$eMenu.find('#pause');
        eEndGame=$eMenu.find('#end_game');
        eScenarioMenu=$eMenu.find('#scenario_menu');
        eScenarioMenu.hide();
        eMenu.initMenu();
        eMenu.onMessage();
    },
    onMessage: function(){
        $.jws.bind('pingpong:sendnewgame', function(ev, aToken){
            eMenu.sendNewGame();
            eSendPpause.html("");
        });
        $.jws.bind('pingpong:pause', function(ev, aToken){
            eSendPpause.html(aToken.pause);           
        });
    },
    initMenu:function(){
        eNewGame.click(function(){
            eMenu.newGame();
            eSendPpause.html("");
        });
        ePause.click(function(){
            eMenu.pause();
        });
        eEndGame.click(function(){
            eMenu.endGame();
        });             
    },
    newGame:function(){
        $.jws.submit('pingpong','sendnewgame');       
    },
    pause:function(){
        $.jws.submit('pingpong','pause');
    },
    endGame:function(){
        $.jws.submit('pingpong','endgame');
    },
    sendNewGame:function(){
        jConfirm('Begin Game','Ping Pong Game', function(r) {               
            var args={
                newgame:r
            };            
            $.jws.submit('pingpong','newgame',args);
                              
        }); 
    }    
});
