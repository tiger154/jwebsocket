//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the JDBC Plug-in
//	(C) 2011 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


jws.tests.JDBC = {

	NS: "jws.tests.jdbc", 
	
	TEST_TABLE: "jwebsocket_automated_test",
	TEST_STRING_1: "This is an automated demo text", 
	TEST_STRING_2: "This is an updated demo text", 

	// this spec tests the jdbc plug-in, creating a temporary table for test purposes
	testCreateTable: function() {
		
		var lSpec = this.NS + ": create table (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native create table...
			jws.Tests.getAdminConn().jdbcExecSQL(
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
				1500
			);

			// check result if ok
			runs( function() {
				expect( lResponse.msg ).toEqual( "ok" );
			});

		});
	},
	
	// this spec tests the jdbc plug-in, dropping a temporary table for test purposes
	testDropTable: function() {
		
		var lSpec = this.NS + ": drop table (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native drop table...
			jws.Tests.getAdminConn().jdbcExecSQL(
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
		
		var lSpec = this.NS + ": selectSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native select...
			jws.Tests.getAdminConn().jdbcQuerySQL(
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
		
		var lSpec = this.NS + ": insertSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native insert...
			jws.Tests.getAdminConn().jdbcUpdateSQL(
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
		
		var lSpec = this.NS + ": updateSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native update...
			jws.Tests.getAdminConn().jdbcUpdateSQL(
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
		
		var lSpec = this.NS + ": deleteSQL (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the native delete...
			jws.Tests.getAdminConn().jdbcUpdateSQL(
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
		
		var lSpec = this.NS + ": select (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract select command...
			jws.Tests.getAdminConn().jdbcSelect(
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
		
		var lSpec = this.NS + ": insert (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract insert command
			jws.Tests.getAdminConn().jdbcInsert(
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
		
		var lSpec = this.NS + ": update (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract update command
			jws.Tests.getAdminConn().jdbcUpdate(
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
		
		var lSpec = this.NS + ": delete (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// perform the abstract delete command
			jws.Tests.getAdminConn().jdbcDelete(
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
		
		var lSpec = this.NS + ": getPrimaryKeys (admin)";
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			// try to get 3 new primary keys...
			jws.Tests.getAdminConn().jdbcGetPrimaryKeys(
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
		
		this.testGetPrimaryKeys();
		
		// drop the temporary table (test for DDL commands)
		this.testDropTable();
	},

	runSuite: function() {
		
		// run alls tests as a separate test suite
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};