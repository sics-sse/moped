################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include vmcore/src/vm/util/subdir.mk
-include vmcore/src/vm/fp/subdir.mk
-include vmcore/src/vm/subdir.mk
-include vmcore/src/rts/vxworks/subdir.mk
-include vmcore/src/rts/msc/subdir.mk
-include vmcore/src/rts/gcc-rpi-autosar/subdir.mk
-include vmcore/src/rts/gcc-rpi/subdir.mk
-include vmcore/src/rts/gcc-macosx/subdir.mk
-include vmcore/src/rts/gcc-eb40a/subdir.mk
-include vmcore/src/rts/gcc-9200/subdir.mk
-include vmcore/src/rts/gcc/subdir.mk
-include vmcore/src/rts/cortex-m3/src/MbedTest/subdir.mk
-include vmcore/src/rts/cc/subdir.mk
-include vmcore/build/subdir.mk
-include phoneme/midp/src/rms/rms_api/reference/native/subdir.mk
-include phoneme/midp/src/i18n/i18n_port/default/native/subdir.mk
-include phoneme/midp/src/i18n/i18n_main/reference/native/subdir.mk
-include cldc-native-declarations/build/com/sun/cldc/jna/subdir.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: squawk

# Tool invocations
squawk: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: GCC C Linker'
	gcc  -o "squawk" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(EXECUTABLES)$(OBJS)$(C_DEPS) squawk
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
