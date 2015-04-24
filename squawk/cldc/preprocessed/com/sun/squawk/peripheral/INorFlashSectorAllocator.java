/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.peripheral;

import java.io.IOException;

/**
 * INorFlashSectorAllocator is part of the implementation of the Record Management Store
 * (see {@link javax.microedition.rms.RecordStore}). It should not be used for other purposes.
 *
 */
public interface INorFlashSectorAllocator extends IPeripheral {
	
	/**
	 * @param purpose code (defined in {@link INorFlashSector}) indicating purpose of sectors
	 * @return the sectors that were in use for this purpose at the time of the last VM exit
	 * @throws InsufficientFlashMemoryException
	 */
	INorFlashSector[] getInitialSectors(int purpose) throws IOException;

	/**
	 * @param purpose code (defined in {@link INorFlashSector}) indicating purpose of sector
	 * @return a free sector
	 * @throws InsufficientFlashMemoryException
	 */
	INorFlashSector getExtraSector(int purpose) throws InsufficientFlashMemoryException;

	/**
	 * Notify the allocator that a previously allocated sector is no longer required
	 * @param sector the sector to free
	 * @param purpose code (defined in {@link INorFlashSector}) indicating purpose of sector
	 */
	void releaseSector(INorFlashSector sector, int purpose);

}
