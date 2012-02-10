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

function dialog(aTitle, aMessage, aIsModal, aCloseFunction){
	// Dialog
	var lDialog = $('<div id="dialog"></div>');
	var lContent = $("<p>"+aMessage + "</p>");
	var lButtonsArea = $("<div class='ui-dialog-buttonpane ui-widget-content ui-helper-clearfix'></div>");
	
	var lButton = $('<div style="float: right;" class="button onmouseup" onmouseover="this.className=\'button onmouseover\'" onmousedown="this.className=\'button onmousedown\'"onmouseup="this.className=\'button onmouseup\'"onmouseout="this.className=\'button onmouseout\'" onclick="this.className=\'button onmouseover\'">');
	lButton.append($('<div class="l"></div>')).append($('<div class="c">Ok</div>')).append($('<div class="r"></div>'));
	lButton.click(function(){
		if(aCloseFunction){
			aCloseFunction();
		}
		lDialog.dialog("close");
		$(".ui-dialog").destroy().remove();
	});
	lButtonsArea.append(lButton);
	lDialog.append(lContent);
	
	lDialog.prependTo("body");
    
	lDialog.dialog({
		autoOpen: true,
		resizable: false,
		modal: aIsModal || false,
		width: 300,
		title: aTitle
	});
	lDialog.append(lButtonsArea);
}