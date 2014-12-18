//	---------------------------------------------------------------------------
//	jWebSocket jwsFileUploader plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
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
	STATUS_ERROR: 3,
	STATUS_PAUSED: 4,
	STATUS_CANCELED: 5,
	TT_FILE_SELECTED: "OnFileSelected",
	TT_FILE_UPLOADED: "OnFileSaved",
	TT_FILE_DELETED: "OnFileDeleted",
	TT_UPLOAD_STARTED: "OnUploadStarted",
	TT_UPLOAD_STOPPED: "OnUploadStopped",
	TT_UPLOAD_PROGRESS: "OnUploadProgress",
	TT_UPLOAD_COMPLETE: "OnUploadComplete",
	TT_UPLOAD_CANCELED: "OnUploadCanceled",
	TT_UPLOAD_ERROR: "OnUploadError",
	TT_ERROR: "OnError",
	TT_INFO: "InfoMessage",
	TT_ERROR_DELETING_FILE: "FileDeleteError",
	queue: [],
	chunkSize: 500000,
	defaultScope: jws.SCOPE_PUBLIC,
	defaultAlias: jws.ALIAS_PUBLIC,
	isUploading: false,
	listeners: {},
	browseButton: {},
	userBrowseButton: {},
	isFlashFileReader: function() {
		return window.FileReader && window.FileReader.isFlashFallback ? true : false;
	},
	initUploaderPlugIn: function(aConfig) {
		aConfig = aConfig || {};
		this.listeners = {};
		this.queue = [];
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
				debugMode: typeof aConfig.debugMode !== "undefined" ?
						aConfig.debugMode : false,
				multiple: typeof aConfig.multiple !== "undefined" ?
						aConfig.multiple : true,
				callback: function() {
					var lMsg = "Note: As your browser does not support FileReader " +
							"from HTML5 to read files, the Flash Fallback mechanism " +
							"switched automatically, please consider updating " +
							"your browser later, thanks!";
					alert(lMsg);
					jws.console.log(lMsg);
				}
			});

			this.chunkSize = aConfig.chunkSize || this.chunkSize;
			this.defaultScope = aConfig.defaultScope || this.defaultScope;
			this.defaultAlias = aConfig.defaultAlias || this.defaultAlias;

			// TODO: remove jQuery dependency
			lUserBrowseBtn.onchange = function(aEvt) {
				lMe.onFileSelected(aEvt);
			};
			lMe.setFileSystemCallbacks({
				OnFileSaved: function(aToken) {
					lMe.onFileSaved(aToken.filename);
				},
				OnFileDeleted: function(aToken) {
					lMe.fireUploaderEvent(lMe.TT_FILE_DELETED, {
						item: lMe.getFile(aToken.filename),
						token: aToken
					});
				}
			});
		}
	},
	onFileSaved: function(aFilename) {
		//console.log("File " + aToken.filename + " has been uploaded successfully to the server.");
		var lUploadedItem = this.getFile(aFilename);
		if (lUploadedItem) {
			lUploadedItem.setProgress(100);
			lUploadedItem.setUploadedBytes(lUploadedItem.getFile().size);
			lUploadedItem.setStatus(this.STATUS_UPLOADED);
		}
		this.fireUploaderEvent(this.TT_FILE_UPLOADED, lUploadedItem);
		// clearing for IE
		lUploadedItem = null;
		var lSuccessCount = 0;
		for (var lIdx = 0; lIdx < this.queue.length; lIdx++) {
			if (this.queue[lIdx].getStatus() === this.STATUS_UPLOADED) {
				lSuccessCount++;
			}
		}
		if (lSuccessCount === this.queue.length) {
			this.fireUploaderEvent(this.TT_UPLOAD_COMPLETE);
			this.isUploading = false;
		}
	},
	browse: function() {
		// Invoke the browse method without clicking the browse field
		if (!this.isFlashFileReader()) {
			this.browseButton.value = "";
			this.browseButton.click();
		}
	},
	onFileSelected: function(aEvt) {
		this.fireUploaderEvent(this.TT_FILE_SELECTED, aEvt.target.files);
		for (var lIdx = 0; lIdx < aEvt.target.files.length; lIdx++) {
			this.queue.push(new jws.UploadItem(aEvt.target.files[lIdx]));
		}
		//TODO: check if the files are folders, upload nested folders
//		for (var i = 0; i < aEvt.target.files.length; i++) {
//			var lFile = aEvt.target.files[i];
//			var lReader = new FileReader();
//			lReader.file = aEvt.target.files[i];
//
//			var lChunkSize = 250000, lReadChunks = 0;
//			var lComplete = false;
//			lReader.onload = function(aReference) {
////				console.log(this.file.name + " loaded chunk: " + (!lComplete ? (lReadChunks / lChunkSize) : "LAST") + ", loaded: " +
////						aReference.loaded + ", length: " +
////						aReference.target.result.length);
//				lReadChunks += lChunkSize;
//				chunkNext(this.file);
//			};
//
//			var chunkNext = function(aFile) {
//				var lNextChunk = lReadChunks + lChunkSize;
//				var lBlob = null;
//				if (lNextChunk < aFile.size) {
//					if (lReadChunks <= lFile.size) {
//						lBlob = aFile.slice(lReadChunks, lNextChunk);
//						lReader.readAsDataURL(lBlob);
//					}
//				} else {
//					if (!lComplete) {
//						lComplete = true;
//						if (lReadChunks <= lFile.size) {
//							lBlob = aFile.slice(lReadChunks, aFile.size);
//							lReader.readAsDataURL(lBlob);
//						}
//					} else {
//						lReadChunks = 0;
//					}
//				}
//			};
//			var lBlob = lFile.slice(lReadChunks, lChunkSize);
//			lReader.readAsDataURL(lBlob);


//			lReader.onload = function(aReference) {
//				console.log("File completely loaded with the following info, bytes loaded: " + aReference.loaded + ", length: " + aReference.target.result.length);
//			};
//			lReader.readAsDataURL(lFile);

//		}
	},
	uploadFileInChunks: function(aUploadItem, aResume) {
		if (!aUploadItem) {
			return;
		}
		if (aUploadItem.getStatus() === this.STATUS_READY ||
				aUploadItem.getStatus() === this.STATUS_ERROR) {
			aUploadItem.setStatus(this.STATUS_UPLOADING);
			var lFile = aUploadItem.getFile(),
					lChunkSize = this.chunkSize,
					lMe = this,
					lReader = new FileReader(),
					lTotalBytes = lFile.size,
					lBytesSent = 0,
					lBytesRead = 0;
			aUploadItem.setChunkSize(lChunkSize);

			if (aResume) {
				lBytesSent = aUploadItem.getUploadedBytes();
				lBytesRead = aUploadItem.getUploadedBytes();
			}

			this.fireUploaderEvent(this.TT_UPLOAD_STARTED, aUploadItem);

			var lSave = function(aEvt) {
				if (lMe.isConnected()) {
					// We check if the upload is not paused
					if (aUploadItem.getStatus() === lMe.STATUS_UPLOADING ||
							aUploadItem.getStatus() === lMe.STATUS_ERROR) {
						aUploadItem.setUploadedBytes(lBytesRead);
						var lIsLast = lBytesRead >= lTotalBytes,
								lData = aEvt.target.result,
								lChunkContent = lData.substr(lData.indexOf("base64,") + 7);
						lMe.fileSaveByChunks(lFile.name, lChunkContent, lIsLast, {
							encoding: "base64",
							encode: false,
							scope: lMe.defaultScope || "public",
							alias: lMe.defaultAlias || "publicDir",
							OnSuccess: function(aEvent) {
								aUploadItem.setUploadedBytes(lBytesSent);
								lMe.fireUploaderEvent(lMe.TT_UPLOAD_PROGRESS, {
									item: aUploadItem,
									progress: aUploadItem.getProgress( )
								});
								lBytesSent += lChunkSize;

								if (!lIsLast) {
									var lBlob = lFile.slice(lBytesSent, lBytesSent + lChunkSize);
									lReader.readAsDataURL(lBlob);
								} else {
									aUploadItem.setProgress(100);
									aUploadItem.setUploadedBytes(aUploadItem.getFile().size);
									aUploadItem.setStatus(this.STATUS_UPLOADED);
									this.fireUploaderEvent(this.TT_FILE_UPLOADED, aUploadItem);
								}
							},
							OnFailure: function(aEvent) {
								var lMsg = "Upload failed, sorry your upload process failed on " +
										lBytesRead / lChunkSize + " chunk!" +
										" With the message: \"" + aEvent.msg + "\"";
								aUploadItem.setStatus(this.STATUS_ERROR);
								lMe.fireUploaderEvent(lMe.TT_UPLOAD_ERROR, {
									item: aUploadItem,
									msg: lMsg,
									errorEvt: aEvent
								});
								jws.console.log("Upload process failed on " + lBytesRead / lChunkSize + " chunk!");
								jws.console.log(aEvent);
							}
						});
					}
				} else {
					var lMsg = "Upload failed, the connection with the server was lost in chunk: " +
							lBytesRead / lChunkSize +
							". Please try your upload later";
					aUploadItem.setStatus(this.STATUS_ERROR);
					lMe.fireUploaderEvent(lMe.TT_UPLOAD_ERROR, {
						item: aUploadItem,
						msg: lMsg
					});
				}
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
		var lMe = this;
		lReader.onload = function(aEvent) {
			if (lMe.isConnected()) {
				if (aUploadItem.getStatus() !== lMe.STATUS_UPLOADED) {
					aUploadItem.setStatus(lMe.STATUS_UPLOADING);
					jws.console.log("File completely loaded with the following info, " +
							"bytes loaded: " + aEvent.loaded + ", length: " +
							aEvent.target.result.length);
					var lResult = aEvent.target.result,
							lContent = lResult.substr(lResult.indexOf("base64,") + 7);
					lMe.fileSave(aUploadItem.getName(), lContent, {
						encoding: "base64",
						encode: false,
						scope: lMe.defaultScope,
						alias: lMe.defaultAlias,
						OnSuccess: function(aToken) {
							// File saved correctly
							lMe.onFileSaved(aUploadItem.getName());
						}, OnFailure: function(aEvent) {
							var lMsg = "Upload failed, sorry your upload process " +
									"failed on file: " + aUploadItem.getName() + " With the message: \"" + aEvent.msg + "\"";
							aUploadItem.setStatus(this.STATUS_ERROR);
							lMe.fireUploaderEvent(lMe.TT_UPLOAD_ERROR, {
								item: aUploadItem,
								msg: lMsg,
								errorEvt: aEvent
							});
						}});
				}
			} else {
				var lMsg = "Upload failed, the connection with the server was lost while uploading file: " +
						aUploadItem.getName() + ". Please try your upload later";
				aUploadItem.setStatus(this.STATUS_ERROR);
				lMe.fireUploaderEvent(lMe.TT_UPLOAD_ERROR, {
					item: aUploadItem,
					msg: lMsg
				});
			}
		};
		lReader.readAsDataURL(aUploadItem.getFile());
	},
	startUpload: function() {
		this.fireUploaderEvent(this.TT_UPLOAD_STARTED);
		for (var lIdx = 0; lIdx < this.queue.length; lIdx++) {
			this.isUploading = true;
			// If the item will be uploaded in chunks or not
			if (this.queue[lIdx].getChunked()) {
				this.uploadFileInChunks(this.queue[lIdx]);
			} else {
				this.uploadCompleteFile(this.queue[lIdx]);
			}
		}
	},
	/*
	 * Removes a file from the server filesystem
	 * @param {type} aFilename the name of the file
	 * @param {type} aScope the scope in the server where the file was uploaded
	 * @returns {void}
	 */
	removeFile: function(aFilename, aScope) {
		var lMe = this;
		lMe.cancelUpload(aFilename);
		// delete a file from the fs aFilename, aForce, aOptions
		lMe.fileDelete(aFilename, true, {
			scope: aScope || this.defaultScope,
			OnSuccess: function(aToken) {
				lMe.fireUploaderEvent(lMe.TT_FILE_DELETED, {
					item: lMe.getFile(aFilename),
					token: aToken
				});
				lMe.removeFileFromQueue(aFilename);
			},
			OnFailure: function(aToken) {
				var lMsg = "Error found while removing the file: " + aFilename + "." +
						" The server returned the following error: " + aToken.msg;

				lMe.fireUploaderEvent(lMe.TT_ERROR, {
					type: lMe.TT_ERROR_DELETING_FILE,
					item: lMe.getFile(aFilename),
					msg: lMsg
				});
				lMe.removeFileFromQueue(aFilename);
			}
		});
	},
	removeFileFromQueue: function(aFilename) {
		for (var lIdx = 0; lIdx < this.queue.length; lIdx++) {
			if (this.queue[lIdx].getName() === aFilename) {
				this.queue.splice(lIdx, 1);
				return;
			}
		}
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
	setChunkSize: function(aChunkSize) {
		this.chunkSize = aChunkSize || this.chunkSize;
	},
	setUploadScope: function(aScope) {
		if (this.isUploading) {
			this.fireUploaderEvent(this.TT_ERROR, {
				type: this.TT_INFO,
				msg: "You can only change the scope when the upload is " +
						"complete, please try again later."
			});
			return;
		}
		this.defaultScope = aScope;
	},
	setUploadAlias: function(aAlias) {
		if (this.isUploading) {
			this.fireUploaderEvent(this.TT_ERROR, {
				type: this.TT_INFO,
				msg: "You can only change the alias when the upload is " +
						"complete, please try again later."
			});
			return;
		}
		if (!aAlias) {
			this.fireUploaderEvent(this.TT_ERROR, {
				type: this.TT_INFO,
				msg: "Please provide an alias, this field is required"
			});
			return;
		}
		this.defaultAlias = aAlias;
	},
	cancelUpload: function(aFilename) {
		var lFile = this.getFile(aFilename);
		if (lFile) {
			this.getFile(aFilename).setStatus(this.STATUS_CANCELED);
			this.fireUploaderEvent(this.TT_UPLOAD_CANCELED, lFile);
		}
	},
	pauseUpload: function(aFilename) {
		for (var lIdx = 0; lIdx < this.queue.length; lIdx++) {
			if (this.queue[lIdx].getName() === aFilename) {
				this.queue[lIdx].setStatus(this.STATUS_PAUSED);
			}
		}
	},
	resumeUpload: function(aFilename) {
		var lUploadItem = this.getFile(aFilename);
		lUploadItem.setStatus(this.STATUS_READY);
		// If the item will be uploaded in chunks or not
		if (lUploadItem.getChunked()) {
			// Additional parameter to tell the chunking system that the upload 
			// will continue
			this.uploadFileInChunks(lUploadItem, true);
		} else {
			this.uploadCompleteFile(lUploadItem);
		}
	}
};

// add the jWebSocket FileSystem PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.FileUploaderPlugIn);

jws.oop.declareClass('jws', 'UploadItem', null, {
	status: null,
	progress: 0,
	file: null,
	isChunked: null,
	uploadPaused: null,
	create: function(aFile) {
		this.name = aFile.name;
		this.file = aFile;
		this.progress = 0;
		this.status = jws.FileUploaderPlugIn.STATUS_READY,
				this.isChunked = true;
		this.uploadPaused = false;
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

// add the jWebSocket FileSystem PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.FileUploaderPlugIn);

debug = function(aMsg) {
	if (jws.isIE) {
		$('body').append($("<div style='float: right;right:20px;top:0; margin:5px; " +
				"width: 40%; z-index: 999;font-size: 9pt;background: white;border: gray 2px solid;'>")
				.html(debugObjIE(aMsg)).dblclick(function() {
			$(this).remove();
		}));
	}
	console.log(aMsg);
};
debugObjIE = function(aObject, aPadding) {
	aPadding = aPadding || 4;
	var lResult = "";
	var lDiv = "<div style='padding-left: " + aPadding + "px;" +
			(aPadding === 0 ? 'overflow-y: scroll;height: 400px;width: 96%;' : '') + "'>";
	for (var lIdx in aObject) {
		if (typeof aObject[lIdx] !== "function") {
			lResult += "<b>" + lIdx + ": </b>";
			if (isObject(aObject[lIdx]) || isArray(aObject[lIdx])) {
				if (isArray(aObject[lIdx]) && aObject[lIdx].length === 0) {
					lResult += "[]";
				} else if (isObject(aObject[lIdx]) && isEmpty(aObject[lIdx])) {
					lResult += "{}";
				} else {
					lResult += debugObjIE(aObject[lIdx], aPadding + 8);
				}
			} else {
				lResult += ((aObject[lIdx].length > 500 ?
						(aObject[lIdx].substr(0, 500) + "...(too long object)") :
						(typeof aObject[lIdx] === "function" ? "[...]" : aObject[lIdx] || '""'))) +
						"<font color='green'> [" + typeof aObject[lIdx] + "]</font>";
			}
			lResult += "<br/>";
		}
	}
	return lResult ? lDiv + lResult + "</div>" : "";
};
isObject = function(aObj) {
	if (typeof aObj === "object") {
		for (var lIdx in aObj) {
			if (aObj[lIdx]) {
				return true;
			}
		}
		return true;
	}
	return false;
};
isArray = function(aObj) {
	return (aObj && typeof aObj.push === "function");
};
isEmpty = function(aObj) {
	if (aObj === null || typeof aObj === "undefined") {
		return true;
	} else {
		for (var lIdx in aObj) {
			return false;
		}
		return true;
	}
};