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
echo Auto Generation of jWebSocket v%JWEBSOCKET_VER% Java Docs, are you sure?
pause
:dontAsk1

rem set log=..\jWebSocketDeployment\createJavaDocs.log
set log=con

cd /d ..\jWebSocketCommon
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketServerAPI
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketServer
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketSamples
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketClientAPI
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketJavaSEClient
call mvn generate-sources javadoc:javadoc >> %log%

cd /d ..\jWebSocketAppServer
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketAppSrvDemo
call mvn generate-sources javadoc:javadoc >> %log%

cd /d ..\jWebSocketSwingGUI
call mvn generate-sources javadoc:javadoc >> %log%

cd /d ..\jWebSocketJetty
call mvn generate-sources javadoc:javadoc >> %log%

rem switch back to deployment folder
cd ..\jWebSocketDeployment

echo finished! Please check if JavaDocs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2