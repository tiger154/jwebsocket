@echo off

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
echo This will clean and build jWebSocket v%JWEBSOCKET_VER%. Are you sure?
pause

:dontAsk1
echo -------------------------------------------------------------------------
echo Building jWebSocket Community Edition (CE)...
echo -------------------------------------------------------------------------
rem save current dir
pushd %JWEBSOCKET_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%
echo cleaning up temporary work files...
del /p /s *.?.nblh~
call mvn clean install

echo -------------------------------------------------------------------------
echo Building jWebSocket Enterprise Edition (EE)...
echo -------------------------------------------------------------------------
cd %JWEBSOCKET_EE_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%-Enterprise
call mvn clean install

echo -------------------------------------------------------------------------
echo Building jWebSocketActiveMQStockTicker...
echo -------------------------------------------------------------------------
cd %JWEBSOCKET_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%\jWebSocketActiveMQStockTicker
call mvn clean install

echo -------------------------------------------------------------------------
echo Building jWebSocketWebAppDemo...
echo -------------------------------------------------------------------------
cd %JWEBSOCKET_HOME%..\..\branches\jWebSocket-%JWEBSOCKET_VER%\jWebSocketWebAppDemo
call ant

rem restore current dir
popd

rem copy newly created libs to Tomcat's lib folder
call libs2tomcat.bat %1

rem cd jWebSocketDeployment

if "%1"=="/y" goto dontAsk2
pause
:dontAsk2
