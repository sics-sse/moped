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

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * This subclass of JTree provides extended tree functionality.
 *
 */
public final class TraceTree extends JTree {

    /**
     * Returns an instance of <code>JTree</code> which displays the root node
     * -- the tree is created using the specified data model.
     *
     * @param model  the <code>DefaultTreeModel</code> to use as the data model
     */
    public TraceTree(DefaultTreeModel model) {
        super(model);
        installMouseAndKeyListeners();
    }

    /**
     * Updates all the registered <code>ModifiedTreeExpansionAdapter</code>s to inform them
     * of the modifiers that were present in the last mouse or keyboard event.
     *
     * @param modifiers  the modifiers that were present in the last mouse or keyboard event
     */
    private void updateModifiedTreeExpansionAdapters(int modifiers) {
        TreeExpansionListener[] listeners = getTreeExpansionListeners();
        for (int i = 0; i != listeners.length; ++i) {
            if (listeners[i] instanceof ModifiedTreeExpansionAdapter) {
                ModifiedTreeExpansionAdapter mtea = (ModifiedTreeExpansionAdapter)listeners[i];
                mtea.setModifiers(modifiers);
            }
        }
    }

    /**
     * Installs the mouse and key listener that will modify node expansion. These listeners
     * must preceed and other already installed mouse and key listeners as they must
     * communicate with any registered <code>ModifiedTreeExpansionAdapter</code>s before
     * these expansion listeners are fired.
     */
    private void installMouseAndKeyListeners() {
        MouseListener[] mouseListeners = getMouseListeners();
        for (int i = 0; i != mouseListeners.length; ++i) {
            removeMouseListener(mouseListeners[i]);
        }
        KeyListener[] keyListeners = getKeyListeners();
        for (int i = 0; i != keyListeners.length; ++i) {
            removeKeyListener(keyListeners[i]);
        }

        addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                updateModifiedTreeExpansionAdapters(e.getModifiers());
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                updateModifiedTreeExpansionAdapters(e.getModifiers());
            }
        });

        for (int i = 0; i != mouseListeners.length; ++i) {
            addMouseListener(mouseListeners[i]);
        }
        for (int i = 0; i != keyListeners.length; ++i) {
            addKeyListener(keyListeners[i]);
        }
    }
}
