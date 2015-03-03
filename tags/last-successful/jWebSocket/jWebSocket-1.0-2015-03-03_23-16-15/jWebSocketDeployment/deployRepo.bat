@echo off
echo -------------------------------------------------------------------------
echo jWebSocket Maven Repo Deployment
echo (C) Copyright 2013-2014 Innotrade GmbH
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

set orig=%CD%

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

if "%1"=="/y" goto dontAsk1
echo This deploys all jars to the configured online repository
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
echo Are you sure?
echo.
pause
:dontAsk1

:jWebSocketDynamicSQL
cd %base%jWebSocketLibs\jWebSocketDynamicSQL
call mvn clean deploy -Dversion=1.0RC3

goto end

:jWebSocketCommon
cd %base%jWebSocketCommon
call mvn clean deploy

:jWebSocketServerAPI
cd %base%jWebSocketServerAPI
call mvn clean deploy

:jWebSocketServer
cd %base%jWebSocketServer
call mvn clean deploy

:end

cd %orig%
rem call cleanupRepo.bat

pause