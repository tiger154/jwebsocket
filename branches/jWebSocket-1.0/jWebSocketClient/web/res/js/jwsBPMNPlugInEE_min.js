
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise BPMN Client Plug-In
//	(C) Copyright 2012-2013 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.BPMNPlugIn={NS:jws.NS_BASE+".plugins.bpmn",aQ:function(au,ai,J,aI){J.aT=ai;J.alias=au;J.ns=jws.BPMNPlugIn.NS;J.type='aQ';
this.sendToken(J,aI);},ah:function(J,aI){J.ns=jws.BPMNPlugIn.NS;J.type='ah';this.sendToken(J,aI);},bK:function(ap,be,J,aI){J.ns=
jws.BPMNPlugIn.NS;J.type='bK';J.ad=be;J.key=ap;this.sendToken(J,aI);},ar:function(ap,J,aI){J.ns=jws.BPMNPlugIn.NS;J.type='ar';J.key=
ap;this.sendToken(J,aI);},bJ:function(ap,J,aI){J.ns=jws.BPMNPlugIn.NS;J.type='bJ';J.key=ap;this.sendToken(J,aI);}};
jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.BPMNPlugIn); 