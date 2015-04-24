#!/bin/bash

# Compile plugin
./d.sh user-clean tests/ClassLoaderInput/
./d.sh user-suite tests/ClassLoaderInput/

# deliver plugin
scp tests/ClassLoaderInput/ClassLoaderInput.suite pi@193.10.66.159:/home/pi/server_simulator/
scp ../ecm-linux/target/ecm-linux-0.0.1-SNAPSHOT.jar pi@193.10.66.159:/home/pi/server_simulator/
