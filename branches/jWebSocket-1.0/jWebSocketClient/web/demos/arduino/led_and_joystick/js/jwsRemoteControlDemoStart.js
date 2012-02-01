// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
function init(){	

    if(jws.browserSupportsWebSockets()){
        
        jws.myConn = new jws.jWebSocketJSONClient();
        jws.myConn.open(jws.JWS_SERVER_URL, {
            OnOpen: function (){ 
                console.log('connected successful');                              
                securityFilter = new jws.SecurityFilter();
                securityFilter.OnNotAuthorized = function(aEvent){
                    console.log("Failure: NOT AUTHORIZED to notify an event with id '" + aEvent.type + "'. Logon first!");
                }

                cacheFilter = new jws.CacheFilter();
                cacheFilter.cache = new Cache();
                validatorFiler = new jws.ValidatorFilter();

                //Creating a event notifier
                notifier = new jws.EventsNotifier();
                notifier.ID = "notifier0";
                notifier.NS = "rc";
                notifier.jwsClient = jws.myConn;
                jws.user = new jws.AppUser();
                notifier.filterChain = [securityFilter, cacheFilter, validatorFiler];
                notifier.initialize();
                //Creating a plugin generator
                generator = new jws.EventsPlugInGenerator();
     
                gRcPlugin = generator.generate("rc", notifier, function(){                   
					
                    
                     startArduinoRemoteControl();
                    gRcPlugin.ledState = function(aEvent){  
                        changeledsStatus(aEvent.blue, aEvent.red, aEvent.green, aEvent.yellow);  
						
                    } 
                    
                    gRcPlugin.joystickPosition = function(aEvent){	
                       changePosition(aEvent.x, aEvent.y);                       
                      
                    }
                    
                    gRcPlugin.message = function(aEvent){						
                        alert(aEvent.content)
                    }             
                   
                });
               
            },
            OnClose:function(aEvent){
                console.log('Connection loss')
            },
            OnMessage:  function(aMessage)
            {
                
            }
        });
    } else {
        var lMsg = jws.MSG_WS_NOT_SUPPORTED;
        alert( lMsg );
        log( lMsg );
    }
}

function sendCommand(aCmd){	
    gRcPlugin.command({
        args: {            
            cmd: parseInt(aCmd)
        },        
        OnSuccess: function(aResponse){
            if(aResponse.message != null)
                alert(aResponse.message);
        }
    });
}

function startArduinoRemoteControl(){
    gRcPlugin.startrc({       
              OnSuccess: function(aResponse){
          if(aResponse.message != null)
              alert(aResponse.message);
        }					
    });    
}