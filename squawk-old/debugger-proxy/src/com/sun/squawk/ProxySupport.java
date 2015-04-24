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

import com.sun.squawk.translator.*;
import com.sun.squawk.pragma.*;


/**
 * Support routines for the Squawk Debugger Proxy (SDP) that must live in
 * package <code>java.lang</code>.
 *
 */
public class ProxySupport {

    private static void setProperty(String key, String value) {
        if (VM.isHosted()) {
            System.setProperty(key, value);
        } else {
            VM.getCurrentIsolate().setProperty(key, value);
        }
    }
    
    /**
     * Creates and initializes the translator.
     *
     * @param classPath   the class search path
     */
    public static void initializeTranslator(String classPath) throws HostedPragma {
        if (VM.getCurrentIsolate() == null) {
            Suite suite = new Suite("-proxy suite-", null, Suite.EXTENDABLE_LIBRARY);
            VM.setCurrentIsolate(null);
            Isolate isolate = new Isolate(null, null, suite);
            VM.setCurrentIsolate(isolate);
            setProperty("translator.deadMethodElimination", "false");
            setProperty("translator.deadClassElimination",  "false");
            
            isolate.setTranslator(new Translator());
            
            TranslatorInterface translator = isolate.getTranslator();
            MethodMetadata.preserveLineNumberTables();
            MethodMetadata.preserveLocalVariableTables();
            translator.open(suite, classPath);
        }
    }
}
