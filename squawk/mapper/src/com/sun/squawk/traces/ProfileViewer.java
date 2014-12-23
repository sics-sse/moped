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
import java.util.Arrays;
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
import com.sun.squawk.traces.ProfileViewer.*;


/**
 * The ProfileViewer is a GUI tool for showing the execution profile of a Squawk VM execution.
 *
 */
public class ProfileViewer extends JFrame implements WindowListener, ComponentListener {

    /**
     * The default value for the threshold governing which profile nodes are to be displayed.
     */
    private static final float DEFAULT_THRESHOLD = 0.0F;

    /**
     * The TraceTree component used to render the profile as a tree.
     */
    private TraceTree profileTree;

    /**
     * The root node in the profile's data model.
     */
    private MethodNode profileRoot;

    /**
     * The search path for finding and opening source files.
     */
    private ClasspathConnection sourcePath;

    /**
     * A cache of JTextAreas for previously viewed source files.
     */
    private HashMap<String, JTextArea> sourceFileCache;

    /**
     * The status bar.
     */
    private JTextArea statusBar;

    /**
     * The criteria to use when sorting nodes.
     */
    private Comparator nodeSortingCriteria = ProfileNode.SORT_BY_FREQUENCY;

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
     * Constructs a ProfileViewer.
     */
    private ProfileViewer() {
        profileRoot = new MethodNode(null);
        sourceFileCache = new HashMap<String, JTextArea>();
    }

    /**
     * Creates and returns an AddressRelocator based on the first line of the profile.
     *
     * @param line   the first line of the profile
     * @return the created AddressRelocator
     */
    AddressRelocator createAddressRelocator(String line) {
        Matcher m = Pattern.compile("\\*TRACE\\*:\\*ROM\\*:(\\d+):(\\d+):\\*NVM\\*:(\\d+):(\\d+):\\*(\\d+)\\*").matcher(line);
        // Matcher m = Pattern.compile("\\*TRACE\\*:\\*ROM\\*:(\\d+):(\\d+):\\*NVM\\*:(\\d+):(\\d+)").matcher(line);
        boolean matches = m.matches();
        Assert.that(matches, "first line of trace does not match the required pattern: " + m.pattern().pattern());
        
        try {
            long romStart = Long.parseLong(m.group(1));
            
            long romEnd   = Long.parseLong(m.group(2));
            long nvmStart = Long.parseLong(m.group(3));
            long nvmEnd   = Long.parseLong(m.group(4));
            boolean is64Bit = m.group(5).equals("64");
            
            return new AddressRelocator(romStart, (int)(romEnd - romStart), nvmStart, (int)(nvmEnd - nvmStart));
        } catch (IllegalStateException e) {
            System.err.println("Error parsing line: ");
            System.err.println(line);
            throw e;
        }
    }

    /**
     * Pattern for matching a line that starts a profile stack trace.
     * Capturing group 1 is the backward branch count at which the sample was taken and group 2
     * is the mnemonic of the instruction executed just before the sample was taken
     */
    public static Pattern STACK_TRACE_START = Pattern.compile("\\*STACKTRACESTART\\*:(-?\\d+):\\*PROFILE TRACE\\*:(.*)");

    /**
     * Pattern for matching a line that is an element in a (profile) stack trace.
     * Capturing group 1 is the method address and group 2 is a bytecode offset.
     */
    public static Pattern STACK_TRACE_ELEMENT = Pattern.compile("\\*STACKTRACE\\*:(\\d+):(\\d+)");

    /**
     * Pattern for matching a line that is a repeat element in a (profile) stack trace.
     * Capturing group 1 is the repetition count.
     */
    public static Pattern STACK_TRACE_REPETITION_ELEMENT = Pattern.compile("\\*STACKTRACE\\*:\"(\\d+)");

    /**
     * Pattern for matching a line that is the last element in a profile stack trace.
     */
    public static Pattern STACK_TRACE_END = Pattern.compile("\\*STACKTRACEEND\\*");

    ProfileNode getProfileRoot() {
        return profileRoot;
    }

    /**
     * Adds a sample (i.e. stack trace) to the tree and clears the vector holding the sample.
     *
     * @param sample   the sample to be added
     * @param depth    the maximum number of elements to use from each stack trace, starting
     *                 from the leaf element. A value of 0 means all elements are to be used.
     * @param mnemonic the mnemonic of the instruction of the inner most call of <code>sample</code>
     */
    private void addSample(ReverseTraversableVector sample, int depth, String mnemonic) {
        if (!sample.isEmpty()) {
            if (depth == 0) {
                depth = sample.size();
            }
            Enumeration callees = sample.relements(depth);
            ExecutionPoint ep = (ExecutionPoint)callees.nextElement();
            profileRoot.addInstruction(ep, callees).setMnemonic(mnemonic);
            sample.removeAllElements();
        }
    }

    /**
     * Reads lines from an input file until a line matching the profile stack trace start pattern
     * is recognized.
     *
     * @param in  the input file
     * @return the mnemonic of the instruction executed just before the stack trace was taken or null if no stack trace start line was found
     */
    private String readStackTraceStart(InputFile in) {
        // ignore lines until a stack trace start pattern is recognised
        String line;
        while ((line = in.readLine()) != null) {
            Matcher m = STACK_TRACE_START.matcher(line);
            if (m.matches()) {
                return m.group(2);
            }
        }
        return null;
    }

    /**
     * Reads the samples (i.e. stack traces) from a given profile and generates the corresponding tree model.
     *
     * @param in     the reader containing the profile to read
     * @param depth  the maximum number of elements to use from each stack trace, starting
     *               from the leaf element. A value of 0 means all elements are to be used.
     * @return the total number of samples read
     */
    private int readSamples(InputFile in, int depth) {
        int count = 0;
        ReverseTraversableVector<ExecutionPoint> sample = new ReverseTraversableVector<ExecutionPoint>();
        String mnemonic;
        while ((mnemonic = readStackTraceStart(in)) != null) {

            boolean done = false;
            while (!done) {
                String line = in.readLine();

                if (line == null) {
                    System.err.println("warning: " + in.formatErrorMessage("incomplete stack trace"));
                    return count;
                }

                Matcher m = STACK_TRACE_ELEMENT.matcher(line);
                if (m.matches()) {
                    long methodAddress = Long.parseLong(m.group(1));
                    int pc = Integer.parseInt(m.group(2));

                    // The bytecode offset always points to the opcode of the instruction
                    // that will be executed next in the given method. As such, it must be
                    // decremented by one so that the source line corresponding to the
                    // the sampled instruction is used (unless it's the first instruction
                    // in the method).
                    if (pc != 0) {
                        pc = pc - 1;
                    }

                    Symbols.Method method = symbols.lookupMethod(methodAddress);
                    sample.addElement(new ExecutionPoint(method, method.getSourceLineNumber(pc), pc));
                } else {
                    m = STACK_TRACE_END.matcher(line);
                    if (m.matches()) {
                        // Found end-stack-trace-element line. Complete current stack trace
                        // and continue at top of outer loop as current line may start
                        // next stack trace
                        done = true;
                    } else {
                        System.err.println("warning: " + in.formatErrorMessage("parse error of stack trace entry"));
                        return count;
                    }
                }
            }

            addSample(sample, depth, mnemonic);
            count++;
        }
        if (count == 0) {
            System.err.println("warning: no stack traces were parsed from the trace file");
        }
        return count;
    }

    /**
     * Reads the profile data from a file and builds the corresponding profile model.
     *
     * @param file   the file containing the profile
     * @param depth  the maximum number of elements to use from each stack trace, starting
     *               from the leaf element. A value of 0 means all elements are to be used.
     * @return the total number of samples in the profile
     */
    private int readProfile(File file, int depth) {
        InputFile in = new InputFile(file);

        // Create a relocator from the first line and use it to relocate any canonical
        // address in the symbols
        AddressRelocator relocator = createAddressRelocator(in.readLine());
        symbols.relocate(relocator, false);

        // Read the remainder of the profile, extracting the stack traces
        return readSamples(in, depth);
    }

    /**
     * The map from addresses to symbolic information.
     */
    private Symbols symbols = new Symbols();

    /**
     * Hides nodes in the tree based on a given filter.
     *
     * @param filter  hides all nodes in the tree that are matched by <code>filter</code>
     */
    void hideNodes(ProfileNode.Filter filter) {

        // Unhide all nodes first
        profileRoot.hideNodes(null);

        // Now do the hiding
        profileRoot.hideNodes(filter);

        // (Re-)establish the sort order
        profileRoot.sort(nodeSortingCriteria);

        // Redraw the tree
        if (profileTree != null) {
            DefaultTreeModel profileModel = new DefaultTreeModel(profileRoot);
            profileTree.setModel(profileModel);
            this.validate();
        }
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
        out.println("Usage: ProfileViewer [-options] profile ");
        out.println("where options include:");
        out.println("    -map:<file>         map file containing method meta info");
        out.println("    -sp:<path>          where to find source files");
        out.println("    -depth:n            ignore all but the last n elements of each stack trace");
        out.println("    -threshold:n        samples with frequency below n% or total sample count are hidden (default="+DEFAULT_THRESHOLD+")");
        out.println("    -h                  show this message and exit");
        out.println();
    }

    /**
     * Parses the command line arguments and starts the profile viewer.
     *
     * @param args   command line arguments
     */
    private void run(String[] args) {

        Vector<String> symbolsToLoad = new Vector<String>();
        symbolsToLoad.addElement("squawk.sym");
        symbolsToLoad.addElement("squawk_dynamic.sym");

        String sourcePathStr = ".";
        int depth = 0;
        float threshold = DEFAULT_THRESHOLD;

        int argc = 0;
        while (argc != args.length) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            } else if (arg.startsWith("-sp:")) {
                sourcePathStr = arg.substring("-sp:".length());
            } else if (arg.startsWith("-map:")) {
                symbolsToLoad.addElement(arg.substring("-map:".length()));
            } else if (arg.startsWith("-depth:")) {
                depth = Integer.parseInt(arg.substring("-depth:".length()));
            } else if (arg.startsWith("-threshold:")) {
                try {
                    threshold = Float.parseFloat(arg.substring("-threshold:".length()));
                    if (threshold < 0 || threshold > 100) {
                        threshold = DEFAULT_THRESHOLD;
                        throw new NumberFormatException("value must be between 0 and 100");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("warning: invalid threshold - will use default (cause: " + e.getMessage() + ")");
                }
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
            usage("Missing profile");
            return;
        }

        // Load the symbols
        for (Enumeration e = symbolsToLoad.elements(); e.hasMoreElements();) {
            symbols.loadIfFileExists(new File((String)e.nextElement()));
        }

        // Initialize the source path
        try {
            sourcePathStr = sourcePathStr.replace(':', File.pathSeparatorChar);
            this.sourcePath = (ClasspathConnection)Connector.open("classpath://" + sourcePath);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        File profileFile = new File(args[argc]);
        initializeUI(readProfile(profileFile, depth), threshold);
    }

    /**
     * Command line entry point.
     *
     * @param args  command line arguments
     */
    public static void main(String[] args) {
        ProfileViewer viewer = new ProfileViewer();
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
    private static final String FRAME_TITLE_PREFIX = "Squawk profile viewer";

    /**
     * Initializes the UI of the ProfileViewer. This must be called only after the
     * data in the tree is complete.
     *
     * @param  samples  the total number of samples in the profile
     */
    private void initializeUI(int samples, float initialThreshold) {

        // Create the tree data model
        DefaultTreeModel profileModel = new DefaultTreeModel(profileRoot);

        // Create the tree
        profileTree = new TraceTree(profileModel);

        // Configure the tree
        profileTree.putClientProperty("JTree.lineStyle", "Angled");
        profileTree.setShowsRootHandles(true);
        profileTree.setCellRenderer(new ProfileTreeCellRenderer(samples));
        profileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        profileTree.setScrollsOnExpand(false);

        // Create the other panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Put the tree in a scrollable pane
        JScrollPane treeScrollPane = new JScrollPane(profileTree);

        // Place holder until a node is selected
        JPanel noSourcePanel = new JPanel(new GridBagLayout());
        noSourcePanel.add(new JLabel("No source file selected/available"));
        JScrollPane sourceView = new JScrollPane(noSourcePanel);

        // Create "hide nodes" panel
        HideNodesPanel hideNodesPanel = new HideNodesPanel(this, samples, initialThreshold);
        mainPanel.add("North", hideNodesPanel);

        // Create source view panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScrollPane, sourceView);
        splitPane.setDividerLocation(500);
        mainPanel.add("Center", splitPane);

        // Create the status bar
        statusBar = new JTextArea();
        mainPanel.add("South", statusBar);

        // Add the listener that will update the source view when a new tree node is selected
        ProfileTreeSelectionListener ptsl = new ProfileTreeSelectionListener(sourceView);
        profileTree.addTreeSelectionListener(ptsl);

        setTitle(FRAME_TITLE_PREFIX);

        // Maximize the window
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        mainPanel.setPreferredSize(dimension);
        getContentPane().add(mainPanel);

        addWindowListener(this);
        addComponentListener(this);
        validate();
        pack();

        // Add a tree expansion listener that will modify what is expanded if SHIFT is pressed
        profileTree.addTreeExpansionListener(new ModifiedTreeExpansionAdapter(InputEvent.SHIFT_MASK) {
            public void treeExpanded(TreeExpansionEvent e, boolean modified) {
                TreePath path = e.getPath();

                // If the expansion occurred with a mouse click while SHIFT was pressed,
                // the complete path from the expanded node to its last leaf is expanded
                if (modified) {

                    ProfileNode node = (ProfileNode)path.getLastPathComponent();
                    ProfileNode leafParent = (ProfileNode)node.getLastLeaf().getParent();
                    Assert.that(leafParent != null);
                    TreePath leafPath = new TreePath(leafParent.getPath());
                    if (!profileTree.isVisible(leafPath)) {
                        profileTree.expandPath(leafPath);
                        return;
                    }
                }

                profileTree.setSelectionPath(path);
                profileTree.scrollPathToVisible(path);
            }
        });

        // Display the GUI
        setVisible(true);

        // Applies the intial threshold
        hideNodesPanel.apply();
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
     * Updates the message in the status bar.
     *
     * @param text  the message to display
     */
    public void updateStatusBar(String text) {
        statusBar.setText(text);
    }

    /**
     * This class implements a listener that attempts to display the source file for current node
     * when the selected node in the tree changes. If it manages to find and display the source
     * file, it will also position scroll position on the source line corresponding to the
     * execution point represented by the selected node.
     */
    class ProfileTreeSelectionListener implements TreeSelectionListener {

        /**
         * The scroll pane in which source files are to be displayed.
         */
        private final JScrollPane view;


        /**
         * Constructs a ProfileTreeSelectionListener.
         *
         * @param view JScrollPane
         */
        ProfileTreeSelectionListener(JScrollPane view) {
            this.view = view;
        }

        /**
         * {@inheritDoc}
         */
        public void valueChanged(TreeSelectionEvent e) {

            ProfileNode node = (ProfileNode)profileTree.getLastSelectedPathComponent();
            if (node == null) {
                return;
            }

            // Get a handle to the method containing the selected execution point
            boolean isExpanded = profileTree.isExpanded(new TreePath(node));
            ExecutionPoint ep = node.getExecutionPointView();
            if (ep == null) {
                return;
            }


            String path = ep.method.getFilePath();
            JTextArea text;
            try {
                text = getSourceFile(path);
            } catch (IOException ioe) {
                setTitle(FRAME_TITLE_PREFIX + " - ??/" + path);
                view.getViewport().setView(new JTextArea("An exception occurred while reading "+path+":\n\t"+ioe));
                return;
            }

            final int lineNo = ep.lineNumber;
            view.getViewport().setView(text);
            try {
                final int startPos = text.getLineStartOffset(lineNo - 1);
                final int endPos   = text.getLineEndOffset(lineNo - 1);
                text.setCaretPosition(endPos);
                text.moveCaretPosition(startPos);
                text.getCaret().setSelectionVisible(true);

                setTitle(FRAME_TITLE_PREFIX + " - " + path + ":" + lineNo);

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
            } catch (IllegalArgumentException iae) {
            }
        }
    }
}

/**
 * A subclass of vector that can have its elements sorted by a {@link Comparator}.
 */
final class SortableVector<E> extends Vector<E> {

    SortableVector() {

    }

    SortableVector(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Sorts the elements in the vector with a given Comparator.
     *
     * @param comparator  the sorter
     */
    @SuppressWarnings("unchecked")
    public void sort(Comparator comparator) {
        Arrays.sort(elementData, 0, elementCount, comparator);
    }
}

/**
 * A subclass of vector whose enumerator traverses its elements from last to first.
 */
final class ReverseTraversableVector<E> extends Vector<E> {

    /**
     * Returns an enumeration of the components of this vector. The
     * returned <tt>Enumeration</tt> object will generate all items in
     * this vector. The first item generated is the {@link #lastElement() last element},
     * then the previous item and so on until the {@link #firstElement() first element}.
     *
     * @param limit  the maximum number of elements in the enumeration
     * @return  an enumeration of the components of this vector.
     */
    public Enumeration relements(final int limit) {
        return new Enumeration() {
            int count = Math.min(elementCount, limit);

            public boolean hasMoreElements() {
                return count > 0;
            }

            public Object nextElement() {
                if (count > 0) {
                    return elementData[--count];
                }
                throw new NoSuchElementException();
            }
        };
    }
}

/**
 * A <code>ProfileNode</code> instance encapsulates an execution point within one or more
 * stack trace samples. It records the source code location of the execution point as well
 * as the frequency with which the point appears in a stack trace sample.
 */
abstract class ProfileNode extends DefaultMutableTreeNode {

    /**
     * Used to implement hiding of nodes.
     */
    private Set<ProfileNode> hiddenChildren;

    /**
     * The number of times that the execution point represented by this node appears in a profile sample.
     */
    private int frequency;

    /**
     * Gets the execution point that should be displayed when this node is selected.
     *
     * @return the execution point that should be displayed when this node is selected
     */
    public abstract ExecutionPoint getExecutionPointView();

    /**
     * Gets the number of times that the execution point represented by this node appears in a profile sample.
     *
     * @return the number of times that the execution point represented by this node appears in a profile sample
     */
    public final int getFrequency() {
        return frequency;
    }

    /**
     * Bumps the frequency up by one.
     */
    protected void incrementFrequency() {
        frequency++;
    }

    /**
     * Configures a given renderer used to draw this node in a JTree.
     *
     * @param renderer  the renderer to configure
     * @return the label to display
     */
    public abstract String render(ProfileTreeCellRenderer renderer);

    /**
     * Sorts the children of this node according to a given comparator.
     *
     * @param comparator  determines how the children are sorted
     */
    public final void sort(Comparator comparator) {
        if (children != null && children.size() > 0) {
            ((SortableVector)children).sort(comparator);
            for (Enumeration e = children.elements(); e.hasMoreElements();) {
                ProfileNode child = (ProfileNode)e.nextElement();
                child.sort(comparator);
            }
        }
    }

    /**
     * A comparator for sorting children in ascending order of frequency.
     */
    public static final Comparator SORT_BY_FREQUENCY = new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((ProfileNode)o1).getFrequency() - ((ProfileNode)o2).getFrequency();

        }
    };

    /**
     * A comparator for sorting children in ascending order of line number.
     */
    public static final Comparator SORT_BY_LINE_NUMBER = new Comparator() {
        public int compare(Object o1, Object o2) {
            ExecutionPoint ep1 = ((ProfileNode)o1).getExecutionPointView();
            ExecutionPoint ep2 = ((ProfileNode)o2).getExecutionPointView();
            return ep1.lineNumber - ep2.lineNumber;
        }
    };

    /**
     * {@inheritDoc}
     *
     * Overrides super class to ensure a SortableVector is created to store the children.
     */
    public void insert(MutableTreeNode newChild, int childIndex) {
        if (children == null) {
            children = new SortableVector();
        }
        super.insert(newChild, childIndex);
    }

    /**
     * A filter is used as a predicate to partition a set of ProfileNodes into two disjoint groups.
     */
    interface Filter {

        /**
         * Determines if a given ProfileNode satisfies the predicate represented by this object.
         *
         * @param node ProfileNode  the node to test
         * @return true if <code>node</code> satisifies the predicate
         */
        public boolean matches(ProfileNode node);
    }

    /**
     * Gets the number of nodes in the (sub)tree rooted at this node that are hidden.
     *
     * @param onlyHidden   only count the hidden nodes
     * @param thisIsHidden specifies that this node is a hidden node
     * @return the number of nodes in the (sub)tree rooted at this node that are hidden
     */
    public final int getNodeCount(boolean onlyHidden, boolean thisIsHidden) {
        int count = 0;
        if (children != null) {
            if (!onlyHidden || thisIsHidden) {
                count = children.size();
            }
            for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
                ProfileNode child = (ProfileNode)e.nextElement();
                count += child.getNodeCount(onlyHidden, thisIsHidden);
            }
        }

        if (hiddenChildren != null) {
            count += hiddenChildren.size();
            for (Iterator iterator = hiddenChildren.iterator(); iterator.hasNext(); ) {
                ProfileNode child = (ProfileNode)iterator.next();
                count += child.getNodeCount(onlyHidden, true);
            }
        }
        return count;
    }

    /**
     * Hides the descendants of this node that match a given filter. If the filter is null,
     * then any currently hidden descendants are unhidden.
     *
     * @param filter  the children matching this filter will be hidden
     */
    public final void hideNodes(Filter filter) {
        if (filter == null) {
            if (children != null) {

                // Process all the descendents of the unhidden children
                for (Enumeration e = children.elements(); e.hasMoreElements();) {
                    ProfileNode child = (ProfileNode)e.nextElement();
                    child.hideNodes(null);
                }

                // Now process the hidden children and their descendants
                if (hiddenChildren != null) {
                    for (Iterator iterator = hiddenChildren.iterator(); iterator.hasNext(); ) {
                        ProfileNode child = (ProfileNode)iterator.next();
                        super.insert(child, getChildCount());
                        child.hideNodes(null);
                    }
                    hiddenChildren = null;
                }

            }
        } else {
            if (children != null) {

                for (int i = 0; i < children.size(); ) {
                    ProfileNode child = (ProfileNode)children.elementAt(i);

                    // Recurse on descendents first
                    child.hideNodes(filter);

                    if ((child.getChildCount() == 0 && child instanceof MethodNode) || filter.matches(child)) {
                        if (hiddenChildren == null) {
                            hiddenChildren = new HashSet<ProfileNode>();
                        }
                        this.remove(child);
                        hiddenChildren.add(child);
                    } else {
                        ++i;
                    }
                }
            }
        }
    }
}

/**
 * A <code>MethodNode</code> instance represents a method that appears in one or more execution paths in an execution profile.
 */
final class MethodNode extends ProfileNode {

    /**
     * Constructs a MethodNode representing one or more a point in one or more execution paths
     * through a given method.
     *
     * @param method   a method present in at least one sampled execution path
     */
    MethodNode(Symbols.Method method) {
        setUserObject(method);
    }

    /**
     * Gets the method.
     *
     * @return the method represented by this node
     */
    public Symbols.Method getMethod() {
        return (Symbols.Method)getUserObject();
    }

    /**
     * Gets the instruction that corresponds with an execution point within this method.
     *
     * @param ep   an execution point within this method
     * @return the instruction node corresponding to <code>ep</code> or null if there is no such node
     */
    public InstructionNode getInstruction(ExecutionPoint ep) {
        Assert.that(ep.method == getMethod() || getMethod() == null);
        if (children != null) {
            for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
                InstructionNode instruction = (InstructionNode)e.nextElement();
                ExecutionPoint ep2 = instruction.getExecutionPoint();
                if (instruction.getExecutionPoint().equals(ep)) {
                    return instruction;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ExecutionPoint getExecutionPointView() {
        Symbols.Method method = getMethod();
        if (method != null) {
            return new ExecutionPoint(method, method.getSourceLineNumber(0), 0);
        } else {
            return null;
        }

    }

    /**
     * Adds an instruction node to represent an execution point within this method.
     *
     * @param ep      an execution point in a sample
     * @param callees the nested calls if <code>ep</code> is at a method invocation
     * @return  the InstructionNode representing the inner most execution point at which the sample was taken
     */
    public InstructionNode addInstruction(ExecutionPoint ep, Enumeration callees) {
        incrementFrequency();
        Symbols.Method method = getMethod();
        Assert.that(method == ep.method || method == null);

        InstructionNode instruction = getInstruction(ep);
        if (instruction == null) {
            instruction = new InstructionNode(ep);
            insert(instruction, getChildCount());
        }
        return instruction.addCallees(callees);
    }

    /**
     * {@inheritDoc}
     */
    public String render(ProfileTreeCellRenderer renderer) {
        Symbols.Method method = getMethod();
        if (method == null) {
            return "<scheduler>";
        } else {
            float percent = (float)(100 * getFrequency()) / renderer.samples;
            renderer.setFont(renderer.bold);
            return method.getSignature() + " [" + percent + "%]";
        }
    }
}


/**
 * An <code>InstructionNode</code> instance encapsulates an execution point within a method.
 */
final class InstructionNode extends ProfileNode {

    /**
     * The mnemonic of an instruction in an inner most call.
     */
    private String mnemonic;

    /**
     * Constructs an InstructionNode to represent an execution point within a method.
     *
     * @param ep   an execution point
     */
    InstructionNode(ExecutionPoint ep) {
        setUserObject(ep);
    }

    /**
     * Sets the mnemonic of this instruction.
     *
     * @param mnemonic the mnemonic of this instruction
     */
    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    /**
     * The mnemonic of this instruction if was in the inner most call of at least one sample
     * otherwise null.
     *
     * @return mnemonic of this instruction or null
     */
    public String getMnemonic() {
        return mnemonic;
    }

    /**
     * Gets the execution point represented by this node.
     *
     * @return  the execution point represented by this node
     */
    public ExecutionPoint getExecutionPoint() {
        return (ExecutionPoint)getUserObject();
    }

    /**
     * {@inheritDoc}
     */
    public ExecutionPoint getExecutionPointView() {
        return getExecutionPoint();
    }

    /**
     * Gets the method node corresponding to a given method that is invoked by this instruction.
     *
     * @param method  the method to search for
     * @return the MethodNode corresponding to an invocation of <code>method</code> or null
     *                if there is no such node
     */
    public MethodNode getCallee(Symbols.Method method) {
        if (children != null) {
            for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
                MethodNode node = (MethodNode)e.nextElement();
                if (node.getMethod() == method) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Adds the remaining call chain corresponding to this instruction.
     *
     * @param callees   the remaining call chain from this instruction. A non-empty
     *                  enumeration implies that this must be an invoke instruction.
     * @return  the InstructionNode representing the inner most execution point at which the sample was taken
     */
    public InstructionNode addCallees(Enumeration callees) {
        incrementFrequency();
        if (callees.hasMoreElements()) {
            ExecutionPoint ep = (ExecutionPoint)callees.nextElement();
            Symbols.Method method = ep.method;

            MethodNode callee = getCallee(method);
            if (callee == null) {
                callee = new MethodNode(method);
                insert(callee, getChildCount());
            }
            return callee.addInstruction(ep, callees);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String render(ProfileTreeCellRenderer renderer) {
        renderer.setFont(renderer.fixed);
        ExecutionPoint ep = getExecutionPoint();
        Symbols.Method method = ep.method;
        float percent = (float)(100 * getFrequency()) / renderer.samples;
        StringBuffer label = new StringBuffer();
        label.append(ep.lineNumber).append(':');
        ProfileViewer.pad(label, LINE_NUMBER_PADDING);
        label.append(percent).append('%');
        return label.toString();
    }

    /**
     * The padding to apply for the line number of an execution point.
     */
    public static final char[] LINE_NUMBER_PADDING = "      ".toCharArray();

}

/**
 * A tree renderer that can render ProfileNodes.
 */
class ProfileTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Some preallocated fonts for differentiating the nodes' text.
     */
    public final Font plain, italic, bold, fixed;

    /**
     * The total number of samples comprising the profile being rendered.
     */
    public final int samples;

    /**
     * Constructs a ProfileTreeCellRenderer.
     *
     * @param samples  the total number of samples comprising the profile being rendered
     */
    ProfileTreeCellRenderer (int samples) {
        this.samples = samples;

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
        ProfileNode node = (ProfileNode)value;
        super.getTreeCellRendererComponent(tree, node.render(this), sel, expanded, leaf, row, hasFocus);
        return this;
    }
}

/**
 * A GUI panel for hiding nodes in the tree whose frequency falls below a user-entered threshold.
 */
class HideNodesPanel extends JPanel {

    /**
     * The button that applies the hiding/unhiding.
     */
    private final JButton applyButton;

    /**
     * Constructs the panel.
     *
     * @param viewer            the viewer that contains the profile tree
     * @param samples           the total number of samples comprising the profile
     * @param initialThreshold  the initial frequency threshold of the nodes to be displayed
     */
    HideNodesPanel(final ProfileViewer viewer, final int samples, float initialThreshold) {

        FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
        setLayout(layout);

        // Search text panel
        final JTextField thresholdField = new JTextField(5);
        add(new JLabel("Hide nodes with frequency below: "));
        add(thresholdField);
        thresholdField.setText("" + initialThreshold);

        // Buttons
        applyButton = new JButton("Apply");
        add(applyButton);

        // Actions for buttons
        ActionListener buttonListener = new ActionListener() {
            float lastThreshold = -1F;

            private void updateThreshold(final float threshold) {
                if (threshold != lastThreshold) {
                    viewer.hideNodes(new ProfileNode.Filter() {
                        public boolean matches(ProfileNode node) {
                            float percent = (float)(100 * node.getFrequency()) / samples;
                            return percent < threshold;
                        }
                    });
                    lastThreshold = threshold;

                    // Report the number of nodes hidden/unhidden in the status bar
                    ProfileNode root = viewer.getProfileRoot();
                    int hidden = root.getNodeCount(true, false);
                    int total = root.getNodeCount(false, false);
                    viewer.updateStatusBar(" " + hidden + " of " + total + " nodes are hidden");
                }
            }

            public void actionPerformed(ActionEvent e) {
                String text = thresholdField.getText().trim();
                try {
                    float threshold = Float.parseFloat(text);
                    if (threshold < 0 || threshold > 100) {
                        throw new NumberFormatException();
                    }
                    updateThreshold(threshold);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Threshold value must be a number between 0 and 100");
                }
            }
        };
        applyButton.addActionListener(buttonListener);
        thresholdField.addActionListener(buttonListener);
    }

    /**
     * Programmatically press the "Apply" button on this panel.
     */
    public void apply() {
        applyButton.doClick();
    }
}
