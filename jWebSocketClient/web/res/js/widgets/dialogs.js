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
 * @author vbarzana
 */
/**
 * @function jwsDialog
 * @example
 *	jwsDialog( "Are you sure?", "title", true, "error", 
 *			function( ) {alert( "closing" )}, lButtons );
 * @param aTitle
 * @param aMessage
 * @param aIsModal
 * @param aIconType 
 *	 Optional, { error|information|alert|important|warning }
 * @param aCloseFunction
 * @param aMoreButtons
 *   var lButtons = [{
 *		id: "buttonYes",
 *		text: "Yes",
 *		aFunction: function( ) {
 *			alert( "you clicked YES button" );
 *		}
 *	},{
 *		id: "buttonNo",
 *		text: "No",
 *		aFunction: function( ) {
 *			alert( "you clicked button NO" );
 *		}
 *	}];
 * This is an example of how to use this function:
 * @param aWidth Optional, the width of the dialog window
 */
function jwsDialog( aMessage, aTitle, aIsModal, aIconType, aCloseFunction, aMoreButtons, aWidth ) {
	var lDialog = $(  "<div id='dialog'></div>"  ).css({overflow:'hidden'}), 
	lContentWidth = aIconType?"80%":"99%",
	lContent = $(  "<div><p>" + aMessage + "</p></div>"  ).css({
		"width": lContentWidth,
		"float": "left"
	}),
	lButtonsArea = $( "<div class='ui-dialog-buttonpane "+ 
		"ui-widget-content ui-helper-clearfix'></div>" ),
	lButton = $( '<div style="float: right;" '+
		'class="button onmouseup" '+
		'onmouseover="this.className=\'button onmouseover\'" '+
		'onmousedown="this.className=\'button onmousedown\'" ' +
		'onmouseup="this.className=\'button onmouseup\'"' + 
		'onmouseout="this.className=\'button onmouseout\'" ' + 
		'onclick="this.className=\'button onmouseover\'">' );
	
	if( aMoreButtons ) {
		$( aMoreButtons ).each( function( aIndex, aElement ) {
			var lText = aElement.text || "aButton";
			var lFunction = aElement.aFunction;
			var lNewButton = lButton.clone( )
			
			.click( function( ) {
				lFunction( );
				lDialog.dialog( "close" );
				$( this ).parent( ).parent( ).remove( );
			});
			if (  aElement.id  ) {
				lNewButton.attr( "id", aElement.id );
			}

			lNewButton.append( $( '<div class="btn_left"></div>' ) )
			.append( $( '<div class="btn_center">' + lText + '</div>' ) )
			.append( $( '<div class="btn_right"></div>' ) );
			lButtonsArea.prepend( lNewButton );
		});
	}else{
		lButton.append( $( '<div class="btn_left"></div>' ) )
		.append( $( '<div class="btn_center">Ok</div>' ) )
		.append( $( '<div class="btn_right"></div>' ) );
		lButton.click( function( ) {
			if( aCloseFunction ) {
				aCloseFunction( );
			}
			lDialog.dialog( "close" );
			$( this ).parent( ).parent( ).remove( );
		});
		lButtonsArea.append( lButton );
	}
	if( aIconType ) {
		if(  aIconType == "error" || aIconType == "information" || 
			aIconType == "warning" || aIconType == "alert" || 
			aIconType == "important"  ) {
			var lIcon = $( "<div id='icon' class='"+ "icon_" +
				aIconType + "'></div>" );
			lDialog.append( lIcon );
		}
		else {
			console.log( "Unrecognized type of icon+' " + aIconType + 
				"', the allowed types are { error|information|warning|alert }" )
		}
	}
	lDialog.append( lContent );
	lDialog.prependTo( "body" );
	
	lDialog.dialog({
		autoOpen: true,
		resizable: false,
		modal: aIsModal || false,
		width: aWidth || 300,
		title: aTitle || "jWebSocket Message"
	});
	lDialog.append( lButtonsArea );
}

function closeDialog( ) {
	var	lDialog = $( '<div id="dialog"></div>' );
	lDialog.dialog( "close" ); 
	$( ".ui-dialog" ).remove( );
}