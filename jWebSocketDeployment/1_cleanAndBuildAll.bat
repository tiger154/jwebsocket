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
echo This will clean and build jWebSocket v%JWEBSOCKET_VER%. Are you sure?
pause
:dontAsk1

rem save current dir
pushd %JWEBSOCKET_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%

rem cleanup temporary work files...
del /p /s *.?.nblh~

call mvn clean install

cd %JWEBSOCKET_EE_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%

rem cleanup temporary work files...
del /p /s *.?.nblh~

call mvn clean install

rem restore current dir
popd 

rem cd jWebSocketDeployment

if "%1"=="/y" goto dontAsk2
pause
:dontAsk2
