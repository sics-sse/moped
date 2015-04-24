/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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
package com.sun.squawk.vm2c;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.Attribute.Constant;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.tree.JCTree;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.type.DeclaredType;

import static com.sun.squawk.vm2c.CCodeBuffer.*;

/**
 * Utility class for extracting vm2c specific annotations from the javadoc
 * of a javac AST node. The format of these annotations is:
 *
 *  "@vm2c" key [ '(' value ')' ]
 *
 */
public class AnnotationParser {

    public static class Annotation {

        final String key;
        final boolean valueIsOptional;

        Annotation(String key, boolean valueIsOptional) {
            this.key = key;
            this.valueIsOptional = valueIsOptional;
        }
    }
    /**
     * The annotations understood by this parser.
     */
    final Map<String, Annotation> annotations;

    public AnnotationParser(Map<String, Annotation> annotations) {
        this.annotations = annotations;
    }

    public AnnotationParser() {
        this(DEFAULT_ANNOTATIONS);
    }
    public static final Map<String, Annotation> DEFAULT_ANNOTATIONS;

    static {
        Map<String, Annotation> m = new HashMap<String, Annotation>(5);
        m.put("root", new Annotation("root", false));
        m.put("code", new Annotation("code", false));
        m.put("macro", new Annotation("macro", false));
        m.put("proxy", new Annotation("proxy", true));
        m.put("implementers", new Annotation("implementers", false));
        DEFAULT_ANNOTATIONS = Collections.unmodifiableMap(m);
    }

    private void error(JCTree tree, String msg) {
        throw new InconvertibleNodeException(tree, msg);
    }

    /**
     * Parses the javadoc for a method and extracts the annotations
     * (if any) that this parser was configured with.
     */
    public Map<String, String> parse(ProcessedMethod method) {
        List<Compound> list = method.sym.getAnnotationMirrors();
        Map<String, String> result = Collections.emptyMap();
        for (Compound c : list) {
            ClassType annotationType = (ClassType) c.getAnnotationType();
            String annotationTypeName = annotationType.tsym.getQualifiedName().toString();
            if (annotationTypeName.equals("com.sun.squawk.Vm2c")) {
                String key = null;
                String value = null;
                for (Map.Entry<MethodSymbol, Attribute> entry : c.getElementValues().entrySet()) {
                    key = entry.getKey().getQualifiedName().toString();
                    ClassType valueType = (ClassType) entry.getValue().type;
                    if (valueType.tsym.getQualifiedName().toString().equals("java.lang.String")) {
                        value = entry.getValue().toString();
                        if (value.length() == 0) {
                            value = null;
                        } else {
                            value = value.substring(1, value.length() - 1);
                            value = value.replace("\\\"", "\"");
                        }
                    } else {
                        error(method.tree, "Vm2c annotation values should be strings");
                        return null;
                    }
                }
                if (result.isEmpty()) {
                    result = new HashMap<String, String>();
                }
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Align code to be indented to left margin.
     */
    private static StringBuilder indent(int margin, StringBuilder buf) {
        for (int i = 0; i < margin; i++) {
            buf.append(' ');
        }
        return buf;
    }

    /**
     * Parses the javadoc for an AST node and extracts the original javadoc (if any).
     */
    public static String getDocComment(JCTree.JCCompilationUnit unit, JCTree node, int margin) {
        if (unit.docComments != null) {
            String dc = unit.docComments.get(node);
            if (dc != null) {
                StringBuilder buf = new StringBuilder(dc.length() * 2);
                buf.append("/**").append(LINE_SEP);
                int pos = 0;
                int endpos = lineEndPos(dc, pos);
                while (pos < dc.length()) {
                    indent(margin, buf).append(" *");
                    if (pos < dc.length() && dc.charAt(pos) > ' ') {
                        buf.append(" ");
                    }
                    buf.append(dc.substring(pos, endpos)).append(LINE_SEP);
                    pos = endpos + 1;
                    endpos = lineEndPos(dc, pos);
                }
                indent(margin, buf).append(" */").append(LINE_SEP);
                return buf.toString();
            }
        }
        return "";
    }

    private static int lineEndPos(String s, int start) {
        int pos = s.indexOf('\n', start);
        if (pos < 0) {
            pos = s.length();
        }
        return pos;
    }
}
