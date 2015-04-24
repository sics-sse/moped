################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../cldc-native-declarations/build/com/sun/cldc/jna/JNAPlatformImpl.c 

OBJS += \
./cldc-native-declarations/build/com/sun/cldc/jna/JNAPlatformImpl.o 

C_DEPS += \
./cldc-native-declarations/build/com/sun/cldc/jna/JNAPlatformImpl.d 


# Each subdirectory must supply rules for building sources it contributes
cldc-native-declarations/build/com/sun/cldc/jna/%.o: ../cldc-native-declarations/build/com/sun/cldc/jna/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


