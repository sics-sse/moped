################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/vm/fp/e_rem_pio2.c \
../vmcore/src/vm/fp/e_sqrt.c \
../vmcore/src/vm/fp/fp_bytecodes.c \
../vmcore/src/vm/fp/k_cos.c \
../vmcore/src/vm/fp/k_rem_pio2.c \
../vmcore/src/vm/fp/k_sin.c \
../vmcore/src/vm/fp/k_tan.c \
../vmcore/src/vm/fp/s_ceil.c \
../vmcore/src/vm/fp/s_copysign.c \
../vmcore/src/vm/fp/s_cos.c \
../vmcore/src/vm/fp/s_fabs.c \
../vmcore/src/vm/fp/s_floor.c \
../vmcore/src/vm/fp/s_scalbn.c \
../vmcore/src/vm/fp/s_sin.c \
../vmcore/src/vm/fp/s_tan.c \
../vmcore/src/vm/fp/w_sqrt.c 

OBJS += \
./vmcore/src/vm/fp/e_rem_pio2.o \
./vmcore/src/vm/fp/e_sqrt.o \
./vmcore/src/vm/fp/fp_bytecodes.o \
./vmcore/src/vm/fp/k_cos.o \
./vmcore/src/vm/fp/k_rem_pio2.o \
./vmcore/src/vm/fp/k_sin.o \
./vmcore/src/vm/fp/k_tan.o \
./vmcore/src/vm/fp/s_ceil.o \
./vmcore/src/vm/fp/s_copysign.o \
./vmcore/src/vm/fp/s_cos.o \
./vmcore/src/vm/fp/s_fabs.o \
./vmcore/src/vm/fp/s_floor.o \
./vmcore/src/vm/fp/s_scalbn.o \
./vmcore/src/vm/fp/s_sin.o \
./vmcore/src/vm/fp/s_tan.o \
./vmcore/src/vm/fp/w_sqrt.o 

C_DEPS += \
./vmcore/src/vm/fp/e_rem_pio2.d \
./vmcore/src/vm/fp/e_sqrt.d \
./vmcore/src/vm/fp/fp_bytecodes.d \
./vmcore/src/vm/fp/k_cos.d \
./vmcore/src/vm/fp/k_rem_pio2.d \
./vmcore/src/vm/fp/k_sin.d \
./vmcore/src/vm/fp/k_tan.d \
./vmcore/src/vm/fp/s_ceil.d \
./vmcore/src/vm/fp/s_copysign.d \
./vmcore/src/vm/fp/s_cos.d \
./vmcore/src/vm/fp/s_fabs.d \
./vmcore/src/vm/fp/s_floor.d \
./vmcore/src/vm/fp/s_scalbn.d \
./vmcore/src/vm/fp/s_sin.d \
./vmcore/src/vm/fp/s_tan.d \
./vmcore/src/vm/fp/w_sqrt.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/vm/fp/%.o: ../vmcore/src/vm/fp/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


