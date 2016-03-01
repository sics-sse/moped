
Arctic Core - the open source AUTOSAR embedded platform

This software is meant as a starting kit for setting up an experimental platform for research and development
on cyber-physical systems. 

Arctic Core OS has been ported to the Raspberry Pi platform, being the only hardware supported by this code release.
The code also include drivers for sensors and actuators that are needed to drive an RC car, using a couple of Raspberry
Pis as control units. 

For information on Autosar porting to Raspberry Pi, see 
  https://moped.sics.se/wordpress/wp-content/uploads/2014/08/ewili13.pdf (short version, paper)
  http://www.idt.mdh.se/utbildning/exjobb/files/TR1442.pdf (long version, MSc-report)
  
For a quick glance at the additional (RC-car related) hardware setup (incl. sensors and actuators), 
supported by this release, see
  https://moped.sics.se/wordpress/wp-content/uploads/2014/08/MOPED.pdf

For information on getting started with Arctic Core, please take a look at: 
  http://www.arccore.com


INSTALLATION INSTRUCTIONS:

The following steps explain how to set up an Autosar OS on a Raspberry PI.

First, copy the contents of ../boot/-folder to an SD card. Now, necessary bootloaders are in place. This only needs
to be done once for each new SD-card.

Next, set up cross-compilation tools for ARM GNU-EABI on your working station, for example Sourcery CodeBench,
http://www.mentor.com/embedded-software/sourcery-tools/sourcery-codebench/evaluations/ (however, it is no longer 
free for ARM hardware). The goal is to compile the code into an image file, which is the final missing link to get 
the Autosar OS up and running on a Raspberry Pi.

On a Linux system:
Edit the CROSS_COMPILE environment variable in build.sh to the prefix of your cross-compilation tools. For example, 
if your GNU cross compiler is named arm-none-eabi-gcc and located in 
/opt/CodeSourcery/Sourcery_CodeBench_Lite_for_ARM_EABI/bin/, you should write:
  export CROSS_COMPILE=/opt/CodeSourcery/Sourcery_CodeBench_Lite_for_ARM_EABI/bin/arm-none-eabi-
in build.sh.

Now, run "build.sh" (or "build.sh clean" if you want to clean the binaries before compilation). If everything works, there should be an image file (kernel.img) created in the core/binaries/Raspberry_Pi/-folder. Copy it to the SD-card and have fun!

If you plan to actively develop the Autosar code and re-compile the code rather often, it is more convenient to use the bootloader kernel image for baremetal systems (found in ../tools) to avoid playing around with the SD-card each time the code is compiled. In that case, first copy ../tools/baremetal_bootloader_kernel.img to the SD-card, renaming it to kernel.img. Next, modify the makefile in your example folder (e.g. src/examples/Raspberry_Pi/demo_VCU/makefile) to output kernel.bin. In other words, replace:
  build-bin-y = kernel.img
with 
  build-bin-y = kernel.bin
Now, you can upload kernel.bin through an xmodem terminal.

On a Windows system:
If you are using ArcticStudio-tools, they include a cross-compiler for ARM. Otherwise, it might be a bit tricky. 
The best solution is probably to go for a Linux Virtual Machine, for example https://www.virtualbox.org/, 
and follow the above steps. 


