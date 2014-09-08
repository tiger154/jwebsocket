
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise Filesystem Client Plug-In
//	(C) Copyright 2012-2013 Innotrade GmbH, Herzogenrath Germany
//	Author: Rolando Santamaria Maso
//	---------------------------------------------------------------------------
jws.FileSystemPlugIn={NS:jws.NS_BASE+".plugins.filesystem",ALIAS_PRIVATE:"privateDir",ALIAS_PUBLIC:"publicDir",ALIAS_SESSION:
"sessionDir",ALIAS_USER_SESSION:"uuidDir",processToken:function(L){if(L.ns==jws.FileSystemPlugIn.NS){if("loadByChunks"==L.reqType){
if(L.code==0){if(this.OnChunkLoaded){this.OnChunkLoaded(L);}}}else if("event"==L.type){if("fsdcreated"==L.name){if(
this.OnFSDirectoryCreated){this.OnFSDirectoryCreated(L);}}else if("fsdchanged"==L.name){if(this.OnFSDirectoryChanged){
this.OnFSDirectoryChanged(L);}}else if("fsddeleted"==L.name){if(this.OnFSDirectoryDeleted){this.OnFSDirectoryDeleted(L);}}else if(
"fsfcreated"==L.name){if(this.OnFSFileCreated){this.OnFSFileCreated(L);}}else if("fsfchanged"==L.name){if(this.OnFSFileChanged){
this.OnFSFileChanged(L);}}else if("fsfdeleted"==L.name){if(this.OnFSFileDeleted){this.OnFSFileDeleted(L);}}else if("chunkreceived"==
L.name){if(this.OnChunkReceived){this.OnChunkReceived(L);}}}}},fileSaveByChunks:function(K,O,j,J){var C=this.createDefaultResult();
J=jws.getOptions(J,{encoding:"base64",encode:true,notify:false,scope:jws.SCOPE_PRIVATE});var t={};if(J.encode){t.data=J.encoding;}
if(this.isConnected()){var G={ns:jws.FileSystemPlugIn.NS,type:"saveByChunks",scope:J.scope,encoding:J.encoding,encode:J.encode,
notify:(jws.SCOPE_PUBLIC===J.scope)&&J.notify,data:O,filename:K,isLast:j||false,enc:t};if(J.alias){G.alias=J.alias;}this.sendToken(
G,J);}else{C.code= -1;C.localeKey="jws.jsc.res.notConnected";C.msg="Not connected.";}return C;},fileLoadByChunks:function(K,au,T,v,
J){var C=this.createDefaultResult();if(this.isConnected()){var G={ns:jws.FileSystemPlugIn.NS,type:"loadByChunks",alias:au,filename:
K,offset:T,length:v,encoding:J['encoding']};this.sendToken(G,J);}else{C.code= -1;C.localeKey="jws.jsc.res.notConnected";C.msg=
"Not connected.";}return C;},fileRename:function(K,bV,aM,J){var C=this.checkConnected();if(0==C.code){var G={ns:
jws.FileSystemPlugIn.NS,type:"rename",scope:aM,filename:K,newFilename:bV};this.sendToken(G,J);}return C;},directoryDelete:function(
bb,J){var C=this.checkConnected();if(0==C.code){var G={ns:jws.FileSystemPlugIn.NS,type:"deleteDirectory",directory:bb};
this.sendToken(G,J);}return C;},fsStartObserve:function(J){var C=this.checkConnected();if(0==C.code){var G={ns:
jws.FileSystemPlugIn.NS,type:"startObserve"};this.sendToken(G,J);}return C;},fsStopObserve:function(J){var C=this.checkConnected();
if(0==C.code){var G={ns:jws.FileSystemPlugIn.NS,type:"stopObserve"};this.sendToken(G,J);}return C;},fileSendByChunks:function(bC,K,
O,j,J){var bG=false;var H="base64";var R=true;var V=j||false;if(J){H=J["encoding"]||"base64";if(J.isNode!=undefined){bG=J.isNode;}
if(J.encode!=undefined){R=J.encode;}}var C=this.checkConnected();if(0==C.code){var t={};if(R){t.data=H;}var G={ns:
jws.FileSystemPlugIn.NS,type:"sendByChunks",data:O,enc:t,encode:R,encoding:H,filename:K,isLast:V};if(bG){G.unid=bC;}else{G.targetId=
bC;}this.sendToken(G,J);}return C;},fileUpload:function(k,J){var aF=this;J=jws.getOptions(J,{OnError:function(){},OnComplete:
function(){},OnProgress:function(){},prefix:"",postfix:"",encode:false,encoding:"base64",name:k.name,scope:jws.SCOPE_PRIVATE,
chunkSize:5*1024});try{if(!(k instanceof File)){throw new Error("The 'file' argument require to be a 'File' class object!");}var h=
J.chunkSize;var bo=J.prefix+(J.name)+J.postfix;var aW=new FileReader();aF.fileDelete(bo,true,{});J.bytesSent=J.offset||0;
J.bytesTotal=k.size;J.file=k;var aE=function(T,v){var bU=k.slice(T,T+v);if(J.encode){aW.readAsText(bU);}else{aW.readAsDataURL(bU);}}
;aW.onload=function(bp){var V=J.bytesSent+h>=J.bytesTotal;var bN=bp.target.result;aF.fileSaveByChunks(bo,bN,V,{encoding:J.encoding,
encode:J.encode,scope:J.scope,OnSuccess:function(){J.bytesSent+=(J.bytesSent+h>k.size)?k.size-J.bytesSent:h;J.OnProgress();if(!V){
aE(J.bytesSent,h);}else{J.OnComplete();}},OnFailure:function(L){J.OnError(L);}});};aE(J.bytesSent,h);}catch(aC){J.OnError(aC);}},
setEnterpriseFileSystemCallbacks:function(F){if(!F){F={};}if(F.OnFileLoaded!==undefined){this.OnFileLoaded=F.OnFileLoaded;}if(
F.OnFileSaved!==undefined){this.OnFileSaved=F.OnFileSaved;}if(F.OnFileReceived!==undefined){this.OnFileReceived=F.OnFileReceived;}
if(F.OnFileSent!==undefined){this.OnFileSent=F.OnFileSent;}if(F.OnFileError!==undefined){this.OnFileError=F.OnFileError;}if(
F.OnLocalFileRead!==undefined){this.OnLocalFileRead=F.OnLocalFileRead;}if(F.OnLocalFileError!==undefined){this.OnLocalFileError=
F.OnLocalFileError;}if(F.OnChunkReceived!==undefined){this.OnChunkReceived=F.OnChunkReceived;}if(F.OnChunkLoaded!==undefined){
this.OnChunkLoaded=F.OnChunkLoaded;}if(F.OnFSDirectoryCreated!==undefined){this.OnFSDirectoryCreated=F.OnFSDirectoryCreated;}if(
F.OnFSDirectoryChanged!==undefined){this.OnFSDirectoryChanged=F.OnFSDirectoryChanged;}if(F.OnFSDirectoryDeleted!==undefined){
this.OnFSDirectoryDeleted=F.OnFSDirectoryDeleted;}if(F.OnFSFileDeleted!==undefined){this.OnFSFileDeleted=F.OnFSFileDeleted;}if(
F.OnFSFileCreated!==undefined){this.OnFSFileCreated=F.OnFSFileCreated;}if(F.OnFSFileChanged!==undefined){this.OnFSFileChanged=
F.OnFSFileChanged;}}};jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.FileSystemPlugIn); 