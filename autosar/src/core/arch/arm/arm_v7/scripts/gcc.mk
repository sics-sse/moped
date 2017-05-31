# prefered version
CC_VERSION=4.4.5
# ARMv7
cflags-y 	+= -O2  -mcpu=cortex-a7 -mfpu=neon-vfpv4
cflags-y 	+= -ggdb

cflags-y += -ffunction-sections

lib-y   	+= -lgcc -lc
ASFLAGS 	+= -mcpu=cortex-a7

