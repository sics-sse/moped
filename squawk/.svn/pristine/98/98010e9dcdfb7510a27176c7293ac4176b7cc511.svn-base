/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2010 Oracle. All Rights Reserved.
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
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */

package com.sun.squawk.vm;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.ChannelConstants;

/**
 * This class contains the host side of the Squawk channel architecture that is used
 * to implement IO, graphics and events. A separate ChannelIO instance provides the
 * IO system for each isolate.
 */
public class ChannelIO implements java.io.Serializable {

    /**
     * The table of channel I/O instances.
     */
    private static SerializableIntHashtable contexts = new SerializableIntHashtable();

    /**
     * The next availible context identifier.
     */
    private static int nextContext = 1;

    /**
     * The context identifier.
     */
    private int context;

    /**
     * Flags if this IO system is in rundown mode.
     */
    private boolean rundown;

    /**
     * The table of channels open by this IO system.
     */
    private SerializableIntHashtable channels = new SerializableIntHashtable();

    /**
     * Used to allocate a new channel ID. Channels 0, 1, and 2 are reserved.
     */
    private int nextAvailableChannelID = ChannelConstants.CHANNEL_LAST_FIXED + 1;

/*if[ENABLE_CHANNEL_GUI]*/
    /**
     * The special events channel.
     */
    private GUIInputChannel guiInputChannel;

    /**
     * The special graphics channel.
     */
    private GUIOutputChannel guiOutputChannel;
/*end[ENABLE_CHANNEL_GUI]*/

    /**
     * The class of the exception (if any) that occurred in the last call to
     * {@link #execute} with a channel ID of 0 or to {@link #hibernate}.
     */
    private String exceptionClassName;

    /**
     * The result of the last channel operation.
     */
    private long theResult;

    /**
     * Determines if tracing is enabled.
     */
    static final boolean TRACING_ENABLED = System.getProperty("cio.tracing", "false").equals("true");

    /**
     * Determines if logging is enabled.
     */
    static final boolean LOGGING_ENABLED = System.getProperty("cio.logging", "false").equals("true");

    /**
     * Temparary byte array to hold serialised data
     */
    transient byte[] hibernationData;

    /**
     * Prints a trace message on a line.
     *
     * @param msg   the trace message to print
     */
    static void trace(String msg) {
        System.out.println(msg);
    }

    /**
     * Executes an operation on a given channel.
     *
     * @param  context the identifier of the channel context.
     * @param  op      the operation to perform
     * @param  channel the identifier of the channel to execute the operation on
     * @param  i1
     * @param  i2
     * @param  i3
     * @param  i4
     * @param  i5
     * @param  i6
     * @param  o1
     * @param  o2
     * @return the result
     */
    public static int execute(int    context,
                              int    op,
                              int    channel,
                              int    i1,
                              int    i2,
                              int    i3,
                              int    i4,
                              int    i5,
                              int    i6,
                              Object o1,
                              Object o2)
    {
        if (context == -1) {
            switch (op) {
                case ChannelConstants.GLOBAL_GETEVENT: {
                    return EventQueue.getEvent();
                }
                case ChannelConstants.GLOBAL_POSTEVENT: {
		    //EventQueue.unblock(EventQueue.getNextEventNumber());   //  Need generic event to unblock waiters
                    EventQueue.sendNotify();
                    return ChannelConstants.RESULT_OK;
                }
                case ChannelConstants.GLOBAL_WAITFOREVENT: {
                    long time = (((long)i1) << 32) | (((long)i2) & 0x00000000FFFFFFFFL);
                    EventQueue.waitFor(time);
                    return ChannelConstants.RESULT_OK;
                }
                case ChannelConstants.GLOBAL_CREATECONTEXT: {
                    if (contexts.get(1) == null) {   // let all Isolates share the same context
                        ChannelIO cio = createCIO((byte[])o1);
                        int index = 1; // nextContext++;
                        contexts.put(index, cio);
                        cio.context = index;
                    }
                    return 1;
                }
                default: {
                    throw Assert.shouldNotReachHere("Unknown global IO operation opcode: " + op);
                }
            }
        } else {
            ChannelIO cio = (ChannelIO)contexts.get(context);
            if (cio == null) {
                return ChannelConstants.RESULT_BADCONTEXT;
            }
            int result = cio.execute(op, channel, i1, i2, i3, i4, i5, i6, o1, o2);
            return result;
        }
    }

    /**
     * Gets the result cio index.
     *
     * @return  the value
     */
    int getCIOIndex() {
        return context;
    }

    private static ChannelIO deserialize(byte[] serializedData) throws IOException, ClassNotFoundException {
        //System.out.println("Deserializing channel from "+file);
        ByteArrayInputStream in = new ByteArrayInputStream(serializedData);
        ObjectInputStream ois = new ObjectInputStream(in);
        ChannelIO cio = (ChannelIO)ois.readObject();
        in.close();
        return cio;
    }

    /**
     * Static constructor to create a ChannelIO system for an isolate.
     *
     * @param serializedData  the raw bytes of the serialised data.
     *
     * @return ChannelIO the created IO system
     */
    private static ChannelIO createCIO(byte[] serializedData) {
        if (serializedData != null) {
            try {
                ChannelIO cio = deserialize(serializedData);
                cio.rundown = false;
/*if[ENABLE_CHANNEL_GUI]*/
                cio.guiInputChannel.addToGUIInputQueue(ChannelConstants.GUIIN_REPAINT, 0, 0, 0); // Add a repaint command
/*end[ENABLE_CHANNEL_GUI]*/
                return cio;
            } catch (Exception ex) {
                System.err.println("Error deserializing channel "+ex);
            }
        }
        if (ChannelIO.TRACING_ENABLED) trace("new channel for isolate");
        return new ChannelIO();
    }

    /**
     * Constructor.
     */
    private ChannelIO() {
/*if[ENABLE_CHANNEL_GUI]*/
        guiInputChannel  = new GUIInputChannel(this, ChannelConstants.CHANNEL_GUIIN);
        guiOutputChannel = new GUIOutputChannel(this, ChannelConstants.CHANNEL_GUIOUT, guiInputChannel);
        channels.put(ChannelConstants.CHANNEL_GUIIN, guiInputChannel);
        channels.put(ChannelConstants.CHANNEL_GUIOUT, guiOutputChannel);
/*end[ENABLE_CHANNEL_GUI]*/
    }

    /**
     * Creates a new stream channel.
     */
    GenericConnectionChannel createGenericConnectionChannel() {
        while (channels.get(nextAvailableChannelID) != null) {
            nextAvailableChannelID++;
        }
        int channelID = nextAvailableChannelID++;
        GenericConnectionChannel channel = new GenericConnectionChannel(this, channelID);
        channels.put(channelID, channel);
        if (ChannelIO.TRACING_ENABLED) trace("++createGenericConnectionChannel = "+channel);
        return channel;
    }

    /**
     * Executes an operation on a given channel.
     *
     * @param  op        the operation to perform
     * @param  channelID the identifier of the channel to execute the operation on
     * @param  i1
     * @param  i2
     * @param  i3
     * @param  i4
     * @param  i5
     * @param  i6
     * @param  o1
     * @param  o2
     * @return the result
     */
    private int execute(
                         int    op,
                         int    channelID,
                         int    i1,
                         int    i2,
                         int    i3,
                         int    i4,
                         int    i5,
                         int    i6,
                         Object o1,
                         Object o2
                       ) {
        if (ChannelIO.TRACING_ENABLED) {
/*if[DEBUG_CODE_ENABLED]*/
            trace("execute channel "+channelID+" op " +  ChannelConstants.getMnemonic(op));
/*else*/
            trace("execute channel "+channelID+" op ");
/*end[DEBUG_CODE_ENABLED]*/                       
        }

        /*
         * Reject the I/O request if the ChannelIO has been rundown.
         */
        if (rundown) {
            if (ChannelIO.TRACING_ENABLED) trace("execute status = javax.microedition.io.ConnectionNotFoundException");
            return raiseException("IsolateRundownError");
        }

        /*
         * Deal with some special case resuests.
         */
        switch (op) {
            case ChannelConstants.CONTEXT_GETRESULT: {
                return (int)theResult;
            }
            case ChannelConstants.CONTEXT_GETRESULT_2: {
                return (int)(theResult >>> 32);
            }
            case ChannelConstants.CONTEXT_GETERROR: {
                return getError();
            }
            case ChannelConstants.CONTEXT_GETCHANNEL: {
                switch (i1) { // Channel type
/*if[ENABLE_CHANNEL_GUI]*/
                    case ChannelConstants.CHANNEL_GUIIN:
                    case ChannelConstants.CHANNEL_GUIOUT: {
                        return i1; // Both channels are already open and the channel number is the channel type
                    }
/*end[ENABLE_CHANNEL_GUI]*/
                    case ChannelConstants.CHANNEL_GENERIC: {
                        return createGenericConnectionChannel().getChannelID();
                    }
                    default: {
                        return ChannelConstants.RESULT_BADPARAMETER;
                    }
                }
            }
            case ChannelConstants.CONTEXT_FREECHANNEL: {
                Channel channel = (Channel)channels.get(channelID);
                if (channel != null) {
//System.err.println("freeing channel " + context + ":" + channelID);
                    channel.close();
                }
                channels.remove(channelID);
                return ChannelConstants.RESULT_OK;
            }

            case ChannelConstants.CONTEXT_GETHIBERNATIONDATA: {
                if (this.hibernationData != null) {
                    System.arraycopy(hibernationData, 0, o2, 0, i2);
                }
                hibernationData = null;
                return ChannelConstants.RESULT_OK;
            }

            case ChannelConstants.CONTEXT_HIBERNATE: {
                hibernationData = new byte[0]; // hibernate();
                if (hibernationData != null) {
                    return hibernationData.length;
                } else {
                    return ChannelConstants.RESULT_EXCEPTION;
                }
            }

            case ChannelConstants.CONTEXT_DELETE: { // since all isolates use same context never delete it
//                close();
                // Remove this context object from the table of contexts
//                contexts.remove(this.context);
                return ChannelConstants.RESULT_OK;
            }
        }

        /*
         * Lookup the channel and reject the request if it does not exist.
         */
        Channel channel = (Channel)channels.get(channelID);
        if (channel == null) {
            if (ChannelIO.TRACING_ENABLED) trace("execute status = javax.microedition.io.ConnectionNotFoundException");
            return raiseException("javax.microedition.io.ConnectionNotFoundException");
        }

        /*
         * Execute the channel request and save the result if it worked.
         */
        channel.clearResult(); // Set the result to zero by default
        int status;
        try {
            status = channel.execute(op, i1, i2, i3, i4, i5, i6, o1, o2);
        } catch (Throwable ex) {
//System.out.println("Exception -\n"+ex);
//ex.printStackTrace();
            status = raiseException(ex.toString());
        }
        theResult = channel.getResult();

        /*
         * Return the status of the operation.
         */
        if (ChannelIO.TRACING_ENABLED) trace("execute status = "+status);
        return status;
    }

    /**
     * Get the next character of the error.
     *
     * @return the next character or 0 if none remain
     */
    private int getError() {
        if (exceptionClassName != null) {
            int ch = exceptionClassName.charAt(0);
            int length = exceptionClassName.length();
            if (length == 1) {
                exceptionClassName = null;
            } else {
                exceptionClassName = exceptionClassName.substring(1);
            }
            return ch;
        }
        return 0;
    }

    /**
     * Closes the IO system for an isolate.
     */
    private void close() {
        if (rundown == false) {
            rundown = true;
            Enumeration e = channels.elements();
            while (e.hasMoreElements()) {
                Channel channel = (Channel)e.nextElement();
                if (channel != null) {
                    channel.close();
                }
            }
            if (ChannelIO.TRACING_ENABLED) trace("++close ");
        }
    }



    /**
     * Hibernates the IO system for an isolate.
     *
     * @return   the positive identifier of the serialized file or a negative 'x' where x is the length of the
     *           name of the class of the exception that occurred during serialization
     */
    private byte[] hibernate() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            return baos.toByteArray();
        } catch (Throwable t) {
            System.err.println("Error serializing channel: " + t);
            t.printStackTrace();
            raiseException(t.getClass().getName());
            return null;
        }
    }


    /**
     * Registers an exception that occurred on a non-channel specific call to this IO system.
     *
     * @param exceptionClassName   the name of the class of the exception that was raised
     * @return the negative value returned to the Squawk code indicating both that an error occurred
     *         as well as the length of the exception class name
     */
    private int raiseException(String exceptionClassName) {
        //System.out.println("raiseException >>>> "+exceptionClassName);
        if (exceptionClassName.length() == 0) {
            exceptionClassName = "?raiseException?";
        }
        this.exceptionClassName = exceptionClassName;
        return ChannelConstants.RESULT_EXCEPTION;
    }

    /*
     * unblock
     */
    void unblock(int event) {
        EventQueue.unblock(event);
    }


    /*=======================================================================*\
     *                           I/O Server code                             *
    \*=======================================================================*/

    /*
     * The following code can be run from the command line to make a standalone
     * I/O server. Squawk then has to be run with the -Xioport:8888 switch to
     * commumicate with the server.
     */

    /**
     * Debugging flag.
     */
    private static boolean DEBUG = false;

    /**
     * Timing interval. A zero value disables timing.
     */
    private static int timing = 0;

    /**
     * Prints the usage message.
     *
     * @param  errMsg  an optional error message
     */
    private static void usage(String errMsg) {
        PrintStream out = System.out;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("Usage: ChannelIO [-options]");
        out.println("where options include:");
        out.println();
        out.println("    -port:<port>  the port to listen on (default=9090)");
        out.println("    -d            run in debug mode");
        out.println("    -t<n>         show timing info every 'n' IO operations");
        out.println();
    }

    /**
     * Prints the time stats.
     */
    static class IOTimeInfo {
        long execute;
        long receive;
        long send;
        int count;

        void dump(PrintStream out) {
            long total = (receive + execute + send);
            out.println("average time per IO operation: ");
            out.println("    receive: " + (receive / count) + "ms");
            out.println("    send:    " + (send / count) + "ms");
            out.println("    execute: " + (execute / count) + "ms");
            out.println("    total:   " + (total / count) + "ms");
        }
    }

    /**
     * Entry point for the I/O server
     */
    public static void main(String[] args) {
        int port = 9090;

        // Parse the command line arguments
        for (int argc = 0; argc != args.length; ++argc) {
            String arg = args[argc];
            if (arg.startsWith("-port:")) {
                port = Integer.parseInt(arg.substring("-port:".length()));
            } else if (arg.equals("-d")) {
                DEBUG = true;
            } else if (arg.startsWith("-t")) {
                timing = Integer.parseInt(arg.substring(2));
            } else {
                usage("Unknown option: " + arg);
                System.exit(1);
            }
        }

        try {
            System.out.println("Starting server on port "+port);
            StreamConnectionNotifier ssocket = (StreamConnectionNotifier)Connector.open("serversocket://:"+port);

            IOTimeInfo timingInfo = null;
            if (timing != 0) {
                timingInfo = new IOTimeInfo();
            }

            for (;;) {
                try {
                    if (DEBUG) System.out.println("listening on port " + port);
                    StreamConnection con = ssocket.acceptAndOpen();
                    if (DEBUG) System.out.println("Got connection");
                    DataInputStream  in  = con.openDataInputStream();
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(con.openOutputStream()));
                    for (;;) {
                        long start = 0L;
                        long end = 0L;
                        if (timing != 0) {
                            start = System.currentTimeMillis();
                        }
                        if (DEBUG) System.err.print("IO server receiving: ");
                        int cio    = readInt(in, "cio");            // 0
                        int op     = readInt(in, "op");             // 1
                        int cid    = readInt(in, "cid");            // 2
                        int i1     = readInt(in, "i1");             // 3
                        int i2     = readInt(in, "i2");             // 4
                        int i3     = readInt(in, "i3");             // 5
                        int i4     = readInt(in, "i4");             // 6
                        int i5     = readInt(in, "i5");             // 7
                        int i6     = readInt(in, "i6");             // 8
                        int retLth = readInt(in, "lth");            // 9
                        Object s1  = readObject(in);

                        if (timing != 0) {
                            end = System.currentTimeMillis();
                            timingInfo.receive += (end - start);
                            start = end;
                        }

                        if (DEBUG) System.err.println();
                        byte[] buf = new byte[retLth];
                        int status = -1;
                        try {
                            status = execute(cio, op, cid, i1, i2, i3, i4, i5, i6, s1, buf);
                        } catch(Throwable ex) {
                            System.err.println("Exception cause in I/O server "+ex);
                            buf = new byte[0];
                        }
                        int low  = (cio == -1) ? -1 : execute(cio, ChannelConstants.CONTEXT_GETRESULT,   -1, 0, 0, 0, 0, 0, 0, null, null);
                        int high = (cio == -1) ? -1 : execute(cio, ChannelConstants.CONTEXT_GETRESULT_2, -1, 0, 0, 0, 0, 0, 0, null, null);

                        if (timing != 0) {
                            end = System.currentTimeMillis();
                            timingInfo.execute += (end - start);
                            start = end;
                        }

                        if (DEBUG) System.err.print("IO server sending: ");
                        writeInt(out, "magic",  0xCAFEBABE);
                        writeInt(out, "status", status);
                        writeInt(out, "r-low ", low);
                        writeInt(out, "r-high", high);
                        writeInt(out, "resLth", buf.length);
                        out.write(buf);
                        out.flush();

                        if (timing != 0) {
                            end = System.currentTimeMillis();
                            timingInfo.send += (end - start);
                            timingInfo.count++;

                            if ((timingInfo.count % timing) == 0) {
                                timingInfo.dump(System.err);
                            }
                        }
                    }
                    //out.close();
                    //in.close();
                    //con.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }
    }

    /**
     * Read an int from the input stream
     *
     * @param in the stream
     * @param name debugging label
     * @return the int
     */
    private static int readInt(DataInputStream in, String name) throws IOException {
        int val = in.readInt();
        if (DEBUG) System.err.print(name + "=" + val+ " ");
        return val;
    }

    /**
     * Read an int from the input stream
     *
     * @param in the stream
     * @param name debugging label
     * @return the int
     */
    private static void writeInt(DataOutputStream out, String name, int val) throws IOException {
        out.writeInt(val);
        if (DEBUG) System.err.print(name + "=" + val+ " ");
    }

    /**
     * Read an array from the input stream
     *
     * @param in the stream
     * @return the array object
     */
    private static Object readObject(DataInputStream in) throws IOException {
        int cno = in.readInt();
        int lth = in.readInt();
        if (cno == 0) {
            return null;
        } else if (cno == CID.BYTE_ARRAY || cno == CID.STRING_OF_BYTES) {
            byte[] buf = new byte[lth];
            in.readFully(buf);
            return (cno == CID.BYTE_ARRAY) ? (Object)buf : new String(buf);
        } else if (cno == CID.CHAR_ARRAY || cno == CID.STRING) {
            char[] buf = new char[lth];
            for (int i = 0 ; i < lth ; i++) {
                buf[i] = in.readChar();
            }
            return (cno == CID.CHAR_ARRAY) ? (Object)buf : new String(buf);
        } else if (cno == CID.INT_ARRAY) {
            int[] buf = new int[lth];
            for (int i = 0 ; i < lth ; i++) {
                buf[i] = in.readInt();
            }
            return buf;
        } else {
            System.err.println("Bad object type "+cno);
            return null;
        }
    }
}
