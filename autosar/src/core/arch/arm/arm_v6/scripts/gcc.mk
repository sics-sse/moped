# prefered version
CC_VERSION=4.4.5
# ARMv6, little endian
cflags-y += -O2  -mcpu=arm1176jz-s -mfpu=vfp
cflags-y += -ggdb

cflags-y += -ffunction-sections

lib-y   += -lgcc -lc
ASFLAGS += -mcpu=arm1176jz-s -mfpu=vfp

