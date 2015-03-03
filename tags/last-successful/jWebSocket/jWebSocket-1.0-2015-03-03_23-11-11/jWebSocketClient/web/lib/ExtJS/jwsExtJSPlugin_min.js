
//  ---------------------------------------------------------------------------
//  jWebSocket - ExtJS PlugIn (Community Edition, CE)
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
Ext.define('Ext.jws',{requires:['Ext.form.Basic'],extend:'Ext.util.Observable',singleton:true,constructor:function(K){
this.addEvents({'open':true,'close':true,'timeout':true,'logon':true,'logoff':true,'beforesend':true,'message':true});
this.superclass.constructor.call(this,K)},init:function(K){Ext.form.Basic.prototype.submit=function(options){return this.doAction(
this.standardSubmit?'standardsubmit':this.api?'directsubmit':this.jwsSubmit?'jwssubmit':'submit',options);};
Ext.form.Basic.prototype.load=function(V){return this.doAction(this.api?'directload':this.jwsSubmit?'jwsload':'load',V);}},open:
function(bD,O,v){var self=this;if(jws.browserSupportsWebSockets()){var aA=bD||jws.getAutoServerURL();if(O)this.bu=O;else this.bu=
new jws.jWebSocketJSONClient();this.bu.open(aA,{OnWelcome:function(C){self.init();self.fireEvent('open');},OnClose:function(){
self.fireEvent('close');},OnTimeout:function(){self.fireEvent('timeout');},OnLogon:function(C){self.fireEvent('logon',C);},OnLogoff:
function(C){self.fireEvent('logoff',C);},OnMessage:function(C){self.fireEvent('message',C);}});if(v)this.setDefaultTimeOut(v);}else{
var R=jws.MSG_WS_NOT_SUPPORTED;Ext.Error.raise(R);}},getConnection:function(){return this.bu;},send:function(bi,an,L,F,j){var T=j;
var l={};if(L){l=Ext.clone(L);}l.ns=bi;l.type=an;this.fireEvent('beforesend',l);this.bu.sendToken(l,{OnResponse:function(C){if(
C.code<0){if('function'==typeof F['failure']){if(j==undefined){return F.failure(C);}F.failure.call(T,C);}}else{if('function'==
typeof F['success']){if(j==undefined){return F.success(C);}F.success.call(T,C);}}},OnTimeOut:function(C){if('function'==
typeof F['timeout']){if(j==undefined){return F.timeout(C);}F.timeout.call(T,C);}}});},addPlugIn:function(aJ){this.bu.addPlugIn(aJ);}
,setDefaultTimeOut:function(v){if(this.bu)this.bu.DEF_RESP_TIMEOUT=v;else jws.DEF_RESP_TIMEOUT=v;},close:function(){this.bu.close();
this.fireEvent('close');}});Ext.define('Ext.jws.data.Proxy',{alternateClassName:'Ext.jws.data.proxy',extend:'Ext.data.proxy.Server',
alias:'proxy.jws',ns:undefined,api:{create:'create',read:'read',update:'update',destroy:'destroy'},transform:function(){},
constructor:function(K){var self=this;self.callParent(arguments);if(self.ns==undefined)Ext.Error.raise(
"the namespace must be specify, jws proxy requires a namespace");},doRequest:function(H,aw,j){var self=this;var aK=this.getWriter();
var G=this.buildRequest(H,aw,j);if(H.allowWrite()){G=aK.write(G);}var l=this.setupDataForRequest(G);this.transform(l);Ext.jws.send(
l.ns,l.type,l.data,{success:function(C){var ar=Ext.encode(C);var aN={request:G,requestId:G.id,status:C.code,statusText:C.msg,
responseText:ar,responseObject:C};self.processResponse(true,H,G,aN,aw,j);},failure:function(C){var ar=Ext.encode(C);var aN={request:
G,requestId:G.id,status:C.code,statusText:C.msg,responseText:ar,responseObject:C};self.processResponse(false,H,G,aN,aw,j);}},j);},
setupDataForRequest:function(V){var bo=V.params||{},bP=V.jsonData,bn=this.ns,J=undefined,t;var T=V;if(Ext.isFunction(bo)){bo=
bo.call(T,V);}t=V.rawData||V.xmlData||bP||null;switch(V.action){case 'create':J=this.api.create;break;case 'update':J=
this.api.update;break;case 'destroy':J=this.api.destroy;break;case 'read':J=this.api.read;break;default:break;}return{ns:bn,type:J,
data:t||bo||null};},setException:function(H,k){H.setException({status:k.status,statusText:k.statusText,responseText:k.responseText,
responseObject:k.responseObject});}});Ext.define('Ext.jws.data.Reader',{extend:'Ext.data.reader.Json',alternateClassName:
'Ext.jws.data.Reader',alias:'reader.jws',root:'data',transform:function(){},readRecords:function(bV){this.transform(bV);
return this.callParent([bV]);}});Ext.define('Ext.jws.form.action.Submit',{extend:'Ext.form.action.Submit',alternateClassName:
'Ext.jws.form.Action.Submit',alias:'formaction.jwssubmit',type:'jwssubmit',ns:undefined,tokentype:undefined,constructor:function(K){
var self=this;self.callParent(arguments);if(self.ns==undefined)Ext.Error.raise("You must specify a namespace (ns) value!");if(
self.tokentype==undefined)Ext.Error.raise("You must specify a token type (tokentype) value!");},getNS:function(){return this.ns||
this.form.ns;},getTokenType:function(){return this.tokentype||this.form.tokentype;},doSubmit:function(){var bM,jwsOptions=Ext.apply(
{ns:this.getNS(),tokentype:this.getTokenType()});var aj=this.createCallback();if(this.form.hasUpload()){bM=jwsOptions.form=
this.buildForm();jwsOptions.isUpload=true;}else{jwsOptions.params=this.getParams();}Ext.jws.send(jwsOptions.ns,jwsOptions.tokentype,
jwsOptions.params,aj,this);if(bM){Ext.removeNode(bM);}},processResponse:function(k){this.as=k;if(!k.responseText&& !k.responseXML&&
 !k.type){return true;}return(this.aM=this.handleResponse(k));},handleResponse:function(k){if(k){var ab=k.data;var t=[];if(ab){for(
var i=0,len=ab.length;i<len;i++){t[i]=ab[i];}}if(t.length<1){t=null;}return{success:k.success,data:t};}return Ext.decode(k.data);}})
;Ext.define('Ext.jws.form.action.Load',{extend:'Ext.form.action.Load',requires:['Ext.direct.Manager'],alternateClassName:
'Ext.jws.form.action.Load',alias:'formaction.jwsload',type:'jwsload',ns:undefined,tokentype:undefined,constructor:function(K){
var self=this;self.callParent(arguments);if(self.ns==undefined)Ext.Error.raise("You must specify a namespace (ns) value!");if(
self.tokentype==undefined)Ext.Error.raise("You must specify a token type (tokentype) value!");},run:function(){var aj=
this.createCallback();Ext.jws.send(Ext.apply({ns:this.ns,tokentype:this.tokentype,params:this.getParams()}));Ext.jws.send(this.ns,
this.tokentype,this.getParams(),aj,this);},processResponse:function(k){this.as=k;if(!k.responseText&& !k.responseXML&& !k.type){
return true;}return(this.aM=this.handleResponse(k));},handleResponse:function(k){if(k){var data=k.data[0]?k.data:null;return{
success:k.success,data:data};}return Ext.decode(k.data);}});