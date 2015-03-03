
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise ANTLR Client Plug-In
//	(C) Copyright 2012-2014 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.ANTLRPlugIn={NS:jws.NS_BASE+".plugins.antlr",antlrCall:function(au,J){var C=this.checkConnected();if(0===C.code){var G={ns:
jws.ANTLRPlugIn.NS,type:"...",alias:au};this.sendToken(G,J);}return C;}};jws.oop.addPlugIn(jws.jWebSocketTokenClient,
jws.ANTLRPlugIn); 