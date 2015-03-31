@echo off
echo Creating a new 4096 bit private key file in the file private.key!
echo CAUTION! An existing private.key file will be overwritten.
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo.

pause

openssl genrsa -out demo.key 4096

pause
