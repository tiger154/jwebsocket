@echo off
echo -------------------------------------------------------------------------
echo jWebSocket JavaDocs Generator
echo (C) Copyright 2011-2014 Innotrade GmbH
echo -------------------------------------------------------------------------

rem select specific maven version
set path=C:\Program Files\NetBeans 8.0 Beta\java\maven\bin;%PATH%
rem select specific Java version
rem set path=C:\Program Files\Java\jdk1.7.0_09\bin;%PATH%

set M2_HOME=C:\Program Files\NetBeans 8.0 Beta\java\maven
set M3_HOME=C:\Program Files\NetBeans 8.0 Beta\java\maven

rem set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_31

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

cd ..
set base=%CD%\
set plugins=%base%jWebSocketPlugIns\
set engines=%base%jWebSocketEngines\
set libs=%base%jWebSocketLibs\

set javadocs=%base%..\..\javadocs\

set log=%base%jWebSocketDeployment\createJavaDocs.log
del %log%
rem set log=con

if "%1"=="/y" goto dontAsk1
echo ========================================================================
echo Auto Generation of jWebSocket v%JWEBSOCKET_VER% Java Docs, are you sure?
echo ========================================================================
echo Basefolder: %base%
echo Logging to: %log%
call mvn -version
java -version
echo ========================================================================

pause
:dontAsk1

:jWebSocketDynamicSQL
echo ------------------------------------------------------------------------
echo jWebSocketDynamicSQL
echo ------------------------------------------------------------------------
cd %libs%jWebSocketDynamicSQL
if exist %javadocs%jWebSocketDynamicSQL rd %javadocs%jWebSocketDynamicSQL /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

goto end

echo ------------------------------------------------------------------------
echo jWebSocketActiveMQStockTicker
echo ------------------------------------------------------------------------
cd %base%jWebSocketActiveMQStockTicker
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketAppSrvDemo
echo ------------------------------------------------------------------------
cd %base%jWebSocketAppSrvDemo
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketClientAPI
echo ------------------------------------------------------------------------
cd %base%jWebSocketClientAPI
call mvn generate-sources javadoc:javadoc >> %log%

:jWebSocketCommon
echo ------------------------------------------------------------------------
echo jWebSocketCommon
echo ------------------------------------------------------------------------
cd %base%jWebSocketCommon
if exist %javadocs%jWebSocketCommon rd %javadocs%jWebSocketCommon /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketServer
echo ------------------------------------------------------------------------
echo jWebSocketServer
echo ------------------------------------------------------------------------
cd %base%jWebSocketServer
if exist %javadocs%jWebSocketServer rd %javadocs%jWebSocketServer /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketServerAPI
echo ------------------------------------------------------------------------
echo jWebSocketServerAPI
echo ------------------------------------------------------------------------
cd %base%jWebSocketServerAPI
if exist %javadocs%jWebSocketServerAPI rd %javadocs%jWebSocketServerAPI /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

goto plugins

:engines

:jWebSocketGrizzlyEngine
echo ------------------------------------------------------------------------
echo jWebSocketGrizzlyEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketGrizzlyEngine
if exist %javadocs%jWebSocketGrizzlyEngine rd %javadocs%jWebSocketGrizzlyEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJettyEngine
echo ------------------------------------------------------------------------
echo jWebSocketJettyEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketJettyEngine
if exist %javadocs%jWebSocketJettyEngine rd %javadocs%jWebSocketJettyEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketNettyEngine
echo ------------------------------------------------------------------------
echo jWebSocketNettyEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketNettyEngine
if exist %javadocs%jWebSocketNettyEngine rd %javadocs%jWebSocketNettyEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketTomcatEngine
echo ------------------------------------------------------------------------
echo jWebSocketTomcatEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketTomcatEngine
if exist %javadocs%jWebSocketTomcatEngine rd %javadocs%jWebSocketTomcatEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

goto end

echo ------------------------------------------------------------------------
echo jWebSocketJavaSEClient
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJavaSEClient
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSClient
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJMSGateway\jWebSocketJMSClient
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSDemoPlugIn
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJMSGateway\jWebSocketJMSDemoPlugIn
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSEndPoint
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJMSGateway\jWebSocketJMSEndPoint
call mvn generate-sources javadoc:javadoc >> %log%
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSServer
echo ------------------------------------------------------------------------
cd /d ..\jWebSocketJMSGateway\jWebSocketJMSServer
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

:plugins

:jWebSocketAdminPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketAdminPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketAdminPlugIn
if exist %javadocs%jWebSocketAdminPlugIn rd %javadocs%jWebSocketAdminPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketAPIPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketAPIPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketAPIPlugIn
if exist %javadocs%jWebSocketAPIPlugIn rd %javadocs%jWebSocketAPIPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketArduinoPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketArduinoPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketArduinoPlugIn
if exist %javadocs%jWebSocketArduinoPlugIn rd %javadocs%jWebSocketArduinoPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketBenchmarkPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketBenchmarkPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketBenchmarkPlugIn
if exist %javadocs%jWebSocketBenchmarkPlugIn rd %javadocs%jWebSocketBenchmarkPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketChannelPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketChannelPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketChannelPlugIn
if exist %javadocs%jWebSocketChannelPlugIn rd %javadocs%jWebSocketChannelPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketChatPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketChatPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketChatPlugIn
if exist %javadocs%jWebSocketChatPlugIn rd %javadocs%jWebSocketChatPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketClusterPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketClusterPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketBenchmarkPlugIn
if exist %javadocs%jWebSocketClusterPlugIn rd %javadocs%jWebSocketClusterPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketEventsPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketEventsPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketEventsPlugIn
if exist %javadocs%jWebSocketEventsPlugIn rd %javadocs%jWebSocketEventsPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketExtProcessPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketExtProcessPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketExtProcessPlugIn
if exist %javadocs%jWebSocketExtProcessPlugIn rd %javadocs%jWebSocketExtProcessPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

goto end

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

:end
rem switch back to deployment folder
cd %base%jWebSocketDeployment

echo finished! Please check if JavaDocs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2