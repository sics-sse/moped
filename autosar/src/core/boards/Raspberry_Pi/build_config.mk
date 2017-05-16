# ARCH defines
#ARCH = arm_v7
ARCH = arm_v6
ARCH_FAM = arm
# rpi 1
ARCH_MCU = bcm2835
# rpi 2
#ARCH_MCU = bcm2836
# rpi 3
#ARCH_MCU = bcm2837

#
# CFG (y/n) macros
# ARM_V6, ARM_V7 ARM_V8
CFG = ARM ARM_RPI ARM_V6
# Add our board
CFG += Raspberry_Pi

# What buildable modules does this board have,
# default or private

# MCAL
MOD_AVAIL+=DIO MCU PORT SPI PWM DMA KERNEL
# System + Communication + Diagnostic
MOD_AVAIL+=CANIF CANTP J1939TP COM DCM DEM DET ECUM IOHWAB KERNEL PDUR WDGM RTE
# Network management
MOD_AVAIL+=COMM NM CANNM CANSM
# Additional
MOD_AVAIL+=RAMLOG TCF LWIP SLEEP SOAD USB ETH I2C CAN
# CRC
MOD_AVAIL+=CRC32 CRC16
# Required modules
MOD_USE += MCU KERNEL ECUM

# Default cross compiler
DEFAULT_CROSS_COMPILE = /opt/arm-none-eabi/bin/arm-none-eabi-
