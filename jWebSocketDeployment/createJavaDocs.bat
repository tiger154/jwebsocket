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

echo ------------------------------------------------------------------------
echo jWebSocketAppSrvDemo
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketAppSrvDemo
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketClientAPI
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketClientAPI
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketCommon
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketCommon
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJavaSEClient
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJavaSEClient
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJMSClient
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJMSClient
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketSamples
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketSamples
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketServer
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketServer
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketServerAPI
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketServerAPI
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketSwingGUI
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketSwingGU
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketWebAppDemo
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketWebAppDemo
call mvn generate-sources javadoc:javadoc >> %log%


rem --- Engines ---
echo ------------------------------------------------------------------------
echo jWebSocketGrizzlyEngine
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketEngines\jWebSocketGrizzlyEngine
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJettyEngine
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJettyEngine
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketNettyEngine
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketNettyEngine
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketTomcatEngine
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketTomcatEngine
call mvn generate-sources javadoc:javadoc >> %log%

rem --- Plug-ins ---
echo ------------------------------------------------------------------------
echo jWebSocketAdminPlugIn
echo ------------------------------------------------------------------------
cd /d ..\..\jWebSocketPlugins\jWebSocketAdminPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jjWebSocketAPIPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jjWebSocketAPIPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketArduinoPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketArduinoPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketBenchmarkPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketBenchmarkPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketChannelPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketChannelPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketChatPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketChatPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketClusterPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketClusterPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketEventsPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketEventsPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketExtProcessPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketExtProcessPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketFileSystemPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketFileSystemPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketItemStorage
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketItemStorage
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJCaptchaPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJCaptchaPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJDBCPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJDBCPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJMSPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJMSPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJMXPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJMXPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJQueryPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJQueryPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketLoadBalancerPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketLoadBalancerPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketLoggingPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketLoggingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketMailPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketMailPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketMonitoringPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketMonitoringPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo JWebSocketPingPongGame
echo ------------------------------------------------------------------------
cd /d ..\JWebSocketPingPongGame
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketProxyPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketProxyPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketRemoteShellPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketRemoteShellPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketReportingPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketReportingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketRPCPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketRPCPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketRTCPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketRTCPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketScriptingPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketScriptingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketSenchaPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketSenchaPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketSharedCanvasPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketSharedCanvasPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketSharedObjectsPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketSharedObjectsPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketSMSPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketSMSPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketStatisticsPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketStatisticsPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketStockTickerPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketStockTickerPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketStreamingPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketStreamingPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketTestPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketTestPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketTwitterPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketTwitterPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketXMPPPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketXMPPPlugIn
call mvn generate-sources javadoc:javadoc >> %log%

rem switch back to deployment folder
cd ..\jWebSocketDeployment

echo finished! Please check if JavaDocs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2