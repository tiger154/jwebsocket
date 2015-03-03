@echo off

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue

:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit

:continue
echo This will clean-up the known local work files (e.g. file uploads, stored mails, generated reports, caches etc.). 
echo.
echo Are you sure?
echo.
pause

set jwsroot="C:\svn\jWebSocketDev\"
echo removing temporary cached reports...
del %jwsroot%branches\jWebSocket-1.0\jWebSocketClient\web\public\reports\*.pdf

echo removing temporary canvas demo .png's...
del %jwsroot%branches\jWebSocket-1.0\jWebSocketClient\web\public\canvas_demo_?.png

echo removing uploaded files from file system folder...
rd /S %jwsroot%rte\jWebSocket-1.0\filesystem\private\
rd /S %jwsroot%rte\jWebSocket-1.0\filesystem\public\
md %jwsroot%rte\jWebSocket-1.0\filesystem\private
md %jwsroot%rte\jWebSocket-1.0\filesystem\public

echo removing cached mails and attachments...
rd /S %jwsroot%rte\jWebSocket-1.0\mails\
md %jwsroot%rte\jWebSocket-1.0\mails

echo removing obsolete ApacheMQ data...
del %jwsroot%branches\jWebSocket-1.0\jWebSocketServer\activemq-data\localhost\KahaDB\db*.*
del %jwsroot%branches\jWebSocket-1.0\jWebSocketServer\activemq-data\localhost\KahaDB\lock

echo.
echo Clean-up done!
echo.

pause