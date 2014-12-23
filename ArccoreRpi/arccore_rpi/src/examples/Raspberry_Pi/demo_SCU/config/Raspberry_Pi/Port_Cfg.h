/*
* Configuration of module: Port (Port_Cfg.h)
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


#if !(((PORT_SW_MAJOR_VERSION == 1) && (PORT_SW_MINOR_VERSION == 0)) )
#error Port: Configuration file expected BSW module version to be 1.0.*
#endif

#ifndef PORT_CFG_H_
#define PORT_CFG_H_

#include "Std_Types.h"


#define	PORT_VERSION_INFO_API				STD_OFF    //correspond to the function in port.c is not executed
#define	PORT_DEV_ERROR_DETECT				STD_OFF
#define PORT_SET_PIN_DIRECTION_API	        STD_OFF
#define PORT_SET_PIN_MODE_API				STD_OFF

typedef struct {
	volatile uint32_t *gipo_select0;
	volatile uint32_t *gipo_clear0;
	volatile uint32_t *gipo_level0;
	volatile uint32_t *gipo_pudclk0;

	volatile uint32_t *gipo_select1;
	volatile uint32_t *gipo_clear1;
	volatile uint32_t *gipo_level1;
	volatile uint32_t *gipo_pudclk1;
	unsigned int gipo_pin_base;
}Port_ConfigType;


/** Instance of the top level configuration container */
extern const Port_ConfigType PortConfigData;

#endif /* PORT_CFG_H_ */
