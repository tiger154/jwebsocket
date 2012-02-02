/*if(document.layers) document.captureEvents(Event.MOUSEMOVE|Event.KEYDOWN|Event.KEYUP);
document.onmousemove = getMouseY;
document.onkeydown = getKeyCode;
document.onkeyup = function() {
    keyPressed = 0;
    lbar.pixel = 0;
}
function getMouseY(e) {
    if(e && e.pageY) mouseY = e.pageY;
    else if(typeof event != 'undefined') mouseY = event.clientY + getScrollTop();
    $('#game_over').html(e.clientY);
}
*/

$.widget("jws.player",{
        
    _init:function(){
        ePlayer=this;
        $ePlayer=this.element;        
        ePlayerAracket=$ePlayer.find('#playerA_racket');
        ePlayerBracket=$ePlayer.find('#playerB_racket');
        ePlayerAname=$ePlayer.find('#playerA_name');
        ePlayerBname=$ePlayer.find('#playerB_name');
        ePlayerApoints=$ePlayer.find('#playerA_points');
        ePlayerBpoints=$ePlayer.find('#playerB_points');
        ePlayer.onMessage();
    //ePlayer.captureEvent();
    },
    broadcastPlayer: function(e,v){
        
        var args={
            e:parseInt(e),
            v:v
        };            
        $.jws.submit('pingpong','moveplayer',args);
       
    },//aqui es donde me entrantra todos los msj desde el servidor
    onMessage: function(){
        
        // aqui se inicializa las raquetas (player)
        $.jws.bind('pingpong:submitrequest', function(ev, aToken){
            ePlayer.initPlayer(aToken.player,aToken.width,aToken.Heigth,aToken.posX,aToken.posY);          
        }); 
        //se actualiza los score
        $.jws.bind('pingpong:score', function(ev, aToken){
            ePlayer.scoreUpdate(aToken.username1,aToken.score1,aToken.username2,aToken.score2);          
        });
    },
    initPlayer:function(player,width,Heigth,posX,posY){
        if(player=="playLeft"){
            ePlayerAracket.css({ 
                'width' :width+'px', 
                'height' :Heigth+'px',
                'top': posY+'px',
                'left': posX+'px'
            });
        }else if(player=="playRight"){
            ePlayerBracket.css({ 
                'width' :width+'px', 
                'height' :Heigth+'px',
                'top':posY +'px',
                'left': posX+'px'        
            });
        }else{
            
            ePlayerAracket.css({ 
                'width' :width+'px', 
                'height' :Heigth+'px',
                'top': posY+'px',
                'left': posX+'px'
        
            });
            ePlayerAracket.css({ 
                'width' :width+'px', 
                'height' :Heigth+'px',
                'top':posY +'px',
                'left': posX+'px'        
            });
            
        }
                
    },
    captureEvent:function(){ 
        $('body').bind({
            'keydown':function(e){
                if(e.keyCode==38 ||e.keyCode==40){                           
                    ePlayer.broadcastPlayer(e.keyCode,"k"); 
                    e.stopPropagation();
                    e.preventDefault();
                }
            },
            'scroll mousedown DOMMouseScroll mousewheel': function(e) {
                if(e.wheelDelta>20){                
                    ePlayer.broadcastPlayer(38,"m"); 
                }else{
                    ePlayer.broadcastPlayer(40,"m"); 
                } 
                e.stopPropagation();
                e.preventDefault();
            }            
        });  
    },
    decouplingEvent:function(){ 
        $('body').unbind('keydown');
        $('body').unbind('scroll mousedown DOMMouseScroll mousewheel keyup');       
    },
    scoreUpdate:function(username1,score1,username2,score2){
        
        ePlayerAname.text(username1); 
        ePlayerBname.text(username2);         
        ePlayerApoints.text(score1); 
        ePlayerBpoints.text(score2);
    }    
        
});