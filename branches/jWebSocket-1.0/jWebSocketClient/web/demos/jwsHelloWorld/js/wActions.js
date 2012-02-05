$.widget("jws.actions",{
    _init:function(){
        w.actions=this;
        
        w.actions.eMessage = w.actions.element.find("#message_box_text");
        w.actions.eFunctionsArea = w.actions.element.find("#function_area");
        w.actions.eTokensArea = w.actions.element.find("#token_area");
        w.actions.eFilesArea = w.actions.element.find("#file_area");
		
        //--------------- BUTTONS --------------------------------
        //Buttons
        w.actions.eBtnBroadcast = w.actions.element.find("#message_box_broadcast_btn");
		
		
		
		//Tokens buttons
        w.actions.eBtnComplexObject = w.actions.element.find("#complex_object_btn");
        w.actions.eBtnGetTime = w.actions.element.find("#get_time_btn");
        w.actions.eBtnTokenizable = w.actions.element.find("#tokenizable_btn");
		
		//Functions buttons 
        w.actions.eBtnListener	= w.actions.element.find("#listener_btn");
        w.actions.eBtnRPC		= w.actions.element.find("#rpc_btn");
        w.actions.eBtnSelect	= w.actions.element.find("#select_btn");
		
		//File buttons
        w.actions.eBtnSaveFile = w.actions.element.find("#save_file_btn");
        w.actions.eBtnFileList = w.actions.element.find("#file_list_btn");
        w.actions.eBtnLoadFile = w.actions.element.find("#load_file_btn");
		
        w.actions.eBtnTokens = w.actions.element.find("#tokens");
        w.actions.eBtnFunctions = w.actions.element.find("#functions");
        w.actions.eBtnFiles = w.actions.element.find("#files");
        //--------------- BUTTONS --------------------------------
		
        w.actions.eFunctionsArea.hide();
        w.actions.eFilesArea.hide();
        w.actions.registerEvents();
    },
    
    registerEvents: function(){
        //registering events
        w.actions.eBtnBroadcast.click(w.actions.broadcast);
		
		//Tokens
        w.actions.eBtnComplexObject.click(w.actions.exchangeComplexObjects);
        w.actions.eBtnGetTime.click(w.actions.sampleGetTime);
        w.actions.eBtnTokenizable.click(w.actions.sampleTokenizable);
		
        //Functions
		w.actions.eBtnListener.click(w.actions.sampleListener);
        w.actions.eBtnRPC.click(w.actions.sampleRPC);
        w.actions.eBtnSelect.click(w.actions.sampleSelect);
		
		//Files
        w.actions.eBtnSaveFile.click(w.actions.saveFile);
        w.actions.eBtnFileList.click(w.actions.getFilelist);
        w.actions.eBtnLoadFile.click(w.actions.loadFile);
        
        //change functional_box_content
        w.actions.eBtnTokens.click(function(){
            w.actions.changeContent("token_area")
        });
        w.actions.eBtnFunctions.click(function(){
            w.actions.changeContent("function_area")
        });
        w.actions.eBtnFiles.click(function(){
            w.actions.changeContent("file_area")
        });

        //other actions
        w.actions.eMessage.blur(w.actions.messageOnBlur);
        w.actions.eMessage.click(w.actions.messageOnClick);
    },
    broadcast: function() {
        var lMsg = w.actions.eMessage.val();
        if( lMsg.length > 0 ) {
            log( "Broadcasting '" + lMsg + "'..." );
            var lRes = mWSC.broadcastText(
                "",		// broadcast to all clients (not limited to a certain pool)
                lMsg	// broadcast this message
                );
            // you may want log error only,
            // on success don't confuse the user
            // if( lRes.code != 0 ) {
            log( mWSC.resultToString( lRes ) );
        //  }
        //
        // optionally clear message to not accidentally send it twice
        // w.actions.eMessage.value = "";
        }
    },
    
    // example how to exchange arbitrary complex objects between clients
    // the processComplexObject method in the server side sample plug-in
    exchangeComplexObjects: function() {
        log( "Retreiving a complex object from the server via WebSockets..." );
        if( mWSC.isConnected() ) {
            var lToken = {
                ns: jws.SamplesPlugIn.NS,
                type: "processComplexObject",
                int_val: 1234,
                float_val: 1234.5678,
                bool_val: true,
                date_val: jws.tools.date2ISO( new Date() ),
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
            mWSC.sendToken( lToken,	{
                });
        } else {
            log( "Not connected." );
        }
    },
    // example how to exchange data with a server side listener
    sampleListener: function() {
        log( "Retreiving a token from the server via a jWebSocket listener..." );
        if( mWSC.isConnected() ) {
            var lToken = {
                // ns: "my.namespace",
                // type: "getInfo"
                ns: "tld.domain.plugins.myplugin",
                type: "mydemo_function"
            };
            mWSC.sendToken( lToken,	{
                OnResponse: function( aToken ) {
                    log("Server responded: "
                        + "vendor: " + aToken.vendor
                        + ", version: " + aToken.version
                        );
                }
            });
        } else {
            log( "Not connected." );
        }
    },
    // example how to obtain any tokenizable object fro the server
    sampleTokenizable: function() {
        log( "Retreiving a tokenizable object from the server via a jWebSocket listener..." );
        if( mWSC.isConnected() ) {
            var lToken = {
                ns: "my.namespace",
                type: "getTokenizable"
            };
            mWSC.sendToken( lToken,	{
                OnResponse: function( aToken ) {
                    log("Server responded: "
                        + "aToken: " + aToken
                        );
                }
            });
        } else {
            log( "Not connected." );
        }
    },
    // example how to request a result from a server side plugin
    sampleGetTime: function() {
        log( "Requesting server time via WebSockets..." );
        // call the getTime method of the client side plug-in
        var lRes = mWSC.requestServerTime();
        // log error only, on success don't confuse the user
        if( lRes.code != 0 ) {
            log( mWSC.resultToString( lRes ) );
        }
    },
    
    // example how to request a result from a server side plugin
    sampleRPC: function() {
        log( "Calling RPC via WebSockets..." );
        // call the getMD5 method of the server
        /*
                        var lRes = mWSC.rpc(
                                "org.jwebsocket.rpc.sample.SampleRPCLibrary", // class
                                "getMD5", // method
                                [ w.actions.eMessage.value ],  // args
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
            // w.actions.eMessage.value // string
            [ 1, 2, 3, 4, "a", "b", "c", "d" ] // array/list
            ],
            {
            }
            );
        // log error only, on success don't confuse the user
        if( lRes.code != 0 ) {
            log( mWSC.resultToString( lRes ) );
        }
    },
    // example how to request a result from a server side plugin
    sampleSelect: function() {
				
        // example how to request a database 
        // result from a server side plugin
        log( "Requesting JDBC data via WebSockets..." );
        // call the getTime method of the client side plug-in
        var lRes = mWSC.jdbcQuerySQL("select * from demo_master");
        // log error only, on success don't confuse the user
        if( lRes.code != 0 ) {
            log( mWSC.resultToString( lRes ) );
        }
				
    /*
                        log( "Requesting JDBC data via WebSockets..." );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcSelect({
                                tables	: [ "demo_master" ],
                                fields	: [ "*" ],
                                orders	: [ "master_id" ],
                                where	: "",
                                group	: "",
                                having	: ""
                        });
                        // log error only, on success don't confuse the user
                        if( lRes.code != 0 ) {
                                log( mWSC.resultToString( lRes ) );
                        }
         */
    /*
                        log( "Updating JDBC data via WebSockets..." );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcUpdate({
                                table	: "demo_master",
                                fields	: [ "master_string" ],
                                values	: [ "Master Row #1 (updated)" ],
                                where	: "master_id=1"
                        });
                        // log error only, on success don't confuse the user
                        if( lRes.code != 0 ) {
                                log( mWSC.resultToString( lRes ) );
                        }
         */
    /*
                        log( "Inserting JDBC data via WebSockets..." );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcInsert({
                                table	: "demo_master",
                                fields	: [ "master_string" ],
                                values	: [ "Master Row #1 (updated)" ]
                        });
                        // log error only, on success don't confuse the user
                        if( lRes.code != 0 ) {
                                log( mWSC.resultToString( lRes ) );
                        }
         */
    /*
                        log( "Deleting JDBC data via WebSockets..." );
                        // call the getTime method of the client side plug-in
                        var lRes = mWSC.jdbcDelete({
                                table	: "demo_master",
                                where	: "master_id=6"
                        });
         */
    },
    getFilelist: function() {
        log( "Retrieving file list from the server via WebSockets..." );
        // call the getFilelist method of the client side plug-in
        var lRes = mWSC.fileGetFilelist(
            "publicDir", [ "*.*" ],
            {
                recursive: true
            }
            );
        // log error only, on success don't confuse the user
        if( lRes.code != 0 ) {
            log( mWSC.resultToString( lRes ) );
        }
    /*
                        log( "Creating a report via WebSockets..." );
                        // call the getReports method of the client side plug-in
                        var lRes = mWSC.reportingCreateReport(
                                "Browser Usage",	// report id
                                null				// report params
                        );
                        // log error only, on success don't confuse the user
                        if( lRes.code != 0 ) {
                                log( mWSC.resultToString( lRes ) );
                        }
                        log( "Retrieving reports via WebSockets..." );
                        // call the getReports method of the client side plug-in
                        var lRes = mWSC.reportingGetReports				(
                                [ "*.*" ],
                        );
                        // log error only, on success don't confuse the user
                        if( lRes.code != 0 ) {
                                log( mWSC.resultToString( lRes ) );
                        }
         */
    },
    loadFile: function() {
        log( "Loading a file from the server via WebSockets..." );
        // call the getTime method of the client side plug-in
        var lRes = mWSC.fileLoad(
            "test.txt",
            {
                scope: jws.SCOPE_PUBLIC
            }
            );
        // log error only, on success don't confuse the user
        if( lRes.code != 0 ) {
            log( mWSC.resultToString( lRes ) );
        }
    },
    saveFile: function() {
        log( "Saving a file from the server via WebSockets..." );
        // call the getTime method of the client side plug-in
        var lRes = mWSC.fileSave(
            "test.txt",
            w.actions.eMessage.val(),
            {
                scope: jws.SCOPE_PUBLIC
            }
            );
        // log error only, on success don't confuse the user
        if( lRes.code != 0 ) {
            log( mWSC.resultToString( lRes ) );
        }
    },
    cgiTest: function() {
        mWSC.sendToken({
            ns: "org.jwebsocket.plugins.system",
            type: "send",
            subType: "exec",
            unid: "ssal",
            cmd: "test()"
        });
    },
    messageOnBlur: function(){
        if(w.actions.eMessage.val() == ""){
            w.actions.eMessage.val("Type your message...");
        }
    },
    messageOnClick: function(){
        if(w.actions.eMessage.val() == "Type your message..."){
            w.actions.eMessage.val("");
        }
    },
    changeContent: function(name){
        
        switch(name){
            case "token_area":{
                    
                w.actions.eFunctionsArea.hide();
                w.actions.eFilesArea.hide();
                w.actions.eTokensArea.fadeIn("fast");
                
                if (!w.actions.eBtnTokens.hasClass("pressed"))
                    w.actions.eBtnTokens.addClass("pressed");
                w.actions.eBtnFiles.removeClass("pressed");
                w.actions.eBtnFunctions.removeClass("pressed");
                break;
            }
            case "function_area":{
                w.actions.eTokensArea.hide();
                w.actions.eFilesArea.hide();
                w.actions.eFunctionsArea.fadeIn("fast");  
                
                if (!w.actions.eBtnFunctions.hasClass("pressed"))
                    w.actions.eBtnFunctions.addClass("pressed");
                w.actions.eBtnFiles.removeClass("pressed");
                w.actions.eBtnTokens.removeClass("pressed");
                break;
            }
            case "file_area":{
                w.actions.eTokensArea.hide();
                w.actions.eFunctionsArea.hide();
                w.actions.eFilesArea.fadeIn("fast");
                
                if (!w.actions.eBtnFiles.hasClass("pressed"))
                    w.actions.eBtnFiles.addClass("pressed");
                w.actions.eBtnFunctions.removeClass("pressed");
                w.actions.eBtnTokens.removeClass("pressed");
                break;
            }
            default:
                break;
        }
    }
});

//CALLBACKS
function getServerTimeCallback( aToken ) {
    log( "Server time: " + aToken.time );
}

function onFileLoadedObs( aToken ) {
    log( "Loaded file: " + aToken.data );
    w.actions.eMessage.val(aToken.data);
}
function onFileErrorObs( aToken ) {
    log( "Error loading file: " + aToken.msg );
}
function onFileSavedObs( aToken ) {
    var lURL = aToken.url.toLowerCase();
    if( lURL.indexOf(".png") > 0
        || lURL.indexOf(".jpg") > 0
        || lURL.indexOf(".jpeg") > 0
        || lURL.indexOf(".gif") > 0) {
        var lHTML = "<img src=\"" + aToken.url + "\"/>";
        log( lHTML );
    } else {
        log( "File " + aToken.url + " has been stored on server.");
    }
}