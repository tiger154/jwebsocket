package File
{
	import flash.events.*; 
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.FileReferenceList;
	import flash.external.ExternalInterface;
	
	public class FileInput extends EventDispatcher
	{
		private var _id:String;
		public var multiple:Boolean;
		private var _queue:Array;
		public var _files:*;
		public var _read_pos:*;
		
		private var filters:Array = [];
		
		public function FileInput(id:String, multiple:Boolean = false) {
			_id = id;
			this.multiple = multiple;
			initFiles();
			
			_read_pos = new Array();
			_queue = new Array();
			
			/*function lTimeHandler(event:TimerEvent):void {
				//event.target.stop();
				// Proceeding with the memory cleanup
			}
			var readyTimer:Timer = new Timer(100, 0);
			readyTimer.addEventListener(TimerEvent.TIMER, timerHandler);
			readyTimer.start();*/
		}
		private function initFiles():void{
			if(_files != null){
				if(_files.hasEventListener(Event.SELECT)){
					_files.removeEventListener(Event.SELECT, this.onSelect);
				}
				if(_files.hasEventListener(Event.CANCEL)){
					_files.removeEventListener(Event.CANCEL, this.onCancel);
				}
				_files = null;
			}
			
			if (multiple) {
				_files = new FileReferenceList();
			} else {
				_files = new FileReference();
			}
			_files.addEventListener(Event.SELECT, onSelect);
			_files.addEventListener(Event.CANCEL, onCancel);
		}
		
		public function addExtensions(label:String, extensions:String):int {
			return filters.push(new FileFilter(label, extensions));
		}
		
		public function addMimeType(mimeType:String):int {
			var extensions:* = MimeTypeMap.getExtensions(mimeType);
			if (extensions) {
				return filters.push(new FileFilter(mimeType, extensions));
			} else {
				return filters.length;
			}
		}
		
		public function browse():void {
			_files.browse(filters);
		}
		
		public function getFile(aFilename:String):FileReference {
			for each (var lFile:FileReference in this._queue) {
				if (aFilename == lFile.name) {
					return lFile;
				}
			}
			return null;
		}
		
		// Gets the current read position for a defined file
		public function getReadPosition(aFilename:String):Object {
			for (var lIdx = 0; lIdx < this._queue.length; lIdx++) {
				if (aFilename == this._queue[lIdx].name) {
					if(this._read_pos.length >= lIdx) {
						return this._read_pos[lIdx];
					}
					return null;
				}
			}
			return null;
		}
		
		public function setReadPosition(aFilename:Object, aReadPosition:Object):void {
			for (var lIdx = 0; lIdx < this._queue.length; lIdx++){
				if (aFilename == this._queue[lIdx].name) {
					if(this._read_pos[lIdx] != null){
						this._read_pos[lIdx].start = aReadPosition.start;
						this._read_pos[lIdx].end = aReadPosition.end;
						this._read_pos[lIdx].name = aFilename;
					} else {
						this._read_pos[lIdx] = aReadPosition;
					}
				}
			}
		}
		public function destroy(aFilename: String):void{
			var lLength = this._queue.length;
			for(var lIdx = 0; lIdx < lLength;lIdx++){
				if(aFilename == this._queue[lIdx].name){
					this._queue[lIdx].cancel();
					this._queue[lIdx] = null;
					_queue.splice(lIdx, 1);
					_read_pos[lIdx] = null;
					_read_pos.splice(lIdx, 1);
					break;
				}
			}
			initFiles();
		}
		
		public function get id():String {
			return _id;
		}
		
		public function get files():Array {
			if (multiple) {
				return _files.fileList;
			} else {
				return [_files];
			}
		}
		
		public function filesToJSON():Array {
			var lArray:Array = [];
			for each (var file:FileReference in this.files) {
				lArray.push({
					'type': MimeTypeMap.getMimeType(file.name.substr(-3)),
					'size': file.size,
					'name': file.name,
					'lastModifiedDate': file.modificationDate,
					'input': _id
				});
			}
			return lArray;
		}
		
		/* 
		* Events Handlers
		*/
		public function onSelect(evt:Event):void {
			//this._read_pos = new Array();
			for each (var lFile:FileReference in this.files) {
				var lSize:Number = lFile.size;
				if(!queueContaisFile(lFile)){
					this._queue.push(lFile);
					if(getReadPosition(lFile.name) == null) {
						this._read_pos.push({
							'start': 0,
							'end': lSize,
							'name': lFile.name,
							'input': _id,
							'length': lSize
						});
					}
				}
			}
			//this._files = this._queue;
			dispatchEvent(new Event('change'));
		}
		
		private function queueContaisFile(aFile:FileReference):Boolean{
			var lContains: Boolean = false;
			var lLength = this._queue.length;
			for(var lIdx = 0; lIdx < lLength; lIdx++){
				if(this._queue[lIdx].name == aFile.name){
					lContains = true;
				}
			}
			return lContains;
		}
		
		public function onCancel(evt:Event):void {
			dispatchEvent(new Event('cancel'));
		}
	}
}