
#ifndef OS_CFG_H_
#define OS_CFG_H_

#if !(((OS_SW_MAJOR_VERSION == 1) && (OS_SW_MINOR_VERSION == 0)) )
#error OS: Configuration file expected OS module version to be 1.0.X*
#endif

#if !(((OS_AR_RELEASE_MAJOR_VERSION == 4) && (OS_AR_RELEASE_MINOR_VERSION == 0)) )
#error OS: Configuration file expected AUTOSAR version to be 4.0.*
#endif

// Application Id's
#define APPLICATION_ID_OsApplication 0u


// Alarm Id's
#define ALARM_ID_CanFunctionAlarm	((AlarmType)0)
#define ALARM_ID_RteFunctionAlarm	((AlarmType)1)
#define ALARM_ID_SensorAlarm	((AlarmType)2)
#define ALARM_ID_SquawkAlarm	((AlarmType)3)


// Counter Id's
#define COUNTER_ID_OsCounter	((CounterType)0)


// System counter
#define OSMAXALLOWEDVALUE		UINT_MAX// NOT CONFIGURABLE IN TOOLS
#define OSTICKSPERBASE			1u       // NOT CONFIGURABLE IN TOOLS
#define OSMINCYCLE				1u		// NOT CONFIGURABLE IN TOOLS
#define OSTICKDURATION			1000000UL    // Time between ticks in nano seconds

// Counter macros
#define OSMAXALLOWEDVALUE_OsCounter		OSMAXALLOWEDVALUE
#define OSTICKSPERBASE_OsCounter			1u // NOT CONFIGURABLE IN TOOLS (counter.Os CounterTicksPerBase.value)
#define OSMINCYCLE_OsCounter				1u
#define OS_TICKS2SEC_OsCounter(_ticks)		( (OSTICKDURATION * _ticks)/1000000000UL )
#define OS_TICKS2MS_OsCounter(_ticks)		( (OSTICKDURATION * _ticks)/1000000UL )
#define OS_TICKS2US_OsCounter(_ticks)		( (OSTICKDURATION * _ticks)/1000UL )
#define OS_TICKS2NS_OsCounter(_ticks)		(OSTICKDURATION * _ticks)



// Event masks
#define EVENT_MASK_CanFunctionEvent	 (EventMaskType)((EventMaskType)1u<<0)
#define EVENT_MASK_RteFunctionEvent	 (EventMaskType)((EventMaskType)1u<<1)
#define EVENT_MASK_SpeedEvent	 (EventMaskType)((EventMaskType)1u<<2)
#define EVENT_MASK_SensorEvent	 (EventMaskType)((EventMaskType)1u<<3)
#define EVENT_MASK_ServoEvent	 (EventMaskType)((EventMaskType)1u<<4)
#define EVENT_MASK_AdcEvent	 (EventMaskType)((EventMaskType)1u<<5)
#define EVENT_MASK_FrontWheelEvent	 (EventMaskType)((EventMaskType)1u<<6)
#define EVENT_MASK_RearWheelEvent	 (EventMaskType)((EventMaskType)1u<<7)
#define EVENT_MASK_pluginInstallationEvent	 (EventMaskType)((EventMaskType)1u<<8)
#define EVENT_MASK_pluginacknowledgeVCUEvent	 (EventMaskType)((EventMaskType)1u<<9)
#define EVENT_MASK_SquawkEvent	 (EventMaskType)((EventMaskType)1u<<10)
#define EVENT_MASK_pluginCommunicationSCUEvent	 (EventMaskType)((EventMaskType)1u<<11)
#define EVENT_MASK_pluginCommunicationTCUEvent	 (EventMaskType)((EventMaskType)1u<<12)
#define EVENT_MASK_positionDataEvent	 (EventMaskType)((EventMaskType)1u<<13)
#define EVENT_MASK_LedEvent	 (EventMaskType)((EventMaskType)1u<<14)
#define EVENT_MASK_SpeedSelectEvent	 (EventMaskType)((EventMaskType)1u<<15)
#define EVENT_MASK_SteerSelectEvent	 (EventMaskType)((EventMaskType)1u<<16)
#define EVENT_MASK_SpeedSteerEvent	 (EventMaskType)((EventMaskType)1u<<17)

// Isr Id's


// Resource Id's



// Linked resource id's


// Resource masks



// Task Id's
#define TASK_ID_OsIdle ((TaskType)0)



#define TASK_ID_ActuatorTask	((TaskType)1)
#define TASK_ID_CanFunctionTask	((TaskType)2)
#define TASK_ID_RteTask	((TaskType)3)
#define TASK_ID_SensorTask	((TaskType)4)
#define TASK_ID_SquawkTask	((TaskType)5)
#define TASK_ID_StartupTask	((TaskType)6)


// Task entry points

void OsIdle( void );




void ActuatorTask ( void );
void CanFunctionTask ( void );
void RteTask ( void );
void SensorTask ( void );
void SquawkTask ( void );
void StartupTask ( void );


// Schedule table id's


// Spinlock ids
#define SPINLOCK_ID_OsSpinlock  0

#define OS_SPINLOCK			1
#define OS_RTE_SPINLOCK		2

// Stack size
#define OS_INTERRUPT_STACK_SIZE	2048u
#define OS_OSIDLE_STACK_SIZE 512u 

#define OS_ALARM_CNT			4u 
#define OS_TASK_CNT				7u
#define OS_SCHTBL_CNT			0u
#define OS_COUNTER_CNT			1u
#define OS_EVENTS_CNT			18u
//#define OS_ISRS_CNT			0u
#define OS_RESOURCE_CNT			0u
#define OS_LINKED_RESOURCE_CNT	0u

#define OS_APPLICATION_CNT		1u

#define OS_SPINLOCK_CNT			3

#define OS_SERVICE_CNT			0u  /* ARCTICSTUDIO_GENERATOR_TODO */

#define CFG_OS_DEBUG			STD_OFF


#define OS_SC1 						STD_ON


#define OS_USE_APPLICATIONS			STD_ON
#define OS_USE_MEMORY_PROT			STD_OFF	/* NOT CONFIGURABLE IN TOOLS */
#define OS_USE_TASK_TIMING_PROT		STD_OFF	/* NOT CONFIGURABLE IN TOOLS */
#define OS_USE_ISR_TIMING_PROT		STD_OFF	/* NOT CONFIGURABLE IN TOOLS */
#define OS_STACK_MONITORING			STD_ON


#define OS_STATUS_EXTENDED			STD_OFF


#define OS_USE_GET_SERVICE_ID		STD_ON	/* NOT CONFIGURABLE IN TOOLS */
#define OS_USE_PARAMETER_ACCESS		STD_ON	/* NOT CONFIGURABLE IN TOOLS */
#define OS_RES_SCHEDULER			STD_ON	/* NOT CONFIGURABLE IN TOOLS */

#define OS_ISR_CNT			0u
#define OS_ISR2_CNT 		0u
#define OS_ISR1_CNT			0u

#define OS_ISR_MAX_CNT		10u

#define OS_NUM_CORES		1u

#define OS_CORE_0_MAIN_APPLICATION	APPLICATION_ID_OsApplication
 

#endif /*OS_CFG_H_*/

