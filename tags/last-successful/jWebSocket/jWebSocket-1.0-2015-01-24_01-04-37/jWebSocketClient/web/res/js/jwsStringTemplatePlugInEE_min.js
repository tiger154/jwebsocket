
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise StringTemplate Client Plug-In
//	(C) Copyright 2012-2014 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.StringTemplatePlugIn={NS:jws.NS_BASE+".plugins.stringtemplate",strTemplCall:function(au,J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.StringTemplatePlugIn.NS,type:"...",alias:au};this.sendToken(G,J);}return C;}};jws.oop.addPlugIn(
jws.jWebSocketTokenClient,jws.StringTemplatePlugIn); 