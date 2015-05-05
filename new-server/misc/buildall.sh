#! /bin/bash

CODEBENCH=/home/arndt/moped/arm/Sourcery_CodeBench_Lite_for_ARM_EABI

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
./build.sh clean; BDIR=../examples/Raspberry_Pi/demo_VCU ./build.sh
mkdir -p VCU
cp src/core/binaries/Raspberry_Pi/VCU-kernel.img VCU

./build.sh clean; BDIR=../examples/Raspberry_Pi/demo_SCU ./build.sh
mkdir -p SCU
cp src/core/binaries/Raspberry_Pi/SCU-kernel.img SCU
cd ..
