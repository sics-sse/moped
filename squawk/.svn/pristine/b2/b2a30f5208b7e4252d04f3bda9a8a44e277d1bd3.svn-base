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

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A FileSet instance represents a selection of files under a given directory that match a criteria
 * defined by a {@link Selector}.
 *
 */
public class FileSet {

    /**
     * A Selector specifies the criteria that a file or directory must match to be included in a FileSet.
     */
    public static interface Selector {

        /**
         * Determines if a given file matches the criteria for inclusion in a FileSet.
         *
         * @param file   the file to test
         * @return true if <code>file</code> matches this Selector's criteria
         */
        public boolean isSelected(File file);
    }

    /**
     * A SuffixSelector selects files whose {@link File#getPath paths} start with a a given suffix.
     */
    public static class SuffixSelector implements Selector {

        /**
         * The suffix to match.
         */
        private final String suffix;

        /**
         * Creates a SuffixSelector.
         *
         * @param suffix   the suffix to match
         */
        public SuffixSelector(String suffix) {
            this.suffix = suffix;
        }

        /**
         * Returns true if <code>file.getPath()</code> ends with {@link #suffix}.
         *
         * @param file   the file to test
         * @return true if <code>file.getPath()</code> ends with {@link #suffix}
         */
        public boolean isSelected(File file) {
            return file.getPath().endsWith(suffix);
        }
    }

    /**
     * A PathPrefixSelector selects files whose {@link File#getPath paths} start with a given prefix.
     */
    public static class PathPrefixSelector implements Selector {

        /**
         * The prefix to match.
         */
        private final String prefix;

        /**
         * Creates a PrefixSelector.
         *
         * @param prefix   the prefix to match
         */
        public PathPrefixSelector(String prefix) {
            this.prefix = prefix;
        }

        /**
         * Returns true if <code>file.getPath()</code> starts with {@link #prefix}.
         *
         * @param file   the file to test
         * @return true if <code>file.getPath()</code> starts with {@link #prefix}
         */
        public boolean isSelected(File file) {
            return file.getPath().startsWith(prefix);
        }
    }

    /**
     * A NamePrefixSelector selects files whose {@link File#getName name} start with a a given prefix.
     */
    public static class NamePrefixSelector implements Selector {

        /**
         * The prefix to match.
         */
        private final String prefix;

        /**
         * Creates a PrefixSelector.
         *
         * @param prefix   the prefix to match
         */
        public NamePrefixSelector(String prefix) {
            this.prefix = prefix;
        }

        /**
         * Returns true if <code>file.getName()</code> starts with {@link #prefix}.
         *
         * @param file   the file to test
         * @return true if <code>file.getName()</code> starts with {@link #prefix}
         */
        public boolean isSelected(File file) {
            return file.getName().startsWith(prefix);
        }
    }


    /**
     * A SubstringSelector selects files whose {@link File#getPath paths} contain a given substring.
     */
    public static class SubstringSelector implements Selector {

        /**
         * The substring to match.
         */
        private final String substring;

        /**
         * Creates a SubstringSelector.
         *
         * @param substring   the substring to match
         */
        public SubstringSelector(String substring) {
            this.substring = substring;
        }

        /**
         * Returns true if <code>file.getPath()</code> contains {@link #substring}.
         *
         * @param file   the file to test
         * @return true if <code>file.getPath()</code> contains {@link #substring}
         */
        public boolean isSelected(File file) {
            return file.getPath().indexOf(substring) != -1;
        }
    }

    /**
     * A PatternSelector selects files whose {@link File#getName name} or {@link File#getPath path} matches a given {@link Pattern}.
     */
    public static class PatternSelector implements Selector {

        /**
         * The pattern to match.
         */
        private final Pattern pattern;

        /**
         * Specifies if only the file's name should be matched.
         */
        private final boolean matchNameOnly;

        /**
         * Creates a NameSelector.
         *
         * @param pattern        the {@link Pattern} to match
         * @param matchNameOnly  specifies if a file's {@link File#getName name} or {@link File#getPath path} should be matched
         */
        public PatternSelector(String pattern, boolean matchNameOnly) {
            this.pattern = Pattern.compile(pattern);
            this.matchNameOnly = matchNameOnly;
        }

        /**
         * Returns true if <code>file.getName()</code> equals {@link #name}.
         *
         * @param file   the file to test
         * @return true if <code>file.getName()</code> equals {@link #name}
         */
        public boolean isSelected(File file) {
            String toMatch = matchNameOnly ? file.getName() : file.getPath();
            return pattern.matcher(toMatch).matches();
        }
    }

    /**
     * A NameSelector selects files whose {@link File#getName name} equals a given string.
     */
    public static class NameSelector implements Selector {

        /**
         * The substring to match.
         */
        private final String name;

        /**
         * Creates a NameSelector.
         *
         * @param name   the name to match
         */
        public NameSelector(String name) {
            this.name = name;
        }

        /**
         * Returns true if <code>file.getName()</code> equals {@link #name}.
         *
         * @param file   the file to test
         * @return true if <code>file.getName()</code> equals {@link #name}
         */
        public boolean isSelected(File file) {
            return file.getName().equals(name);
        }
    }

    /**
     * An OrSelector aggregates 2 selectors such that if either of them match a given file, then the OrSelector matches the file.
     */
    public static class OrSelector implements Selector {

        /**
         * The first of the pair of selectors to be or'ed.
         */
        private final Selector left;

        /**
         * The second of the pair of selectors to be or'ed.
         */
        private final Selector right;

        /**
         * Creates an OrSelector.
         *
         * @param left   the first selector
         * @param right   the second selector
         */
        public OrSelector(Selector left, Selector right) {
            this.left = (left);
            this.right = (right);
        }

        /**
         * Returns true if either the {@link #left} or {@link #right} selector matches a given file.
         *
         * @param file   the file to test
         * @return true if either the {@link #left} or {@link #right} selector matches <code>file</code>
         */
        public boolean isSelected(File file) {
            return left.isSelected(file) || right.isSelected(file);
        }
    }

    /**
     * An AndSelector aggregates 2 selectors such that if both of them match a given file, then the AndSelector matches the file.
     */
    public static class AndSelector implements Selector {

        /**
         * The first of the pair of selectors to be and'ed.
         */
        private final Selector left;

        /**
         * The second of the pair of selectors to be and'ed.
         */
        private final Selector right;

        /**
         * Creates an AndSelector.
         *
         * @param left   the first selector
         * @param right   the second selector
         */
        public AndSelector(Selector left, Selector right) {
            this.left = (left);
            this.right = (right);
        }

        /**
         * Returns true if either the {@link #left} and {@link #right} selector matches a given file.
         *
         * @param file   the file to test
         * @return true if either the {@link #left} and {@link #right} selector matches <code>file</code>
         */
        public boolean isSelected(File file) {
            return left.isSelected(file) && right.isSelected(file);
        }
    }

    /**
     * Selector that filters files based on whether they are newer than a matching file in another directory tree.
     */
    public static class DependSelector implements Selector {

        /**
         * The mapper used to map a file to its dependent file.
         */
        protected final Mapper mapper;

        /**
         * Creates a DependSelector.
         *
         * @param mapper  used to map a file to its dependent file
         */
        public DependSelector(Mapper mapper) {
            this.mapper = mapper;
        }

        /**
         * Gets the mapper used to map a file to its dependent file.
         *
         * @return the mapper
         */
        public Mapper getMapper() {
            return mapper;
        }

        /**
         * Returns true if the file mapped from <code>file</code> does not exist or is older than <code>file</code>.
         *
         * @param file   the file to test
         * @return true if the file mapped from <code>file</code> does not exist or is older than <code>file</code>
         */
        public boolean isSelected(File file) {
            File dependentFile = mapper.map(file);
            return !dependentFile.exists() || dependentFile.lastModified() < file.lastModified();
        }
    }

    /**
     * A Mapper instance maps a file to another file.
     */
    public interface Mapper {

        /**
         * Gets the file that is mapped from a given file.
         *
         * @param from   the source of the mapping
         * @return the file that <code>from</code> maps to
         */
        public File map(File from);
    }

    /**
     * A SourceDestDirMapper maps a source file in one directory to the file in another directory
     * with the same relative path.
     */
    public static class SourceDestDirMapper implements Mapper {

        /**
         * The source directory.
         */
        private final File srcDir;

        /**
         * The destination directory.
         */
        private final File destDir;

        /**
         * Creates a SourceDestDirMapper instance.
         *
         * @param srcDir  the source directory
         * @param destDir the destination directory
         */
        public SourceDestDirMapper(File srcDir, File destDir) {
            this.srcDir = srcDir;
            this.destDir = destDir;
        }

        /**
         * {@inheritDoc}
         */
        public File map(File from) {
            return new File(destDir, FileSet.getRelativePath(srcDir, from));
        }
    }



    /**
     * The SelectorExpressionParser class contains a single public method for creating a Selector from
     * a selector expression. The grammar for a selector expression is:
     *
     * <p><hr><blockquote><pre>
     *   EXPR      ::=  AND | OR | PREFIX | SUFFIX | SUBSTRING | NAME | PATTERN
     *   AND       ::= ":and" EXPR EXPR
     *   OR        ::= ":or"  EXPR EXPR
     *   PREFIX    ::= ":pfx" token
     *   SUFFIX    ::= ":sfx" token
     *   SUBSTRING ::= ":sub" token
     *   NAME      ::= ":name" token
     *   PATTERN   ::= ":ptn" token ( "true" | "false" )
     * </pre></blockquote><hr></p>
     *
     * where a <code>token</code> is a string containing no white space. If a token starts with ':', then
     * it must be escaped with a '\'.
     */
    public static final class SelectorExpressionParser {

        /**
         * Parses a selector expression to create a Selector.
         *
         * @param expression   the expression to parse
         * @return  the Selector built from <code>expression</code>
         * @throws IllegalArgumentException if the expression is not well formed
         */
        public static Selector parse(String expression) {
            StringTokenizer st = new StringTokenizer(expression);
            if (!st.hasMoreTokens()) {
                throw new IllegalArgumentException("selector expression cannot be empty");
            }
            try {
                Selector selector = parseSelector(st);
                if (st.hasMoreTokens()) {
                    throw new IllegalArgumentException("selector expression has trailing characters starting with " + st.nextToken());
                }
                return selector;
            } catch (NoSuchElementException nsee) {
                throw new IllegalArgumentException("error parsing selector expression \"" + expression + "\": " + nsee.getMessage());
            }
        }

        private static Selector parseSelector(StringTokenizer parser)  throws NoSuchElementException {
            String operator = parser.nextToken();
            if (operator.equals(":pfx")) {
                String token = parseToken(parser);
                return new PathPrefixSelector(token);
            } else if (operator.equals(":sfx")) {
                String token = parseToken(parser);
                return new SuffixSelector(token);
            } else if (operator.equals(":sub")) {
                String token = parseToken(parser);
                return new SubstringSelector(token);
            } else if (operator.equals(":name")) {
                String token = parseToken(parser);
                return new NameSelector(token);
            } else if (operator.equals(":and")) {
                Selector left = parseSelector(parser);
                Selector right = parseSelector(parser);
                return new AndSelector(left, right);
            } else if (operator.equals(":or")) {
                Selector left = parseSelector(parser);
                Selector right = parseSelector(parser);
                return new OrSelector(left, right);
            } else if (operator.equals(":ptn")) {
                String token = parseToken(parser);
                String matchNameOnly = parseToken(parser);
                return new PatternSelector(token, Boolean.valueOf(matchNameOnly).booleanValue());
            } else {
                throw new NoSuchElementException("unknown selector operator: " + operator);
            }
        }

        private static String parseToken(StringTokenizer parser) {
            String token = parser.nextToken();
            if (token.charAt(0) == ':') {
                throw new NoSuchElementException("expected token, not selector operator");
            }
            if (token.charAt(0) == '\\') {
                return token.substring(1);
            } else {
                return token;
            }

        }
    }

    /**
     * The base directory for finding files.
     */
    private final File baseDir;

    /**
     * The matching criteria.
     */
    private final Selector selector;

    /**
     * Creates a FileSet.
     *
     * @param baseDir   the base directory for finding files in the set.
     * @param selector  the selector for matching files in the set if all files in the directory are to be matched
     */
    public FileSet(File baseDir, Selector selector) {
        this.baseDir = baseDir;
        this.selector = selector;
    }

    /**
     * Creates a FileSet.
     *
     * @param baseDir    the base directory for finding files in the set.
     * @param expression an expression that is {@link SelectionExpressionParser parsed} to create a Selector
     */
    public FileSet(File baseDir, String expression) {
        this.baseDir = baseDir;
        this.selector = SelectorExpressionParser.parse(expression);
    }

    /**
     * Gets the base directory for files in this set.
     *
     * @return the base directory for files in this set
     */
    public File getBaseDir() {
        return baseDir;
    }

    /**
     * Gets the selector for matching files in the set if all files in the directory are to be matched
     *
     * @return the selector for matching files in the set if all files in the directory are to be matched
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * Replaces the base directory of a file in this FileSet.
     *
     * @param file        a file that was returned during an iteration over this FileSet
     * @param newBaseDir  the directory to replace the base dir of <code>file</code>
     * @return a new file in <code>newBaseDir</code> whose relative path is the same as it was to {@link #baseDir}
     */
    public File replaceBaseDir(File file, File newBaseDir) {
        assert file.getPath().startsWith(baseDir.getPath());
        String relativePath = getRelativePath(file);
        return new File(newBaseDir, relativePath);
    }

    /**
     * Given a file and a directory, returns the path of the file relative to the directory.
     *
     * @param file a file or directory
     * @param dir  a directory containing <code>file</code>
     * @return the path of <code>file</code> relative to <code>dir</code>
     * @throws IllegalArgumentException if <code>dir</code>'s path is not a component of <code>file</code>'s path
     */
    public static String getRelativePath(File dir, File file) {
        if (!file.getPath().startsWith(dir.getPath())) {
            throw new IllegalArgumentException(file + " is not in " + dir);
        }
        if (file.equals(dir)) {
            return "";
        }
        return file.getPath().substring(dir.getPath().length() + File.separator.length());
    }

    /**
     * Returns the path of a file relative to the base directory of this file set.
     *
     * @param file        a file that was returned during an iteration over this FileSet
     * @return the path of <code>file</code> relative to the base directory of this file set
     */
    public String getRelativePath(File file) {
        return getRelativePath(baseDir, file);
    }

    /**
     * Gets the files currently in the set as a list.
     *
     * @return the files in the set
     */
    public List<File> list() {
        final ArrayList<File> list = new ArrayList<File>();
        new FileVisitor() {
            public boolean visit(File file) {
                if (!file.isDirectory()) {
                    if (selector == null || selector.isSelected(file)) {
                        list.add(file);
                    }
                }
                return true;
            }
        }.run(baseDir);
        return list;
    }

    /**
     * Gets the files currently in the set as a list.
     *
     * @return the files in the set
     */
    public List<String> listStrings() {
        final ArrayList<String> list = new ArrayList<String>();
        new FileVisitor() {
            public boolean visit(File file) {
                if (!file.isDirectory()) {
                    if (selector == null || selector.isSelected(file)) {
                        list.add(file.toString());
                    }
                }
                return true;
            }
        }.run(baseDir);
        return list;
    }

    /**
     * Tests harness.
     *
     * @param args  the command line args where args[0] is the directory to search from and the
     *              remaining elements form a {@link SelectorExpressionParser selection expression}
     */
    public static void main(String[] args) {
        String baseDir = args[0];
        StringBuffer buf = new StringBuffer();
        for (int i = 1; i < args.length; i++) {
            buf.append(args[i]).append(' ');
        }

        FileSet fs;
        if (buf.length() != 0) {
            fs = new FileSet(new File(baseDir), buf.toString());
        } else {
            fs = new FileSet(new File(baseDir), (Selector)null);
        }
        for (File file : fs.list()) {
            System.out.println(file.getPath());
        }
    }
}
