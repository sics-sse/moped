//if[SUITE_VERIFIER]
/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.translator.ir.verifier;

import java.util.Hashtable;
import java.util.Vector;
import com.sun.squawk.util.Assert;
import com.sun.squawk.*;

/**
 * This class keeps track of non-backward targets that have not yet been processed.
 * This includes the targets of branch instruction, the beginning of exception handlers,
 * and the beginning and end of blocks of code protected by an exception handler.
 *
 */
final class TargetList {
    private abstract class Target {
        public int dst;

        Target(int dst) {
            this.dst = dst;
        }

        abstract public void error();
    }

    private class ForwardTarget extends Target {
        public int src;

        ForwardTarget(int dst, int src) {
            super(dst);
            this.src = src;
        }

        public void error() {
            VerifyError.check(false, src, "branch target " + dst + " is not the start of an instruction");
        }
    }

    private class ExceptionTarget extends Target {
        ExceptionTarget(int dst) {
            super(dst);
        }

        public void error() {
            VerifyError.check(false, 0, "exception boundary " + dst + " is not the start of an instruction");
        }
    }

    private Vector targets;

    TargetList() {
        targets = new Vector();
    }

    public void setIP(int ip) {
        if (targets.size() > 0) {
            Target t = (Target)targets.elementAt(0);
            if (ip > t.dst)
                t.error();
            if (ip == t.dst)
                targets.removeElementAt(0);
        }
    }

    private void insert(int ip, Target t) {
        for (int i = 0; i < targets.size(); i++) {
            int target = ((Target)targets.elementAt(i)).dst;
            if (target >= ip) {
                if (target > ip)
                    targets.insertElementAt(t, i);
                return;
            }
        }
    }

    public void setExceptionTarget(int ip) {
        insert(ip, new ExceptionTarget(ip));
    }

    public void setForwardTarget(int ip, int src) {
        insert(ip, new ForwardTarget(ip, src));
    }

    public boolean isEmpty() {
        return targets.isEmpty();
    }
}

class StackElement {
    private Klass klass;
    private int ip;

    StackElement(Klass k, int _ip) {
        klass = k;
        ip = _ip;
    }

    public final Klass getKlass() {
        return klass;
    }

    public final int getIP() {
        return ip;
    }
    
    public String toString() {
        return this.getClass().getName() + "[type: " + klass + " ip: " + ip + "]";
    }
    
    public StackElement merge(StackElement e, Klass generalType, Frame frame) {
        frame.checkMerge(!(e instanceof UninitializedReference),
                         "attempted to merge an uninitialized object with something else", this, e);
        if (getKlass() == Klass.NULL)
            return new StackElement(e.getKlass(), frame.getIP());
        else if (e.getKlass() == Klass.NULL)
            return new StackElement(getKlass(), frame.getIP());
        else if (generalType == Klass.OBJECT || generalType.getName().equals("java.lang.Offset")) {
            Klass k = getKlass();
            if (k == Klass.NULL)
                return new StackElement(e.getKlass(), frame.getIP());
            else {
                boolean loopDone = false;
                while (!Frame.isAssignable(k, e.getKlass())) {
                    loopDone = true;
                    k = k.getSuperclass();
                }
                return (loopDone) ? new StackElement(k, frame.getIP()) : this;
            }
        }
        else {
            Klass k1 = getKlass(), k2 = e.getKlass();
            if (Frame.isAssignable(k1, k2))
                return new StackElement(k1, frame.getIP());
            else if (Frame.isAssignable(k2, k1))
                return new StackElement(k2, frame.getIP());
            frame.checkMerge(false,
                             "types " + e.getKlass() + " and " + getKlass() + " do not match, general type is " + generalType,
                             this, e);
            return this;
        }
    }
}

class UninitializedReference extends StackElement {
    private boolean param;

    UninitializedReference(Klass k, int ip, boolean param) {
        super(k, ip);
        this.param = param;
    }

    UninitializedReference(Klass k, int ip) {
        this(k, ip, false);
    }

    public StackElement merge(StackElement e, Klass generalType, Frame frame) {
        frame.checkMerge(equals(e), "attempted to merge two different uninitialized objects", this, e);
        return this;
    }

    public final boolean checkKlass(Klass k) {
        if (k == getKlass())
            return true;
        if (param && k == getKlass().getSuperclass())
            return true;
        return false;
    }

    public boolean equals(Object o) {
        if (o instanceof UninitializedReference) {
            UninitializedReference r = (UninitializedReference)o;
            if (getKlass().getName().equals(r.getKlass().getName())) {
                if (getIP() == r.getIP()) {
                    return true;
                }
            }
        }
        return false;
    }
}

class StackObject extends StackElement {
    private Object object;

    StackObject(Object o, Klass k, int ip) {
        super(k, ip);
        object = o;
    }

    public final Object getObject() {
        return object;
    }

    public StackElement merge(StackElement e, Klass generalType, Frame frame) {
        if (e instanceof StackObject) {
            StackObject o = (StackObject)e;
            if (getObject() == o.getObject())
                return this;
        }
        return super.merge(e, generalType, frame);
    }
    
    public String toString() {
        return super.toString() + "[object: " + object + "]";
    }
}

class StackConstInt extends StackElement {
    private int val;

    StackConstInt(int i, Klass k, int ip) {
        super(k, ip);
        Assert.that(k == Klass.INT || k == Klass.SHORT || k == Klass.CHAR || k == Klass.BYTE || k == Klass.BOOLEAN);
        val = i;
    }

    public final int getValue() {
        return val;
    }

    public StackElement merge(StackElement e, Klass generalType, Frame frame) {
        if (e instanceof StackConstInt) {
            StackConstInt i = (StackConstInt)e;
            if (getValue() == i.getValue())
                return this;
        }
        return super.merge(e, generalType, frame);
    }
    
    public String toString() {
        return super.toString() + "[val: " + val + "]";
    }
}

class StackMethodSlot extends StackElement {
    private Method method;

    StackMethodSlot(Method m, int ip) {
        super(Klass.INT, ip);
        method = m;
    }

    public final Method getMethod() {
        return method;
    }

    public StackElement merge(StackElement e, Klass generalType, Frame frame) {
        if (e instanceof StackMethodSlot) {
            StackMethodSlot ms = (StackMethodSlot)e;
            if (getMethod() == ms.getMethod())
                return this;
        }
        return super.merge(e, generalType, frame);
    }
    
     public String toString() {
        return super.toString() + "[method: " + method + "]";
    }
}

/**
 * A utility class to help the verifier keep track of elements on the stack,
 * in local variables, and in input parameters.
 */
final class Frame {
    /**
     * Construct a new instance.
     *
     * @param body the method that will be verified
     */
    Frame(MethodBody body) {
        stack = new StackElement[8];
        sp = 0;
        flow = true;
        stackMap = new Hashtable();
        localMap = new Hashtable();
        parmMap = new Hashtable();
        changed = false;

        checkTargets = true;
        bbtargets = new Hashtable();
        tlist = new TargetList();

        Klass types[] = body.getTypes();
        generalParms = new Klass[body.getParametersCount()];
        System.arraycopy(types, 0, generalParms, 0,  body.getParametersCount());
        generalLocals = new Klass[types.length -  body.getParametersCount()];
        System.arraycopy(types, body.getParametersCount(),  generalLocals, 0, generalLocals.length);
        for (int i = 0; i < generalLocals.length; i++) {
            if (generalLocals[i] == Klass.REFERENCE) {
                generalLocals[i] = Klass.OBJECT;
            }
        }

        specificLocals = new StackElement[generalLocals.length];
        specificLocals[0] = new StackElement(generalLocals[0], 0);
        specificParms = new StackElement[generalParms.length];
        if (specificParms.length > 0) {
            Method definingMethod = body.getDefiningMethod();
            if (body.getDefiningClass() != Klass.OBJECT &&
                definingMethod.isConstructor() &&
                !definingMethod.isReplacementConstructor()) {
                specificParms[0] = new UninitializedReference (body.getDefiningClass(), -1, true);
            } else {
                specificParms[0] = new StackElement(generalParms[0],  0);
            }
        }
        for (int i = 1; i < specificParms.length; i++) {
            specificParms[i] = new StackElement(generalParms[i], 0);
        }

        for (int i = 0; i < generalParms.length; i++) {
            if (isAssignable(Klass.OBJECT, generalParms[i])) {
                generalParms[i] = Klass.OBJECT;
            }
        }

        handlers = body.getExceptionTable();
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                ExceptionHandler handler = handlers[i];
                tlist.setExceptionTarget(handler.getStart());
                if (handler.getEnd() != body.getCode().length) {
                    tlist.setExceptionTarget(handler.getEnd());
                }
                tlist.setExceptionTarget(handler.getHandler());
            }
        }
    }

    void checkMerge(boolean cond, String msg) {
        check(cond, msg);
    }

    void checkMerge(boolean cond, String msg, StackElement e1, StackElement e2) {
        check(cond, msg + "; merging data from " + e1.getIP() + " and " + e2.getIP());
    }

    void check(boolean cond, String msg, StackElement e) {
        check(cond, msg + "; data [" + e + "] came from " + e.getIP());
    }

    void check(boolean cond, String msg) {
        VerifyError.check(cond, ip, msg);
    }

    void check(boolean cond) {
        VerifyError.check(cond, ip);
    }

    private StackElement stack[];
    private ExceptionHandler[] handlers;
    private int sp;
    private int ip;
    private Klass generalLocals[];
    private Klass generalParms[];
    private StackElement specificLocals[];
    private StackElement specificParms[];
    private boolean flow;
    private boolean checkTargets;
    private Hashtable bbtargets;
    private TargetList tlist;

    /**
     * Returns the number of elements currently on the stack.
     * Double-word elements only count as one element.
     *
     * @return number of elements on the stack
     */
    int getSP() {
        return sp;
    }

    /**
     * Print the current contents of the stack.
     * Should only really be used for debugging purposes.
     */
    void printStack() {
        for (int i = 0; i < sp; i++) {
            System.out.println("stack element " + i + ": " + stack[i].getKlass());
        }
    }

    /**
     * Push an element onto the stack.
     *
     * @param e the element to push onto the stack
     */
    private void push(StackElement e) {
        if (sp >= stack.length) {
            StackElement tmp[] = new StackElement[Math.max(8, stack.length * 2)];
            System.arraycopy(stack, 0, tmp, 0, stack.length);
            stack = tmp;
        }
        stack[sp++] = e;
    }

    /**
     * Push an element of the given type onto the stack.
     *
     * @param k the type of the data to be pushed onto the stack
     */
    public void push(Klass k) {
        if (k.isInterface())
            k = Klass.OBJECT;
        push(new StackElement(k, ip));
    }

    /**
     * Push an exception onto the stack.  The Frame
     * will look up which exception handler starts at the current
     * address and push the appropriate exception type.
     */
    public void pushHandlerException() {
        Assert.that(handlers != null);
        for (int i = 0; i < handlers.length; i++)
            if (handlers[i].getHandler() == ip) {
                push(handlers[i].getKlass());
                return;
            }
        check(false, "no exception handler at " + ip);
    }

    /**
     * Push the result of a new instruction.  The given type
     * is the type the object will be after being initialized.
     * The fact that it is not yet initialized is kept track of
     * by internal mechanisms.
     *
     * @see #popForInitialization()
     */
    public void pushUninitialized(Klass k) {
        push(new UninitializedReference(k, ip));
    }

    /**
     * Push a compile-time known object, such as the result
     * of an object instruction.
     *
     * @param o the object to push
     */
    public void pushObject(Object o) {
        Klass k = Klass.OBJECT;
        if (o instanceof String)
            k = Klass.STRING;
        else if (o instanceof Klass)
            k = Klass.KLASS;
        else if (o instanceof byte[])
            k = Klass.BYTE_ARRAY;
        else if (o instanceof short[])
            k = Klass.SHORT_ARRAY;
        else if (o instanceof char[])
            k = Klass.CHAR_ARRAY;
        else if (o instanceof int[])
            k = Klass.INT_ARRAY;
        else if (o instanceof long[])
            k = Klass.LONG_ARRAY;
        else if (o instanceof Object[])
            k = Klass.OBJECT_ARRAY;
        push(new StackObject(o, k, ip));
    }

    /**
     * Push a compile-time known 32-bit integer constant.
     *
     * @param i the value to push
     * @param k the type of the value (BOOLEAN, BYTE, etc.)
     */
    public void pushConstInt(int i, Klass k) {
        push(new StackConstInt(i, k, ip));
    }

    /**
     * Push a compile-time known 32-bit integer constant.
     *
     * @param i the value to push
     */
    public void pushConstInt(int i) {
        pushConstInt(i, Klass.INT);
    }

    /**
     * Push the result of a findslot instruction.
     * This is an integer, though not necessarily compile-time
     * known, but it does correspond to an interface method that
     * is compile-time known.
     *
     * @param m interface method being looked up by the findslot instruction
     * @see #popMethodSlot()
     */
    public void pushMethodSlot(Method m) {
        push(new StackMethodSlot(m, ip));
    }

    /**
     * Pop an element from the stack.
     *
     * @return the top of the stack
     */
    private StackElement popElement() {
        check(sp > 0, "tried to pop from empty stack");
        return stack[--sp];
    }

    /**
     * Pop an element from the top of the stack of the
     * given type.  If the top of the stack is not assignable
     * to the given type, an exception will be thrown.
     *
     * @param k type the top of the stack should be
     * @return the top of the stack
     */
    public Klass pop(Klass k) {
        if (k.isInterface())
            k = Klass.OBJECT;
        StackElement e = popElement();
        check(isAssignable(k, e.getKlass()),
              "stack element of wrong type: wanted " + k + ", got " + e.getKlass(), e);
        return e.getKlass();
    }

    /**
     * Initialize all occurrences of the given uninitialized reference
     * in the given array.  This is done when a constructor is called.
     *
     * @param r the uninitialized reference to initialize
     * @param a the array to look for the uninitialized reference in
     */
    private void initialize(UninitializedReference r, StackElement a[]) {
        for (int i = 0; i < a.length; i++) {
            if (r.equals(a[i]))
                a[i] = new StackElement(r.getKlass(), ip);
        }
    }

    /**
     * Pop an element of the given type off of the stack to be passed to a constructor.
     * Thus, the top of the stack should be an uninitialized reference.
     *
     * @param k type the top of the stack should be
     */
    public void popForInitialization(Klass k) {
        StackElement e = popElement();
        check(e instanceof UninitializedReference,
              "attempted to initialize object that has already been initialized", e);
        UninitializedReference r = (UninitializedReference)e;
        check(r.checkKlass(k), "attempted to initialize object with a constructor from the wrong class (not " + k + ")", e);
        initialize(r, stack);
        initialize(r, specificLocals);
        initialize(r, specificParms);
    }

    /**
     * Pop an element off of the stack.
     *
     * @return the type of the top of the stack
     */
    public Klass pop() {
        return popElement().getKlass();
    }

    /**
     * Pop a compile-time known object off of the stack.
     *
     * @return the top of the stack
     */
    public Object popObject() {
        return ((StackObject)popElement()).getObject();
    }

    /**
     * Pop a compile-time known integer constant off of the stack.
     *
     * @return the integer constant from the top of the stack
     */
    public Integer popConstInt() {
        StackElement e = popElement();
        check(isAssignable(Klass.INT, e.getKlass()), "tried to pop int off the stack, got " + e.getKlass(), e);
        if (e instanceof StackConstInt)
            return new Integer(((StackConstInt)e).getValue());
        else
            return null;
    }

    /**
     * Pop the result of a findslot instruction off the top of the stack.
     *
     * @return the interface method that was the input to the findslot instruction
     */
    public Method popMethodSlot() {
        return ((StackMethodSlot)popElement()).getMethod();
    }

    /**
     * Set the current instruction pointer.
     */
    public void setIP(int ip) {
        //removeRecord(ip);
        this.ip = ip;
        if (flow)
            merge(ip, false);
        flow = false;
        if (checkTargets)
            tlist.setIP(ip);
    }

    /**
     * Get the current instruction pointer.
     */
    public int getIP() {
        return ip;
    }

    private Hashtable stackMap;
    private Hashtable parmMap;
    private Hashtable localMap;
    private boolean changed;

    private void mergeArray(int _ip, boolean insert, Hashtable map,
                            StackElement types[], Klass generalTypes[], String name) {
        Integer ip = new Integer(_ip);
        if (!map.containsKey(ip)) {
            if (insert) {
                StackElement newTypes[] = new StackElement[types.length];
                System.arraycopy(types, 0, newTypes, 0, newTypes.length);
                map.put(ip, newTypes);
            }
        }
        else {
            StackElement newTypes[] = (StackElement[])map.get(ip);
            checkMerge(newTypes.length == types.length, "attempted to merge different sized stacks");
            for (int i = 0; i < types.length; i++) {
                if (newTypes[i] != null)
                    check(isAssignable(generalTypes[i], newTypes[i].getKlass()),
                          name + " slot " + i + ": "+ newTypes[i].getKlass() + " not assignable to general " + generalTypes[i],
                          newTypes[i]);
                if (types[i] != null)
                    check(isAssignable(generalTypes[i], types[i].getKlass()),
                          name + " slot " + i + ": "+ types[i].getKlass() + " not assignable to general " + generalTypes[i],
                          types[i]);
                StackElement newType = null;
                if (types[i] != null && newTypes[i] != null)
                    newType = newTypes[i].merge(types[i], generalTypes[i], this);
                if (newType != newTypes[i]) {
                    changed = true;
                    newTypes[i] = newType;
                }
                if (!insert)
                    types[i] = newType;
            }
        }
    }

    public void mayCauseGC() {
        for (int i = 0; i < specificLocals.length; i++)
            if (specificLocals[i] == null)
                check(generalLocals[i] != Klass.OBJECT, "local slot " + i + " was not written to");
    }

    private void mergeLocals(int ip, boolean insert) {
        mergeArray(ip, insert, localMap, specificLocals, generalLocals, "local");
        mergeArray(ip, insert, parmMap, specificParms, generalParms, "parameter");
    }

    private void merge(int _ip, boolean insert) {
        mergeLocals(_ip, insert);
        Integer ip = new Integer(_ip);
        if (!stackMap.containsKey(ip)) {
            if (insert) {
                StackElement newTypes[] = new StackElement[sp];
                System.arraycopy(stack, 0, newTypes, 0, newTypes.length);
                stackMap.put(ip, newTypes);
            }
        }
        else {
            StackElement newTypes[] = (StackElement[])stackMap.get(ip);
            checkMerge(newTypes.length == sp, "attempted to merge stacks of different sizes");
            for (int i = 0; i < sp; i++) {
                Klass k = stack[i].getKlass();
                Klass generalType = null;
                if (k.isPrimitive()) {
                    checkMerge(k == newTypes[i].getKlass(),
                               "attempted to merge stack elements of type " + k + " and type " + newTypes[i].getKlass(),
                               stack[i], newTypes[i]);
                    generalType = k;
                }
                else {
                    checkMerge(!newTypes[i].getKlass().isPrimitive(),
                               "attempted to merge reference type " + newTypes[i].getKlass() + " with primitive type " + k,
                               stack[i], newTypes[i]);
                    generalType = Klass.OBJECT;
                }
                StackElement newType = newTypes[i].merge(stack[i], generalType, this);
                if (newType != newTypes[i]) {
                    changed = true;
                    newTypes[i] = newType;
                }
                if (!insert)
                    stack[i] = newType;
            }
        }
    }

    public void addTarget(int ip) {
        if (checkTargets)
            tlist.setForwardTarget(ip, this.ip);
        merge(ip, true);
    }

    private void checkForUninitialized(StackElement a[]) {
        for (int i = 0; i < a.length; i++)
            if (a[i] != null)
                if (a[i] instanceof UninitializedReference) {
                    StackElement r = a[i];
                    check(false, "slot " + i + ": uninitialized " + r.getKlass() + " from " + r.getIP(), r);
                }
    }

    private void checkForUninitialized() {
        checkForUninitialized(stack);
        checkForUninitialized(specificLocals);
        checkForUninitialized(specificParms);
    }

    public void addBackwardsTarget(int ip) {
        checkForUninitialized();
        if (checkTargets)
            check(bbtargets.containsKey(new Integer(ip)),
                  "branch target " + ip + " is not a bbtarget instruction");
        mergeLocals(ip, true);
    }

    public void doReturn() {
        // checkForUninitialized(); <-- Not a valid assumption for the TCK javasoft.sqe.tests.vm.instr.newX.new006.new00601m1.new00601m1_wrapper
    }

    public void bbtarget() {
        if (checkTargets)
            bbtargets.put(new Integer(ip), this);
    }

    public void fallthrough() {
        flow = true;
    }

    public void finished() {
        check(!flow, "control flows off the end of the code");
        if (checkTargets) {
            check(tlist.isEmpty(), "some forward branch targets are past the end of the code");
            checkTargets = false;
            bbtargets = null;
            tlist = null;
        }
    }

    public Klass getGeneralParmType(int index) {
        return generalParms[index];
    }

    public Klass getParmType(int index) {
        return specificParms[index].getKlass();
    }
    
    public boolean isParmUninitialized(int index) {
        return specificParms[index] instanceof UninitializedReference;
    }

    public void setParmType(int index, Klass k) {
        specificParms[index] = new StackElement(k, ip);
    }

    public Klass getGeneralLocalType(int index) {
        return generalLocals[index];
    }

    public Klass getLocalType(int index) {
        return specificLocals[index].getKlass();
    }

    public void setLocalType(int index, Klass k) {
        specificLocals[index] = new StackElement(k, ip);
    }

    public void load(int index) {
        if (specificLocals[index] == null) {
            push(generalLocals[index]);
        } else {
            push(specificLocals[index]);
        }
    }

    public void loadparm(int index) {
        push(specificParms[index]);
    }

    private void store(int index, StackElement specific[], Klass general[]) {
        StackElement e = popElement();
        check(isAssignable(general[index], e.getKlass()),
              "" + e.getKlass() + " not assignable to " + general[index], e);
        specific[index] = e;
    }

    public void store(int index) {
        check(index > 0, "attempted to store to local 0");
        store(index, specificLocals, generalLocals);
    }

    public void storeparm(int index) {
        store(index, specificParms, generalParms);
    }

    public boolean hasChanged() {
        boolean tmp = changed;
        changed = false;
        return tmp;
    }

    public static boolean isAssignable(Klass dst, Klass src) {
        if (dst.isInterface())
            dst = Klass.OBJECT;
        if (src == Klass.ADDRESS || src == Klass.UWORD || src == Klass.OFFSET) {
            if (dst == Klass.OBJECT)
                return true;
            else if (dst == src)
                return true;
            else {
                if (Klass.SQUAWK_64) {
                    if (dst == Klass.LONG)
                        return true;
                }
                else {
                    if (dst == Klass.INT)
                        return true;
                }
            }
            return false;
        }

        /*
         * The primitive one-word, non-float types are all assignment compatible with each other
         */
        if (dst.isPrimitive() && !dst.isDoubleWord() && dst != Klass.FLOAT) {
            dst = Klass.INT;
        }

        return dst.isAssignableFrom(src);
    }

    public void clearStack() {
        sp = 0;
    }

    public boolean isStackEmpty() {
        return sp == 0;
    }
}
