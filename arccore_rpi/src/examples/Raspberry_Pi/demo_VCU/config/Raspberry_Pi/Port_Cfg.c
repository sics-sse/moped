/*
* Configuration of module: Port (Port_Cfg.c)
*
* Created by:              
* Copyright:               
*
* Configured for (MCU):    bcm2835
*
* Module vendor:           SICS
* Generator version:       null
*
*/

	
#include "Port.h"

const Port_ConfigType PortConfigData =
{
    .gipo_select0 = &(*(volatile uint32_t *)(0x2020001C)),
	.gipo_clear0  = &(*(volatile uint32_t *)(0x20200028)),
	.gipo_level0  = &(*(volatile uint32_t *)(0x20200034)),
	.gipo_pudclk0 = &(*(volatile uint32_t *)(0x20200098)),

	.gipo_select1 = &(*(volatile uint32_t *)(0x20200020)),
	.gipo_clear1  = &(*(volatile uint32_t *)(0x2020002C)),
	.gipo_level1  = &(*(volatile uint32_t *)(0x2020002C)),
	.gipo_pudclk1 = &(*(volatile uint32_t *)(0x2020009C)),
	.gipo_pin_base = 32,

};
