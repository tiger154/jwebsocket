@echo off

if "%1"=="" goto ERROR
if "%2"=="" goto ERROR

echo ----------------------------------------------------
echo PROCESSING MODULE: %1
echo VERSION TO BE DEPLOYED: %JWEBSOCKET_VER%%2
echo MAVEN LOCATION: %MAVEN_PATH%
echo REPOSITORY ID - URL: %REPO_ID% - %REPO_URL%
echo ----------------------------------------------------

set REPO_ID=mvn-jwebsocket-org
set REPO_URL=ftp://mvn.jwebsocket.org/
set JWS_DEPLOY_VER=%2

goto READY_TO_GO

:ERROR
	echo Do not use this script directly, please use runFTPDeploymentByModule.bat!
	pause
	exit

:READY_TO_GO
	set SCRIPT_DIR=%CD%\
	set LIBS_FOLDER=%CD%\..\..\..\rte\jWebSocket-%JWEBSOCKET_VER%%JWS_DEPLOY_VER%\libs
	set repo=..\..\..\repo\
	rem Save current directory and change to target directory
	pushd %repo%
	rem Save value of CD variable (current directory)
	set abs_repo=%CD%\
	rem Restore original directory
	popd
	
	set base=%CD%\..\..\..\branches\jWebSocket-%JWEBSOCKET_VER%\%1
	rem Save current directory and change to target directory
	rem pushd %base%
	rem Save value of CD variable (current directory)
	rem set base=%CD%\
	rem Restore original directory
	rem popd
	
	echo PROJECT BASE FOLDER:
	echo %base%
	
	rem set /p option=Are you sure you want to proceed with the deployment (y/n)?
	rem if "%option%"=="y" goto PROCEED_DEPLOYMENT
	rem goto END

:JWEBSOCKET_HOME
	echo ----------------------------------------------------
	echo             SECTION 2, CHANGING VERSIONS          
	echo ----------------------------------------------------
	cd %base%
	echo Setting the version of all jWebSocket subprojects to %JWEBSOCKET_VER%%JWS_DEPLOY_VER%
	call mvn versions:set -DnewVersion=%JWEBSOCKET_VER%%JWS_DEPLOY_VER% -DprocessParent=false
	rem call mvn -N versions:update-child-modules
	rem call mvn --batch-mode release:update-versions -DdevelopmentVersion=%JWEBSOCKET_VER%%JWS_DEPLOY_VER%

:PROCEED_COMPILE
	echo ----------------------------------------------------
	echo         SECTION 3, COMPILING THE SOLUTION	        
	echo ----------------------------------------------------
	rem set /p option=Are you sure you want to compile jWebSocket-%JWEBSOCKET_VER%%JWS_DEPLOY_VER% and sub projects (y/n)?
	rem if "%option%"=="n" goto PROCEED_DEPLOY

	copy pom.xml pomSave.xml
	echo ------------------------------------------------------------------------
	echo REMOVING THE PARENT TAGS FROM THE POM.XML
	echo %SCRIPT_DIR%
	echo ------------------------------------------------------------------------
	type pomSave.xml|(%SCRIPT_DIR%repl "\s*<parent(\s[^>]*)?>([\s\S](?!</parent>))*[\s\S]</parent>" "" m) >pom.xml
	call mvn clean install

:PROCEED_DEPLOY
	echo ----------------------------------------------------
	echo         SECTION 4, DEPLOYING JARS
	echo ----------------------------------------------------
	rem set /p option=Are you sure you want to DEPLOY jWebSocket-%JWEBSOCKET_VER%%JWS_DEPLOY_VER% and sub projects to %REPO_URL% (y/n)?
	rem if "%option%"=="n" goto END
	
	echo DEPLOYING JWEBSOCKET LIBRARIES TO %REPO_URL%
	rem call mvn deploy -DaltDeploymentRepository=%REPO_ID%::default::%REPO_URL%
	call mvn deploy:deploy-file -DgroupId=org.jwebsocket -DartifactId=%3 -Dversion=%JWEBSOCKET_VER%%JWS_DEPLOY_VER% -Dpackaging=jar -Dfile=%LIBS_FOLDER%\%%3-%JWEBSOCKET_VER%%JWS_DEPLOY_VER%.jar -DpomFile=%CD%\pom.xml -DrepositoryId=%REPO_ID% -Durl=%REPO_URL%
	echo ------------------------------------------------------------------------
	echo RESTORING CHANGED POMS
	echo ------------------------------------------------------------------------
	copy pomSave.xml pom.xml
	del pomSave.xml
:END
rem call mvn versions:revert
cd %orig%