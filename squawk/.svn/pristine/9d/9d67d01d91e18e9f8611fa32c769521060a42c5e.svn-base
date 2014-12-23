#!/bin/bash
./d.sh clean

cd builder
./bld.sh
cd ..
./d.sh -comp:gcc-rpi copyphoneme

./d.sh -comp:gcc-rpi
./d.sh -comp:gcc-rpi -prod -mac -verbose rom -metadata cldc imp debugger
./createSquawkArray.sh
./d.sh -comp:gcc-rpi
./d.sh -comp:gcc-rpi -prod -mac -verbose rom -metadata cldc imp debugger

#./d.sh -prod -mac -o2 rom cldc imp translator >> output
#./d.sh user-clean tests/HelloworldMain >> output
#./d.sh user-suite tests/HelloWorldMain >> output
