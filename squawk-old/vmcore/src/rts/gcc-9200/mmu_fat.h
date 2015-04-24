/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle. All Rights Reserved.
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
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */

#ifndef MMU_FAT_H
#define	MMU_FAT_H

// FAT constants: these must match the constants in FlashFile.java and ConfigPage.java
#define VIRTUAL_ADDRESS_FILE_COUNT			8
#define FAT_SECTOR							5
#define SECTOR_SIZE							0x10000
#define VIRTUAL_ADDRESS_FILE_SPACING 		(1024*1024)
#define VIRTUAL_ADDRESS_SPACE_LOWER_BOUND 	0x10800000
#define VIRTUAL_ADDRESS_SPACE_UPPER_BOUND 	(VIRTUAL_ADDRESS_SPACE_LOWER_BOUND + (VIRTUAL_ADDRESS_FILE_COUNT*VIRTUAL_ADDRESS_FILE_SPACING))
#define FAT_IDENTIFIER_V3					0x1234567A


/*
 * Initialise the MMU using the level one page table stored in flash.
 */
void mmu_enable(void);

/*
 * Initialise the page table that controls the MMU's operation. The page table is created in RAM,
 * and then compared to a copy held in flash, which is overwritten only if it
 * is different. The RAM copy is then discarded. This strategy means that
 * the top level page table - which doesn't change in normal operation - is not
 * wasting valuable RAM space.
 */
void page_table_init();

/*
 * Answer the space (in bytes) allocated to the FlashFile that occupies
 * the given virtual address. If there is no such file, answers -1.
 *
 * required_virtual_address   Virtual address of required FlashFile
 */
int get_allocated_file_size(int required_virtual_address);

/*
 * Answer the virtual address of the flash file with a specified name. If
 * there is no such file, answers -1.
 *
 * target_file_name_length    Length of the file name
 * target_file_name           Address of the buffer containing the file name
 */
unsigned int get_file_virtual_address(int target_file_name_length, char* target_file_name);

/*
 * Reprogram the MMU to map files into virtual memory as implied
 * by the virtual memory addresses specified in the FAT. Answer whether
 * a valid FAT was detected (if not, the MMU is left untouched).
 *
 * ignore_obsolete_files    specify whether or not to map obsolete files
 */
int reprogram_mmu(int ignore_obsolete_files);

#endif	/* MMU_FAT_H */

