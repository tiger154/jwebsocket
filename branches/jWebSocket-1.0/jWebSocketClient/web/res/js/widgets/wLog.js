$.widget("jws.log",{
    
    _init:function(){
        w.log               = this;
        w.log.logVisible    = true;
        w.log.eLog          = w.log.element.find("#log_box_content");

        this.registerEvents();
    },
    
    registerEvents: function(){
        //adding click functions
        w.log.element.find('#show_hide_log').click(w.log.showHide);
        w.log.element.find('#clear_log').click(w.log.clearLog);
    },
    
    showHide: function(){
        //if it's shown we have to hide it
        if(w.log.logVisible){
            w.log.element.find("#show_hide_log").removeClass("hide").addClass("show").text("Show Log");
            w.log.element.find("#log_box_content").slideUp(500, function(){
                $(this).removeClass("log_box_visible").addClass("log_box_hidden").slideDown(100).hide();
            });
            w.log.logVisible = false;
        }
        else{
            w.log.element.find("#show_hide_log").removeClass("show").addClass("hide").text("Hide Log");
            w.log.element.find("#log_box_content").fadeOut(100, function(){
                $(this).removeClass("log_box_hidden").addClass("log_box_visible").slideDown(500);
            });
            w.log.logVisible = true;
        }
    },
    clearLog: function(){
        w.log.element.find("#log_box_content").text("");
    }
});

function log( aString ) {
    w.log.eLog.append(aString + "<br>");
    w.log.eLog.scrollTop(w.log.eLog.get(0).scrollHeight - w.log.eLog.get(0).clientHeight);
}