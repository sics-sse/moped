#------------------------------------------------------------------------------
#-      EB40a Initialisation routine
#-      Flash version
#------------------------------------------------------------------------------

       	.INCLUDE     "parts/r40008/gnu_r40008.inc"  /*- library definition */

#;----------------------------------------
#;- AT91EB40a External Memories Definition
#;----------------------------------------

#;- Flash Memory : AT49BV1614
.equ  		FLASH_BASE,           0x01000000
.equ  		FLASH_SIZE,           0x200000

#;- EXTERNAL SRAM MEMORY

#;- WARNING !! No External memories are fitted on the board.
#;- This definition is only if the user add external SRAM devices.
#;-
#;- EXT_SRAM_BASE        0x02000000
#;- EXT_SRAM_SIZE        (256*1024)
#;- EXT_SRAM_LIMIT       (EXT_SRAM_BASE + EXT_SRAM_SIZE)

#;- Redefinition of External SRAM to Internal SRAM

#- Don't understand these definitions!
.equ  		EXT_SRAM_BASE,        0x02000000	/* 0x0 */
.equ  		EXT_SRAM_SIZE,        RAM_SIZE		/* (256*1024) */

.equ  		EXT_SRAM_LIMIT,       (RAM_BASE + RAM_SIZE)

#;------------------------------------------------------------------------------
#;- EBI Initialization Data
#;-------------------------
#;- The EBI User Interface Image which is copied by the boot.
#;- 32,768MHz master clock assumed.
#;------------------------------------------------------------------------------

.equ  		EBI_CSR_0,            (FLASH_BASE | 0x2531)    /* 0x01000000, 16MB, 2 tdf, 16 bits, 5 WS */
.equ  		EBI_CSR_1,            (EXT_SRAM_BASE | 0x2525) /* 0x02000000, 16MB, 2 TDF, 16 bits, 2 WS */
.equ  		EBI_CSR_2,            0x20000000  /* unused */
.equ  		EBI_CSR_3,            0x30000000  /* unused */
.equ  		EBI_CSR_4,            0x40000000  /* unused */
.equ  		EBI_CSR_5,            0x50000000  /* unused */
.equ  		EBI_CSR_6,            0x60000000  /* unused */
.equ  		EBI_CSR_7,            0x70000000  /* unused */

#;Master Clock
.equ  		MCK,              66000

#;------------------------------------------------------------------------------
#;- Area Definition
#;-----------------
#;- The .reset section must be located at the first location in flash, so that
#;- at reset execution begins at this point
#;------------------------------------------------------------------------------
	.section 	.reset, "xa"
	.code 32
	.align 	0

	.global	_start
_start:

#;------------------------------------------------------------------------------
#;- Exception vectors
#;--------------------
#;- The following vectors are used only up to the remap.
#;- For the moment, all are just looping (which may be
#;- dangerous in a final system). If an exception occurs before remap, this
#;- would result in an infinite loop.
#;------------------------------------------------------------------------------
                B           InitReset       /*0x00000000 reset */
undefvec:
                B           undefvec        /*0x00000004 Undefined Instruction */
swivec:
                B           swivec          /*0x00000008 Software Interrupt */
pabtvec:
                B           pabtvec         /*0x0000000C Prefetch Abort */
dabtvec:
                B           dabtvec         /*0x00000010 Data Abort */
rsvdvec:
                B           rsvdvec         /*0x00000014  */
irqvec:
                B           irqvec          /*0x00000018 IRQ */
fiqvec:
                B           fiqvec          /*0x0000001C FIQ */

#;------------------------------------------------------------------------------
#;- Exception vectors ( after cstartup execution )
#;------------------------------------
#;- These vectors are located at RAM address 0x0 after the remap command
#;- is performed in the EBI. As they will be relocated at address 0x0
#;- relative addressing is forbidden. The only possibility to get an absolute
#;- addressing for an ARM vector is to read a PC relative value at a defined
#;- offset. It is easy to reserve the locations 0x20 to 0x3C (the 8 next
#;- vectors) for storing the absolute exception handler address.
#;- The AIC vectoring access vectors are saved in the interrupt and fast
#;- interrupt ARM vectors. So, only 5 offsets are required ( reserved vector
#;- offset is never used).
#;- The initial handlers perform an infinite loop and can be
#;- modified at any time.
#;- Note also that the reset is only accessible by a jump from the application
#;- to 0. It is an actual software reset.
#;- As the 13 first locations are used by the vectors, the read/write link
#;- address must be defined from 0x34 if internal data mapping is required.
#;------------------------------------------------------------------------------
.extern	UndefHandler
.extern	SWIHandler
.extern	PrefetchAbortHandler
.extern	DataAbortHandler
VectorTable:
                ldr         pc, [pc, #0x18]         /* SoftReset	    */
                ldr         pc, [pc, #0x18]         /* UndefHandler	    */
                ldr         pc, [pc, #0x18]         /* SWIHandler	    */
                ldr         pc, [pc, #0x18]         /* PrefetchAbortHandler */
                ldr         pc, [pc, #0x18]         /* DataAbortHandler	    */
                nop                                 /* Reserved		    */
                ldr         pc, [pc,#-0xF20]        /* IRQ : read the AIC   */
                ldr         pc, [pc,#-0xF20]        /* FIQ : read the AIC   */

#;- There are only 5 offsets as the vectoring is used.
                .word         InitRemap
                .word         UndefHandler
                .word         SWIHandler
                .word         PrefetchAbortHandler
                .word         DataAbortHandler

#;------------------------------------------------------------------------------
#;- EBI Initialization Data
#;-------------------------
#;- The EBI values depend on target choice , Clock, and memories access time.
#;- The EBI User Interface Image which is copied by the boot.
#;- The EBI_CSR_x are defined in the target and hardware depend.
#;- That's hardware! Details in the Electrical Datasheet of the AT91 device.
#;- EBI Base Address is added at the end for commodity in copy code.
#;- ICE note :For ICE debug no need to set the EBI value these values already set
#;- by the boot function.
#;------------------------------------------------------------------------------
InitTableEBI:
            .word         EBI_CSR_0
            .word         EBI_CSR_1
            .word         EBI_CSR_2
            .word         EBI_CSR_3
            .word         EBI_CSR_4
            .word         EBI_CSR_5
            .word         EBI_CSR_6
            .word         EBI_CSR_7
            .word         0x00000001  /* REMAP command			 */
            .word         0x00000006  /* 6 memory regions, standard read	 */
PtEBIBase:								 
            .word         EBI_BASE    /* EBI Base Address			 */

#;------------------------------------------------------------------------------
#;- The reset handler before Remap
#;--------------------------------
#;- The vector at 0x0 jumps to here following a reset
#;------------------------------------------------------------------------------
InitReset:

#;------------------------------------------------------------------------------
#;- Speed up the Boot sequence
#;----------------------------
#;- After reset, the number os wait states on chip select 0 is 8. All AT91
#;- Evaluation Boards fits fast flash memories, so that the number of wait
#;- states can be optimized to fast up the boot sequence.
#;- ICE note :For ICE debug no need to set the EBI value these values already set
#;- by the boot function.
#;------------------------------------------------------------------------------
#;- Load System EBI Base address and CSR0 Init Value
                ldr     r0, PtEBIBase
                ldr     r1, [pc,#-(8+.-InitTableEBI)] /* values (relative)  */

#;- Speed up code execution by disabling wait state on Chip Select 0
                str     r1, [r0]

#;------------------------------------------------------------------------------
#;- Setup Exception Vectors in Internal RAM before Remap
#;------------------------------------------------------
#;- It's important to perform this operation before Remap in order to guarantee
#;- that the core has valid vectors at any time during the remap operation.
#;- Note: There are only 5 offsets as the vectoring is used.
#;-  Before Remap the internal RAM is at 0x300000
#;-  After  Remap the internal RAM is at 0x000000
#;-  If Remap has already executed it's not possible to write to 0x300000.
#;------------------------------------------------------------------------------
#;- Copy the ARM exception vectors

#;- The RAM_BASE = 0 it's specific for ICE
                ldr     r8,.RAM_BASE            /* @ of the hard vector after remap  in internal RAM 0x0 */

                add     r9, pc,#-(8+.-VectorTable)  /* @ where to read values (relative)  */
                ldmia   r9!, {r0-r7}            /* read 8 vectors  */

                stmia   r8!, {r0-r7}            /* store them				*/

                ldmia   r9!, {r0-r4}            /* read 5 absolute handler addresses	*/
                stmia   r8!, {r0-r4}            /* store them				*/

#;------------------------------------------------------------------------------
#;- Initialise the Memory Controller
#;----------------------------------
#;- That's principaly the Remap Command. Actually, all the External Bus
#;- Interface is configured with some instructions and the User Interface Image
#;- as described above. The jump "mov pc, r12", which continues execution by
#;- jumping to the new location InitRemap, is located after the remap
#;- but will actually be executed thanks to the Arm core pipeline.
#;- The IniTableEBI addressing must be relative .
#;- The PtInitRemap must be absolute as the processor jumps at this address
#;- immediatly after the Remap is performed.
#;- Note also that the EBI base address is loaded in r11 by the "ldmia".
#;------------------------------------------------------------------------------
#;- Copy the Image of the Memory Controller
                sub     r10, pc,#(8+.-InitTableEBI) /* get the address of the chip select register image  */
                ldr     r12, PtInitRemap            /* get the real jump address ( after remap )*/

#;- Copy Chip Select Register Image to Memory Controller and command remap
                ldmia   r10!, {r0-r9,r11}           /* load the complete image and the EBI base */
                stmia   r11!, {r0-r9}               /* store the complete image with the remap command */

#;- Jump to ROM at its new address
                mov     pc, r12                     /* jump and break the pipeline	*/

PtInitRemap:
                .word     InitRemap                 /* address where to jump after REMAP */
.RAM_BASE:
				.word	0x300000					/* address of RAM before the remap */

#;- Default Interrupt Handlers
AicData:
                .word     AIC_BASE                /* AIC Base Address */
#;------------------------------------------------------------------------------
#;- Default Interrupt Handler
#;---------------------------
#;- These function are defined in the AT91 library. If you want to change this
#;- you can redifine these function in your appication code
#;------------------------------------------------------------------------------

                .global  at91_default_fiq_handler
                .global  at91_default_irq_handler
                .global  at91_spurious_handler
PtDefaultHandler:
                .word     at91_default_fiq_handler
                .word     at91_default_irq_handler
                .word     at91_spurious_handler

#;------------------------------------------------------------------------------
#;- The Reset Handler after Remap
#;-------------------------------
#;- The zero vector jumps here if used after a reset
#;- From here, the code executes from its designated link address.
#;------------------------------------------------------------------------------
InitRemap:

#;------------------------------------------------------------------------------
#;- Reset the Interrupt Controller
#;--------------------------------
#;- Normally, the code is executed only if a reset has been actually performed.
#;- So, the AIC initialization resumes at setting up the default vectors.
#;------------------------------------------------------------------------------
#;- Load the AIC Base Address and the default handler addresses
                ldr     r0, AicData  /* @ where to read values */
                ldmia   r0, {r1-r4}

#;- Setup the Spurious Vector
                str     r4, [r1, #AIC_SPU]      /* r4 = spurious handler  */

#;- mask all interrupts and clear pending ones
				mov		r0, #0
				sub		r0, r0, #1
				str		r0, [r1, #AIC_IDCR | 0]
				str		r0, [r1, #AIC_ICCR | 0]
				
#;- ICE note : For ICE debug
#;- Perform 8 End Of Interrupt Command to make sure AIC will not lock out nIRQ
                mov         r0, #8
LoopAic0:
                str         r1, [r1, #AIC_EOICR]    /* any value written  */
                subs        r0, r0, #1
                bhi         LoopAic0

#;- Set up the default interrupt handler vectors
                str     r2, [r1, #AIC_SVR]      /* SVR[0] for FIQ  */
                add     r1, r1, #AIC_SVR
                mov     r0, #31                 /* counter   */
LoopAic1:
                str     r3, [r1, r0, LSL #2]    /* SVRs for IRQs   */
                subs    r0, r0, #1              /* do not save FIQ */
                bhi     LoopAic1
                
# Now continue with common code
