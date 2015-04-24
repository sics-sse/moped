################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/vm/util/sha.c 

OBJS += \
./vmcore/src/vm/util/sha.o 

C_DEPS += \
./vmcore/src/vm/util/sha.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/vm/util/%.o: ../vmcore/src/vm/util/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


