
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
#define ALARM_ID_CanFuntionAlarm	((AlarmType)0)
#define ALARM_ID_SensorAlarm	((AlarmType)1)
#define ALARM_ID_RteAlarm	((AlarmType)2)


// Counter Id's
#define COUNTER_ID_OsCounter	((CounterType)0)


// System counter
#define OSMAXALLOWEDVALUE		UINT_MAX// NOT CONFIGURABLE IN TOOLS
#define OSTICKSPERBASE			1u       // NOT CONFIGURABLE IN TOOLS
#define OSMINCYCLE				1u		// NOT CONFIGURABLE IN TOOLS
#define OSTICKDURATION			10000000UL    // Time between ticks in nano seconds

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
#define EVENT_MASK_SensorEvent	 (EventMaskType)((EventMaskType)1u<<1)
#define EVENT_MASK_RteEvent	 (EventMaskType)((EventMaskType)1u<<2)
#define EVENT_MASK_IMUEvent	 (EventMaskType)((EventMaskType)1u<<3)
#define EVENT_MASK_UltraEvent	 (EventMaskType)((EventMaskType)1u<<4)
#define EVENT_MASK_SCUInstallationEvent	 (EventMaskType)((EventMaskType)1u<<5)
#define EVENT_MASK_VCUCommunicationEvent	 (EventMaskType)((EventMaskType)1u<<6)
#define EVENT_MASK_TCUCommunicationEvent	 (EventMaskType)((EventMaskType)1u<<7)

// Isr Id's


// Resource Id's



// Linked resource id's


// Resource masks



// Task Id's
#define TASK_ID_OsIdle ((TaskType)0)



#define TASK_ID_CanFunctionTask	((TaskType)1)
#define TASK_ID_RteTask	((TaskType)2)
#define TASK_ID_SensorTask	((TaskType)3)
#define TASK_ID_SquawkTask	((TaskType)4)
#define TASK_ID_StartupTask	((TaskType)5)


// Task entry points

void OsIdle( void );




void CanFunctionTask ( void );
void RteTask ( void );
void SensorTask ( void );
void SquawkTask ( void );
void StartupTask ( void );


// Schedule table id's


// Spinlock ids

#define OS_SPINLOCK			0
#define OS_RTE_SPINLOCK		1

// Stack size
#define OS_INTERRUPT_STACK_SIZE	2048u
#define OS_OSIDLE_STACK_SIZE 512u 

#define OS_ALARM_CNT			3u 
#define OS_TASK_CNT				6u
#define OS_SCHTBL_CNT			0u
#define OS_COUNTER_CNT			1u
#define OS_EVENTS_CNT			8u
//#define OS_ISRS_CNT			0u
#define OS_RESOURCE_CNT			0u
#define OS_LINKED_RESOURCE_CNT	0u

#define OS_APPLICATION_CNT		1u

#define OS_SPINLOCK_CNT			2

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

