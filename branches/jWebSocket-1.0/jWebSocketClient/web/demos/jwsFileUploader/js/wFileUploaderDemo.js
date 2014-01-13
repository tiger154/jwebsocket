//	---------------------------------------------------------------------------
//	jWebSocket FileUploader Demo ( Community Edition, CE )
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH ( jWebSocket.org )
//  Alexander Schulze, Germany ( NRW )
//
//	Licensed under the Apache License, Version 2.0 ( the "License" );
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
		this.eBtnDownload = this.element.find("#download");
		this.eBtnGetFileList = this.element.find("#get_file_list");
		this.eFileTree = this.element.find("#file_tree");
		this.eMainContainer = $("#demo_box");
		this.eTableContainer = this.element.find("#filelist table");
		this.eChunkInput = this.element.find("#chunkSize input");
		this.eAliasInput = this.element.find("#alias input");
		this.eFileMask = this.element.find("#filemask input");
		this.eScopeChooser = this.element.find("input[name=scope]");
		this.eChecksumEnabled = this.element.find("#checksumEnabled");
		this.eChecksumAlgorithm = this.element.find("#checksumAlgorithm");

		w.fileUploader = this;
		w.fileUploader.registerEvents( );
	},
	registerEvents: function( ) {
		w.fileUploader.eChunkInput.attr('disabled', true);
		w.fileUploader.eChunkInput.change(function( ) {
			mWSC.setChunkSize($(this).val( ));
		});
		w.fileUploader.eChecksumEnabled.change(function() {
			w.fileUploader.eChecksumAlgorithm.attr('disabled', $(this).attr("checked")?false:true);
		});
		w.fileUploader.eBtnGetFileList.click(function( ) {
			w.fileUploader.getFileList();
		});
		w.fileUploader.eBtnDownload.click(function( ) {
			// TODO: implement this...
		});

		w.fileUploader.eFileMask.attr('disabled', true);

		w.fileUploader.eAliasInput.attr('disabled', true);
		w.fileUploader.eAliasInput.change(function( ) {
			mWSC.setUploadAlias($(this).val( ));
		});

		w.fileUploader.eFileMask.change(w.fileUploader.getFileList);

		w.fileUploader.eBtnUpload.click(function( ) {
			mWSC.startUpload( );
		});
		w.fileUploader.eScopeChooser.change(function( ) {
			w.fileUploader.setScope($(this).val( ));
			if ($(this).val() === "private") {
				w.fileUploader.eAliasInput.val("privateDir");
				w.fileUploader.eAliasInput.attr('disabled', true);
			} else {
				w.fileUploader.eAliasInput.val("publicDir");
				w.fileUploader.eAliasInput.attr('disabled', false);
			}
		});


		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
//			lURL: "ws://localhost:8787/jWebSocket/jWebSocket",
			OnMessage: function(aEvent, aToken) {
				w.fileUploader.processToken(aEvent, aToken);
			},
			OnOpen: function( ) {
				w.fileUploader.eChunkInput.attr('disabled', false);
				w.fileUploader.eAliasInput.attr('disabled', false);
				w.fileUploader.eFileMask.attr('disabled', false);

				w.fileUploader.eScopeChooser.each(function(aIdx, aInput) {
					if (aInput.checked) {
						w.fileUploader.setScope($(this).val( ));
					}
				});

				// Note: This is the only thing you need to do if you want to 
				// configure the jWebSocket File Uploader PlugIn
				mWSC.initUploaderPlugIn({
					// we just need to pass the id of the button that will upload the files
					browseButtonId: 'select_file',
					// TODO: support also the following property "drop_area" only for 
					// browsers that support drag and drop
					drop_area: 'drop_files_area',
					chunkSize: parseInt(w.fileUploader.eChunkInput.val( )),
					defaultAlias: w.fileUploader.eAliasInput.val( ),
					defaultScope: "public"
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
			OnClose: function( ) {
				w.fileUploader.eChunkInput.attr('disabled', true);
				w.fileUploader.eAliasInput.attr('disabled', true);
				w.fileUploader.eFileMask.attr('disabled', true);
			}
		};
		w.fileUploader.eMainContainer.auth(lCallbacks);
	},
	createFile: function(aFile) {
		//canRead: true
		//canWrite: true
		//directory: true
		//filename: "bomloader_538"
		//hidden: false
		//mime: "application/octet-stream"
		//modified: "2013-09-26T12:16:02Z"
		//relativePath: ""
		//size: 0
		var lProperties = "",
				lTrimAt = 15,
				lName = aFile.filename.length > lTrimAt ? aFile.filename.substr(0, lTrimAt) + "..." : aFile.filename;
		for (var lIdx in aFile) {
			lProperties += lIdx + ": " + aFile[lIdx] + "\n";
		}
		return $("<div class='" + (aFile.directory ? "folder" : "file") + "' title='" + lProperties + "'>" + lName + "</div>");

	},
	getFileList: function( ) {
		mWSC.fileGetFilelist(w.fileUploader.eAliasInput.val(), [w.fileUploader.eFileMask.val() || '*.*'], {
			recursive: true,
			includeDirs: true,
			OnSuccess: function(aToken) {
				w.fileUploader.eFileTree.html("");
				if (aToken.files) {
					for (var lIdx = 0; lIdx < aToken.files.length; lIdx++) {
						w.fileUploader.eFileTree.append(w.fileUploader.createFile(aToken.files[lIdx]));
					}
				}
			},
			OnFailure: function(aToken) {
				console.log("failure");
				console.log(aToken);
			}
		});
	},
	startUpload: function( ) {
		mWSC.startUpload( );
	},
	onFileSelected: function(aEvent, aFiles) {
		for (var lIdx = 0; lIdx < aFiles.length; lIdx++) {
			var lExists = false;
			for (var lFileIdx = 0; lFileIdx < mWSC.queue.length; lFileIdx++) {
				if (aFiles[lIdx].name === mWSC.queue[lFileIdx].getName( )) {
					lExists = true;
					break;
				}
			}
			if (!lExists) {
				w.fileUploader.addFileToTable(aFiles[lIdx]);
			}
//			w.fileUploader.updateProgress( aFiles[lIdx].name, 100 );
		}
	},
	onFileDeleted: function(aEvent, aData) {
		w.fileUploader.removeFileFromTable(aData.item.getName( ));
	},
	onFileUploaded: function(aEvent, aItem) {
		w.fileUploader.updateProgress(aItem.getName( ), aItem.getProgress( ));
		w.fileUploader.updateStatus(aItem.getName( ), aItem.getStatus( ));
	},
	onFileUploadError: function(aEvent, aData) {
		jwsDialog(aData.msg, "Error uploading file", true, 'error');
		w.fileUploader.updateStatus(aData.getName( ), aData.getStatus( ));
	},
	onError: function(aEvent, aErrorData) {
		var lItem = aErrorData.item;
		// The errorrs must bring a type when they are fired, so we can control 
		// them if the error does not bring a message, at least it must bring a 
		// default type: aErrorData.type === TT_ERROR
		if (aErrorData.type) {
			if (aErrorData.type === mWSC.TT_ERROR_DELETING_FILE) {
				w.fileUploader.updateStatus(lItem.getName( ), lItem.getStatus( ));
				jwsDialog(aErrorData.msg + "Do you want to remove this file from the list?",
						"Error detected", true, 'error', null, [{
								text: 'yes',
								aFunction: function( ) {
									w.fileUploader.removeFileFromTable(lItem.getName( ));
								}
							}, {
								text: 'no',
								aFunction: function( ) {
								}
							}]);
			} else if (aErrorData.type === mWSC.TT_ERROR) {
				jwsDialog(aErrorData.msg, "Error detected", true, 'error', null);
			} else if (aErrorData.type === mWSC.TT_INFO) {
				jwsDialog(aErrorData.msg, "Info", true, 'info', null);
			}
		}
	},
	onUploadComplete: function(aEvent, aData) {
		console.log("Upload completed");
	},
	onUploadProgress: function(aEvent, aProgressData) {
		var lItem = aProgressData.item;
		w.fileUploader.updateProgress(lItem.getName( ), lItem.getProgress( ));
		w.fileUploader.updateStatus(lItem.getName( ), lItem.getStatus( ));
		jws.console.log("Upload progress in file: " + lItem.getName( ) + ", with progress: " + aProgressData.progress);
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
	cleanID: function(aFilename, aCallback) {
		$.when({id: aFilename.replace(/[`~!@#$%^&*_( )|+\-=?;:'" ,.<>/]/gi, '_')}).done(
				function(aData) {
					if (aCallback && "function" === typeof aCallback) {
						aCallback(aData.id);
					}
				});
	},
	setScope: function(aScope) {
		switch (aScope) {
			case "public":
				mWSC.setUploadScope(jws.SCOPE_PUBLIC);
				break;
			case "private":
				mWSC.setUploadScope(jws.SCOPE_PRIVATE);
				break;
		}
	},
	addFileToTable: function(aFile) {
		var lGetId = function(aId) {
			var lTr = $("<tr id='" + aId + "'></tr>"),
					lNameCell = $("<td>" + aFile.name + "</td>"),
					lPercentCell = $("<td class='progress'>0%</td>"),
					lInputCell = $('<td></td>').append($('<input type="checkbox" ' +
					'checked></input>').change(function( ) {
				var lFile = mWSC.getFile(aFile.name);
				if (lFile && lFile.getStatus( ) !== mWSC.STATUS_UPLOADING) {
					lFile.setChunked($(this).prop("checked"));
				} else {
					log("This file is being uploaded, please wait until the " +
							"upload finishes or remove this file and add it again.");
				}
			})),
					lActions = $('<td/>').append(
					$('<div class="pauseUpload" title="Pause upload"></div>').click(function( ) {
				w.fileUploader.toggleUpload($(this), aFile.name);
			})).append(
					$('<div class="delete_file" title="Delete file"></div>').click(function( ) {
				mWSC.removeFile(aFile.name);
			}));
			w.fileUploader.eTableContainer.append(lTr.append(lNameCell).append(lPercentCell)
					.append(lInputCell).append(lActions));
		};
		w.fileUploader.cleanID(aFile.name, lGetId);
	},
	removeFileFromTable: function(aFilename) {
		var lGetId = function(aId) {
			var lRow = w.fileUploader.eTableContainer.find("#" + aId);
			if (lRow) {
				lRow.remove( );
			}
		};
		w.fileUploader.cleanID(aFilename, lGetId);
	},
	toggleUpload: function(aDomObj, aFilename) {
		var lFile = mWSC.getFile(aFilename);
		// If the upload is paused we resume it, otherwise we pause
		if (lFile.getStatus( ) === mWSC.STATUS_PAUSED) {
			aDomObj.attr('class', 'pauseUpload');
			mWSC.resumeUpload(aFilename);
		} else {
			aDomObj.attr('class', 'resumeUpload');
			mWSC.pauseUpload(aFilename);
		}
	},
	updateProgress: function(aFilename, aProgress) {
		var lGetId = function(aId) {
			var lFileRow = $('#' + aId);
			if (lFileRow && lFileRow.context) {
				var lProgressRow = lFileRow.find(".progress");
				if (lProgressRow) {
					lProgressRow.text(aProgress + "%");
				}
			}
		};
		w.fileUploader.cleanID(aFilename, lGetId);
	},
	updateStatus: function(aFilename, aStatus) {
		var lGetId = function(aId) {
			var lFileRow = $('#' + aId);
			if (lFileRow && lFileRow.context) {
				switch (aStatus) {
					//	STATUS_READY: 0,
					case 0:
						lFileRow.attr('class', 'status_ready');
						break;
						//	STATUS_UPLOADING: 1,
					case 1:
						lFileRow.find('.pauseUpload').show( );
						lFileRow.find('.resumeUpload').show( );
						lFileRow.attr('class', 'status_uploading');
						break;
						//	STATUS_UPLOADED: 2,
					case 2:
						lFileRow.find('.pauseUpload').hide( );
						lFileRow.find('.resumeUpload').hide( );
						lFileRow.attr('class', 'status_uploaded');
						break;
						//	STATUS_ERROR: 3,
					case 3:
						lFileRow.find('.pauseUpload').hide( );
						lFileRow.find('.resumeUpload').hide( );
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
		};
		w.fileUploader.cleanID(aFilename, lGetId);
	},
	processToken: function(aToken) {

	}
});