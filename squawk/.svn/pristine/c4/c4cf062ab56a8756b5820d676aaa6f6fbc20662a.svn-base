#  Project Name
PROJECT=mbed_test
#  List of the objects files to be compiled/assembled
OBJECTS=startup_LPC17xx.o core_cm3.o system_LPC17xx.o os_main.o 
LSCRIPT=LPC17xx.ld

OPTIMIZATION = 0
DEBUG = 

#LISTING = -ahls

#  Compiler Options
GCFLAGS =  -Wall -fno-common -mcpu=cortex-m3 -mthumb -O$(OPTIMIZATION) $(DEBUG) -IE:/CodeSourcery/SourceryG++Lite/arm-none-eabi/include/c++/4.4.1 -IE:/CodeSourcery/SourceryG++Lite/arm-none-eabi/include/c++/4.4.1/arm-none-eabi/thumb2

#GCFLAGS += -Wcast-align -Wcast-qual -Wimplicit -Wpointer-arith -Wswitch
#GCFLAGS += -Wredundant-decls -Wreturn-type -Wshadow -Wunused
LDFLAGS = -v -LE:\CodeSourcery\SourceryG++Lite\arm-none-eabi\lib\thumb2  -lc -lm -lgcc -mcpu=cortex-m3 -mthumb -O$(OPTIMIZATION) -nostartfiles -Wl,-Map=$(PROJECT).map -T$(LSCRIPT) 
ASFLAGS = $(LISTING) -mcpu=cortex-m3

#  Compiler/Assembler/Linker Paths
GCC = arm-none-eabi-gcc
AS = arm-none-eabi-as
LD = arm-none-eabi-ld
OBJCOPY = arm-none-eabi-objcopy
REMOVE = rm -f
SIZE = arm-none-eabi-size

#########################################################################

all:: $(PROJECT).hex $(PROJECT).bin

$(PROJECT).bin: $(PROJECT).elf
	$(OBJCOPY) -O binary -j .text -j .data $(PROJECT).elf $(PROJECT).bin

$(PROJECT).hex: $(PROJECT).elf
	$(OBJCOPY) -R .stack -O ihex $(PROJECT).elf $(PROJECT).hex

$(PROJECT).elf: $(OBJECTS)
	$(GCC) $(LDFLAGS) $(OBJECTS) -o $(PROJECT).elf

stats: $(PROJECT).elf
	$(SIZE) $(PROJECT).elf

clean:
	$(REMOVE) $(OBJECTS)
	$(REMOVE) $(PROJECT).hex
	$(REMOVE) $(PROJECT).elf
	$(REMOVE) $(PROJECT).map
	$(REMOVE) $(PROJECT).bin
	$(REMOVE) $(OBJECTS)

#########################################################################
#  Default rules to compile .c and .cpp file to .o
#  and assemble .s files to .o

.c.o :
	$(GCC) $(GCFLAGS) -c $<

.cpp.o :
	$(GCC) $(GCFLAGS) -c $<

.S.o :
	$(AS) $(ASFLAGS) -o $(PROJECT)_crt.o $< > $(PROJECT)_crt.lst

#########################################################################