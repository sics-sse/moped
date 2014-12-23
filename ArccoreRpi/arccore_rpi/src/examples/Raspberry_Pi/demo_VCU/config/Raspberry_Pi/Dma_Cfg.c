/*
 * Dma_Cfg.c
 *
 *  Created on: Sep 16, 2013
 *      Author: Zhang Shuzhou
 */


#include "Dma.h"


const Dma_ChannelConfigType DmaChannelConfig [DMA_NUMBER_OF_CHANNELS] =
{
	{
		.DMA_CHANNEL_PRIORITY = DMA_CHANNEL0, .DMA_CHANNEL_PREEMTION_ENABLE = 0x01
	},

	{
		.DMA_CHANNEL_PRIORITY = DMA_CHANNEL1, .DMA_CHANNEL_PREEMTION_ENABLE = 0x02
	},

//	{
//		.DMA_CHANNEL_PRIORITY = DMA_CHANNEL2, .DMA_CHANNEL_PREEMTION_ENABLE = 0x04
//	},
//
//	{
//		.DMA_CHANNEL_PRIORITY = DMA_CHANNEL3, .DMA_CHANNEL_PREEMTION_ENABLE = 0x08
//	}

};


const Dma_ConfigType DmaConfig []=
{
  {DmaChannelConfig, DMA_FIXED_PRIORITY_ARBITRATION}
};

