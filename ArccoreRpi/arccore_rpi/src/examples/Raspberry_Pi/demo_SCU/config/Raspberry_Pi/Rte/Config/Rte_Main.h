/**
 * Lifecycle Header File
 *
 * @req SWS_Rte_01158
 */

#ifndef RTE_MAIN_H_
#define RTE_MAIN_H_

/** @req SWS_Rte_02569 @req SWS_Rte_01309 @req SWS_Rte_02585 @req SWS_Rte_01261 @req SWS_Rte_01262 */
Std_ReturnType Rte_Start(void);

/** @req SWS_Rte_02570 @req SWS_Rte_01310 @req SWS_Rte_02584 @req SWS_Rte_01259 @req SWS_Rte_01260 */
Std_ReturnType Rte_Stop(void);

/*
 5.8.6 Rte_Init
 Purpose: SchedulesRunnableEntitys for initialization purpose.
 Signature: [SWS_Rte_06749] d
 void Rte_Init_<InitContainer>(void)
 Where<InitContainer>is the short name of the RteInitial-izationRunnableBatchcontainer.c(SRS_Rte_00240)
 Existence: [SWS_Rte_06750] d An Rte_Init API shall be created
 for each RteInitializationRunnableBatch container.
 c(SRS_Rte_00240)

 5.8.7 Rte_StartTiming
 Purpose: Starts the triggering of recurrent events.
 Signature: [SWS_Rte_06754] d
 void Rte_StartTiming(void)
 c(SRS_Rte_00240)
 Existence: [SWS_Rte_06755] dAnRte_StartTimingAPI shall be created if
 anyRte_InitAPI is created.c(SRS_Rte_00240)
 */

#endif /* RTE_MAIN_H_ */
