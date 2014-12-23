#!/bin/sh
#
 
if [ $1 = SOCKET ]; then
    echo "Can't run smoke test on SOCKET platform"
    exit 0
fi
 
if [ $1 = BARE_METAL ]; then
    echo "Can't run smoke test on BARE_METAL platform"
    exit 0
fi
 
./squawk com.sun.squawk.Test
STAT=$?
# status got trunctated from 12345 to 57
if [ $STAT -eq 57 ]; then
	echo good  $STAT
else
	echo bad $STAT
	exit $STAT
fi

java -jar build.jar user-clean tests/HelloWorld
java -jar build.jar  user-suite tests/HelloWorld
./squawk -suite:tests/HelloWorld/HelloWorld

java -jar build.jar  user-clean tests/HelloWorldMain
java -jar build.jar  user-suite tests/HelloWorldMain
./squawk -suite:tests/HelloWorldMain/HelloWorldMain tests.HelloWorldMain

