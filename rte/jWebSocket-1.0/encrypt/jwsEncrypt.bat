@echo off
echo Encrpyting a file using a private certificate.
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
rem  Authors: Domma Moreno Dager, Alexander Schulze
echo.

rem you can change the name of your private key file here:
set pcrtfile=demo.crt

if "%1"=="" goto argErr
if "%2"=="" goto argErr
if not exist %pcrtfile% goto keyErr

goto start

:argErr
echo encryptFile needs to be called with to arguments: jwsEncrypt.bat sourceFile(original) encryptedFile(target)
goto :end

:keyErr
echo jwsEncrypt.bat needs have a private key file called %pcrtfile% in the same folder like the batch
goto :end

:start
openssl smime -encrypt -binary -aes-256-cbc -in %1 -out %2 -outform DER %pcrtfile%

:end

pause