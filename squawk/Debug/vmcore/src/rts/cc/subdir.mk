################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/cc/os.c \
../vmcore/src/rts/cc/os_devices.c 

OBJS += \
./vmcore/src/rts/cc/os.o \
./vmcore/src/rts/cc/os_devices.o 

C_DEPS += \
./vmcore/src/rts/cc/os.d \
./vmcore/src/rts/cc/os_devices.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/cc/%.o: ../vmcore/src/rts/cc/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


