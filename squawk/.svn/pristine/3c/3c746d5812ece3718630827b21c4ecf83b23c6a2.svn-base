/*
 * Copyright 2002-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.tck.cldc.javatest.agent;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.sun.cldc.communication.Client;
import com.sun.cldc.communication.MultiClient;
import com.sun.squawk.VM;
import com.sun.tck.cldc.javatest.util.UTFConverter;
import com.sun.tck.cldc.lib.Status;
import com.sun.tck.cldc.lib.Test;

public class CldcAgent {

    public static void main(String[] args) {
        CldcAgent agent = new CldcAgent();
        if (args.length == 0) {
            args = agent.getArgsFromResource("/agent.dat");
        }
        agent.init(args);
        agent.run();
        agent.trace("CldcAgent successfully finished");
        // >>> Change for Squawk <<<
        VM.stopVM(0);
        // >>> Change for Squawk <<<
    }

    public void init(String[] args) {
        String clientClass = null;
        String[] clientArgs = null;
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-client") &&
	            i+1 < args.length) {
	        Vector v = new Vector();
	        while (i+1 < args.length &&
	                ! args[i+1].equals("-verbose") &&
	                ! args[i+1].equals("-bundleId") &&
	                ! args[i+1].equals("-timeout")) {
	            v.addElement(args[++i]);
	        }
	        if (v.size() == 0) {
                    throw new RuntimeException(
                            "Empty client defined");
	        }
	        clientClass = (String) v.firstElement();
	        v.removeElementAt(0);
	        clientArgs = new String[v.size()];
	        v.copyInto(clientArgs);
	    } else if (args[i].equals("-verbose")) {
	        verbose = true;
	    } else if (args[i].equals("-bundleId") &&
	            i+1 < args.length) {
	        bundleId = args[++i];
	    } else if (args[i].equals("-timeout") &&
	            i+1 < args.length) {
                try {
	            timeout = Long.parseLong(args[++i]) * 1000;
	        } catch (NumberFormatException nfe) {
	        }           
            } else {
	        throw new RuntimeException(
	                "Unrecognized argument for the agent:" +  args[i]);
	    }
	}
	if (clientClass == null) {
	    throw new RuntimeException("Client not defined");
	}
	initClient(clientClass, clientArgs);
    }

    public void run() {
        byte[] testRequest = null;

        while (true) {
            endTime = System.currentTimeMillis() + timeout;
            testRequest = getNextTest();
            trace("Successfully received information about the next test");
            if (testRequest == null) {
                trace("No more tests in the bundle");
                return;
            }
            trace("Available memory: " + Runtime.getRuntime().freeMemory());
            byte[] res = handleRequest(testRequest);
            sendTestResult(res);
            trace("Successfully sent back the test result");
        }
    }

    protected void initClient(String clientClass, String[] clientArgs) {
	try {
            client = (Client) Class.forName(clientClass).newInstance();
            trace(clientClass + " successfully instantiated");
        } catch (ClassCastException e) {
	    throw new RuntimeException(clientClass + 
	            " does not implement Client interface");
        } catch (ClassNotFoundException e) {
	    throw new RuntimeException(clientClass + 
	            " not found");
        } catch (InstantiationException e) {
	    throw new RuntimeException(clientClass + 
	            " cannot be instantiated");
        } catch (IllegalAccessException e) {
	    throw new RuntimeException(clientClass + 
	            " cannot be accessed");
        }
        client.init(clientArgs);
        trace(clientClass + " successfully initialized");
        try {
            ((MultiClient)client).setBundleID(bundleId);
            trace(clientClass + " received bundle Id: " + bundleId);
        } catch (ClassCastException e) {
        }
    }

    protected String[] getArgsFromResource(String name) {
        InputStream argsStream = getClass().getResourceAsStream(name);
        if(argsStream == null) {
            throw new RuntimeException(
                    "Agent configuration resource can not be opened");
        }
        return UTFConverter.UTFStreamToStrings(argsStream);
    }

    protected byte[] handleRequest(byte[] request) {
        String[] req = UTFConverter.bytesToStrings(request);
        // now we have test ID, executeClass, then executeArgs
        String[] execArgs = new String[req.length - 2];
        System.arraycopy(req, 2, execArgs, 0, req.length - 2);
        refBaos.reset();
        logBaos.reset();
        trace("Executing " + req[0]);
        Status s = executeTest(req[1], execArgs, new PrintStream(refBaos),
                new PrintStream(logBaos));
        trace("Status: " + s);
        String[] res = new String[4];
        res[0] = req[0]; // copying test ID
        res[1] = s.toString();
        res[2] = new String(logBaos.toByteArray());
        res[3] = new String(refBaos.toByteArray());
        return UTFConverter.stringsToBytes(res);
    }

    protected Status executeTest(String className, String[] args,
            PrintStream ref, PrintStream log) {
        try {
            Class c = Class.forName(className);
            Test t = (Test) (c.newInstance());
            return t.run(args, log, ref);
        } catch (ClassNotFoundException e) {
            return Status.failed("Class not found: " + className);
        } catch (InstantiationException e) {
            return Status.failed("Could not instantiate class: " + className);
        } catch (IllegalAccessException e) {
            return Status.failed("IllegalAccessException trying to access " + className);
        } catch (Throwable t) {
            return Status.failed(t + " thrown while executing " + className);
        } finally {
            ref.flush();
            log.flush();
        }
    }

    protected void trace(String s) {
        if(verbose) {
            System.out.println("AGENT:     " + s);
        }
    }

    private byte[] getNextTest() {
        while (true) {
            try {
                return client.getNextTest();
            } catch (RuntimeException e) {
                trace("Failed to receive information about the next test: " + e);
                e.printStackTrace();
                if (System.currentTimeMillis() > endTime) {
                    trace("Timed out trying to get next test");
                    throw e;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {}
            }
        }
    };

    private void sendTestResult(byte[] res) {
        while (true) {
            try {
                client.sendTestResult(res);
                return;
            } catch (RuntimeException e) {
                trace("Failed to send back the test result: " + e);
                if (System.currentTimeMillis() > endTime) {
                    trace("Timed out trying to send back the test result");
                    throw e;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {}
            }
        }
    };

    private ByteArrayOutputStream refBaos = new ByteArrayOutputStream();
    private ByteArrayOutputStream logBaos = new ByteArrayOutputStream();

    private boolean verbose = false;
    private Client client = null;
    private String bundleId = null;
    private long timeout = 2 * 10 * 60 * 1000;
    private long endTime;
}
