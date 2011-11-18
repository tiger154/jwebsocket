//	---------------------------------------------------------------------------
//	jWebSocket Sample Client PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
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


//	---------------------------------------------------------------------------
//  jWebSocket Sample Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.JDBCPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.JDBCPlugIn[/tt] class.
jws.JDBCPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.jdbc (jws.NS_BASE + ".plugins.jdbc")
	//:d:en:Namespace for the [tt]JDBCPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.jdbc",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.JDBCPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "selectSQL" == aToken.reqType ) {
				if( this.OnJDBCRowSet ) {
					this.OnJDBCRowSet( aToken );
				}
			}
		}
	},

	jdbcQuerySQL: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "querySQL",
				sql: aQuery
			};
			this.sendToken( lToken, aOptions );
		}
		return lRes;
	},

	jdbcQueryScript: function( aScript, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "querySQL",
				script: aScript
			};
			this.sendToken( lToken, aOptions );
		}
		return lRes;
	},

	jdbcUpdateSQL: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "updateSQL",
				sql: aQuery
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcUpdateScript: function( aScript, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "updateSQL",
				script: aScript
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcExecSQL: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "execSQL",
				sql: aQuery
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcSelect: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lTables = aQuery.tables;
			if( lTables && !lTables.length ) {
				lTables = [ lTables ];
			}
			var lFields = aQuery.fields;
			if( lFields && !lFields.length ) {
				lFields = [ lFields ];
			}
			var lJoins = aQuery.joins;
			if( lJoins && !lJoins.length ) {
				lJoins = [ lJoins ];
			}
			var lOrders = aQuery.orders;
			if( lOrders && !lOrders.length ) {
				lOrders = [ lOrders ];
			}
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "select",
				tables: lTables,
				joins: lJoins,
				fields: lFields,
				orders: lOrders,
				where: aQuery.where,
				group: aQuery.group,
				having: aQuery.having
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcUpdate: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "update",
				table: aQuery.table,
				fields: aQuery.fields,
				values: aQuery.values,
				where: aQuery.where
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcInsert: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "insert",
				table: aQuery.table,
				fields: aQuery.fields,
				values: aQuery.values
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	jdbcDelete: function( aQuery, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "delete",
				table: aQuery.table,
				where: aQuery.where
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},
	
	jdbcGetPrimaryKeys: function( aSequence, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lCount = 1;
			if( aOptions ) {
				if( aOptions.count != undefined ) {
					lCount = aOptions.count;
				}
			}
			var lToken = {
				ns: jws.JDBCPlugIn.NS,
				type: "getNextSeqVal",
				sequence: aSequence,
				count: lCount
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	setJDBCCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnJDBCRowSet !== undefined ) {
			this.OnJDBCRowSet = aListeners.OnJDBCRowSet;
		}
		if( aListeners.OnJDBCResult !== undefined ) {
			this.OnJDBCResult = aListeners.OnJDBCResult;
		}
	}

}

// add the JWebSocket Shared Objects PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.JDBCPlugIn );
