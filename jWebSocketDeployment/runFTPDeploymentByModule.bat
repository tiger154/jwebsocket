@echo off
echo -------------------------------------------------------------------------
echo JWEBSOCKET REPO Deployment Script over FTP
echo (C) Copyright 2013-2014 Innotrade GmbH
echo -------------------------------------------------------------------------
echo           SECTION 1, PREPARING ENVIRONMENT
echo -------------------------------------------------------------------------

set MAVEN_PATH=C:\maven
set REPO_ID=mvn-jwebsocket-org
set REPO_URL=ftp://mvn.jwebsocket.org/
rem set DEPLOYMENT_VERSION=-RC3-41010
set DEPLOYMENT_VERSION=-RC3

IF NOT EXIST %MAVEN_PATH% GOTO NO_MAVEN_PATH

rem select specific maven version
set path=%MAVEN_PATH%\bin;%PATH%

set M2_HOME=%MAVEN_PATH%
set M3_HOME=%MAVEN_PATH%

if "%JWEBSOCKET_HOME%"=="" goto ERROR
if "%JWEBSOCKET_VER%"=="" goto ERROR
goto CONTINUE

:NO_MAVEN_PATH
	echo The path %MAVEN_PATH% does not exist in the filesystem, please download MAVEN and add the path in the script!
	pause
	exit

:ERROR
	echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
	pause
	exit

:CONTINUE
	echo Maven Version:
	call mvn -version
	echo -------------------------------------------------------------------------
	set orig=%CD%
	set /p option=Are you sure that you correctly configured the sections (servers and profiles) from %MAVEN_PATH%\conf\settings.xml. \n It is explained in our Maven Deployment Tutorial: https://jwebsocket.atlassian.net/wiki/display/JWSDEVSTD/Deployment#Deployment-2.3.1.POMandsettingsconfig (y/n)?
	if "%option%"=="y" goto PROCEED_DEPLOYMENT
	goto END

:PROCEED_DEPLOYMENT

setlocal EnableDelayedExpansion

set MODULES[1]=jWebSocketLibs\jWebSocketActiveMQPlugIn
set ARTIFACT_ID[1]=jWebSocketActiveMQPlugIn
set JWS_DEPLOY_VER[1]=%DEPLOYMENT_VERSION%

set MODULES[2]=jWebSocketLibs\jWebSocketDynamicSQL
set ARTIFACT_ID[2]=jWebSocketDynamicSQL
set JWS_DEPLOY_VER[2]=%DEPLOYMENT_VERSION%

set MODULES[3]=jWebSocketLibs\jWebSocketLDAP
set ARTIFACT_ID[3]=jWebSocketLDAP
set JWS_DEPLOY_VER[3]=%DEPLOYMENT_VERSION%

set MODULES[4]=jWebSocketLibs\jWebSocketSSO
set ARTIFACT_ID[4]=jWebSocketSSO
set JWS_DEPLOY_VER[4]=%DEPLOYMENT_VERSION%

set MODULES[5]=jWebSocketCommon
set ARTIFACT_ID[5]=jWebSocketCommon
set JWS_DEPLOY_VER[5]=%DEPLOYMENT_VERSION%

set MODULES[6]=jWebSocketServerAPI
set ARTIFACT_ID[6]=jWebSocketServerAPI
set JWS_DEPLOY_VER[6]=%DEPLOYMENT_VERSION%

set MODULES[7]=jWebSocketClientAPI
set ARTIFACT_ID[7]=jWebSocketClientAPI
set JWS_DEPLOY_VER[7]=%DEPLOYMENT_VERSION%

set MODULES[8]=jWebSocketJMSGateway/jWebSocketJMSEndPoint
set ARTIFACT_ID[8]=jWebSocketJMSEndPoint
set JWS_DEPLOY_VER[8]=%DEPLOYMENT_VERSION%

set MODULES[9]=jWebSocketServer
set ARTIFACT_ID[9]=jWebSocketServer
set JWS_DEPLOY_VER[9]=%DEPLOYMENT_VERSION%

set MODULES[10]=jWebSocketJavaSEClient
set ARTIFACT_ID[10]=jWebSocketJavaSEClient
set JWS_DEPLOY_VER[10]=%DEPLOYMENT_VERSION%

set MODULES[11]=jWebSocketJMSGateway/jWebSocketJMSClient
set ARTIFACT_ID[11]=jWebSocketJMSClient
set JWS_DEPLOY_VER[11]=%DEPLOYMENT_VERSION%

set MODULES[12]=jWebSocketJMSGateway/jWebSocketJMSServer
set ARTIFACT_ID[12]=jWebSocketJMSServer
set JWS_DEPLOY_VER[12]=%DEPLOYMENT_VERSION%

set MODULES[13]=jWebSocketPlugIns/jWebSocketJMSPlugIn
set ARTIFACT_ID[13]=jWebSocketJMSPlugIn
set JWS_DEPLOY_VER[13]=%DEPLOYMENT_VERSION%

set MODULES[14]=jWebSocketJMSGateway/jWebSocketJMSDemoPlugIn
set ARTIFACT_ID[14]=jWebSocketJMSDemoPlugIn
set JWS_DEPLOY_VER[14]=%DEPLOYMENT_VERSION%

set MODULES[15]=jWebSocketEngines/jWebSocketTomcatEngine
set ARTIFACT_ID[15]=jWebSocketTomcatEngine
set JWS_DEPLOY_VER[15]=%DEPLOYMENT_VERSION%

set MODULES[16]=jWebSocketEngines/jWebSocketJettyEngine
set ARTIFACT_ID[16]=jWebSocketJettyEngine
set JWS_DEPLOY_VER[16]=%DEPLOYMENT_VERSION%

set MODULES[17]=jWebSocketEngines/jWebSocketGrizzlyEngine
set ARTIFACT_ID[17]=jWebSocketGrizzlyEngine
set JWS_DEPLOY_VER[17]=%DEPLOYMENT_VERSION%

set MODULES[18]=jWebSocketSwingGUI
set ARTIFACT_ID[18]=jWebSocketSwingGUI
set JWS_DEPLOY_VER[18]=%DEPLOYMENT_VERSION%

set MODULES[19]=jWebSocketProxy
set ARTIFACT_ID[19]=jWebSocketProxy
set JWS_DEPLOY_VER[19]=%DEPLOYMENT_VERSION%

rem --------------------------------------------------------------------
rem    JWEBSOCKET PLUGINS MAY ALSO BE INCLUDED IN THE DEPLOYMENT
rem --------------------------------------------------------------------
rem jWebSocketSamples
set MODULES[20]=jWebSocketSamples
set ARTIFACT_ID[20]=jWebSocketSamples
set JWS_DEPLOY_VER[20]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketChatPlugIn
set MODULES[21]=jWebSocketChatPlugIn
set ARTIFACT_ID[21]=jWebSocketPlugIns/jWebSocketChatPlugIn
set JWS_DEPLOY_VER[21]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketSenchaPlugIn
set MODULES[22]=jWebSocketSenchaPlugIn
set ARTIFACT_ID[22]=jWebSocketPlugIns/jWebSocketSenchaPlugIn
set JWS_DEPLOY_VER[22]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketJQueryPlugIn
set MODULES[23]=jWebSocketJQueryPlugIn
set ARTIFACT_ID[23]=jWebSocketPlugIns/jWebSocketJQueryPlugIn
set JWS_DEPLOY_VER[23]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketJCaptchaPlugIn
set MODULES[24]=jWebSocketJCaptchaPlugIn
set ARTIFACT_ID[24]=jWebSocketPlugIns/jWebSocketJCaptchaPlugIn
set JWS_DEPLOY_VER[24]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketJDBCPlugIn
set MODULES[25]=jWebSocketJDBCPlugIn
set ARTIFACT_ID[25]=jWebSocketPlugIns/jWebSocketJDBCPlugIn
set JWS_DEPLOY_VER[25]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketRPCPlugIn
set MODULES[26]=jWebSocketRPCPlugIn
set ARTIFACT_ID[26]=jWebSocketPlugIns/jWebSocketRPCPlugIn
set JWS_DEPLOY_VER[26]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketRTCPlugIn
set MODULES[27]=jWebSocketRTCPlugIn
set ARTIFACT_ID[27]=jWebSocketPlugIns/jWebSocketRTCPlugIn
set JWS_DEPLOY_VER[27]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketJMXPlugIn
set MODULES[28]=jWebSocketJMXPlugIn
set ARTIFACT_ID[28]=jWebSocketPlugIns/jWebSocketJMXPlugIn
set JWS_DEPLOY_VER[28]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketSMSPlugIn
set MODULES[29]=jWebSocketSMSPlugIn
set ARTIFACT_ID[29]=jWebSocketPlugIns/jWebSocketSMSPlugIn
set JWS_DEPLOY_VER[29]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketMailPlugIn
set MODULES[30]=jWebSocketMailPlugIn
set ARTIFACT_ID[30]=jWebSocketPlugIns/jWebSocketMailPlugIn
set JWS_DEPLOY_VER[30]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketReportingPlugIn
set MODULES[31]=jWebSocketReportingPlugIn
set ARTIFACT_ID[31]=jWebSocketPlugIns/jWebSocketReportingPlugIn
set JWS_DEPLOY_VER[31]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketFileSystemPlugIn
set MODULES[32]=jWebSocketFileSystemPlugIn
set ARTIFACT_ID[32]=jWebSocketPlugIns/jWebSocketFileSystemPlugIn
set JWS_DEPLOY_VER[32]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketExtProcessPlugIn
set MODULES[33]=jWebSocketExtProcessPlugIn
set ARTIFACT_ID[33]=jWebSocketPlugIns/jWebSocketExtProcessPlugIn
set JWS_DEPLOY_VER[33]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketChannelPlugIn
set MODULES[34]=jWebSocketChannelPlugIn
set ARTIFACT_ID[34]=jWebSocketPlugIns/jWebSocketChannelPlugIn
set JWS_DEPLOY_VER[34]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketEventsPlugIn
set MODULES[35]=jWebSocketEventsPlugIn
set ARTIFACT_ID[35]=jWebSocketPlugIns/jWebSocketEventsPlugIn
set JWS_DEPLOY_VER[35]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketLoggingPlugIn
set MODULES[36]=jWebSocketLoggingPlugIn
set ARTIFACT_ID[36]=jWebSocketPlugIns/jWebSocketLoggingPlugIn
set JWS_DEPLOY_VER[36]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketStreamingPlugIn
set MODULES[37]=jWebSocketStreamingPlugIn
set ARTIFACT_ID[37]=jWebSocketPlugIns/jWebSocketStreamingPlugIn
set JWS_DEPLOY_VER[37]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketTwitterPlugIn
set MODULES[38]=jWebSocketTwitterPlugIn
set ARTIFACT_ID[38]=jWebSocketPlugIns/jWebSocketTwitterPlugIn
set JWS_DEPLOY_VER[38]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketXMPPPlugIn
set MODULES[39]=jWebSocketXMPPPlugIn
set ARTIFACT_ID[39]=jWebSocketPlugIns/jWebSocketXMPPPlugIn
set JWS_DEPLOY_VER[39]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketStatisticsPlugIn
set MODULES[40]=jWebSocketStatisticsPlugIn
set ARTIFACT_ID[40]=jWebSocketPlugIns/jWebSocketStatisticsPlugIn
set JWS_DEPLOY_VER[40]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketStockTickerPlugIn
set MODULES[41]=jWebSocketStockTickerPlugIn
set ARTIFACT_ID[41]=jWebSocketPlugIns/jWebSocketStockTickerPlugIn
set JWS_DEPLOY_VER[41]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketSharedObjectsPlugIn
set MODULES[42]=jWebSocketSharedObjectsPlugIn
set ARTIFACT_ID[42]=jWebSocketPlugIns/jWebSocketSharedObjectsPlugIn
set JWS_DEPLOY_VER[42]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketSharedCanvasPlugIn
set MODULES[43]=jWebSocketSharedCanvasPlugIn
set ARTIFACT_ID[43]=jWebSocketPlugIns/jWebSocketSharedCanvasPlugIn
set JWS_DEPLOY_VER[43]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketBenchmarkPlugIn
set MODULES[44]=jWebSocketBenchmarkPlugIn
set ARTIFACT_ID[44]=jWebSocketPlugIns/jWebSocketBenchmarkPlugIn
set JWS_DEPLOY_VER[44]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketAPIPlugIn
set MODULES[45]=jWebSocketAPIPlugIn
set ARTIFACT_ID[45]=jWebSocketPlugIns/jWebSocketAPIPlugIn
set JWS_DEPLOY_VER[45]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketProxyPlugIn
set MODULES[46]=jWebSocketProxyPlugIn
set ARTIFACT_ID[46]=jWebSocketPlugIns/jWebSocketProxyPlugIn
set JWS_DEPLOY_VER[46]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketAdminPlugIn
set MODULES[47]=jWebSocketAdminPlugIn
set ARTIFACT_ID[47]=jWebSocketPlugIns/jWebSocketAdminPlugIn
set JWS_DEPLOY_VER[47]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketArduinoPlugIn
set MODULES[48]=jWebSocketArduinoPlugIn
set ARTIFACT_ID[48]=jWebSocketPlugIns/jWebSocketArduinoPlugIn
set JWS_DEPLOY_VER[48]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketMonitoringPlugIn
set MODULES[49]=jWebSocketMonitoringPlugIn
set ARTIFACT_ID[49]=jWebSocketPlugIns/jWebSocketMonitoringPlugIn
set JWS_DEPLOY_VER[49]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketTestPlugIn
set MODULES[50]=jWebSocketTestPlugIn
set ARTIFACT_ID[50]=jWebSocketPlugIns/jWebSocketTestPlugIn
set JWS_DEPLOY_VER[50]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketCloudPlugIn
set MODULES[51]=jWebSocketCloudPlugIn
set ARTIFACT_ID[51]=jWebSocketPlugIns/jWebSocketCloudPlugIn
set JWS_DEPLOY_VER[51]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketFTPPlugIn
set MODULES[52]=jWebSocketFTPPlugIn
set ARTIFACT_ID[52]=jWebSocketPlugIns/jWebSocketFTPPlugIn
set JWS_DEPLOY_VER[52]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketLoadBalancerPlugIn
set MODULES[53]=jWebSocketLoadBalancerPlugIn
set ARTIFACT_ID[53]=jWebSocketPlugIns/jWebSocketLoadBalancerPlugIn
set JWS_DEPLOY_VER[53]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketValidatorPlugIn
set MODULES[54]=jWebSocketValidatorPlugIn
set ARTIFACT_ID[54]=jWebSocketPlugIns/jWebSocketValidatorPlugIn
set JWS_DEPLOY_VER[54]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/JWebSocketPingPongGame
set MODULES[55]=JWebSocketPingPongGame
set ARTIFACT_ID[55]=jWebSocketPlugIns/JWebSocketPingPongGame
set JWS_DEPLOY_VER[55]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketItemStorage
set MODULES[56]=jWebSocketItemStorage
set ARTIFACT_ID[56]=jWebSocketPlugIns/jWebSocketItemStorage
set JWS_DEPLOY_VER[56]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketScriptingPlugIn
set MODULES[57]=jWebSocketScriptingPlugIn
set ARTIFACT_ID[57]=jWebSocketPlugIns/jWebSocketScriptingPlugIn
set JWS_DEPLOY_VER[57]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketQuotaPlugIn
set MODULES[58]=jWebSocketQuotaPlugIn
set ARTIFACT_ID[58]=jWebSocketPlugIns/jWebSocketQuotaPlugIn
set JWS_DEPLOY_VER[58]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketPayPalPlugIn
set MODULES[59]=jWebSocketPayPalPlugIn
set ARTIFACT_ID[59]=jWebSocketPlugIns/jWebSocketPayPalPlugIn
set JWS_DEPLOY_VER[59]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketJCRPlugIn
set MODULES[60]=jWebSocketJCRPlugIn
set ARTIFACT_ID[600]=jWebSocketPlugIns/jWebSocketJCRPlugIn
set JWS_DEPLOY_VER[60]=%DEPLOYMENT_VERSION%

rem jWebSocketPlugIns/jWebSocketTTSPlugIn
set MODULES[61]=jWebSocketTTSPlugIn
set ARTIFACT_ID[61]=jWebSocketPlugIns/jWebSocketTTSPlugIn
set JWS_DEPLOY_VER[61]=%DEPLOYMENT_VERSION%

set LENGTH=61

echo -------------------------------------------------------------------------
echo               STARTING DEPLOYMENT PROCESS
echo -------------------------------------------------------------------------

for /L %%i in (7,1,%LENGTH%) do (
	if not exist "%CD%\deployment_logs\!MODULES[%%i]!" (
		mkdir %CD%\deployment_logs\!MODULES[%%i]!
	)
	echo -------------------------------------------------------------------------
	echo PROCESSING MODULE: !MODULES[%%i]!
	echo PROCESSING STAGE: %%i of %LENGTH%
	echo VERSION: %JWEBSOCKET_VER%!JWS_DEPLOY_VER[%%i]!
	echo REPOSITORY ID - URL: %REPO_ID% - %REPO_URL%
	echo Please wait until the process finishes the execution...
	call runFTPDeployment.bat !MODULES[%%i]! !JWS_DEPLOY_VER[%%i]! !ARTIFACT_ID[%%i]! > %CD%\deployment_logs\!MODULES[%%i]!\output.log
	echo REVERTING VERSION TO THE ORIGINAL %JWEBSOCKET_VER%
	pushd ..\!MODULES[%%i]!
	call mvn versions:set -DnewVersion=%JWEBSOCKET_VER% > %CD%\deployment_logs\!MODULES[%%i]!\version_reverted.log
	popd
	echo VERSION REVERTED!
	echo DEPLOYMENT PROCESS FINISHED FOR !MODULES[%%i]!
	echo PLEASE CHECK LOGS FOLDER %CD%\deployment_logs\!MODULES[%%i]!\output.log
	echo ----------------------------------------------------
)
:END
pause