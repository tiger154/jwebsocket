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
		
		w.image.lJWSID = "jWebSocket Canvas";
		w.image.eCanvas = null;
		w.image.eStatus = null;
		w.image.mIsConnected = false;
		w.image.mColor = "#000000";
		CANVAS_ID = "c1";

		IN = 0;
		OUT = 1;
		EVT = 2;
		SYS = "SYS";
		USR = null;
		
		w.image.ctx = null;
		mPainting = false;
		mX1 = -1;
		mY1 = -1;
		
		w.image.eAvg = null;
		w.image.loops = 0;
		w.image.total = 0;
		
		w.image.mImgIdx = 0;
		w.image.mImages = new Array();
		
		w.image.mImages[ 0 ] = new Image();
		w.image.mImages[ 1 ] = new Image();
		w.image.mImages[ 2 ] = new Image();
		w.image.mImages[ 3 ] = new Image();
		w.image.mImages[ 4 ] = new Image();
		w.image.mImages[ 5 ] = new Image();
		w.image.mImages[ 6 ] = new Image();
		w.image.mImages[ 7 ] = new Image();
		w.image.mImages[ 8 ] = new Image();
		
		w.image.mImages[ 0 ].src = "../../res/img/image1.jpg";
		w.image.mImages[ 1 ].src = "../../res/img/image2.jpg";
		w.image.mImages[ 2 ].src = "../../res/img/image3.jpg";
		w.image.mImages[ 3 ].src = "../../res/img/image4.jpg";
		w.image.mImages[ 4 ].src = "../../res/img/image5.jpg";
		w.image.mImages[ 5 ].src = "../../res/img/image6.jpg";
		w.image.mImages[ 6 ].src = "../../res/img/image7.jpg";
		w.image.mImages[ 7 ].src = "../../res/img/image8.jpg";
		w.image.mImages[ 8 ].src = "../../res/img/image9.jpg";
		
		lRollingId = 1, lMaxRollingIDs = 9;
		
		w.image.initPage();
	},
	
	
	registerEvents: function(){
		w.image.eBtnImg.click(w.image.showImageArea);
		w.image.eBtnPaint.click(w.image.showPaintArea);
	},
	
	mouseDownLsnr: function( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( w.image.mIsConnected ) {
			mPainting = true;
			mX1 = aEvent.clientX - w.image.eCanvas.offsetLeft;
			mY1 = aEvent.clientY - w.image.eCanvas.offsetTop;
		}
	},
	
	mouseMoveLsnr: function ( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( w.image.mIsConnected && mPainting ) {
			var lX2 = aEvent.clientX - w.image.eCanvas.offsetLeft;
			var lY2 = aEvent.clientY - w.image.eCanvas.offsetTop;

			w.image.loops++;
			start = new Date().getTime();

			mWSC.canvasLine( CANVAS_ID, mX1, mY1, lX2, lY2, {
				color: w.image.mColor
			});

			mX1 = lX2;
			mY1 = lY2;

			w.image.total += ( new Date().getTime() - start );
			w.image.eAvg.innerHTML = ( w.image.total / w.image.loops + "ms" );
		}
	}, 
	
	mouseUpLsnr: function ( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( w.image.mIsConnected && mPainting ) {
			lX2 = aEvent.clientX - w.image.eCanvas.offsetLeft;
			lY2 = aEvent.clientY - w.image.eCanvas.offsetTop;
			mWSC.canvasLine( CANVAS_ID, mX1, mY1, lX2, lY2, {
				color: w.image.mColor
			});
			mPainting = false;
		}
	},

	mouseOutLsnr: function ( aEvent ) {
		mouseUpLsnr( aEvent );
	},

	selectColor: function (aColor ) {
		w.image.mColor = aColor;
		jws.$( "spanSettings" ).style.borderColor = w.image.mColor;
	},

	doClear: function() {
		if( w.image.mIsConnected ) {
			mWSC.canvasClear( CANVAS_ID );
		}
	},

	

	paint: function() {
		var lCanvas = document.getElementById( "cnvDemo" );
		lCanvas.clear = true;
		var lContext = lCanvas.getContext( "2d" );
		/*
					for( var lIdx = 0; lIdx < w.image.mImages.length; lIdx++ ){
						lImg.src = w.image.mImages[ lIdx ];
						lContext.drawImage( lImg, 0, 0 );
					}
		 */
		lContext.drawImage( w.image.mImages[ w.image.mImgIdx ], 0, 0 );
		if ( w.image.mImgIdx >= 8 ) {
			w.image.mImgIdx = 0;
		} else {
			w.image.mImgIdx++;
		}
	/*
					lRes = mWSC.fileSend(
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
		if( w.image.mIsConnected ) {
			// png should be supported by all HTML5 compliant browser
			// jpeg may not be supported yet (as of 2011-03-01)
			// by Safari and Opera. Thus, take png as default for now.
			var lRes = mWSC.canvasGetBase64( CANVAS_ID, "image/png" );
			if( lRes.code == 0 ) {
				// the image could be loaded successfully
				// from the canvase element
				var lRes = mWSC.fileSave(
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
	
	doOpen: function() {
		// adjust this URL to your jWebSocket server
		var lURL = jws.getDefaultServerURL()
		+ ( frameElement.id ? ";unid=" + frameElement.id : "");

		// try to establish connection to jWebSocket server
		mWSC.logon( lURL, "Guest", "guest", {

			// OnOpen callback
			OnOpen: function( aEvent ) {
				// start keep alive if user selected that option
				mWSC.startKeepAlive({
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
				mWSC.stopKeepAlive();
			}
					
		});
	},

	doClose: function() {
		// disconnect automatically logs out!
		mWSC.stopKeepAlive();
		var lRes = mWSC.close({
			// wait a maximum of 3 seconds for server good bye message
			timeout: 3000
		});
	},

	initPage: function() {
		// get some required HTML elements
		w.image.eAvg = jws.$("spnAvg");
		w.image.eCanvas = document.getElementById( "can" );
		
		w.image.eStatus = jws.$( "simgStatus" );
		w.image.ctx = w.image.eCanvas.getContext( "2d" );
		
		$(w.image.eCanvas).mousedown(w.image.mouseDownLsnr);
		$(w.image.eCanvas).mousemove(w.image.mouseMoveLsnr);
		$(w.image.eCanvas).mouseup(w.image.mouseUpLsnr);
		$(w.image.eCanvas).mouseout(w.image.mouseOutLsnr);
		
		// check if WebSockets are supported by the browser
		if( jws.browserSupportsWebSockets() ) {
			// instaniate new TokenClient, either JSON, CSV or XML
			mWSC = new jws.jWebSocketJSONClient({
				});
//			mWSC.setFileSystemCallbacks({
//				OnFileSaved: onFileSavedObs,
//				OnFileSent: onFileSentObs
			// OnLocalFileRead: onLocalFileLoadedObs,
			// OnLocalFileError: onLocalFileErrorObs
//			});
			
			mWSC.canvasOpen( CANVAS_ID, "cnvDemo" );

		} else {
			// jws.$( "sbtnClearLog" ).setAttribute( "disabled", "disabled" );
					
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			alert( lMsg );
		}
	},

	exitPage: function() {
		// this allows the server to release the current session
		// immediately w/o waiting on the timeout.
		if( mWSC ) {
			mWSC.close({
				// force immediate client side disconnect
				timeout: 0
			});
		}
		mWSC.canvasClose( CANVAS_ID );
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