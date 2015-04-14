#/bin/bash

ROOT=~/moped/moped

HERE=$ROOT/new-server/misc
PLUGINS=$ROOT/plugins
SQUAWK=$ROOT/squawk-zeni
AUTOSAR=$ROOT/autosar

cd $SQUAWK
./startScript.sh

cd $AUTOSAR
./build.sh clean
./build.sh

cd $HERE
./build2.sh LEDLighter 1.0
