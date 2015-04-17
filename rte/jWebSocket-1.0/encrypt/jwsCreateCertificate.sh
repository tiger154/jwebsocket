#! /bin/bash 
clear
echo Encrpyting a file using a private key!
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo .

pkeyfile=private.key
openssl req -new -key $pkeyfile -out info.csr
openssl x509 -req -days 7300 -in info.csr -signkey $pkeyfile -out private.crt  