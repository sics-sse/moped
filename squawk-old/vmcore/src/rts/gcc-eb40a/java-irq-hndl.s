#;
#; Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
#; DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
#; 
#; This program is free software; you can redistribute it and/or
#; modify it under the terms of the GNU General Public License version
#; 2 only, as published by the Free Software Foundation.
#; 
#; This program is distributed in the hope that it will be useful, but
#; WITHOUT ANY WARRANTY; without even the implied warranty of
#; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
#; General Public License version 2 for more details (a copy is
#; included at /legal/license.txt).
#; 
#; You should have received a copy of the GNU General Public License
#; version 2 along with this work; if not, write to the Free Software
#; Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
#; 02110-1301 USA
#; 
#; Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
#; Clara, CA 95054 or visit www.sun.com if you need additional
#; information or have any questions.
#;
#;------------------------------------------------------
#;- Interrupt Handler for 'Java' interrupts
#;
#;------------------------------------------------------
			.text
.INCLUDE     "periph/arm7tdmi/gnu_arm.inc"
.equ	AIC_BASE, 0xfffff000
.equ	AIC_EOICR, 0x130
.equ	AIC_ISR, 0x108
.equ	AIC_IDCR, 0x124

.equ	FRAME_R0	,	0x00
.equ	FRAME_R1	,	FRAME_R0+4
.equ	FRAME_R2	,	FRAME_R1+4
.equ	FRAME_R3	,	FRAME_R2+4
.equ	FRAME_R4	,	FRAME_R3+4
.equ	FRAME_R5	,	FRAME_R4+4
.equ	FRAME_R6	,	FRAME_R5+4
.equ	FRAME_R7	,	FRAME_R6+4
.equ	FRAME_R8	,	FRAME_R7+4
.equ	FRAME_R9	,	FRAME_R8+4
.equ	FRAME_R10	,	FRAME_R9+4
.equ	FRAME_R11	,	FRAME_R10+4
.equ	FRAME_R12	,	FRAME_R11+4
.equ	FRAME_PSR	,	FRAME_R12+4
.equ	FRAME_LR	,	FRAME_PSR+4
.equ	FRAME_PC	,	FRAME_LR+4
.equ	FRAME_SIZE	,	FRAME_PC+4


.global	java_irq_hndl

java_irq_hndl:
			# Adjust and save LR_irq
            sub         lr, lr, #4
            
            # Save context to stack
            stmdb       sp!, {r0-r4,r12,lr}

			# get the interrupt number into r0
            ldr         r4, =AIC_BASE
            ldr			r0, [r4, #AIC_ISR]
            
			# turn irq number into mask in r1
			mov			r1, #1
			mov			r1, r1, lsl r0
			
			# get the current status value into r3
			ldr			r2, =java_irq_status
			ldr			r3, [r2]
			# set the status bit and store status
			orr			r3, r3, r1
			str			r3, [r2]
			
			# mask this interrupt
			str			r1, [r4, #AIC_IDCR]

			# Mark the End of Interrupt on the AIC
            str         r0, [r4, #AIC_EOICR]
.ifndef KERNEL_SQUAWK
			ldmfd      	sp!, {r0-r4, r12, pc}^
.else
			# That marks the end of the basic interrupt handler.
			# Next we will
			# 1. Move the IRQ stack pointer up
			# 2. Switch to the SYSTEM stack with interrupts still disabled
			# 3. Make a stack frame and store r5-r11 into it
			# 4. Recover the saved context for r0-r4,r12,lr from the IRQ stack into r5-r11
			# 6. Use the recovered IRQ stack context to fill in the gaps in the stack frame
			# 7. Run the kernel vm interrupt routine (it will reenable interrupts where reqd.)
			# 8. Return using our carefully constructed stack frame for r0-r12,lr
            
            # save the spsr
            mrs			r2, spsr

            # copy the sp for the irq stack
            mov			r3, sp
            
            # reset the irq stack (earlier we stored r0-r4, r12, lr, hence 7 words)
            add			sp, sp, #7*4
            
            # switch to system mode (interrupts still off)
			mov     	r1,  #ARM_MODE_SYS | I_BIT | F_BIT
			msr     	cpsr_c, r1
			
			# make space on system stack
			sub sp, sp, #FRAME_SIZE-FRAME_R5
			# save r5-r11 into system stack frame
			stmia sp, {r5-r11}
			# get saved context from irq stack and stash it in high registers
			ldmia r3, {r5-r11}
			
			# this would be the logical place to enable interrupts from asm, 
			# but we aren't doing so as Squawk will enable them when it wants to

			# move context retrieved from irq stack into system stack frame
			# r0-r4 from irq stack
			stmdb		sp!, {r5-r9}			
			# r12 from irq stack
			str			r10, [sp, #FRAME_R12]	
			# spsr from irq stack
			str			r2, [sp, #FRAME_PSR]	
			# lr from irq stack
			str			r11, [sp, #FRAME_PC]	
			# lr from system stack
			str			lr, [sp, #FRAME_LR]	

			# call the C
.extern     deviceSignalHandler
			ldr     	r2, =deviceSignalHandler
			mov     	lr, pc
			bx      	r2
			
            # restore context and spsr (spsr via the lr??)
			ldmia		sp!, {r0-r12,lr}
			msr			spsr_cxsf, lr
			
			# return (already in system mode so no need to switch)
			ldmia		sp!, {lr, pc}^
			
.endif
