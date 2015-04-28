#!/bin/sh

export BOARDDIR=Raspberry_Pi

if [ -z "$BDIR" ]; then
    export BDIR=../examples/Raspberry_Pi/demo_VCU
fi

export BUILDDIR=.
export SQUAWK=true

if [ -z "$CROSS_COMPILE" ]; then
export CROSS_COMPILE=/opt/CodeSourcery/Sourcery_CodeBench_Lite_for_ARM_EABI/bin/arm-none-eabi-
fi

cd src/core
if [ "$1" = "clean" ]; then
	make clean
else
	make all
fi
