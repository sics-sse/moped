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

package com.sun.squawk;

import com.sun.squawk.util.*;

/**
 * Key value pair of values found in {@link Suite#PROPERTIES_MANIFEST_RESOURCE_NAME} main section.
 *
 */
public final class ManifestProperty {
	/**
	 * Creates a property object.
     * @param name property nane
     * @param value property value
	 */
	public ManifestProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * The name of the property, for example "MIDlet-Vendor".
	 */
	public final String name;
	
	/**
	 * The value of the property, for example "Sun Microsystems, Inc.".
	 */
	public final String value;
	
	/**
	 * Comparator for IMletProperty objects (which are sorted by the property names).
	 */
	public final static Comparer comparer = new Comparer() {
		/**
		 * Compares either two IMletProperty objects, or an IMletProperty object and a String object for their relative ordering based on the property names.
		 *
		 * @param o1 a IMletProperty object
		 * @param o2 either a IMletProperty object or a String object
         * @throws ClassCastException if the arguments' types prevent them from
         * 	       being compared by this Comparer.
		 */
		public int compare(Object o1, Object o2) {
			if (o1 == o2) {
				return 0;
			}
			ManifestProperty mf1 = (ManifestProperty)o1;
			if (o2 instanceof ManifestProperty) {
				return mf1.name.compareTo(((ManifestProperty) o2).name);
			} else {
				return mf1.name.compareTo((String) o2);
			}
		}
	};
}
