################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/gcc-rpi/os.c \
../vmcore/src/rts/gcc-rpi/os_devices.c \
../vmcore/src/rts/gcc-rpi/rpi-io.c \
../vmcore/src/rts/gcc-rpi/squawkSuiteArray.c \
../vmcore/src/rts/gcc-rpi/squawk_memory.c 

OBJS += \
./vmcore/src/rts/gcc-rpi/os.o \
./vmcore/src/rts/gcc-rpi/os_devices.o \
./vmcore/src/rts/gcc-rpi/rpi-io.o \
./vmcore/src/rts/gcc-rpi/squawkSuiteArray.o \
./vmcore/src/rts/gcc-rpi/squawk_memory.o 

C_DEPS += \
./vmcore/src/rts/gcc-rpi/os.d \
./vmcore/src/rts/gcc-rpi/os_devices.d \
./vmcore/src/rts/gcc-rpi/rpi-io.d \
./vmcore/src/rts/gcc-rpi/squawkSuiteArray.d \
./vmcore/src/rts/gcc-rpi/squawk_memory.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/gcc-rpi/%.o: ../vmcore/src/rts/gcc-rpi/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


