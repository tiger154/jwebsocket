@echo off
echo -------------------------------------------------------------------------
echo jWebSocket Run Time File Generator
echo (C) Copyright 2013-2014 Innotrade GmbH
echo -------------------------------------------------------------------------

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_EE_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME, JWEBSOCKET_EE_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

if "%1"=="/y" goto dontAsk1
echo This will create the runtime files for jWebSocket v%JWEBSOCKET_VER%. Are you sure?
pause
:dontAsk1

pushd jWebSocket

rem Create Windows Executables to RTE bin folder
exe4jc jWebSocketServer32.exe4j
exe4jc jWebSocketServer64.exe4j
exe4jc jWebSocketService32.exe4j
exe4jc jWebSocketService64.exe4j
exe4jc jWebSocketAdmin32.exe4j
exe4jc jWebSocketAdmin64.exe4j

exe4jc jWebSocketAMQStockTickerService32.exe4j
exe4jc jWebSocketAMQStockTickerService64.exe4j

popd

if "%1"=="/y" goto dontAsk2
pause
:dontAsk2
