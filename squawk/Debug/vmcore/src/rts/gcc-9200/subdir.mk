################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../vmcore/src/rts/gcc-9200/9200-io.c \
../vmcore/src/rts/gcc-9200/mmu_fat.c \
../vmcore/src/rts/gcc-9200/os.c 

OBJS += \
./vmcore/src/rts/gcc-9200/9200-io.o \
./vmcore/src/rts/gcc-9200/mmu_fat.o \
./vmcore/src/rts/gcc-9200/os.o 

C_DEPS += \
./vmcore/src/rts/gcc-9200/9200-io.d \
./vmcore/src/rts/gcc-9200/mmu_fat.d \
./vmcore/src/rts/gcc-9200/os.d 


# Each subdirectory must supply rules for building sources it contributes
vmcore/src/rts/gcc-9200/%.o: ../vmcore/src/rts/gcc-9200/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


