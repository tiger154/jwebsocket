#/bin/sh
if [ "$JWEBSOCKET_HOME" == "" ]; then
  pushd ..
  JWEBSOCKET_HOME=`pwd`;
  export JWEBSOCKET_HOME popd
fi
echo "(C) Copyright 2010-2013 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath"

java -jar $JWEBSOCKET_HOME/libs/jWebSocketSwingGUI-1.0.jar $1 $2 $3 $4 $5 $6 $7 $8 $9
exit
