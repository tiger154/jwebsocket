#! /bin/bash 
clear
echo Decrpyting a file using a private key!
echo (C) Copyright 2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
echo .

#you can change the name of your private key file here:
pkeyfile=private.key

if [! -f $1]; then 
echo decryptFile needs to be called with to arguments: decryptFile sourceFile(encrypted) decryptedFile(decrypted)

elif [! -f $2]; then 
echo decryptFile needs to be called with to arguments: decryptFile sourceFile(encrypted) decryptedFile(decrypted)

elif [! -f $pkeyfile]; then
echo decryptFile needs have a private key file called $pkeyfile in the same folder like the batch

else
openssl smime -decrypt -binary -in $1 -inform DER -out $2 -inkey $pkeyfile
fi