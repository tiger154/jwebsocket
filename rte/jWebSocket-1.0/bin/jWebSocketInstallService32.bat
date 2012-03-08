@echo off
echo jWebSocket Windows Service Installer (32bit)
echo (C) Copyright 2012 Innotrade GmbH - jWebSocket.org
echo.
echo Usage jWebSocketInstallService32.bat [service name]   (default: 'jWebSocket Service')
echo.
set svc_name=jWebSocket Service
if not "%~1"=="" set svc_name=%~1
echo Installing jWebSocket Server (32bit) as Windows service (service name '%svc_name%')...
jWebSocketService32.exe /install non-interactive %svc_name%
set svc_name=
pause
