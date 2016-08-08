/**
 * RTE Header File
 *
 * @req SWS_Rte_01157
 */

#ifndef RTE_H_
#define RTE_H_

/** --- Includes ----------------------------------------------------------------------------------
 *  @req SWS_Rte_01164
 */
#include <Std_Types.h>

#include <Rte_Cfg.h>

#include <string.h>

/** --- Versions ----------------------------------------------------------------------------------
 */
#define RTE_AR_RELEASE_MAJOR_VERSION	4
#define RTE_AR_RELEASE_MINOR_VERSION	0

/** === ERROR CODES ===============================================================================
 */

/** --- Std_ReturnType bits -----------------------------------------------------------------------
 *
 *  LSB           MSB
 *   0 1 2 3 4 5 6 7
 *  |    A    |B|C|D|
 *
 *   A : 6 bits available for error codes
 *   B : Overlayed Error Flag
 *   C : Error Flag
 *   D : Immediate Infrastructure
 */

/** @req SWS_Rte_07404 */
#define Rte_IsInfrastructureError(status) ((status & 128U) !=0)

/** @req SWS_Rte_07405 */
#define Rte_HasOverlayedError(status) ((status & 64U) != 0)

/** @req SWS_Rte_07406 */
#define Rte_ApplicationError(status) (status & 63U)

/** --- Error values ------------------------------------------------------------------------------
 *  @req SWS_Rte_01269
 */

/** No error occurred.
 *  @req SWS_Rte_01058 */
#define RTE_E_OK                          0

/** --- Standard Application Error Values: */

/** Generic application error indicated by signal invalidation in sender receiver
 *  communication with data semantics on the receiver side.
 *  @req SWS_Rte_02594
 *
 *  NOTE: Value '1' also used as:
 *    Symbolic name: To be defined by the corresponding AUTOSAR Service
 *    Comments:      Returned by AUTOSAR Services to indicate a generic application error.
 */
#define RTE_E_INVALID	                 1

/** An IPDU group was disabled while the application was waiting for the transmission acknowledgment.
 *  No value is available. This is not considered a fault, since the IPDU group is switched off on purpose.
 *  This semantics are as follows:
 *  - The OUT buffers of a client or of explicit read APIs are not modified
 *  - no runnable with startOnEvent on a DataReceivedEvent for this VariableDataPrototype is triggered.
 *  - the buffers for implicit read access will keep the previous value.
 *  @req SWS_Rte_01060 */
#define RTE_E_COM_STOPPED              128

/** A blocking API call returned due to expiry of a local timeout rather than the intended result.
 *  OUT buffers are not modified. The interpretation of this being an error depends on the application.
 *  @req SWS_Rte_01064 */
#define RTE_E_TIMEOUT                  129

/** A internal RTE limit has been exceeded. Request could not be handled.
 *  OUT buffers are not modified.
 *  @req SWS_Rte_01317 */
#define RTE_E_LIMIT 130

/** An explicit read API call returned no data. (This is no error.)
 *  @req SWS_Rte_01061 */
#define RTE_E_NO_DATA                  131

/** Transmission acknowledgement received.
 *  @req SWS_Rte_01065 */
#define RTE_E_TRANSMIT_ACK             132

/** No data received for the corresponding unqueued data element since system start or partition restart.
 *  @req SWS_Rte_07384 */
#define RTE_E_NEVER_RECEIVED           133

/** The port used for communication is not connected.
 *  @req SWS_Rte_07655 */
#define RTE_E_UNCONNECTED              134

/** The error is returned by a blocking API and indicates that the runnable could not enter a wait state,
 *  because one ExecutableEntity of the current task's call stack has entered or is running in an ExclusiveArea.
 *  @req SWS_Rte_02739 */
#define RTE_E_IN_EXCLUSIVE_AREA        135

/** The error can be returned by an RTE API, if the parameters contain a direct or indirect
 *  reference to memory that is not accessible from the callers partition.
 *  @req SWS_Rte_02757 */
#define RTE_E_SEG_FAULT                136

/** The received data is out of range.
 *  @req SWS_Rte_08065 */
#define RTE_E_OUT_OF_RANGE             137

/** An error during serialization or deserialization occurred.
 *  @req SWS_Rte_08725 */
#define RTE_E_SERIALIZATION_ERROR      138

/** Buffer for serialization operation could not be created.
 *  @req SWS_Rte_08726 */
#define RTE_E_SERIALIZATION_LIMIT      139

/** --- Overlayed Errors
 *  These errors do not refer to the data returned with the API. They can be overlayed
 *  with other Application- or Immediate Infrastructure Errors. */

/** An API call for reading received data with event semantics indicates that some incoming data
 *  has been lost due to an overflow of the receive queue or due to an error of the underlying
 *  communication stack.
 *  @req SWS_Rte_02571 */
#define RTE_E_LOST_DATA                 64

/** An API call for reading received data withdata semantics indicates that the available data
 *  has exceeded the aliveTimeout limit. A COM signal outdated callback will result in this error.
 *  @req SWS_Rte_02702 */
#define RTE_E_MAX_AGE_EXCEEDED          64

#endif /* RTE_H_ */
