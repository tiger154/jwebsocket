
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise Cluster Admin PlugIn
//	(C) Copyright 2012-2013 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.ClusterAdminPlugInEE={NS:jws.NS_BASE+".plugins.clusteradmin",clusterGetinfo:function(J){var C=this.checkConnected();if(0===
C.code){var G={ns:jws.ClusterAdminPlugInEE.NS,type:"getClusterInfo"};this.sendToken(G,J);}return C;},clusterListNodes:function(J){
var C=this.checkConnected();if(0===C.code){var G={ns:jws.ClusterAdminPlugInEE.NS,type:"listNodes"};this.sendToken(G,J);}return C;},
clusterGetOptimumNode:function(J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.ClusterAdminPlugInEE.NS,type:
"getOptimumNode"};this.sendToken(G,J);}return C;},clusterPauseNode:function(aw,J){var C=this.checkConnected();if(0===C.code){var G={
ns:jws.ClusterAdminPlugInEE.NS,type:"pauseNode",nodeId:aw};this.sendToken(G,J);}return C;},clusterResumeNode:function(aw,J){var C=
this.checkConnected();if(0===C.code){var G={ns:jws.ClusterAdminPlugInEE.NS,type:"resumeNode",nodeId:aw};this.sendToken(G,J);}
return C;},clusterShutdownNode:function(aw,J){var C=this.checkConnected();if(0===C.code){var G={ns:jws.ClusterAdminPlugInEE.NS,type:
"shutdownNode",nodeId:aw};this.sendToken(G,J);}return C;}};jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.ClusterAdminPlugInEE);