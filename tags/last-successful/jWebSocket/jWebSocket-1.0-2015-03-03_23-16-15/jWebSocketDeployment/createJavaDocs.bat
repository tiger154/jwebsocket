@echo off
@echo off
echo -------------------------------------------------------------------------
echo jWebSocket JavaDocs and JavaSources Generator
echo (C) Copyright 2011-2014 Innotrade GmbH
echo -------------------------------------------------------------------------

set MAVEN_HOME=C:\Program Files\NetBeans 8.0 Beta\java\maven
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_51
rem set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_20

rem select specific maven version
set path=%MAVEN_HOME%\bin;%PATH%
rem select specific Java version
set path=%JAVA_HOME%\bin;%PATH%
rem set path=C:\Program Files\Java\jdk1.8.0_20\bin;%PATH%

set M2_HOME=%MAVEN_HOME%
set M3_HOME=%MAVEN_HOME%

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_EE_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

cd ..
set base=%CD%\
set baseEE=%JWEBSOCKET_EE_HOME%..\..\branches\jWebSocket-1.0-Enterprise\
pushd %baseEE%
set baseEE=%CD%\
popd

set plugins=%base%jWebSocketPlugIns\
set pluginsEE=%baseEE%jWebSocketPlugIns\
set engines=%base%jWebSocketEngines\
set jmsgw=%base%jWebSocketJMSGateway\
set libs=%base%jWebSocketLibs\

set javadocs=%base%..\..\javadocs\

set log=%base%jWebSocketDeployment\createJavaDocs.log
del %log%
rem set log=con

if "%1"=="/y" goto dontAsk1
echo This generates the jWebSocket v%JWEBSOCKET_VER% JavaDocs and JavaSource jars, are you sure?
echo -------------------------------------------------------------------------
echo Basefolder (CE):
echo %base%
echo Basefolder (EE):
echo %baseEE%
echo -------------------------------------------------------------------------
echo Logging to:
echo %log%
echo -------------------------------------------------------------------------
echo Maven Version:
call mvn -version
echo -------------------------------------------------------------------------
echo Java Version:
java -version
echo -------------------------------------------------------------------------

pause
:dontAsk1

rem @echo on
rem goto jWebSocketProxyPlugIn

:jWebSocketItemStorageEE
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketItemStorage
echo ------------------------------------------------------------------------
cd %pluginsEE%jWebSocketItemStoragePlugInEE
if exist %javadocs%jWebSocketItemStoragePlugInEE rd %javadocs%jWebSocketItemStoragePlugInEE /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketOntologyPlugInEE
echo ------------------------------------------------------------------------
echo PlugInsEE/jWebSocketOntologyPlugInEE
echo ------------------------------------------------------------------------
cd %pluginsEE%jWebSocketOntologyPlugInEE
if exist %javadocs%jWebSocketOntologyPlugInEE rd %javadocs%jWebSocketOntologyPlugInEE /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketANTLRPlugInEE
echo ------------------------------------------------------------------------
echo PlugInsEE/jWebSocketANTLRPlugInEE
echo ------------------------------------------------------------------------
cd %pluginsEE%jWebSocketANTLRPlugInEE
if exist %javadocs%jWebSocketANTLRPlugInEE rd %javadocs%jWebSocketANTLRPlugInEE /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketStringTemplatePlugInEE
echo ------------------------------------------------------------------------
echo PlugInsEE/jWebSocketStringTemplatePlugInEE
echo ------------------------------------------------------------------------
cd %pluginsEE%jWebSocketStringTemplatePlugInEE
if exist %javadocs%jWebSocketStringTemplatePlugInEE rd %javadocs%jWebSocketStringTemplatePlugInEE /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketMASPlugInEE
echo ------------------------------------------------------------------------
echo PlugInsEE/jWebSocketMASPlugInEE
echo ------------------------------------------------------------------------
cd %pluginsEE%jWebSocketMASPlugInEE
if exist %javadocs%jWebSocketMASPlugInEE rd %javadocs%jWebSocketMASPlugInEE /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketEnapsoPlugInEE
echo ------------------------------------------------------------------------
echo PlugInsEE/jWebSocketEnapsoPlugInEE
echo ------------------------------------------------------------------------
cd %pluginsEE%jWebSocketEnapsoPlugInEE
if exist %javadocs%jWebSocketEnapsoPlugInEE rd %javadocs%jWebSocketEnapsoPlugInEE /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketBPMNPlugInEE
echo ------------------------------------------------------------------------
echo PlugInsEE/jWebSocketBPMNPlugInEE
echo ------------------------------------------------------------------------
cd %pluginsEE%jWebSocketBPMNPlugInEE
if exist %javadocs%jWebSocketBPMNPlugInEE rd %javadocs%jWebSocketBPMNPlugInEE /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketDynamicSQL
echo ------------------------------------------------------------------------
echo Libraries/jWebSocketDynamicSQL
echo ------------------------------------------------------------------------
cd %libs%jWebSocketDynamicSQL
if exist %javadocs%jWebSocketDynamicSQL rd %javadocs%jWebSocketDynamicSQL /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSSO
echo ------------------------------------------------------------------------
echo Libraries/jWebSocketSSO
echo ------------------------------------------------------------------------
cd %libs%jWebSocketSSO
if exist %javadocs%jWebSocketSSO rd %javadocs%jWebSocketSSO /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketLDAP
echo ------------------------------------------------------------------------
echo Libraries/jWebSocketLDAP
echo ------------------------------------------------------------------------
cd %libs%jWebSocketLDAP
if exist %javadocs%jWebSocketLDAP rd %javadocs%jWebSocketLDAP /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketCommon
echo ------------------------------------------------------------------------
echo Core/jWebSocketCommon
echo ------------------------------------------------------------------------
cd %base%jWebSocketCommon
if exist %javadocs%jWebSocketCommon rd %javadocs%jWebSocketCommon /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketServerAPI
echo ------------------------------------------------------------------------
echo Core/jWebSocketServerAPI
echo ------------------------------------------------------------------------
cd %base%jWebSocketServerAPI
if exist %javadocs%jWebSocketServerAPI rd %javadocs%jWebSocketServerAPI /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketServer
echo ------------------------------------------------------------------------
echo Core/jWebSocketServer
echo ------------------------------------------------------------------------
cd %base%jWebSocketServer
if exist %javadocs%jWebSocketServer rd %javadocs%jWebSocketServer /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketClientAPI
echo ------------------------------------------------------------------------
echo Core/jWebSocketClientAPI
echo ------------------------------------------------------------------------
cd %base%jWebSocketClientAPI
if exist %javadocs%jWebSocketClientAPI rd %javadocs%jWebSocketClientAPI /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJavaSEClient
echo ------------------------------------------------------------------------
echo Core/jWebSocketJavaSEClient
echo ------------------------------------------------------------------------
cd %base%jWebSocketJavaSEClient
if exist %javadocs%jWebSocketJavaSEClient rd %javadocs%jWebSocketJavaSEClient /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSwingGUI
echo ------------------------------------------------------------------------
echo Core/jWebSocketSwingGUI
echo ------------------------------------------------------------------------
cd %base%jWebSocketSwingGUI
if exist %javadocs%jWebSocketSwingGUI rd %javadocs%jWebSocketSwingGUI /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:engines

:jWebSocketGrizzlyEngine
echo ------------------------------------------------------------------------
echo Engines/jWebSocketGrizzlyEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketGrizzlyEngine
if exist %javadocs%jWebSocketGrizzlyEngine rd %javadocs%jWebSocketGrizzlyEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJettyEngine
echo ------------------------------------------------------------------------
echo Engines/jWebSocketJettyEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketJettyEngine
if exist %javadocs%jWebSocketJettyEngine rd %javadocs%jWebSocketJettyEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketNettyEngine
echo ------------------------------------------------------------------------
echo Engines/jWebSocketNettyEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketNettyEngine
if exist %javadocs%jWebSocketNettyEngine rd %javadocs%jWebSocketNettyEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketTomcatEngine
echo ------------------------------------------------------------------------
echo Engines/jWebSocketTomcatEngine
echo ------------------------------------------------------------------------
cd %engines%jWebSocketTomcatEngine
if exist %javadocs%jWebSocketTomcatEngine rd %javadocs%jWebSocketTomcatEngine /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSGateway

:jWebSocketJMSClient
echo ------------------------------------------------------------------------
echo JMS Gateway/jWebSocketJMSClient
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSClient
if exist %javadocs%jWebSocketJMSClient rd %javadocs%jWebSocketJMSClient /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSDemoPlugIn
echo ------------------------------------------------------------------------
echo JMS Gateway/jWebSocketJMSDemoPlugIn
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSDemoPlugIn
if exist %javadocs%jWebSocketJMSDemoPlugIn rd %javadocs%jWebSocketJMSDemoPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSEndPoint
echo ------------------------------------------------------------------------
echo JMS Gateway/jWebSocketJMSEndPoint
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSEndPoint
if exist %javadocs%jWebSocketJMSEndPoint rd %javadocs%jWebSocketJMSEndPoint /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSServer
echo ------------------------------------------------------------------------
echo JMS Gateway/jWebSocketJMSServer
echo ------------------------------------------------------------------------
cd %jmsgw%jWebSocketJMSServer
if exist %javadocs%jWebSocketJMSServer rd %javadocs%jWebSocketJMSServer /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:plugins

:jWebSocketAdminPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketAdminPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketAdminPlugIn
if exist %javadocs%jWebSocketAdminPlugIn rd %javadocs%jWebSocketAdminPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketAPIPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketAPIPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketAPIPlugIn
if exist %javadocs%jWebSocketAPIPlugIn rd %javadocs%jWebSocketAPIPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketArduinoPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketArduinoPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketArduinoPlugIn
if exist %javadocs%jWebSocketArduinoPlugIn rd %javadocs%jWebSocketArduinoPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketBenchmarkPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketBenchmarkPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketBenchmarkPlugIn
if exist %javadocs%jWebSocketBenchmarkPlugIn rd %javadocs%jWebSocketBenchmarkPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketChannelPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketChannelPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketChannelPlugIn
if exist %javadocs%jWebSocketChannelPlugIn rd %javadocs%jWebSocketChannelPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketChatPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketChatPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketChatPlugIn
if exist %javadocs%jWebSocketChatPlugIn rd %javadocs%jWebSocketChatPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketCloudPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketCloudPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketCloudPlugIn
if exist %javadocs%jWebSocketCloudPlugIn rd %javadocs%jWebSocketCloudPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketEventsPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketEventsPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketEventsPlugIn
if exist %javadocs%jWebSocketEventsPlugIn rd %javadocs%jWebSocketEventsPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketExtProcessPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketExtProcessPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketExtProcessPlugIn
if exist %javadocs%jWebSocketExtProcessPlugIn rd %javadocs%jWebSocketExtProcessPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketFileSystemPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketFileSystemPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketFileSystemPlugIn
if exist %javadocs%jWebSocketFileSystemPlugIn rd %javadocs%jWebSocketFileSystemPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketFTPPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketFTPPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketFTPPlugIn
if exist %javadocs%jWebSocketFTPPlugIn rd %javadocs%jWebSocketFTPPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketItemStorage
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketItemStorage
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketItemStorage
if exist %javadocs%jWebSocketItemStorage rd %javadocs%jWebSocketItemStorage /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJCaptchaPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketJCaptchaPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJCaptchaPlugIn
if exist %javadocs%jWebSocketJCaptchaPlugIn rd %javadocs%jWebSocketJCaptchaPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJCRPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketJCRPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJCRPlugIn
if exist %javadocs%jWebSocketJCRPlugIn rd %javadocs%jWebSocketJCRPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJDBCPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketJDBCPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJDBCPlugIn
if exist %javadocs%jWebSocketJDBCPlugIn rd %javadocs%jWebSocketJDBCPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMSPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketJMSPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJMSPlugIn
if exist %javadocs%jWebSocketJMSPlugIn rd %javadocs%jWebSocketJMSPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJMXPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketJMXPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJMXPlugIn
if exist %javadocs%jWebSocketJMXPlugIn rd %javadocs%jWebSocketJMXPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketJQueryPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketJQueryPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketJQueryPlugIn
if exist %javadocs%jWebSocketJQueryPlugIn rd %javadocs%jWebSocketJQueryPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketLoadBalancerPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketLoadBalancerPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketLoadBalancerPlugIn
if exist %javadocs%jWebSocketLoadBalancerPlugIn rd %javadocs%jWebSocketLoadBalancerPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketLoggingPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketLoggingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketLoggingPlugIn
if exist %javadocs%jWebSocketLoggingPlugIn rd %javadocs%jWebSocketLoggingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketMailPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketMailPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketMailPlugIn
if exist %javadocs%jWebSocketMailPlugIn rd %javadocs%jWebSocketMailPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketMonitoringPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketMonitoringPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketMonitoringPlugIn
if exist %javadocs%jWebSocketMonitoringPlugIn rd %javadocs%jWebSocketMonitoringPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketPingPongGame
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketPingPongGame
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketPingPongGame
if exist %javadocs%jWebSocketPingPongGame rd %javadocs%jWebSocketPingPongGame /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketProxyPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketProxyPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketProxyPlugIn
if exist %javadocs%jWebSocketProxyPlugIn rd %javadocs%jWebSocketProxyPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketQuotaPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketQuotaPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketQuotaPlugIn
if exist %javadocs%jWebSocketQuotaPlugIn rd %javadocs%jWebSocketQuotaPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketReportingPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketReportingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketReportingPlugIn
if exist %javadocs%jWebSocketReportingPlugIn rd %javadocs%jWebSocketReportingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketRPCPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketRPCPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketRPCPlugIn
if exist %javadocs%jWebSocketRPCPlugIn rd %javadocs%jWebSocketRPCPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketRTCPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketRTCPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketRTCPlugIn
if exist %javadocs%jWebSocketRTCPlugIn rd %javadocs%jWebSocketRTCPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketScriptingPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketScriptingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketScriptingPlugIn
if exist %javadocs%jWebSocketScriptingPlugIn rd %javadocs%jWebSocketScriptingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSenchaPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketSenchaPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketSenchaPlugIn
if exist %javadocs%jWebSocketSenchaPlugIn rd %javadocs%jWebSocketSenchaPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSharedObjectsPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketSharedObjectsPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketSharedObjectsPlugIn
if exist %javadocs%jWebSocketSharedObjectsPlugIn rd %javadocs%jWebSocketSharedObjectsPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSMSPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketSMSPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketSMSPlugIn
if exist %javadocs%jWebSocketSMSPlugIn rd %javadocs%jWebSocketSMSPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketStatisticsPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketStatisticsPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketStatisticsPlugIn
if exist %javadocs%jWebSocketStatisticsPlugIn rd %javadocs%jWebSocketStatisticsPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketStockTickerPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketStockTickerPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketStockTickerPlugIn
if exist %javadocs%jWebSocketStockTickerPlugIn rd %javadocs%jWebSocketStockTickerPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketStreamingPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketStreamingPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketStreamingPlugIn
if exist %javadocs%jWebSocketStreamingPlugIn rd %javadocs%jWebSocketStreamingPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketTestPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketTestPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketTestPlugIn
if exist %javadocs%jWebSocketTestPlugIn rd %javadocs%jWebSocketTestPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketTwitterPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketTwitterPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketTwitterPlugIn
if exist %javadocs%jWebSocketTwitterPlugIn rd %javadocs%jWebSocketTwitterPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketValidatorPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketValidatorPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketValidatorPlugIn
if exist %javadocs%jWebSocketValidatorPlugIn rd %javadocs%jWebSocketValidatorPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketXMPPPlugIn
echo ------------------------------------------------------------------------
echo Plug-ins/jWebSocketXMPPPlugIn
echo ------------------------------------------------------------------------
cd %plugins%jWebSocketXMPPPlugIn
if exist %javadocs%jWebSocketXMPPPlugIn rd %javadocs%jWebSocketXMPPPlugIn /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:appserver

:jWebSocketAppSrvDemo
echo ------------------------------------------------------------------------
echo AppServer/jWebSocketAppSrvDemo
echo ------------------------------------------------------------------------
cd %base%jWebSocketAppSrvDemo
if exist %javadocs%jWebSocketAppSrvDemo rd %javadocs%jWebSocketAppSrvDemo /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketWebAppDemo
echo ------------------------------------------------------------------------
echo AppServer/jWebSocketWebAppDemo
echo ------------------------------------------------------------------------
rem ### THE WEBAPP DEMO IS ANT BASED! ###
cd %base%jWebSocketWebAppDemo
call ant javadoc >> %log%
if exist %javadocs%jWebSocketWebAppDemo rd %javadocs%jWebSocketWebAppDemo /s/q
md %javadocs%jWebSocketWebAppDemo\apidocs
xcopy %base%jWebSocketWebAppDemo\dist\javadoc\*.* %javadocs%jWebSocketWebAppDemo\apidocs\ /s /i /y >> %log%

:others

:jWebSocketActiveMQStockTicker
echo ------------------------------------------------------------------------
echo Others/jWebSocketActiveMQStockTicker
echo ------------------------------------------------------------------------
cd %base%jWebSocketActiveMQStockTicker
if exist %javadocs%jWebSocketActiveMQStockTicker rd %javadocs%jWebSocketActiveMQStockTicker /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:jWebSocketSamples
echo ------------------------------------------------------------------------
echo Others/jWebSocketSamples
echo ------------------------------------------------------------------------
cd %base%jWebSocketSamples
if exist %javadocs%jWebSocketSamples rd %javadocs%jWebSocketSamples /s/q
call mvn generate-sources javadoc:javadoc >> %log%
call mvn generate-sources javadoc:jar >> %log%
call mvn source:jar >> %log%

:end

rem switch back to deployment folder
cd %base%jWebSocketDeployment

echo finished! Please check if JavaDocs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2