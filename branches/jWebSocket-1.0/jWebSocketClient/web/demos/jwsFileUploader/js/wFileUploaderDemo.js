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
		this.eBtnUpload = this.element.find("#start_upload");
		this.eMainContainer = $("#demo_box");
		this.eTableContainer = this.element.find("#filelist table");

		w.fileUploader = this;
		w.fileUploader.registerEvents( );
	},
	registerEvents: function( ) {
		w.fileUploader.eBtnUpload.click(function() {
			mWSC.startUpload();
		});
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function(aEvent, aToken) {
				w.fileUploader.processToken(aEvent, aToken);
			},
			OnOpen: function() {
				mWSC.init({
					// we just need to pass the id of the button that will upload the files
					browseButtonId: 'select_file',
					// TODO: support also the following property "drop_area" only for 
					// browsers that support drag and drop
					drop_area: 'drop_files_area'
				});

				mWSC.addUploaderListeners({
					OnFileSelected: w.fileUploader.onFileSelected,
					OnUploadStarted: w.fileUploader.onUploadStarted,
					OnUploadComplete: w.fileUploader.onUploadComplete,
					OnUploadProgress: w.fileUploader.onUploadProgress,
					OnFileSaved: w.fileUploader.onFileUploaded
				});
			}
		};
		w.fileUploader.eMainContainer.auth(lCallbacks);
	},
	startUpload: function() {
		mWSC.startUpload();
	},
	onFileSelected: function(aEvent, aFiles) {
		for (var lIdx = 0; lIdx < aFiles.length; lIdx++) {
			w.fileUploader.addFileToTable(aFiles[lIdx]);
//			w.fileUploader.updateProgress(aFiles[lIdx].name, 100);
		}
	},
	onFileUploaded: function(aEvent, aItem) {
		w.fileUploader.updateProgress(aItem.getName(), aItem.getProgress());
	},
	onUploadComplete: function(aEvent, aData) {
		console.log("Upload completed");
	},
	onUploadProgress: function(aEvent, aProgressData) {
		var lItem = aProgressData.item;
		w.fileUploader.updateProgress(lItem.getName(), lItem.getProgress());
		console.log("upload progress in file: " + lItem.getName() + ", with progress: " + aProgressData.progress);
	},
	onUploadStarted: function(aFiles) {
		console.log("upload started successfully");
	},
	onUploadStopped: function(aEvt) {
		console.log("upload has stopped");
	},
	onUploadPaused: function(aEvt) {
		console.log("upload paused successfully");
	},
	onUploadResumed: function(aEvt) {
		console.log("upload resumed successfully");
	},
	addFileToTable: function(aFile) {
		var lId = aFile.name.split(".").join("_");
		var lTr = $("<tr id='" + lId + "'></tr>"),
				lNameCell = $("<td>" + aFile.name + "</td>"),
				lPercentCell = $("<td class='progress'>0%</td>"),
				lInputCell = $('<td></td>').append($('<input type="checkbox" ' +
				'checked></input>').change(function() {
			var lFile = mWSC.getFile(aFile.name);
			lFile && lFile.setChunked($(this).prop("checked"));
		})),
				lDeleteAction = $('<td/>').append(
				$('<div class="delete_file"></div>').click(function() {
			$(this).parent().parent().remove();
		}));
		this.eTableContainer.append(lTr.append(lNameCell).append(lPercentCell)
				.append(lInputCell).append(lDeleteAction));
	},
	updateProgress: function(aFilename, aProgress) {
		var lId = aFilename.split(".").join("_");
		var lFileRow = $('#' + lId);
		if (lFileRow && lFileRow.context) {
			var lProgressRow = lFileRow.find(".progress");
			if (lProgressRow) {
				lProgressRow.text(aProgress + "%");
			}
		}
	},
	processToken: function(aEvent, aToken) {

	}
});