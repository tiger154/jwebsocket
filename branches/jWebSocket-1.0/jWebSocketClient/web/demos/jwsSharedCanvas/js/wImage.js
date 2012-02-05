//	****************************************************************************
//	jWebSocket Hello World (uses jWebSocket Client and Server)
//	(C) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
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
//	****************************************************************************

/*
 * @author vbarzana
 */
$.widget("jws.image",{
    
	_init:function(){
		w.image   = this;
		w.image.eBoard		= w.image.element.find("#board");
		w.image.eImage		= w.image.element.find("#image");
		
		w.image.eBtnImg		= w.image.element.find("#tab_insert_img");
		w.image.eBtnPaint	= w.image.element.find("#tab_paint");
		
		
		w.image.eImage.hide();
		w.image.eBoard.show();
		
		
		w.image.registerEvents();
		
		var lJWSID = "jWebSocket Canvas";
		lWSC = null;
		w.image.eCanvas = null;
		eStatus = null;
		lIsConnected = false;
		lColor = "#000000";
		CANVAS_ID = "c1";

		var IN = 0;
		var OUT = 1;
		var EVT = 2;
		var SYS = "SYS";
		var USR = null;
		
		var ctx;
		var lPainting = false;
		var lX1 = -1;
		var lY1 = -1;
		
		var eAvg = null;
		var loops = 0;
		var total = 0;
		
		var lImgIdx = 0;
		var lImages = new Array();
		
		lImages[ 0 ] = new Image();
		lImages[ 1 ] = new Image();
		lImages[ 2 ] = new Image();
		lImages[ 3 ] = new Image();
		lImages[ 4 ] = new Image();
		lImages[ 5 ] = new Image();
		lImages[ 6 ] = new Image();
		lImages[ 7 ] = new Image();
		lImages[ 8 ] = new Image();

		lImages[ 0 ].src = "../../res/img/image1.jpg";
		lImages[ 1 ].src = "../../res/img/image2.jpg";
		lImages[ 2 ].src = "../../res/img/image3.jpg";
		lImages[ 3 ].src = "../../res/img/image4.jpg";
		lImages[ 4 ].src = "../../res/img/image5.jpg";
		lImages[ 5 ].src = "../../res/img/image6.jpg";
		lImages[ 6 ].src = "../../res/img/image7.jpg";
		lImages[ 7 ].src = "../../res/img/image8.jpg";
		lImages[ 8 ].src = "../../res/img/image9.jpg";
		
		var lRollingId = 1, lMaxRollingIDs = 9;
	},
	
	
	registerEvents: function(){
		w.image.eBtnImg.click(w.image.showImageArea);
		w.image.eBtnPaint.click(w.image.showPaintArea);
	},

	doOpen: function() {
		// adjust this URL to your jWebSocket server
		var lURL = jws.getDefaultServerURL()
		+ ( frameElement.id ? ";unid=" + frameElement.id : "");

		// try to establish connection to jWebSocket server
		lWSC.logon( lURL, "Guest", "guest", {

			// OnOpen callback
			OnOpen: function( aEvent ) {
				// start keep alive if user selected that option
				lWSC.startKeepAlive({
					interval: 30000
				});
				eStatus.src = "../../images/authenticated.png";
				lIsConnected = true;
			},

			// OnMessage callback
			OnMessage: function( aEvent, aToken ) {
			},

			// OnClose callback
			OnClose: function( aEvent ) {
				eStatus.src = "../../images/disconnected.png";
				lIsConnected = false;
				lWSC.stopKeepAlive();
			}
					
		});
	}, 
	doClose: function() {
		// disconnect automatically logs out!
		lWSC.stopKeepAlive();
		var lRes = lWSC.close({
			// wait a maximum of 3 seconds for server good bye message
			timeout: 3000
		});
	},

	mouseDownLsnr: function( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( lIsConnected ) {
			lPainting = true;
			lX1 = aEvent.clientX - w.image.eCanvas.offsetLeft;
			lY1 = aEvent.clientY - w.image.eCanvas.offsetTop;
		}
	},

	

	mouseMoveLsnr: function ( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( lIsConnected && lPainting ) {
			var lX2 = aEvent.clientX - w.image.eCanvas.offsetLeft;
			var lY2 = aEvent.clientY - w.image.eCanvas.offsetTop;

			loops++;
			start = new Date().getTime();

			lWSC.canvasLine( CANVAS_ID, lX1, lY1, lX2, lY2, {
				color: lColor
			});

			lX1 = lX2;
			lY1 = lY2;

			total += ( new Date().getTime() - start );
			eAvg.innerHTML = ( total / loops + "ms" );
		}
	}, 
	
	mouseUpLsnr: function ( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( lIsConnected && lPainting ) {
			lX2 = aEvent.clientX - w.image.eCanvas.offsetLeft;
			lY2 = aEvent.clientY - w.image.eCanvas.offsetTop;
			lWSC.canvasLine( CANVAS_ID, lX1, lY1, lX2, lY2, {
				color: lColor
			});
			lPainting = false;
		}
	},

	mouseOutLsnr: function ( aEvent ) {
		mouseUpLsnr( aEvent );
	},

	selectColor: function (aColor ) {
		lColor = aColor;
		jws.$( "spanSettings" ).style.borderColor = lColor;
	},

	doClear: function() {
		if( lIsConnected ) {
			lWSC.canvasClear( CANVAS_ID );
		}
	},

	

	paint: function() {
		var lCanvas = document.getElementById( "cnvDemo" );
		lCanvas.clear = true;
		var lContext = lCanvas.getContext( "2d" );
		/*
					for( var lIdx = 0; lIdx < lImages.length; lIdx++ ){
						lImg.src = lImages[ lIdx ];
						lContext.drawImage( lImg, 0, 0 );
					}
		 */
		lContext.drawImage( lImages[ lImgIdx ], 0, 0 );
		if ( lImgIdx >= 8 ) {
			lImgIdx = 0;
		} else {
			lImgIdx++;
		}
	/*
					lRes = lWSC.fileSend(
						// target was passed as optional argument
						// and thus can be used here
						"target2", // Token.args.target,
						"painting", // Token.fileName,
						// lCanvas.toDataURL( "image/jpeg" ),
						lCanvas.toDataURL( "image/png" ),
						{	encoding: "base64",
							isNode: true
						}
					);
		 */
	},

	onFileSentObs: function( aToken ) {
		// console.log( new Date() + ": " + aToken.data.length +  " " + aToken.data.substr(0, 40));
		var lImg = new Image();
		// document.body.appendChild(lImg);
		lImg.src = aToken.data;
		lImg.onload = function() {
			var lCanvas = document.getElementById( "cnvDemo" );
			var lContext = lCanvas.getContext( "2d" );
			lContext.drawImage( lImg, 0, 0 );
		}
	},

	onFileSavedObs: function( aToken ) {
		var lImg = new Image();
		lImg.src = aToken.url;
		lImg.onload = function() {
			var lCanvas = document.getElementById( "cnvDemo" );
			var lContext = lCanvas.getContext( "2d" );
			lContext.drawImage( lImg, 0, 0 );
		}
	},

	snapshot: function() {
		if( lIsConnected ) {
			// png should be supported by all HTML5 compliant browser
			// jpeg may not be supported yet (as of 2011-03-01)
			// by Safari and Opera. Thus, take png as default for now.
			var lRes = lWSC.canvasGetBase64( CANVAS_ID, "image/png" );
			if( lRes.code == 0 ) {
				// the image could be loaded successfully
				// from the canvase element
				var lRes = lWSC.fileSave(
					// use hardcoded file name for now in this
					// demo to keep it as simple as possible
					"canvas_demo_" + lRollingId + ".png",
					// the data is already base64 encoded!
					lRes.data,
					{
						scope: jws.SCOPE_PUBLIC,
						encoding: "base64",
						suppressEncoder: true // data already base64 encoded!
					}
					);
				lRollingId++;
				if( lRollingId > lMaxRollingIDs ) {
					lRollingId = 1;
				}
			} else {
				// an error occured
				alert( lRes.msg );
			}
		}
	},

	initPage: function() {
		// get some required HTML elements
		eAvg = jws.$("spnAvg");
		w.image.eCanvas = jws.$( "can" );
		eStatus = jws.$( "simgStatus" );
		ctx = w.image.eCanvas.getContext( "2d" );

		jws.events.addEventListener( w.image.eCanvas, "mousedown", mouseDownLsnr );
		jws.events.addEventListener( w.image.eCanvas, "mousemove", mouseMoveLsnr );
		jws.events.addEventListener( w.image.eCanvas, "mouseup", mouseUpLsnr );
		jws.events.addEventListener( w.image.eCanvas, "mouseout", mouseOutLsnr );
		
		// check if WebSockets are supported by the browser
		if( jws.browserSupportsWebSockets() ) {
			// instaniate new TokenClient, either JSON, CSV or XML
			lWSC = new jws.jWebSocketJSONClient({
				});
			lWSC.setFileSystemCallbacks({
				OnFileSaved: onFileSavedObs,
				OnFileSent: onFileSentObs
			// OnLocalFileRead: onLocalFileLoadedObs,
			// OnLocalFileError: onLocalFileErrorObs
			});


			lWSC.canvasOpen( CANVAS_ID, "cnvDemo" );
			doOpen();

		} else {
			// jws.$( "sbtnClearLog" ).setAttribute( "disabled", "disabled" );
					
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			alert( lMsg );
		}
	},

	exitPage: function() {
		// this allows the server to release the current session
		// immediately w/o waiting on the timeout.
		if( lWSC ) {
			lWSC.close({
				// force immediate client side disconnect
				timeout: 0
			});
		}
		lWSC.canvasClose( CANVAS_ID );
	},
	
	showImageArea: function(){
		console.log("image");
		w.image.eBtnImg.removeClass("enabled").addClass("enabled");
		w.image.eBtnPaint.removeClass("enabled");
		w.image.eBoard.hide();
		w.image.eImage.show();
	},
	
	showPaintArea: function(){
		console.log("paint");
		w.image.eBtnImg.removeClass("enabled");
		w.image.eBtnPaint.removeClass("enabled").addClass("enabled");
		w.image.eImage.hide();
		w.image.eBoard.show();
	}
});