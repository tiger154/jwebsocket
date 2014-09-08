
//	---------------------------------------------------------------------------
//	jWebSocket Enterprise BPMN Client Plug-In
//	(C) Copyright 2012-2013 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	---------------------------------------------------------------------------
jws.BPMNPlugIn={NS:jws.NS_BASE+".plugins.bpmn",ELEMENT_NODE:1,ATTRIBUTE_NODE:2,TEXT_NODE:3,CDATA_SECTION_NODE:4,
ENTITY_REFERENCE_NODE:5,ENTITY_NODE:6,PROCESSING_INSTRUCTION_NODE:7,COMMENT_NODE:8,DOCUMENT_NODE:9,DOCUMENT_TYPE_NODE:10,
DOCUMENT_FRAGMENT_NODE:11,NOTATION_NODE:12,bpmnGet:function(au,J){var C=this.checkConnected();if(0===C.code){var G={ns:
jws.BPMNPlugIn.NS,type:"getBPMN",alias:au};this.sendToken(G,J);}return C;},bpmnGetNodeId:function(bx){if(bx.nodeType===
this.ELEMENT_NODE&&bx.hasAttributes()){var az=bx.attributes.getNamedItem("id");if(null!==az){return az.value;}}return null;},
bpmnGetProcessById:function(bu,aD){var bk=bu.getElementsByTagName("process");if(null!==bk){for(var aO=0;aO<bk.length;aO++){var bd=
bk[aO];if(aD===this.bpmnGetNodeId(bd)){return bd;}}}return null;},bpmnGetChildNodeById:function(aZ,aw){var aX=aZ.childNodes;if(
null!==aX){for(var aO=0;aO<aX.length;aO++){var aS=aX[aO];if(aw===this.bpmnGetNodeId(aS)){return aS;}}}return null;},
bpmnProcessStartNode:function(bx){},bpmnProcessTaskNode:function(bx){},bpmnProcessGatewayNode:function(bx){},bpmnProcessEndNode:
function(bx){},bpmnRunDoc:function(bu,aD,bD){var bd=this.bpmnGetProcessById(bu,aD);if(null!==bd){jws.console.log("Process: "+bd);
var ay=this.bpmnGetChildNodeById(bd,bD);jws.console.log("StartNode: "+ay);}},bpmnRunText:function(aI,aj,bm){var parser=
new DOMParser();try{var be=parser.parseFromString(aI,"text/xml");if(null!==be){this.bpmnRunDoc(be,aj,bm);}else{}}catch(ex){}}};
jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.BPMNPlugIn); 