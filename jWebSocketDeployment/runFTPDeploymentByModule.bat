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
set DEPLOYMENT_VERSION=-RC3-41010

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

set MODULES[8]=jWebSocketServer
set ARTIFACT_ID[8]=jWebSocketServer
set JWS_DEPLOY_VER[8]=%DEPLOYMENT_VERSION%

set MODULES[9]=jWebSocketJavaSEClient
set ARTIFACT_ID[9]=jWebSocketJavaSEClient
set JWS_DEPLOY_VER[9]=%DEPLOYMENT_VERSION%

set LENGTH=9

echo -------------------------------------------------------------------------
echo               STARTING DEPLOYMENT PROCESS
echo -------------------------------------------------------------------------

for /L %%i in (1,1,%LENGTH%) do (
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