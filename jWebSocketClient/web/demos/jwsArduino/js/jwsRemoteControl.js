// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------

function changeledsStatus(aBlue, aRed, aGreen, aYellow){
  
	if(aBlue){
		gElements.eLedBlue.removeClass("off").addClass("on");
	}
	else{
		gElements.eLedBlue.removeClass("on").addClass("off");
	}
	if(aRed){
		gElements.eLedRed.removeClass("off").addClass("on");
	}
	else{
		gElements.eLedRed.removeClass("on").addClass("off");
	}
	if(aGreen){
		gElements.eLedGreen.removeClass("off").addClass("on");
	}
	else{
		gElements.eLedGreen.removeClass("on").addClass("off");
	}
	if(aYellow){
		gElements.eLedYellow.removeClass("off").addClass("on");
	}
	else{
		gElements.eLedYellow.removeClass("on").addClass("off");
	}
}
  
function changePosition(aX , aY){
	//positioning value of x and y value in the center    
	var lX = 147 - (aX ) * 27;
	var lY = 147 + (aY) * 27;
	var lDistance = Math.sqrt(Math.pow(147 - lX, 2) + Math.pow(147 - lY, 2)   );
   
	if(lDistance <= 138){       
		gPoint.attr("cy", lY);  
		gPoint.attr("cx", lX);   
	}    
}
function showMessage(aMessage, aType){
    
	/* var lMessageBox = $("<div id='messageBox'> </div>");
    switch(aType){
        case 'error':
            lMessageBox.append("<div id='messageType' class='error'></div>");
            break;
        case 'info':
            lMessageBox.append("<div id='messageType' class='info'></div>");
            break;
        case 'alert':
            lMessageBox.append("<div id='messageType' class='alert'></div>");
            break;        
    }
    
    lMessageBox.append("<div id='messageText'>"+aMessage+"</div>");   
    lMessageBox.dialog({
        height: 270,
        width: 400,
        draggable: true,
        modal: true,
        buttons: {
            'Ok': function(event, ui) {
                $(this).dialog('close');
            }
        }
				
    }); */
	var lButtons = [{
		id: "buttonOk",        
		text: "Ok",
		aFunction: function(){
			this.close()
		}
	}];

	dialog(aMessage, "Arduino Remote Control Demo", true, null, lButtons, aType);


}
