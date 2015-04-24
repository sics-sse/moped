################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../phoneme/midp/src/rms/rms_api/reference/native/recordStoreFile.c 

OBJS += \
./phoneme/midp/src/rms/rms_api/reference/native/recordStoreFile.o 

C_DEPS += \
./phoneme/midp/src/rms/rms_api/reference/native/recordStoreFile.d 


# Each subdirectory must supply rules for building sources it contributes
phoneme/midp/src/rms/rms_api/reference/native/%.o: ../phoneme/midp/src/rms/rms_api/reference/native/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


