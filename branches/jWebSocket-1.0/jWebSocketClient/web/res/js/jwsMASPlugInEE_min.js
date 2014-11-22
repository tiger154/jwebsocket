
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise MAS Client Plug-In
//	(C) Copyright 2012-2014 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.MASPlugIn={NS:jws.NS_BASE+".plugins.mas",masCall:function(au,J){var C=this.checkConnected();if(0===C.code){var G={ns:
jws.MASPlugIn.NS,type:"...",alias:au};this.sendToken(G,J);}return C;}};jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.MASPlugIn); 