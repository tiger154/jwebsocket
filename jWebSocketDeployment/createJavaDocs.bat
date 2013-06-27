@echo off

set path=C:\Program Files\NetBeans 7.3\java\maven\bin;%PATH%
set path=C:\Program Files\Java\jdk1.6.0_31\bin;%PATH%

set M2_HOME=C:\Program Files\NetBeans 7.3\java\maven
set M3_HOME=C:\Program Files\NetBeans 7.3\java\maven

set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_31

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

if "%1"=="/y" goto dontAsk1
echo ========================================================================
echo Auto Generation of jWebSocket v%JWEBSOCKET_VER% Java Docs, are you sure?
echo ========================================================================
call mvn -version
java -version
echo ========================================================================

pause
:dontAsk1

rem set log=..\jWebSocketDeployment\createJavaDocs.log
set log=con


rem --- Main Modules ---

cd /d ..\jWebSocketClientAPI
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketCommon
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketJavaSEClient
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketServer
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketServerAPI
call mvn generate-sources javadoc:javadoc >> %log%


rem --- Engines ---
cd /d ..\jWebSocketEngines\jWebSocketGrizzlyEngine
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketJettyEngine
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketNettyEngine
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketTomcatEngine
call mvn generate-sources javadoc:javadoc >> %log%

rem --- Plug-ins ---
cd /d ..\..\jWebSocketPlugins\jWebSocketAdminPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketAPIPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketArduinoPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketBenchmarkPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketChannelPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketChatPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketClusterPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketEventsPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketExtProcessPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketFileSystemPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketItemStorage
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketJCaptchaPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketJDBCPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketJMSPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketJMXPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketJQueryPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketLoadBalancerPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketLoggingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketMailPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketMonitoringPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\JWebSocketPingPongGame
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketProxyPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketRemoteShellPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketReportingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketRPCPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketRTCPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketScriptingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketSenchaPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketSharedCanvasPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketSharedObjectsPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketSMSPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketStatisticsPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketStockTickerPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketStreamingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketTestPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketTwitterPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketPlugins\jWebSocketXMPPPlugIn
call mvn generate-sources javadoc:javadoc >> %log%

goto end

cd /d ..\jWebSocketSamples
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketAppServer
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketAppSrvDemo
call mvn generate-sources javadoc:javadoc >> %log%
cd /d ..\jWebSocketSwingGUI
call mvn generate-sources javadoc:javadoc >> %log%

cd /d ..\jWebSocketJetty
call mvn generate-sources javadoc:javadoc >> %log%


:end

rem switch back to deployment folder
cd ..\jWebSocketDeployment

echo finished! Please check if JavaDocs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2