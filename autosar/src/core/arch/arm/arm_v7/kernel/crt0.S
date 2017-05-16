	.syntax unified

	.extern	__ram_end__
	.extern	__und_stack_size__
	.extern __abt_stack_size__
	.extern __fiq_stack_size__
	.extern __irq_stack_size__
	.extern __svc_stack_size__
	.extern _bss_start
	.extern _bss_end
	.extern IrqHandler
	.extern Irq_Entry
	.extern main

	.set    MODE_USR, 0x10
	.set    MODE_FIQ, 0x11
	.set    MODE_IRQ, 0x12
	.set    MODE_SVC, 0x13
	.set    MODE_ABT, 0x17
	.set    MODE_UND, 0x1B
	.set    MODE_SYS, 0x1F

	.set    I_BIT, 0x80
	.set    F_BIT, 0x40

	.section .init
	.code 32
	.balign 4
	.globl _start
_start:
	/* All the following instruction should be read as: Load the address at symbol into the program counter.*/
	ldr  pc,reset_handler		/* Processor Reset handler -- we will have to force this on the raspi! Because this is the first instruction executed, of cause it causes an immediate branch into reset!*/
	ldr  pc,undefined_handler	/* Undefined instruction handler -- processors that dont have thumb can emulate thumb!*/
	ldr  pc,swi_handler			/* Software interrupt / TRAP (SVC) -- system SVC handler for switching to kernel mode.*/
	ldr  pc,prefetch_handler	/* Prefetch/abort handler.*/
	ldr  pc,data_handler		/* Data abort handler/*/
	nop
	ldr  pc,irq_handler			/* IRQ handler*/
	ldr  pc,fiq_handler			/* Fast interrupt handler.*/

/* Here we create an exception address table! This means that reset/hang/irq can be absolute addresses*/
reset_handler:
	.word ResetHandler
undefined_handler:
	.word UndHandler
swi_handler:
	.word SwiHandler
prefetch_handler:
	.word PrefetchHandler
data_handler:
	.word AbortHandler
	.word 0
irq_handler:
	.word IrqHandler
fiq_handler:
	.word FiqHandler

	.globl io_base_addr
io_base_addr:	.word 0
	.globl cpu_id
cpu_id:	.word 0

	.code   32
	.balign 4
	.global ResetHandler
ResetHandler:
	/*=================================================================
	  Auto-Detect Raspberry PI IO base address => sets io_base_addr
	  =================================================================*/
	ldr r4, =#0x61757830	// reset value of miniuart
	ldr r7, =#0x20215010
	ldr r3, [r7]		// Fetch value at 0x20215010 being uart0

	cmp r3, r4		// Check if we have the value the uart0 will be at reset
	bne .not_pi_1

	/* **** Detected Pi at Pi1 address being 0x20000000 ******/
	ldr r7, =io_base_addr
	mov r3, #0x20000000
	str r3, [r7]
	b .autodetect_done;

.not_pi_1:
	ldr r7, =#0x3f215010
	ldr r3, [r7]		// Fetch value at 0x3f215010

	cmp r3, r4		// Check if we have the value the uart0 will be at reset
	beq .pi_found

	/* **** Auto-Detected failed it is not safe to do anything but deadloop  ******/
.pi_detect_fail:
	b .pi_detect_fail

	/*@ **** Detected Pi at Pi2/3 address being 0x3f000000 ******/
.pi_found:
	/*; Pi2/3 address 0x3f000000 */
	ldr r7, =io_base_addr
	mov r3, #0x3f000000
	str r3, [r7]

.autodetect_done:
	/*=================================================================
	  Read the Arm Main CPUID register => sets cpu_id
	  =================================================================*/
	ldr r7, =cpu_id
	mrc p15,0, r0,c0,c0,0	// Read Main ID Register
	str r0, [r7]

	/*=================================================================
	  Bring fpu online
	  =================================================================*/
	/* global acces to fpu for everyone */
	mrc p15, 0, r0, c1, c1, 2
	orr r0, r0, #3<<10
	mcr p15, 0, r0, c1, c1, 2

	/* enable fpu */
	mrc p15,0,r0,c1,c0, #2			// R0 = Access Control Register
	orr r0, #(0x300000 + 0xC00000)	// Enable Single & Double Precision
	mcr p15,0,r0,c1,c0, #2			// Access Control Register = R0
	mov r0, #0x40000000				// R0 = Enable VFP
	vmsr fpexc,r0					// FPEXC = R0
	/* TODO: enable multicore */
.if (BCM2836 == 1 || BCM2837 == 1)
	mrc  p15, 0, r0, c0, c0, 5
	ands r0, r0, #0x3
	beq  reset_single_c0
reset_single_cn:
	wfi
	b    reset_single_cn
reset_single_c0:
.endif

.if (BCM2835 == 0)
	/* non-secure cpu mode*/
	mrc p15, 0, r0, c1, c1, 0
	orr r0, r0, #0x1
	mcr p15, 0, r0, c1, c1, 0
.endif

.if BCM2837 == 1
	/* invalidate caching and branch predictions */
	mov r12, #0
	mcr p15, 0, r12, c7, c10, 1
	dsb
	mov r12, #0
	mcr p15, 0, r12, c7, c5, 0
	mov r12, #0
	mcr p15, 0, r12, c7, c5, 6
	dsb
	isb
.endif
	/*=================================================================
	  Enable L1 Cache
	  =================================================================*/
	mrc p15, 0, r0, c1, c0, 0	;@ r0 = System Control Register
	orr r0, #0x0004			;@ Data Cache (Bit 2)
	orr r0, #0x0800			;@ Branch Prediction (Bit 11)
	orr r0, #0x1000			;@ Instruction Caches (Bit 12)
	mcr p15, 0, r0, c1, c0, 0	;@ System Control Register = R0

.if MULTI_CPU == 1
	/* cpu core reset branch */
	mrc p15, 0, r0, c0, c0, 5
	and r0, r0, #0x3
	cmp r0, #1
	beq reset_c1
	cmp r0, #2
	beq reset_c2
	cmp r0, #3
	beq reset_c3
.endif

	/*=================================================================
	  Clear bss
	  =================================================================*/
	mov r0, #0
	ldr r1, =_bss_start
	ldr r2, =_bss_end
bssloop:
	cmp r1, r2
	strlo r0, [r1], #4
	blo bssloop

	ldr     r0, =__ram_end__
	/* Undefined */
	msr cpsr_c, #MODE_UND | I_BIT | F_BIT
	mov sp, r0
	ldr r1, =__und_stack_size__
	sub r0, r0, r1
	/* Abort */
	msr cpsr_c, #MODE_ABT | I_BIT | F_BIT
	mov sp, r0
	ldr r1, =__abt_stack_size__
	sub r0, r0, r1
	/* FIQ */
	msr cpsr_c, #MODE_FIQ | I_BIT | F_BIT
	mov sp, r0
	ldr r1, =__fiq_stack_size__
	sub r0, r0, r1
	/* IRQ */
	msr cpsr_c, #MODE_IRQ | I_BIT | F_BIT
	mov sp, r0
	ldr r1, =__irq_stack_size__
	sub r0, r0, r1
	/* Supervisor */
	msr cpsr_c, #MODE_SVC | I_BIT | F_BIT
	mov sp, r0
	ldr r1, =__svc_stack_size__
	sub r0, r0, r1
	/* System */
	msr cpsr_c, #MODE_SYS | I_BIT | F_BIT
	mov sp, r0

.if BCM2835 == 1
	/* alignement exception behaviour. only readable on v7 or newer architecturs */
	mrc p15, 0, r0, c1, c0, 0
	orr r0, #1 << 22
	mcr p15, 0, r0, c1, c0, 0
.endif

	/* Call application entry point */
	mov     r2, #0xDF
	msr     cpsr_c, r2

.if DEBUG == 1
	// --> debug toggle gpio 26
	ldr r7, =io_base_addr
	add r7, r7, 0x200000 // gpio device offset
	ldr r3, [r7, #8] // GPFSEL2
	orr r3, r3, #0x01 << 18 // output
	str r3, [r7, #0x8]

	mov r3, #0x1<<26
	str r3, [r7, #0x28] //GPFCLR0, pin 26

toggle_loop:
	str r3, [r7, #0x1c] // GPFSET0 pin 26
	ldr r0, =#0x000fffff
	bl delay_loop
	str r3, [r7, #0x28] //GPFCLR0, pin 26
	ldr r0, =#0x000fffff
	bl delay_loop
	b toggle_loop
	// --> end debug
.endif

	b       main

	.code 32
	.balign 4

	.weak UndHandler
UndHandler:

	.weak SwiHandler
SwiHandler:

	.weak PrefetchHandler
PrefetchHandler:

	.weak AbortHandler
AbortHandler:

	.weak FiqHandler
FiqHandler:

	.global _unhandled_exception
_unhandled_exception:
	b _unhandled_exception

.if MULTI_CPU == 1
	/* TODO: proper multi core support */
reset_c1:
	/* set IRQ stack */
	mov r0, #0xD2
	msr cpsr_c, r0	;@ (PSR_IRQ_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	mov sp, #0x7000
	/* set FIQ stack */
	mov r0, #0xD1
	msr cpsr_c, r0	;@ (PSR_FIQ_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	mov sp, #0x3000
	/* set SVC stack */
	mov r0, #0xD3
	msr cpsr_c, r0	;@ (PSR_SVC_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	ldr r0, =0x7F80000
	mov sp, r0

	bl main
	b _unhandled_exception

reset_c2:
	/* set IRQ stack */
	mov r0, #0xD2
	msr cpsr_c, r0	;@ (PSR_IRQ_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	mov sp, #0x6000
	/* set FIQ stack */
	mov r0, #0xD1
	msr cpsr_c, r0	;@ (PSR_FIQ_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	mov sp, #0x2000
	/* set SVC stack */
	mov r0, #0xD3
	msr cpsr_c, r0	;@ (PSR_SVC_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	ldr r0, =0x7F00000
	mov sp, r0

	bl main
	b _unhandled_exception

reset_c3:
	/* set IRQ stack */
	mov r0, #0xD2
	msr cpsr_c, r0	;@ (PSR_IRQ_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	mov sp, #0x5000
	/* set FIQ stack */
	mov r0, #0xD1
	msr cpsr_c, r0	;@ (PSR_FIQ_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	mov sp, #0x1000
	/* set SVC stack */
	mov r0, #0xD3
	msr cpsr_c, r0	;@ (PSR_SVC_MODE|PSR_FIQ_DIS|PSR_IRQ_DIS)
	ldr r0, =0x7E80000
	mov sp, r0
	bl main
	b _unhandled_exception
.endif

.if DEBUG == 1
	.code 32
	.type delay_loop, %function
delay_loop:
	subs r0, r0, #1
	isb
	nop
	nop
	nop
	bne delay_loop
	mov pc, lr // return
.endif
