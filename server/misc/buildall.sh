#! /bin/bash

CODEBENCH=/home/arndt/moped/arm/Sourcery_CodeBench_Lite_for_ARM_EABI
#CODEBENCH=/home/arndt/moped/arm/gcc-arm-none-eabi-4_9-2015q3
#CODEBENCH=/home/arndt/moped/arm/gcc-arm-none-eabi-4_7-2014q2
#CODEBENCH=/home/arndt/gccarm/gcc-arm-none-eabi-6-2017-q1-update

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
export PATH=$CODEBENCH/bin:$PATH
# from autosar/d.sh jvmenv
export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64/server:$JAVA_HOME/jre/lib/amd64:$LD_LIBRARY_PATH

export CROSS_COMPILE=$CODEBENCH/bin/arm-none-eabi-

set -e

if true; then
cd api
mvn clean install
cd ..

cd ecm-core
mvn clean install
cd ..

cd ecm-linux
mvn clean install
cd ..

cd plugins
mvn clean install
cd ..

cd server
mvn clean install
cd ..

cd simulator
mvn clean install
cd ..

cd squawk
./startScript.sh
cd ..
fi

cd autosar

# CAN card frequency and type of RPi 1 are really independent, but we choose
# to put the new cards (frequency 16 Mhz) on the RPi 1B+ and the old
# cards on the RPi 1B, and then we can associate the type of RPi with the
# frequency.

for ecu in SCU VCU; do

    for arch in v6 v7; do

	sed -i "s/^ARCH=arm_v.*/ARCH=arm_$arch/" src/core/boards/Raspberry_Pi/build_config.mk

	for freq in 16 20; do

	    export BDIR=../examples/Raspberry_Pi/demo_$ecu
	    export CANFREQ=$freq
	    if [ "$freq" = 16 ];
	    then
		RPIBPLUS=1
	    else
		RPIBPLUS=0
	    fi
	    echo "RPIBPLUS = $RPIBPLUS"
	    export RPIBPLUS
	    ./build.sh clean; ./build.sh
	    mkdir -p $ecu
	    cp src/core/binaries/Raspberry_Pi/$ecu-kernel.img $ecu/$ecu-kernel-$freq-$arch.img
	done
    done
done
