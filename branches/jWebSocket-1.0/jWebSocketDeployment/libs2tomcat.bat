@echo off
echo This copies the jWebSocketServer-Bundle as well as the jWebSocketTomcatEngine 
echo from the jWbeSocket lib folder to the Tomcat lib folder for deployment.
echo Are you sure?
echo.
if "%1"=="/y" goto dontAsk1
pause
:dontAsk1
copy "%JWEBSOCKET_HOME%\libs\jWebSocketServer-Bundle-%JWEBSOCKET_VER%.jar" "%CATALINA_HOME%\lib"
copy "%JWEBSOCKET_HOME%\libs\jWebSocketTomcatEngine-%JWEBSOCKET_VER%.jar" "%CATALINA_HOME%\lib"
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2
