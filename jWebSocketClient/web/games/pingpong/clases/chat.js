$.widget("jws.chat",{
    _init:function(){
        eChat=this;
        $eChat=this.element;
        eChatChat=$eChat.find('#chat')
        eChat.hidden = true;
        eChat.minimized = false;
        eChatWindow = $eChat.find("#chat_text");
        eChatArea = $eChat.find("#messages_area");
        eMinimizeChatText = $eChat.find("#minimize_chat_text");
        eChat.captureEvent();
        eChat.onMessage();
        eChatWindow.hide();
        eBallTimeout=0;
    }, 
    
    captureEvent:function(){
        eChatChat.keypress(function(e){
            if (e.charCode == 13){
                eChat.broadcastMessage($(this).val());
                $(this).val("");
                if(eChat.hidden){
                    eChatWindow.fadeIn(150);
                    eChat.hidden = false;
                }
                else if(eChat.minimized){
                    eChat.restore();
                }
            }
        });
        eMinimizeChatText.click(function(){
            eChat.minimize();
        });
    },    
    minimize: function(){
        eChatWindow.slideUp();
        var elem = $("<div id='minimized'>Back to chat</div>").click(function(){
            eChat.restore();
        });
        eObjArea.prepend(elem);
        eChat.minimized = true;
    },
    //    shake: function(elem){
    //        elem.fadeOut(300).fadeIn(400).fadeOut(100).fadeIn(100);
    //    },
    restore: function(){
        eChatWindow.slideDown();
        $eChat.find("#minimized").fadeOut(function(){
            $(this).remove();
        });
        eChat.minimized = false;
    },
    addText:function(text,user,username){
        if(user=="0"){
            var $O_p   = $("<div><p class='sms'><label id='user_name_a'>"+username+": "+"</label>"+text+"</p></div>");
        }else{
            $O_p   = $("<div><p class='sms'><label id='user_name_b'>"+username+": "+"</label>"+text+"</p></div>");
        }
        eChatArea.append($O_p);       
        eChatArea.scrollTop(eChatArea.get(0).scrollHeight - eChatArea.get(0).clientHeight);
        $eChat.find("#minimized").fadeOut(function(){
            $(this).remove();
        });
    },
    broadcastMessage: function(text){
        var args = {
            text: text
        };
        $.jws.submit("pingpong","sms", args);
    },
    onMessage: function(){
        $.jws.bind('pingpong:sms', function(ev, aToken){
            if(eChat.hidden){
                eChatWindow.fadeIn(150);
                eChat.hidden = false;
            }
            else if(eChat.minimized){
                eChat.restore();
            }
            eChat.addText(aToken.text,aToken.user,aToken.username);
            if( eBallTimeout) clearTimeout( eBallTimeout);
            eBallTimeout = setTimeout('eChat.minimize()', 5000);
           
        });
        
    }
});
