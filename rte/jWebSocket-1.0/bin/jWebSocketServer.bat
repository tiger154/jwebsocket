@echo off
echo Starting the jWebSocket Server...
echo (C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.
rem if JWEBSOCKET_HOME not set try to create a temporary one
goto start

if not "%JWEBSOCKET_HOME%"=="" goto start
pushd ..
set JWEBSOCKET_HOME=%cd%
popd

:start

rem if JWEBSOCKET_EE_HOME not set set it to JWEBSOCKET_HOME
if "%JWEBSOCKET_EE_HOME%"=="" set JWEBSOCKET_EE_HOME=%JWEBSOCKET_HOME%

rem Allowed options:
rem -config \path\to\jWebSocket.xml
rem -home \path\to\jwebsocket_home

rem Command-line Java VM Memory options (example):
rem initial memory usage: -Xms256m
rem maximum memory usage: -Xmx2048m

rem Environment Java VM Memory options (example):
rem JAVA_OPTS=-Xms512m -Xmx2048m

cd ..
java -jar libs\jWebSocketServer-1.0.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
