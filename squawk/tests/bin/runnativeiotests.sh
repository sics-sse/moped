#!/bin/sh -ex
#

./squawk com.sun.squawk.io.j2me.http.file file://build.properties
./squawk com.sun.squawk.platform.posix.callouts.NetDB
./squawk -Xtgc:1 com.sun.squawk.io.j2me.http.Protocol http://www.sun.com/index.html

./d user-clean tests/TestBlocking
./d user-suite tests/TestBlocking
./squawk -suite:tests/TestBlocking/TestBlocking
