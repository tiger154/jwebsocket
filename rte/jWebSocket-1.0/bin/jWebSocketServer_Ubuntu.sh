#!/bin/sh
if [ ! -d "$JWEBSOCKET_HOME" ]; then
 JWEBSOCKET_HOME=$(cd `dirname $0` && pwd)/..
 export JWEBSOCKET_HOME
fi
echo "(C) Copyright 2010-2013 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath"

java -jar $JWEBSOCKET_HOME/libs/jWebSocketServer-1.0.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
