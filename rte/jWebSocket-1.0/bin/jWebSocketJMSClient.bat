@echo off
echo Starting the jWebSocket JMS Client...
echo (C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.
rem if JWEBSOCKET_HOME not set try to create a temporary one
if not "%JWEBSOCKET_HOME%"=="" goto start
pushd ..
set JWEBSOCKET_HOME=%cd%
popd

:start

rem Command-line Java VM Memory options (example):
rem initial memory usage: -Xms256m
rem maximum memory usage: -Xmx2048m

rem Environment Java VM Memory options (example):
rem JAVA_OPTS=-Xms512m -Xmx2048m

java -jar ..\libs\jWebSocketJMSClient-Bundle-1.0.jar

pause
