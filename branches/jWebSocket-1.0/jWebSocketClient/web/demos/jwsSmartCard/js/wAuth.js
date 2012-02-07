//	****************************************************************************
//	jWebSocket Hello World (uses jWebSocket Client and Server)
//	(C) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	****************************************************************************

/*
 * @author vbarzana
 */
$.widget("jws.auth",{
    
	_init:function(){
		w.auth   = this;
        
		w.auth.logoffArea      = w.auth.element.find("#logoff_area");
		w.auth.logonArea       = w.auth.element.find("#login_area");
		w.auth.eUsername       = w.auth.element.find("#user_text");
		w.auth.ePassword       = w.auth.element.find("#user_password");
		w.auth.eClientStatus   = w.auth.element.find("#client_status");
		w.auth.eUserInfoName   = w.auth.element.find("#user_info_name");
		w.auth.eWebSocketType  = w.auth.element.find("#websocket_type");
		w.auth.eClientId       = w.auth.element.find("#client_id");
		w.auth.eLoginButton    = w.auth.element.find('#login_button');
		w.auth.eLogoffButton   = w.auth.element.find('#logoff_button');
        
		w.auth.logoffArea.hide();
        
		w.auth.eLoginButton.click(w.auth.logon);
		w.auth.eLogoffButton.click(w.auth.logoff);
	},
	
	logoff: function(){
		auth.logoff();
		lWSC.fUsername = null;
		w.auth.eUsername.val("");
		w.auth.ePassword.val("");
		
		resetDetails();
	},
	
	logon: function(){
		var lUsername = w.auth.eUsername.val();
		var lPassword = w.auth.ePassword.val();
		if(lUsername == "" || lPassword == ""){
			dialog("Incorrect Data", "User or password are not correct, please check");
			return;
		}
	
		auth.logon({
			args: {
				username: lUsername,
				password: lPassword
			},
			OnFailure: function(aResponseEvent){
				log( "<font style='color:red'>Invalid credentials. Try again.</font>" );
			},
			OnSuccess: function(aResponseEvent){
				log( "<font style='color:red'>User authenticated successfully.</font>" );
				lWSC.fUsername = aResponseEvent.username;

				//Populating the application user instance with the principal, uuid and roles
				jws.user.principal = aResponseEvent.username;
				jws.user.uuid = aResponseEvent.uuid;
				jws.user.roles = aResponseEvent.roles;
				
				jc.getUserInfo({
					OnSuccess: function(aResponse){
						$("#text").find("p").html("Welcome: "+ aResponse.firstname + "<br/>You are authenticated!&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
						$("#img").attr("style", "background: url(css/images/" + lWSC.fUsername + ".png)");
						$(".firstname").attr("value", aResponse.firstname);
						$(".secondname").attr("value", aResponse.secondname);
						$(".address").text(aResponse.address);
					}
				});
				
			}
		});
	}
});

