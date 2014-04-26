@echo off
echo Starting the jWebSocket JMS Server...
echo (C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.
rem if JWEBSOCKET_HOME not set try to create a temporary one
goto start

if not "%JWEBSOCKET_HOME%"=="" goto start
pushd ..
set JWEBSOCKET_HOME=%cd%
popd

:start

cd ..
java -jar jWebSocketJMSServer-1.0.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
