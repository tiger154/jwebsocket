
//  ---------------------------------------------------------------------------
//  jWebSocket - jQuery PlugIn (Community Edition, CE)
//  ---------------------------------------------------------------------------
//  Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  ---------------------------------------------------------------------------
(function($){$.jws=$({});$.jws.open=function(bD,O,v){if(jws.browserSupportsWebSockets()){var bv=bD||jws.getAutoServerURL();if(O){
$.jws.bu=O;}else{$.jws.bu=new jws.jWebSocketJSONClient();}if(!this.isConnected()){var h={OnOpen:function(C){$.jws.bu.addPlugIn(
$.jws);$.jws.trigger('open',C);},OnWelcome:function(C){$.jws.trigger('welcome',C);},OnClose:function(C){var R=
"jWebSocket connection closed, please "+"check that your jWebSocket server is running";jws.console.log(R);$.jws.trigger('close',C);}
,OnTimeout:function(C){$.jws.trigger('timeout',C);},OnLogon:function(C){$.jws.trigger('logon',C);},OnLogoff:function(C){
$.jws.trigger('logoff',C);},OnMessage:function(C){$.jws.trigger('message',C);}};if(v){h.timeout=v;}$.jws.bu.open(bv,h);}else{
jws.console.log("The connection was already open while "+"trying to open a new one!");}}else{var R=jws.MSG_WS_NOT_SUPPORTED;alert(R)
;}};$.jws.send=function(bc,an,L,F,V){if(bc&&an){var l={};if(L){l=L;}l.ns=bc;l.type=an;var h={OnResponse:function(C){if(C.code=== -1)
{if(F&&typeof F.failure==="function"){return F.failure(C);}}else if(C.code===0){if(F&&typeof F.success==="function"){
return F.success(C);}}},OnTimeOut:function(){if(F&&typeof F.timeout==="function"){return F.timeout();}}};if(V&&V.timeout){h.timeout=
V.timeout;}if(this.isConnected()){this.bu.sendToken(l,h);}else{jws.console.log("Trying to send a message using a non "+
"connected client, please verify that your jWebSocket"+" connection is opened");}}else{jws.console.log(
"The namespace and the token type are required, "+"please provide them correctly to send the data to the server");}};
$.jws.processToken=function(C){$.jws.trigger('all:all',C);$.jws.trigger('all:'+C.type,C);$.jws.trigger(C.ns+':all',C);$.jws.trigger(
C.ns+':'+C.type,C);};$.jws.getDefaultServerURL=function(){if(this.bu&&typeof this.bu.getAutoServerURL==="function"){
return this.bu.getAutoServerURL();}else{return jws.getAutoServerURL();}};$.jws.setDefaultTimeOut=function(v){if(this.bu){
this.bu.DEF_RESP_TIMEOUT=v;}else{jws.DEF_RESP_TIMEOUT=v;}};$.jws.close=function(){try{var bk=this.bu.close({timeout:
jws.DEF_RESP_TIMEOUT});if(bk.code!==0){jws.console.log(bk.msg);}else{$.jws.trigger('close',bk);}}catch(ad){jws.console.log(ad);}};
$.jws.setTokenClient=function(O){$.jws.bu=O;$.jws.bu.addPlugIn($.jws);};$.jws.getConnection=function(){return this.bu;};
$.jws.isConnected=function(){if($.jws.bu){return $.jws.bu.isConnected();}return false;};$.jws.addPlugIn=function(aJ){if(aJ&&
typeof aJ.processToken==="function"){$.jws.bu.addPlugIn(aJ);}else{throw("Please check, your PlugIn Object should contain a "+
"processToken function");}};})(jQuery); 