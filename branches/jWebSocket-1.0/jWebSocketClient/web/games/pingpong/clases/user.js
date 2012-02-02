$.widget("jws.user",{
        
    _init:function(){
        eUser=this;
        $eUser=this.element;
        eDemoBoxHeaderLogin=$eUser.find('#demo_box_header_login');
        eUser.onMessage();
        eUser.showLoginWindow();
    },
    onMessage: function(){
        $.jws.bind('pingpong:loggedinfo', function(ev, aToken){
            eUser.showLoggedWindow(aToken.username);            
        });
    
        $.jws.bind('pingpong:logoff', function(ev, aToken){
            eUser.showLoginWindow();
        });
        $.jws.bind('pingpong:userincorrect', function(ev, aToken){
            jAlert(aToken.message,'Ping Pong Game');
            eUser.messageOnBlur();
        });
    },
    
    showLoginWindow:function(){
        var loginform = $('<div id="login_form"><input id="user_text" type="text" value="User" style="color: graytext"/><input id="user_password" type="text" value="Password" style="color: graytext"/></div>');        
        eDemoBoxHeaderLogin.css("width"," 400px");
        eDemoBoxHeaderLogin.html("");
        eDemoBoxHeaderLogin.append(loginform);
        eDemoBoxHeaderLogin.append('<div id="login_button" onclick="eUser.logIn()"></div>');
        var create = $('<div class="register_login"></div>').click(function(){
            eUser.showCreateLoginWindow();
        });
        eDemoBoxHeaderLogin.append(create);
        eUser.captureEvent();        
    },
    showCreateLoginWindow:function(){
        var loginformcrate = $('<div id="login_form_create"><input id="user_text" type="text" value="User" style="color: graytext"/><input id="user_password" type="text" value="Password" style="color: graytext"/><input id="user_rpassword" type="text" value="Rep Password" style="color: graytext"/></div>');        
        eDemoBoxHeaderLogin.css("width"," 480px");
        eDemoBoxHeaderLogin.html("").append(loginformcrate);
        var create = $('<div class="register_login"></div>').click(function(){
            eUser.createAccount();
        });
        
        var loginBtn = $('<div id="login_button"></div>').css({
            "margin-left": "3px"
        });
        
        loginBtn.click(function(){
            eUser.showLoginWindow();
        });
        eDemoBoxHeaderLogin.append(create).append(loginBtn);
        eUser.captureEvent();       
    },
    showLoggedWindow:function(username){
        var name = $('<div class="name">').text(username).append(" | ");
        var logout = $('<div class="logout"></div>').click(function(){
            eUser.logoff();
        }); 
        var logged = $('<div id="user_info">').append(name).append(logout);
        eDemoBoxHeaderLogin.html(logged);
        eUser.captureEvent();
    },
    logoff:function(){
        $.jws.submit('pingpong','logoff');
    },
    logIn:function(){
        var usser= $('#user_text').val();
        var pass=$('#user_password').val();
        $('#user_text').val("");
        $('#user_password').val("");
    
        var args={
            pwsname:pass,
            username:usser
        };
        if(usser=="User" || pass=="Password"){
            jAlert('Date incorrect','Ping Pong Game');
            eUser.messageOnBlur();
           
        }
        else{ 
            $.jws.submit('pingpong','usser',args);
           
        }
    },
    createAccount:function(){
        var usser= $('#user_text').val();
        var pass=$('#user_password').val();
        var rpass=$('#user_rpassword').val();
        $('#user_text').val("");
        $('#user_password').val("");
        $('#user_rpassword').val("");
        var args={
            pwsname:pass,
            username:usser,
            rpwsname:rpass
        };
        if(usser.length>11){
            jAlert('The user must be less than 12 characters','Ping Pong Game');
            eUser.messageOnBlur();
        }else
        if(usser=="User" || pass=="Password" || rpass=="Rep Password" || rpass!= pass){
            jAlert('Data incorrect','Ping Pong Game');
            eUser.messageOnBlur();
           
        }else{
            $.jws.submit('pingpong','createaccount',args);
            eUser.showLoginWindow();
        }        
              
    },
    messageOnBlur:function(){
        if($('#user_text').val() == ""){
            $('#user_text').val("User").css('color', 'graytext');
        }
        if($('#user_password').val() == ""){
            $('#user_password').val("Password").css('color', 'graytext');
            document.getElementById("user_password").setAttribute("type","text");
        }
        if($('#user_rpassword').val() == ""){
            $('#user_rpassword').val("Rep Password").css('color', 'graytext');
            document.getElementById("user_rpassword").setAttribute("type","text");
        }
    },
    messageOnClick: function(n){
       
        if( $('#user_text').val() == "User" && n==0){
            $('#user_text').val("").css('color', 'black');
        } 
        if( $('#user_password').val() == "Password" && n==1){
            $('#user_password').val("").css('color', 'black'); 
            document.getElementById("user_password").setAttribute("type","password");
        }
        if( $('#user_rpassword').val() == "Rep Password" && n==2){
            $('#user_rpassword').val("").css('color', 'black'); 
            document.getElementById("user_rpassword").setAttribute("type","password");
        }
    } ,    
    captureEvent:function(){ 
        $('#user_text').bind('focus',function(){
            eUser.messageOnClick(0);
        });
        $('#user_text').bind('blur',function(){
            eUser.messageOnBlur();
        });
        $('#user_password').bind('focus',function(){
            eUser.messageOnClick(1);
        });
        $('#user_password').bind('blur',function(){
            eUser.messageOnBlur();
        });
        $('#user_rpassword').bind('focus',function(){
            eUser.messageOnClick(2);
        });
        $('#user_rpassword').bind('blur',function(){
            eUser.messageOnBlur();
        });
    }
});
