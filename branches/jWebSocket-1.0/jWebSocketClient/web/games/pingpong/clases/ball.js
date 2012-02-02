$.widget("jws.ball",{
    _init:function(){
        eBall=this;
        $eBall=this.element;
       eBall.onMessage();
        eBallBall=$eBall.find('#ball');
        eBallBall.hide();
       
    }, 
    onMessage: function(){
     
        $.jws.bind('pingpong:ball', function(ev, aToken){
            eBall.updateBall(aToken.width,aToken.height);           
        }); 
        $.jws.bind('pingpong:moveball', function(ev, aToken){
           eBall.moveBall(aToken.posX,aToken.posY);           
        });
        $.jws.bind('pingpong:sound', function(ev, aToken){
            $("#sound")[0].play();         
        });
    },
    updateBall:function(width,height){
        eBallBall.css({ 
            'width' :width+'px', 
            'height' :height+'px'                        
        });        
    },
    moveBall:function(posX,posY){
       eBallBall.css({ 
            'left' :posX+'px', 
            'top' :posY+'px'                        
        });   
    }
});