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
    var lX = 135 - (aX ) * 23;
    var lY = 135 + (aY) * 23;
    var lDistance = Math.sqrt(Math.pow(135 - lX, 2) + Math.pow(135 - lY, 2)   );
   
    if(lDistance <= 138){
        gElements.ePosition.css({
            "left": lX,
            "top" : lY
        });
   
    }    
}

function sendCommand(aCmd){	
    gRcPlugin.command({
        args: {            
            cmd: parseInt(aCmd)
        },        
        OnSuccess: function(aResponse){
            if(aResponse.message != null)
                alert(aResponse.message);
        }
    });
}

function startArduinoRemoteControl(){
    gRcPlugin.startrc({       
              OnSuccess: function(aResponse){
          if(aResponse.message != null)
              alert(aResponse.message);
        }					
    });    
}


        

	

