@echo off
set JSZip=%JWEBSOCKET_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%\jWebSocketClient\web\lib\JSZip\
echo This creates a minified version of the JSZip compression library
echo Path: %JSZip%
pause
copy /b %JSZip%jszip_min.js + %JSZip%jszip-deflate_min.js + %JSZip%jszip-inflate_min.js + %JSZip%jszip-load_min.js %JSZip%jszip-full_min.js
pause