This document explains how to set up an Autosar OS on a Raspberry PI.

First, copy the contents of ../boot/-folder to an SD card. Now, necessary bootloaders are in place. This only needs
to be done once for each new SD-card.

Next, set up cross-compilation tools for ARM GNU-EABI on your working station. The goal is to compile the code into an
image file, which is the final missing link to get the Autosar OS up and running on a Raspberry Pi.

On a Linux system:
Edit the CROSS_COMPILE environment variable in build.sh to the prefix of your cross-compilation tools. For example, 
if your GNU cross compiler is named arm-none-eabi-gcc and located in 
/opt/CodeSourcery/Sourcery_CodeBench_Lite_for_ARM_EABI/bin/, you should write:
  export CROSS_COMPILE=/opt/CodeSourcery/Sourcery_CodeBench_Lite_for_ARM_EABI/bin/arm-none-eabi-
in build.sh.

Now, run "build.sh" (or "build.sh clean" if you want to clean the binaries before compilation). If everything works, 
there should be an image file (kernel.img) created in the core/binaries/Raspberry_Pi/-folder. Copy it to the SD-card 
and have fun!


