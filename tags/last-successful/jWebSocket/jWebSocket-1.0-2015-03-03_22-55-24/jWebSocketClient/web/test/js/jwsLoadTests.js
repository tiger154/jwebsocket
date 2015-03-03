//	---------------------------------------------------------------------------
//	jWebSocket System Plug-in test specs (Community Edition, CE)
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

jws.tests.Load = {
	title: "Load tests",
	description: "jWebSocket server performance tests.",
	category: "Server Benchmarks",
	// this spec tests the speed of a complete client connection to the server
	testConcurrentConnections: function(aAmount, aFlag) {
		var lSpec = "Trying to establish " + aAmount + " concurrent connections...";
		it(lSpec, function() {
			var lConnected = 0;
			var lConns = [];

			if (jws.isIExplorer()) {
				waitsFor(
						function() {
							return(aFlag.running == 0);
						},
						'waiting for previous test round',
						10000
						);

				runs(function() {
					aFlag.running = aAmount;
					for (var lIdx = 0; lIdx < aAmount; lIdx++) {
						lConns[ lIdx ] = new jws.jWebSocketJSONClient();
						lConns[ lIdx ].setParam("connectionIndex", lIdx);
						lConns[ lIdx ].open(jws.getDefaultServerURL(), {
							OnWelcome: function(aToken) {
								lConnected++;
							},
							OnClose: function() {
								aFlag.running--;
							}
						});
					}
				});
			} else {
				for (var lIdx = 0; lIdx < aAmount; lIdx++) {
					lConns[ lIdx ] = new jws.jWebSocketJSONClient();
					lConns[ lIdx ].setParam("connectionIndex", lIdx);
					lConns[ lIdx ].open(jws.getDefaultServerURL(), {
						OnWelcome: function(aToken) {
							lConnected++;
						}
					});
				}
			}
			waitsFor(
					// wait a maximum of 300ms per connection
							function() {
								return(lConnected == aAmount);
							},
							lSpec,
							aAmount * 500
							);

					runs(function() {
						expect(lConnected).toEqual(aAmount);
						for (var lIdx = 0, lCnt = lConns.length; lIdx < lCnt; lIdx++) {
							if (jws.isIExplorer()) {
								lConns[ lIdx ].close({
									fireClose: true
								});
							} else {
								lConns[ lIdx ].close();
							}
						}
					});
				});
	},
	// this spec tests the send method of the system plug-in by sending
	// this spec requires an established connection
	testEcho: function() {
		var lSpec = "Send and Loopback";
		it(lSpec, function() {

			// we need to "control" the server to broadcast to all connections here
			var lResponse = {};
			var lMsg = "This is my message";

			// open a separate control connection
			var lToken = {
				ns: jws.NS_SYSTEM,
				type: "send",
				targetId: jws.Tests.getAdminTestConn().getId(),
				sourceId: jws.Tests.getAdminTestConn().getId(),
				sender: jws.Tests.getAdminTestConn().getUsername(),
				data: lMsg
			};

			var lListener = function(aToken) {
				if ("org.jwebsocket.plugins.system" == aToken.ns
						&& "send" == aToken.type) {
					lResponse = aToken;
				}
			};

			jws.Tests.getAdminTestConn().addListener(lListener);
			jws.Tests.getAdminTestConn().sendToken(lToken);

			waitsFor(
					function() {
						return(lResponse.data == lMsg);
					},
					lSpec,
					1500
					);

			runs(function() {
				expect(lResponse.data).toEqual(lMsg);
				jws.Tests.getAdminTestConn().removeListener(lListener);
			});

		});
	},
	runSpecs: function() {
		// jws.tests.System.testEcho();

		// considering that IE only supports 6 concurrent WebSocket connections
		var lConcurrentConnections = (jws.isIExplorer()) ? (4 - ($('#tls_set').val() === 'wss' ? 2 : 0)) : 10;

		// creating connections
		var lFlag = {running: 0};
		for (var lIndex = 0; lIndex < 10; lIndex++) {
			jws.tests.Load.testConcurrentConnections(lConcurrentConnections, lFlag);
		}
	}
};

