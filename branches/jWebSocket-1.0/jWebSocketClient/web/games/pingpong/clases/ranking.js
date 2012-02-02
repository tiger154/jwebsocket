$.widget("jws.ranking",{
        
    _init:function(){
        eRanking=this;
        $eRanking=this.element; 
        eRankingDiv=$eRanking.find('#ranking div.ranking');        
        eRanking.onMessage();
         
    },
    onMessage: function(){        
        $.jws.bind('pingpong:ranking', function(ev, aToken){
            
            eRankingDiv.html("");
            for (var i = 0; i < aToken.username.length; i++) {
                eRanking.initRanking(aToken.username[i],aToken.wins[i],aToken.lost[i]);
            }                       
        });    
        $.jws.bind('pingpong:deleteranking', function(ev, aToken){
            eRankingDiv.html("");                                
        });     
    },
    initRanking:function(username,wins,lost){
        var $O_p   =$('<div class="name">').text(username).append('</div>');
        var $O_pp   =$('<div class="points">').text(wins+" - "+lost).append('</div>');
        eRankingDiv.append($O_p);
        eRankingDiv.append($O_pp);
       
    }
});
