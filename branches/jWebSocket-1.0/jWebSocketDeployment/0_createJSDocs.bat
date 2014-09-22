@echo off
echo -------------------------------------------------------------------------
echo jWebSocket JavaScript Docs Generator and Obfuscator
echo (C) Copyright 2013-2014 Innotrade GmbH
echo -------------------------------------------------------------------------

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit

:continue
echo.
set jsCE=%JWEBSOCKET_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%\jWebSocketClient\web\res\js
set jsJQ=%JWEBSOCKET_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%\jWebSocketClient\web\lib\jQuery
set jsEE=%JWEBSOCKET_EE_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%-Enterprise\jWebSocketClient\web\res\js
set jsUAEE=%JWEBSOCKET_EE_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%-Enterprise\jWebSocketClient\web\useradmin\res\libs\jWebSocket

pushd %jsCE%
set jsCE=%CD%\
popd

pushd %jsJQ%
set jsJQ=%CD%\
popd

pushd %jsEE%
set jsEE=%CD%\
popd

pushd %jsUAEE%
set jsUAEE=%CD%\
popd

echo jsCE=%jsCE%
echo jsJQ=%jsJQ%
echo jsEE=%jsEE%

echo.
if "%1"=="/y" goto dontAsk1
echo Auto Generation of jWebSocket v%JWEBSOCKET_VER% JavaScript Docs, are you sure?
pause
:dontAsk1

rem set log=..\jWebSocketDeployment\createJSDocs.log
set log=con

rem save current deployment folder
cd
pushd ..\jWebSocketClient\web\res\js

rem do *NOT* include the demo plug-in here! This will be loaded separately and is proprietary to the jWebSocket demos only!
copy /b jWebSocket.js + jWebSocketComet.js + jwsCache.js + jwsWorker.js + jwsAPIPlugIn.js + jwsCanvasPlugIn.js + jwsCanvasPlugIn.js + jwsChannelPlugIn.js + jwsChatPlugIn.js + jwsClientGamingPlugIn.js + jwsEventsPlugIn.js + jwsExtProcessPlugIn.js + jwsFileSystemPlugIn.js + jwsIOC.js + jwsItemStoragePlugIn.js + jwsJDBCPlugIn.js + jwsJMSPlugIn.js + jwsLoggingPlugIn.js + jwsMailPlugIn.js + jwsReportingPlugIn.js + jwsRPCPlugIn.js + jwsRTCPlugIn.js + jwsSamplesPlugIn.js + jwsScriptingPlugIn.js + jwsSharedObjectsPlugIn.js + jwsStreamingPlugIn.js + jwsTestPlugIn.js + jwsTwitterPlugIn.js + jwsXMPPPlugIn.js jWebSocket_Bundle.js

rem switch back to deployment folder
popd
set cd=%cd%

rem call obfuscator CE
start /wait "" "%JASOB_HOME%\jasob.exe" /src:"jWebSocket\jWebSocket.jsbp" /log:%cd%\jasobCE.log

rem call obfuscator EE
start /wait "" "%JASOB_HOME%\jasob.exe" /src:"%JWEBSOCKET_EE_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%-Enterprise\jWebSocketDeployment\jWebSocketEE.jsbp" /log:%cd%\jasobEE.log

rem call obfuscator jWS 3rd party libs
start /wait "" "%JASOB_HOME%\jasob.exe" /src:"jWebSocket\jWebSocket3rdPartyLibs.jsbp" /log:%cd%\jasob3rdP.log


rem copy minified/obfuscated EE editions into CE deployment

echo Copying minified/obfuscated enterprise js files to ce version...
copy %jsEE%jwsItemStoragePlugInEE_min.js %jsCE% /v
copy %jsEE%jwsFileSystemPlugInEE_min.js %jsCE% /v
copy %jsEE%jwsMailPlugInEE_min.js %jsCE% /v
copy %jsEE%jwsOntologyPlugInEE_min.js %jsCE% /v
copy %jsEE%jwsBPMNPlugInEE_min.js %jsCE% /v
copy %jsEE%jwsANTLRPlugInEE_min.js %jsCE% /v
copy %jsEE%jwsStringTemplatePlugInEE_min.js %jsCE% /v
copy %jsEE%jwsClusterAdminPlugInEE_min.js %jsCE% /v
copy %jsEE%jwsMASPlugInEE_min.js %jsCE% /v
copy %jsEE%jwsEnapsoPlugInEE_min.js %jsCE% /v

rem copy minified/obfuscated CE editions into EE deployment
echo Copying minified/obfuscated community js files to enterprise version...
copy %jsCE%jWebSocket_min.js %jsUAEE% /v
copy %jsCE%jwsFilesystemPlugIn_min.js %jsUAEE% /v
copy %jsJQ%jWebSocketJQueryPlugIn_min.js %jsUAEE% /v

echo finished! Please check if JavaScript Docs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2