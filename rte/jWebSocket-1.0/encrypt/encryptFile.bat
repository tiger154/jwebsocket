@echo off
echo Encrpyting a file using a private key!
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.

rem you can change the name of your private key file here:
set pkeyfile=demo.key

if "%1"=="" goto argErr
if "%2"=="" goto argErr
if not exist %pkeyfile% goto keyErr

goto start

:argErr
echo encryptFile needs to be called with to arguments: encryptFile sourceFile(original) encryptedFile(target)
goto :end

:keyErr
echo encryptFile needs have a private key file called %pkeyfile% in the same folder like the batch
goto :end

:start
rem openssl aes-256-cbc -a -salt -in %1 -out %2 -pass file:%pkeyfile%
openssl rsautl -encrypt -in %1 -out %2 -inkey %pkeyfile%

:end
pause