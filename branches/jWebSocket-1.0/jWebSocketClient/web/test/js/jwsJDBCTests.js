//	---------------------------------------------------------------------------
//	jWebSocket JDBC Plug-in test specs (Community Edition, CE)
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


jws.tests.JDBC = {

	title: "JDBC plug-in",
	description: "jWebSocket JDBC plug-in",
	category: "Community Edition",
	enabled: true,
	
	TEST_TABLE: "jwebsocket_automated_test",
	TEST_STRING_1: "This is an automated demo text", 
	TEST_STRING_2: "This is an updated demo text", 

	// this spec tests the jdbc plug-in, creating a temporary table for test purposes
	testCreateTable: function() {
		
		var lSpec = "create table (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native create table...
			jws.Tests.getAdminTestConn().jdbcExecSQL(
				"create table " + jws.tests.JDBC.TEST_TABLE + " (id int, text varchar(80))",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.msg !== undefined );
				},
				lSpec,
				5000 * 5
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
			});

		});
	},
	
	// this spec tests the jdbc plug-in, dropping a temporary table for test purposes
	testDropTable: function() {
		
		var lSpec = "drop table (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native drop table...
			jws.Tests.getAdminTestConn().jdbcExecSQL(
				"drop table " + jws.tests.JDBC.TEST_TABLE,
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.msg !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
			});

		});
	},
	
	// this spec tests the native SQL select function of the JDBC plug-in
	testSelectSQL: function() {
		
		var lSpec = "selectSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native select...
			jws.Tests.getAdminTestConn().jdbcQuerySQL(
				"select * from " + jws.tests.JDBC.TEST_TABLE,
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},

	// this spec tests the native SQL insert function of the JDBC plug-in
	testInsertSQL: function() {
		
		var lSpec = "insertSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native insert...
			jws.Tests.getAdminTestConn().jdbcUpdateSQL(
				"insert into " 
					+ jws.tests.JDBC.TEST_TABLE 
					+ " (id, text) values (1, '" 
					+ jws.tests.JDBC.TEST_STRING_1 + "')",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected[0] ).toEqual( 1 );
			});

		});
	},

	// this spec tests the native SQL update function of the JDBC plug-in
	testUpdateSQL: function() {
		
		var lSpec = "updateSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native update...
			jws.Tests.getAdminTestConn().jdbcUpdateSQL(
				"update " 
				+ jws.tests.JDBC.TEST_TABLE 
				+ " set text = '" + jws.tests.JDBC.TEST_STRING_2 + "'"
				+ " where id = 1",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected[0] ).toEqual( 1 );
			});

		});
	},

	// this spec tests the native SQL delete function of the JDBC plug-in
	testDeleteSQL: function() {
		
		var lSpec = "deleteSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native delete...
			jws.Tests.getAdminTestConn().jdbcUpdateSQL(
				"delete from " 
				+ jws.tests.JDBC.TEST_TABLE 
				+ " where id = 1",
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected[0] ).toEqual( 1 );
			});

		});
	},


	// this spec tests the abstract select function of the JDBC plug-in
	testSelect: function() {
		
		var lSpec = "select (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract select command...
			jws.Tests.getAdminTestConn().jdbcSelect(
				{	tables: [ jws.tests.JDBC.TEST_TABLE ],
					fields: [ "id", "text" ],
					where: "id=1"
				},
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.data.length ).toEqual( 1 );
				expect( lResponse.data[0][1] ).toEqual( jws.tests.JDBC.TEST_STRING_2 );
			});

		});
	},

	// this spec tests the abstract insert function of the JDBC plug-in
	testInsert: function() {
		
		var lSpec = "insert (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract insert command
			jws.Tests.getAdminTestConn().jdbcInsert(
				{	table: jws.tests.JDBC.TEST_TABLE ,
					fields: [ "id", "text" ],
					values: [ 1, jws.tests.JDBC.TEST_STRING_1 ]
				},
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected[0] ).toEqual( 1 );
			});

		});
	},

	// this spec tests the abstract update function of the JDBC plug-in
	testUpdate: function() {
		
		var lSpec = "update (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract update command
			jws.Tests.getAdminTestConn().jdbcUpdate(
				{	table: jws.tests.JDBC.TEST_TABLE ,
					fields: [ "text" ],
					values: [ jws.tests.JDBC.TEST_STRING_2 ],
					where: "id=1"
				},
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected[0] ).toEqual( 1 );
			});

		});
	},

	// this spec tests the abstract delete function of the JDBC plug-in
	testDelete: function() {
		
		var lSpec = "delete (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract delete command
			jws.Tests.getAdminTestConn().jdbcDelete(
				{	table: jws.tests.JDBC.TEST_TABLE,
					where: "id=1"
				},
				{	OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
				expect( lResponse.rowsAffected[0] ).toEqual( 1 );
			});

		});
	},

	// this spec tests the native SQL select function of the JDBC plug-in
	testGetPrimaryKeys: function() {
		
		var lSpec = "getPrimaryKeys (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// try to get 3 new primary keys...
			jws.Tests.getAdminTestConn().jdbcGetPrimaryKeys(
				"sq_pk_system_log",
				{	count: 3,
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonable timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.code !== undefined );
				},
				lSpec,
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				expect( lResponse.values.length ).toEqual( 3 );
			});

		});
	},

	runSpecs: function() {
		// run alls tests within an outer test suite
		
		// create a temporary table (test for DDL commands)
		this.testCreateTable();
		
		// run native tests
		this.testInsertSQL();
		this.testUpdateSQL();
		this.testSelectSQL();
		this.testDeleteSQL();
				
		// run abstract tests
		this.testInsert();
		this.testUpdate();
		this.testSelect();
		this.testDelete();	
		
		// drop the temporary table (test for DDL commands)
		this.testDropTable();
	}
};