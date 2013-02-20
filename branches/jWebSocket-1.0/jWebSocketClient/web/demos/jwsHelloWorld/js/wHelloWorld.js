$.widget( "jws.helloWorld",{
	_init:function( ) {
		w.helloWorld = this;
        
		w.helloWorld.eMessage = w.helloWorld.element.find( "#message_box_text" );
		w.helloWorld.eFunctionsArea = w.helloWorld.element.find( "#function_area" );
		w.helloWorld.eTokensArea = w.helloWorld.element.find( "#token_area" );
		w.helloWorld.eFilesArea = w.helloWorld.element.find( "#file_area" );
		
		//--------------- BUTTONS --------------------------------
		//Buttons
		w.helloWorld.eBtnBroadcast = w.helloWorld.element.find( "#message_box_broadcast_btn" );
		
		//Tokens buttons
		w.helloWorld.eBtnComplexObject = w.helloWorld.element.find( "#complex_object_btn" );
		w.helloWorld.eBtnGetTime = w.helloWorld.element.find( "#get_time_btn" );
		w.helloWorld.eBtnTokenizable = w.helloWorld.element.find( "#tokenizable_btn" );
		
		//Functions buttons 
		w.helloWorld.eBtnListener	= w.helloWorld.element.find( "#listener_btn" );
		w.helloWorld.eBtnRPC		= w.helloWorld.element.find( "#rpc_btn" );
		w.helloWorld.eBtnSelect	= w.helloWorld.element.find( "#select_btn" );
		
		//File buttons
		w.helloWorld.eBtnSaveFile = w.helloWorld.element.find( "#save_file_btn" );
		w.helloWorld.eBtnFileList = w.helloWorld.element.find( "#file_list_btn" );
		w.helloWorld.eBtnLoadFile = w.helloWorld.element.find( "#load_file_btn" );
		
		w.helloWorld.eBtnTokens = w.helloWorld.element.find( "#tokens" );
		w.helloWorld.eBtnFunctions = w.helloWorld.element.find( "#functions" );
		w.helloWorld.eBtnFiles = w.helloWorld.element.find( "#files" );
		//--------------- BUTTONS --------------------------------
		
		w.helloWorld.eFunctionsArea.hide( );
		w.helloWorld.eFilesArea.hide( );
		w.helloWorld.registerEvents( );
	},
    
	registerEvents: function( ) {
		//registering events
		w.helloWorld.eBtnBroadcast.click( w.helloWorld.broadcast );
		
		//Tokens
		w.helloWorld.eBtnComplexObject.click( w.helloWorld.exchangeComplexObjects );
		w.helloWorld.eBtnGetTime.click( w.helloWorld.sampleGetTime );
		w.helloWorld.eBtnTokenizable.click( w.helloWorld.sampleTokenizable );
		
		//Functions
		w.helloWorld.eBtnListener.click( w.helloWorld.sampleListener );
		w.helloWorld.eBtnRPC.click( w.helloWorld.sampleRPC );
		w.helloWorld.eBtnSelect.click( w.helloWorld.sampleSelect );
		
		//Files
		w.helloWorld.eBtnSaveFile.click( w.helloWorld.saveFile );
		w.helloWorld.eBtnFileList.click( w.helloWorld.getFilelist );
		w.helloWorld.eBtnLoadFile.click( w.helloWorld.loadFile );
        
		//change functional_box_content
		w.helloWorld.eBtnTokens.click( function( ) {
			w.helloWorld.changeContent( "token_area" );
		});
		w.helloWorld.eBtnFunctions.click( function( ) {
			w.helloWorld.changeContent( "function_area" );
		});
		w.helloWorld.eBtnFiles.click( function( ) {
			w.helloWorld.changeContent( "file_area" );
		});

		//other actions
		w.helloWorld.eMessage.blur( w.helloWorld.messageOnBlur );
		w.helloWorld.eMessage.click( w.helloWorld.messageOnClick );
	},
	broadcast: function( ) {
		var lMsg = w.helloWorld.eMessage.val( );
		if(  lMsg.length > 0  ) {
			log(  "Broadcasting '" + lMsg + "'..."  );
			var lRes = mWSC.broadcastText( 
				"",		// broadcast to all clients ( not limited to a certain pool )
				lMsg	// broadcast this message
				 );
			// you may want log error only,
			// on success don't confuse the user
			// if(  lRes.code != 0  ) {
			log(  mWSC.resultToString(  lRes  )  );
		//  }
		//
		// optionally clear message to not accidentally send it twice
		// w.helloWorld.eMessage.value = "";
		}
	},
    
	// example how to exchange arbitrary complex objects between clients
	// the processComplexObject method in the server side sample plug-in
	exchangeComplexObjects: function( ) {
		log(  "Retreiving a complex object from the server via WebSockets..."  );
		if(  mWSC.isConnected( )  ) {
			var lToken = {
				ns: jws.SamplesPlugIn.NS,
				type: "processComplexObject",
				int_val: 1234,
				float_val: 1234.5678,
				bool_val: true,
				date_val: jws.tools.date2ISO(  new Date( )  ),
				object: {
					field1: "value1",
					field2: "value2",
					array1: [ "array1Item1", "array1Item2" ],
					array2: [ "array2Item1", "array2Item2" ],
					object1: {
						obj1field1: "obj1value1", 
						obj1field2: "obj1value2"
					},
					object2: {
						obj2field1: "obj2value1", 
						obj2field2: "obj2value2"
					}
				}
			};
			mWSC.sendToken(  lToken,	{
				});
		} else {
			log(  "Not connected."  );
		}
	},
	// example how to exchange data with a server side listener
	sampleListener: function( ) {
		log(  "Retreiving a token from the server via a jWebSocket listener..."  );
		if(  mWSC.isConnected( )  ) {
			var lToken = {
				// ns: "my.namespace",
				// type: "getInfo"
				ns: "tld.domain.plugins.myplugin",
				type: "mydemo_function"
			};
			mWSC.sendToken(  lToken,	{
				OnResponse: function(  aToken  ) {
					log( "Server responded: "
						+ "vendor: " + aToken.vendor
						+ ", version: " + aToken.version
						 );
				}
			});
		} else {
			log(  "Not connected."  );
		}
	},
	// example how to obtain any tokenizable object fro the server
	sampleTokenizable: function( ) {
		log(  "Retreiving a tokenizable object from the server via a jWebSocket listener..."  );
		if(  mWSC.isConnected( )  ) {
			var lToken = {
				ns: "my.namespace",
				type: "getTokenizable"
			};
			mWSC.sendToken(  lToken,	{
				OnResponse: function(  aToken  ) {
					log( "Server responded: "
						+ "aToken: " + aToken
						 );
				}
			});
		} else {
			log(  "Not connected."  );
		}
	},
	// example how to request a result from a server side plugin
	sampleGetTime: function( ) {
		log(  "Requesting server time via WebSockets..."  );
		// call the getTime method of the client side plug-in
		var lRes = mWSC.requestServerTime( );
		// log error only, on success don't confuse the user
		if(  lRes.code != 0  ) {
			log(  mWSC.resultToString(  lRes  )  );
		}
	},
    
	// example how to request a result from a server side plugin
	sampleRPC: function( ) {
		log(  "Calling RPC via WebSockets..."  );
		// call the getMD5 method of the server
		/*
                        var lRes = mWSC.rpc( 
                                "org.jwebsocket.rpc.sample.SampleRPCLibrary", // class
                                "getMD5", // method
                                [ w.helloWorld.eMessage.value ],  // args
                                {
						
                                }
                         );
         */
		// call the getMD5 method of the server
		var lRes = mWSC.rpc( 
			"org.jwebsocket.rpc.sample.SampleRPCLibrary", // class
			"runListDemo", // method
			[ // args, a list in an array of arguments
			// 1234	// int
			// w.helloWorld.eMessage.value // string
			[ 1, 2, 3, 4, "a", "b", "c", "d" ] // array/list
			],
			{
			}
			 );
		// log error only, on success don't confuse the user
		if(  lRes.code != 0  ) {
			log(  mWSC.resultToString(  lRes  )  );
		}
	},
	// example how to request a result from a server side plugin
	sampleSelect: function( ) {
				
		// example how to request a database 
		// result from a server side plugin
		log(  "Requesting JDBC data via WebSockets..."  );
		// call the getTime method of the client side plug-in
		var lRes = mWSC.jdbcQuerySQL( "select * from demo_master" );
		// log error only, on success don't confuse the user
		if(  lRes.code != 0  ) {
			log(  mWSC.resultToString(  lRes  )  );
		}
				
	/*
                        log(  "Requesting JDBC data via WebSockets..."  );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcSelect( {
                                tables	: [ "demo_master" ],
                                fields	: [ "*" ],
                                orders	: [ "master_id" ],
                                where	: "",
                                group	: "",
                                having	: ""
                        });
                        // log error only, on success don't confuse the user
                        if(  lRes.code != 0  ) {
                                log(  mWSC.resultToString(  lRes  )  );
                        }
         */
	/*
                        log(  "Updating JDBC data via WebSockets..."  );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcUpdate( {
                                table	: "demo_master",
                                fields	: [ "master_string" ],
                                values	: [ "Master Row #1 ( updated )" ],
                                where	: "master_id=1"
                        });
                        // log error only, on success don't confuse the user
                        if(  lRes.code != 0  ) {
                                log(  mWSC.resultToString(  lRes  )  );
                        }
         */
	/*
                        log(  "Inserting JDBC data via WebSockets..."  );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcInsert( {
                                table	: "demo_master",
                                fields	: [ "master_string" ],
                                values	: [ "Master Row #1 ( updated )" ]
                        });
                        // log error only, on success don't confuse the user
                        if(  lRes.code != 0  ) {
                                log(  mWSC.resultToString(  lRes  )  );
                        }
         */
	/*
                        log(  "Deleting JDBC data via WebSockets..."  );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcDelete( {
                                table	: "demo_master",
                                where	: "master_id=6"
                        });
         */
	},
	getFilelist: function( ) {
		log(  "Retrieving file list from the server via WebSockets..."  );
		// call the getFilelist method of the client side plug-in
		var lRes = mWSC.fileGetFilelist( 
			"publicDir", [ "*.*" ],
			{
				recursive: true
			}
			 );
		// log error only, on success don't confuse the user
		if(  lRes.code != 0  ) {
			log(  mWSC.resultToString(  lRes  )  );
		}
	/*
                        log(  "Creating a report via WebSockets..."  );
                        // call the getReports method of the client side plug-in
                        var lRes = mWSC.reportingCreateReport( 
                                "Browser Usage",	// report id
                                null				// report params
                         );
                        // log error only, on success don't confuse the user
                        if(  lRes.code != 0  ) {
                                log(  mWSC.resultToString(  lRes  )  );
                        }
                        log(  "Retrieving reports via WebSockets..."  );
                        // call the getReports method of the client side plug-in
                        var lRes = mWSC.reportingGetReports				( 
                                [ "*.*" ],
                         );
                        // log error only, on success don't confuse the user
                        if(  lRes.code != 0  ) {
                                log(  mWSC.resultToString(  lRes  )  );
                        }
         */
	},
	loadFile: function( ) {
		log(  "Loading a file from the server via WebSockets..."  );
		// call the getTime method of the client side plug-in
		var lRes = mWSC.fileLoad( 
			"test.txt",
			{
				scope: jws.SCOPE_PUBLIC
			}
			 );
		// log error only, on success don't confuse the user
		if(  lRes.code != 0  ) {
			log(  mWSC.resultToString(  lRes  )  );
		}
	},
	saveFile: function( ) {
		log(  "Saving a file from the server via WebSockets..."  );
		// call the getTime method of the client side plug-in
		var lRes = mWSC.fileSave( 
			"test.txt",
			w.helloWorld.eMessage.val( ),
			{
				scope: jws.SCOPE_PUBLIC
			}
			 );
		// log error only, on success don't confuse the user
		if(  lRes.code != 0  ) {
			log(  mWSC.resultToString(  lRes  )  );
		}
	},
	cgiTest: function( ) {
		mWSC.sendToken( {
			ns: "org.jwebsocket.plugins.system",
			type: "send",
			subType: "exec",
			unid: "ssal",
			cmd: "test( )"
		});
	},
	messageOnBlur: function( ) {
		if( w.helloWorld.eMessage.val( ) == "" ) {
			w.helloWorld.eMessage.val( "Type your message..." );
		}
	},
	messageOnClick: function( ) {
		if( w.helloWorld.eMessage.val( ) == "Type your message..." ) {
			w.helloWorld.eMessage.val( "" );
		}
	},
	changeContent: function( name ) {
        
		switch( name ) {
			case "token_area":{
                    
				w.helloWorld.eFunctionsArea.hide( );
				w.helloWorld.eFilesArea.hide( );
				w.helloWorld.eTokensArea.fadeIn( "fast" );
                
				if ( !w.helloWorld.eBtnTokens.hasClass( "pressed" ) )
					w.helloWorld.eBtnTokens.addClass( "pressed" );
				w.helloWorld.eBtnFiles.removeClass( "pressed" );
				w.helloWorld.eBtnFunctions.removeClass( "pressed" );
				break;
			}
			case "function_area":{
				w.helloWorld.eTokensArea.hide( );
				w.helloWorld.eFilesArea.hide( );
				w.helloWorld.eFunctionsArea.fadeIn( "fast" );  
                
				if ( !w.helloWorld.eBtnFunctions.hasClass( "pressed" ) )
					w.helloWorld.eBtnFunctions.addClass( "pressed" );
				w.helloWorld.eBtnFiles.removeClass( "pressed" );
				w.helloWorld.eBtnTokens.removeClass( "pressed" );
				break;
			}
			case "file_area":{
				w.helloWorld.eTokensArea.hide( );
				w.helloWorld.eFunctionsArea.hide( );
				w.helloWorld.eFilesArea.fadeIn( "fast" );
                
				if ( !w.helloWorld.eBtnFiles.hasClass( "pressed" ) )
					w.helloWorld.eBtnFiles.addClass( "pressed" );
				w.helloWorld.eBtnFunctions.removeClass( "pressed" );
				w.helloWorld.eBtnTokens.removeClass( "pressed" );
				break;
			}
			default:
				break;
		}
	}
});

//CALLBACKS
function getServerTimeCallback(  aToken  ) {
	log(  "Server time: " + aToken.time  );
}

function onFileLoadedObs(  aToken  ) {
	log(  "Loaded file: " + aToken.data  );
	w.helloWorld.eMessage.val( aToken.data );
}
function onFileErrorObs(  aToken  ) {
	log(  "Error loading file: " + aToken.msg  );
}
function onFileSavedObs(  aToken  ) {
	var lURL = aToken.url.toLowerCase( );
	if(  lURL.indexOf( ".png" ) > 0
		|| lURL.indexOf( ".jpg" ) > 0
		|| lURL.indexOf( ".jpeg" ) > 0
		|| lURL.indexOf( ".gif" ) > 0 ) {
		var lHTML = "<img src=\"" + aToken.url + "\"/>";
		log(  lHTML  );
	} else {
		log(  "File " + aToken.url + " has been stored on server." );
	}
}