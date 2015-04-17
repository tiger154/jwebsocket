#! /bin/bash 
clear
echo Encrpyting a file using a private key!
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo .

#you can change the name of your private key file here:
pkeyfile=private.crt

if [! -f $1]; then 
echo encryptFile needs to be called with to arguments: encryptFile sourceFile(original) encryptedFile(target)

elif [! -f $2]; then 
echo encryptFile needs to be called with to arguments: encryptFile sourceFile(original) encryptedFile(target)

elif [! -f $pkeyfile]; then
echo encryptFile needs have a private key file called $pkeyfile in the same folder like the batch

else
openssl smime -encrypt -binary -aes-256-cbc -in $1 -out $2 -outform DER $pkeyfile
fi
