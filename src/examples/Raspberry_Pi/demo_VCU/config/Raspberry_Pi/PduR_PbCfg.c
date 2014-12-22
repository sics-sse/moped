
#include "PduR.h"
#include "MemMap.h"

 
SECTION_POSTBUILD_DATA const PduR_PBConfigType PduR_Config = {
	.PduRConfigurationId = 0,
	.RoutingPaths = NULL,
	.NRoutingPaths = 0,
	.DefaultValues = NULL,
	.DefaultValueLengths = NULL,
};


