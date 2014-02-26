@echo off
echo -------------------------------------------------------------------------
echo jWebSocket Maven Repo Deployment
echo (C) Copyright 2013-2014 Innotrade GmbH
echo -------------------------------------------------------------------------

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
echo Project Basefolder: %base%
echo Are you sure?
echo.
pause
:dontAsk1

cd %base%jWebSocketLibs\jWebSocketDynamicSQL
call mvn clean deploy

cd %base%jWebSocketCommon
call mvn clean deploy

cd %base%jWebSocketServerAPI
call mvn clean deploy

cd %base%jWebSocketServer
call mvn clean deploy

cd %orig%
call cleanupRepo.bat

pause