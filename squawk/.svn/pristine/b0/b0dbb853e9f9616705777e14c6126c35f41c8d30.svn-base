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

package com.sun.squawk.traces;

/**
 * An <code>AddressRelocator</code> assists with relocating canonical addresses (within
 * a suite file for example) to a runtime address based on where the memory was relocated
 * at runtime.
 *
 * This relocator assumes that there is no overlap between canonical addresses and
 * runtime addresses.
 */
class AddressRelocator {

    /**
     * The address at which ROM was relocated.
     */
    private final long romStart;

    /**
     * The canonical end of ROM (the canonical start of ROM is 0).
     */
    private final int canonicalRomEnd;

    /**
     * The address at which NVM was relocated.
     */
    private final long nvmStart;

    /**
     * The canonical end of NVM (the canonical start of NVM is equal to the canonical end of ROM).
     */
    private final int canonicalNvmEnd;

    /**
     * Constructs an AddressRelocator instance.
     *
     * @param romStart  the address at which ROM starts
     * @param romSize   the size of ROM
     * @param nvmStart  the address at which NVM starts
     * @param nvmSize   the size of NVM
     */
    public AddressRelocator(long romStart, int romSize, long nvmStart, int nvmSize) {
        this.romStart = romStart;
        this.canonicalRomEnd = romSize;
        this.nvmStart = nvmStart;
        this.canonicalNvmEnd = canonicalRomEnd + nvmSize;

//System.out.println("0 - " + canonicalRomEnd + " -> " + romStart);
//System.out.println(canonicalRomEnd + " - " + canonicalNvmEnd + " -> " + nvmStart);
    }

    /**
     * Relocates a given address if it a canonical address.
     *
     * @param address  the address to relocate
     * @return the relocated value of <code>address</code> if it is determined to be in canonical form otherwise
     *         the unmodified value of <code>address</code>
     */
    public long relocate(long address) {
        if (address > 0) {
            if (address <= canonicalRomEnd) {
                return address + romStart;
            } else if (address <= canonicalNvmEnd) {
                return address - canonicalRomEnd + nvmStart;
            }
        }
        return address;
    }
}
