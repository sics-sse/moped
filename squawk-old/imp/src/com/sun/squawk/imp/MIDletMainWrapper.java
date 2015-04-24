/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.imp;

import javax.microedition.midlet.*;

import com.sun.squawk.*;

/**
 * 
 *
 *
 */
public class MIDletMainWrapper {
    
    /**
     * Purely static class should not be instantiated.
     */
    private MIDletMainWrapper() {}
    
    /**
     * Wait forever. When midlet is done, Midlet.notifyDestroyed gets called, which terminates the isolate.
     */
    private static void waitForMidletDone() {
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e1) {
            }
        }
    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        String className;
        Isolate iso = Isolate.currentIsolate();
         
        if (args.length == 2 && args[0].equals("-name")) {
            className = args[1];
            if (className == null || className.length() == 0) {
                throw new IllegalArgumentException("Empty midlet class name specified");
            }
        } else if (args.length != 1) {
            throw new IllegalArgumentException("Expected to find MIDlet property name as argument nothing less or more");
        } else {
            String midletPropertyName = args[0];
            String midletDescription = VM.getManifestProperty(midletPropertyName);
            if (midletDescription == null) {
                throw new IllegalArgumentException(midletPropertyName + " property must exist in " + Suite.PROPERTIES_MANIFEST_RESOURCE_NAME);
            }
            int index = midletDescription.lastIndexOf(',');
            if (index == -1) {
                throw new IllegalArgumentException("Found property "+midletPropertyName+" not containing icon ([label, icon, class name]): " + midletDescription);
            }
            className = midletDescription.substring(index + 1).trim();
            if (className == null || className.length() == 0) {
                throw new IllegalArgumentException("Found property "+midletPropertyName+" not containing class name ([label, icon, class name]): " + midletDescription);
            }
        }
        Klass klass;
        
        // Give the Isolate and thread sensible names...
        iso.setName(className);
        String thrname = new StringBuffer(className).append(" - main").toString();
        VMThread.currentThread().setName(thrname);
        
        try {
            klass = Klass.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("MIDlet class specified, " + className + ", was not found");
        }
        if (!MIDlet.class.isAssignableFrom(Klass.asClass(klass))) {
            throw new IllegalArgumentException("Specified class, " + className + ", must be subclass of javax.microedition.midlet.MIDlet");
        }
        MIDlet midLet = (MIDlet) klass.newInstance();
        
/*if[ENABLE_SDA_DEBUGGER]*/
        // Notify debugger of event:
        if (iso.getDebugger() != null) {
            iso.getDebugger().notifyEvent(new Debugger.Event(Debugger.Event.VM_INIT, VMThread.currentThread()));
        }
/*end[ENABLE_SDA_DEBUGGER]*/

        while (true) {
            try {
            	ImpGlobal.midLetTunnel.callStartApp(midLet);
                waitForMidletDone();  // never returns.
            } catch (MIDletStateChangeException e) {
                // Handle the transient oriented exceptions and try again in a while
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            } catch (Throwable t) {
                try {
                    t.printStackTrace();
                    ImpGlobal.midLetTunnel.callDestroyApp(midLet, true);
                    midLet.notifyDestroyed();
                } catch (MIDletStateChangeException e) {
                    // We should ignore since we requested an unconditional exit
                }
                break;
            }
        }
    }

}
