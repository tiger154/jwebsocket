@echo off
echo Decrpyting a file using a private key.
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
rem  Authors: Domma Moreno Dager, Alexander Schulze
echo.

rem you can change the name of your private key file here:
set pkeyfile=demo.key

if "%1"=="" goto argErr
if "%2"=="" goto argErr
if not exist %pkeyfile% goto keyErr

goto start

:argErr
echo jwsDecrypt needs to be called with to arguments: jwsDecrypt sourceFile(encrypted) decryptedFile(decrypted)
goto :end

:keyErr
echo decryptFile needs have a private key file called %pkeyfile% in the same folder like the batch
goto :end

:start
openssl smime -decrypt -binary -in %1 -inform DER -out %2 -inkey %pkeyfile%

:end
pause