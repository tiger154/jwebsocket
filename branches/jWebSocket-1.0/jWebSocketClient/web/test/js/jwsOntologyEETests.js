//	---------------------------------------------------------------------------
//	jWebSocket ItemStorage Plug-in EE test specs (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------

jws.tests.OntologyEE = {
	title: "Ontology PlugIn EE",
	description: "The jWebSocket Enterprise Ontology PlugIn is  web facade for the OWLAPI library.",
	category: "Enterprise Edition",
	priority: 100,
	ontologyAlias: "blankOntology",
	ontologyIRI: null,
	ontology: null,
	buildIRI: function (aShortForm) {
		return function () {
			if (!aShortForm) {
				return null;
			}
			if (aShortForm.indexOf(":") >= 0) {
				return aShortForm;
			}
			return jws.tests.OntologyEE.ontologyIRI + "#" + aShortForm;
		};
	},
	runSpecs: function () {
		var self = this;
		// getting the test case runner function
		var run = jws.Tests.runTC;
		var iri = self.buildIRI;
		var ontAlias = self.ontologyAlias;

		// generating plugin
		run(function () {
			return jws.Tests.getAdminConn();
		}, "getOntologyPlugIn", [], function (aPlugIn) {
			self.ontology = aPlugIn;
			expect(null != self.ontology).toEqual(true);
		});

		// getting ontology IRI
		run(function () {
			return self.ontology;
		}, "getOntologyIRI", [ontAlias], function (aResponse) {
			self.ontologyIRI = aResponse.data;
			expect(null != self.ontologyIRI).toEqual(true);
		});

		run(function () {
			return self.ontology;
		}, "getIRI", [ontAlias, iri("Head"), true], -1);

		run(function () {
			return self.ontology;
		}, "getIRI", [ontAlias, iri("Head"), false]);

		// - Executing API test cases
		run(function () {
			return self.ontology;
		}, "getClasses", [ontAlias]);

		run(function () {
			return self.ontology;
		}, "addClass", [ontAlias, iri("Head"), iri("HumanBodyPart")]);

		run(function () {
			return self.ontology;
		}, "getIRI", [ontAlias, iri("Head"), true]);

		run(function () {
			return self.ontology;
		}, "addClass", [ontAlias, iri("Ear"), iri("Head")]);

		run(function () {
			return self.ontology;
		}, "addClass", [ontAlias, iri("Noise"), iri("Head")]);

		run(function () {
			return self.ontology;
		}, "addClass", [ontAlias, iri("Eye"), iri("Head")]);

		run(function () {
			return self.ontology;
		}, "addClass", [ontAlias, iri("Hair"), iri("Head")]);

		run(function () {
			return self.ontology;
		}, "addClass", [ontAlias, iri("Mouth"), iri("Head")]);

		run(function () {
			return self.ontology;
		}, "addDisjointClassesAxiom", [ontAlias, ["Noise", "Eye", "Hair", "Mouth"]]);

		run(function () {
			return self.ontology;
		}, "getClasses", [ontAlias], function (aResponse) {
			expect(7).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "refreshReasoner", [ontAlias]);

		run(function () {
			return self.ontology;
		}, "getSubClasses", [ontAlias, iri("Head"), true], function (aResponse) {
			expect(5).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "addDataProperty", [ontAlias, iri("size"), null]);

		run(function () {
			return self.ontology;
		}, "addDataProperty", [ontAlias, iri("name"), null]);

		run(function () {
			return self.ontology;
		}, "addDataProperty", [ontAlias, iri("address"), null]);

		run(function () {
			return self.ontology;
		}, "addDisjointDataPropertiesAxiom", [ontAlias, [iri("name"), iri("address")]]);

		run(function () {
			return self.ontology;
		}, "addDataPropertyRangeAxiom", [ontAlias, iri("size"), "xsd:string"]);

		run(function () {
			return self.ontology;
		}, "addDataPropertyDomainAxiom", [ontAlias, iri("size"), "HumanBodyPart"]);

		run(function () {
			return self.ontology;
		}, "addFunctionalDataPropertyAxiom", [ontAlias, iri("size")]);

		run(function () {
			return self.ontology;
		}, "addObjectProperty", [ontAlias, iri("hasPart"), null]);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyRangeAxiom", [ontAlias, iri("hasPart"), "HumanBodyPart"]);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyDomainAxiom", [ontAlias, iri("hasPart"), "HumanBodyPart"]);

		run(function () {
			return self.ontology;
		}, "getObjectProperty", [ontAlias, iri("hasPart")], function (aResponse) {
			expect(aResponse.data.IRI).toEqual(iri("hasPart")());
		});

		run(function () {
			return self.ontology;
		}, "addAnnotationProperty", [ontAlias, iri("description"), "rdfs:comment"]);

		run(function () {
			return self.ontology;
		}, "addAnnotationAssertionAxiom", [ontAlias, iri("HumanBodyPart"), iri("description"), "The human body parts...", "xsd:string"]);

		run(function () {
			return self.ontology;
		}, "addIndividual", [ontAlias, iri("RolandoHead"), iri("Head")]);

		run(function () {
			return self.ontology;
		}, "getAllIndividuals", [ontAlias], function (aResponse) {
			expect(1).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "refreshReasoner", [ontAlias]);

		run(function () {
			return self.ontology;
		}, "removeIndividual", [ontAlias, iri("RolandoHead")]);

		run(function () {
			return self.ontology;
		}, "getAllIndividuals", [ontAlias], function (aResponse) {
			expect(0).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "addIndividual", [ontAlias, iri("RolandoHead"), iri("Head")]);
		run(function () {
			return self.ontology;
		}, "addIndividual", [ontAlias, iri("RolandoEye1"), iri("Eye")]);
		run(function () {
			return self.ontology;
		}, "addIndividual", [ontAlias, iri("RolandoEye2"), iri("Eye")]);
		run(function () {
			return self.ontology;
		}, "addIndividual", [ontAlias, iri("RolandoNoise"), iri("Noise")]);
		run(function () {
			return self.ontology;
		}, "addIndividual", [ontAlias, iri("RolandoMouth"), iri("Mouth")]);
		run(function () {
			return self.ontology;
		}, "addIndividual", [ontAlias, iri("RolandoHair"), iri("Hair")]);

		run(function () {
			return self.ontology;
		}, "addDataPropertyAssertionAxiom", [ontAlias, iri("RolandoHead"), iri("size"), "big", "xsd:string"]);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyAssertionAxiom", [ontAlias, iri("RolandoHead"), iri("hasPart"), iri("RolandoEye1")]);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyAssertionAxiom", [ontAlias, iri("RolandoHead"), iri("hasPart"), iri("RolandoEye2")]);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyAssertionAxiom", [ontAlias, iri("RolandoHead"), iri("hasPart"), iri("RolandoHair")]);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyAssertionAxiom", [ontAlias, iri("RolandoHead"), iri("hasPart"), iri("RolandoNoise")]);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyAssertionAxiom", [ontAlias, iri("RolandoHead"), iri("hasPart"), iri("RolandoMouth")]);

		run(function () {
			return self.ontology;
		}, "addEquivalentClasssAxiom", [ontAlias, ["Head",
				"hasPart some Hair and hasPart exactly 2 Eye and hasPart exactly 1 Mouth and hasPart exactly 1 Noise"]]);

		run(function () {
			return self.ontology;
		}, "refreshReasoner", [ontAlias], function (aResponse) {
			expect(true).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "getInconsistencies", [ontAlias], function (aResponse) {
			expect(0).toEqual(aResponse.data.length);
		});

		run(function () {
			return self.ontology;
		}, "getIndividual", [ontAlias, iri("RolandoHead")], function (aResponse) {
			expect(aResponse.data.objectProperties["hasPart"].indexOf(iri("RolandoEye1")()) >= 0).toEqual(true);
			expect(aResponse.data.objectProperties["hasPart"].indexOf(iri("RolandoEye2")()) >= 0).toEqual(true);
			expect(aResponse.data.dataProperties["size"].indexOf("big") >= 0).toEqual(true);
			expect(aResponse.data.type).toEqual(iri("Head")());
		});

		run(function () {
			return self.ontology;
		}, "getSuperClassesAxioms", [ontAlias, iri("Eye"), true], function (aResponse) {
			expect(2).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "isClassExpressionValid", [ontAlias, "Eye"], function (aResponse) {
			expect(true).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "isClassExpressionValid", [ontAlias, iri("Wrong Class Exression")], function (aResponse) {
			expect(false).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasClass", [ontAlias, iri("Eye")], function (aResponse) {
			expect(true).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasClass", [ontAlias, iri("SuperEye")], function (aResponse) {
			expect(false).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasIndividual", [ontAlias, iri("RolandoHead")], function (aResponse) {
			expect(true).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasIndividual", [ontAlias, iri("Batman")], function (aResponse) {
			expect(false).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasDataProperty", [ontAlias, iri("size")], function (aResponse) {
			expect(true).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasDataProperty", [ontAlias, iri("SuperSize")], function (aResponse) {
			expect(false).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasObjectProperty", [ontAlias, iri("hasPart")], function (aResponse) {
			expect(true).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasObjectProperty", [ontAlias, iri("hasLeg")], function (aResponse) {
			expect(false).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasAnnotationProperty", [ontAlias, iri("description")], function (aResponse) {
			expect(true).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "hasAnnotationProperty", [ontAlias, iri("priority")], function (aResponse) {
			expect(false).toEqual(aResponse.data);
		});

		run(function () {
			return self.ontology;
		}, "getClassAxioms", [ontAlias, iri("Eye")], function (aResponse) {
			expect(2).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "getClassAxioms", [ontAlias, iri("Eye2")], -1);

		run(function () {
			return self.ontology;
		}, "getClassAxiomsAsString", [ontAlias, iri("Eye")]);

		run(function () {
			return self.ontology;
		}, "getClassAxiomsAsString", [ontAlias, iri("Eye2")], -1);

		run(function () {
			return self.ontology;
		}, "getAnnotationPropertyAxioms", [ontAlias, iri("description")]);


		run(function () {
			return self.ontology;
		}, "getAnnotationPropertyAxioms", [ontAlias, iri("description2")], -1);

		run(function () {
			return self.ontology;
		}, "getAnnotationPropertyAxiomsAsString", [ontAlias, iri("description2")], -1);

		run(function () {
			return self.ontology;
		}, "getIndividualAxioms", [ontAlias, iri("RolandoHead")]);

		run(function () {
			return self.ontology;
		}, "getIndividualAxioms", [ontAlias, iri("RolandoHead2")], -1);

		run(function () {
			return self.ontology;
		}, "getIndividualAxiomsAsString", [ontAlias, iri("RolandoHead")]);

		run(function () {
			return self.ontology;
		}, "getIndividualAxiomsAsString", [ontAlias, iri("RolandoHead2")], -1);

		run(function () {
			return self.ontology;
		}, "getClass", [ontAlias, iri("Head")], function (aResponse) {
			expect(aResponse.data).toEqual(iri("Head")());
		});

		// class does not exists
		run(function () {
			return self.ontology;
		}, "getClass", [ontAlias, iri("unexisting_class")], -1);

		// invoking remove*
		run(function () {
			return self.ontology;
		}, "removeIndividual", [ontAlias, iri("RolandoEye1")]);

		run(function () {
			return self.ontology;
		}, "removeIndividual", [ontAlias, iri("RolandoEye2")]);

		run(function () {
			return self.ontology;
		}, "removeIndividual", [ontAlias, iri("RolandoHair")]);

		run(function () {
			return self.ontology;
		}, "removeIndividual", [ontAlias, iri("RolandoNoise")]);

		run(function () {
			return self.ontology;
		}, "removeIndividual", [ontAlias, iri("RolandoMouth")]);

		run(function () {
			return self.ontology;
		}, "removeIndividual", [ontAlias, iri("RolandoHead")]);

		run(function () {
			return self.ontology;
		}, "removeClass", [ontAlias, iri("Ear")]);

		run(function () {
			return self.ontology;
		}, "removeClass", [ontAlias, iri("Noise")]);

		run(function () {
			return self.ontology;
		}, "removeClass", [ontAlias, iri("Eye")]);

		run(function () {
			return self.ontology;
		}, "removeClass", [ontAlias, iri("Hair")]);

		run(function () {
			return self.ontology;
		}, "removeClass", [ontAlias, iri("Mouth")]);

		run(function () {
			return self.ontology;
		}, "removeClass", [ontAlias, iri("Head")]);

		run(function () {
			return self.ontology;
		}, "removeClass", [ontAlias, iri("HumanBodyPart")]);

		run(function () {
			return self.ontology;
		}, "removeProperty", [ontAlias, iri("size"), "data"]);

		run(function () {
			return self.ontology;
		}, "removeProperty", [ontAlias, iri("name"), "data"]);

		run(function () {
			return self.ontology;
		}, "removeProperty", [ontAlias, iri("address"), "data"]);

		run(function () {
			return self.ontology;
		}, "removeProperty", [ontAlias, iri("size"), "data"], -1);

		run(function () {
			return self.ontology;
		}, "removeProperty", [ontAlias, iri("hasPart"), "object"]);

		run(function () {
			return self.ontology;
		}, "removeProperty", [ontAlias, iri("description"), "annotation"]);
	}
};