#!/bin/sh

export BOARDDIR=Raspberry_Pi
export BDIR=../examples/Raspberry_Pi/demo_VCU
export BUILDDIR=.
export CROSS_COMPILE=/opt/CodeSourcery/Sourcery_CodeBench_Lite_for_ARM_EABI/bin/arm-none-eabi-

cd src/core
if [ "$1" = "clean" ]; then
	make clean
else
	make all
fi





