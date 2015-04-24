################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/vm/address.c \
../vmcore/src/vm/bytecodes.c \
../vmcore/src/vm/cio.c \
../vmcore/src/vm/debug.c \
../vmcore/src/vm/debugger.c \
../vmcore/src/vm/devices.c \
../vmcore/src/vm/io_delegating.c \
../vmcore/src/vm/io_metal.c \
../vmcore/src/vm/io_native.c \
../vmcore/src/vm/io_socket.c \
../vmcore/src/vm/lisp2.c \
../vmcore/src/vm/memory.c \
../vmcore/src/vm/msg.c \
../vmcore/src/vm/os_posix.c \
../vmcore/src/vm/squawk.c \
../vmcore/src/vm/suite.c \
../vmcore/src/vm/switch.c \
../vmcore/src/vm/trace.c \
../vmcore/src/vm/vm2c.c 

OBJS += \
./vmcore/src/vm/address.o \
./vmcore/src/vm/bytecodes.o \
./vmcore/src/vm/cio.o \
./vmcore/src/vm/debug.o \
./vmcore/src/vm/debugger.o \
./vmcore/src/vm/devices.o \
./vmcore/src/vm/io_delegating.o \
./vmcore/src/vm/io_metal.o \
./vmcore/src/vm/io_native.o \
./vmcore/src/vm/io_socket.o \
./vmcore/src/vm/lisp2.o \
./vmcore/src/vm/memory.o \
./vmcore/src/vm/msg.o \
./vmcore/src/vm/os_posix.o \
./vmcore/src/vm/squawk.o \
./vmcore/src/vm/suite.o \
./vmcore/src/vm/switch.o \
./vmcore/src/vm/trace.o \
./vmcore/src/vm/vm2c.o 

C_DEPS += \
./vmcore/src/vm/address.d \
./vmcore/src/vm/bytecodes.d \
./vmcore/src/vm/cio.d \
./vmcore/src/vm/debug.d \
./vmcore/src/vm/debugger.d \
./vmcore/src/vm/devices.d \
./vmcore/src/vm/io_delegating.d \
./vmcore/src/vm/io_metal.d \
./vmcore/src/vm/io_native.d \
./vmcore/src/vm/io_socket.d \
./vmcore/src/vm/lisp2.d \
./vmcore/src/vm/memory.d \
./vmcore/src/vm/msg.d \
./vmcore/src/vm/os_posix.d \
./vmcore/src/vm/squawk.d \
./vmcore/src/vm/suite.d \
./vmcore/src/vm/switch.d \
./vmcore/src/vm/trace.d \
./vmcore/src/vm/vm2c.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/vm/%.o: ../vmcore/src/vm/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


