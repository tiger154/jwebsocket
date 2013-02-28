@echo off
echo This copies the jWebSocketServer-Bundle as well as the jWebSocketTomcatEngine 
echo from the jWbeSocket lib folder to the Tomcat lib folder for deployment.
echo Are you sure?
echo.
pause
rem copy "%JWEBSOCKET_HOME%\libs\jWebSocketServer-Bundle-1.0.jar" "%CATALINA_HOME%\lib"
copy "%JWEBSOCKET_HOME%\libs\jWebSocketServer-1.0.jar" "%CATALINA_HOME%\lib"
copy "%JWEBSOCKET_HOME%\libs\jWebSocketServerAPI-1.0.jar" "%CATALINA_HOME%\lib"
copy "%JWEBSOCKET_HOME%\libs\jWebSocketCommon-1.0.jar" "%CATALINA_HOME%\lib"
copy "%JWEBSOCKET_HOME%\libs\jWebSocketTomcatEngine-1.0.jar" "%CATALINA_HOME%\lib"
pause