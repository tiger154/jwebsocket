
function init(){
    //    eLog = $('#elog');

    if(jws.browserSupportsWebSockets()){
        
        jws.myConn = new jws.jWebSocketJSONClient();
        jws.myConn.open(jws.JWS_SERVER_URL, {
            OnOpen: function (){ 
                console.log('connected successful');                              
                securityFilter = new jws.SecurityFilter();
                securityFilter.OnNotAuthorized = function(aEvent){
                //                    log("<b><font color='red'>Failure: </font></b><br/>&nbsp;NOT AUTHORIZED to notify an event with id '" + aEvent.type + "'. Logon first!");
                }

                cacheFilter = new jws.CacheFilter();
                cacheFilter.cache = new jws.cache.Cache(new jws.cache.MemoryStorage());
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
     
                rcplugin = generator.generate("rc", notifier, function(){                   
                    rcplugin.startrc({ });
                  
                    rcplugin.s2cLedState = function(event){  
                        changeledsStatus(event.blue, event.red, event.green, event.yellow);                     
                    } 
                    
                    rcplugin.s2cJyostickPosition = function(event){
                        console.log('x: '+event.x + ' y:'+event.y);
                    }
                    
                    rcplugin.s2cMsg = function(event){
                        console.log(event.msg);
                    } 
                    
                   
                });
               
            },
            OnClose:function(event){
                console.log('Connection loss')
            },
            OnMessage:  function(message)
            {
                
            }
        });
    } else {
        var lMsg = jws.MSG_WS_NOT_SUPPORTED;
        alert( lMsg );
        log( lMsg );
    }
}

function sendCommand(command){	
    rcplugin.command({
        args: {            
            order: parseInt(command)
        },        
        OnSuccess: function(response){
           
        }
    });
}