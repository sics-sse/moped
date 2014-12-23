//if[NEW_IIC_MESSAGES]
/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */
package com.sun.squawk.io.mailboxes;

/**
 * A ObjectEnvelope can be used to pass a copy of an ICopiable object between isolates.
 */
public class ObjectEnvelope extends Envelope {
    protected ICopiable contents;
    
    public ObjectEnvelope(ICopiable contents) {
        this.contents = contents;
    }

   /**
     * Return the contents of the envelope.
     */
    public Object getContents() {
        checkCallContext();
        return contents;
    }
    
    /**
     * The copy() method copies the contents of the envelope by calling the ICopiable.copyFrom method.
     * 
     * Called by the system once per Envelope, sometime between when the message is sent by send(),
     * and when it is received by receive().
     *
     * If the ICopiable contents class does not a have a public, no-args constructor, the 
     * contents will not be copied.
     *
     * @return a copy of this Envelope
     */
    /* package-private*/ Envelope copy() {
        ObjectEnvelope theCopy = (ObjectEnvelope)super.copy();
        theCopy.contents = null;
        try {
            theCopy.contents = (ICopiable)this.contents.getClass().newInstance();
            theCopy.contents.copyFrom(this.contents);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        // on failure, contents will be null

        return theCopy;
    }

}
