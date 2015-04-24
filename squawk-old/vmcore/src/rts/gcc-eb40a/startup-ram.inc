.equ 		ARM_MODE_USER,	0x10
.equ 		ARM_MODE_FIQ,	0x11
.equ 		ARM_MODE_IRQ,	0x12
.equ 		ARM_MODE_SVC,	0x13
.equ 		ARM_MODE_ABORT,	0x17
.equ 		ARM_MODE_UNDEF,	0x1B
.equ 		ARM_MODE_SYS,	0x1F

.equ 		I_BIT,			0x80
.equ 		F_BIT,			0x40
.equ 		T_BIT,			0x20

.equ	IRQ_VECTOR_ADDRESS,	0x18
.equ	IRQ_REDBBOOT_VECTOR_ADDRESS,	IRQ_VECTOR_ADDRESS+0x20

	.global _start

	.text

VectorIRQ:
        sub         sp,sp,#4 		/* Reserve one word for PC */
        stmfd       sp!, {r0-r3}	/* Save context */
	
        /* get interrupt data from table below */
        add     	r0, pc,#-(8+.-vectored_handler)
        ldmia   	r0, {r0-r2}
	
		/* Test IPR to see if WD request pending */
    	ldr         r3,[r0, #0x10C]  /* AIC_IPR */
    	ands		r1,r3,r1
		/* get vector from IVR if WD request */
		ldrne       r2,[r0, #0x100]  /* AIC_IVR */
    	/* save the branch address in stack */
        add         sp, sp,#(5*4)  ; 
        stmfd       sp, {r2}
    	sub         sp,sp,#(5*4)            
    	ldmia       sp!,{r0-r3,r15}    	

/* Assume Red Boot using USART 0 for debug */
	.equ	  IMR_RED_BOOT,    	    0x00000004
	.equ	  IMR_WD,	    	    0x00000080

vectored_handler:
    .word     0xfffff000            /* R0:AIC Base Address */
	.word     IMR_WD			    /* R1: mask for our WD interrupt */
	.word     0x01006484			/* R2: addr of Redboot IRQ handler */
	# TODO - remove this nasty hard coded address	

_start:
		/* overwrite red Boot's IRQ vector with address of our handler */
		mov		r8, #IRQ_REDBBOOT_VECTOR_ADDRESS
    	add     r9, pc,#-(8+.-VectorIRQ)  	/* handler addr  */
    	str     r9, [r8]                   	/* store it		*/

    	# continue with common code
    	
