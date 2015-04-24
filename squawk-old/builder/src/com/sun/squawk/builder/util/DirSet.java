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

package com.sun.squawk.builder.util;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * A FileSet instance represents a selection of directories under a given directory that match a criteria
 * defined by a {@link File.Selector}.
 *
 */
public class DirSet extends FileSet {

    /**
     * Creates a FileSet.
     *
     * @param baseDir   the base directory for finding directories in the set.
     * @param selector  the selector for matching files in the set if all directories in the directory are to be matched
     */
    public DirSet(File baseDir, Selector selector) {
        super(baseDir, selector);
    }

    /**
     * Creates a FileSet.
     *
     * @param baseDir    the base directory for finding directories in the set.
     * @param expression an expression that is {@link SelectionExpressionParser parsed} to create a Selector
     */
    public DirSet(File baseDir, String expression) {
        super(baseDir, expression);
    }

    /**
     * Gets the directories in the set as a list.
     *
     * @return the directories in the set
     */
    public List<File> list() {
        final ArrayList<File> list = new ArrayList<File>();
        final Selector selector = getSelector();
        new FileVisitor() {
            public boolean visit(File file) {
                if (file.isDirectory()) {
                    if (selector == null || selector.isSelected(file)) {
                        list.add(file);
                    }
                }
                return true;
            }
        }.run(getBaseDir());
        return list;
    }


}