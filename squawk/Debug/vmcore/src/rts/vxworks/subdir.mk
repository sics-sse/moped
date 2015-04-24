################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/vxworks/os.c \
../vmcore/src/rts/vxworks/os_devices.c \
../vmcore/src/rts/vxworks/os_main.c \
../vmcore/src/rts/vxworks/socket_release.c 

OBJS += \
./vmcore/src/rts/vxworks/os.o \
./vmcore/src/rts/vxworks/os_devices.o \
./vmcore/src/rts/vxworks/os_main.o \
./vmcore/src/rts/vxworks/socket_release.o 

C_DEPS += \
./vmcore/src/rts/vxworks/os.d \
./vmcore/src/rts/vxworks/os_devices.d \
./vmcore/src/rts/vxworks/os_main.d \
./vmcore/src/rts/vxworks/socket_release.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/vxworks/%.o: ../vmcore/src/rts/vxworks/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


