/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.util.Assert;

/**
 * Class that manages callbacks for occasional events, which might be notified by
 * isolate's other than the one that registered the callback objects.
 *
 * This class manages executing callbacks in the context of the isolate that registered the callback.
 */
public final class CallbackManager {

    /**
     * If runHooks is only called once, then we can clean up and clear as we go.
     */
    private final boolean runOnce;

    /**
     * Set to true by runHooks().
     */
    private boolean ran;

    /**
     * The set of CallbackGroups (never null).
     */
    private final SquawkVector hooks;

    /**
     * Creates a new instance of CallbackManager
     *
     * @param runOnce if true clean up aggressively while executing run()
     */
    public CallbackManager(boolean runOnce) {
        this.runOnce = runOnce;
        this.hooks = new SquawkVector(Isolate.ENABLE_MULTI_ISOLATE ? 3 : 1);
    }

    /**
     * Return the callback group for this isolate.
     *
     * @param iso the isolate to search for
     * @return the callback group for the isolate, or null.
     */
    private CallbackGroup findGroup(Isolate iso) {
        int len = hooks.size();
        for (int i = 0; i < len; i++) {
            CallbackGroup cbg = (CallbackGroup)hooks.elementAt(i);
            if (cbg.iso == iso) {
                return cbg;
            }
        }
        return null;
    }

    /**
     * Should only be run once, like for a shutdown hook.
     * @return true if this callback should be
     */
    public boolean isRunOnce() {
        return runOnce;
    }

//    /**
//     * Return true if this should only be run once, like for a shutdown hook.
//     */
//    public int count() {
//        int len = hooks.size();
//        int count = 0;
//        for (int i = 0; i < len; i++) {
//            CallbackGroup cbg = (CallbackGroup)hooks.elementAt(i);
//            count += cbg.count();
//        }
//        return count;
//    }

    /**
     * Add a hook to run in isolate <code>iso</code>.
     *
     * @param iso the isolate context in which to run hook
     * @param hook the Runnable to run
     * @throws IllegalArgumentException if the hook is already registered to run in this isolate.
     */
    public synchronized void add(Isolate iso, Runnable hook) {
        if (hook == null) {
            throw new IllegalArgumentException();
        }

        CallbackGroup cbg = findGroup(iso);
        if (cbg == null) {
            cbg = new CallbackGroup(iso);
            hooks.addElement(cbg);
        }
        cbg.add(hook);
    }

    /**
     * Add a hook to run in the current isolate's context.
     *
     * @param hook
     * @throws IllegalArgumentException if the hook is already registered to run in this isolate.
     */
    public void add(Runnable hook) {
        add(Isolate.currentIsolate(), hook);
    }

    /**
     * Remove a hook previously-registered for the isolate <ocde>iso</code>.  Like the add method, this method
     * does not do any security checks.
     *
     * @param iso the isolate context to search
     * @param hook the hook to search for
     * @return true if hook was registered in the iso context.
     */
    public synchronized boolean remove(Isolate iso, Runnable hook) {
        if (hook == null) {
            throw new IllegalArgumentException();
        }

        CallbackGroup cbg = findGroup(iso);
//        if (cbg == null) {
//            System.out.println("findGroup FAILED for " + iso + " " + hook);
//        }
        if (cbg != null && cbg.remove(hook)) {
            if (cbg.hooks.size() == 0) {
                hooks.removeElement(cbg); // remove empty groups
            }
            return true;
        }
//        System.out.println("cbg.remove FAILED for " + iso + " " + hook);
        return false;
    }

    /**
     * Remove a hook previously-registered for the isolate <ocde>iso</code>. The wrapped hook
     * implements some callback interface, but we don't care which.
     *
     * @param iso the isolate context to search
     * @param hook the wrapped hook to search for
     * @return a HookWrapper wrapper for hook was found; otherwise null.
     */
    synchronized HookWrapper findHookWrapper(Isolate iso, Object hook) {
        if (hook == null) {
            throw new IllegalArgumentException();
        }

        CallbackGroup cbg = findGroup(iso);
        if (cbg != null) {
            return cbg.findHookWrapper(hook);
        }

        return null;
    }


    /**
     * Remove an actual hook based on some wrapped hook.
     */

    /**
     * Remove a hook previously-registered for the current isolate.  Like the add method, this method
     * does not do any security checks.
     *
     * @param hook the hook to search for
     * @return true if hook was registered in the iso context.
     */
    public boolean remove(Runnable hook) {
        return remove(Isolate.currentIsolate(), hook);
    }

    /**
     * Remove all hooks previously-registered for the given isolate.
     *
     * @param iso the isolate context to search
     * @return true if any hooks were registered in the iso context.
     */
    public synchronized boolean remove(Isolate iso) {
        CallbackGroup cbg = findGroup(iso);
        if (cbg != null) {
            hooks.removeElement(cbg); // remove empty groups
            return true;
        }
        return false;
    }

    /**
     * Remove all hooks.
     */
    public synchronized void removeAll() {
        hooks.removeAllElements();
    }

    /*
     * Run all registered hooks. Can be called from a different isolate than the isolate that registered the callbacks.
     * Any hooks added or removed by calling the hooks are ignoreed during the execution of this method.
     */
    public void runHooks() {
        CallbackGroup[] grps; // snapshot of groups;

        synchronized (this) {
            if (runOnce & ran) {
                // can happen as a race...
                if (VM.isVeryVerbose()) {
                    System.err.println("Already ran one-time callbacks");
                }
                return;
            }
            ran = true;
            grps = new CallbackGroup[hooks.size()];
            hooks.copyInto(grps);
        }

        for (int i = 0; i < grps.length; i++) {
            CallbackGroup cbg = grps[i];
            if (!Isolate.ENABLE_MULTI_ISOLATE || cbg.iso.isAlive()) {
                cbg.run();
            } else {
                System.err.println("Tried to execute callbacks in the context of an isolate that is no longer alive: " + cbg.iso);
                System.err.println("Skipping that set of callbacks");
            }
            if (runOnce) {
                hooks.setElementAt(null, i);
            }
        }
    }

    /**
     * If this CallbackManager contains any callbacks to be run in a context other than the current context, return true.
     * @return true if contains cross-isolate callback.
     */
    public synchronized boolean containsOtherIsolates() {
        if (hooks != null) {
            for (int i = 0; i < hooks.size(); i++) {
                CallbackGroup cbg = (CallbackGroup)hooks.elementAt(i);
                if (cbg.iso != Isolate.currentIsolate()) {
                    return true;
                }
            }
        }
        return false;
    }

}

/**
 * A thread that will run a set of callbacks in a specifc isolate's context.
 */
class CallbackThread extends CrossIsolateThread {
    CallbackGroup cbg;

    CallbackThread(CallbackGroup cbg) {
        super(cbg.iso, "callback thread");
        this.cbg = cbg;
    }

    public void run() {
        cbg.runAllHooksInContext();
    }

}

/**
 * Interface for hooks that are wrappers on other hooks. The type of the wrapped hook
 * could be any interface. We need to be able to remove a wrpeer hook based on looking up the wrapped hook,
 * so this interface
 */
interface HookWrapper extends Runnable {

    /**
     * Return the wrapped hoook that the user may have registered.
     * Used for finding the actual hook (a HookWrapper) based on
     * the wrapped hook. The wrapped hook might be a Runnable, or it may implement some other interface.
     * At this level, we don't care.
     */
    Object getWrappedHook();
}

/**
 * A group of callbacks to be called in the context of the same Isolate.
 */
final class CallbackGroup {

    final Isolate iso;

    SquawkVector hooks;

    /**
     * Create a group.
     *
     * @param iso the isolate context to run all this group's callbacks.
     */
    CallbackGroup(Isolate iso) {
        this.iso = iso;
        this.hooks = new SquawkVector(3);
    }

    /**
     * Return the number of hooks in this group.
     */
    int count() {
        return hooks.size();
    }

    /**
     * Add a hook to this group.
     * @param hook
     * @throws IllegalArgumentException if the hook is already registered to run in this group.
     */
    synchronized void add(Runnable hook) {
        if (findHook(hook) >= 0) {
            throw new IllegalArgumentException();
        }
        hooks.addElement(hook);
    }

    private int findHook(Runnable hook) {
        int len = hooks.size();
        for (int i = 0; i < len; i++) {
            if (hooks.elementAt(i) == hook) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Remove a hook from this group.
     * @param hook
     * @return true if the hook was found.
     */
    synchronized boolean remove(Runnable hook) {
        int i = findHook(hook);
        if (i >= 0) {
            hooks.removeElementAt(i);
            return true;
        }
        return false;
    }

    /**
     * Find the index of a HookWrapper that wraps this hook.
     *
     * @param hook some user hook that implements some interface (we don't care)
     * @return the index of a HookWrapper that wraps "hook"; otherwise return -1;
     */
    synchronized HookWrapper findHookWrapper(Object hook) {
        int len = hooks.size();
        for (int i = 0; i < len; i++) {
            Object h = hooks.elementAt(i);
            if (h instanceof HookWrapper) {
                HookWrapper hw = (HookWrapper)h;
                if (hw.getWrappedHook() == hook) {
                    return hw;
                }
            }
        }
        return null;
    }

    /**
     * Run all of the hooks in the current isolate. RuntimeExceptions thrown by callback are squashed.
     */
    void runAllHooksInContext() {
        Assert.that(Isolate.currentIsolate() == iso);
        // run serially, to minimize memory overhead.
        Runnable[] hks; // snapshot of hooks;

        synchronized (this) {
            hks = new Runnable[hooks.size()];
            hooks.copyInto(hks);
        }

        int len = hooks.size();
        for (int i = 0; i < len; i++) {
            Runnable hook = hks[i];
            try {
//              System.out.println("Running a hook in runAllHooksInContext" + hook);
                if (VM.isVerbose()) {
                    System.out.println("Running hook: " + hook);
                }
                hook.run();
            } catch (RuntimeException e) {
                System.err.print("Exception thrown executing callback. Exception printed and ignored:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Run all of the callbacks in this group's isolate context.
     */
    void run() {
//        System.out.println("Running hooks " + count() + " in CallbackGroup");
        if (!Isolate.ENABLE_MULTI_ISOLATE || Isolate.currentIsolate() == iso) {
            runAllHooksInContext();
        } else {
            Thread thr = new CallbackThread(this);
            thr.start();
            try {
                thr.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
    }

}
