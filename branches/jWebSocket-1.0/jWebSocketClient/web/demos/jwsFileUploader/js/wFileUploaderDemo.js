//	---------------------------------------------------------------------------
//	jWebSocket FileUploader Demo (Community Edition, CE)
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


/**
 * jWebSocket File Uploader Widget
 * @author Victor Antonio Barzana Crespo
 */
$.widget("jws.fileUploaderDemo", {
	_init: function( ) {
		this.eBtnUpload = this.element.find("#upload_btn");
		this.eMainContainer = $("#demo_box");

		jws.FileUploaderPlugIn.init({
			// we just need to pass the id of the button that will upload the files
			browseButtonId: 'select_file',
			// TODO: support also the following property "drop_area" only for 
			// browsers that support drag and drop
			drop_area: 'drop_files_area'
		});
		w.fileUploader = this;
		w.fileUploader.registerEvents( );
	},
	registerEvents: function( ) {

		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function(aEvent, aToken) {
				w.fileUploader.processToken(aEvent, aToken);
			}
		};
		w.fileUploader.eMainContainer.auth(lCallbacks);
	},
	processToken: function(aEvent, aToken) {
		console.log(aToken);
	}
});