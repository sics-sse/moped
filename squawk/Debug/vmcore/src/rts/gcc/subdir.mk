################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/gcc/os.c \
../vmcore/src/rts/gcc/os_devices.c 

OBJS += \
./vmcore/src/rts/gcc/os.o \
./vmcore/src/rts/gcc/os_devices.o 

C_DEPS += \
./vmcore/src/rts/gcc/os.d \
./vmcore/src/rts/gcc/os_devices.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/gcc/%.o: ../vmcore/src/rts/gcc/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


