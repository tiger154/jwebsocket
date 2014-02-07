@echo off
echo jWebSocket Windows Service Control
echo (C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.
echo Usage jWebSocketWinSvcCtl.bat start^|stop^|delete [service name]   (default: 'jWebSocket Service')
echo.

set svc_name=jWebSocket Service
if not "%~2"=="" set svc_name=%~2

if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="delete" goto delete

echo Please pass one of the commands 'start', 'stop' or 'delete'
goto end

:start
echo Starting service '%svc_name%'...
sc start "%svc_name%"
goto end

:stop
echo Stopping service '%svc_name%'...
sc stop "%svc_name%"
goto end

:delete
echo Stopping and removing service '%svc_name%'...
sc stop "%svc_name%"
sc delete "%svc_name%"

:end
pause