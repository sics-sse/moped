#! /bin/bash

CODEBENCH=/home/arndt/moped/arm/Sourcery_CodeBench_Lite_for_ARM_EABI

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
export PATH=$CODEBENCH/bin:$PATH
# from autosar/d.sh jvmenv
export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64/server:$JAVA_HOME/jre/lib/amd64:$LD_LIBRARY_PATH

export CROSS_COMPILE=$CODEBENCH/bin/arm-none-eabi-

set -e

#cd squawk
#./startScript.sh
#cd ..


cd autosar

# CAN card frequency and type of RPi 1 are really independent, but we choose
# to put the new cards (frequency 16 Mhz) on the RPi 1B+ and the old
# cards on the RPi 1B, and then we can associate the type of RPi with the
# frequency.

for ecu in SCU VCU; do

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
	cp src/core/binaries/Raspberry_Pi/$ecu-kernel.img $ecu/$ecu-kernel-$freq.img
    done
done
