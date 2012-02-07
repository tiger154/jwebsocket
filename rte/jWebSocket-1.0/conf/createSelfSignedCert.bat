@echo off
set name=jWebSocket2

echo This script generates a new self signed certificate and a keystore for jWebSocket.
echo The name of the keystore is specified as %name%.
echo Are you sure to overwrite potentially existing files?
pause

set keyFile=%name%.key
set csrFile=%name%.csr
set crtFile=%name%.crt
set pkcsFile=%name%.pkcs12
set ksFile=%name%.ks

rem Generate the private key file
openssl genrsa -out %keyFile% 2048

rem Generate the certificate signing request
openssl req -new -config openssl.cnf -key %keyFile% -out %csrFile%

rem Generate the self signed certificate
openssl x509 -req -days 730 -in %csrFile% -signkey %keyFile% -out %crtFile%

rem create temporary pkcs12 file to be converted into Java KeyStore
openssl pkcs12 -inkey %keyFile% -in %crtFile% -export -out %pkcsFile%

rem Generate the Java KeyStore file
keytool -importkeystore -srckeystore %pkcsFile% -srcstoretype PKCS12 -destkeystore %ksFile%
