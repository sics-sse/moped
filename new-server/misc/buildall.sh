#! /bin/bash

CODEBENCH=/home/arndt/moped/arm/Sourcery_CodeBench_Lite_for_ARM_EABI
#CODEBENCH=/home/arndt/moped/arm/gcc-arm-none-eabi-4_9-2015q3
#CODEBENCH=/home/arndt/moped/arm/gcc-arm-none-eabi-4_7-2014q2

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

cd new-server
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

for ecu in VCU SCU; do

    for freq in 16 20; do

	export BDIR=../examples/Raspberry_Pi/demo_$ecu
	export CANFREQ=$freq
	./build.sh clean; ./build.sh
	mkdir -p $ecu
	cp src/core/binaries/Raspberry_Pi/$ecu-kernel.img $ecu/$ecu-kernel-$freq.img

    done
done
