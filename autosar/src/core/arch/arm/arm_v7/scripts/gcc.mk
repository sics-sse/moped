# prefered version
CC_VERSION=6.2.1
# ARMv7, little endian
cflags-y += -O2  -mcpu=cortex-a7 -mfpu=neon
cflags-y += -ggdb

cflags-y += -ffunction-sections

lib-y   += -lgcc -lc
ASFLAGS += -mcpu=cortex-a7 -mfpu=neon
