################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/msc/os.c \
../vmcore/src/rts/msc/os_devices.c 

OBJS += \
./vmcore/src/rts/msc/os.o \
./vmcore/src/rts/msc/os_devices.o 

C_DEPS += \
./vmcore/src/rts/msc/os.d \
./vmcore/src/rts/msc/os_devices.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/msc/%.o: ../vmcore/src/rts/msc/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


