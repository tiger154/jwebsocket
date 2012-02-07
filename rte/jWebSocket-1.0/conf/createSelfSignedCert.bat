set keyFile=jWebSocket.key
set csrFile=jWebSocket.csr
set crtFile=jWebSocket.crt
set pkcsFile=jWebSocket.pkcs12
set ksFile=jWebSocket.ks

rem openssl genrsa -out %keyFile% 2048
rem openssl req -new -config openssl.cnf -key %keyFile% -out %csrFile%
rem openssl x509 -req -days 730 -in %csrFile% -signkey %keyFile% -out %crtFile%

rem create temporary pkcs12 file to be converted into Java KeyStore
rem openssl pkcs12 -inkey %keyFile% -in %crtFile% -export -out %pkcsFile%

rem Generate the Java KeyStore file
keytool -importkeystore -srckeystore %pkcsFile% -srcstoretype PKCS12 -destkeystore %ksFile%
