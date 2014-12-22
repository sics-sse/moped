
#include <Rte_Internal.h>

extern RPortStatusesType RPortStatuses;
extern ModeMachinesType ModeMachines;
extern boolean RteInitialized;

/** === Lifecycle API =============================================================================
 */
Std_ReturnType Rte_Start(void) {
    // Initialize port statuses

    // Initialize mode machines

    RteInitialized = true;
    return RTE_E_OK;
}

Std_ReturnType Rte_Stop(void) {
    RteInitialized = false;
    return RTE_E_OK;
}

