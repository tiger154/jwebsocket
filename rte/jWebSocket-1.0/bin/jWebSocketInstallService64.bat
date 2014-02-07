@echo off
echo jWebSocket Windows Service Installer (64bit)
echo (C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.
echo Usage jWebSocketInstallService64.bat [service name]   (default: 'jWebSocket Service')
echo.
set svc_name=jWebSocket Service
if not "%~1"=="" set svc_name=%~1
echo Installing jWebSocket Server (64bit) as Windows service (service name '%svc_name%')...
jWebSocketService64.exe /install non-interactive %svc_name%
rem In case your local user does not have sufficient access rights:
rem runas /profile /user:administrator "jWebSocketService64.exe /install non-interactive %svc_name%"
set svc_name=
pause
