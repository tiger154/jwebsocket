@echo off
echo -------------------------------------------------------------------------
echo jWebSocket JavaDocs and JavaSources Generator
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
set jmsgw=%base%jWebSocketJMSGateway\
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

goto jWebSocketRPCPlugIn

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

:jWebSocketJMSGateway

:jWebSocketJMSClient
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSClient
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSClient
if exist %javadocs%jWebSocketJMSClient rd %javadocs%jWebSocketJMSClient /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSDemoPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSDemoPlugIn
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSDemoPlugIn
if exist %javadocs%jWebSocketJMSDemoPlugIn rd %javadocs%jWebSocketJMSDemoPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSEndPoint
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSEndPoint
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSEndPoint
if exist %javadocs%jWebSocketJMSEndPoint rd %javadocs%jWebSocketJMSEndPoint /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSServer
echo ------------------------------------------------------------------------
echo jWebSocketJMSGateway/jWebSocketJMSServer
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSServer
if exist %javadocs%jWebSocketJMSServer rd %javadocs%jWebSocketJMSServer /s/q
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
echo jWebSocketPlugIns/jWebSocketAdminPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketAdminPlugIn
if exist %javadocs%jWebSocketAdminPlugIn rd %javadocs%jWebSocketAdminPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketAPIPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketAPIPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketAPIPlugIn
if exist %javadocs%jWebSocketAPIPlugIn rd %javadocs%jWebSocketAPIPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketArduinoPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketArduinoPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketArduinoPlugIn
if exist %javadocs%jWebSocketArduinoPlugIn rd %javadocs%jWebSocketArduinoPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketBenchmarkPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketBenchmarkPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketBenchmarkPlugIn
if exist %javadocs%jWebSocketBenchmarkPlugIn rd %javadocs%jWebSocketBenchmarkPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketChannelPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketChannelPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketChannelPlugIn
if exist %javadocs%jWebSocketChannelPlugIn rd %javadocs%jWebSocketChannelPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketChatPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketChatPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketChatPlugIn
if exist %javadocs%jWebSocketChatPlugIn rd %javadocs%jWebSocketChatPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketClusterPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketClusterPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketBenchmarkPlugIn
if exist %javadocs%jWebSocketClusterPlugIn rd %javadocs%jWebSocketClusterPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketEventsPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketEventsPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketEventsPlugIn
if exist %javadocs%jWebSocketEventsPlugIn rd %javadocs%jWebSocketEventsPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketExtProcessPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketExtProcessPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketExtProcessPlugIn
if exist %javadocs%jWebSocketExtProcessPlugIn rd %javadocs%jWebSocketExtProcessPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketFileSystemPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketFileSystemPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketFileSystemPlugIn
if exist %javadocs%jWebSocketFileSystemPlugIn rd %javadocs%jWebSocketFileSystemPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketItemStorage
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketItemStorage
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketItemStorage
if exist %javadocs%jWebSocketItemStorage rd %javadocs%jWebSocketItemStorage /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJCaptchaPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketJCaptchaPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJCaptchaPlugIn
if exist %javadocs%jWebSocketJCaptchaPlugIn rd %javadocs%jWebSocketJCaptchaPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

rem :jWebSocketJCRPlugIn
rem echo ------------------------------------------------------------------------
rem echo jWebSocketPlugIns/jWebSocketJCRPlugIn
rem echo ------------------------------------------------------------------------
rem cd %plugins%jWebSocketJCRPlugIn
rem if exist %javadocs%jWebSocketJCRPlugIn rd %javadocs%jWebSocketJCRPlugIn /s/q
rem call mvn generate-sources javadoc:javadoc >> %log%
rem call mvn generate-sources javadoc:jar >> %log%
rem call mvn source:jar >> %log%

:jWebSocketJDBCPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketJDBCPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJDBCPlugIn
if exist %javadocs%jWebSocketJDBCPlugIn rd %javadocs%jWebSocketJDBCPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketJMSPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJMSPlugIn
if exist %javadocs%jWebSocketJMSPlugIn rd %javadocs%jWebSocketJMSPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMXPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketJMXPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJMXPlugIn
if exist %javadocs%jWebSocketJMXPlugIn rd %javadocs%jWebSocketJMXPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJQueryPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketJQueryPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJQueryPlugIn
if exist %javadocs%jWebSocketJQueryPlugIn rd %javadocs%jWebSocketJQueryPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketLoadBalancerPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketLoadBalancerPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketLoadBalancerPlugIn
if exist %javadocs%jWebSocketLoadBalancerPlugIn rd %javadocs%jWebSocketLoadBalancerPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketLoggingPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketLoggingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketLoggingPlugIn
if exist %javadocs%jWebSocketLoggingPlugIn rd %javadocs%jWebSocketLoggingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketMailPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketMailPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketMailPlugIn
if exist %javadocs%jWebSocketMailPlugIn rd %javadocs%jWebSocketMailPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketMonitoringPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketMonitoringPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketMonitoringPlugIn
if exist %javadocs%jWebSocketMonitoringPlugIn rd %javadocs%jWebSocketMonitoringPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketPingPongGame
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketPingPongGame
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketPingPongGame
if exist %javadocs%jWebSocketPingPongGame rd %javadocs%jWebSocketPingPongGame /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketProxyPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketProxyPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketProxyPlugIn
if exist %javadocs%jWebSocketProxyPlugIn rd %javadocs%jWebSocketProxyPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketQuotaPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketQuotaPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketQuotaPlugIn
if exist %javadocs%jWebSocketQuotaPlugIn rd %javadocs%jWebSocketQuotaPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketReportingPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketReportingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketReportingPlugIn
if exist %javadocs%jWebSocketReportingPlugIn rd %javadocs%jWebSocketReportingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketRPCPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketRPCPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketRPCPlugIn
if exist %javadocs%jWebSocketRPCPlugIn rd %javadocs%jWebSocketRPCPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketRTCPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketRTCPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketRTCPlugIn
if exist %javadocs%jWebSocketRTCPlugIn rd %javadocs%jWebSocketRTCPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketScriptingPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketScriptingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketScriptingPlugIn
if exist %javadocs%jWebSocketScriptingPlugIn rd %javadocs%jWebSocketScriptingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSenchaPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketSenchaPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketSenchaPlugIn
if exist %javadocs%jWebSocketSenchaPlugIn rd %javadocs%jWebSocketSenchaPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSharedObjectsPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketSharedObjectsPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketSharedObjectsPlugIn
if exist %javadocs%jWebSocketSharedObjectsPlugIn rd %javadocs%jWebSocketSharedObjectsPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSMSPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketSMSPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketSMSPlugIn
if exist %javadocs%jWebSocketSMSPlugIn rd %javadocs%jWebSocketSMSPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketStatisticsPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketStatisticsPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketStatisticsPlugIn
if exist %javadocs%jWebSocketStatisticsPlugIn rd %javadocs%jWebSocketStatisticsPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketStockTickerPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketStockTickerPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketStockTickerPlugIn
if exist %javadocs%jWebSocketStockTickerPlugIn rd %javadocs%jWebSocketStockTickerPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketStreamingPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketStreamingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketStreamingPlugIn
if exist %javadocs%jWebSocketStreamingPlugIn rd %javadocs%jWebSocketStreamingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketTestPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketTestPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketTestPlugIn
if exist %javadocs%jWebSocketTestPlugIn rd %javadocs%jWebSocketTestPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketTwitterPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketTwitterPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketTwitterPlugIn
if exist %javadocs%jWebSocketTwitterPlugIn rd %javadocs%jWebSocketTwitterPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketValidatorPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketValidatorPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketValidatorPlugIn
if exist %javadocs%jWebSocketValidatorPlugIn rd %javadocs%jWebSocketValidatorPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketXMPPPlugIn
echo ------------------------------------------------------------------------
echo jWebSocketPlugIns/jWebSocketXMPPPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketXMPPPlugIn
if exist %javadocs%jWebSocketXMPPPlugIn rd %javadocs%jWebSocketXMPPPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

goto end

:end
rem switch back to deployment folder
cd %base%jWebSocketDeployment

echo finished! Please check if JavaDocs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2