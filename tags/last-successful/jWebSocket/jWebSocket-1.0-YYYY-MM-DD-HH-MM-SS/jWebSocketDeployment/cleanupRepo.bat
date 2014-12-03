@echo off
echo -------------------------------------------------------------------------
echo jWebSocket Maven Repo Cleanup
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

set repo=..\..\..\repo\

rem Save current directory and change to target directory
pushd %repo%
rem Save value of CD variable (current directory)
set abs_repo=%CD%\
rem Restore original directory
popd

if "%1"=="/y" goto dontAsk1
echo This deletes all packaged jars from the local SVN maven repo at
echo %abs_repo%*-%JWEBSOCKET_VER%-%JWEBSOCKET_VER%.*
echo Are you sure?
echo.
pause
:dontAsk1

del %abs_repo%*-%JWEBSOCKET_VER%-%JWEBSOCKET_VER%.* /s/q

pause