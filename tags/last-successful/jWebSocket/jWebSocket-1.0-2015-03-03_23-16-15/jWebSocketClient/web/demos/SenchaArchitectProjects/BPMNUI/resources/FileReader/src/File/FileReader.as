package File
{
	import flash.events.*;
	import flash.net.FileReference;
	import flash.utils.ByteArray;
	import flash.errors.EOFError;
	import mx.utils.Base64Encoder;
	import flash.external.ExternalInterface;
	import mx.utils.Base64;
	import flash.display.Loader;

	public class FileReader extends EventDispatcher
	{
		public static const EMPTY:uint = 0;
		public static const LOADING:uint = 1;
		public static const DONE:uint = 2;
		
		public static const DATA:uint = 0;
		public static const BINARY:uint = 1;
		public static const TEXT:uint = 2;
		
		public var readyState:uint;
		public var readFormat:uint;
		
		public var error:Error;
		public var _isBlob:Boolean;
		
		private var _readPosition:Object;
		private var _result:String;
		private var _encoding:String;
		private var _length: uint;
		public var _id:String;
		
		public function FileReader(aIsBlob: Boolean = false, aReadPosition:Object = null, aId:String = null) {
			super();
			_id = aId;
			_readPosition = aReadPosition;
			_isBlob = aIsBlob;
			readyState = EMPTY;
			_result = "";
		}
		
		public function readAsDataURL(aFile:FileReference):void
		{
			readFormat = DATA;
			_start(aFile);
		}
		
		public function readAsBinaryString(aFile:FileReference):void
		{
			readFormat = BINARY;
			_start(aFile);
		}
		
		public function getResult():String{
			return this._result;
		}
		
		public function readAsText(aFile:FileReference, encoding:String = null):void
		{
			readFormat = TEXT;
			if (encoding) _encoding = encoding;
			_start(aFile);
		}
		
		public function abort():void
		{
			if (readyState === LOADING) {
				readyState = DONE;
			}
			_result = null;
			dispatchEvent(new ProgressEvent('abort'));
			dispatchEvent(new ProgressEvent('loadend'));
		}
		
		public function setReadPosition(aReadPosition:Object):void { 
			if(_readPosition != null){
				_readPosition.start = aReadPosition.start;
				_readPosition.end = aReadPosition.end;
			} else {
				_readPosition = aReadPosition;
			}
		}
		
		// _result Get
		public function get result():*
		{
			if (readyState === EMPTY || error) {
				return null;
			}
			return _result;
		}
		
		// Private
		private function _start(aFile:FileReference):void {
			readyState = LOADING;
			
			dispatchEvent(new ProgressEvent('loadstart'));
			// If the file was already loaded just extract it's data
			if(aFile.data != null){
				this.extractFileData(aFile);
			} else {
				aFile.addEventListener('progress', onProgress);
				aFile.addEventListener(Event.COMPLETE, onComplete);
				aFile.addEventListener(IOErrorEvent.IO_ERROR, onIOError); // As additional parameters we shouldn't forget false, 0, true
				aFile.load();
			}
			
		}
		
		private function _done():void
		{
			readyState = DONE;
			dispatchEvent(new ProgressEvent('loadend'));
		}
		
		private function readSlice( lContent:ByteArray, aStart = null, aEnd = null, aContenType:String = null): ByteArray {
			if(_readPosition != null || (aStart != null && aEnd != null)){
				var lContentType: String = _readPosition.contentType;
				if(aContenType != null){
					lContentType = aContenType;
				}
				
				var lResponse:ByteArray = new ByteArray( );
				if(aStart == null){
					aStart = _readPosition.start;
				}
				if(aEnd == null){
					aEnd = _readPosition.end;
				}
				
				if(aEnd > _length){
					aEnd = _length;
				}

				for(var lIdx:uint = aStart; lIdx < aEnd; lIdx++){
					lResponse.writeByte(lContent[lIdx]);
				}
				return lResponse;
			}
			ExternalInterface.call("console.error", "Error: Sorry, you are trying to read a blob that hasn't been sliced, try File.slice and then read it again");
			return null;
		}
		
		// Event Handlers
		
		public function onProgress(aEvt:ProgressEvent):void {
			dispatchEvent(aEvt);
		}

		public function onComplete(aEvt:Event):void {
			aEvt.target.removeEventListener('progress', this.onProgress);
			aEvt.target.removeEventListener(Event.COMPLETE, this.onComplete);
			aEvt.target.removeEventListener(IOErrorEvent.IO_ERROR, onIOError);
			extractFileData(aEvt.target);
		}
		
		private function loadBigFile(aFile):void {
			var lChunkSize:uint = 150000;
			var lCurrChunk:uint = 0;
			
			if(lChunkSize > _length){
				lChunkSize = _length;
			}
			var lStream = 0;
			while(lCurrChunk < _length){
				var lNextChunk = lCurrChunk + lChunkSize;
				if(lNextChunk > _length){
					lNextChunk = _length;
				}
				
				var lProgressEvent:Object = {
						'type': 'loadbigfile',
						'target': _id,
						'result': Base64.encode(readSlice(aFile.data, lCurrChunk, lNextChunk)),
						'stream': _id + "|" + lStream++,
						'isLast': (lCurrChunk + lChunkSize >= _length)
					};
				lCurrChunk += lChunkSize;
				ExternalInterface.call('FileAPIProxy.onFileReaderEvent', lProgressEvent);
				if(lProgressEvent.isLast){
					var lEvent:ProgressEvent = new ProgressEvent('load');
					lEvent.bytesLoaded = _length;
					lEvent.bytesTotal = _length;
		
					dispatchEvent(lEvent);
				}
			}
		}
		
		private function extractFileData(aFile: FileReference):void {
			_length = _readPosition.length;
			var lBytesLoaded:uint = 0;
			switch (readFormat) {
				case DATA:
					 try {
						// The Blob files will be processed by chunks
						if(_isBlob == true) {
							//ExternalInterface.call("console.info", "Reading a blob file from: " + _readPosition.start + "," + _readPosition.end);
							if(_readPosition.start >= _length || _readPosition.start < 0){
								ExternalInterface.call("console.info", "Wrong blob, you are trying to read a Blob out of " +
													  "the range, the length of the file is: " + _length + 
													  ", and you are asking for the Blob: "+ _readPosition.start + "," + 
													  _readPosition.end + " which is out of range");
								return;
							}
							var lResult:ByteArray = readSlice(aFile.data);
							lBytesLoaded = lResult.length;
							_result = "data:;base64," + Base64.encode(lResult);
							lResult = null;
						} else {
							var lMimeType:String = MimeTypeMap.getMimeType(aFile.name.substr(-3));
							if (!lMimeType) {
								lMimeType = 'text/plain';
							}
							_result = "data:" + lMimeType + ";base64,";
							lBytesLoaded = _length;
							
							if(_length > 400000 ){
								//ExternalInterface.call("console.info", "Loading a Big file");
								loadBigFile(aFile);
								aFile = null;
								return;
							} else {
								//ExternalInterface.call("console.info", "Loading a Normal file");
								_result += Base64.encode(aFile.data);
							}
							aFile = null;
						}
					}
					catch(e:Error) {
						ExternalInterface.call("console.error", "The following error was captured while reading the file " + e.toString());
					}
					break;
				case TEXT:
					if (_encoding) {
						_result = aFile.data.readMultiByte(_length, _encoding);
					} else {
						_result = aFile.data.toString();
					}
					break;
				case BINARY:
					_result = aFile.data.readMultiByte(_length, 'ascii');
					break;
			}
			
			
			var lEvent:ProgressEvent = new ProgressEvent('load');
			lEvent.bytesLoaded = lBytesLoaded;
			lEvent.bytesTotal = lBytesLoaded;
			dispatchEvent(lEvent);
			_done();
		}
		
		public function onIOError(evt:IOErrorEvent):void {
			_result = null;
			error = new Error("The File cannot be read!");
			error.name = 'NotReadableError';
			dispatchEvent(new Event('error'));
			_done();
		}
	}
}