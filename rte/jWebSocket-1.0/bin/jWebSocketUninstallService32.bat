@echo off
echo jWebSocket Windows Service Uninstaller (32bit)
echo (C) Copyright 2010-2013 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.
echo Usage jWebSocketUninstallService32.bat [service name]   (default: 'jWebSocket Service')
echo.
set svc_name=jWebSocket Service
if not "%~1"=="" set svc_name=%~1
echo Uninstalling jWebSocket Server (32bit) as Windows service (service name '%svc_name%')...
jWebSocketService32.exe /uninstall %svc_name%
set svc_name=
pause
