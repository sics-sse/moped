#!/bin/bash

set -e

./d.sh clean

cd builder
./bld.sh
cd ..

./d.sh -comp:gcc-rpi-autosar copyphoneme
./d.sh -comp:gcc-rpi-autosar # -verbose
./d.sh -comp:gcc-rpi-autosar -prod -mac -o2 -verbose rom -metadata cldc #imp debugger
./createSquawkArray.sh
./d.sh -comp:gcc-rpi-autosar
./d.sh -comp:gcc-rpi-autosar -prod -mac -o2 -verbose rom -metadata cldc #imp debugger

#./d.sh -prod -mac -o2 rom cldc imp translator >> output
#./d.sh user-clean tests/Helloworld >> output
#./d.sh user-suite tests/HelloWorld >> output
#./d.sh user-clean tests/HelloworldMain >> output
#./d.sh user-suite tests/HelloWorldMain >> output
