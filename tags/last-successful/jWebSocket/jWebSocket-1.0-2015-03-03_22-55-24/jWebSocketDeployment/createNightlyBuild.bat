@echo off
echo -------------------------------------------------------------------------
echo jWebSocket Nightly Build Generator
echo (C) Copyright 2013-2014 Innotrade GmbH
echo -------------------------------------------------------------------------

set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_51
rem set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_20

set path=%JAVA_HOME%\bin;%PATH%
rem set path=C:\Program Files\Java\jdk1.8.0_20\bin;%PATH%

echo Java Version:
java -version
echo -------------------------------------------------------------------------



if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

echo This will create the entire jWebSocket v%JWEBSOCKET_VER% Nightly Build. 
echo.
echo PLEASE ENSURE....
echo - that all build numbers are properly set (server, js client, java client)
echo - custom specific settings reset in server options?
echo - that the jWebSocket server does NOT run
echo - that NetBeans is NOT running
echo - that no folder of jWebSocket is currently in use (e.g. by Windows Explorer)
echo - that all browsers which might have jWebSocket clients running are closed
echo - that the Apache Web Server is stopped to not lock anything
echo.
echo Are you sure?
pause

set logfile=build_log.txt
echo Starting Nightly Build into %logfile%...
echo Starting Nightly Build... > %logfile%

rem goto end

rem generate the java docs (saved to client web)
rem call createJavaDocs.bat

rem create client side bundles and minified versions
echo Running 0_createJSDocs.bat...
call 0_createJSDocs.bat /y >> %logfile%

rem clean and build the project
echo Running 1_cleanAndBuildAll...
call 1_cleanAndBuildAll.bat /y >> %logfile%

rem create Run-Time-Environment
echo Running 2_createRunTimeFiles...
call 2_createRunTimeFiles.bat /y >> %logfile%

rem create download from Run-Time-Environment
echo Running 3_createDownloadFiles...
call 3_createDownloadFiles.bat /y >> %logfile%

:scan
rem scan log file for certain error tags
echo ----------------------------------------------------
fart -i %logfile% error
echo ----------------------------------------------------
echo Please check above section for error messages.
:end
pause
