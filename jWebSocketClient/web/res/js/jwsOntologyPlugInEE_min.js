//	---------------------------------------------------------------------------
//	jWebSocket Enterprise Ontology Client Plug-In
//	(C) Copyright 2012-2014 Innotrade GmbH, Herzogenrath Germany
//	Author: Alexander Schulze
//	Author: Rolando Santamaria Maso
//	---------------------------------------------------------------------------

jws.OntologyPlugIn = {
    // namespace for Ontology plugin
    // if namespace is changed update server plug-in accordingly!
    NS: jws.NS_BASE + ".plugins.ontology",
    //:m:*:getOntologyPlugIn
    //:d:en:Get the JavaScript object representation of the OntologyPlugIn
    //:a:en::aOptions.OnSuccess:function:Function to be called when the plugin instance has been generated.
    //:r:*:::void:none
    getOntologyPlugIn: function (aOptions) {
        aOptions.filter = function (aMethodName, aParams) {
            if ("getOntologyAliases" !== aMethodName) {
                aParams.splice(0, 0, "ontologyAlias");
            }
        };

        return this.getActionPlugIn(jws.OntologyPlugIn.NS, aOptions);
    }
};

// add the jWebSocket OntologyPlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.OntologyPlugIn);
