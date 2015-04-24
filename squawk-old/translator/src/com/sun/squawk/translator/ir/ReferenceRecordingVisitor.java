
package com.sun.squawk.translator.ir;

import com.sun.squawk.Method;
import com.sun.squawk.Field;
import com.sun.squawk.Klass;
import com.sun.squawk.translator.ir.instr.*;

/**
 * Class that walks all instructions, and notes the classes, fields, and methods that are referenced by the instructions.
 *
 */
abstract public class ReferenceRecordingVisitor extends AbstractInstructionVisitor {

    protected abstract void recordKlass(Klass klass);

    protected abstract void recordMethod(Method method);

    protected abstract void recordField(Field field);

    /**
     * {@inheritDoc}
     */
    public void doCheckCast(CheckCast insn) {
        recordKlass(insn.getType());
    }

    /**
     * {@inheritDoc}
     */
    public void doConversionOp(ConversionOp insn) {
        // nothing? should only be dealing with primitive ops.
    }

    /**
     * {@inheritDoc}
     */
    public void doInstanceOf(InstanceOf insn) {
        recordKlass(insn.getCheckType());
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeSlot(InvokeSlot insn) {
        recordMethod(insn.getMethod());
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeStatic(InvokeStatic insn) {
        recordMethod(insn.getMethod());
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeSuper(InvokeSuper insn) {
        recordMethod(insn.getMethod());
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeVirtual(InvokeVirtual insn) {
        recordMethod(insn.getMethod());
    }

    /**
     * {@inheritDoc}
     */
    public void doCatch(Catch insn) {
        recordKlass(insn.getType());
    }

    /**
     * {@inheritDoc}
     */
    public void doGetField(GetField insn) {
        recordField(insn.getField());
    }

    /**
     * {@inheritDoc}
     */
    public void doGetStatic(GetStatic insn) {
        recordField(insn.getField());
    }

    /**
     * {@inheritDoc}
     */
    public void doNewArray(NewArray insn) {
        recordKlass(insn.getType());
    }

    /**
     * {@inheritDoc}
     */
    public void doNewDimension(NewDimension insn) {
        recordKlass(insn.getType());

    }

    /**
     * {@inheritDoc}
     */
    public void doNew(New insn) {
        recordKlass(insn.getRuntimeType());
        recordKlass(insn.getType());
    }

    /**
     * {@inheritDoc}
     */
    public void doPutField(PutField insn) {
        recordField(insn.getField());
    }

    /**
     * {@inheritDoc}
     */
    public void doPutStatic(PutStatic insn) {
        recordField(insn.getField());
    }

}