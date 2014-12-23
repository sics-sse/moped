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

package com.sun.squawk.debugger;

/**
 * Squawk VM extensions to the Java Debug Wire Protocol.
 *
 */
public interface SDWP {

    /**
     * <b>SquawkVM Command Set ({@value})</b>
     * <p>
     * The value of the SDWP command family. Value is
     */
    public static final int SquawkVM_COMMAND_SET = 128;

    /**
     * <b>SteppingInfo Command ({@value})</b>
     * <p>
     * Sent by debug client when it needs to know the valid stepping ranges.
     * <p>
     * <dl>
     *   <dt>Out Data</dt>
     *   <dd>
     *     <table border=1 cellpadding=3 cellspacing=0 width="90%" summary="">
     *       <tr><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="15%"><th width="65%"></tr>
     *       <tr><td colspan=5>referenceTypeID</td><td><i>typeID</i></td><td>Class of method in which thread to be stepped in currently paused.</td></tr>
     *       <tr><td colspan=5>methodID</td>       <td><i>method</i></td><td>Method in which thread to be stepped in currently paused.</td></tr>
     *       <tr><td colspan=5>long</td>           <td><i>bci</i></td>   <td>Bytecode index of instruction in method.</td></tr>
     *     </table>
     *   </dd>
     * </dl>
     * <dl>
     *   <dt>Reply Data</dt>
     *   <dd>
     *     <table border=1 cellpadding=3 cellspacing=0 width="90%" summary="">
     *       <tr><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="15%"><th width="65%"></tr>
     *       <tr><td colspan=5>long</td>  <td><i>targetBCI</i></td>    <td>The offset of the next instruction that starts a new source line or -1.</td></tr>
     *       <tr><td colspan=5>long</td>  <td><i>dupBCI</i></td>       <td>The offset of another instruction apart from the current instruction that
     *                                                                    returns control flow to current source line or -1.</td></tr>
     *       <tr><td colspan=5>long</td>  <td><i>afterDupBCI</i></td>  <td>The offset of the first instruction after <i>dupBCI</i> that is on a new source line or -1.</td></tr>
     *     </table>
     *   </dd>
     * </dl>
     */
    public static final int SquawkVM_SteppingInfo_COMMAND = 2;

    /**
     * <b>AllThreads Command ({@value})</b>
     * <p>
     * Gets information on all the threads in the VM.
     * <p>
     * <dl>
     *   <dt>Out Data</dt>
     *   <dd>(None)</dd>
     * </dl>
     * <dl>
     *   <dt>Reply Data</dt>
     *   <dd>
     *     <table border=1 cellpadding=3 cellspacing=0 width="90%" summary="">
     *       <tr><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="15%"><th width="65%"></tr>
     *       <tr><td colspan=5>int</td>   <td><i>threads</i></td>  <td>Number of threads:</td></tr>
     *       <tr><td colspan=7>Repeated <i>threads</i> times:</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>referenceTypeID</td><td><i>thread</i></td><td>A thread.</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>int</td>            <td><i>status</i></td><td>Thread's JDWP.Status value.</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>boolean</td>        <td><i>suspendCount</i></td><td>The thread's suspension count.</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>string</td>         <td><i>name</i></td><td>Thread's name.</td></tr>
     *     </table>
     *   </dd>
     * </dl>
     */
    public static final int SquawkVM_AllThreads_COMMAND = 3;

    /**
     * <b>ThreadStateChanged Command ({@value})</b>
     * <p>
     * Sent by VM when the debug state of one or more threads changes as the result of a debugger suspend/resume request
     * or a thread starts/dies.
     * <p>
     * <dl>
     *   <dt>Out Data</dt>
     *   <dd>
     *     <table border=1 cellpadding=3 cellspacing=0 width="90%" summary="">
     *       <tr><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="4%"><th width="15%"><th width="65%"></tr>
     *       <tr><td colspan=5>int</td>   <td><i>threads</i></td>  <td>Number of threads:</td></tr>
     *       <tr><td colspan=7>Repeated <i>threads</i> times:</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>referenceTypeID</td><td><i>thread</i></td><td>A thread.</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>int</td>            <td><i>status</i></td><td>Thread's JDWP.Status value.</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>boolean</td>        <td><i>suspendCount</i></td><td>The thread's suspension count.</td></tr>
     *       <tr><td colspan=1></td><td colspan=4>string</td>         <td><i>name</i></td><td>Thread's name.</td></tr>
     *     </table>
     *   </dd>
     * </dl>
     * <dl>
     *   <dt>Reply Data</dt>
     *   <dd>(None)</dd>
     * </dl>
     */
    public static final int SquawkVM_ThreadStateChanged_COMMAND = 4;


}
