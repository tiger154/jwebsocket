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
 * @author daimi
 */
$.widget( "jws.paint", {
    
	_init:function() {
		wPaint   = this;
		
		// Getting some elements from the dom
		wPaint.lJWSID		= "jWebSocket Canvas";
		wPaint.eCanvas		= document.getElementById( "paint_canvas" );
		wPaint.ctx			= wPaint.eCanvas.getContext( "2d" );
		wPaint.eBtnClear	= wPaint.element.find("#clear_paint");
		
		color_blue = { 
			elem: wPaint.element.find("#color_blue"),
			color: "#0072bc"
		};
		color_green = { 
			elem: wPaint.element.find("#color_green"),
			color: "#53b369"
		};
		color_red = { 
			elem: wPaint.element.find("#color_red"),
			color: "#c64e4d"
		};
		color_yellow = { 
			elem: wPaint.element.find("#color_yellow"),
			color: "#c9cb4c"
		};
		color_orange = { 
			elem: wPaint.element.find("#color_orange"),
			color: "#be8e44"
		};
		color_darkblue = { 
			elem: wPaint.element.find("#color_darkblue"),
			color: "#273c4d"
		};
		color_purple = { 
			elem: wPaint.element.find("#color_purple"),
			color: "#6864a1"
		};
		color_gray = { 
			elem: wPaint.element.find("#color_gray"),
			color: "#6864a1"
		};
		color_black = { 
			elem: wPaint.element.find("#color_black"),
			color: "#000000"
		};
		
		wPaint.eStatus = null;
		mIsConnected = false;
		wPaint.mColor = "#000000";
		CANVAS_ID = "c1";

		IN = 0;
		OUT = 1;
		EVT = 2;
		SYS = "SYS";
		USR = null;
		
		mPainting = false;
		mX1 = -1;
		mY1 = -1;
		
		wPaint.eAvg = null;
		wPaint.loops = 0;
		wPaint.total = 0;
		
		wPaint.registerEvents();
		wPaint.initPage();
	},
	
	registerEvents: function(){
		$(wPaint.eCanvas).mousedown(wPaint.mouseDownLsnr);
		$(wPaint.eCanvas).mousemove(wPaint.mouseMoveLsnr);
		$(wPaint.eCanvas).mouseup(wPaint.mouseUpLsnr);
		$(wPaint.eCanvas).mouseout(wPaint.mouseOutLsnr);
		wPaint.eBtnClear.click(wPaint.doClear);
		
				color_blue.elem.click(function(){wPaint.selectColor(color_blue.color)});
		color_green.elem.click(function(){wPaint.selectColor(color_green.color)});
		color_red.elem.click(function(){wPaint.selectColor(color_red.color)});
		color_yellow.elem.click(function(){wPaint.selectColor(color_yellow.color)});
		color_orange.elem.click(function(){wPaint.selectColor(color_orange.color)});
		color_darkblue.elem.click(function(){wPaint.selectColor(color_darkblue.color)});
		color_purple.elem.click(function(){wPaint.selectColor(color_purple.color)});
		color_gray.elem.click(function(){wPaint.selectColor(color_gray.color)});
		color_black.elem.click(function(){wPaint.selectColor(color_black.color)});
	},
	
	mouseDownLsnr: function( aEvent ) {
		jws.events.preventDefault( aEvent );
		if( mIsConnected ) {
			mPainting = true;
			mX1 = aEvent.clientX - wPaint.eCanvas.offsetLeft;
			mY1 = aEvent.clientY - wPaint.eCanvas.offsetTop;
		}
	},
	
	mouseMoveLsnr: function ( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( mIsConnected && mPainting ) {
			var lX2 = aEvent.clientX - wPaint.eCanvas.offsetLeft;
			var lY2 = aEvent.clientY - wPaint.eCanvas.offsetTop;

			mWSC.canvasLine( CANVAS_ID, mX1, mY1, lX2, lY2, {
				color: wPaint.mColor
			});

			mX1 = lX2;
			mY1 = lY2;
		}
	}, 
	
	mouseUpLsnr: function ( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( mIsConnected && mPainting ) {
			lX2 = aEvent.clientX - wPaint.eCanvas.offsetLeft;
			lY2 = aEvent.clientY - wPaint.eCanvas.offsetTop;
			mWSC.canvasLine( CANVAS_ID, mX1, mY1, lX2, lY2, {color: wPaint.mColor});
			mPainting = false;
		}
	},

	mouseOutLsnr: function ( aEvent ) {
		wPaint.mouseUpLsnr( aEvent );
	},

	selectColor: function (aColor ) {
		wPaint.mColor = aColor;
	},

	doClear: function() {
		if( mIsConnected ) {
			mWSC.canvasClear( CANVAS_ID );
		}
	},

	initPage: function(){
		mWSC.canvasOpen( CANVAS_ID, "paint_canvas" );
	},
	exitPage: function() {
		mWSC.canvasClose( CANVAS_ID );
	}
});