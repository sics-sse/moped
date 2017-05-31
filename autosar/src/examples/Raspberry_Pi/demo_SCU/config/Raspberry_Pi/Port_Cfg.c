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
#include "bcm2835.h"

const Port_ConfigType PortConfigData =
{
    .gipo_select0 = &GPSET0,
	.gipo_clear0  = &GPCLR0,
	.gipo_level0  = &GPLEV0,
	.gipo_pudclk0 = &GPPUDCLK0,

	.gipo_select1 = &GPSET1,
	.gipo_clear1  = &GPCLR1,
	.gipo_level1  = &GPLEV1,
	.gipo_pudclk1 = &GPPUDCLK1,
	.gipo_pin_base = 32,

};
