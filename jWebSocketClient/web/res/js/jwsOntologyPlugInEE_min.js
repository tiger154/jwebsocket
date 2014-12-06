
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise Ontology Client Plug-In
//	(C) Copyright 2012-2014 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.OntologyPlugIn={NS:jws.NS_BASE+".plugins.ontology",al:"getConcepts",bH:"getIndividuals",ao:"getProperties",bv:"getProperty",af:
"addConcept",bf:"addIndividual",bA:"addDataTypeProperty",ag:"addObjectProperty",aX:"addStatement",av:"removeConcept",bq:
"removeIndividual",aG:"removeProperty",aB:"removeStatement",ontCall:function(au,J){var C=this.checkConnected();if(0===C.code){var G=
{ns:jws.OntologyPlugIn.NS,type:"...",alias:au};this.sendToken(G,J);}return C;},ae:function(au,J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.al,alias:au};this.sendToken(G,J);}return C;},bd:function(au,J){
var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bH,alias:au};this.sendToken(G,J);
}return C;},aS:function(au,aP,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:
jws.OntologyPlugIn.ao,alias:au,ac:aP};this.sendToken(G,J);}return C;},bm:function(au,aA,J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bv,alias:au,bD:aA};this.sendToken(G,J);}return C;},ak:function(au,
ab,bE,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.af,alias:au,bx:ab,as:bE}
;this.sendToken(G,J);}return C;},az:function(au,aP,ab,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,
type:jws.OntologyPlugIn.bf,alias:au,ac:aP,bx:ab};this.sendToken(G,J);}return C;},ax:function(au,aq,bE,J){var C=this.checkConnected()
;if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bA,alias:au,bD:aq,as:bE};this.sendToken(G,J);}return C;},by:
function(au,aq,bE,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.ag,alias:au,
bD:aq,as:bE};this.sendToken(G,J);}return C;},bu:function(au,aq,J){var C=this.checkConnected();if(0===C.code){var G={ns:
jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.aX,alias:au,bD:aq};this.sendToken(G,J);}return C;},bO:function(au,ab,J){var C=
this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.av,alias:au,bx:ab};this.sendToken(G,J);
}return C;},aR:function(au,aP,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:
jws.OntologyPlugIn.bq,alias:au,ac:aP};this.sendToken(G,J);}return C;},bQ:function(au,aq,J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.aG,alias:au,bD:aq};this.sendToken(G,J);}return C;},aZ:function(au,
bc,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.aB,alias:au,ay:bc};
this.sendToken(G,J);}return C;}};jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.OntologyPlugIn); 