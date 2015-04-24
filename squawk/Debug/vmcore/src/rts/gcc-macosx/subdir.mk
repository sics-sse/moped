################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/gcc-macosx/os.c \
../vmcore/src/rts/gcc-macosx/os_devices.c \
../vmcore/src/rts/gcc-macosx/os_main.c 

OBJS += \
./vmcore/src/rts/gcc-macosx/os.o \
./vmcore/src/rts/gcc-macosx/os_devices.o \
./vmcore/src/rts/gcc-macosx/os_main.o 

C_DEPS += \
./vmcore/src/rts/gcc-macosx/os.d \
./vmcore/src/rts/gcc-macosx/os_devices.d \
./vmcore/src/rts/gcc-macosx/os_main.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/gcc-macosx/%.o: ../vmcore/src/rts/gcc-macosx/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


