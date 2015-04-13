@echo off
echo Creating a private certificate for encryption.
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
rem Authors: Domma Moreno Dager, Alexander Schulze
echo.

set pkeyfile=demo.key
set csrfile=demo.csr
set crtfile=demo.crt

openssl req -new -key %pkeyfile% -out %csrfile%
openssl x509 -req -days 7300 -in %csrfile% -signkey %pkeyfile% -out %crtfile%

pause