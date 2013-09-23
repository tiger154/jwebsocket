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
		this.eChunkInput = this.element.find("#chunkSize input");

		w.fileUploader = this;
		w.fileUploader.registerEvents( );
	},
	registerEvents: function( ) {
		w.fileUploader.eChunkInput.attr('disabled', true);
		w.fileUploader.eChunkInput.change(function() {
			mWSC.setChunkSize($(this).val());
		});

		w.fileUploader.eBtnUpload.click(function() {
			mWSC.startUpload();
		});

		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function(aEvent, aToken) {
				w.fileUploader.processToken(aEvent, aToken);
			},
			OnOpen: function() {
				w.fileUploader.eChunkInput.attr('disabled', false);
				// Note: This is the only thing you need to do if you want to 
				// configure the jWebSocket Uploader PlugIn
				mWSC.initUploaderPlugIn({
					// we just need to pass the id of the button that will upload the files
					browseButtonId: 'select_file',
					// TODO: support also the following property "drop_area" only for 
					// browsers that support drag and drop
					drop_area: 'drop_files_area',
					chunkSize: parseInt(w.fileUploader.eChunkInput.val())
				});

				mWSC.addUploaderListeners({
					OnFileSelected: w.fileUploader.onFileSelected,
					OnUploadStarted: w.fileUploader.onUploadStarted,
					OnUploadComplete: w.fileUploader.onUploadComplete,
					OnUploadProgress: w.fileUploader.onUploadProgress,
					OnFileSaved: w.fileUploader.onFileUploaded,
					OnUploadError: w.fileUploader.onFileUploadError,
					OnError: w.fileUploader.onError,
					OnFileDeleted: w.fileUploader.onFileDeleted
				});
			},
			OnClose: function() {
				w.fileUploader.eChunkInput.attr('disabled', true);
			}
		};
		w.fileUploader.eMainContainer.auth(lCallbacks);
	},
	startUpload: function() {
		mWSC.startUpload();
	},
	onFileSelected: function(aEvent, aFiles) {
		for (var lIdx = 0; lIdx < aFiles.length; lIdx++) {
			var lExists = false;
			for (var lFileIdx = 0; lFileIdx < mWSC.queue.length; lFileIdx++) {
				if (aFiles[lIdx].name === mWSC.queue[lFileIdx].getName()) {
					lExists = true;
					break;
				}
			}
			if (!lExists) {
				w.fileUploader.addFileToTable(aFiles[lIdx]);
			}
//			w.fileUploader.updateProgress(aFiles[lIdx].name, 100);
		}
	},
	onFileDeleted: function(aEvent, aData) {
		w.fileUploader.removeFileFromTable(aData.item.getName());
	},
	onFileUploaded: function(aEvent, aItem) {
		w.fileUploader.updateProgress(aItem.getName(), aItem.getProgress());
		w.fileUploader.updateStatus(aItem.getName(), aItem.getStatus());
	},
	onFileUploadError: function(aEvent, aItem) {
		console.log(aItem);
		w.fileUploader.updateStatus(aItem.getName(), aItem.getStatus());
	},
	onError: function(aEvent, aErrorData) {
		var lItem = aErrorData.item;
		w.fileUploader.updateStatus(lItem.getName(), lItem.getStatus());
		jwsDialog(aErrorData.msg + "Do you want to remove this file from the list?",
				"Error detected", true, 'error', null, [{
				text: 'yes',
				aFunction: function() {
					w.fileUploader.removeFileFromTable(lItem.getName());
				}
			}, {
				text: 'no',
				aFunction: function() {
				}
			}]);
	},
	onUploadComplete: function(aEvent, aData) {
		console.log("Upload completed");
	},
	onUploadProgress: function(aEvent, aProgressData) {
		var lItem = aProgressData.item;
		w.fileUploader.updateProgress(lItem.getName(), lItem.getProgress());
		w.fileUploader.updateStatus(lItem.getName(), lItem.getStatus());
		jws.console.log("Upload progress in file: " + lItem.getName() + ", with progress: " + aProgressData.progress);
	},
	onUploadStarted: function(aFiles) {
		jws.console.log("Upload started successfully");
	},
	onUploadStopped: function(aEvt) {
		jws.console.log("upload has stopped");
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
				lActions = $('<td/>').append(
				$('<div class="pauseUpload" title="Pause upload"></div>').click(function() {
			w.fileUploader.toggleUpload($(this), aFile.name);
		})).append(
				$('<div class="delete_file" title="Delete file"></div>').click(function() {
			mWSC.removeFile(aFile.name);
		}));
		this.eTableContainer.append(lTr.append(lNameCell).append(lPercentCell)
				.append(lInputCell).append(lActions));
	},
	removeFileFromTable: function(aFilename) {
		var lId = aFilename.split(".").join("_"),
				lRow = this.eTableContainer.find("#" + lId);
		if (lRow) {
			lRow.remove();
		}
	},
	toggleUpload: function(aDomObj, aFilename) {
		var lFile = mWSC.getFile(aFilename);
		// If the upload is paused we resume it, otherwise we pause
		if (lFile.getStatus() === mWSC.STATUS_PAUSED) {
			aDomObj.attr('class', 'pauseUpload');
			mWSC.resumeUpload(aFilename);
		} else {
			aDomObj.attr('class', 'resumeUpload');
			mWSC.pauseUpload(aFilename);
		}
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
	updateStatus: function(aFilename, aStatus) {
		var lId = aFilename.split(".").join("_"),
				lFileRow = $('#' + lId);
		if (lFileRow && lFileRow.context) {

			switch (aStatus) {
				//	STATUS_READY: 0,
				case 0:
					lFileRow.attr('class', 'status_ready');
					break;
					//	STATUS_UPLOADING: 1,
				case 1:
					lFileRow.find('.pauseUpload').show();
					lFileRow.find('.resumeUpload').show();
					lFileRow.attr('class', 'status_uploading');
					break;
					//	STATUS_UPLOADED: 2,
				case 2:
					lFileRow.find('.pauseUpload').hide();
					lFileRow.find('.resumeUpload').hide();
					lFileRow.attr('class', 'status_uploaded');
					break;
					//	STATUS_ERROR: 3,
				case 3:
					lFileRow.find('.pauseUpload').hide();
					lFileRow.find('.resumeUpload').hide();
					lFileRow.attr('class', 'status_error');
					break;
					//	STATUS_PAUSED: 4,
				case 4:
					lFileRow.attr('class', 'status_paused');
					break;
					//	STATUS_CANCELED: 5,
				case 5:
					lFileRow.attr('class', 'status_ready');
					break;
				default:
					break;
			}
		}
	},
	processToken: function(aToken) {

	}
});