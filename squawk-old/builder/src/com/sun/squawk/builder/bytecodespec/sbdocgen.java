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

package com.sun.squawk.builder.bytecodespec;

import java.io.*;
import java.util.*;

import com.sun.squawk.builder.Build;

public class sbdocgen {
    private static final Set<Integer> opcodes = new java.util.HashSet<Integer>();

    public static void reportOpcode(int opcode) {
        opcodes.add(opcode);
    }

    public static void main (String args[]) throws IOException {
        List<Instruction> insns = Instructions.getInstructions();
        Collections.sort(insns);

        List<Character> startLetters = new java.util.ArrayList<Character>();
        char last = 0;
        for (int i = 0; i < insns.size(); i++) {
            Instruction insn = (Instruction)insns.get(i);
            char leadChar = insn.getName().charAt(0);
            if (last != leadChar) {
                startLetters.add(leadChar);
                last = leadChar;
            }
        }

        File docdir = new File("SquawkBytecodeSpec");
        Build.mkdir(docdir);

        PrintWriter out = null;
        out = new PrintWriter(new FileWriter(new File(docdir, "Instructions.doc.html")));

        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Squawk Bytecode Instruction Set</title>");
        out.println("</head>");
        out.println("<body BGCOLOR=#eeeeff text=#000000 LINK=#0000ff VLINK=#000077 ALINK=#ff0000>");
        out.println("<table width=100%><tr>");
        out.print("<td>Prev | <a href=\"Instructions2.doc1.html\">Next</a> | <a href=\"Instructions2.index.html\">Index</a> </td>");
        out.println("<td align=right><i><i>Squawk Bytecode Instruction Set</i></i></td>");
        out.println("</tr></table>");
        out.println();
        out.println("<hr><br>");
        out.println();
        for (int i = 0; i < startLetters.size(); i++) {
            Character c = (Character)startLetters.get(i);
            out.println("<a href=\"Instructions2.doc" + (i+1) + ".html\">" + Character.toUpperCase(c.charValue()) + "</a>");
        }
        out.println();
        out.println("<hr><br>");
        out.println();
        out.println("<h1>Squawk Bytecode Instruction Set</h1>");
        out.println("<hr><p>");
        out.println("A Squawk bytecode instruction consists of an opcode specifying the operation");
        out.println("to be performed, followed by zero or more operands embodying values to be operated");
        out.println("upon. This chapter gives details about the format of each Squawk bytecode");
        out.println("instruction and the operation it performs.</p>");
        out.println("<hr>");

        out.println("<p>Prev | <a href=\"Instructions2.doc1.html\">Next</a></p>");
        out.println("</body></html>");

        out.close();

        PrintWriter indexPage = new PrintWriter(new FileWriter(new File(docdir, "Instructions2.index.html")));
        indexPage.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">");
        indexPage.println("<html>");
        indexPage.println("<head>");
        indexPage.println("<title>Squawk Bytecode Instruction Set</title>");
        indexPage.println("</head>");
        indexPage.println("<body BGCOLOR=#eeeeff text=#000000 LINK=#0000ff VLINK=#000077 ALINK=#ff0000>");
        indexPage.println("<table width=100%><tr>");
        indexPage.println("<td><a href=\"Instructions.doc.html\">Contents</a></td>");
        indexPage.println("<td align=right><i><i>Squawk Bytecode Instruction Set</i></i></td>");
        indexPage.println("</tr></table>");
        indexPage.println();
        indexPage.println("<hr><br>");
        indexPage.println();

        Iterator<Instruction> iter = insns.iterator();
        Instruction insn = (iter.hasNext()) ? iter.next() : null;
        for (int i = 0; i < startLetters.size(); i++) {
            String thisPage = "Instructions2.doc" + (i+1) + ".html";
            String prevPage = "Instructions2.doc" + i + ".html";
            String nextPage = "Instructions2.doc" + (i+2) + ".html";

            out = new PrintWriter(new FileWriter(new File(docdir, thisPage)));
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Squawk Bytecode Instruction Descriptions</title>");
            out.println("</head>");
            out.println("<body BGCOLOR=#eeeeff text=#000000 LINK=#0000ff VLINK=#000077 ALINK=#ff0000>");
            out.println("<table width=100%><tr>");
            out.print("<td>");
            out.print(" <a href=\"Instructions.doc.html\">Contents</a> | ");
            if (i > 0)
                out.print("<a href=\"" + prevPage + "\">Prev</a>");
            else
                out.print("<a href=\"Instructions.doc.html\">Prev</a>");
            out.print(" | ");
            if (i < startLetters.size() - 1)
                out.print("<a href=\"" + nextPage +"\">Next</a>");
            else
                out.print("Next");
            out.println(" | <a href=\"Instructions2.index.html\">Index</a> </td>");
            out.println("<td align=right><i><i>Squawk Bytecode Instruction Set</i></i></td>");
            out.println("</tr></table>");
            out.println();
            out.println("<hr><br>");
            out.println();
            for (int j = 0; j < startLetters.size(); j++) {
                Character c = (Character)startLetters.get(j);
                out.println("<a href=\"Instructions2.doc" + (j+1) + ".html\">" + Character.toUpperCase(c.charValue()) + "</a>");
            }
            out.println();

            while (insn != null && Character.toUpperCase(insn.getName().charAt(0)) == Character.toUpperCase(((Character)startLetters.get(i)).charValue())) {

                String name = insn.getName();
                indexPage.println("<a href=\"" + thisPage + "#" + name + "\">" + name + "</a><br>");

                out.println("<hr><a name=\"" + name + "\"><h2>" + name + "</h2>");

                out.println("<p><b>Operation</b></p>");
                out.println("<blockquote>");
                insn.printOperation(out);
                out.println("</blockquote>");

                out.println("<p><b>Format</b></p>");
                out.println("<blockquote>");
                insn.printFormat(out);
                out.println("</blockquote>");

                out.println("<p><b>Forms</b></p>");
                out.println("<blockquote>");
                insn.printForms(out);
                out.println("</blockquote>");

                out.println("<p><b>Operand Stack</b></p>");
                out.println("<blockquote>");
                insn.printOperandStack(out);
                out.println("</blockquote>");

                out.println("<p><b>Description</b></p>");
                out.println("<blockquote>");
                insn.printDescription(out);
                out.println("</blockquote>");

                if (insn.hasNotes()) {
                    out.println("<p><b>Notes</b></p>");
                    out.println("<blockquote>");
                    insn.printNotes(out);
                    out.println("</blockquote>");
                }

                insn = (iter.hasNext()) ? (Instruction)iter.next() : null;
            }

            out.println("<hr>");
            if (i > 0)
                out.print("<a href=\"Instructions2.doc" + i + ".html\">Prev</a>");
            else
                out.print("<a href=\"Instructions.doc.html\">Prev</a>");
            out.print(" | ");
            if (i < startLetters.size() - 1)
                out.print("<a href=\"Instructions2.doc" + (i+2) + ".html\">Next</a>");
            else
                out.print("Next");
            out.println();
            out.println("</body></html>");

            out.flush();
            out.close();
        }

        indexPage.println("</body></html>");
        indexPage.close();

        for (int i = 0; i < 256; i++)
            if (!opcodes.contains(new Integer(i)))
                System.out.println("missing opcode: " + i);
    }
}