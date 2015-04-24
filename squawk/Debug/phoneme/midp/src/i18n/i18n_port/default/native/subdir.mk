################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../phoneme/midp/src/i18n/i18n_port/default/native/defaultGenConv.c \
../phoneme/midp/src/i18n/i18n_port/default/native/defaultLocale.c 

OBJS += \
./phoneme/midp/src/i18n/i18n_port/default/native/defaultGenConv.o \
./phoneme/midp/src/i18n/i18n_port/default/native/defaultLocale.o 

C_DEPS += \
./phoneme/midp/src/i18n/i18n_port/default/native/defaultGenConv.d \
./phoneme/midp/src/i18n/i18n_port/default/native/defaultLocale.d 


# Each subdirectory must supply rules for building sources it contributes
phoneme/midp/src/i18n/i18n_port/default/native/%.o: ../phoneme/midp/src/i18n/i18n_port/default/native/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


