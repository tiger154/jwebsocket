//	---------------------------------------------------------------------------
//	jWebSocket jwsFileUploader plug-in (Community Edition, CE)
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
 * @authors Rolando Santamaria, Victor Antonio Barzana
 */

//:package:*:jws
//:class:*:jws.jwsFileUploaderPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.jwsFileUploaderPlugIn[/tt] class.
//:d:en:This client-side plug-in provides the API to upload files from any web _
//:d:en:based client to the jWebSocket Server. _
jws.FileUploaderPlugIn = {
	STATUS_READY: 0,
	STATUS_UPLOADING: 1,
	STATUS_UPLOADED: 2,
	TT_FILE_SELECTED: "OnFileSelected",
	TT_FILE_UPLOADED: "OnFileSaved",
	TT_UPLOAD_STARTED: "OnUploadStarted",
	TT_UPLOAD_STOPPED: "OnUploadStopped",
	TT_UPLOAD_PROGRESS: "OnUploadProgress",
	TT_UPLOAD_COMPLETE: "OnUploadComplete",
	TT_UPLOAD_ERROR: "OnUploadError",
	queue: [],
	listeners: {},
	browseButton: {},
	userBrowseButton: {},
	isFlashFileReader: function() {
		return window.FileReader && window.FileReader.isFlashFallback ? true : false;
	},
	init: function(aConfig) {
		aConfig = aConfig || {};
		// The upload button is required, we will render a transparent button 
		// over the upload button
		if (aConfig.browseButtonId) {
			var lUserBrowseBtn = document.getElementById(aConfig.browseButtonId),
					lBrowseButton = document.createElement('input');
			lBrowseButton.setAttribute('type', 'file');
			lBrowseButton.setAttribute('multiple', 'true');
			lBrowseButton.style.cssText = 'visibility:hidden !important;display: none !important;';
			var lMe = this;
			lUserBrowseBtn.appendChild(lBrowseButton);
			lUserBrowseBtn.onclick = function() {
				lMe.browse();
			};
			this.browseButton = lBrowseButton;

			var lFlashFileReader = new FlashFileReader({
				inputs: [aConfig.browseButtonId],
				filereader: 'js/FileReader/src/filereader.swf',
				expressInstall: "js/FileReader/swfobject/expressInstall.swf",
				debugMode: false,
				multiple: true,
				callback: function() {
					console.log("Flash HTML5 File API fallback is ready");
				}
			});

			// TODO: remove jQuery dependency
			$(lUserBrowseBtn).on('change', function(aEvt) {
				lMe.onFileSelected(aEvt);
			});
			lMe.setFileSystemCallbacks({
				OnFileSaved: function(aToken) {
					console.log("File " + aToken.filename + " has been uploaded successfully to the server.");
					var lUploadedItem = this.getFile(aToken.filename);
					if (lUploadedItem) {
						lUploadedItem.setProgress(100);
						lUploadedItem.setUploadedBytes(lUploadedItem.getFile().size);
						lUploadedItem.setStatus(this.STATUS_UPLOADED);
					}
					lMe.fireUploaderEvent(lMe.TT_FILE_UPLOADED, lUploadedItem);
					// Fire event "upload successful"
				}
			});
		}
	},
	browse: function() {
		// Invoke the browse method without clicking the browse field
		if (!this.isFlashFileReader()) {
			this.browseButton.click();
		}
	},
	onFileSelected: function(aEvt) {
		this.fireUploaderEvent(this.TT_FILE_SELECTED, aEvt.target.files);

		var lQueueContains = function(aQueue, aFile) {
			for (var lIdx1 = 0; lIdx1 < aQueue.length; lIdx1++) {
				if (aQueue[lIdx1].getName() === aFile.name) {
					return lIdx1;
				}
			}
			return -1;
		};
		for (var lIdx = 0; lIdx < aEvt.target.files.length; lIdx++) {
			if (-1 === lQueueContains(this.queue, aEvt.target.files[lIdx])) {
				var lUploadItem = new jws.UploadItem(aEvt.target.files[lIdx]);
				this.queue.push(lUploadItem);
			}
		}
	},
	uploadFileInChunks: function(aUploadItem) {
		if (!aUploadItem) {
			return;
		}
		var lFile = aUploadItem.getFile();
		var lChunkSize = 250000,
				lCurrentChunk = 0,
				lMe = this,
				lReader = new FileReader(),
				lTotalBytes = lFile.size,
				lBytesSent = 0,
				lBytesRead = 0,
				lChunkId = 0;
		aUploadItem.setChunkSize(lChunkSize);
		if (aUploadItem.getStatus() === this.STATUS_READY) {
			aUploadItem.setStatus(this.STATUS_UPLOADING);

			// was here
			var lSave = function(aEvt) {
				aUploadItem.setUploadedBytes(lBytesRead);
				var lIsLast = lBytesRead >= lTotalBytes;
				var lData = aEvt.target.result;
				var lChunkContent = lData.substr(lData.indexOf("base64,") + 7);
				lMe.fileSaveByChunks(lFile.name, lChunkContent, lIsLast, {
					encoding: "base64",
					encode: false,
					scope: jws.SCOPE_PRIVATE,
					OnSuccess: function(aEvent) {
						aUploadItem.setUploadedBytes(lBytesSent);
						lMe.fireUploaderEvent(lMe.TT_UPLOAD_PROGRESS, {
							item: aUploadItem,
							progress: aUploadItem.getProgress()
						});
						lBytesSent += lChunkSize;
						lCurrentChunk++;

						if (!lIsLast) {
							var lBlob = lFile.slice(lBytesSent, lBytesSent + lChunkSize);
							lReader.readAsDataURL(lBlob);
							lChunkId++;
						}
					},
					OnFailure: function(aEvent) {
						var lMsg = "Upload failed, sorry your upload process failed on " +
								lBytesRead / lChunkSize + " chunk!" +
								" With the message: \"" + aEvent.msg + "\"";
						lMe.fireUploaderEvent(lMe.TT_UPLOAD_ERROR, {
							item: aUploadItem,
							msg: lMsg,
							errorEvt: aEvent
						});
						jws.console.log("Upload process failed on " + lBytesRead / lChunkSize + " chunk!");
						jws.console.log(aEvent);
					}
				});
			};

			lReader.onload = function(aEvt) {
				lBytesRead += lChunkSize;
				lSave(aEvt);
			};

			var lBlob = lFile.slice(lBytesSent, lChunkSize);
			lReader.readAsDataURL(lBlob);
		}
	},
	uploadCompleteFile: function(aUploadItem) {
		var lReader = new FileReader();
		var lScope = this;
		lReader.onload = function(aReference) {
			console.log("File completely loaded with the following info, bytes loaded: " + aReference.loaded + ", length: " + aReference.target.result.length);
			var lResult = aReference.target.result;
//			lScope.setEnterpriseFileSystemCallbacks({
//				OnFileSaved: function(aToken) {
//					console.log("File " + aToken.filename + " has been uploaded successfully to the server.");
////					lMe.onUploadSuccess(aToken, aItem);
//				}
//			});
			var lContent = lResult.substr(lResult.indexOf("base64,") + 7);

			lScope.fileSave(aUploadItem.getName(), lContent, {
				encoding: "base64",
				encode: false,
				scope: jws.SCOPE_PUBLIC,
				OnSuccess: function(aEvent) {
					console.log("success received");
				}});
		};
		lReader.readAsDataURL(aUploadItem.getFile());
	},
	startUpload: function() {
//		if (jws.isConnected()) {
		this.fireUploaderEvent(this.TT_UPLOAD_STARTED);
		for (var lIdx = 0; lIdx < this.queue.length; lIdx++) {
			var lItem = this.queue[lIdx];
			if (lItem.getStatus() === this.STATUS_READY) {
				// If the item will be uploaded in chunks or not
				if (lItem.getChunked()) {
					this.uploadFileInChunks(lItem);
				} else {
					this.uploadCompleteFile(lItem);
				}
			}
		}
//		} else {
//			jws.console.error("Upload failed: You are not connected to " +
//					"jWebSocket Server, please check if your server is running.");
//		}
	},
	addUploaderListener: function(aType, aListener) {
		if (typeof this.listeners[aType] === "undefined") {
			this.listeners[aType] = [];
		}
		this.listeners[aType].push(aListener);
	},
	addUploaderListeners: function(aListeners) {
		for (var lIdx in aListeners) {
			this.addUploaderListener(lIdx, aListeners[lIdx]);
		}
	},
	fireUploaderEvent: function(aEvent, aData) {
		if (typeof aEvent === "string") {
			aEvent = {type: aEvent};
		}
		if (!aEvent.target) {
			aEvent.target = this;
		}

		if (!aEvent.type) {  //falsy
			throw new Error("Event object missing 'type' property.");
		}

		if (this.listeners[aEvent.type] instanceof Array) {
			var listeners = this.listeners[aEvent.type];
			for (var lIdx = 0, len = listeners.length; lIdx < len; lIdx++) {
				listeners[lIdx].call(this, aEvent, aData);
			}
		}
	},
	removeUploaderListener: function(aEvent, aListener) {
		if (this.listeners[aEvent] instanceof Array) {
			var listeners = this.listeners[aEvent];
			for (var lIdx = 0, len = listeners.length; lIdx < len; lIdx++) {
				if (listeners[lIdx] === aListener) {
					listeners.splice(lIdx, 1);
					break;
				}
			}
		}
	},
	getFile: function(aFilename) {
		for (var lIdx = 0; lIdx < this.queue.length; lIdx++) {
			if (this.queue[lIdx].getName() === aFilename) {
				return this.queue[lIdx];
			}
		}
		return null;
	},
	clearUploadedFiles: function() {

	},
	cancelUpload: function() {

	},
	pauseUpload: function() {

	},
	resumeUpload: function() {

	},
	setFileUploaderCallbacks: function(aListeners) {
		if (!aListeners) {
			aListeners = {};
		}
		if (aListeners.OnFileSelected !== undefined) {
			this.OnFileSelected = aListeners.OnFileSelected;
		}
		if (aListeners.OnFileLoaded !== undefined) {
			this.OnFileLoaded = aListeners.OnFileLoaded;
		}
		if (aListeners.OnFileSaved !== undefined) {
			this.OnFileSaved = aListeners.OnFileSaved;
		}
		if (aListeners.OnFileReceived !== undefined) {
			this.OnFileReceived = aListeners.OnFileReceived;
		}
		if (aListeners.OnFileSent !== undefined) {
			this.OnFileSent = aListeners.OnFileSent;
		}
		if (aListeners.OnFileError !== undefined) {
			this.OnFileError = aListeners.OnFileError;
		}
		if (aListeners.OnLocalFileRead !== undefined) {
			this.OnLocalFileRead = aListeners.OnLocalFileRead;
		}
		if (aListeners.OnLocalFileError !== undefined) {
			this.OnLocalFileError = aListeners.OnLocalFileError;
		}
		if (aListeners.OnChunkReceived !== undefined) {
			this.OnChunkReceived = aListeners.OnChunkReceived;
		}
		if (aListeners.OnChunkLoaded !== undefined) {
			this.OnChunkLoaded = aListeners.OnChunkLoaded;
		}
		if (aListeners.OnFSDirectoryCreated !== undefined) {
			this.OnFSDirectoryCreated = aListeners.OnFSDirectoryCreated;
		}
		if (aListeners.OnFSDirectoryChanged !== undefined) {
			this.OnFSDirectoryChanged = aListeners.OnFSDirectoryChanged;
		}
		if (aListeners.OnFSDirectoryDeleted !== undefined) {
			this.OnFSDirectoryDeleted = aListeners.OnFSDirectoryDeleted;
		}
		if (aListeners.OnFSFileDeleted !== undefined) {
			this.OnFSFileDeleted = aListeners.OnFSFileDeleted;
		}
		if (aListeners.OnFSFileCreated !== undefined) {
			this.OnFSFileCreated = aListeners.OnFSFileCreated;
		}
		if (aListeners.OnFSFileChanged !== undefined) {
			this.OnFSFileChanged = aListeners.OnFSFileChanged;
		}
	}
};

// add the jWebSocket FileSystem PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.FileUploaderPlugIn);

jws.oop.declareClass('jws', 'UploadItem', null, {
	status: jws.FileUploaderPlugIn.STATUS_READY,
	progress: 0,
	file: null,
	isChunked: null,
	create: function(aFile) {
		this.name = aFile.name;
		this.file = aFile;
		this.progress = 0;
		this.isChunked = true;
	},
	setChunkSize: function(aChunkSize) {
		this.chunkSize = aChunkSize;
	},
	setUploadedBytes: function(aBytes) {
		this.uploadedBytes = aBytes;
	},
	getChunkSize: function() {
		return this.chunkSize;
	},
	getUploadedBytes: function(aBytes) {
		return this.uploadedBytes;
	},
	getName: function() {
		return this.name;
	},
	setFile: function(aFile) {
		this.file = aFile;
	},
	getFile: function() {
		return this.file;
	},
	getProperty: function(aPropName) {
		if (this.file && this.file[aPropName]) {
			return this.file[aPropName];
		}
		return null;
	},
	setProgress: function(aProgress) {
		this.progress = aProgress;
	},
	/*
	 * Indicates wether the item will be uploaded in chunks or not
	 * @return isChunked:Boolean
	 */
	getChunked: function() {
		return this.isChunked;
	},
	/*
	 * Tells to the uploader that the item will be uploaded in chunks
	 * @return isChunked:Boolean
	 */
	setChunked: function(aChunked) {
		this.isChunked = aChunked;
	},
	getProgress: function() {
		return this.progress = parseInt(this.uploadedBytes * 100 / this.getFile().size);
	},
	setStatus: function(aStatus) {
		this.status = aStatus;
	},
	getStatus: function() {
		return this.status;
	}
});