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

import javax.swing.event.*;

/**
 * An adapter that's notified when a tree expands or collapses a node.
 */
public abstract class ModifiedTreeExpansionAdapter implements TreeExpansionListener {

    final int modifiers;
    private boolean modify;

    /**
     * Creates a listener that is interested to know if certain modifiers were present in the event
     * that caused a tree node to be expanded or collapsed.
     *
     * @param modifiers  the modifiers that the listener is interested in
     */
    public ModifiedTreeExpansionAdapter(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Called whenever an item in the tree has been expanded.
     *
     * @param e         the event detailing the expansion
     * @param modified  true if one of the modifiers specified at construction of this
     *                  object was present in the event that caused the expansion
     */
    public void treeExpanded(TreeExpansionEvent e, boolean modified) {}

    /**
     * Called whenever an item in the tree has been collapsed.
     *
     * @param e         the event detailing the collapse
     * @param modified  true if one of the modifiers specified at construction of this
     *                  object was present in the event that caused the collapse
     */
    public void treeCollapsed(TreeExpansionEvent e, boolean modified) {}

    void setModifiers(int modifiers) {
        modify |= ((this.modifiers & modifiers) != 0);
    }

    /**
     * Redirects notification to {@link #treeExpanded(TreeExpansionEvent, boolean)}.
     */
    public final void treeExpanded(TreeExpansionEvent event) {
        boolean modified = modify;
        modify = false;
        treeExpanded(event, modified);
    }

    /**
     * Redirects notification to {@link #treeCollapsed(TreeExpansionEvent, boolean)}.
     */
    public final void treeCollapsed(TreeExpansionEvent event) {
        boolean modified = modify;
        modify = false;
        treeCollapsed(event, modified);
    }
}
