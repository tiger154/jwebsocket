@echo off
echo jWebSocket Windows Service Uninstaller (64bit)
echo (C) Copyright 2012 Innotrade GmbH - jWebSocket.org
echo.
echo Usage jWebSocketUninstallService64.bat [service name]   (default: 'jWebSocket Service')
echo.
set svc_name=jWebSocket Service
if not "%~1"=="" set svc_name=%~1
echo Uninstalling jWebSocket Server (64bit) as Windows service (service name '%svc_name%')...
jWebSocketService64.exe /uninstall %svc_name%
set svc_name=
pause
