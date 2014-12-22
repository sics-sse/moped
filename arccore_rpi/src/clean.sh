#!/bin/sh

export BOARDDIR=Raspberry_Pi
export BDIR=../examples/Raspberry_Pi/demo_VCU
export CROSS_COMPILE=/opt/CodeSourcery/Sourcery_CodeBench_Lite_for_ARM_EABI/bin/arm-none-eabi-
export BUILDDIR=/home/avenir/Documents/dev/eclipse_ws/autosar/src/core
#export BUILDDIR=/home/avenir/Documents/dev/fresta/autosar_v4/src/doip-1.4.3
#export KVMDIR=/home/avenir/Documents/dev/fresta/j2me_cldc/build/autosar

cd core
make clean

#if [ "$1" == "clean" ]; then
	#make clean
	#	make clean_all #KVM=true 
	#else
#	make clean
	#KVM=true 
#fi




