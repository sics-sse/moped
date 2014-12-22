.extern Os_Sys
.extern Irq_Entry
.extern Os_ArchPanic
.extern Os_ArchTest
.section .text
.code 32
.balign 4
.global IrqHandler
IrqHandler:
 sub lr, lr, #4
 srsdb sp!, #31
 cpsid if, #31
 push {r0-r12,lr}
    sub sp,sp,#8
    mov r4,#0xad
    str r4,[sp,#4]
    mov r0,sp
 ldr r4,=Os_Sys
 ldr r5,[r4,#16]
 cmp r5, #0
 bgt arggg
 ldr sp,[r4,#28]
arggg:
    bl dmb
    bl Irq_Entry
    bl dmb
    mov sp, r0
    add sp,sp,#8
    pop {r0-r12,lr}
    rfeia sp!
  .global Os_ArchSetSpAndCall
  .type Os_ArchSetSpAndCall, %function
Os_ArchSetSpAndCall:
    mov sp,r0
 mov lr,r1
 bx lr
  .global Os_ArchSwapContext
  .type Os_ArchSwapContext, %function
Os_ArchSwapContext:
    push {r0-r12,lr}
    sub sp,sp,#8
    mov r4,#0xde
    str r4,[sp,#4]
    mov r4,sp
    str r4,[r0,#0]
 .global Os_ArchSwapContextTo
 .type Os_ArchSwapContextTo, %function
Os_ArchSwapContextTo:
    ldr r2,[r1,#0]
    mov sp,r2
    ldr r5,= Os_Sys
    str r1,[r5,#0]
    ldr r6,[sp,#4]
    cmp r6,#0xde
    beq os_sc_restore
    cmp r6,#0xad
    beq os_lc_restore
    b Os_ArchPanic
os_sc_restore:
    add sp,sp,#8
    pop {r0-r12,lr}
    bx lr
os_lc_restore:
    add sp,sp,#8
    pop {r0-r12,lr}
    rfeia sp!
.global dmb
dmb:
 .func dmb
 mov r12, #0
 mcr p15, 0, r12, c7, c10, 5
 mov pc, lr
 .endfunc
.global disable
.type disable, %function
disable:
 .func disable
 mrs r0, cpsr
 cpsid i
 mov pc, lr
 .endfunc
.global restore
.type restore, %function
restore:
.func restore
 msr cpsr_c, r0
 mov pc, lr
.endfunc
