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

import java.io.*;
import java.util.*;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.*;
import javax.microedition.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import com.sun.squawk.io.connections.*;
import com.sun.squawk.util.*;
import com.sun.squawk.util.StringTokenizer;
import com.sun.squawk.vm.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * Show a tree based, foldable representation of a VM trace. A trace may include
 * trace lines and non-trace lines.
 *
 */
public class TraceViewer extends JFrame implements WindowListener, ComponentListener {

    /**
     * The map from addresses to symbolic information.
     */
    private Symbols symbols = new Symbols();

    /**
     * The JTree component used to render the trace as a tree.
     */
    private TraceTree tree;

    /**
     * The root node in the trace's data model.
     */
    private TraceNode root;

    /**
     * The model for the stack values of the currently selected instruction node.
     */
    private ValuesPanel stackPanel;

    /**
     * The model for the local variable values of the currently selected instruction node.
     */
    private ValuesPanel localsPanel;

    /**
     * The search path for finding and opening source files.
     */
    private ClasspathConnection sourcePath;

    /**
     * A cache of JTextAreas for previously viewed source files.
     */
    private HashMap<String, JTextArea> sourceFileCache;

    /**
     * Specifies if the trace is from the execution of a 64 bit VM.
     */
    private boolean is64Bit;

    /**
     * Identifier for the service thread.
     */
    private static final String SERVICE_THREAD_ID = "0";

    /**
     * Pads a given string buffer with spaces until its length is equal to the length
     * of a given array of space characters.
     *
     * @param buf     the buffer to pad
     * @param spaces  the spaces to pad with
     */
    public static void pad(StringBuffer buf, char[] spaces) {
        int diff = spaces.length - buf.length();
        if (diff > 0) {
            buf.append(spaces, 0, diff);
        }
    }

    /**
     * Constructs a TraceViewer.
     */
    private TraceViewer() {
        root = new TraceNode(0) {
            public int getCallDepth() {
                return -2;
            }
        };
        sourceFileCache = new HashMap<String, JTextArea>();
    }

    /**
     * Creates and returns an AddressRelocator based on the first line of the trace.
     *
     * @param line   the first line of the trace
     * @return the created AddressRelocator
     */
    private AddressRelocator createAddressRelocator(String line) {
        Matcher m = Pattern.compile("\\*TRACE\\*:\\*ROM\\*:(\\d+):(\\d+):\\*NVM\\*:(\\d+):(\\d+):\\*(\\d+)\\*").matcher(line);
        if (!m.matches()) {
            throw new TraceParseException(line, m.pattern().pattern());
        }

        long romStart = Long.parseLong(m.group(1));
        long romEnd   = Long.parseLong(m.group(2));
        long nvmStart = Long.parseLong(m.group(3));
        long nvmEnd   = Long.parseLong(m.group(4));
        is64Bit = m.group(5).equals("64");

        return new AddressRelocator(romStart, (int)(romEnd - romStart), nvmStart, (int)(nvmEnd - nvmStart));
    }

    /**
     * Gets the ordinal position of the last slice of a thread identified by a given ID.
     *
     * @param threadID  a numerical thread ID
     * @return the ordinal position of the last slice of the thread identified by <code>threadID</code>
     */
    private int getLastSliceForThread(String threadID) {

        int count = root.getChildCount();
        while (--count >= 0) {
            TraceNode child = (TraceNode)root.getChildAt(count);
            if (child instanceof ThreadSliceNode) {
                ThreadSliceNode thread = (ThreadSliceNode)child;
                if (thread.getThreadID().equals(threadID)) {
                    return thread.slice;
                }
            }
        }
        return -1;
    }

    /**
     * Pattern for matching a line that starts a non-profile stack trace.
     * Capturing group 1 is the backward branch count at which the sample was taken and group 2
     * is the message describing the point of execution.
     */
    public static Pattern STACK_TRACE_START = Pattern.compile("\\*STACKTRACESTART\\*:(-?\\d+):(.*)");

    /**
     * Attempts to match a given line against the {@link #STACK_TRACE_START} pattern.
     *
     * @param  line  the line to match
     * @return the matcher used for a successful match or null
     */
    private Matcher matchesStackTraceStart(String line) {
        Matcher m = STACK_TRACE_START.matcher(line);
        if (m.matches()) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * Attempts to match a given line against the {@link ProfileViewer#STACK_TRACE_START} pattern.
     *
     * @param  line  the line to match
     * @return the matcher used for a successful match or null
     */
    private Matcher matchesProfileStackTraceStart(String line) {
        Matcher m = ProfileViewer.STACK_TRACE_START.matcher(line);
        if (m.matches()) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * Attempts to match a given line against the {@link ProfileViewer#STACK_TRACE_ELEMENT} pattern.
     *
     * @param  line  the line to match
     * @return the matcher used for a successful match or null
     */
    private Matcher matchesStackTraceElement(String line) {
        Matcher m = ProfileViewer.STACK_TRACE_ELEMENT.matcher(line);
        if (m.matches()) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * Attempts to match a given line against the {@link ProfileViewer#STACK_TRACE_REPETITION_ELEMENT} pattern.
     *
     * @param  line  the line to match
     * @return the matcher used for a successful match or null
     */
    private Matcher matchesStackTraceRepeitionElement(String line) {
        Matcher m = ProfileViewer.STACK_TRACE_REPETITION_ELEMENT.matcher(line);
        if (m.matches()) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * Attempts to match a given line against the {@link ProfileViewer#STACK_TRACE_END} pattern.
     *
     * @param  line  the line to match
     * @return the matcher used for a successful match or null
     */
    private Matcher matchesStackTraceEnd(String line) {
        Matcher m = ProfileViewer.STACK_TRACE_END.matcher(line);
        if (m.matches()) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * Regular expression matching a stack trace line. The captured groups are:
     *   1 - the fully qualified method name (e.g. "java.lang.Object.wait")
     *   2 - the source file name (e.g. "Object.java")
     *   3 - the source line number (e.g. "234")
     */
    public static final Pattern JAVA_STACK_TRACE_ELEMENT = Pattern.compile("([A-Za-z_][A-Za-z0-9_\\.\\$]*)\\((.*\\.java):([1-9][0-9]*)\\)");

    /**
     * Attempts to match a given line against the {@link #JAVA_STACK_TRACE_ELEMENT} pattern.
     *
     * @param  line  the line to match
     * @return the matcher used for a successful match or null
     */
    private Matcher matchesJavaStackTraceElement(String line) {
        Matcher m = JAVA_STACK_TRACE_ELEMENT.matcher(line.trim());
        if (m.matches()) {
            return m;
        } else {
            return null;
        }
    }

    static LongHashtable unknownMethods = new LongHashtable();
    static Symbols.Method lookupMethod(Symbols symbols, long address) {
        Symbols.Method method;
        try {
            method = symbols.lookupMethod(address);
        } catch (Symbols.UnknownMethodException e) {
            method = (Symbols.Method)unknownMethods.get(address);
            if (method == null) {
                method = new Symbols.Method();
                method.setFilePath("<unknown>");
                method.setSignature("<unknown> <unknown>." + address + "()");
                method.setLineNumberTable("0 -1");
                unknownMethods.put(address, method);
            }
        }
        return method;
    }

    /**
     * Parses a single line from the trace and update the model accordingly.
     *
     * @param line               the line to parse
     * @param currentThread      the current thread
     * @param showServiceThread  specifies if the service thread should be included
     * @return the current thread
     */
    private ThreadSliceNode parseTraceLine(String line, int tracePosition, ThreadSliceNode currentThread, boolean showServiceThread) {

        Matcher m;

        // Most common case first
        if (line.startsWith("*TRACE*:")) {
            TraceLine trace = new TraceLine(line.substring("*TRACE*:".length()), symbols);
            if (currentThread == null) {
                Assert.that(trace.threadID.equals(SERVICE_THREAD_ID));
                return currentThread;
            }

            if (!showServiceThread && trace.threadID.equals(SERVICE_THREAD_ID)) {
                return currentThread;
            }

            MethodNode currentMethod = currentThread.getCurrentMethod();
            if (currentMethod == null) {
                Assert.that(trace.depth == 0);
                currentMethod = currentThread.enterMethod(trace.method, 0, tracePosition);
            } else {
                if (currentMethod.depth > trace.depth) {
                    currentMethod = currentThread.exitToMethod(trace.depth, trace.method);
                } else if (currentMethod.depth < trace.depth) {
                    currentMethod = currentThread.enterMethod(trace.method, trace.depth, tracePosition);
                }
            }

            Assert.that(currentMethod.getMethod() == trace.method && currentMethod.depth == trace.depth);
            currentMethod.add(new InstructionNode(trace, tracePosition));
            return currentThread;
        }

        // Now look for a thread switch line
        if (line.startsWith("*THREADSWITCH*:")) {

            // strip prefix
            line = line.substring("*THREADSWITCH*:".length());

            int index = line.indexOf(':');
            String threadID;
            String stackTrace;
            if (index != -1) {
                threadID = line.substring(0, index);
                stackTrace = line.substring(index + 1);
            } else {
                threadID = line;
                stackTrace = null;
            }

            if (!showServiceThread && threadID.equals(SERVICE_THREAD_ID) ||
                (currentThread != null && currentThread.getThreadID().equals(threadID)))
            {
                return currentThread;
            }

            int slice = getLastSliceForThread(threadID) + 1;
            currentThread = new ThreadSliceNode(threadID, slice, stackTrace, symbols, tracePosition);
            root.add(currentThread);
            return currentThread;
        }

        // Find the right node to attach all other trace lines to
        TraceNode currentNode = root;
        if (currentThread != null) {
            currentNode = currentThread.getCurrentMethod();
            Assert.that(currentNode != null);
        }


        // Match *STACKTRACESTART*...
        if ((m = matchesStackTraceStart(line)) != null || (m = matchesProfileStackTraceStart(line)) != null) {
            currentNode.add(new StackTraceStartNode(m.group(2) + " (bcount=" + m.group(1) + ")", tracePosition));
            return currentThread;
        }

        // Match *STACKTRACE*:<n>:<n>...
        if ((m = matchesStackTraceElement(line)) != null) {
            Symbols.Method method = TraceViewer.lookupMethod(symbols, Long.parseLong(m.group(1)));
            int pc = Integer.parseInt(m.group(2));
            if (pc > 0) {
                // The PC has been advanced to the end of the current instruction so rewinding
                // it back by one should mean that the source line for the current instruction
                // will be correctly retrieved
                pc--;
            }
            ExecutionPoint ep = new ExecutionPoint(method, method.getSourceLineNumber(pc), pc);
            currentNode.add(new StackTraceNode(ep, tracePosition));
            return currentThread;
        }

        // Match *STACKTRACE*:"<n>
        if ((m = matchesStackTraceRepeitionElement(line)) != null) {
            TreeNode node = currentNode.getLastLeaf();
            if (node instanceof StackTraceNode) {
                StackTraceNode stn = (StackTraceNode)node;
                int count = Integer.parseInt(m.group(1));
                while (count-- >= 0) {
                    currentNode.add(new StackTraceNode(stn.getExecutionPoint(), tracePosition));
                }
            } else {
                System.err.println("line " + tracePosition + ": stack trace repetition line follows non-stack trace line: " + line);
            }
            return currentThread;
        }

        // Match *STACKTRACEEND*
        if ((m = matchesStackTraceEnd(line)) != null) {
            return currentThread;
        }

        // Match a line from a Java stack trace dump
        if ((m = matchesJavaStackTraceElement(line)) != null) {
            String name = m.group(1);
            String fileName = m.group(2);
            String lineNumber = m.group(3);

            Symbols.Method method = new Symbols.Method();
            method.setSignature("void " + name + "()");


            // Strip off class name and method name
            File path = new File(name.replace('.', File.separatorChar));
            path = path.getParentFile().getParentFile();
            method.setFilePath(path.getPath() + File.separatorChar + ".java");

            ExecutionPoint ep = new ExecutionPoint(method, Integer.parseInt(lineNumber), -1);
            currentNode.add(new StackTraceNode(ep, tracePosition));
            return currentThread;

        }

        // Catch all for any other lines
System.err.println("other node: " + line);
        currentNode.add(new OtherTraceNode(line, tracePosition));
        return currentThread;
    }

    /**
     * Reads the trace from a file and builds the corresponding trace model.
     *
     * @param file   the file containing the trace
     */
    private void readTrace(File file, boolean showServiceThread) {
        InputFile in = new InputFile(file);

        // Create a relocator from the first line and use it to relocate any canonical
        // address in the symbols
        AddressRelocator relocator = createAddressRelocator(in.readLine());
        symbols.relocate(relocator, false);

        // Read the remainder of the trace
        String line;
        ThreadSliceNode currentThread = null;
        while ((line = in.readLine()) != null) {
            try {
                currentThread = parseTraceLine(line, in.getLineNumber() - 1, currentThread, showServiceThread);
            } catch (TraceParseException tpe) {
                System.err.println(in.formatErrorMessage("error while reading trace - skipping the remainder"));
                tpe.printStackTrace();
            } catch (OutOfMemoryError ome) {
                throw new RuntimeException(in.formatErrorMessage("out of memory"));
            } catch (Symbols.UnknownMethodException e) {
                System.err.println("****** MUST USE '-verbose' VM SWITCH TO TRACE DYNAMICALLY LOADED METHODS *******");
                throw new RuntimeException(in.formatErrorMessage(e.toString()), e);
            } catch (RuntimeException e) {
                throw new RuntimeException(in.formatErrorMessage(e.toString()), e);
            }
        }
    }

    /**
     * Gets a JTextArea component holding the source file corresponding to a given path.
     *
     * @param path  the path to a source file
     * @return a JTextArea component showing the source file annotated with line numbers
     * @throws IOException if the was an error locating, opening or reading the source file
     */
    private JTextArea getSourceFile(String path) throws IOException {
        JTextArea text = (JTextArea)sourceFileCache.get(path);
        if (text == null) {
            text = new JTextArea();
            text.setEditable(false);
            text.setFont(new Font("monospaced", Font.PLAIN, 12));
            InputStream is = sourcePath.openInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer(is.available() * 2);
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                sb.append(lineNo++);
                sb.append(":\t");
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            text.setText(sb.toString());
            sourceFileCache.put(path, text);
        }
        return text;
    }

    /**
     * Prints the usage message.
     *
     * @param errMsg   an optional error message or null
     */
    private void usage(String errMsg) {
        PrintStream out = System.out;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("Usage: TraceViewer [-options] trace ");
        out.println("where options include:");
        out.println("    -map:<file>         map file containing method meta info");
        out.println("    -sp:<path>          where to find source files");
        out.println("    -noservice          exclude service thread trace");
        out.println("    -h                  show this message and exit");
        out.println();
    }

    /**
     * Parses the command line arguments and starts the trace viewer.
     *
     * @param args   command line arguments
     */
    private void run(String[] args) {
        Vector<String> symbolsToLoad = new Vector<String>();
        symbolsToLoad.addElement("squawk.sym");
        symbolsToLoad.addElement("squawk_dynamic.sym");

        String sourcePathStr = ".";
        boolean showServiceThread = true;

        int argc = 0;
        while (argc != args.length) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            } else if (arg.startsWith("-sp:")) {
                sourcePathStr = arg.substring("-sp:".length());
            } else if (arg.startsWith("-map:")) {
                symbolsToLoad.addElement(arg.substring("-map:".length()));
            } else if (arg.equals("-noservice")) {
                showServiceThread = false;
            } else if (arg.equals("-h")) {
                usage(null);
                return;
            } else {
                usage("Unknown option: " + arg);
                return;
            }
            argc++;
        }

        if (argc == args.length) {
            usage("Missing trace");
            return;
        }

        File traceFile = new File(args[argc]);

        // Load the symbols
        for (Enumeration e = symbolsToLoad.elements(); e.hasMoreElements();) {
            File symbolsFile = new File((String)e.nextElement());
            if (symbolsFile.exists() && traceFile.exists() && symbolsFile.lastModified() > traceFile.lastModified()) {
                System.err.println("warning: " + traceFile + " is older than " + symbolsFile + " - may be out of sync");
            }
            symbols.loadIfFileExists(symbolsFile);
        }

        // Initialize the source path
        try {
            sourcePathStr = sourcePathStr.replace(':', File.pathSeparatorChar);
            this.sourcePath = (ClasspathConnection)Connector.open("classpath://" + sourcePathStr);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        readTrace(traceFile, showServiceThread);
        initializeUI();
    }

    /**
     * Command line entry point.
     *
     * @param args  command line arguments
     */
    public static void main(String[] args) {
        TraceViewer viewer = new TraceViewer();
        viewer.run(args);
    }

    /*---------------------------------------------------------------------------*\
     *                                  GUI                                      *
    \*---------------------------------------------------------------------------*/

    /**
     * WindowListener implementation.
     */
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
    public void windowClosed(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    /**
     * ComponentListener implementation.
     */
    public void componentHidden(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentResized(ComponentEvent e) {
        validate();
    }

    /**
     * The prefix to be used for the application GUI frame.
     */
    private static final String FRAME_TITLE_PREFIX = "Squawk trace viewer";

    /**
     * The tree selection listener that updates the source pane when a new node is selected.
     */
    class SourcePaneUpdater implements TreeSelectionListener {

        private final JScrollPane sourcePane;
        private JTextArea currentTextArea;
        private int lineNo = -1;

        SourcePaneUpdater(JScrollPane sourcePane) {
            this.sourcePane = sourcePane;
        }

        /**
         * {@inheritDoc}
         */
        public void valueChanged(TreeSelectionEvent e) {

            // Get the selected node
            TraceNode node = (TraceNode)tree.getLastSelectedPathComponent();

            // Source cannot be shown when there is no selected node
            if (node == null) {
                return;
            }

            ExecutionPoint ep = node.getExecutionPoint();
            if (ep == null) {
                setTitle(FRAME_TITLE_PREFIX + " - ?? [input trace line = " + node.tracePosition + "]");
                return;
            }

            // Don't show source for methods that are collapsed and have children.
            if (node instanceof MethodNode) {
                MethodNode methodNode = (MethodNode)node;
                if (methodNode.computeNestedInstructionCount() != 0 && tree.isCollapsed(new TreePath(node.getPath()))) {
                    return;
                }
            } else if (node instanceof InstructionNode) {
                InstructionNode instructionNode = (InstructionNode)node;
                TraceLine.Words stack = instructionNode.getTraceLine().stack;
                TraceLine.Words locals = instructionNode.getTraceLine().locals;
                stackPanel.tableModel.updateData(stack);
                localsPanel.tableModel.updateData(locals);
            }

            String path = ep.method.getFilePath();
            JTextArea text;
            try {
                text = getSourceFile(path);
            } catch (IOException ioe) {
                setTitle(FRAME_TITLE_PREFIX + " - ??/" + path + "  [input trace line = " + node.tracePosition + "]");
                sourcePane.getViewport().setView(new JTextArea("An exception occurred while reading "+path+":\n\t"+ioe));
                currentTextArea = null;
                return;
            }

            if (currentTextArea != text) {
                sourcePane.getViewport().setView(text);
                currentTextArea = text;
                lineNo = -1;
            }


            if (lineNo == ep.lineNumber) {
                return;
            }

            // Only now do we have to update the current line in the source pane
            lineNo = ep.lineNumber;
            try {
                if ((lineNo - 1) < 0) {
                    return;
                }
                final int startPos = text.getLineStartOffset(lineNo - 1);
                final int endPos   = text.getLineEndOffset(lineNo - 1);
                text.setCaretPosition(endPos);
                text.moveCaretPosition(startPos);
                text.getCaret().setSelectionVisible(true);

                setTitle(FRAME_TITLE_PREFIX + " - " + path + ":" + lineNo + "  [input trace line = " + node.tracePosition + "]");

                final JTextArea textArea = text;

                // Scroll so that the highlighted text is in the center
                // if is not already visible on the screen. The delayed
                // invocation is necessary as the view for the text
                // area will not have been computed yet.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // Compute the desired area of the text to show.
                        Rectangle textScrollRect = new Rectangle();
                        textScrollRect.y = (lineNo - 1) * textArea.getFontMetrics(textArea.getFont()).getHeight();
                        Rectangle visible = textArea.getVisibleRect();

                        textScrollRect.height = visible.height;
                        textScrollRect.y -= (visible.height >> 1);

                        // Compute the upper and lower bounds of what
                        // is acceptable.
                        int upper = visible.y + (visible.height >> 2);
                        int lower = visible.y - (visible.height >> 1);

                        // See if we really should scroll the text area.
                        if ((textScrollRect.y < lower) ||
                            (textScrollRect.y > upper)) {
                            // Check that we're not scrolling past the
                            // end of the text.
                            int newbottom = textScrollRect.y +
                                textScrollRect.height;
                            int textheight = textArea.getHeight();
                            if (newbottom > textheight) {
                                textScrollRect.y -= (newbottom - textheight);
                            }
                            // Perform the text area scroll.
                            textArea.scrollRectToVisible(textScrollRect);
                        }
                    }
                });

            } catch (BadLocationException ble) {
                ble.printStackTrace();
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }
    }

    /**
     * Scrolls the pane containing the tree so that a node identified by a given path is visible.
     *
     * @param path   a tree path identifying a node
     */
    void scrollToNode(final TreePath path) {
        tree.scrollPathToVisible(path);
    }

    /**
     * Initializes the UI of the TraceViewer. This must be called only after the
     * data in the tree is complete.
     */
    private void initializeUI() {

        final DefaultTreeModel model = new DefaultTreeModel(root);
        tree = new TraceTree(model);

        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TraceTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setScrollsOnExpand(false);
        tree.setRootVisible(false);

        // Prevents ellipsis on tree nodes when choosing a format that grows the text size
        if (System.getProperty("os.name").indexOf("inux") == -1) {
            // Large model slows down scrolling through the tree on linux
            tree.setLargeModel(true);
        }

        // Enable tool tips.
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(500);
        ttm.registerComponent(tree);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Put the tree in a scrollable pane
        JScrollPane treeScrollPane = new JScrollPane(tree);

        // Create the panel showing the operand stack values
        stackPanel = new ValuesPanel("Operand stack slots");

        // Create the panel showing the local variable values
        localsPanel = new ValuesPanel("Local variables");

        // Create the panes for the values and the tree
        JSplitPane stackAndLocalsPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stackPanel, localsPanel);
        JSplitPane traceDataPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stackAndLocalsPane, treeScrollPane);

        // Place holder until a node is selected
        JPanel noSourcePanel = new JPanel(new GridBagLayout());
        noSourcePanel.add(new JLabel("No source file selected/available"));
        final JScrollPane sourceView = new JScrollPane(noSourcePanel);

        // Prevent the scroll panel from jumping left when scrolling down to a node
        // that doesn fit on the screen
        sourceView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Create the source pane updater
        tree.addTreeSelectionListener(new SourcePaneUpdater(sourceView));

        // Create search panel
        SearchPanel searchPanel = new SearchPanel();
        mainPanel.add("North", searchPanel);

        // Create the split pane for the tree and source panes
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, traceDataPane, sourceView);
        splitPane.setDividerLocation(500);
        mainPanel.add("Center", splitPane);

        setTitle(FRAME_TITLE_PREFIX);

        // Maximize the window
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        mainPanel.setPreferredSize(dimension);
        getContentPane().add(mainPanel);

        addWindowListener(this);
        addComponentListener(this);
        pack();

        // Add a tree expansion listener that will modify what is expanded if SHIFT is pressed
        tree.addTreeExpansionListener(new ModifiedTreeExpansionAdapter(InputEvent.SHIFT_MASK) {
            public void treeExpanded(TreeExpansionEvent e, boolean modified) {
                TreePath path = e.getPath();

                // If the expansion occurred with a mouse click while SHIFT was pressed,
                // the complete path from the expanded node to its last leaf is expanded
                if (modified) {

                    TraceNode node = (TraceNode)path.getLastPathComponent();
                    if (!node.isLeaf()) {
                        TraceNode last = node.expandInterestingPath(tree);
                        path = new TreePath(last.getPath());
                    }
                    tree.setSelectionPath(path);
                    scrollToNode(path);
                }
            }
        });

        // Expand the 'interesting' path in last thread slice
        if (root.getChildCount() != 0) {
            TraceNode lastSlice = (TraceNode)root.getLastChild();
            TraceNode lastPointOfExecution = lastSlice.expandInterestingPath(tree);

            // Set the selection at the last point of execution
            if (lastPointOfExecution != null) {
//System.err.println("Setting last path of execution to " + lastPointOfExecution);
                TreePath path = new TreePath(lastPointOfExecution.getPath());
                tree.expandPath(path);
                tree.setSelectionPath(path);
                scrollToNode(path);
            }
        }

        // Now resize the top split pane
        int width = Math.max(stackPanel.adjustColumnWidths(), localsPanel.adjustColumnWidths());
        stackAndLocalsPane.setPreferredSize(new Dimension(width, stackAndLocalsPane.getPreferredSize().height));

        traceDataPane.setResizeWeight(0.1d);
        traceDataPane.resetToPreferredSizes();

        stackAndLocalsPane.setResizeWeight(0.33d);
        stackAndLocalsPane.resetToPreferredSizes();

        // Display the GUI
        setVisible(true);
    }

    /*---------------------------------------------------------------------------*\
     *                            Values panel                                  *
    \*---------------------------------------------------------------------------*/

    /**
     * A panel containing a table of values derived from a {@link TraceLine.Words} object.
     */
    final class ValuesPanel extends JPanel {

        /**
         * Provides a JTable model based on a {@link TraceLine.Words} object.
         */
        class ValuesTableModel extends AbstractTableModel {

            /**
             * {@inheritDoc}
             */
            public String getColumnName(int column) {
                return column == 0 ? "Index" : "Value";
            }

            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }

            /**
             * The source for the model.
             */
            private TraceLine.Words data;

            /**
             * Creates a table model for a given set of word values.
             *
             * @param data   the word values used to populate the table
             */
            ValuesTableModel(TraceLine.Words data) {
                this.data = data;
            }

            /**
             * Updates the data source for this model. This will fire the appropriate model redrawing.
             *
             * @param data   the new data model
             */
            public void updateData(TraceLine.Words data) {
                Assert.that(data != null);
                if (data != this.data) {
                    this.data = data;
                    fireTableDataChanged();
                }
            }

            /**
             * {@inheritDoc}
             */
            public int getRowCount() {
                return data.getSize();
            }

            /**
             * {@inheritDoc}
             */
            public int getColumnCount() {
                return 2;
            }

            /**
             * {@inheritDoc}
             */
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.toString(rowIndex);
                } else {
                    Assert.that(columnIndex == 1);
                    long value = data.getValue(rowIndex);
                    if (is64Bit) {
                        switch (format) {
                            case FORMAT_UNSIGNED:
                               if (value < 0) {
                                   String asHex = Long.toHexString(value);
                                   return new java.math.BigInteger(asHex, 16).toString();
                               } else {
                                   return Long.toString(value);
                               }
                            case FORMAT_SIGNED:
                                return Long.toString(value);
                            case FORMAT_HEX:
                                return "0x" + Long.toHexString(value).toUpperCase();
                            case FORMAT_BINARY:
                                if (value < 0) {
                                    String asHex = Long.toHexString(value);
                                    return new java.math.BigInteger(asHex, 16).toString(2);
                                } else {
                                    return Long.toString(value, 2);
                                }
                        }
                    } else {
                        switch (format) {
                            case FORMAT_UNSIGNED:
                                return Long.toString(value & 0xFFFFFFFFL);
                            case FORMAT_SIGNED:
                                return Integer.toString((int)value);
                            case FORMAT_HEX:
                                return "0x" + Integer.toHexString((int)value).toUpperCase();
                            case FORMAT_BINARY:
                                return Long.toString(value & 0xFFFFFFFFL, 2);
                        }
                    }
                    Assert.shouldNotReachHere();
                    return null;
                }
            }
        }

        /**
         * The model for the table. This is the object that will be messaged when a new
         * instruction tree node is selected.
         */
        public final ValuesTableModel tableModel;

        /**
         * The JTable displaying the values.
         */
        private final JTable table;

        /**
         * The format to use when converting the values to a string.
         */
        int format;

        static final int FORMAT_UNSIGNED = 0;
        static final int FORMAT_SIGNED = 1;
        static final int FORMAT_HEX = 2;
        static final int FORMAT_BINARY = 3;


        /**
         * Creates the search panel.
         *
         * @param tree  the JTree that will be searched
         */
        ValuesPanel(String name) {

            setLayout(new BorderLayout());

            // Add title in north pane
            JLabel title = new JLabel(name, JLabel.CENTER);
            title.setForeground(Color.RED);
            add("North", title);

            tableModel = new ValuesTableModel(TraceLine.Words.ZERO_WORDS);
            table = new JTable(tableModel);
            table.setShowGrid(true);

            JScrollPane tableScrollPane = new JScrollPane(table);
            add("Center", tableScrollPane);

            // Format
            final JRadioButton unsigned = new JRadioButton("Unsigned", true);
            final JRadioButton signed = new JRadioButton("Signed", false);
            final JRadioButton hex = new JRadioButton("Hex", false);
            final JRadioButton binary = new JRadioButton("Binary", false);
            final ButtonGroup group = new ButtonGroup();

            group.add(unsigned);
            group.add(signed);
            group.add(hex);
            group.add(binary);

            JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            formatPanel.add(unsigned);
            formatPanel.add(signed);
            formatPanel.add(hex);
            formatPanel.add(binary);

            add("South", formatPanel);

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == unsigned) {
                        format = FORMAT_UNSIGNED;
                    } else if (e.getSource() == signed) {
                        format = FORMAT_SIGNED;
                    } else if (e.getSource() == hex) {
                        format = FORMAT_HEX;
                    } else if (e.getSource() == binary) {
                        format = FORMAT_BINARY;
                    }
//System.err.println("format="+format);
                    tableModel.fireTableDataChanged();
                }
            };

            unsigned.addActionListener(actionListener);
            signed.addActionListener(actionListener);
            hex.addActionListener(actionListener);
            binary.addActionListener(actionListener);

        }

        /**
         * Adjust the (preferred) widths of the columns in a values table.
         *
         * @return  the total of all the widths
         */
        public int adjustColumnWidths() {

            // Configure the index column
            TableColumn column = table.getColumnModel().getColumn(0);
            Component component = table.getDefaultRenderer(String.class).getTableCellRendererComponent(table, "100", false, false, 0, 1);
            int width1 = component.getPreferredSize().width;
            column.setPreferredWidth(width1);

            // Configure the 'Value' column
            column = table.getColumnModel().getColumn(1);
            String maxValue = is64Bit ? Long.toHexString(Long.MAX_VALUE) : Integer.toHexString(Integer.MAX_VALUE);
            component = table.getDefaultRenderer(String.class).getTableCellRendererComponent(table, maxValue, false, false, 0, 1);
            int width2 = component.getPreferredSize().width;
            column.setPreferredWidth(width2);

            return width1 + width2;
        }
    }


    /*---------------------------------------------------------------------------*\
     *                            Searching                                      *
    \*---------------------------------------------------------------------------*/

    /**
     * A Search instance encapsulate the criteria for performing a search for a node.
     */
    abstract class Search {

        /**
         * Specifies if the search should start from the top of the tree.
         */
        public final boolean searchFromTop;

        /**
         * Specifies if the search should be in reverse.
         */
        public final boolean backwards;

        /**
         * Creates a search object.
         *
         * @param searchFromTop boolean
         * @param backwards boolean
         */
        Search(boolean searchFromTop, boolean backwards) {
            this.searchFromTop = searchFromTop;
            this.backwards = backwards;
        }

        /**
         * Determines if a given node's label is matched by this search.
         *
         * @param label  the label to test
         * @return true if <code>label</code> is matched by this search
         */
        public abstract boolean matches(String label);

        /**
         * Execute the search.
         */
        public final void execute() {

            TraceNode node;
            if (searchFromTop) {
                node = (TraceNode)tree.getModel().getRoot();
            } else {
                node = (TraceNode)tree.getLastSelectedPathComponent();
                node = (TraceNode)(backwards ? node.getPreviousNode() : node.getNextNode());
            }

            // Do the search
            while (node != null) {
                if (node.search(this)) {
                    break;
                }
                node = (TraceNode)(backwards ? node.getPreviousNode() : node.getNextNode());
            }

            if (node == null) {
                JOptionPane.showMessageDialog(null, "Finished searching trace");
            } else {
                TreePath path = new TreePath(node.getPath());
                scrollToNode(path);
                tree.setSelectionPath(path);
            }
        }
    }

    /**
     * A JPanel containing components that enable a JTree to be searched.
     */
    final class SearchPanel extends JPanel {

        /**
         * Creates the search panel.
         */
        SearchPanel() {

            FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
            setLayout(layout);

            // Search text panel
            final JTextField textToFind = new JTextField(30);
            add(new JLabel("Find: "));
            add(textToFind);

            // Options
            final JCheckBox searchFromTop = new JCheckBox("Search from top of tree");
            final JCheckBox regex = new JCheckBox("Regular expression");
            final JCheckBox caseSensitive = new JCheckBox("Match case");
            final JCheckBox backward = new JCheckBox("Backward");

            add(searchFromTop);
            add(regex);
            add(caseSensitive);
            add(backward);

            // Buttons
            final JButton find = new JButton("Find");
            add(find);

            // Actions for buttons
            ActionListener buttonListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Search search;
                    if (regex.isSelected()) {
                        search = new Search(searchFromTop.isSelected(), backward.isSelected()) {
                            private final Pattern pattern = Pattern.compile(textToFind.getText(), caseSensitive.isSelected() ? 0 : Pattern.CASE_INSENSITIVE);
                            public boolean matches(String label) {
                                return pattern.matcher(label).matches();
                            }
                        };
                    } else {
                        search = new Search(searchFromTop.isSelected(), backward.isSelected()) {
                            private final String pattern = caseSensitive.isSelected() ? textToFind.getText() : textToFind.getText().toUpperCase();
                            public boolean matches(String label) {
                                if (!caseSensitive.isSelected()) {
                                    label = label.toUpperCase();
                                }
                                return label.indexOf(pattern) != -1;
                            }
                        };
                    }
                    search.execute();
                }
            };
            find.addActionListener(buttonListener);
            textToFind.addActionListener(buttonListener);
        }
    }


    /*---------------------------------------------------------------------------*\
     *                            Tree node types                                *
    \*---------------------------------------------------------------------------*/

    /**
     * The base type for all nodes in a trace viewer tree.
     */
    abstract static class TraceNode extends DefaultMutableTreeNode {

        public final int tracePosition;

        /**
         *
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         */
        TraceNode(int tracePosition) {
            this.tracePosition = tracePosition;
        }

        /**
         * Overrides parent so that a stack trace element node is added under a stack trace entry node if possible.
         *
         * @param newChild  the child being added
         */
        public void add(MutableTreeNode newChild) {
            if (newChild instanceof StackTraceNode) {
                if (getChildCount() > 0) {
                    TraceNode lastChild = (TraceNode)getLastChild();
                    if (lastChild instanceof StackTraceStartNode) {
                        lastChild.add(newChild);
                        return;
                    }
                }
            }
            super.add(newChild);
        }

        /**
         * Configures a given renderer used to draw this node in a JTree.
         *
         * @param renderer    the renderer to configure
         * @param isExpanded  specifies if the node is expanded
         */
        public void configureRenderer(TraceTreeCellRenderer renderer, boolean isExpanded) {
        }

        /**
         * Gets the String to be used as the label for this node in a JTree.
         *
         * @param isExpanded  specifies if the node is expanded
         * @return the label to display
         */
        public String getTreeLabel(boolean isExpanded) {
            return "";
        }

        /**
         * Applies a search to a given node and returns true if the given search object matched
         * any of this node's content.
         *
         * @param search   the search object
         * @return true if <code>search.match(...)</code> returned true for any of this node's content
         */
        public boolean search(Search search) {
            return false;
        }

        /**
         * Gets the object describing an execution point that this node corresponds to.
         *
         * @return an ExecutionPoint or null
         */
        public ExecutionPoint getExecutionPoint() {
            return null;
        }

        /**
         * Returns the number of levels between this node and the method method in the thread.
         * If this is the entry method in a thread, returns 0.
         *
         * @return  the number of levels above this node to the entry method
         */
        public int getCallDepth() {
            TraceNode tp = (TraceNode)getParent();
            return 1 + tp.getCallDepth();
        }

        /**
         * Expands the 'interesting' path of execution from this node. This is the path
         * from this node to it last leaf and includes
         * any exceptions that were thrown along the way.
         *
         * @param tree  the JTree in which to expand the path
         * @return the last instruction in the slice
         */
        public TraceNode expandInterestingPath(JTree tree) {
            TraceNode previous = null;
            if (getChildCount() != 0) {
                TraceNode node = (TraceNode)getFirstChild();
                Enumeration e = node.preorderEnumeration();
                if (e.hasMoreElements()) {
                    previous = (TraceNode)e.nextElement();
                    while (e.hasMoreElements()) {
                        node = (TraceNode)e.nextElement();
                        if (previous.isLeaf()) {
                            // If the previous node jumps back more than one frame (i.e. an
                            // exception throw), then expand it.
                            if ((previous.getCallDepth() - 1) > node.getCallDepth()) {
                                TraceNode tp = (TraceNode)previous.getParent();
                                tree.expandPath(new TreePath(tp.getPath()));
                            }
                        }
                        previous = node;
                    }

                    // Expand the last node
                    TraceNode tp = (TraceNode)previous.getParent();
                    tree.expandPath(new TreePath(tp.getPath()));
                }
                return previous;
            } else {
                return null;
            }
        }

    }

    /**
     * The pattern for a stack trace element.
     * Capturing group 1 is the method address and group 2 bytecode offset.
     */
    private static final Pattern STACKTRACE_ELEMENT = Pattern.compile("(\\d+)@(\\d+)");

    /**
     * Pattern for matching a line that is a repeat element in a stack trace.
     * Capturing group 1 is the repetition count.
     */
    private static final Pattern STACKTRACE_REPETITION_ELEMENT = Pattern.compile("\"(\\d+)");

    /**
     * A ThreadSliceNode encapsulates the trace data for an execution on a particular thread
     * before a switch to another thread occurred.
     */
    final static class ThreadSliceNode extends TraceNode {

        /**
         * The slice of the thread represented by this node.
         */
        public final int slice;

        /**
         * The current method.
         */
        private MethodNode currentMethod;

        /**
         * Creates a ThreadSliceNode.
         *
         * @param threadID   the ID of the thread
         * @param slice      the ordinal position of this slice with respect to all other slices for the thread
         * @param stackTrace the stack trace encapsulating the current call stack for the thread
         * @param symbols    the database of method symbols
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         */
        ThreadSliceNode(String threadID, int slice, String stackTrace, Symbols symbols, int tracePosition) {
            super(tracePosition);
            setUserObject(threadID);
            this.slice = slice;

            if (stackTrace != null) {
                initializeCallStack(stackTrace, symbols, tracePosition);
            }
        }

        /**
         * {@inheritDoc}
         */
        public int getCallDepth() {
            return -1;
        }

        /**
         * Gets the ID of the thread.
         *
         * @return the ID of the thread
         */
        public String getThreadID() {
            return (String)getUserObject();
        }

        /**
         * Gets the current insertion point for new methods or instructions.
         *
         * @return the current insertion point for new methods or instructions
         */
        public MethodNode getCurrentMethod() {
            return currentMethod;
        }

        /**
         * Initializes the call stack and current method of this slice based on a given stack trace.
         *
         * @param stackTrace the stack trace encapsulating the current call stack for the thread
         * @param symbols    the database of method symbols
         */
        private void initializeCallStack(String stackTrace, Symbols symbols, int tracePosition) {
            StringTokenizer st = new StringTokenizer(stackTrace, ":");
            Stack<String> stack = new Stack<String>();
            stack.ensureCapacity(st.countTokens());
            while (st.hasMoreTokens()) {
                stack.push(st.nextToken());
            }


            int repetition = 0;
            int depth = 0;
            while (!stack.empty()) {
                String element = (String)stack.pop();
                Matcher m = STACKTRACE_ELEMENT.matcher(element);

                if (m.matches()) {
                    Symbols.Method method = TraceViewer.lookupMethod(symbols, Long.parseLong(m.group(1)));
                    enterMethod(method, depth++, tracePosition);
                    while (repetition-- > 0) {
                        enterMethod(method, depth++, tracePosition);
                    }
                    continue;
                }

                m = STACKTRACE_REPETITION_ELEMENT.matcher(element);
                if (m.matches()) {
                    repetition = Integer.parseInt(m.group(1));
                    continue;
                }

                throw new TraceParseException(element, m.pattern().pattern());

            }
        }

        /**
         * Adds a node representing the entry to a new method from the current method.
         * The call depth of the new method frame must be one greater than the call depth of
         * the current method.
         *
         * @param method  the method called from the current method
         * @param depth   the depth of the new frame
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         * @return the new current method
         */
        public MethodNode enterMethod(Symbols.Method method, int depth, int tracePosition) {
            if (currentMethod == null) {
                Assert.that(depth == 0);
                currentMethod = new MethodNode(method, depth, tracePosition);
                add(currentMethod);
            } else {
                Assert.that(currentMethod.depth == depth - 1);
                MethodNode methodNode = new MethodNode(method, depth, tracePosition);
                currentMethod.add(methodNode);
                currentMethod = methodNode;
            }
            return currentMethod;
        }

        /**
         * Resets the current method to a method frame higher up on the current call stack.
         *
         * @param depth   the depth to which the call stack is unwound
         * @param method  the method that must be at the frame unwound to
         * @return the new current method
         */
        public MethodNode exitToMethod(int depth, Symbols.Method method) {
            Assert.that(currentMethod != null);
            Assert.that(currentMethod.depth > depth);

            currentMethod = (MethodNode)currentMethod.getParent();
            while (currentMethod.getMethod() != method || currentMethod.depth != depth) {
                currentMethod = (MethodNode)currentMethod.getParent();
                Assert.that(currentMethod != null);
            }
            return currentMethod;
        }

        /**
         * {@inheritDoc}
         */
        public void configureRenderer(TraceTreeCellRenderer renderer, boolean isExpanded) {
            renderer.setFont(renderer.plain);
        }

        /**
         * {@inheritDoc}
         */
        public String getTreeLabel(boolean isExpanded) {
            return "Thread-" + getUserObject() + ":" + slice;
        }

        /**
         * {@inheritDoc}
         */
        public boolean search(Search search) {
            return search.matches(getTreeLabel(false));
        }
    }

    /**
     * A <code>MethodNode</code> instance encapsulates the trace of one or more instructions within
     * a given method in a frame on a thread's call stack.
     */
    final static class MethodNode extends TraceNode {

        /**
         * The call depth of this method's frame within its thread's call stack.
         */
        public final int depth;

        /**
         * The maximum width of the String representation for each component in all the trace
         * lines of this method.
         */
        private final int[] componentWidths;

        /**
         * Creates a MethodNode.
         *
         * @param method   the represented method
         * @param depth    the call depth of this method's frame within its thread's call stack
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         */
        MethodNode(Symbols.Method method, int depth, int tracePosition) {
            super(tracePosition);
            setUserObject(method);
            this.depth = depth;
            this.componentWidths = new int[TraceLine.COMPONENT_COUNT];
        }

        /**
         * {@inheritDoc}
         */
        public ExecutionPoint getExecutionPoint() {
            Symbols.Method method = getMethod();
            return new ExecutionPoint(method, method.getSourceLineNumber(0), 0);
        }

        /**
         * {@inheritDoc}
         */
        public int getCallDepth() {
            return depth;
        }

        /**
         * Overrides parent so that component widths are updated when an InstructionNode is added.
         *
         * @param newChild  the child being added
         */
        public void add(MutableTreeNode newChild) {
            if (newChild instanceof InstructionNode) {
                ((InstructionNode)newChild).getTraceLine().updateComponentWidths(componentWidths);
            }

            super.add(newChild);
        }

        /**
         * Counts the number of descendants of this node that an instance of InstructionNode.
         *
         * @return the number of InstructionNode descendants of this node
         */
        public int computeNestedInstructionCount() {
            int count = 0;
            Enumeration e = breadthFirstEnumeration();
            while (e.hasMoreElements()) {
                if (e.nextElement() instanceof InstructionNode) {
                    ++count;
                }
            }
            return count;
        }

        /**
         * Gets the method represented by this node.
         *
         * @return the method represented by this node
         */
        public Symbols.Method getMethod() {
            return (Symbols.Method)getUserObject();
        }

        /**
         * {@inheritDoc}
         */
        public void configureRenderer(TraceTreeCellRenderer renderer, boolean isExpanded) {
            renderer.setFont(renderer.bold);
        }

        /**
         * {@inheritDoc}
         */
        public String getTreeLabel(boolean isExpanded) {
            Symbols.Method method = (Symbols.Method)getUserObject();
            if (!isExpanded) {
                int count = computeNestedInstructionCount();
                return "[" + count + "] " + method.getSignature();
            } else {
                return method.getSignature();
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean search(Search search) {
            Symbols.Method method = (Symbols.Method)getUserObject();
            return search.matches(method.getSignature());
        }
    }

    /**
     * An <code>InstructionNode</code> instance represents the VM state immediately before executing a particular instruction.
     */
    final static class InstructionNode extends TraceNode {

        /**
         * Creates an InstructionNode to represent a given trace line
         *
         * @param trace   the trace line
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         */
        InstructionNode(TraceLine trace, int tracePosition) {
            super(tracePosition);
            setUserObject(trace);
        }

        /**
         * Gets the TraceLine represents by this node.
         * @return  the TraceLine represents by this node
         */
        public TraceLine getTraceLine() {
            return (TraceLine)getUserObject();
        }

        /**
         * {@inheritDoc}
         */
        public ExecutionPoint getExecutionPoint() {
            TraceLine trace = getTraceLine();
            return new ExecutionPoint(trace.method, trace.method.getSourceLineNumber(trace.pc), trace.pc);
        }

        /**
         * {@inheritDoc}
         */
        public void configureRenderer(TraceTreeCellRenderer renderer, boolean isExpanded) {
            renderer.setFont(renderer.fixed);
        }

        /**
         * {@inheritDoc}
         */
        public String getTreeLabel(boolean isExpanded) {
            int[] componentWidths = ((MethodNode)getParent()).componentWidths;
            return getTraceLine().getTreeNodeLabel(componentWidths);
        }

        /**
         * {@inheritDoc}
         */
        public boolean search(Search search) {
            TraceLine trace = getTraceLine();
            return search.matches(trace.toString()) ||
                   search.matches(trace.stack.toString()) ||
                   search.matches(trace.locals.toString());
        }
    }

    /**
     * A <code>StackTraceStartNode</code> instance represents the start of a statck trace.
     */
    final static class StackTraceStartNode extends TraceNode {

        /**
         * Creates a node representing the start of a stack trace.
         *
         * @param line   the text denoting the type of the exception and any other info
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         */
        StackTraceStartNode(String line, int tracePosition) {
            super(tracePosition);
            setUserObject(line);
        }

        /**
         * {@inheritDoc}
         */
        public void configureRenderer(TraceTreeCellRenderer renderer, boolean isExpanded) {
            renderer.setForeground(Color.RED);
            renderer.setFont(renderer.bold);
        }

        /**
         * {@inheritDoc}
         */
        public String getTreeLabel(boolean isExpanded) {
            return (String)getUserObject();
        }

        /**
         * {@inheritDoc}
         */
        public boolean search(Search search) {
            return search.matches((String)getUserObject());
        }
    }

    /**
     * A <code>StackTraceNode</code> instance represents an element in a stack trace.
     */
    final static class StackTraceNode extends TraceNode {

        /**
         * Creates a node for an element in a stack trace.
         *
         * @param ep  the execution point corresponding to the stack trace element
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         */
        StackTraceNode(ExecutionPoint ep, int tracePosition) {
            super(tracePosition);
            setUserObject(ep);
        }

        /**
         * {@inheritDoc}
         */
        public ExecutionPoint getExecutionPoint() {
            return (ExecutionPoint)getUserObject();
        }

        /**
         * {@inheritDoc}
         */
        public void configureRenderer(TraceTreeCellRenderer renderer, boolean isExpanded) {
            renderer.setFont(renderer.plain);
        }

        /**
         * {@inheritDoc}
         */
        public String getTreeLabel(boolean isExpanded) {
            ExecutionPoint ep = getExecutionPoint();
            Symbols.Method method = ep.method;
            return method.getName(true) + '(' + method.getFile() + ':' + ep.lineNumber + ")  [bci=" + ep.bci + ']';
        }

        /**
         * {@inheritDoc}
         */
        public boolean search(Search search) {
            ExecutionPoint ep = getExecutionPoint();
            Symbols.Method method = ep.method;
            return search.matches(method.getName(true));
        }
    }

    /**
     * An <code>OtherTraceNode</code> instance represents a miscellaneous line of output in the trace file.
     */
    final static class OtherTraceNode extends TraceNode {

        /**
         * Creates a node that represents a miscellaneous line of output in the trace file.
         *
         * @param line  the line
         * @param tracePosition  the position (e.g. line number) in the input trace represented by this node
         */
        OtherTraceNode(String line, int tracePosition) {
            super(tracePosition);
            setUserObject(line);
        }

        /**
         * {@inheritDoc}
         */
        public void configureRenderer(TraceTreeCellRenderer renderer, boolean isExpanded) {
            renderer.setFont(renderer.italic);
            renderer.setIcon(null);
        }

        /**
         * {@inheritDoc}
         */
        public String getTreeLabel(boolean isExpanded) {
            return (String)getUserObject();
        }

        /**
         * {@inheritDoc}
         */
        public boolean search(Search search) {
            return search.matches((String)getUserObject());
        }
    }

    /*---------------------------------------------------------------------------*\
     *                            Tree node renderer                             *
    \*---------------------------------------------------------------------------*/

    /**
     * A tree renderer that can render TraceNodes.
     */
    final class TraceTreeCellRenderer extends DefaultTreeCellRenderer {

        /**
         * Some preallocated fonts for differentiating the nodes' text.
         */
        public final Font plain, italic, bold, fixed;

        /**
         * Constructs a TraceTreeCellRenderer.
         */
        TraceTreeCellRenderer() {
            plain  = new Font(null, Font.PLAIN, 12);
            italic = plain.deriveFont(Font.ITALIC);
            bold   = plain.deriveFont(Font.BOLD);
            fixed  = new Font("monospaced", Font.PLAIN, 12);
        }

        /**
         * {@inheritDoc}
         */
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus)
        {
            TraceNode node = (TraceNode)value;
            String label = node.getTreeLabel(expanded);
//System.err.println("" + (++count) + ": " + label);
            super.getTreeCellRendererComponent(tree, label, sel, expanded, leaf, row, hasFocus);
            node.configureRenderer(this, expanded);

            if (!leaf && !expanded) {
                setToolTipText("Hold SHIFT key when expanding to completely show last execution path");
            } else {
                setToolTipText(null);
            }
            return this;
        }
    }

}

/**
 * The exception thrown when a component in a trace is not formed as expected.
 */
class TraceParseException extends RuntimeException {
    TraceParseException(String input, String pattern) {
        super("\"" + input + "\" failed to match \"" + pattern + "\"");
    }
}

/**
 * A TraceLine instance encapsulates all the information parsed from a single trace line.
 */
final class TraceLine {

    /**
     * The number of logical number of components in a trace line.
     */
    public static final int COMPONENT_COUNT = 5;

    /**
     * An Instruction represents the information about the instruction in a trace line.
     */
    static class Instruction {

        /**
         * The pattern for an instruction within a trace line.
         * Capturing group 1 is the instruction opcode, group 2 is the mutation type of
         * the instruction (which may not be present), group 3 is the WIDE_* or ESCAPE_*
         * prefix (or -1 if there is no prefix) and group 4 is the immediate operand (if any).
         */
        static final Pattern INSTRUCTION = Pattern.compile("(\\d+)(?:#(\\d+))?,(-?\\d+)(?:,(-?.+))?");

        /**
         * Primary opcode.
         */
        public final int opcode;

        /**
         * Opcode of WIDE_* or ESCAPE_* prefix or -1 if there is not prefix.
         */
        public final int prefix;

        /**
         * The operand (if any).
         */
        public final String operand;

        /**
         * The mutation type of the instruction or -1 if VM was not built with the typemap.
         */
        public final int mutationType;

        /**
         * Creates an Instruction from the instruction substring in a trace line.
         *
         * @param trace  the substring which must match the {@link #INSTRUCTION} pattern
         */
        Instruction(String trace, Symbols symbols) {
            Matcher m = INSTRUCTION.matcher(trace);
            if (!m.matches()) {
                throw new TraceParseException(trace, m.pattern().pattern());
            }

            // Parse the opcode
            opcode = Integer.parseInt(m.group(1));

            // Parse the mutation type (if any)
            String mutationTypeGroup = m.group(2);
            if (mutationTypeGroup != null) {
                mutationType = Byte.parseByte(mutationTypeGroup);
            } else {
                mutationType = -1;
            }

            // Parse the WIDE_* or ESCAPE_* prefix
            prefix = Integer.parseInt(m.group(3));

            // Parse the operand
            String operandGroup = m.group(4);
            if (operandGroup == null) {
                operand = null;
            } else {
                switch (opcode) {
                    case OPC.INVOKENATIVE_I:
                    case OPC.INVOKENATIVE_I_WIDE:
                    case OPC.INVOKENATIVE_L:
                    case OPC.INVOKENATIVE_L_WIDE:
                    case OPC.INVOKENATIVE_O:
                    case OPC.INVOKENATIVE_O_WIDE:
                    case OPC.INVOKENATIVE_V:
                    case OPC.INVOKENATIVE_V_WIDE:
/*if[FLOATS]*/
                    case OPC.INVOKENATIVE_F:
                    case OPC.INVOKENATIVE_F_WIDE:
                    case OPC.INVOKENATIVE_D:
                    case OPC.INVOKENATIVE_D_WIDE:
/*end[FLOATS]*/
                    {
                        int nativeMethodIdentifier = Integer.parseInt(operandGroup);
                        String value;
                        try {
                            value = symbols.lookupNativeMethod(nativeMethodIdentifier);
                        } catch (Symbols.UnknownNativeMethodException unme) {
                            System.err.println(unme);
                            value = operandGroup;
                        }
                        operand = value;
                        break;

                    }
                    default:
                        operand = operandGroup;
                }
            }
        }

        /**
         * Appends instruction to a given StringBuffer.
         *
         * @param buf  the buffer to append to
         */
        public void appendTo(StringBuffer buf) {
            if (prefix != -1) {
                buf.append(Mnemonics.getMnemonic(prefix)).append(' ');
            }
            buf.append(Mnemonics.getMnemonic(opcode));
            if (mutationType != -1) {
                int type = mutationType & AddressType.TYPE_MASK;
                buf.append(':').append(AddressType.Mnemonics.charAt(type));
            }
            buf.append(' ');
            if (operand != null) {
                buf.append(' ').append(operand);
            }
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            StringBuffer buf = new StringBuffer(200);
            appendTo(buf);
            return buf.toString();
        }
    }

    /**
     * A Words instance represents the values in a contiguous set of words present in a trace line.
     */
    public static class Words {

        /**
         * Shared constant for an empty set of values.
         */
        private final long[] NO_VALUES = {};

        /**
         * The values in the words.
         */
        private final long[] values;

        /**
         * The type of the values. This will be null if the VM was not compiled with type map support.
         */
        private final byte[] types;

        /**
         * Immutable object denoting zero size set of word values.
         */
        public static Words ZERO_WORDS = new Words("", false);

        /**
         * Creates a Words instance.
         *
         * @param trace     the substring from a trace line containing zero or more word values
         * @param hasTypes  specifies if the word values have an annotated type
         */
        public Words(String trace, boolean hasTypes) {
            StringTokenizer st = new StringTokenizer(trace, ",");
            if (st.hasMoreTokens()) {
                int count = st.countTokens();
                values = new long[count];
                types = (hasTypes) ? new byte[count] : null;

                for (int i = 0; i != count; ++i) {
                    String token = st.nextToken();
                    if (hasTypes) {
                        int index = token.indexOf('#');
                        String value = token.substring(0, index);
                        if (value.equals("X")) {
                            values[i] = 0xdeadbeef;
                        } else {
                            values[i] = Long.parseLong(value);
                        }
                        types[i] = Byte.parseByte(token.substring(index + 1));
                    } else {
                        if (token.equals("X")) {
                            values[i] = 0xdeadbeef;
                        } else {
                            values[i] = Long.parseLong(token);
                        }
                    }
                }
            } else {
                values = NO_VALUES;
                types = null;
            }
        }

        /**
         * Gets the number of values in this object.
         *
         * @return the number of values in this object
         */
        public int getSize() {
            return values.length;
        }

        /**
         * Gets the value at a given index.
         *
         * @param i  the index
         * @return the value at index <code>i</code>
         */
        public long getValue(int i) {
            return values[i];
        }

        /**
         * Gets the type of the value at a given index.
         *
         * @param i  the index
         * @return the type of the value at index <code>i</code> or -1 if there is no type information
         *
         */
        public int getType(int i) {
            if (types == null) {
                return -1;
            }
            return types[i] & 0xFF;
        }

        /**
         * Appends values to a given StringBuffer.
         *
         * @param buf  the buffer to append to
         */
        public void appendTo(StringBuffer buf) {
            boolean hasTypes = (types != null);
            for (int i = 0; i != values.length; ++i) {
                long value = values[i];
                if (value == 0xdeadbeef) {
                    buf.append('X');
                } else {
                    buf.append(value);
                }
                if (hasTypes) {
                    buf.append(':').append(AddressType.getMnemonic(types[i]));
                }
                if (i != values.length - 1) {
                    buf.append(' ');
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            StringBuffer buf = new StringBuffer(200);
            appendTo(buf);
            return buf.toString();
        }
    }

    /**
     * Used to interpret a words component that really says what the max_stack value for a method is.
     */
    static class MaxStack extends Words {

        /**
         * Creates a MaxStack instance.
         *
         * @param trace     the substring from a trace line containing zero or more word values
         */
        MaxStack(String trace) {
            super(trace, false);
            Assert.that(getSize() == 2);
        }

        /**
         * {@inheritDoc}
         */
        public void appendTo(StringBuffer buf) {
            buf.append(this.toString());
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "max_stack=" + getValue(0);
        }
    }

    /**
     * Used to interpret a words component that really says what the max_locals value for a method is.
     */
    static class MaxLocals extends Words {

        /**
         * Creates a MaxLocals instance.
         *
         * @param trace     the substring from a trace line containing zero or more word values
         */
        MaxLocals(String trace) {
            super(trace, false);
            Assert.that(getSize() == 1);
        }

        /**
         * {@inheritDoc}
         */
        public void appendTo(StringBuffer buf) {
            buf.append(this.toString());
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "max_locals=" + getValue(0);
        }
    }

    /**
     * The numeric identifier of the thread.
     */
    public final String threadID;

    /**
     * The call depth of the trace.
     */
    public final int depth;

    /**
     * The method.
     */
    public final Symbols.Method method;

    /**
     * The bytecode offset of the instruction.
     */
    public final int pc;

    /**
     * The instruction.
     */
    public final Instruction instruction;

    /**
     * The values of the local variable slots.
     */
    public final Words locals;

    /**
     * The values on the operand stack.
     */
    public final Words stack;

    /**
     * The address of the top of the operand stack.
     */
    public final long stackPointer;

    /**
     * Backward branch count.
     */
    public final long branchCount;

    /**
     * Number of words remaining on the thread's stack.
     */
    public final long remainingStack;

    /**
     * The pattern for a trace line after the "*TRACE*:" prefix.
     * Capturing group 1 is the thread ID, group 2 is the call depth,
     * group 3 is the method address, group 4 is the bytecode offset, group 5
     * is the instruction opcode, prefix and operand, group 6
     * is the local variable values, group 7 is the operand stack values, group
     * 8 is the stack pointer, group 9 is the backward branch count, group 10
     * is the remaining number of stack words and group 11 is the rest of the
     * line.
     */
    static final Pattern TRACELINE = Pattern.compile("([^:]+):([^:]+):([^:]+):([^:]+):([^:]+):([^:]*):([^:]*):([^:]+):([^:]+):([^:]+)(?::(.*))?");

    /**
     * Most recently constructed TraceLine instance. This is used to share common immutable components.
     */
    private static TraceLine previous = null;
    private static Matcher previousMatcher = null;

    /**
     * Creates a TraceLine from a given line from the trace file/stream.
     *
     * @param line a trace line with the "*TRACE*:" prefix removed.
     */
    TraceLine(String line, Symbols symbols) {

        Matcher m = TRACELINE.matcher(line);
        if (!m.matches()) {
            throw new TraceParseException(line, m.pattern().pattern());
        }

        // Parse thread ID
        threadID = m.group(1);

        // Parse the call depth
        depth = Integer.parseInt(m.group(2));

        // Parse the method address
        method = TraceViewer.lookupMethod(symbols, Long.parseLong(m.group(3)));

        // Parse the bytecode offset
        pc = Integer.parseInt(m.group(4));

        // Parse the instruction
        instruction = new Instruction(m.group(5), symbols);

        // Parse the local variable values
        String group = m.group(6);
        if (previous != null && group.equals(previousMatcher.group(6))) {
            locals = previous.locals;
        } else {
            switch (instruction.opcode) {
                case OPC.EXTEND:
                case OPC.EXTEND0:
                case OPC.EXTEND_WIDE: {
                    locals = new MaxLocals(group);
                    break;
                }
                default: {
                    locals = new Words(group, instruction.mutationType != -1);
                    break;
                }
            }
        }

        // Parse the operand stack values
        group = m.group(7);
        if (previous != null && group.equals(previousMatcher.group(7))) {
            stack = previous.stack;
        } else {
            switch (instruction.opcode) {
                case OPC.EXTEND:
                case OPC.EXTEND0:
                case OPC.EXTEND_WIDE: {
                    stack = new MaxStack(group);
                    break;
                }
                default: {
                    stack = new Words(group, instruction.mutationType != -1);
                    break;
                }
            }
        }

        // Parse the VM state stuff
        stackPointer = Long.parseLong(m.group(8));
        branchCount = Long.parseLong(m.group(9));
        remainingStack = Integer.parseInt(m.group(10));

        previous = this;
        previousMatcher = m;
    }

    /**
     * Updates the maximum width for a component.
     *
     * @param widths the table of maximum component widths
     * @param index  the index of the component to be updated
     * @param width  the width of an component instance
     */
    private static void updateWidth(int[] widths, int index, int width) {
        if (widths[index] < width) {
            widths[index] = width;
        }
    }

    /**
     * Updates the maximum widths for all the components in a trace line.
     *
     * @param widths the table of maximum component widths
     */
    void updateComponentWidths(int[] widths) {
        Assert.that(widths.length == COMPONENT_COUNT);
        updateWidth(widths, 0, Integer.toString(pc).length());
        updateWidth(widths, 1, instruction.toString().length());
        updateWidth(widths, 2, getVMState().length());
    }

    private static void pad(StringBuffer buf, int width) {
        while (buf.length() < width) {
            buf.append(' ');
        }
    }

    /**
     * Gets the label for the node in a JTree representing this trace line.
     *
     * @param componentWidths the table of maximum component widths or null
     * @return the label
     */
    public String getTreeNodeLabel(int[] componentWidths) {
        StringBuffer buf = new StringBuffer(400);
        int pad = (componentWidths == null ? 0 : componentWidths[0]) + 2;

        // Append the bytecode offset
        buf.append(pc).append(": ");
        pad(buf, pad);

        // Append the instruction
        instruction.appendTo(buf);
        buf.append(' ');
        if (componentWidths != null) {
            pad += componentWidths[1] + 1;
            pad(buf, pad);
        }

        // Append the VM state
        appendVMState(buf);

        return buf.toString();
    }

    /**
     * Appends the string representation of the VM stae encapsulated in this trace line
     * to a given StringBuffer
     *
     * @param buf the buffer to append to
     */
    public void appendVMState(StringBuffer buf) {
        buf.append("sp=").
            append(stackPointer).
            append(" bcount=").
            append(branchCount).
            append(" sp-sl=").
            append(remainingStack);
    }

    /**
     * Gets the string representation of the VM stae encapsulated in this trace line.
     *
     * @return the string representation of the VM stae encapsulated in this trace line
     */
    public String getVMState() {
        StringBuffer buf = new StringBuffer(100);
        appendVMState(buf);
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getTreeNodeLabel(null);
    }
}
