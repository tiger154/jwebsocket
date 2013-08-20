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
	queue: [],
	browseButton: {},
	isFlashFileReader: function() {
		return false;
	},
	init: function(aConfig) {
		aConfig = aConfig || {};
		// The upload button is required, we will render a transparent button 
		// over the upload button
		if (aConfig.browseButtonId) {
			if (aConfig.browseButtonId) {
				var lUserBrowseBtn = document.getElementById(aConfig.browseButtonId),
						lUploadBtn = document.createElement('input');
				lUploadBtn.setAttribute('type', 'file');
				lUploadBtn.setAttribute('style', 'visibility:hidden;');
				var lMe = this;
				lUploadBtn.onchange = function(aFiles){
					lMe.onFileSelected(aFiles);
				};
				lUserBrowseBtn.appendChild(lUploadBtn);
				lUserBrowseBtn.onclick = function() {
					lUploadBtn.click();
				};
				this.lUploadBtn = lUploadBtn;
			}
		}

	},
	browse: function() {
		// Invoke the browse method without clicking the browse field
		if (this.isFlashFileReader()) {
//			console.log(this.browseButton);
//			this.browseButton.click();
		}
	},
	onFileSelected: function(aFiles) {
		//TODO: check if the files are folders, upload nested folders
		console.log(aFiles);
	},
	startUpload: function() {

	},
	clearUploadedFiles: function() {

	},
	cancelUpload: function() {

	},
	pauseUpload: function() {

	},
	resumeUpload: function() {

	},
	processToken: function(aToken) {
		// check if namespace matches
		if (aToken.ns === jws.FileSystemPlugIn.NS) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if ("load" === aToken.reqType) {
				if (0 === aToken.code) {
					if (this.OnFileLoaded) {
						this.OnFileLoaded(aToken);
					}
				} else {
					if (this.OnFileError) {
						this.OnFileError(aToken);
					}
				}
			} else if ("send" === aToken.reqType) {
				if (0 === aToken.code) {
					if (this.OnFileSent) {
						this.OnFileSent(aToken);
					}
				} else {
					if (this.OnFileError) {
						this.OnFileError(aToken);
					}
				}
			} else if ("event" === aToken.type) {
				if ("filesaved" === aToken.name) {
					if (this.OnFileSaved) {
						this.OnFileSaved(aToken);
					}
				} else if ("filereceived" === aToken.name) {
					if (this.OnFileReceived) {
						this.OnFileReceived(aToken);
					}
				} else if ("filedeleted" === aToken.name) {
					if (this.OnFileDeleted) {
						this.OnFileDeleted(aToken);
					}
				}
			}
		}
	},
	setFileUploaderCallbacks: function(aListeners) {
		if (!aListeners) {
			aListeners = {};
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
//jws.oop.declareClass(aNamespace, aClassname, aAncestor, aFields)
jws.oop.declareClass('jws', 'UploadItem', null, {
	status: jws.FileUploaderPlugIn.STATUS_READY,
	progress: 0,
	file: null,
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
	getProgress: function() {
		return this.progress;
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
