.IFDEF TARGET_IS_FLASH
/* .INCLUDE "../startup-flash.inc" */
    .INCLUDE     "parts/r40008/gnu_r40008.inc"  /*- library definition */
	.section 	.reset, "xa"
	.code 32
	.align 	0
	.global	_start
_start:
				mov		r6, r0		/* save parameter passed from bootloader */
				mov		r8, r1		/* save parameter passed from bootloader */
				mov		r9, r2		/* save parameter passed from bootloader */
				mov		r10, r3		/* save parameter passed from bootloader */
.ELSE
.INCLUDE "startup-ram.inc"

				mov		r6, #0		/* set parameter to 0 */

#;------------------------------------------------------------------------------
#;- Stack Sizes Definition
#;------------------------

.equ IRQ_STACK_SIZE,     (30*8*4)    /* 30 words per interrupt priority level	*/
.equ FIQ_STACK_SIZE,     (3*4)       /* 3 words   */
.equ ABT_STACK_SIZE,     (1*4)       /* 1 word    */
.equ UND_STACK_SIZE,     (1*4)       /* 1 word    */
.equ SVC_STACK_SIZE,     (100*4)       /* 1 word    */
						     
#;------------------------------------------------------------------------------
#;- Setup the stack for each mode
#;-------------------------------
                ldr     r0, =__ram_top

#;- Set up Fast Interrupt Mode and set FIQ Mode Stack
			    mov     r1, #ARM_MODE_FIQ | I_BIT | F_BIT
                msr     cpsr_c, R1
                mov     r13, r0                     /* Init stack FIQ */
                sub     r0, r0, #FIQ_STACK_SIZE | 0

#;- Set up Interrupt Mode and set IRQ Mode Stack
			    mov     r1, #ARM_MODE_IRQ | I_BIT | F_BIT
                msr     cpsr_c, R1 
                mov     r13, r0                     /* Init stack IRQ */
                sub     r0, r0, #IRQ_STACK_SIZE | 0

#;- Set up Abort Mode and set Abort Mode Stack
				mov     r1, #ARM_MODE_ABORT | I_BIT | F_BIT
                msr     cpsr_c, R1
                mov     r13, r0                     /* Init stack Abort*/
                sub     r0, r0, #ABT_STACK_SIZE | 0

#;- Set up Undefined Instruction Mode and set Undef Mode Stack
				mov     r1, #ARM_MODE_UNDEF | I_BIT | F_BIT
                msr     cpsr_c, R1 
                mov     r13, r0                     /* Init stack Undef	*/
                sub     r0, r0, #UND_STACK_SIZE | 0

#;- Set up Supervisor Mode and set Supervisor Mode Stack
				mov     r1, #ARM_MODE_SVC | I_BIT | F_BIT
                msr     cpsr_c, R1 
                mov     r13, r0                     /* Init stack Sup */
                sub     r0, r0, #SVC_STACK_SIZE | 0

#;- Setup Application Operating Mode and Enable the interrupts
				mov     r1,  #ARM_MODE_SYS | I_BIT | F_BIT          /* set System mode   */
				msr     cpsr_c, R1    
				mov     r13, r0						/* Init stack User */
				mov     r1,  #ARM_MODE_SYS | 0
				msr     cpsr_c, R1    
.ENDIF /* NOT TARGET_IS_FLASH */

#;------------------------------------------------------------------------------
#;- Initialise C variables
#;------------------------
				mov 	R0, #0			/* Second arg: fill value */
				mov	    R11, R0			/* fp: Null frame pointer */
				mov	    r7, R0			/* Null frame pointer for Thumb */

#;- Clear BSS (zero init)
#;- ----------------------
	
				ldr	    R1, .BSS_START		/* a1: First arg: start of memory block */
				ldr	    R2, .BSS_END		/* a3: Second arg: end of memory block */
Loop0:          
				cmp     R1, R2              /* Zero init */
    			strcc   r0, [r1], #4
    			bcc     Loop0

.IFDEF TARGET_IS_FLASH
#;- Copy Initialized data from FLASH to RAM
#;- ---------------------------------------

				ldr		R1, .INIT_DATA_FROM
				ldr		R2, .INIT_DATA_END
				ldr		R3, .INIT_DATA_TO
 
LoopRW:
				cmp     r3, r2                  
				ldrcc   r0, [r1], #4
				strcc   r0, [r3], #4
				bcc     LoopRW      
.ENDIF
	
#;- Set up command line arguments
#;- -----------------------------
				mov		r0, r6			/*  first argument  */
				mov		r1, r8			/*  second argument  */
				mov		r2, r9			/*  third argument  */
				mov		r3, r10			/*  fourth argument  */

#;------------------------------------------------------------------------------
#;- Branch on C code Main function (with interworking)
#;----------------------------------------------------
#;- Branch must be performed by an interworking call as either an ARM or Thumb
#;- main C function must be supported. This makes the code not position-
#;- independant. A Branch with link would generate errors
#;------------------------------------------------------------------------------
    .extern     arm_main

				ldr     r4, =arm_main
				mov     lr, pc
				bx      r4

#;------------------------------------------------------------------------------
#;- Loop for ever
#;---------------
#;- End of application. Normally, never occurs.
#;- Could jump to Software Reset ( B 0x0 ).
#;------------------------------------------------------------------------------
	.global		exit
exit:
		        b       0x0

#;------------------------------------------------------
#;- Interrupt Handlers for exception traps
#;
#;------------------------------------------------------
.global		UndefHandler
.global		SWIHandler
.global		PrefetchAbortHandler
.global		DataAbortHandler
.extern		armTrapHandler

UndefHandler:
			# put the code for this handler in r1 (2nd param)
			mov			r1, #1
			b			LrAdjust4
SWIHandler:
			# put the code for this handler in r1 (2nd param)
			mov			r1, #2
			b			LrAdjust4
PrefetchAbortHandler:
			# put the code for this handler in r1 (2nd param)
			mov			r1, #3
			b			LrAdjust4
DataAbortHandler:
			# put the addr of the offending instruction
			# in r0 (1st parameter)
			sub			r0, lr, #8
			# put the code for this handler in r1 (2nd param)
			mov			r1, #4
			b			CommonHandler
LrAdjust4:
			# put the addr of the offending instruction
			# in r0 (1st parameter)
			sub			r0, lr, #4
CommonHandler:
			# switch to supervisor mode
			# must be in a priviledged mode so that the restart
			# can work
			mov     	r2, #ARM_MODE_SVC | I_BIT | F_BIT
			msr     	cpsr_c, R2
			# call the C trap handler
			ldr     	r2, =armTrapHandler
			mov     	lr, pc
			bx      	r2
			# jump to 0 to restart
			b			0x0

#;- Define locations of important data areas
#;- ----------------------------------------
	.align 0
.BSS_START:
	.word	__bss_start
.BSS_END:
	.word	__bss_end

.IFDEF TARGET_IS_FLASH
.INIT_DATA_FROM:
	.word	__indata_from
.INIT_DATA_END:
	.word	__indata_end
.INIT_DATA_TO:	
	.word	__indata_start
.ENDIF
