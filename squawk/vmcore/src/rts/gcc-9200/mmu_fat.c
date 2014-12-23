/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

#include "cache.h"
#include "system.h"
#include "flash.h"
#include "mmu_fat.h"
/*
 * This file contains routines that query the contents of the FlashFile FAT
 * and set up the MMU to match the virtual file settings in the FAT.
 *
 * To understand this file you will need an external source that explains the
 * ARM9 MMU. We used and recommend "ARM System Developer's Guide" by Sloss, Symes
 * and Wright.
 */


// FAT V2 constants - must match the same constants in FATRecord.java
#define FILE_FAT_RECORD_TYPE				0
#define UNUSED_FAT_RECORD_STATUS			0xFFFF
#define DELETED_FAT_RECORD_STATUS			0x0000
#define FAT_RECORD_HEADER_SIZE				4
#define CURRENT_FAT_RECORD_STATUS			0x00FF

// MMU mapping tables
#define LEVEL_2_TABLE_ENTRIES				(256)
#define LEVEL_2_TABLE_SIZE					(sizeof(int)*LEVEL_2_TABLE_ENTRIES)

#define LEVEL_1_PAGE_TABLE_SIZE				(16 * 1024)
#define LEVEL_2_PAGE_TABLE_SIZE				(LEVEL_2_TABLE_SIZE * VIRTUAL_ADDRESS_FILE_COUNT)

// The page tables must fit inside the MMU_SPACE (see system.h)
#define PAGE_TABLE_SIZE 					(LEVEL_2_PAGE_TABLE_SIZE)

// This must match the constant in FlashFileDescriptor.java
#define OBSOLETE_FLAG_MASK					0x1

#define NUMBER_OF_64K_PAGES_IN_1MB          16

static unsigned int get_level1_page_table_address() {
	return get_mmu_flash_space_address();
}

static unsigned int* get_address_of_level_2_table_containing(unsigned int virtual_address) {
	unsigned int megabyte = (virtual_address - VIRTUAL_ADDRESS_SPACE_LOWER_BOUND) / (1024*1024);
	return (unsigned int *)(get_mmu_ram_space_address()+(LEVEL_2_TABLE_SIZE*megabyte));
}

static void map_level_2_entry_using_addresses(int virtual_address, int physical_address) {
	int j;
	unsigned int* level_2_table = get_address_of_level_2_table_containing(virtual_address);
	unsigned int first_level_2_table_entry = (virtual_address >> 12) & 0xFF;

	// for "coarse" page tables we need 16 identical entries in a row
	for (j=0; j<16; j++) {
		// 31..16 physical base address
		// 15.12 unused (=0)
		// 11..10 access permission for first 16k (=11, allow all)
		// 9..8 access permission for second 16k (=11, allow all)
		// 7..6 access permission for third 16k (=11, allow all)
		// 5..4 access permission for fourth 16k (=11, allow all)
		// 3..2 C & B cache bits (=10, write-through caching)
		// 1..0 entry type (=01, large page of 64k)
		level_2_table[first_level_2_table_entry+j] = physical_address | 0xFF9;
	}
}

/*
 * Initialise the MMU using the level one page table stored in flash.
 */
void mmu_enable(void) {
	// enable access to all domains
	AT91_coprocessor15_3(0, 0xFFFFFFFF);

	// set MMU translation table base address
	AT91_coprocessor15_2(0xFFFFFFFF, get_level1_page_table_address());

	// turn MMU on
	AT91_coprocessor15_1(0, 1<<0);
}

/*
 * Initialise the page table that controls the MMU's operation. The page table is created in RAM,
 * and then compared to a copy held in flash, which is overwritten only if it
 * is different. The RAM copy is then discarded. This strategy means that
 * the top level page table - which doesn't change in normal operation - is not
 * wasting valuable RAM space.
 */
void page_table_init() {
	int i, j;
	unsigned int level_1_table[4096];

	// map virtual to physical for 4GB and make uncacheable
	for (i=0; i<4096; i++) {
		// 31..20 physical base address
		// 19..12 unused (=0)
		// 11.10 access perms (=11, allow all)
		// 9 unused (=0)
		// 8..5 domain (=0)
		// 4 unused (=1)
		// 3..2 C & B cache bits (=0, don't cache)
		// 1..0 entry type (=10 for section descriptor)
		level_1_table[i] = (i<<20) | 0xC12;
	}

	// turn on caching for each 1Mb of RAM
	for (i=0; i < ((get_ram_size() + (1024*1024) - 1) >> 20); i++) {
		level_1_table[(RAM_BASE_ADDR>>20) + i] |= 0xC; // write-back caching for RAM
	}

	// turn on caching for each 1Mb of flash
	for (i=0; i < (get_flash_size() >> 20); i++) {
		level_1_table[(FLASH_BASE_ADDR>>20)+i] |= 0x8; // write-through caching
	}

	level_1_table[UNCACHED_RAM_START_ADDDRESS >> 20] = 0x20000000 | 0xC12;

	// Set up tables for virtual files
	for (j = 0; j < VIRTUAL_ADDRESS_FILE_COUNT; ++j) {
		level_1_table[(VIRTUAL_ADDRESS_SPACE_LOWER_BOUND >> 20) + j] =
			(get_mmu_ram_space_address()+(LEVEL_2_TABLE_SIZE*j)) | 0x11; // subdivide this 1Mb of flash
		for (i=0; i<NUMBER_OF_64K_PAGES_IN_1MB; i++) {
			// map to itself - will cause a memory access fault if not overwritten
			map_level_2_entry_using_addresses(
				VIRTUAL_ADDRESS_SPACE_LOWER_BOUND+(j*VIRTUAL_ADDRESS_FILE_SPACING)+(i*SECTOR_SIZE),
				VIRTUAL_ADDRESS_SPACE_LOWER_BOUND+(j*VIRTUAL_ADDRESS_FILE_SPACING)+(i*SECTOR_SIZE));
		}
	}

	unsigned int* level_1_table_in_flash = (unsigned int*)get_level1_page_table_address();
	int need_to_flash = FALSE;
	for (i=0; i<4096; i++) {
		if (level_1_table[i] != level_1_table_in_flash[i]) {
			need_to_flash = TRUE;
			break;
		}
	}
	if (need_to_flash) {
		sysPrint("Updating MMU table\r\n");
		flash_write_with_erase((unsigned char*)level_1_table, 4096*4, (Flash_ptr)get_level1_page_table_address());
	}
}

static int read_number(unsigned char* ptr, int number_of_bytes) {
	int result = 0;
	int i;
	for (i=0; i<number_of_bytes; i++) {
		result = (result << 8) | *(ptr+i);
	}
	return result;
}

static int is_FAT_valid() {
	int fat_id = read_number((char*)get_sector_address(FAT_SECTOR), 4);
	switch (fat_id) {
		case FAT_IDENTIFIER_V3:
			return TRUE;
		default:
			return FALSE;
	}
}

/*
 * Answer the space (in bytes) allocated to the FlashFile that occupies
 * the given virtual address. If there is no such file, answers -1.
 *
 * required_virtual_address   Virtual address of required FlashFile
 */
int get_allocated_file_size(int required_virtual_address) {
	if (!is_FAT_valid()) {
		return -1;
	}
	char* fat_ptr = (char*)(get_sector_address(FAT_SECTOR)+4); // +4 to skip identifier
	int recordStatus = read_number(fat_ptr, 2);
	int flags, is_obsolete, sector_count;
	unsigned int virtual_address;
	while (recordStatus != UNUSED_FAT_RECORD_STATUS) {
		int recordSize = read_number(fat_ptr+2, 2);
		int recordType = read_number(fat_ptr+4, 1);
		switch (recordStatus) {
			case DELETED_FAT_RECORD_STATUS:
				break;
			case CURRENT_FAT_RECORD_STATUS:
				switch (recordType) {
					case FILE_FAT_RECORD_TYPE:
						flags = read_number(fat_ptr+5, 2);
						is_obsolete = flags & OBSOLETE_FLAG_MASK;
						virtual_address = read_number(fat_ptr+7, 4);
						if (virtual_address == required_virtual_address && !is_obsolete) {
							sector_count = read_number(fat_ptr+11, 2);
							return sector_count * SECTOR_SIZE;
						}
						break;
					default:
						error("FAT contains bad record type ", recordType);
				}
				break;
			default:
				error("FAT contains bad record status ", recordStatus);
		}
		fat_ptr += recordSize;
		if (recordSize % 2 != 0) {
			fat_ptr += 1;
		}
		recordStatus = recordStatus = read_number(fat_ptr, 2);
	}
    return -1;
}



/*
 * Answer the virtual address of the flash file with a specified name. If
 * there is no such file, answers -1.
 *
 * target_file_name_length    Length of the file name
 * target_file_name           Address of the buffer containing the file name
 */
unsigned int get_file_virtual_address(int target_file_name_length, char* target_file_name) {
	if (!is_FAT_valid()) {
		return -1;
	}
	char* fat_ptr = (char*)(get_sector_address(FAT_SECTOR)+4); // +4 to skip identifier
	int recordStatus = read_number(fat_ptr, 2);
	int flags, is_obsolete, sector_count, file_name_length;
	unsigned int virtual_address;
	while (recordStatus != UNUSED_FAT_RECORD_STATUS) {
		int recordSize = read_number(fat_ptr+2, 2);
		int recordType = read_number(fat_ptr+4, 1);
		switch (recordStatus) {
			case DELETED_FAT_RECORD_STATUS:
				break;
			case CURRENT_FAT_RECORD_STATUS:
				switch (recordType) {
					case FILE_FAT_RECORD_TYPE:
						flags = read_number(fat_ptr+5, 2);
						is_obsolete = flags & OBSOLETE_FLAG_MASK;
						virtual_address = read_number(fat_ptr+7, 4);
						sector_count = read_number(fat_ptr+11, 2);
						file_name_length = read_number(fat_ptr+13+(sector_count*2), 2);
						if (!is_obsolete && (file_name_length == target_file_name_length)) {
							if (strncmp(target_file_name, fat_ptr+13+(sector_count*2)+2, file_name_length) == 0) {
								return virtual_address;
							}
						}
						break;
					default:
						error("FAT contains bad record type ", recordType);
				}
				break;
			default:
				error("FAT contains bad record status ", recordStatus);
		}
		fat_ptr += recordSize;
		if (recordSize % 2 != 0) {
			fat_ptr += 1;
		}
		recordStatus = recordStatus = read_number(fat_ptr, 2);
	}
}

/*
 * Reprogram the MMU to map files into virtual memory as implied
 * by the virtual memory addresses specified in the FAT. Answer whether
 * a valid FAT was detected (if not, the MMU is left untouched).
 *
 * ignore_obsolete_files    specify whether or not to map obsolete files
 */
int reprogram_mmu(int ignore_obsolete_files) {
	if (!is_FAT_valid()) {
		return -1;
	}
	char* fat_ptr = (char*)(get_sector_address(FAT_SECTOR)+4); // +4 to skip identifier
    int recordStatus = read_number(fat_ptr, 2);
	int flags, is_obsolete, sector_count, j;
	unsigned int virtual_address;
	while (recordStatus != UNUSED_FAT_RECORD_STATUS) {
		int recordSize = read_number(fat_ptr+2, 2);
		int recordType = read_number(fat_ptr+4, 1);
		switch (recordStatus) {
			case DELETED_FAT_RECORD_STATUS:
				break;
			case CURRENT_FAT_RECORD_STATUS:
				switch (recordType) {
					case FILE_FAT_RECORD_TYPE:
						flags = read_number(fat_ptr+5, 2);
						is_obsolete = flags & OBSOLETE_FLAG_MASK;
						virtual_address = read_number(fat_ptr+7, 4);
						sector_count = read_number(fat_ptr+11, 2);
						for (j = 0; j < sector_count; ++j) {
							int sector_number = read_number(fat_ptr+13+(j*2), 2);
                            if (virtual_address != 0 && !(is_obsolete && ignore_obsolete_files)) {
                                map_level_2_entry_using_addresses(virtual_address, get_sector_address(sector_number));
                                virtual_address += SECTOR_SIZE;
                            }
						}
						break;
					default:
						error("FAT contains bad record type ", recordType);
				}
				break;
			default:
				error("FAT contains bad record status ", recordStatus);
		}
		fat_ptr += recordSize;
		if (recordSize % 2 != 0) {
			fat_ptr += 1;
        }
        recordStatus = read_number(fat_ptr, 2);
	}

	// invalidate data cache
	data_cache_disable();
	invalidate_data_tlb();
	data_cache_enable();
	return TRUE;
}
