@echo off

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

if "%1"=="/y" goto dontAsk1
echo This will run the automated download tests for jWebSocket v%JWEBSOCKET_VER%. Are you sure?
pause
:dontAsk1

set ver=%JWEBSOCKET_VER%
set testroot=c:\jWebSocket-%ver%-test\
set dlroot=%testroot%\downloads\

del %dlroot%\*.zip
set base=http://jwebsocket.googlecode.com/svn/downloads/jWebSocket-%ver%/
wget -P %dlroot% %base%jWebSocket-%ver%.zip"
wget -P %dlroot% %base%jWebSocketAndroidDemo-%ver%.zip"
wget -P %dlroot% %base%jWebSocketAppServer-%ver%.zip"
wget -P %dlroot% %base%jWebSocketAppSrvDemo-%ver%.zip"
wget -P %dlroot% %base%jWebSocketClient-%ver%.zip"
wget -P %dlroot% %base%jWebSocketFullSources-%ver%.zip"
wget -P %dlroot% %base%jWebSocketJetty-%ver%.zip"
wget -P %dlroot% %base%jWebSocketProxy-%ver%.zip"
wget -P %dlroot% %base%jWebSocketServer-%ver%.zip"
wget -P %dlroot% %base%jWebSocketServer32-%ver%.zip"
wget -P %dlroot% %base%jWebSocketServer64-%ver%.zip"
wget -P %dlroot% %base%jWebSocketServer-Bundle-%ver%.zip"
wget -P %dlroot% %base%jWebSocketService32-%ver%.zip"
wget -P %dlroot% %base%jWebSocketService64-%ver%.zip"
echo ----------------------------------------------------------------------------
echo Please check if 14 files have been loaded!
echo ----------------------------------------------------------------------------

dir %dlroot%*.zip

pause