
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise BPMN Client Plug-In
//	(C) Copyright 2012-2013 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.BPMNPlugIn={NS:jws.NS_BASE+".plugins.bpmn",bm:function(){return{ns:jws.BPMNPlugIn.NS};},aT:function(au,bk,J){var aB=
this.checkConnected();if(0===aB.code){var G=this.bm();G.bc=bk;G.alias=au;G.type='loadFile';this.sendToken(G,J);}return aB;},ab:
function(J){var aB=this.checkConnected();if(0===aB.code){var G=this.bm();G.type='getAvailableProcesses';this.sendToken(G,J);}
return aB;},aK:function(aq,af,J){var aB=this.checkConnected();if(0===aB.code){var G=this.bm();G.type='startProcessInstance';G.ap=af;
G.key=aq;this.sendToken(G,J);}return aB;},ai:function(aq,J){var aB=this.checkConnected();if(0===aB.code){var G=this.bm();G.type=
'suspendProcess';G.key=aq;this.sendToken(G,J);}return aB;},an:function(aq,J){var aB=this.checkConnected();if(0===aB.code){var G=
this.bm();G.type='getExecutionProcesses';G.key=aq;this.sendToken(G,J);}return aB;},am:function(aq,J){var aB=this.checkConnected();
if(0===aB.code){var G=this.bm();G.type='getTasksByProcessInstance';G.key=aq;this.sendToken(G,J);}return aB;},aP:function(aj,ak,J){
var aB=this.checkConnected();if(0===aB.code){var G=this.bm();G.type='completeTask';G.bM=aj;G.aa=ak;this.sendToken(G,J);}return aB;}}
;jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.BPMNPlugIn); 