


#include "kernel.h"


// ###############################    EXTERNAL REFERENCES    #############################
 
/* Application externals */




/* Interrupt externals */

// Set the os tick frequency
OsTickType OsTickFreq = 1000;


// ###############################    DEBUG OUTPUT     #############################
uint32 os_dbg_mask =  0;                     


// ###############################    APPLICATIONS     #############################
GEN_APPLICATION_HEAD = {

	GEN_APPLICATION(
				/* id           */ APPLICATION_ID_OsApplication,
				/* name         */ "OsApplication",
				/* trusted      */ TRUE, /* NOT CONFIGURABLE IN TOOLS */
				/* core         */ 0,
				/* StartupHook  */ NULL,
				/* ShutdownHook */ NULL,
				/* ErrorHook    */ NULL,
				/* rstrtTaskId  */ 0 // NOT CONFIGURABLE IN TOOLS (OsTasks.indexOf(app.Os RestartTask.value))
				)

};

// #################################    COUNTERS     ###############################

GEN_COUNTER_HEAD = {
	GEN_COUNTER(
				/* id          */		COUNTER_ID_OsCounter,
				/* name        */		"OsCounter",
				/* counterType */		COUNTER_TYPE_HARD,
				/* counterUnit */		COUNTER_UNIT_NANO,
				/* maxAllowed  */		0xffff,
				/*             */		1,
				/* minCycle    */		1,
				/*             */		0,
				/* owningApp   */		APPLICATION_ID_OsApplication,
				/* accAppMask..*/       (1 << APPLICATION_ID_OsApplication)
 ) 
};




CounterType Os_Arc_OsTickCounter = COUNTER_ID_OsCounter;




// ##################################    ALARMS     ################################
GEN_ALARM_AUTOSTART(
				ALARM_ID_CanFunctionAlarm,
				ALARM_AUTOSTART_ABSOLUTE,
				5,
				5,
				OSDEFAULTAPPMODE );

GEN_ALARM_AUTOSTART(
				ALARM_ID_RteFunctionAlarm,
				ALARM_AUTOSTART_ABSOLUTE,
				20,
				20,
				OSDEFAULTAPPMODE );

GEN_ALARM_AUTOSTART(
				ALARM_ID_SensorAlarm,
				ALARM_AUTOSTART_ABSOLUTE,
				500,
				500,
				OSDEFAULTAPPMODE );

GEN_ALARM_AUTOSTART(
				ALARM_ID_SquawkAlarm,
				ALARM_AUTOSTART_ABSOLUTE,
				20,
				20,
				OSDEFAULTAPPMODE );




GEN_ALARM_HEAD = {

	GEN_ALARM(	ALARM_ID_CanFunctionAlarm,
				"CanFunctionAlarm",
				COUNTER_ID_OsCounter,
				GEN_ALARM_AUTOSTART_NAME(ALARM_ID_CanFunctionAlarm),
				ALARM_ACTION_SETEVENT,
				TASK_ID_CanFunctionTask,
				EVENT_MASK_CanFunctionEvent,
				0,
				APPLICATION_ID_OsApplication, /* Application owner */
				( 1 << APPLICATION_ID_OsApplication)
 /* Accessing application mask */
			)
,
	GEN_ALARM(	ALARM_ID_RteFunctionAlarm,
				"RteFunctionAlarm",
				COUNTER_ID_OsCounter,
				GEN_ALARM_AUTOSTART_NAME(ALARM_ID_RteFunctionAlarm),
				ALARM_ACTION_SETEVENT,
				TASK_ID_RteTask,
				EVENT_MASK_RteFunctionEvent,
				0,
				APPLICATION_ID_OsApplication, /* Application owner */
				( 1 << APPLICATION_ID_OsApplication)
 /* Accessing application mask */
			)
,
	GEN_ALARM(	ALARM_ID_SensorAlarm,
				"SensorAlarm",
				COUNTER_ID_OsCounter,
				GEN_ALARM_AUTOSTART_NAME(ALARM_ID_SensorAlarm),
				ALARM_ACTION_SETEVENT,
				TASK_ID_SensorTask,
				EVENT_MASK_SensorEvent,
				0,
				APPLICATION_ID_OsApplication, /* Application owner */
				( 1 << APPLICATION_ID_OsApplication)
 /* Accessing application mask */
			)
,
	GEN_ALARM(	ALARM_ID_SquawkAlarm,
				"SquawkAlarm",
				COUNTER_ID_OsCounter,
				GEN_ALARM_AUTOSTART_NAME(ALARM_ID_SquawkAlarm),
				ALARM_ACTION_SETEVENT,
				TASK_ID_SquawkTask,
				EVENT_MASK_SquawkEvent,
				0,
				APPLICATION_ID_OsApplication, /* Application owner */
				( 1 << APPLICATION_ID_OsApplication)
 /* Accessing application mask */
			)

};

 
// ################################    RESOURCES     ###############################


// ##############################    STACKS (TASKS)     ############################

DECLARE_STACK(OsIdle, OS_OSIDLE_STACK_SIZE);


DECLARE_STACK(ActuatorTask,  2048);
DECLARE_STACK(CanFunctionTask,  2048);
DECLARE_STACK(RteTask,  2048);
DECLARE_STACK(SensorTask,  2048);
DECLARE_STACK(SquawkTask,  10485760);
DECLARE_STACK(StartupTask,  2048);


// ##################################    TASKS     #################################
GEN_TASK_HEAD = {
	
	GEN_BTASK(	/* 	        		*/ OsIdle,
				/* name        		*/ "OsIdle",
				/* priority    		*/ 0,
				/* schedule    		*/ FULL,
				/* autostart   		*/ TRUE,
				/* resource_int_p   */ NULL,
				/* resource mask	*/ 0,
				/* activation lim. 	*/ 1,
				/* App owner    	*/ OS_CORE_0_MAIN_APPLICATION,
				/* Accessing apps   */ (1 << OS_CORE_0_MAIN_APPLICATION)
	),
	



	GEN_ETASK(
		/* 	        		*/ ActuatorTask,
		/* name        		*/ "ActuatorTask",
		/* priority    		*/ 6,
		/* schedule    		*/ FULL,
		/* autostart   		*/ TRUE,
		/* resource_int_p   */ NULL,
		/* resource mask	*/ 0 ,
		/* event mask */       0 | EVENT_MASK_SpeedEvent | EVENT_MASK_ServoEvent | EVENT_MASK_LedEvent | EVENT_MASK_SpeedSelectEvent | EVENT_MASK_SteerSelectEvent ,
		/* App owner    	*/ APPLICATION_ID_OsApplication,
		/* Accessing apps   */ (1 <<APPLICATION_ID_OsApplication)
	),			


	GEN_ETASK(
		/* 	        		*/ CanFunctionTask,
		/* name        		*/ "CanFunctionTask",
		/* priority    		*/ 7,
		/* schedule    		*/ FULL,
		/* autostart   		*/ TRUE,
		/* resource_int_p   */ NULL,
		/* resource mask	*/ 0 ,
		/* event mask */       0 | EVENT_MASK_CanFunctionEvent ,
		/* App owner    	*/ APPLICATION_ID_OsApplication,
		/* Accessing apps   */ (1 <<APPLICATION_ID_OsApplication)
	),			


	GEN_ETASK(
		/* 	        		*/ RteTask,
		/* name        		*/ "RteTask",
		/* priority    		*/ 4,
		/* schedule    		*/ FULL,
		/* autostart   		*/ TRUE,
		/* resource_int_p   */ NULL,
		/* resource mask	*/ 0 ,
		/* event mask */       0 | EVENT_MASK_RteFunctionEvent | EVENT_MASK_AdcEvent | EVENT_MASK_FrontWheelEvent | EVENT_MASK_RearWheelEvent | EVENT_MASK_pluginInstallationEvent | EVENT_MASK_pluginCommunicationSCUEvent | EVENT_MASK_pluginCommunicationTCUEvent | EVENT_MASK_positionDataEvent | EVENT_MASK_SpeedSteerEvent ,
		/* App owner    	*/ APPLICATION_ID_OsApplication,
		/* Accessing apps   */ (1 <<APPLICATION_ID_OsApplication)
	),			


	GEN_ETASK(
		/* 	        		*/ SensorTask,
		/* name        		*/ "SensorTask",
		/* priority    		*/ 3,
		/* schedule    		*/ FULL,
		/* autostart   		*/ TRUE,
		/* resource_int_p   */ NULL,
		/* resource mask	*/ 0 ,
		/* event mask */       0 | EVENT_MASK_SensorEvent ,
		/* App owner    	*/ APPLICATION_ID_OsApplication,
		/* Accessing apps   */ (1 <<APPLICATION_ID_OsApplication)
	),			


	GEN_ETASK(
		/* 	        		*/ SquawkTask,
		/* name        		*/ "SquawkTask",
		/* priority    		*/ 2,
		/* schedule    		*/ FULL,
		/* autostart   		*/ TRUE,
		/* resource_int_p   */ NULL,
		/* resource mask	*/ 0 ,
		/* event mask */       0 | EVENT_MASK_SquawkEvent ,
		/* App owner    	*/ APPLICATION_ID_OsApplication,
		/* Accessing apps   */ (1 <<APPLICATION_ID_OsApplication)
	),			


	GEN_BTASK(
		/* 	        		*/ StartupTask,
		/* name        		*/ "StartupTask",
		/* priority    		*/ 8,
		/* schedule    		*/ FULL,
		/* autostart   		*/ TRUE,
		/* resource_int_p   */ NULL,
		/* resource mask	*/ 0 
,
		/* activation lim. 	*/ 1,
		/* App owner    	*/ APPLICATION_ID_OsApplication,
		/* Accessing apps   */ (1 <<APPLICATION_ID_OsApplication)
	),			

};

// ##################################    HOOKS     #################################
GEN_HOOKS( 
	StartupHook, 
	NULL,
	ShutdownHook, 
 	ErrorHook,
 	NULL,
 	NULL 
);

// ##################################    ISRS     ##################################



GEN_ISR_MAP = {
  0
};

// ############################    SCHEDULE TABLES     #############################


 
 // ############################    SPINLOCKS     ##################################



