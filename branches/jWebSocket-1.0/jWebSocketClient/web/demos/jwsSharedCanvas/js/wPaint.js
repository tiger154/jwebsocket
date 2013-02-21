//	****************************************************************************
//	jWebSocket Hello World ( uses jWebSocket Client and Server )
//	( C ) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or ( at your
//	option ) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	****************************************************************************

/*
 * @author daimi
 */
$.widget( "jws.paint", { 
    
	_init:function( ) {
		// Getting some elements from the dom
		this.lJWSID			= "jWebSocket Canvas";
		this.eCanvas		= document.getElementById( "paint_canvas" );
		this.ctx			= this.eCanvas.getContext( "2d" );
		this.eBtnClear		= this.element.find( "#clear_paint" );
		
		this.color = {
			blue : {  
				elem: this.element.find( "#color_blue" ),
				color: "#0072bc"
			},
			green : {  
				elem: this.element.find( "#color_green" ),
				color: "#53b369"
			}
			,
			red : {  
				elem: this.element.find( "#color_red" ),
				color: "#c64e4d"
			}
			,
			yellow : {  
				elem: this.element.find( "#color_yellow" ),
				color: "#c9cb4c"
			}
			,
			orange : {  
				elem: this.element.find( "#color_orange" ),
				color: "#be8e44"
			}
			,
			darkblue : {  
				elem: this.element.find( "#color_darkblue" ),
				color: "#273c4d"
			}
			,
			purple : {  
				elem: this.element.find( "#color_purple" ),
				color: "#6864a1"
			}
			,
			gray : {  
				elem: this.element.find( "#color_gray" ),
				color: "#6864a1"
			}
			,
			black : {  
				elem: this.element.find( "#color_black" ),
				color: "#000000"
			}
		}
		
		this.eStatus = null;
		this.mColor = "#000000";
		CANVAS_ID = "c1";

		IN = 0;
		OUT = 1;
		EVT = 2;
		SYS = "SYS";
		USR = null;
		
		this.mIsPainting = false;
		mX1 = -1;
		mY1 = -1;
		
		this.eAvg = null;
		this.loops = 0;
		this.total = 0;
		
		w.paint   = this;
		w.paint.registerEvents( );
		w.paint.initPage( );
	},
	
	registerEvents: function( ) {
		$( w.paint.eCanvas ).mousedown( w.paint.mouseDownLsnr );
		$( w.paint.eCanvas ).mousemove( w.paint.mouseMoveLsnr );
		$( w.paint.eCanvas ).mouseup( w.paint.mouseUpLsnr );
		$( w.paint.eCanvas ).mouseout( w.paint.mouseOutLsnr );
		w.paint.eBtnClear.click( w.paint.doClear );
		
		w.paint.color.blue.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.blue.color );
		});
		w.paint.color.green.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.green.color );
		});
		w.paint.color.red.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.red.color );
		});
		w.paint.color.yellow.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.yellow.color );
		});
		w.paint.color.orange.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.orange.color );
		});
		w.paint.color.darkblue.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.darkblue.color );
		});
		w.paint.color.purple.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.purple.color );
		});
		w.paint.color.gray.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.gray.color );
		});
		w.paint.color.black.elem.click( function( ) {
			w.paint.selectColor( w.paint.color.black.color );
		});
	},
	
	mouseDownLsnr: function( aEvent ) {
		jws.events.preventDefault( aEvent );
		if( mWSC.isConnected( ) ) {
			w.paint.mIsPainting = true;
			mX1 = aEvent.clientX - w.paint.eCanvas.offsetLeft;
			mY1 = aEvent.clientY - w.paint.eCanvas.offsetTop;
		}
	},
	
	mouseMoveLsnr: function ( aEvent ) {
		aEvent.preventDefault( );
		if( mWSC.isConnected( ) && w.paint.mIsPainting ) {
			var lX2 = aEvent.clientX - w.paint.eCanvas.offsetLeft;
			var lY2 = aEvent.clientY - w.paint.eCanvas.offsetTop;

			mWSC.canvasLine( CANVAS_ID, mX1, mY1, lX2, lY2, { 
				color: w.paint.mColor
			});

			mX1 = lX2;
			mY1 = lY2;
		}
	}, 
	
	mouseUpLsnr: function ( aEvent ) {
		// aEvent.preventDefault( );
		jws.events.preventDefault( aEvent );
		if( mWSC.isConnected( ) && w.paint.mIsPainting ) {
			lX2 = aEvent.clientX - w.paint.eCanvas.offsetLeft;
			lY2 = aEvent.clientY - w.paint.eCanvas.offsetTop;
			mWSC.canvasLine( CANVAS_ID, mX1, mY1, lX2, lY2, {
				color: w.paint.mColor
			});
			w.paint.mIsPainting = false;
		}
	},

	mouseOutLsnr: function ( aEvent ) {
		w.paint.mouseUpLsnr( aEvent );
	},

	selectColor: function ( aColor ) {
		w.paint.mColor = aColor;
	},

	doClear: function( ) {
		if( mWSC.isConnected( ) ) {
			mWSC.canvasClear( CANVAS_ID );
		}
	},

	initPage: function( ) {
		mWSC.canvasOpen( CANVAS_ID, "paint_canvas" );
	},
	exitPage: function( ) {
		mWSC.canvasClose( CANVAS_ID );
	}
});