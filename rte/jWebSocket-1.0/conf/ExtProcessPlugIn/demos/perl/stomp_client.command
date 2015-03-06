#/bin/sh
if [ "$PERL_HOME" == "" ]; then
  export PERL_HOME=/usr/local/ActivePerl-5.16
fi

export PERL_HOME=/usr/local/ActivePerl-5.16
export PERL_SCRIPTS_HOME=/svn/jWebSocket.dev/rte/jWebSocket-1.0/conf/ExtProcessPlugIn/demos/perl

echo "(C) Copyright 2010-2015 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath"
$PERL_HOME/bin/perl $PERL_SCRIPTS_HOME/stomp_client.pl
