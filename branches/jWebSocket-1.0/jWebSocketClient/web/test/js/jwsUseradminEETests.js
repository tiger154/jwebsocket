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

jws.tests.UseradminEE = {
	title: "UserAdmin EE plug-in",
	description: "jWebSocket UserAdmin (enterprise edition) plug-in. Designed for generic authentication manager.",
	category: "Enterprise Edition",
	priority: 31,
	NS: jws.NS_BASE + ".plugins.useradmin",
	conn: new jws.jWebSocketJSONClient(),
	DATA: {},
	copyObjectAttrs: function(aCopyTo, aCopyFrom) {
		for (var lKey in aCopyFrom) {
			if (aCopyFrom.hasOwnProperty(lKey)) {
				aCopyTo[lKey] = aCopyFrom[lKey];
			}
		}
		return aCopyTo;
	},
	getConn: function() {
		return this.conn;
	},
	testLogon: function(aUsername, aPassword) {
		var lSpec = "Opening connection and logon with the user: " + aUsername + ".";
		it(lSpec, function() {

// we need to "control" the server to broadcast to all connections here
			if (null === jws.tests.UseradminEE.getConn()) {
				jws.Tests.setAdminConn(new jws.jWebSocketJSONClient());
			}
			var lResponse = {};
			// open a separate control connection
			if (jws.tests.UseradminEE.getConn().isOpened()) {
				jws.tests.UseradminEE.getConn().systemLogon(aUsername, aPassword, {
					OnResponse: function(aToken) {
						lResponse = aToken;
					}
				});
			} else {
				jws.tests.UseradminEE.getConn().open(jws.getDefaultServerURL()
						+ ";sessionCookieName=myUserAdminSession", {
					OnWelcome: function(aToken) {
						jws.tests.UseradminEE.getConn().systemLogon(aUsername, aPassword, {
							OnResponse: function(aToken) {
								lResponse = aToken;
							}
						});
					}
				});
			}

			waitsFor(
					function() {
						return(lResponse.code !== undefined);
					},
					lSpec,
					3000
					);
			runs(function() {
				expect(lResponse.username).toEqual(aUsername);
			});
		});
	},
	testLogout: function() {
		var lSpec = "Logout from the authenticated user.";
		it(lSpec, function() {

			var lResponse = {};
			jws.tests.UseradminEE.getConn().systemLogoff({
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(
					function() {
						return(lResponse.code !== undefined);
					},
					lSpec,
					3000
					);
			runs(function() {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	//**********************************************************************
	// UserClient functionalities
	//**********************************************************************
	testUpdateUser: function(aUser, aExpectedCode) {
		var lSpec = "updateUser (" + aUser + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = aUser;
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "updateUser";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUpdateUserSubscriptions: function(aIdUser, aSubscriptions, aExpectedCode) {
		var lSpec = "UpdateUserSubscriptions (" + aIdUser + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {
				ns: jws.tests.UseradminEE.NS,
				type: "updateUserSubscriptions",
				id: aIdUser,
				subscriptions: aSubscriptions
			};
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testChangePassword: function(aOldPassword, aNewPassword, aExpectedCode) {
		var lSpec = "ChangePassword (" + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {
				ns: jws.tests.UseradminEE.NS,
				type: "changePassword",
				oldPassword: aOldPassword,
				newPassword: aNewPassword
			};
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUsernameExists: function(aUsername, aResponse, aExpectedCode) {
		var lSpec = "UsernameExists (" + aUsername + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {
				ns: jws.tests.UseradminEE.NS,
				type: "usernameExist",
				username: aUsername
			};
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
				if (0 === lResponse.code) {
					expect(aResponse === lResponse.data);
				}
			});
		});
	},
	//**********************************************************************
	// AdminClient functionalities
	//**********************************************************************
	testCreateRight: function(aRight, aExpectedCode) {
		var lSpec = "createRight (" + aRight + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = aRight;
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "createRight";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
					aRight.id = aToken.id;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testRemoveRight: function(aRight, aExpectedCode) {
		var lSpec = "removeRight (" + aRight + ", " + aExpectedCode + ")";
		it(lSpec, function() {
			var lResponse = null;
			var lToken = {id: aRight.id};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "removeRight";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUpdateRight: function(aRight, aUpdate, aExpectedCode) {
		var lSpec = "updateRight (" + aRight + ", " + aUpdate + ", " + aExpectedCode + ")";
		it(lSpec, function() {
			var lResponse = null;
			var lToken = jws.tests.UseradminEE.copyObjectAttrs(aRight, aUpdate);
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "updateRight";

			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testGetRights: function(aStart, aLimit, aExpectedCode) {
		var lSpec = "getRights (" + aStart + ", " + aLimit + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {start: aStart, limit: aLimit};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "getRights";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testCreateRole: function(aRole, aExpectedCode) {
		var lSpec = "createRole (" + aRole + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = aRole;
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "createRole";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
					aRole.id = aToken.id;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testRemoveRole: function(aRole, aExpectedCode) {
		var lSpec = "removeRole (" + aRole + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {id: aRole.id};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "removeRole";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUpdateRole: function(aRole, aExpectedCode) {
		var lSpec = "updateRole (" + aRole + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = aRole;
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "updateRole";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testGetRoles: function(aStart, aLimit, aExpectedCode) {
		var lSpec = "getRoles (" + aStart + ", " + aLimit + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {start: aStart, limit: aLimit};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "getRoles";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUpdateRightsByRole: function(aIdRole, aListRights, aExpectedCode) {
		var lSpec = "updateRightsByRole (" + aIdRole + ", " + aListRights + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {id: aIdRole, listRights: aListRights};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "updateRightsByRole";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testCreateUser: function(aUser, aExpectedCode) {
		var lSpec = "addUser (" + aUser + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = aUser;
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "addUser";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
					aUser.id = aToken.id;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testRemoveUser: function(aUser, aExpectedCode) {
		var lSpec = "removeUser (" + aUser + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {id: aUser.id};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "removeUser";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUpdateUserByAdmin: function(aUser, aExpectedCode) {
		var lSpec = "updateUserByAdmin (" + aUser + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = aUser;
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "updateUserByAdmin";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUpdateUserSubscriptionsByAdmin: function(aIdUser, aSubscriptions, aExpectedCode) {
		var lSpec = "UpdateUserSubscriptionsByAdmin (" + aIdUser + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {
				ns: jws.tests.UseradminEE.NS,
				type: "updateUserSubscriptionsByAdmin",
				id: aIdUser,
				subscriptions: aSubscriptions
			};
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testChangePasswordByAdmin: function(aUsername, aPassword, aExpectedCode) {
		var lSpec = "ChangePasswordByAdmin (" + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {
				ns: jws.tests.UseradminEE.NS,
				type: "changePasswordByAdmin",
				username: aUsername,
				password: aPassword
			};
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testSetUserEnabled: function(aIdUser, aEnabled, aExpectedCode) {
		var lSpec = "setUserEnabled (" + aIdUser + ", " + aEnabled + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {id: aIdUser, enabled: aEnabled};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "setUserEnabled";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testUpdateRolesByUser: function(aIdUser, aListRoles, aExpectedCode) {
		var lSpec = "updateRolesByUser (" + aIdUser + ", " + aListRoles + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {id: aIdUser, listRoles: aListRoles};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "updateRolesByUser";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testGetUser: function(aFieldKey, aFieldValue, aExpectedCode) {
		var lSpec = "getUser (" + aFieldKey + ", " + aFieldValue + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {fieldKey: aFieldKey, fieldValue: aFieldValue};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "getUser";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testGetUsers: function(aStart, aLimit, aExpectedCode) {
		var lSpec = "getUsers (" + aStart + ", " + aLimit + ", " + aExpectedCode + ")";
		it(lSpec, function() {

			var lResponse = null;
			var lToken = {start: aStart, limit: aLimit};
			lToken.ns = jws.tests.UseradminEE.NS;
			lToken.type = "getUsers";
			jws.tests.UseradminEE.getConn().sendToken(lToken, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 3000);
			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	runSpecs: function() {
		//All data needed for UserAdmin tests.
		this.DATA.Right1 = {name: "org.jwebsocket.tests.read"};
		this.DATA.Right2 = {name: "org.jwebsocket.tests.write", description: "Allows test the right."};
		this.DATA.Role = {name: "userAdministrator"};
		this.DATA.User = {
			"firstname": "Firstname",
			"lastname": "Lastname",
			"email": "useradmin@gmail.com",
			"mobile": "+53 405 0000",
			"username": "useradmin",
			"password": "UserAdmin00",
			"securityQuestion": "Color",
			"securityAnswer": "negro",
			"myInterest": "interest_02",
			"subscriptions": [{"subscription": "sub_01", "media": "media_01"}, {"subscription": "sub_02", "media": "media_03"}]
		};
		this.testLogon("alexander", "A.schulze0");

		//creates
		this.testCreateRight(this.DATA.Right1, 0);
		this.testCreateRight(this.DATA.Right2, 0);
		this.testCreateRole(this.DATA.Role, 0);
		this.testCreateUser(this.DATA.User, 0);

		this.testUsernameExists(this.DATA.User.username, true, 0);
		this.testUsernameExists("alexander1", false, 0);

		//updates
		this.testUpdateRight(this.DATA.Right1, {description: this.DATA.Right2.description}, 0);
//		this.testUpdateRole(this.DATA.Role, 0);
//		this.testUpdateUserByAdmin(this.DATA.User, 0);

		//removes
		this.testRemoveRight(this.DATA.Right1, 0);
		this.testRemoveRight(this.DATA.Right2, 0);
		this.testRemoveRole(this.DATA.Role, 0);
		this.testRemoveUser(this.DATA.User, 0);
//		this.testUpdateRightsByRole(lRole.id, [lRight1.id, lRight2.id], 0);

		this.testLogout();
	},
	runSuite: function() {
		var lThis = this;
		describe("Performing test suite: " + jws.tests.UseradminEE.NS + "...", function() {
			lThis.runSpecs();
		});
	}
};