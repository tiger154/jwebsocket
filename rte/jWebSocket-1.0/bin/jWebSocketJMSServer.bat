@echo off
echo Starting the jWebSocket JMS Server...
echo (C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.
rem if JWEBSOCKET_HOME not set try to create a temporary one
if not "%JWEBSOCKET_HOME%"=="" goto start
pushd ..
set JWEBSOCKET_HOME=%cd%
popd

:start

java -jar ..\libs\jWebSocketJMSServerBundle-1.0.jar tcp://hqdvalsap01:61616 org.jwebsocket.jms.gateway org.jwebsocket.jms.gateway jWebSocketJMSService

pause
