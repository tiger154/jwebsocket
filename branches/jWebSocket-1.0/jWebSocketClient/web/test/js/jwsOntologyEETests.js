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

		// generating plugin
		run(function () {
			return jws.Tests.getAdminConn();
		}, "getOntologyPlugIn", [], 0, function (aPlugIn) {
			self.ontology = aPlugIn;
			expect(null != self.ontology).toEqual(true);
		});

		// getting ontology IRI
		run(function () {
			return self.ontology;
		}, "getOntologyIRI", [self.ontologyAlias], 0, function (aResponse) {
			self.ontologyIRI = aResponse.data;
			expect(null != self.ontologyIRI).toEqual(true);
		});

		// - Executing API test cases
		run(function () {
			return self.ontology;
		}, "getClasses", [self.ontologyAlias], 0);

		run(function () {
			return self.ontology;
		}, "addClass", [self.ontologyAlias, iri("Head"), iri("HumanBodyPart")], 0);

		run(function () {
			return self.ontology;
		}, "addClass", [self.ontologyAlias, iri("Ear"), iri("Head")], 0);

		run(function () {
			return self.ontology;
		}, "addClass", [self.ontologyAlias, iri("Noise"), iri("Head")], 0);

		run(function () {
			return self.ontology;
		}, "addClass", [self.ontologyAlias, iri("Eye"), iri("Head")], 0);

		run(function () {
			return self.ontology;
		}, "addClass", [self.ontologyAlias, iri("Hair"), iri("Head")], 0);

		run(function () {
			return self.ontology;
		}, "addClass", [self.ontologyAlias, iri("Mouth"), iri("Head")], 0);

		run(function () {
			return self.ontology;
		}, "getClasses", [self.ontologyAlias], 0);

		run(function () {
			return self.ontology;
		}, "addDataProperty", [self.ontologyAlias, iri("size"), null], 0);

		run(function () {
			return self.ontology;
		}, "addDataPropertyRangeAxiom", [self.ontologyAlias, iri("size"), null], 0);

		run(function () {
			return self.ontology;
		}, "addDataPropertyDomainAxiom", [self.ontologyAlias, iri("size"), "xsd:string"], 0);
		
		run(function () {
			return self.ontology;
		}, "addFunctionalDataPropertyAxiom", [self.ontologyAlias, iri("size")], 0);

		run(function () {
			return self.ontology;
		}, "addObjectProperty", [self.ontologyAlias, iri("hasPart"), null], 0);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyRangeAxiom", [self.ontologyAlias, iri("hasPart"), "HumanBodyPart"], 0);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyDomainAxiom", [self.ontologyAlias, iri("hasPart"), "HumanBodyPart"], 0);

		run(function () {
			return self.ontology;
		}, "addAnnotationProperty", [self.ontologyAlias, iri("description"), "rdfs:comment"], 0);

		run(function () {
			return self.ontology;
		}, "addAnnotationAssertionAxiom", [self.ontologyAlias, iri("HumanBodyPart"), iri("description"), "The human body parts...", "xsd:string"], 0);

		run(function () {
			return self.ontology;
		}, "addIndividual", [self.ontologyAlias, iri("RolandoHead"), iri("Head")], 0);

		run(function () {
			return self.ontology;
		}, "getIndividuals", [self.ontologyAlias], 0, function (aResponse) {
			expect(1).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "removeIndividual", [self.ontologyAlias, iri("RolandoHead")], 0);

		run(function () {
			return self.ontology;
		}, "getIndividuals", [self.ontologyAlias], 0, function (aResponse) {
			expect(0).toEqual(aResponse.data.rowCount);
		});

		run(function () {
			return self.ontology;
		}, "addIndividual", [self.ontologyAlias, iri("RolandoHead"), iri("Head")], 0);
		run(function () {
			return self.ontology;
		}, "addIndividual", [self.ontologyAlias, iri("RolandoEye1"), iri("Eye")], 0);
		run(function () {
			return self.ontology;
		}, "addIndividual", [self.ontologyAlias, iri("RolandoEye2"), iri("Eye")], 0);

		run(function () {
			return self.ontology;
		}, "addDataPropertyAssertionAxiom", [self.ontologyAlias, iri("RolandoHead"), iri("size"), "big", "xsd:string"], 0);
		
		run(function () {
			return self.ontology;
		}, "addObjectPropertyAssertionAxiom", [self.ontologyAlias, iri("RolandoHead"), iri("hasPart"), iri("RolandoEye1")], 0);

		run(function () {
			return self.ontology;
		}, "addObjectPropertyAssertionAxiom", [self.ontologyAlias, iri("RolandoHead"), iri("hasPart"), iri("RolandoEye2")], 0);

//		// invoking remove*
//		run(function () {
//			return self.ontology;
//		}, "removeIndividual", [self.ontologyAlias, iri("RolandoEye1")], 0);
//		
//		run(function () {
//			return self.ontology;
//		}, "removeIndividual", [self.ontologyAlias, iri("RolandoEye2")], 0);
//		
//		run(function () {
//			return self.ontology;
//		}, "removeIndividual", [self.ontologyAlias, iri("RolandoHead")], 0);
//		
//		run(function () {
//			return self.ontology;
//		}, "removeClass", [self.ontologyAlias, iri("Ear")], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeClass", [self.ontologyAlias, iri("Noise")], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeClass", [self.ontologyAlias, iri("Eye")], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeClass", [self.ontologyAlias, iri("Hair")], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeClass", [self.ontologyAlias, iri("Mouth")], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeClass", [self.ontologyAlias, iri("Head")], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeClass", [self.ontologyAlias, iri("HumanBodyPart")], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeProperty", [self.ontologyAlias, iri("size"), "data"], 0);
//
//		run(function () {
//			return self.ontology;
//		}, "removeProperty", [self.ontologyAlias, iri("size"), "data"], -1);
	}
};