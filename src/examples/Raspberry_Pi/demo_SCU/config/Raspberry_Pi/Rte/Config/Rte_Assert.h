/**
 * Rte Runtime Assert
 *
 */

#ifndef RTE_ASSERT_H_
#define RTE_ASSERT_H_

#include <Det.h>
#include <Os.h>
#include <Modules.h>

#define RTE_E_DET_ILLEGAL_SIGNAL_ID                            (0x01) /** @req SWS_Rte_06633 */
#define RTE_E_DET_ILLEGAL_VARIANT_CRITERION_VALUE              (0x02) /** @req SWS_Rte_06634 @req SWS_Rte_07684 */
#define RTE_E_DET_ILLEGAL_INVOCATION                           (0x03) /** @req SWS_Rte_06635 */
#define RTE_E_DET_WAIT_IN_EXCLUSIVE_AREA                       (0x04) /** @req SWS_Rte_06637 */
#define RTE_E_DET_ILLEGAL_NESTED_EXCLUSIVE_AREA                (0x05) /** @req SWS_Rte_07675 */
#define RTE_E_DET_SEG_FAULT                                    (0x06) /** @req SWS_Rte_07685 */
#define RTE_E_DET_UNINIT                                       (0x07) /** @req SWS_Rte_07682 @req SWS_Rte_07683*/

/** @req SWS_Rte_06632 */
#define RTE_SERVICEID_API_PORTS                                (0x10)
#define RTE_SERVICEID_API_NPORTS                               (0x11)
#define RTE_SERVICEID_API_PORT                                 (0x12)
#define RTE_SERVICEID_API_SEND                                 (0x13)
#define RTE_SERVICEID_API_WRITE                                (0x14)
#define RTE_SERVICEID_API_SWITCH                               (0x15)
#define RTE_SERVICEID_API_INVALIDATE                           (0x16)
#define RTE_SERVICEID_API_FEEDBACK                             (0x17)
#define RTE_SERVICEID_API_SWITCHACK                            (0x18)
#define RTE_SERVICEID_API_READ                                 (0x19)
#define RTE_SERVICEID_API_DREAD                                (0x1A)
#define RTE_SERVICEID_API_RECEIVE                              (0x1B)
#define RTE_SERVICEID_API_CALL                                 (0x1C)
#define RTE_SERVICEID_API_RESULT                               (0x1D)
#define RTE_SERVICEID_API_PIM                                  (0x1E)
#define RTE_SERVICEID_API_CDATA                                (0x1F)
#define RTE_SERVICEID_API_PRM                                  (0x20)
#define RTE_SERVICEID_API_IREAD                                (0x21)
#define RTE_SERVICEID_API_IWRITE                               (0x22)
#define RTE_SERVICEID_API_IWRITEREF                            (0x23)
#define RTE_SERVICEID_API_IINVALIDATE                          (0x24)
#define RTE_SERVICEID_API_ISTATUS                              (0x25)
#define RTE_SERVICEID_API_IRVIREAD                             (0x26)
#define RTE_SERVICEID_API_IRVIWRITE                            (0x27)
#define RTE_SERVICEID_API_IRVREAD                              (0x28)
#define RTE_SERVICEID_API_IRVWRITE                             (0x29)
#define RTE_SERVICEID_API_ENTER                                (0x2A)
#define RTE_SERVICEID_API_EXIT                                 (0x2B)
#define RTE_SERVICEID_API_MODE                                 (0x2C)
#define RTE_SERVICEID_API_TRIGGER                              (0x2D)
#define RTE_SERVICEID_API_IRTRIGGER                            (0x2E)
#define RTE_SERVICEID_API_IFEEDBACK                            (0x2F)
#define RTE_SERVICEID_API_ISUPDATED                            (0x30)

#define RTE_SERVICEID_EVENT_TIMINGEVENT                        (0x50)
#define RTE_SERVICEID_EVENT_BACKGROUNDEVENT                    (0x51)
#define RTE_SERVICEID_EVENT_SWCMODESWITCHEVENT                 (0x52)
#define RTE_SERVICEID_EVENT_ASYNCHRONOUSSERVERCALLRETURNSEVENT (0x53)
#define RTE_SERVICEID_EVENT_DATARECEIVEERROREVENT              (0x54)
#define RTE_SERVICEID_EVENT_OPERATIONINVOKEDEVENT              (0x55)
#define RTE_SERVICEID_EVENT_DATARECEIVEDEVENT                  (0x56)
#define RTE_SERVICEID_EVENT_DATASENDCOMPLETEDEVENT             (0x57)
#define RTE_SERVICEID_EVENT_EXTERNALTRIGGEROCCURREDEVENT       (0x58)
#define RTE_SERVICEID_EVENT_INTERNALTRIGGEROCCURREDEVENT       (0x59)
#define RTE_SERVICEID_EVENT_DATAWRITECOMPLETEDEVENT            (0x5A)

#define RTE_SERVICEID_API_START                                (0x70)
#define RTE_SERVICEID_API_STOP                                 (0x71)
#define RTE_SERVICEID_API_PARTITIONTERMINATED                  (0x72)
#define RTE_SERVICEID_API_PARTITIONRESTARTING                  (0x73)
#define RTE_SERVICEID_API_RESTARTPARTITION                     (0x74)
#define RTE_SERVICEID_API_INIT                                 (0x75)
#define RTE_SERVICEID_API_STARTTIMING                          (0x76)

#define RTE_SERVICEID_CBK_COMCBKTACK_SIGNAL                    (0x90)
#define RTE_SERVICEID_CBK_COMCBKTERR_SIGNAL                    (0x91)
#define RTE_SERVICEID_CBK_COMCBKINV_SIGNAL                     (0x92)
#define RTE_SERVICEID_CBK_COMCBKRXTOUT_SIGNAL                  (0x93)
#define RTE_SERVICEID_CBK_COMCBKTXTOUT_SIGNAL                  (0x94)
#define RTE_SERVICEID_CBK_COMCBK_GROUP                         (0x95)
#define RTE_SERVICEID_CBK_COMCBKTACK_GROUP                     (0x96)
#define RTE_SERVICEID_CBK_COMCBKTERR_GROUP                     (0x97)
#define RTE_SERVICEID_CBK_COMCBKINV_GROUP                      (0x98)
#define RTE_SERVICEID_CBK_COMCBKRXTOUT_GROUP                   (0x99)
#define RTE_SERVICEID_CBK_COMCBKTXTOUT_GROUP                   (0x9A)
#define RTE_SERVICEID_CBK_SETMIRROR                            (0x9B)
#define RTE_SERVICEID_CBK_GETMIRROR                            (0x9C)
#define RTE_SERVICEID_CBK_NVMNOTIFYJOBFINISHED                 (0x9D)
#define RTE_SERVICEID_CBK_NVMNOTIFYINITBLOCK                   (0x9E)

#define SCHM_SERVICEID_API_INIT                                (0x00)
#define SCHM_SERVICEID_API_DEINIT                              (0x01)
#define SCHM_SERVICEID_API_GETVERSIONINFO                      (0x02)
#define SCHM_SERVICEID_API_ENTER                               (0x03)
#define SCHM_SERVICEID_API_EXIT                                (0x04)
#define SCHM_SERVICEID_API_ACTMAINFUNCTION                     (0x05)
#define SCHM_SERVICEID_API_SWITCH                              (0x06)
#define SCHM_SERVICEID_API_MODE                                (0x07)
#define SCHM_SERVICEID_API_SWITCHACK                           (0x08)
#define SCHM_SERVICEID_API_TRIGGER                             (0x09)

#define Rte_Assert_Enter(cond)			Rte_Assert(cond, RTE_SERVICEID_API_ENTER, RTE_E_DET_ILLEGAL_NESTED_EXCLUSIVE_AREA)

/** @req SWS_Rte_06630
 *  @req SWS_Rte_07676
 *  @req SWS_Rte_06632
 *  !req SWS_Rte_06631 */
#if (RTE_DEV_ERROR_DETECT == STD_ON)

#define Rte_Assert(cond, service, error) \
		do { \
			if (!(cond)) { \
				Det_ReportError( MODULE_ID_RTE, 0, service, error ); \
				ShutdownOS( E_OS_RTE ); \
			} \
		} while(0)

#else

#define Rte_Assert(cond, service, error)

#endif

#endif /* RTE_ASSERT_H_ */

