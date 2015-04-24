#!/bin/bash
./d.sh clean

COMP=-comp:gcc-rpi
COMP=

cd builder
./bld.sh
cd ..
./d.sh $COMP copyphoneme

./d.sh $COMP
./d.sh $COMP -prod -mac -verbose rom -metadata cldc imp debugger

#./createSquawkArray.sh
#./d.sh $COMP
#./d.sh $COMP -prod -mac -verbose rom -metadata cldc imp debugger

#./d.sh -prod -mac -o2 rom cldc imp translator >> output
#./d.sh user-clean tests/HelloworldMain >> output
#./d.sh user-suite tests/HelloWorldMain >> output
