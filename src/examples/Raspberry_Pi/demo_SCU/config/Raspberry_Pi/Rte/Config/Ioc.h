/**
 * IOC Header File
 */

#include <Rte_Type.h>

#define IOC_E_OK           RTE_E_OK
#define IOC_E_NOK          RTE_E_NOK
#define IOC_E_LIMIT        RTE_E_LIMIT
#define IOC_E_LOST_DATA    RTE_E_LOST_DATA
#define IOC_E_NO_DATA      RTE_E_NO_DATA

#ifndef IOC_H_
#define IOC_H_

typedef struct {
    uint8 _dummy;
} IocBuffersType;

void IocInit(void);

#endif /* IOC_H_ */
