################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/cortex-m3/src/MbedTest/core_cm3.c \
../vmcore/src/rts/cortex-m3/src/MbedTest/os_main.c \
../vmcore/src/rts/cortex-m3/src/MbedTest/system_LPC17xx.c 

OBJS += \
./vmcore/src/rts/cortex-m3/src/MbedTest/core_cm3.o \
./vmcore/src/rts/cortex-m3/src/MbedTest/os_main.o \
./vmcore/src/rts/cortex-m3/src/MbedTest/system_LPC17xx.o 

C_DEPS += \
./vmcore/src/rts/cortex-m3/src/MbedTest/core_cm3.d \
./vmcore/src/rts/cortex-m3/src/MbedTest/os_main.d \
./vmcore/src/rts/cortex-m3/src/MbedTest/system_LPC17xx.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/cortex-m3/src/MbedTest/%.o: ../vmcore/src/rts/cortex-m3/src/MbedTest/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


