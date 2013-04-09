//	---------------------------------------------------------------------------
//	jWebSocket Shared Canvas Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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


/*
 * @author daimi, vbarzana
 */
$.widget( "jws.image", {
    
	_init:function( ) {
		//Images
		this.eToolImg01		= this.element.find( "#image01_small" );
		this.eToolImg02		= this.element.find( "#image02_small" );
		this.eToolImg03		= this.element.find( "#image03_small" );
		this.eToolImg04		= this.element.find( "#image04_small" );
		this.eToolImg05		= this.element.find( "#image05_small" );
		this.eToolImg06		= this.element.find( "#image06_small" );
		this.eToolImg07		= this.element.find( "#image07_small" );
		this.eToolImg08		= this.element.find( "#image08_small" );
		this.eToolImg09		= this.element.find( "#image09_small" );
		
		this.eBtnClear		= this.element.find( "#clear_image" );
		this.undo			= this.element.find( "#undo" );
		this.redo			= this.element.find( "#redo" );
		this.eCanvas		= document.getElementById( "image_canvas" );
		this.ctx			= this.eCanvas.getContext( "2d" );
		
		this.canvasWidth	= this.eCanvas.width;
		this.canvasHeight	= this.eCanvas.height;
		
		this.mBgImage		= new Image;	
		this.mAuxImage		= new Image;
		this.mActiveImage	= "1";
        
		mHistory = [];
		mSteps = 0;	
        
		this.mIsDrawing = false;
		x1 = -1;
		y1 = -1;
		x2 = "";
		y2 = "";
		mWidth = '';
		mHeight = '';
		
		w.img    = this;
		w.img.registerEvents( );
		w.img.listener( );
	},
	
	registerEvents: function( ) {
		$( w.img.eCanvas ).mousedown( w.img.mouseDownLsnr );
		$( w.img.eCanvas ).mousemove( w.img.mouseMoveLsnr );
		$( w.img.eCanvas ).mouseup( w.img.mouseUpLsnr );
		$( w.img.eCanvas ).mouseout( w.img.mouseOutLsnr );
		
		w.img.eToolImg01.click( function( ) {
			w.img.setActiveImage( 1 );
		});
		w.img.eToolImg02.click( function( ) {
			w.img.setActiveImage( 2 );
		});
		w.img.eToolImg03.click( function( ) {
			w.img.setActiveImage( 3 );
		});
		w.img.eToolImg04.click( function( ) {
			w.img.setActiveImage( 4 );
		});
		w.img.eToolImg05.click( function( ) {
			w.img.setActiveImage( 5 );
		});
		w.img.eToolImg06.click( function( ) {
			w.img.setActiveImage( 6 );
		});
		w.img.eToolImg07.click( function( ) {
			w.img.setActiveImage( 7 );
		});
		w.img.eToolImg08.click( function( ) {
			w.img.setActiveImage( 8 );
		});
		w.img.eToolImg09.click( function( ) {
			w.img.setActiveImage( 9 );
		});
        
		w.img.eBtnClear.click( w.img.doClear );
		//        w.img.undo.click( w.img.undo );
		//        w.img.undo.click( w.img.redo );
	},
	
	mouseDownLsnr: function( aEvent ) {
		aEvent.preventDefault( );
		//		mWSC.events.preventDefault( aEvent );
		if( mWSC.isConnected( ) ) {
			w.img.mIsDrawing = true;
			x1 = aEvent.clientX - w.img.eCanvas.offsetLeft;
			y1 = aEvent.clientY - w.img.eCanvas.offsetTop;
			mWidth=0;
			mHeight=0;
            
			var img = document.createElement( "img" );
            
			img.setAttribute( "src", "../../res/img/image" + 
				w.img.mActiveImage + ".jpg" );
			
			var args = {
				x: x1,
				y: y1,
				mWidth: mWidth,
				mHeight: mHeight,
				src: img.src
			};
            
			w.img.broadcast( args, "first" );
		}
	},
    
	mouseMoveLsnr: function ( aEvent ) {
		aEvent.preventDefault( );
		if( mWSC.isConnected( ) && w.img.mIsDrawing ) {
            
			if( w.img.mIsDrawing ) {
				x2 = aEvent.clientX - w.img.eCanvas.offsetLeft;
				y2 = aEvent.clientY - w.img.eCanvas.offsetTop;
                
				if( ( x2>x1 ) && ( y1>y2 ) ) {
					mWidth=x2-x1;
					mHeight=y1-y2;
				}
        
				if( ( x1>x2 ) && ( y1>y2 ) ) {
					mWidth=x1-x2;
					mHeight=y1-y2;
				}
                
				if( ( x2>x1 ) && ( y2>y1 ) ) {
					mWidth=x2-x1;
					mHeight=y2-y1;
				}
                
				if( ( x1>x2 ) && ( y2>y1 ) ) {
					mWidth=x1-x2;
					mHeight=y2-y1;
				}
               
				var args = {
					mWidth: mWidth,
					mHeight: mHeight
				};
                
				w.img.broadcast( args, "moveto" );
			}
		}
	}, 
	
	mouseUpLsnr: function ( aEvent ) {
		aEvent.preventDefault( );
		//		mWSC.events.preventDefault( aEvent );
		if( mWSC.isConnected( ) && w.img.mIsDrawing ) {
			w.img.mIsDrawing = false;
		}
	},
    
	broadcast:function( aArgs, aType ) {
		$.jws.submit( NS, aType, aArgs );
	},
    
	listener:function( ) {
		$.jws.bind( NS + ':clearall', function( aEvt, aToken ) {
			w.img.ctx.fillStyle = 'white';
			w.img.ctx.fillRect( 0, 0, w.img.canvasWidth, w.img.canvasHeight );
		});
            
		$.jws.bind( NS + ":first", function( aEvt, aToken ) {
			w.img.mAuxImage = document.createElement( "img" );
			w.img.mAuxImage.setAttribute( "src", aToken.src );
			x1 = aToken.x;
			y1 = aToken.y;
			mWidth = aToken.mWidth;
			mHeight = aToken.mHeight;    
			w.img.mBgImage.src = w.img.ctx.canvas.toDataURL( "image/png" );
			mHistory[mSteps] = w.img.mBgImage.src;
			mSteps ++;
		});
            
		$.jws.bind( NS + ":moveto", function( aEvt, aToken ) {
			mWidth = aToken.mWidth;
			mHeight = aToken.mHeight;
			try{
				w.img.ctx.clearRect( 0, 0, w.img.canvasWidth, w.img.canvasHeight );
				w.img.ctx.drawImage( w.img.mBgImage, 0, 0, w.img.canvasWidth, w.img.canvasHeight );
				w.img.ctx.drawImage( w.img.mAuxImage, x1, y1, mWidth, mHeight );
			} catch( lException ) {
				jws.console.log( lException );
			}
		});
        
		$.jws.bind( NS + ":undo", function( aEvt, aToken ) {
			w.img.ctx.clearRect( 0, 0, w.img.canvasWidth, w.img.canvasHeight );
			lUndo = new Image;
			lUndo.src = mHistory[--mSteps];
                 
			w.img.ctx.drawImage( lUndo, 0, 0, w.img.canvasWidth, w.img.canvasHeight );
		});
		$.jws.bind( NS + ":redo", function( aEvt, aToken ) {
			w.img.ctx.clearRect( 0, 0, w.img.canvasWidth, w.img.canvasHeight );
			lUndo = new Image;
			lUndo.src = mHistory[++mSteps];
                 
			w.img.ctx.drawImage( lUndo, 0, 0, w.img.canvasWidth, w.img.canvasHeight );
		});
        
	},
        
	setActiveImage: function( aNumber ) {
		w.img.mActiveImage = aNumber;
	},
	
    
	undo: function ( ) {
		$.jws.submit( NS, "undo" );
	},
    
	redo: function ( ) {
		$.jws.submit( NS, "redo" );
	},

	mouseOutLsnr: function ( aEvent ) {
		w.img.mouseUpLsnr( aEvent );
	},


	doClear: function( ) {
		if( mWSC.isConnected( ) ) {
			$.jws.submit( NS, "clearall" );
			w.img.ctx.clearRect( 0, 0, w.img.canvasWidth, w.img.canvasHeight );
		}
	}
});