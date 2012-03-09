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
$.widget("jws.image",{
    
	_init:function(){
		wImage    = this;
		//Images
		wImage.image01 = wImage.element.find("#image01_small");
		wImage.image02 = wImage.element.find("#image02_small");
		wImage.image03 = wImage.element.find("#image03_small");
		wImage.image04 = wImage.element.find("#image04_small");
		wImage.image05 = wImage.element.find("#image05_small");
		wImage.image06 = wImage.element.find("#image06_small");
		wImage.image07 = wImage.element.find("#image07_small");
		wImage.image08 = wImage.element.find("#image08_small");
		wImage.image09 = wImage.element.find("#image09_small");
		
		wImage.eBtnClear= wImage.element.find("#clear_image");
		wImage.undo = wImage.element.find("#undo");
		wImage.redo = wImage.element.find("#redo");
            
		wImage.eCanvas = document.getElementById( "image_canvas" );
		wImage.ctx = wImage.eCanvas.getContext( "2d" );

		canvas_w= wImage.eCanvas.width;
		canvas_h= wImage.eCanvas.height;
		
		bg = new Image;	
		picture = new Image;
		
		image01= false;
		image02= false;
		image03= false;
		image04= false;
		image05= false;
		image06= false;
		image07= false;
		image08= false;
		image09= false;
        
		mHistory = [];
		mSteps = 0;	
        
		mPainting = false;
		x1 = -1;
		y1 = -1;
		x2 = "";
		y2 = "";
		w = '';
		h = '';
		
		wImage.registerEvents();
		wImage.listener();
	},
	
	registerEvents: function(){
		$(wImage.eCanvas).mousedown(wImage.mouseDownLsnr);
		$(wImage.eCanvas).mousemove(wImage.mouseMoveLsnr);
		$(wImage.eCanvas).mouseup(wImage.mouseUpLsnr);
		$(wImage.eCanvas).mouseout(wImage.mouseOutLsnr);
		
		wImage.image01.click(wImage.setActive01);
		wImage.image02.click(wImage.setActive02);
		wImage.image03.click(wImage.setActive03);
		wImage.image04.click(wImage.setActive04);
		wImage.image05.click(wImage.setActive05);
		wImage.image06.click(wImage.setActive06);
		wImage.image07.click(wImage.setActive07);
		wImage.image08.click(wImage.setActive08);
		wImage.image09.click(wImage.setActive09);
        
		wImage.eBtnClear.click(wImage.doClear);
	//        wImage.undo.click(wImage.undo);
	//        wImage.undo.click(wImage.redo);
	},
	
	mouseDownLsnr: function( aEvent ) {
		jws.events.preventDefault( aEvent );
		if( mIsConnected ) {
			mPainting = true;
			x1 = aEvent.clientX - wImage.eCanvas.offsetLeft;
			y1 = aEvent.clientY - wImage.eCanvas.offsetTop;
			w=0;
			h=0;
            
			var img = document.createElement("img");
            
			if(image01){
				img.setAttribute("src", "../../res/img/image1.jpg");
			}

			if(image02){
				img.setAttribute("src", "../../res/img/image2.jpg");
			}
            
			if(image03){
				img.setAttribute("src", "../../res/img/image3.jpg");
			}
            
			if(image04){
				img.setAttribute("src", "../../res/img/image4.jpg");
			}
            
			if(image05){
				img.setAttribute("src", "../../res/img/image5.jpg");
			}
            
			if(image06){
				img.setAttribute("src", "../../res/img/image6.jpg");
			}
            
			if(image07){
				img.setAttribute("src", "../../res/img/image7.jpg");
			}
            
			if(image08){
				img.setAttribute("src", "../../res/img/image8.jpg");
			}
            
			if(image09){
				img.setAttribute("src", "../../res/img/image9.jpg");
			}
            
			var args = {
				x: x1,
				y: y1,
				w: w,
				h: h,
				src: img.src
			};
            
			wImage.broadcast(args, "first");
		}
	},
    
	mouseMoveLsnr: function ( aEvent ) {
		// aEvent.preventDefault();
		jws.events.preventDefault( aEvent );
		if( mIsConnected && mPainting ) {
            
			if(mPainting){
				x2 = aEvent.clientX - wImage.eCanvas.offsetLeft;
				y2 = aEvent.clientY - wImage.eCanvas.offsetTop;
                
				if((x2>x1) && (y1>y2)){
					w=x2-x1;
					h=y1-y2;
				}
        
				if((x1>x2) && (y1>y2)){
					w=x1-x2;
					h=y1-y2;
				}
                
				if((x2>x1) && (y2>y1)){
					w=x2-x1;
					h=y2-y1;
				}
                
				if((x1>x2) && (y2>y1)){
					w=x1-x2;
					h=y2-y1;
				}
               
				var args = {
					w: w,
					h: h
				};
                
				wImage.broadcast(args, "moveto");
			}
		}
	}, 
	
	mouseUpLsnr: function ( aEvent ) {
		jws.events.preventDefault( aEvent );
		if( mIsConnected && mPainting ) {
			mPainting = false;
		}
	},
    
	broadcast:function(aArgs, aType){
		$.jws.submit(NS, aType, aArgs);
	},
    
	listener:function(){
		$.jws.bind(NS + ':clearall', function(aEvt, aToken){
			wImage.ctx.fillStyle = 'white';
			wImage.ctx.fillRect(0, 0, canvas_w, canvas_h);
		});
            
		$.jws.bind(NS + ":first", function(aEvt, aToken){
			picture = document.createElement("img");
			picture.setAttribute("src", aToken.src);
			x1 = aToken.x;
			y1 = aToken.y;
			w = aToken.w;
			h = aToken.h;    
			bg.src = wImage.ctx.canvas.toDataURL("image/png");
			mHistory[mSteps] = bg.src;
			mSteps ++;
		});
            
		$.jws.bind(NS + ":moveto", function(aEvt, aToken){
			w = aToken.w;
			h = aToken.h;

			wImage.ctx.clearRect(0, 0, canvas_w, canvas_h);
			wImage.ctx.drawImage(bg, 0, 0, canvas_w, canvas_h);
			wImage.ctx.drawImage(picture, x1, y1, w, h);
                
		});
        
		$.jws.bind(NS + ":undo", function(aEvt, aToken){
			wImage.ctx.clearRect(0, 0, canvas_w, canvas_h);
			lUndo = new Image;
			lUndo.src = mHistory[--mSteps];
                 
			wImage.ctx.drawImage(lUndo, 0, 0, canvas_w, canvas_h);
		});
		$.jws.bind(NS + ":redo", function(aEvt, aToken){
			wImage.ctx.clearRect(0, 0, canvas_w, canvas_h);
			lUndo = new Image;
			lUndo.src = mHistory[++mSteps];
                 
			wImage.ctx.drawImage(lUndo, 0, 0, canvas_w, canvas_h);
		});
        
	},
        
	setActive01: function(){
		image01 = true;
		image02= false;
		image03= false;
		image04= false;
		image05= false;
		image06= false;
		image07= false;
		image08= false;
		image09= false;
	},
        
	setActive02: function(){
		image02 = true;
		image01= false;
		image03= false;
		image04= false;
		image05= false;
		image06= false;
		image07= false;
		image08= false;
		image09= false;
	},
        
	setActive03: function(){
		image03 = true;
		image01= false;
		image02= false;
		image04= false;
		image05= false;
		image06= false;
		image07= false;
		image08= false;
		image09= false;
	},
        
	setActive04: function(){
		image04 = true;
		image01= false;
		image02= false;
		image03= false;
		image05= false;
		image06= false;
		image07= false;
		image08= false;
		image09= false;
	},
        
	setActive05: function(){
		image05 = true;
		image01= false;
		image02= false;
		image03= false;
		image04= false;
		image06= false;
		image07= false;
		image08= false;
		image09= false;
	},
        
	setActive06: function(){
		image06 = true;
		image01= false;
		image02= false;
		image03= false;
		image04= false;
		image05= false;
		image07= false;
		image08= false;
		image09= false;
	},
        
	setActive07: function(){
		image07 = true;
		image01= false;
		image02= false;
		image03= false;
		image04= false;
		image05= false;
		image06= false;
		image08= false;
		image09= false;
	},
        
	setActive08: function(){
		image08 = true;
		image01= false;
		image02= false;
		image03= false;
		image04= false;
		image05= false;
		image06= false;
		image07= false;
		image09= false;
	},
        
	setActive09: function(){
		image09 = true;
		image01= false;
		image02= false;
		image03= false;
		image04= false;
		image05= false;
		image06= false;
		image07= false;
		image08= false;
	},
    
	undo: function (){
		$.jws.submit(NS, "undo");
	},
    
	redo: function (){
		$.jws.submit(NS, "redo");
	},

	mouseOutLsnr: function ( aEvent ) {
		wImage.mouseUpLsnr( aEvent );
	},


	doClear: function() {
		if( mIsConnected ) {
			$.jws.submit(NS, "clearall");
			wImage.ctx.clearRect(0, 0, canvas_w, canvas_h);
		}
	}
});