@echo off
echo -------------------------------------------------------------------------
echo jWebSocket JavaScript Automated Test Runner
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

if "%1"=="/y" goto dontAsk1
echo This will run the semi-automated installation tests for jWebSocket v%JWEBSOCKET_VER%. Are you sure?
pause
echo ----------------------------------------------------------------------------
echo Please check if the following instances are running properly:
echo   1) Apache httpd server
echo   2) Apache AMQ server
echo   3) Mongo DB server
echo ----------------------------------------------------------------------------
pause
:dontAsk1

set ver=%JWEBSOCKET_VER%
set testroot=c:\jWebSocket-%ver%-test\
set dlroot=c:\svn\jWebSocketDev\downloads\
set logroot=%testroot%logs\
rem set dlroot=%testroot%downloads\
set logfile=%logroot%install_tests.log

rem save script root
set scriptroot=%CD%\

cd \
echo Checking test folder %testroot% to be removed...
if exist %logroot% rd /s /q %logroot%
if exist %testroot% rd /s /q %testroot%

md %testroot%
md %logroot%
echo Starting install test suite... > %logfile%

if "%CHROME_APP%"=="" ( set CHROME_APP="C:\Users\aschulze\AppData\Local\Google\Chrome\Application\chrome.exe" ) else ( set CHROME_APP="%CHROME_APP%" )
if "%FIREFOX_APP%"=="" ( set FIREFOX_APP="C:\Program Files (x86)\Mozilla Firefox 4\firefox.exe" ) else ( set FIREFOX_APP="%FIREFOX_APP%" )
if "%OPERA_APP%"=="" ( set OPERA_APP="C:\Program Files (x86)\Opera 11.00 beta\opera.exe" ) else ( set OPERA_APP="%OPERA_APP%" )
if "%SAFARI_APP%"=="" ( set SAFARI_APP="C:\Program Files (x86)\Safari\safari.exe" ) else ( set SAFARI_APP="%SAFARI_APP%" )
if "%IEXPLORER_APP%"=="" ( set IEXPLORER_APP="C:\Program Files\Internet Explorer\iexplore.exe" ) else ( set IEXPLORER_APP="%IEXPLORER_APP%" )

echo Chrome: %CHROME_APP%
echo Firefox: %FIREFOX_APP%
echo Opera: %OPERA_APP%
echo Safari: %SAFARI_APP%
echo IExplorer: %IEXPLORER_APP%

pause

set TEST_URL=http://localhost/jwcDev/test/runTests.htm

rem -------------------------------------------------
rem UNPACK SERVER FILES
rem -------------------------------------------------
echo Unpacking server files ...
cd /d %dlroot%jWebSocket-%ver%
7z x jWebSocketServer-%ver%.zip -o"%testroot%" >> %logfile%
7z x jWebSocketServer32-%ver%.zip -o"%testroot%"\jWebSocket-%ver%\bin >> %logfile%
7z x jWebSocketServer64-%ver%.zip -o"%testroot%"\jWebSocket-%ver%\bin >> %logfile%
7z x jWebSocketService32-%ver%.zip -o"%testroot%"\jWebSocket-%ver%\bin >> %logfile%
7z x jWebSocketService64-%ver%.zip -o"%testroot%"\jWebSocket-%ver%\bin >> %logfile%
7z x jWebSocketAMQStockTickerService32-%ver%.zip -o"%testroot%"\jWebSocket-%ver%\bin >> %logfile%
7z x jWebSocketAMQStockTickerService64-%ver%.zip -o"%testroot%"\jWebSocket-%ver%\bin >> %logfile%

set JWEBSOCKET_HOME=%testroot%jWebSocket-%ver%\

echo ----------------------------------------------------------------------------
echo Please check if all Server files have been extracted properly to
echo JWEBSOCKET_HOME: %JWEBSOCKET_HOME%
echo and also check %logfile%
echo ----------------------------------------------------------------------------
pause

rem -------------------------------------------------
rem START ADMIN GUI
rem -------------------------------------------------
echo Starting jWebSocket Admin GUI via batch ...
cd /d "%testroot%jWebSocket-%ver%\bin"
start "Admin GUI" cmd.exe /c call jWebSocketAdmin.bat
rem goto service

:server_jar
rem -------------------------------------------------
rem RUN SERVER.JAR TEST
rem -------------------------------------------------
echo Starting jWebSocket Server via .jar...
cd /d "%testroot%jWebSocket-%ver%"

echo ----------------------------------------------------------------------------
echo WAIT FOR SERVER START UP TO START TEST SUITE....
echo ----------------------------------------------------------------------------

start "Running jWebSocket Server via java -jar ..." java -jar libs/jWebSocketServer-%ver%.jar

pause

cmd.exe /c %scriptroot%C_runSuite.bat Chrome %CHROME_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Firefox %FIREFOX_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Safari %SAFARI_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Opera %OPERA_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat IExplorer %IEXPLORER_APP% %TEST_URL%
echo ----------------------------------------------------------------------------
echo Please shutdown the server via Admin UI now to continue the tests!
echo ----------------------------------------------------------------------------
pause
goto cleanup

:server_bat
rem -------------------------------------------------
rem RUN SERVER.BAT TEST
rem -------------------------------------------------
echo Starting jWebSocket Server via batch ...
cd /d "%testroot%jWebSocket-%ver%\bin"
start "Running jWebSocket Server via jWebSocketServer.bat ..." cmd.exe /c call jWebSocketServer.bat
cmd.exe /c %scriptroot%C_runSuite.bat Chrome %CHROME_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Firefox %FIREFOX_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Safari %SAFARI_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Opera %OPERA_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat IExplorer %IEXPLORER_APP% %TEST_URL%
echo ----------------------------------------------------------------------------
echo Please shutdown the server via Admin UI now 
echo and close the Admin UI to continue the tests!
echo ----------------------------------------------------------------------------
pause
rem goto cleanup

:service
rem -------------------------------------------------
rem RUN JWEBSOCKET SERVICE TEST
rem -------------------------------------------------
echo Installing and starting jWebSocket Service...
cd /d "%testroot%jWebSocket-%ver%\bin"
cmd.exe /c jWebSocketInstallService64.bat
jWebSocketService64.exe /start
cmd.exe /c %scriptroot%C_runSuite.bat Chrome %CHROME_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Firefox %FIREFOX_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Safari %SAFARI_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat Opera %OPERA_APP% %TEST_URL%
cmd.exe /c %scriptroot%C_runSuite.bat IExplorer %IEXPLORER_APP% %TEST_URL%
echo ----------------------------------------------------------------------------
echo Please shutdown the server via Admin UI now to continue the tests!
echo ----------------------------------------------------------------------------
pause
echo Stopping and un-installing jWebSocket Service...
jWebSocketService64.exe /stop
cmd.exe /c jWebSocketUninstallService64.bat
rem goto cleanup

:cleanup
rem -------------------------------------------------
rem FINAL CLEANUP
rem -------------------------------------------------
echo ----------------------------------------------------------------------------
echo Please close the Admin UI now to remove the temporary test folders!
echo ----------------------------------------------------------------------------
pause

cd \
echo Removing test folder %testroot% ...
rd /s /q %logroot%
rd /s /q %testroot%

echo jWebSocket semi automated tests done!
pause
