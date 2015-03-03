require({
      baseUrl: "./",
      paths: {
        'jquery' : 'resources/camunda/lib/jquery/jquery-1.7.2.min',
        'bpmn/Bpmn' : 'resources/camunda/build/bpmn.min',
      },
      packages: [
        { name: "dojo", location: "resources/camunda/lib/dojo/dojo" },
        { name: "dojox", location: "resources/camunda/lib/dojo/dojox"},
        // provided by build/bpmn.min.js
        // { name: "bpmn", location: "src/bpmn" }
      ]
    });