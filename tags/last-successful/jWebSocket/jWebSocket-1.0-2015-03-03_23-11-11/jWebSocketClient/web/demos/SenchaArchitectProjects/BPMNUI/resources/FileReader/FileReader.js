//There is a memory leak with IE and swfObject and this is the patch ;)
if (jws.isIExplorer() && jws.getBrowserVersion() <= 9) {
	function fixOutOfMemoryError() {
		__flash_unloadHandler = function() {
		};
		__flash_savedUnloadHandler = function() {
		};
	}
	window.attachEvent("onbeforeunload", fixOutOfMemoryError);
}
// If the FileReader object is defined then we don't use our PlugIn
if (window.FileReader) {
	FlashFileReader = function() {
		return this;
	};
} else {

	/**
	 * Flash FileReader Proxy
	 */
	window.FileAPIProxy = {
		ready: false,
		init: function(aObject, aFlashFileReader) {
			var lMe = this;
			this.debugMode = aObject.debugMode;
			this.container = $('<div>').attr('id', aObject.id)
					.wrap('<div>')
					.parent()
					.css({
				position: 'fixed',
//				 top:'0px',
				width: '1px',
				height: '1px',
				display: 'inline-block',
				background: 'transparent',
				'z-index': 99999
			}).on('mouseover mouseout mousedown mouseup', function(aEvt) {
				if (aFlashFileReader.currentTarget) {
					$('#' + aFlashFileReader.currentTarget).trigger(aEvt.type);
				}
			}).appendTo('body');

			swfobject.embedSWF(aObject.filereader, aObject.id, '100%', '100%', '10',
					aObject.expressInstall, {
				debugMode: aObject.debugMode ? true : '',
				chunked: aObject.chunked,
				chunkSize: aObject.chunkSize
			},
			{'wmode': 'transparent', 'allowScriptAccess': 'sameDomain'}, {},
					function(aEvent) {
						lMe.swfObject = aEvent.ref;
						$(lMe.swfObject)
								.css({
							display: 'block',
							outline: 0
						})
								.attr('tabindex', 0);
						if (aEvent.success) {
							lMe.ready = aEvent.success && typeof aEvent.ref.add === "function";
							var lReadyCBs = function() {
								for (var lIdx = 0; lIdx < aFlashFileReader.readyCallbacks.length; lIdx++) {
									aFlashFileReader.readyCallbacks[lIdx](aEvent);
								}
							};
							if (lMe.ready) {
								lReadyCBs();
							} else {
								// The first load, sometimes the external interface
								// requires an extra time to be totally ready, 
								// so that javascript can identify the callbacks 
								// registered in the External Interface, this can be 10 ms
								setTimeout(function() {
									lReadyCBs();
								}, 1000);
							}
						}
					});

		},
		swfObject: null,
		container: null,
		// Inputs Registry
		inputs: {},
		// Readers Registry
		readers: {},
		// Receives FileInput events
		onFileInputEvent: function(aEvt) {
			if (aEvt.target in this.inputs) {
//				var lElement = document.getElementById(aEvt.target);
				var aElement = this.inputs[aEvt.target];
//				aEvt.target = aElement[0];
				if (aEvt.type === 'change') {
					aEvt.files = new FileList(aEvt.files);
					aEvt.target = {files: aEvt.files};
				}
//				var evt;
//				if ("createEvent" in document) {
//					evt = document.createEvent("HTMLEvents");
//					evt.initEvent("change", true, false);
//					lElement.dispatchEvent(evt);
//				}
//				else {
//					evt = document.createEventObject();
//					lElement.fireEvent("onchange", evt);
//				}
				aElement.trigger(aEvt);
				aElement = null;
			}
			aEvt.files = null;
			aEvt.target = null;
			//window.focus();
			aEvt = null;
		},
		// Receives FileReader ProgressEvents
		onFileReaderEvent: function(aEvt) {
			if (this.debugMode)
				console.info('FileReader Event ', aEvt.type, aEvt, aEvt.target in this.readers);
			if (aEvt.target in this.readers) {
				aEvt.target = this.readers[aEvt.target];
				aEvt.target._handleFlashEvent.call(aEvt.target, aEvt);
			}
			aEvt = null;
			delete aEvt;
		},
		// Receives flash FileReader Error Events
		onFileReaderError: function(aError) {
//			if (this.debugMode)
			jws.console.error(aError);
		}
	};


	/**
	 * Add FileReader to the window object
	 */
	window.FileReader = FileReader = function() {
		// states
		this.EMPTY = 0;
		this.LOADING = 1;
		this.DONE = 2;
		this.isFlashFallback = true;

		this.readyState = 0;

		// File or Blob data
		this.result = null;

		this.error = null;

		// event handler attributes
		this.onloadstart = null;
		this.onprogress = null;
		this.onload = null;
		this.onabort = null;
		this.onerror = null;
		this.onloadend = null;

		// Event Listeners handling using JQuery Callbacks
		this._listeners = {
			loadstart: null, //$.Callbacks("unique"),
			progress: null, //$.Callbacks("unique"),
			abort: null, //$.Callbacks("unique"),
			error: null, //$.Callbacks("unique"),
			load: null, //$.Callbacks("unique"),
			loadend: null //$.Callbacks("unique")
		};

		// Custom properties
		this._id = null;
	};

	window.FileReader.prototype = {
		// async read methods
		readAsBinaryString: function(aBlob) {
			this._start(aBlob);
			FileAPIProxy.swfObject.read(aBlob.input, aBlob.name, 'readAsBinaryString');
			aBlob = null;
		},
		readAsText: function(aBlob, aEncoding) {
			this._start(aBlob);
			FileAPIProxy.swfObject.read(aBlob.input, aBlob.name, 'readAsText');
			aBlob = aEncoding = null;
		},
		readAsDataURL: function(aBlob) {
			this._start(aBlob);
			var lIsBlob = true;
			if (aBlob instanceof File) {
				lIsBlob = false;
			}
			setTimeout(function() {
				FileAPIProxy.swfObject.read(aBlob.input, aBlob.name, 'readAsDataURL', lIsBlob);
				aBlob = lIsBlob = null;
			}, 0);
		},
		readAsArrayBuffer: function(aBlob) {
			throw("Whoops FileReader.readAsArrayBuffer is not implemented yet");
		},
		abort: function() {
			this.result = null;
			if (this.readyState === this.EMPTY || this.readyState === this.DONE)
				return;
			FileAPIProxy.swfObject.abort(this._id);
		},
		// Event Target interface
		addEventListener: function(aType, aListener) {
			if (aType in this._listeners) {
				this._listeners[aType].add(aListener);
			}
		},
		removeEventListener: function(aType, aListener) {
			if (aType in this._listeners)
				this._listeners[aType].remove(aListener);
		},
		dispatchEvent: function(aEvent) {
			aEvent.target = this;
			if (aEvent.type in this._listeners) {
				var lType = 'on' + aEvent.type;
				if (typeof this[lType] === "function") {
					this[lType](aEvent);
				}
				aEvent.target = null;
				aEvent = lType = null;
				//this._listeners[aEvent.type].fire(aEvent);
			}
			return true;
		},
		// Registers FileReader instance for flash callbacks
		_register: function(aFile) {
			this._id = aFile.input + '.' + aFile.name;
			FileAPIProxy.readers[this._id] = this;
		},
		_start: function(aFile) {
			this._register(aFile);
			if (this.readyState === this.LOADING) {
				throw {
					type: 'InvalidStateError',
					code: 11,
					message: 'The object is in an invalid state.'};
			}
		},
		_handleFlashEvent: function(aEvt) {
			switch (aEvt.type) {
				case 'loadstart':
					this.readyState = this.LOADING;
					break;
				case 'loadend':
					this.readyState = this.DONE;
					break;
				case 'loadbigfile':
					this.tempResult = this.tempResult || {};
					var lStreamSplit = aEvt.stream.split("|"),
							lStreamType = lStreamSplit[0],
							lStreamId = parseInt(lStreamSplit[1]);

					this.tempResult[lStreamType] = this.tempResult[lStreamType] || [];
					this.tempResult[lStreamType][lStreamId] = aEvt.result;
					this.streams = this.streams || {};
					this.streams[lStreamType] = this.LOADING;

					if (aEvt.isLast) {
						this.streams = this.streams || {};
						this.streams[lStreamType] = this.DONE;
					}
					lStreamSplit = lStreamId = lStreamType = null;
					break;
				case 'load':
					this.readyState = this.DONE;
					this.result = aEvt.result;
					// If the file was previously loaded in chunks we need to 
					// gather the streams together in only one
					if (this.streams) {
						this.result = "";
						for (var lStreamType in this.streams) {
							if (this.streams[lStreamType] === this.DONE) {
								for (var lIdx1 in this.tempResult[lStreamType]) {
									this.result += this.tempResult[lStreamType][lIdx1];
								}
								delete this.tempResult[lStreamType];
								delete this.streams[lStreamType];
								break;
							}
						}
					}
					aEvt.result = null;
					delete aEvt.result;
					break;
				case 'error':
					this.result = null;
					this.error = {
						name: 'NotReadableError',
						message: 'The File cannot be read!'
					};
			}
			var lScope = this;
			setTimeout(function() {
				var lEvent = new FileReaderEvent(aEvt);
				lScope.dispatchEvent(lEvent);
				// Removing references for IE
				for (var lIdx in lEvent) {
					lEvent[lIdx] = null;
					delete lEvent[lIdx];
				}
				for (var lIdx in aEvt) {
					aEvt[lIdx] = null;
					delete aEvt[lIdx];
				}
				lEvent = aEvt = lScope = null;
			}, 0);
		}
	};

	/*
	 * HTML5 Blob API definition
	 * This interface represents immutable raw data. It provides a method to 
	 * slice data objects between ranges of bytes into further chunks of raw 
	 * data. It also provides an attribute representing the size of the chunk 
	 * of data. The File interface inherits from this interface.
	 */

	jws.oop.declareClass('window', 'Blob', null, {
		size: undefined,
		type: undefined,
		/* If invoked with zero parameters, return a new Blob object consisting 
		 * of 0 bytes, with size set to 0, and with type set to the empty string.
		 * Otherwise, the constructor is invoked with a blobParts sequence. Let a be that sequence.
		 * Let bytes be an empty sequence of bytes.
		 */
		create: function(aBlobParts, aBlobPropertyBag) {
			if (arguments.length === 0) {
				aBlobParts = {};
			} else if (typeof aBlobParts === 'string') {
				aBlobParts = {data: aBlobParts};
			}

			this.size = aBlobParts.size || 0;
			this.type = aBlobParts.type || '';
			this.data = aBlobParts.data || '';
			aBlobParts = aBlobPropertyBag = null;
			return this;
		},
		/*
		 * 
		 * @param {type} start The optional start parameter is a value for the 
		 * start point of a slice call, and must be treated as a byte-order position,
		 * with the zeroth position representing the first byte.
		 * @param {type} end
		 * @param {type} contentType
		 * @returns {undefined}
		 */
		slice: function(aStart, aEnd, aContentType) {
			var lBlob = new Blob({
				size: aEnd - aStart,
				type: aContentType
			});
			lBlob.name = this.name; // We need to keep in the blob the reference of which filename and input it belongs to
			lBlob.input = this.input;

			// Saying to Flash that the next time it will read will be from this blob
			FileAPIProxy.swfObject.slice(lBlob.input, lBlob.name, aStart, aEnd, aContentType);
			aStart = aEnd = aContentType = null;
			return lBlob;
		},
		close: function() {
			var lScope = this;
			setTimeout(function() {
				FileAPIProxy.swfObject.close(lScope.input, lScope.name);
				lScope = null;
			}, 0);
		}
	});

	/*
	 * HTML5 File API definition
	 */
	jws.oop.declareClass('window', 'File', Blob, {
		create: function(aFile) {
			aFile = aFile || {};
			for (var lIdx in aFile) {
				this[lIdx] = aFile[lIdx];
			}
			this.name = aFile.name || "";
			this.lastModifiedDate = aFile.lastModifiedDate || new Date();
			arguments.callee.inherited.call(this, aFile);
		}});



	function extend(aDestination, aSource) {
		for (var lKey in aSource) {
			if (aSource.hasOwnProperty(lKey)) {
				aDestination[lKey] = (aSource[lKey] && aSource[lKey] !== false) ? aSource[lKey] : aDestination[lKey];
			}
		}
		return aDestination;
	}

	/**
	 * FileReader ProgressEvent implenting Event interface
	 */
	FileReaderEvent = function(aEvt) {
		this.initEvent(aEvt);
	};

	FileReaderEvent.prototype = {
		initEvent: function(aEvent) {
			extend(this, extend(aEvent, {
				type: null,
				target: null,
				currentTarget: null,
				eventPhase: 2,
				bubbles: false,
				cancelable: false,
				defaultPrevented: false,
				isTrusted: false,
				timeStamp: new Date().getTime()
			}));
			aEvt = null;
		},
		stopPropagation: function() {
		},
		stopImmediatePropagation: function() {
		},
		preventDefault: function() {
		}
	};

	/**
	 * FileList interface (Object with item function)
	 */
	FileList = function(aFiles) {
		if (aFiles) {
			for (var lIdx = 0; lIdx < aFiles.length; lIdx++) {
				this[lIdx] = new File(aFiles[lIdx]);
			}
			this.length = aFiles.length;
		} else {
			this.length = 0;
		}
	};

	FileList.prototype = {
		item: function(aIdx) {
			if (aIdx in this)
				return this[aIdx];
			return null;
		}
	};
	FlashFileReader = function(aOptions) {
		this.defaults = {
			inputs: [],
			id: 'fileReaderSWFObject', // ID for the created swf object container,
			multiple: null,
			accept: null,
			label: null,
			extensions: null,
			filereader: 'files/filereader.swf', // The path to the filereader swf file
			expressInstall: null, // The path to the express install swf file
			debugMode: false,
			callback: false // Callback function when Filereader is ready
		};
		aOptions = extend(this.defaults, aOptions);
		
		this.readyCallbacks = [];
		this.lInputsCount = 0;
		this.currentTarget = 0;

		var lMe = this;
		this.readyCallbacks.push(function() {
			return lMe.initInputs(aOptions);
		});
		if ("function" === typeof aOptions.callback) {
			lMe.readyCallbacks.push(aOptions.callback);
		}

		if (!FileAPIProxy.ready) {
			FileAPIProxy.init(aOptions, this);
		}
		return this;
	};

	FlashFileReader.prototype.initInputs = function(aOptions) {
		jws.console.log("initializing Flash FileAPI inside the inputs: " +
				aOptions.inputs.join(","));
		
		for (var lIdx = 0; lIdx < aOptions.inputs.length; lIdx++) {
//			var aInput = document.getElementById(aConfig.browseButtonId)
			var lInput = $("#" + aOptions.inputs[lIdx]);
			var lId = lInput.attr('id');
			if (!lId) {
				lId = 'flashFileInput' + this.lInputsCount;
				lInput.attr('id', lId);
				this.lInputsCount++;
			}
			aOptions.multiple = !!(aOptions.multiple === null ? lInput.attr('multiple') : aOptions.multiple);
			aOptions.accept = aOptions.accept === null ? lInput.attr('accept') : aOptions.accept;

			FileAPIProxy.inputs[lId] = lInput;
			console.log(FileAPIProxy.swfObject.add);
			FileAPIProxy.swfObject.add(lId, aOptions.multiple, aOptions.accept, aOptions.label, aOptions.extensions);
			lInput.css('z-index', 0)
					.mouseover(function(e) {
				if (lId !== this.currentTarget) {
					e = e || window.event;
					this.currentTarget = lId;
					FileAPIProxy.swfObject.mouseover(lId);
					FileAPIProxy.container
							.height(lInput.height())
							.width(lInput.width())
							.css(lInput.offset());
				}
			})
					.click(function(aEvt) {
				aEvt.preventDefault();
				aEvt.stopPropagation();
				aEvt.stopImmediatePropagation();
				return false;
			});
		}
	};
}