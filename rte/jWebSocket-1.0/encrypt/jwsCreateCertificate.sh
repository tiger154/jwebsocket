#! /bin/bash
clear
echo "Creating a private certificate for encryption."
echo "(C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath"
# Authors: Domma Moreno Dager, Alexander Schulze
echo "."
pkeyfile=demo.key
csrfile=demo.csr
crtfile=demo.crt
openssl req -new -key $pkeyfile -out $csrfile
openssl x509 -req -days 7300 -in $csrfile -signkey $pkeyfile -out $crtfile
