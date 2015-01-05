
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise Ontology Client Plug-In
//	(C) Copyright 2012-2014 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.OntologyPlugIn={NS:jws.NS_BASE+".plugins.ontology",bf:"getConcepts",aH:"getIndividuals",bO:"getProperties",bH:"getProperty",bK:
"addConcept",bI:"addIndividual",aG:"addDataTypeProperty",bq:"addObjectProperty",ae:"addStatement",al:"removeConcept",bE:
"removeIndividual",bF:"removeProperty",av:"removeStatement",ontCall:function(au,J){var C=this.checkConnected();if(0===C.code){var G=
{ns:jws.OntologyPlugIn.NS,type:"...",alias:au};this.sendToken(G,J);}return C;},bx:function(au,J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bf,alias:au};this.sendToken(G,J);}return C;},bd:function(au,J){
var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.aH,alias:au};this.sendToken(G,J);
}return C;},ay:function(au,bn,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:
jws.OntologyPlugIn.bO,alias:au,bQ:bn};this.sendToken(G,J);}return C;},bv:function(au,ba,J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bH,alias:au,aV:ba};this.sendToken(G,J);}return C;},bu:function(au,
aO,bz,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bK,alias:au,bL:aO,aD:bz}
;this.sendToken(G,J);}return C;},by:function(au,bn,aO,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,
type:jws.OntologyPlugIn.bI,alias:au,bQ:bn,bL:aO};this.sendToken(G,J);}return C;},bB:function(au,aS,bz,J){var C=this.checkConnected()
;if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.aG,alias:au,aV:aS,aD:bz};this.sendToken(G,J);}return C;},aX:
function(au,aS,bz,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bq,alias:au,
aV:aS,aD:bz};this.sendToken(G,J);}return C;},ao:function(au,aS,J){var C=this.checkConnected();if(0===C.code){var G={ns:
jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.ae,alias:au,aV:aS};this.sendToken(G,J);}return C;},be:function(au,aO,J){var C=
this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.al,alias:au,bL:aO};this.sendToken(G,J);
}return C;},aI:function(au,bn,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:
jws.OntologyPlugIn.bE,alias:au,bQ:bn};this.sendToken(G,J);}return C;},bA:function(au,aS,J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.bF,alias:au,aV:aS};this.sendToken(G,J);}return C;},bS:function(au,
bP,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.OntologyPlugIn.NS,type:jws.OntologyPlugIn.av,alias:au,aL:bP};
this.sendToken(G,J);}return C;}};jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.OntologyPlugIn); 