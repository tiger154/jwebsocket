@echo off
echo Starting the jWebSocket Server in the current path, 
echo temporarily overwriting JWEBSOCKET_HOME and JWEBSOCKET_EE_HOME!
echo (C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.

pushd ..
set JWEBSOCKET_HOME=%cd%\
set JWEBSOCKET_EE_HOME=%cd%\
popd

:start
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
