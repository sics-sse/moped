################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/gcc-rpi-autosar/autosar-io.c \
../vmcore/src/rts/gcc-rpi-autosar/os.c \
../vmcore/src/rts/gcc-rpi-autosar/os_devices.c \
../vmcore/src/rts/gcc-rpi-autosar/simple_stdio.c \
../vmcore/src/rts/gcc-rpi-autosar/squawkSuiteArray.c \
../vmcore/src/rts/gcc-rpi-autosar/squawk_memory.c 

OBJS += \
./vmcore/src/rts/gcc-rpi-autosar/autosar-io.o \
./vmcore/src/rts/gcc-rpi-autosar/os.o \
./vmcore/src/rts/gcc-rpi-autosar/os_devices.o \
./vmcore/src/rts/gcc-rpi-autosar/simple_stdio.o \
./vmcore/src/rts/gcc-rpi-autosar/squawkSuiteArray.o \
./vmcore/src/rts/gcc-rpi-autosar/squawk_memory.o 

C_DEPS += \
./vmcore/src/rts/gcc-rpi-autosar/autosar-io.d \
./vmcore/src/rts/gcc-rpi-autosar/os.d \
./vmcore/src/rts/gcc-rpi-autosar/os_devices.d \
./vmcore/src/rts/gcc-rpi-autosar/simple_stdio.d \
./vmcore/src/rts/gcc-rpi-autosar/squawkSuiteArray.d \
./vmcore/src/rts/gcc-rpi-autosar/squawk_memory.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/gcc-rpi-autosar/%.o: ../vmcore/src/rts/gcc-rpi-autosar/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


