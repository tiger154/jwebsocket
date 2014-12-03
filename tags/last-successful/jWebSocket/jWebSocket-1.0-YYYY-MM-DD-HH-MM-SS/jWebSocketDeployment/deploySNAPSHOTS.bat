@echo off
echo -------------------------------------------------------------------------
echo SONATYPE Maven Deployment Script
echo (C) Copyright 2013-2014 Innotrade GmbH
echo -------------------------------------------------------------------------

echo ----------------------------------------------------
echo           SECTION 1, PREPARING ENVIRONMENT        
echo ----------------------------------------------------

set MAVEN_PATH=C:\maven
set REPO_ID=sonatype-nexus-snapshots
set REPO_URL=https://oss.sonatype.org/content/repositories/snapshots

IF NOT EXIST %MAVEN_PATH% GOTO NO_MAVEN_PATH

rem select specific maven version
set path=%MAVEN_PATH%\bin;%PATH%

set M2_HOME=%MAVEN_PATH%
set M3_HOME=%MAVEN_PATH%

rem set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_31

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
	set orig=%CD%
	set /p option=Are you sure that you correctly configured the sections (servers and profiles) from %MAVEN_PATH%\conf\settings.xml as explained in our Maven Deployment Tutorial: https://jwebsocket.atlassian.net/wiki/display/JWSDEVSTD/Deployment#Deployment-2.3.1.POMandsettingsconfig (y/n)?
	if "%option%"=="y" goto MAVEN_CONFIGURED
	goto END

:MAVEN_CONFIGURED
	set repo=..\..\..\repo\
	rem Save current directory and change to target directory
	pushd %repo%
	rem Save value of CD variable (current directory)
	set abs_repo=%CD%\
	rem Restore original directory
	popd

	set base=..\..\..\branches\jWebSocket-%JWEBSOCKET_VER%\
	rem Save current directory and change to target directory
	pushd %base%
	rem Save value of CD variable (current directory)
	set base=%CD%\
	rem Restore original directory
	popd

	echo This deploys all jars as SNAPSHOTS to the repository %REPO_ID%: "%REPO_URL%"
	echo -------------------------------------------------------------------------
	echo Project Basefolder: 
	echo %base%
	echo -------------------------------------------------------------------------
	echo Maven Version:
	call mvn -version
	echo -------------------------------------------------------------------------
	echo Java Version:
	java -version
	echo -------------------------------------------------------------------------
	set /p option=Are you sure you want to proceed with the deployment (y/n)?
	if "%option%"=="y" goto PROCEED_DEPLOYMENT
	goto END

:PROCEED_DEPLOYMENT

:JWEBSOCKET_HOME
	echo ----------------------------------------------------
	echo             SECTION 2, CHANGING VERSIONS          
	echo ----------------------------------------------------
	cd %base%
	echo Setting the version of all jWebSocket subprojects to %JWEBSOCKET_VER%-SNAPSHOT
	call mvn versions:set -DnewVersion=%JWEBSOCKET_VER%-SNAPSHOT
	call mvn -N versions:update-child-modules
	call mvn --batch-mode release:update-versions -DdevelopmentVersion=%JWEBSOCKET_VER%-SNAPSHOT

:PROCEED_COMPILE
	echo ----------------------------------------------------
	echo         SECTION 3, COMPILING THE SOLUTION	        
	echo ----------------------------------------------------

	set /p option=Are you sure you want to compile jWebSocket-%JWEBSOCKET_VER%-SNAPSHOT and sub projects (y/n)?
	if "%option%"=="n" goto PROCEED_DEPLOY
	call mvn clean install
	
:PROCEED_DEPLOY
	echo ----------------------------------------------------
	echo         SECTION 4, DEPLOYING SNAPSHOTS
	echo ----------------------------------------------------
	set /p option=Are you sure you want to DEPLOY jWebSocket-%JWEBSOCKET_VER%-SNAPSHOT and sub projects to %REPO_URL% (y/n)?
	if "%option%"=="n" goto END
	
	echo DEPLOYING JWEBSOCKET LIBRARIES TO %REPO_URL%
	call mvn clean deploy -DaltDeploymentRepository=%REPO_ID%::default::%REPO_URL%

:END
call mvn versions:revert
cd %orig%

pause