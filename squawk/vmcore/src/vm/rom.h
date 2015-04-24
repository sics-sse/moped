/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
*/
#define java_lang_Object 1
#define java_lang_String 2
#define java_lang_Throwable 3
#define java_lang_Throwable_detailMessage(oop) getObject((oop), 0)
#define java_lang_Throwable_trace(oop) getObject((oop), 1)
#define set_java_lang_Throwable_detailMessage(oop, value) setObject((oop), 0, value)
#define set_java_lang_Throwable_trace(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_Klass 4
#define com_sun_squawk_Klass_self(oop) getObject((oop), 0)
#define com_sun_squawk_Klass_virtualMethods(oop) getObject((oop), 1)
#define com_sun_squawk_Klass_staticMethods(oop) getObject((oop), 2)
#define com_sun_squawk_Klass_name(oop) getObject((oop), 3)
#define com_sun_squawk_Klass_componentType(oop) getObject((oop), 4)
#define com_sun_squawk_Klass_superType(oop) getObject((oop), 5)
#define com_sun_squawk_Klass_interfaces(oop) getObject((oop), 6)
#define com_sun_squawk_Klass_interfaceVTableMaps(oop) getObject((oop), 7)
#define com_sun_squawk_Klass_objects(oop) getObject((oop), 8)
#define com_sun_squawk_Klass_oopMap(oop) getObject((oop), 9)
#define com_sun_squawk_Klass_oopMapWord(oop) getUWord((oop), 10)
#define com_sun_squawk_Klass_dataMap(oop) getObject((oop), 11)
#define com_sun_squawk_Klass_dataMapWord(oop) getUWord((oop), 12)
#define com_sun_squawk_Klass_modifiers(oop) getInt((oop), 13)
#define com_sun_squawk_Klass_dataMapLength(oop) getShort((oop), 28)
#define com_sun_squawk_Klass_id(oop) getShort((oop), 29)
#define com_sun_squawk_Klass_instanceSizeBytes(oop) getShort((oop), 30)
#define com_sun_squawk_Klass_staticFieldsSize(oop) getShort((oop), 31)
#define com_sun_squawk_Klass_refStaticFieldsSize(oop) getShort((oop), 32)
#define com_sun_squawk_Klass_state(oop) getByte((oop), 66)
#define com_sun_squawk_Klass_initModifiers(oop) getByte((oop), 67)
#define set_com_sun_squawk_Klass_self(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Klass_virtualMethods(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_Klass_staticMethods(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_Klass_name(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_Klass_componentType(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_Klass_superType(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_Klass_interfaces(oop, value) setObject((oop), 6, value)
#define set_com_sun_squawk_Klass_interfaceVTableMaps(oop, value) setObject((oop), 7, value)
#define set_com_sun_squawk_Klass_objects(oop, value) setObject((oop), 8, value)
#define set_com_sun_squawk_Klass_oopMap(oop, value) setObject((oop), 9, value)
#define set_com_sun_squawk_Klass_oopMapWord(oop, value) setUWord((oop), 10, value)
#define set_com_sun_squawk_Klass_dataMap(oop, value) setObject((oop), 11, value)
#define set_com_sun_squawk_Klass_dataMapWord(oop, value) setUWord((oop), 12, value)
#define set_com_sun_squawk_Klass_modifiers(oop, value) setInt((oop), 13, value)
#define set_com_sun_squawk_Klass_dataMapLength(oop, value) setShort((oop), 28, value)
#define set_com_sun_squawk_Klass_id(oop, value) setShort((oop), 29, value)
#define set_com_sun_squawk_Klass_instanceSizeBytes(oop, value) setShort((oop), 30, value)
#define set_com_sun_squawk_Klass_staticFieldsSize(oop, value) setShort((oop), 31, value)
#define set_com_sun_squawk_Klass_refStaticFieldsSize(oop, value) setShort((oop), 32, value)
#define set_com_sun_squawk_Klass_state(oop, value) setByte((oop), 66, value)
#define set_com_sun_squawk_Klass_initModifiers(oop, value) setByte((oop), 67, value)
#define com_sun_squawk_Klass_ILLEGAL_METHOD_OFFSET 65535
#define com_sun_squawk_Klass_ENABLE_DYNAMIC_CLASSLOADING true
#define com_sun_squawk_Klass_CANONICAL_SPECIAL_METHOD_INDEX 0
#define com_sun_squawk_Klass_DEBUG_CODE_ENABLED false
#define com_sun_squawk_Klass_ASSERTIONS_ENABLED false
#define com_sun_squawk_Klass_TRACING_ENABLED false
#define com_sun_squawk_Klass_SQUAWK_64 false
#define com_sun_squawk_Klass_STATE_DEFINED 0
#define com_sun_squawk_Klass_STATE_LOADING 1
#define com_sun_squawk_Klass_STATE_LOADED 2
#define com_sun_squawk_Klass_STATE_CONVERTING 3
#define com_sun_squawk_Klass_STATE_CONVERTED 4
#define com_sun_squawk_Klass_STATE_ERROR 5
#define com_sun_squawk_Klass_DATAMAP_ENTRY_BITS 2
#define com_sun_squawk_Klass_DATAMAP_ENTRY_MASK 3
#define com_sun_squawk_Klass_DATAMAP_ENTRIES_PER_WORD 16
#define com_sun_squawk_Klass_INITSTATE_NOTINITIALIZED 0
#define com_sun_squawk_Klass_INITSTATE_INITIALIZING 1
#define com_sun_squawk_Klass_INITSTATE_INITIALIZED 2
#define com_sun_squawk_Klass_INITSTATE_FAILED 3
#define com_sun_squawk_Klass_none 0
#define com_sun_squawk_Klass_publik 1
#define com_sun_squawk_Klass_synthetic 524289
#define com_sun_squawk_Klass_synthetic2 1572865
#define com_sun_squawk_Klass_primitive 786433
#define com_sun_squawk_Klass_primitive2 1835009
#define com_sun_squawk_Klass_squawkarray 4194305
#define com_sun_squawk_Klass_squawkprimitive 8388608
#define com_sun_squawk_StringOfBytes 26
#define com_sun_squawk_Address 34
#define com_sun_squawk_UWord 36
#define com_sun_squawk_Offset 38
#define com_sun_squawk_NativeUnsafe 39
#define com_sun_squawk_NativeUnsafe_NATIVE_TASK_EVENTID_OFFSET 1
#define com_sun_squawk_NativeUnsafe_NATIVE_TASK_RESULT_OFFSET 4
#define com_sun_squawk_NativeUnsafe_NATIVE_TASK_LOW_RESULT_OFFSET 5
#define com_sun_squawk_NativeUnsafe_NATIVE_TASK_NT_ERRNO_RESULT_OFFSET 6
#define com_sun_squawk_NativeUnsafe_NATIVE_TASK_ARGS_OFFSET 7
#define java_lang_Class 47
#define java_lang_Class_klass(oop) getObject((oop), 0)
#define set_java_lang_Class_klass(oop, value) setObject((oop), 0, value)
#define java_lang_StringBuilder 48
#define java_lang_StringBuilder_value(oop) getObject((oop), 0)
#define java_lang_StringBuilder_count(oop) getInt((oop), 1)
#define set_java_lang_StringBuilder_value(oop, value) setObject((oop), 0, value)
#define set_java_lang_StringBuilder_count(oop, value) setInt((oop), 1, value)
#define java_lang_InterruptedException 49
#define com_sun_squawk_pragma_ReplacementConstructorPragma 50
#define java_lang_StringBuffer 52
#define java_lang_StringBuffer_value(oop) getObject((oop), 0)
#define java_lang_StringBuffer_count(oop) getInt((oop), 1)
#define java_lang_StringBuffer_encoding(oop) getByte((oop), 8)
#define set_java_lang_StringBuffer_value(oop, value) setObject((oop), 0, value)
#define set_java_lang_StringBuffer_count(oop, value) setInt((oop), 1, value)
#define set_java_lang_StringBuffer_encoding(oop, value) setByte((oop), 8, value)
#define java_lang_StringBuffer_IS_EIGHT_BIT 0
#define java_lang_StringBuffer_IS_NOT_EIGHT_BIT 1
#define java_lang_StringBuffer_UNKNOWN_ENCODING 2
#define com_sun_squawk_ExecutionPoint 54
#define com_sun_squawk_ExecutionPoint_frame(oop) getUWord((oop), 0)
#define com_sun_squawk_ExecutionPoint_bci(oop) getUWord((oop), 1)
#define com_sun_squawk_ExecutionPoint_mp(oop) getObject((oop), 2)
#define set_com_sun_squawk_ExecutionPoint_frame(oop, value) setUWord((oop), 0, value)
#define set_com_sun_squawk_ExecutionPoint_bci(oop, value) setUWord((oop), 1, value)
#define set_com_sun_squawk_ExecutionPoint_mp(oop, value) setObject((oop), 2, value)
#define java_lang_OutOfMemoryError 56
#define com_sun_squawk_VMThread 57
#define com_sun_squawk_VMThread_time(oop) getLongAtWord((oop), 0)
#define com_sun_squawk_VMThread_isolate(oop) getObject((oop), 2)
#define com_sun_squawk_VMThread_stack(oop) getObject((oop), 3)
#define com_sun_squawk_VMThread_stackSize(oop) getInt((oop), 4)
#define com_sun_squawk_VMThread_apiThread(oop) getObject((oop), 5)
#define com_sun_squawk_VMThread_nextThread(oop) getObject((oop), 6)
#define com_sun_squawk_VMThread_waitingToJoin(oop) getObject((oop), 7)
#define com_sun_squawk_VMThread_nextTimerThread(oop) getObject((oop), 8)
#define com_sun_squawk_VMThread_joiners(oop) getObject((oop), 9)
#define com_sun_squawk_VMThread_threadNumber(oop) getInt((oop), 10)
#define com_sun_squawk_VMThread_name(oop) getObject((oop), 11)
#define com_sun_squawk_VMThread_hitBreakpoint(oop) getObject((oop), 12)
#define com_sun_squawk_VMThread_step(oop) getObject((oop), 13)
#define com_sun_squawk_VMThread_monitor(oop) getObject((oop), 14)
#define com_sun_squawk_VMThread_appThreadTop(oop) getUWord((oop), 15)
#define com_sun_squawk_VMThread_errno(oop) getInt((oop), 16)
#define com_sun_squawk_VMThread_debuggerSuspendCount(oop) getInt((oop), 17)
#define com_sun_squawk_VMThread_eventEP(oop) getObject((oop), 18)
#define com_sun_squawk_VMThread_monitorDepth(oop) getShort((oop), 38)
#define com_sun_squawk_VMThread_state(oop) getByte((oop), 78)
#define com_sun_squawk_VMThread_priority(oop) getByte((oop), 79)
#define com_sun_squawk_VMThread_inqueue(oop) getByte((oop), 80)
#define com_sun_squawk_VMThread_isDaemon(oop) getByte((oop), 81)
#define com_sun_squawk_VMThread_pendingInterrupt(oop) getByte((oop), 82)
#define set_com_sun_squawk_VMThread_time(oop, value) setLongAtWord((oop), 0, value)
#define set_com_sun_squawk_VMThread_isolate(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_VMThread_stack(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_VMThread_stackSize(oop, value) setInt((oop), 4, value)
#define set_com_sun_squawk_VMThread_apiThread(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_VMThread_nextThread(oop, value) setObject((oop), 6, value)
#define set_com_sun_squawk_VMThread_waitingToJoin(oop, value) setObject((oop), 7, value)
#define set_com_sun_squawk_VMThread_nextTimerThread(oop, value) setObject((oop), 8, value)
#define set_com_sun_squawk_VMThread_joiners(oop, value) setObject((oop), 9, value)
#define set_com_sun_squawk_VMThread_threadNumber(oop, value) setInt((oop), 10, value)
#define set_com_sun_squawk_VMThread_name(oop, value) setObject((oop), 11, value)
#define set_com_sun_squawk_VMThread_hitBreakpoint(oop, value) setObject((oop), 12, value)
#define set_com_sun_squawk_VMThread_step(oop, value) setObject((oop), 13, value)
#define set_com_sun_squawk_VMThread_monitor(oop, value) setObject((oop), 14, value)
#define set_com_sun_squawk_VMThread_appThreadTop(oop, value) setUWord((oop), 15, value)
#define set_com_sun_squawk_VMThread_errno(oop, value) setInt((oop), 16, value)
#define set_com_sun_squawk_VMThread_debuggerSuspendCount(oop, value) setInt((oop), 17, value)
#define set_com_sun_squawk_VMThread_eventEP(oop, value) setObject((oop), 18, value)
#define set_com_sun_squawk_VMThread_monitorDepth(oop, value) setShort((oop), 38, value)
#define set_com_sun_squawk_VMThread_state(oop, value) setByte((oop), 78, value)
#define set_com_sun_squawk_VMThread_priority(oop, value) setByte((oop), 79, value)
#define set_com_sun_squawk_VMThread_inqueue(oop, value) setByte((oop), 80, value)
#define set_com_sun_squawk_VMThread_isDaemon(oop, value) setByte((oop), 81, value)
#define set_com_sun_squawk_VMThread_pendingInterrupt(oop, value) setByte((oop), 82, value)
#define com_sun_squawk_VMThread_FATAL_MONITOR_ERRORS false
#define com_sun_squawk_VMThread_INITIAL_STACK_SIZE 168
#define com_sun_squawk_VMThread_MAX_STACK_GROWTH_FRACTION 8
#define com_sun_squawk_VMThread_MIN_STACK_SIZE 78
#define com_sun_squawk_VMThread_MIN_PRIORITY 1
#define com_sun_squawk_VMThread_NORM_PRIORITY 5
#define com_sun_squawk_VMThread_MAX_PRIORITY 10
#define com_sun_squawk_VMThread_MAX_SYS_PRIORITY 12
#define com_sun_squawk_VMThread_REAL_MAX_SYS_PRIORITY 14
#define com_sun_squawk_VMThread_NEW 0
#define com_sun_squawk_VMThread_ALIVE 1
#define com_sun_squawk_VMThread_DEAD 2
#define com_sun_squawk_VMThread_Q_NONE 0
#define com_sun_squawk_VMThread_Q_MONITOR 1
#define com_sun_squawk_VMThread_Q_CONDVAR 2
#define com_sun_squawk_VMThread_Q_RUN 3
#define com_sun_squawk_VMThread_Q_EVENT 4
#define com_sun_squawk_VMThread_Q_JOIN 5
#define com_sun_squawk_VMThread_Q_ISOLATEJOIN 6
#define com_sun_squawk_VMThread_Q_HIBERNATEDRUN 7
#define com_sun_squawk_VMThread_Q_TIMER 8
#define com_sun_squawk_VMThread_MAXDEPTH 32767
#define com_sun_squawk_Klass_KlassInitializationState 60
#define com_sun_squawk_Klass_KlassInitializationState_next(oop) getObject((oop), 0)
#define com_sun_squawk_Klass_KlassInitializationState_thread(oop) getObject((oop), 1)
#define com_sun_squawk_Klass_KlassInitializationState_klass(oop) getObject((oop), 2)
#define com_sun_squawk_Klass_KlassInitializationState_classState(oop) getObject((oop), 3)
#define set_com_sun_squawk_Klass_KlassInitializationState_next(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Klass_KlassInitializationState_thread(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_Klass_KlassInitializationState_klass(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_Klass_KlassInitializationState_classState(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_util_SquawkHashtable 61
#define com_sun_squawk_util_SquawkHashtable_entryTable(oop) getObject((oop), 0)
#define com_sun_squawk_util_SquawkHashtable_backupTable(oop) getObject((oop), 1)
#define com_sun_squawk_util_SquawkHashtable_count(oop) getInt((oop), 2)
#define com_sun_squawk_util_SquawkHashtable_threshold(oop) getInt((oop), 3)
#define com_sun_squawk_util_SquawkHashtable_rehasher(oop) getObject((oop), 4)
#define set_com_sun_squawk_util_SquawkHashtable_entryTable(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_SquawkHashtable_backupTable(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_SquawkHashtable_count(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_util_SquawkHashtable_threshold(oop, value) setInt((oop), 3, value)
#define set_com_sun_squawk_util_SquawkHashtable_rehasher(oop, value) setObject((oop), 4, value)
#define com_sun_squawk_util_SquawkHashtable_loadFactorPercent 75
#define com_sun_squawk_Debugger_Event 62
#define com_sun_squawk_Debugger_Event_kind(oop) getInt((oop), 0)
#define com_sun_squawk_Debugger_Event_object(oop) getObject((oop), 1)
#define com_sun_squawk_Debugger_Event_threadID(oop) getObject((oop), 2)
#define com_sun_squawk_Debugger_Event_thread(oop) getObject((oop), 3)
#define set_com_sun_squawk_Debugger_Event_kind(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_Debugger_Event_object(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_Debugger_Event_threadID(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_Debugger_Event_thread(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_Debugger_Event_VM_DISCONNECTED 100
#define com_sun_squawk_Debugger_Event_SINGLE_STEP 1
#define com_sun_squawk_Debugger_Event_BREAKPOINT 2
#define com_sun_squawk_Debugger_Event_FRAME_POP 3
#define com_sun_squawk_Debugger_Event_EXCEPTION 4
#define com_sun_squawk_Debugger_Event_USER_DEFINED 5
#define com_sun_squawk_Debugger_Event_THREAD_START 6
#define com_sun_squawk_Debugger_Event_THREAD_END 7
#define com_sun_squawk_Debugger_Event_CLASS_PREPARE 8
#define com_sun_squawk_Debugger_Event_CLASS_UNLOAD 9
#define com_sun_squawk_Debugger_Event_CLASS_LOAD 10
#define com_sun_squawk_Debugger_Event_FIELD_ACCESS 20
#define com_sun_squawk_Debugger_Event_FIELD_MODIFICATION 21
#define com_sun_squawk_Debugger_Event_EXCEPTION_CATCH 30
#define com_sun_squawk_Debugger_Event_METHOD_ENTRY 40
#define com_sun_squawk_Debugger_Event_METHOD_EXIT 41
#define com_sun_squawk_Debugger_Event_VM_INIT 90
#define com_sun_squawk_Debugger_Event_VM_DEATH 99
#define com_sun_squawk_Debugger_Event_VM_START 90
#define com_sun_squawk_Debugger_Event_THREAD_DEATH 7
#define com_sun_squawk_KlassMetadata 63
#define com_sun_squawk_KlassMetadata_definedClass(oop) getObject((oop), 0)
#define com_sun_squawk_KlassMetadata_symbols(oop) getObject((oop), 1)
#define com_sun_squawk_KlassMetadata_classTable(oop) getObject((oop), 2)
#define set_com_sun_squawk_KlassMetadata_definedClass(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_KlassMetadata_symbols(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_KlassMetadata_classTable(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_MethodBody 64
#define com_sun_squawk_MethodBody_definingMethod(oop) getObject((oop), 0)
#define com_sun_squawk_MethodBody_maxStack(oop) getInt((oop), 1)
#define com_sun_squawk_MethodBody_parametersCount(oop) getInt((oop), 2)
#define com_sun_squawk_MethodBody_exceptionTable(oop) getObject((oop), 3)
#define com_sun_squawk_MethodBody_metadata(oop) getObject((oop), 4)
#define com_sun_squawk_MethodBody_localTypes(oop) getObject((oop), 5)
#define com_sun_squawk_MethodBody_code(oop) getObject((oop), 6)
#define com_sun_squawk_MethodBody_inlinedSuperConstructor(oop) getByte((oop), 28)
#define set_com_sun_squawk_MethodBody_definingMethod(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_MethodBody_maxStack(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_MethodBody_parametersCount(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_MethodBody_exceptionTable(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_MethodBody_metadata(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_MethodBody_localTypes(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_MethodBody_code(oop, value) setObject((oop), 6, value)
#define set_com_sun_squawk_MethodBody_inlinedSuperConstructor(oop, value) setByte((oop), 28, value)
#define com_sun_squawk_Suite 65
#define com_sun_squawk_Suite_classes(oop) getObject((oop), 0)
#define com_sun_squawk_Suite_name(oop) getObject((oop), 1)
#define com_sun_squawk_Suite_metadatas(oop) getObject((oop), 2)
#define com_sun_squawk_Suite_type(oop) getInt((oop), 3)
#define com_sun_squawk_Suite_parent(oop) getObject((oop), 4)
#define com_sun_squawk_Suite_resourceFiles(oop) getObject((oop), 5)
#define com_sun_squawk_Suite_manifestProperties(oop) getObject((oop), 6)
#define com_sun_squawk_Suite_noClassDefFoundErrorClassNames(oop) getObject((oop), 7)
#define com_sun_squawk_Suite_stripClassesLater(oop) getObject((oop), 8)
#define com_sun_squawk_Suite_configuration(oop) getObject((oop), 9)
#define com_sun_squawk_Suite_closed(oop) getByte((oop), 40)
#define com_sun_squawk_Suite_isPropertiesManifestResourceInstalled(oop) getByte((oop), 41)
#define set_com_sun_squawk_Suite_classes(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Suite_name(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_Suite_metadatas(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_Suite_type(oop, value) setInt((oop), 3, value)
#define set_com_sun_squawk_Suite_parent(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_Suite_resourceFiles(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_Suite_manifestProperties(oop, value) setObject((oop), 6, value)
#define set_com_sun_squawk_Suite_noClassDefFoundErrorClassNames(oop, value) setObject((oop), 7, value)
#define set_com_sun_squawk_Suite_stripClassesLater(oop, value) setObject((oop), 8, value)
#define set_com_sun_squawk_Suite_configuration(oop, value) setObject((oop), 9, value)
#define set_com_sun_squawk_Suite_closed(oop, value) setByte((oop), 40, value)
#define set_com_sun_squawk_Suite_isPropertiesManifestResourceInstalled(oop, value) setByte((oop), 41, value)
#define com_sun_squawk_Suite_APPLICATION 0
#define com_sun_squawk_Suite_LIBRARY 1
#define com_sun_squawk_Suite_EXTENDABLE_LIBRARY 2
#define com_sun_squawk_Suite_DEBUG 3
#define com_sun_squawk_Suite_METADATA 4
#define com_sun_squawk_Debugger 66
#define com_sun_squawk_TranslatorInterface 67
#define com_sun_squawk_util_SquawkVector 68
#define com_sun_squawk_util_SquawkVector_elementData(oop) getObject((oop), 0)
#define com_sun_squawk_util_SquawkVector_elementCount(oop) getInt((oop), 1)
#define com_sun_squawk_util_SquawkVector_capacityIncrement(oop) getInt((oop), 2)
#define set_com_sun_squawk_util_SquawkVector_elementData(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_SquawkVector_elementCount(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_util_SquawkVector_capacityIncrement(oop, value) setInt((oop), 2, value)
#define com_sun_squawk_Method 69
#define com_sun_squawk_Field 70
#define com_sun_squawk_ClassFileField 71
#define com_sun_squawk_ClassFileField_type(oop) getObject((oop), 3)
#define set_com_sun_squawk_ClassFileField_type(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_ClassFileMethod 73
#define com_sun_squawk_ClassFileMethod_returnType(oop) getObject((oop), 3)
#define com_sun_squawk_ClassFileMethod_parameterTypes(oop) getObject((oop), 4)
#define com_sun_squawk_ClassFileMethod_pragmas(oop) getInt((oop), 5)
#define com_sun_squawk_ClassFileMethod_code(oop) getObject((oop), 6)
#define set_com_sun_squawk_ClassFileMethod_returnType(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_ClassFileMethod_parameterTypes(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_ClassFileMethod_pragmas(oop, value) setInt((oop), 5, value)
#define set_com_sun_squawk_ClassFileMethod_code(oop, value) setObject((oop), 6, value)
#define com_sun_squawk_Member 75
#define com_sun_squawk_Member_metadata(oop) getObject((oop), 0)
#define com_sun_squawk_Member_id(oop) getInt((oop), 1)
#define set_com_sun_squawk_Member_metadata(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Member_id(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_SymbolParser 76
#define com_sun_squawk_SymbolParser_classTable(oop) getObject((oop), 2)
#define com_sun_squawk_SymbolParser_sectionStart(oop) getObject((oop), 3)
#define com_sun_squawk_SymbolParser_sectionCount(oop) getObject((oop), 4)
#define com_sun_squawk_SymbolParser_modifiers(oop) getInt((oop), 5)
#define com_sun_squawk_SymbolParser_pragmas(oop) getInt((oop), 6)
#define com_sun_squawk_SymbolParser_offset(oop) getInt((oop), 7)
#define com_sun_squawk_SymbolParser_nameLength(oop) getInt((oop), 8)
#define com_sun_squawk_SymbolParser_selection(oop) getShort((oop), 18)
#define com_sun_squawk_SymbolParser_nameStart(oop) getShort((oop), 19)
#define com_sun_squawk_SymbolParser_signatureStart(oop) getShort((oop), 20)
#define com_sun_squawk_SymbolParser_signatureCount(oop) getShort((oop), 21)
#define com_sun_squawk_SymbolParser_sectionsParsed(oop) getByte((oop), 44)
#define set_com_sun_squawk_SymbolParser_classTable(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_SymbolParser_sectionStart(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_SymbolParser_sectionCount(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_SymbolParser_modifiers(oop, value) setInt((oop), 5, value)
#define set_com_sun_squawk_SymbolParser_pragmas(oop, value) setInt((oop), 6, value)
#define set_com_sun_squawk_SymbolParser_offset(oop, value) setInt((oop), 7, value)
#define set_com_sun_squawk_SymbolParser_nameLength(oop, value) setInt((oop), 8, value)
#define set_com_sun_squawk_SymbolParser_selection(oop, value) setShort((oop), 18, value)
#define set_com_sun_squawk_SymbolParser_nameStart(oop, value) setShort((oop), 19, value)
#define set_com_sun_squawk_SymbolParser_signatureStart(oop, value) setShort((oop), 20, value)
#define set_com_sun_squawk_SymbolParser_signatureCount(oop, value) setShort((oop), 21, value)
#define set_com_sun_squawk_SymbolParser_sectionsParsed(oop, value) setByte((oop), 44, value)
#define com_sun_squawk_SymbolParser_INSTANCE_FIELDS 0
#define com_sun_squawk_SymbolParser_STATIC_FIELDS 1
#define com_sun_squawk_SymbolParser_VIRTUAL_METHODS 2
#define com_sun_squawk_SymbolParser_STATIC_METHODS 3
#define com_sun_squawk_MethodMetadata 77
#define com_sun_squawk_MethodMetadata_lnt(oop) getObject((oop), 0)
#define com_sun_squawk_MethodMetadata_offset(oop) getInt((oop), 1)
#define set_com_sun_squawk_MethodMetadata_lnt(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_MethodMetadata_offset(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_Isolate 79
#define com_sun_squawk_Isolate_debugger(oop) getObject((oop), 0)
#define com_sun_squawk_Isolate_id(oop) getInt((oop), 1)
#define com_sun_squawk_Isolate_mainClassName(oop) getObject((oop), 2)
#define com_sun_squawk_Isolate_args(oop) getObject((oop), 3)
#define com_sun_squawk_Isolate_leafSuite(oop) getObject((oop), 4)
#define com_sun_squawk_Isolate_bootstrapSuite(oop) getObject((oop), 5)
#define com_sun_squawk_Isolate_childThreads(oop) getObject((oop), 6)
#define com_sun_squawk_Isolate_state(oop) getInt((oop), 7)
#define com_sun_squawk_Isolate_exitCode(oop) getInt((oop), 8)
#define com_sun_squawk_Isolate_parentSuiteSourceURI(oop) getObject((oop), 9)
#define com_sun_squawk_Isolate_classPath(oop) getObject((oop), 10)
#define com_sun_squawk_Isolate_channelContext(oop) getInt((oop), 11)
#define com_sun_squawk_Isolate_hibernatedRunThreads(oop) getObject((oop), 12)
#define com_sun_squawk_Isolate_hibernatedTimerThreads(oop) getObject((oop), 13)
#define com_sun_squawk_Isolate_monitorHashtable(oop) getObject((oop), 14)
#define com_sun_squawk_Isolate_translator(oop) getObject((oop), 15)
#define com_sun_squawk_Isolate_classStateQueue(oop) getObject((oop), 16)
#define com_sun_squawk_Isolate_internedStrings(oop) getObject((oop), 17)
#define com_sun_squawk_Isolate_properties(oop) getObject((oop), 18)
#define com_sun_squawk_Isolate_jadProperties(oop) getObject((oop), 19)
#define com_sun_squawk_Isolate_transitioningState(oop) getInt((oop), 20)
#define com_sun_squawk_Isolate_mailboxes(oop) getObject((oop), 21)
#define com_sun_squawk_Isolate_mailboxAddresses(oop) getObject((oop), 22)
#define com_sun_squawk_Isolate_shutdownHooks(oop) getObject((oop), 23)
#define com_sun_squawk_Isolate_parentIsolate(oop) getObject((oop), 24)
#define com_sun_squawk_Isolate_childIsolates(oop) getObject((oop), 25)
#define com_sun_squawk_Isolate_suspendHooks(oop) getObject((oop), 26)
#define com_sun_squawk_Isolate_resumeHooks(oop) getObject((oop), 27)
#define com_sun_squawk_Isolate_shutdownHook(oop) getObject((oop), 28)
#define com_sun_squawk_Isolate_joiners(oop) getObject((oop), 29)
#define com_sun_squawk_Isolate_name(oop) getObject((oop), 30)
#define com_sun_squawk_Isolate_stdout(oop) getObject((oop), 31)
#define com_sun_squawk_Isolate_stderr(oop) getObject((oop), 32)
#define com_sun_squawk_Isolate_breakpoints(oop) getObject((oop), 33)
#define com_sun_squawk_Isolate_classKlassInitialized(oop) getByte((oop), 136)
#define set_com_sun_squawk_Isolate_debugger(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Isolate_id(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_Isolate_mainClassName(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_Isolate_args(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_Isolate_leafSuite(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_Isolate_bootstrapSuite(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_Isolate_childThreads(oop, value) setObject((oop), 6, value)
#define set_com_sun_squawk_Isolate_state(oop, value) setInt((oop), 7, value)
#define set_com_sun_squawk_Isolate_exitCode(oop, value) setInt((oop), 8, value)
#define set_com_sun_squawk_Isolate_parentSuiteSourceURI(oop, value) setObject((oop), 9, value)
#define set_com_sun_squawk_Isolate_classPath(oop, value) setObject((oop), 10, value)
#define set_com_sun_squawk_Isolate_channelContext(oop, value) setInt((oop), 11, value)
#define set_com_sun_squawk_Isolate_hibernatedRunThreads(oop, value) setObject((oop), 12, value)
#define set_com_sun_squawk_Isolate_hibernatedTimerThreads(oop, value) setObject((oop), 13, value)
#define set_com_sun_squawk_Isolate_monitorHashtable(oop, value) setObject((oop), 14, value)
#define set_com_sun_squawk_Isolate_translator(oop, value) setObject((oop), 15, value)
#define set_com_sun_squawk_Isolate_classStateQueue(oop, value) setObject((oop), 16, value)
#define set_com_sun_squawk_Isolate_internedStrings(oop, value) setObject((oop), 17, value)
#define set_com_sun_squawk_Isolate_properties(oop, value) setObject((oop), 18, value)
#define set_com_sun_squawk_Isolate_jadProperties(oop, value) setObject((oop), 19, value)
#define set_com_sun_squawk_Isolate_transitioningState(oop, value) setInt((oop), 20, value)
#define set_com_sun_squawk_Isolate_mailboxes(oop, value) setObject((oop), 21, value)
#define set_com_sun_squawk_Isolate_mailboxAddresses(oop, value) setObject((oop), 22, value)
#define set_com_sun_squawk_Isolate_shutdownHooks(oop, value) setObject((oop), 23, value)
#define set_com_sun_squawk_Isolate_parentIsolate(oop, value) setObject((oop), 24, value)
#define set_com_sun_squawk_Isolate_childIsolates(oop, value) setObject((oop), 25, value)
#define set_com_sun_squawk_Isolate_suspendHooks(oop, value) setObject((oop), 26, value)
#define set_com_sun_squawk_Isolate_resumeHooks(oop, value) setObject((oop), 27, value)
#define set_com_sun_squawk_Isolate_shutdownHook(oop, value) setObject((oop), 28, value)
#define set_com_sun_squawk_Isolate_joiners(oop, value) setObject((oop), 29, value)
#define set_com_sun_squawk_Isolate_name(oop, value) setObject((oop), 30, value)
#define set_com_sun_squawk_Isolate_stdout(oop, value) setObject((oop), 31, value)
#define set_com_sun_squawk_Isolate_stderr(oop, value) setObject((oop), 32, value)
#define set_com_sun_squawk_Isolate_breakpoints(oop, value) setObject((oop), 33, value)
#define set_com_sun_squawk_Isolate_classKlassInitialized(oop, value) setByte((oop), 136, value)
#define com_sun_squawk_Isolate_DEBUG_CODE_ENABLED false
#define com_sun_squawk_Isolate_ENABLE_MULTI_ISOLATE true
#define com_sun_squawk_Isolate_NEW 0
#define com_sun_squawk_Isolate_ALIVE 1
#define com_sun_squawk_Isolate_HIBERNATED 2
#define com_sun_squawk_Isolate_EXITED 3
#define com_sun_squawk_Isolate_SHUTDOWN_EVENT_MASK 1
#define com_sun_squawk_Isolate_HIBERNATE_EVENT_MASK 2
#define com_sun_squawk_Isolate_UNHIBERNATE_EVENT_MASK 4
#define com_sun_squawk_Isolate_SUPPORTED_EVENTS 7
#define java_lang_RuntimeException 80
#define java_lang_ClassNotFoundException 82
#define com_sun_squawk_pragma_AllowInlinedPragma 84
#define com_sun_squawk_pragma_ForceInlinedPragma 85
#define com_sun_squawk_pragma_HostedPragma 86
#define com_sun_squawk_pragma_NotInlinedPragma 87
#define com_sun_squawk_util_IntHashtable 88
#define com_sun_squawk_util_IntHashtable_table(oop) getObject((oop), 0)
#define com_sun_squawk_util_IntHashtable_count(oop) getInt((oop), 1)
#define com_sun_squawk_util_IntHashtable_threshold(oop) getInt((oop), 2)
#define set_com_sun_squawk_util_IntHashtable_table(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_IntHashtable_count(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_util_IntHashtable_threshold(oop, value) setInt((oop), 2, value)
#define com_sun_squawk_util_IntHashtable_loadFactorPercent 75
#define com_sun_squawk_pragma_NativePragma 89
#define com_sun_squawk_util_BitSet 90
#define com_sun_squawk_util_BitSet_bits(oop) getObject((oop), 0)
#define com_sun_squawk_util_BitSet_bytesInUse(oop) getInt((oop), 1)
#define com_sun_squawk_util_BitSet_bitsAreExternal(oop) getByte((oop), 8)
#define set_com_sun_squawk_util_BitSet_bits(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_BitSet_bytesInUse(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_util_BitSet_bitsAreExternal(oop, value) setByte((oop), 8, value)
#define com_sun_squawk_util_ArrayHashtable 92
#define com_sun_squawk_util_ArrayHashtable_table(oop) getObject((oop), 0)
#define com_sun_squawk_util_ArrayHashtable_count(oop) getInt((oop), 1)
#define com_sun_squawk_util_ArrayHashtable_threshold(oop) getInt((oop), 2)
#define set_com_sun_squawk_util_ArrayHashtable_table(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_ArrayHashtable_count(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_util_ArrayHashtable_threshold(oop, value) setInt((oop), 2, value)
#define com_sun_squawk_util_ArrayHashtable_loadFactorPercent 75
#define java_lang_IndexOutOfBoundsException 93
#define com_sun_squawk_AppThread 94
#define com_sun_squawk_AppThread_mainClass(oop) getObject((oop), 2)
#define com_sun_squawk_AppThread_args(oop) getObject((oop), 3)
#define set_com_sun_squawk_AppThread_mainClass(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_AppThread_args(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_Base1 95
#define com_sun_squawk_ByteBufferDecoder 96
#define com_sun_squawk_ByteBufferDecoder_buf(oop) getObject((oop), 0)
#define com_sun_squawk_ByteBufferDecoder_pos(oop) getInt((oop), 1)
#define set_com_sun_squawk_ByteBufferDecoder_buf(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ByteBufferDecoder_pos(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_ByteBufferEncoder 97
#define com_sun_squawk_ByteBufferEncoder_count(oop) getInt((oop), 0)
#define com_sun_squawk_ByteBufferEncoder_buffer(oop) getObject((oop), 1)
#define set_com_sun_squawk_ByteBufferEncoder_count(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_ByteBufferEncoder_buffer(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_CallbackGroup 98
#define com_sun_squawk_CallbackGroup_iso(oop) getObject((oop), 0)
#define com_sun_squawk_CallbackGroup_hooks(oop) getObject((oop), 1)
#define set_com_sun_squawk_CallbackGroup_iso(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_CallbackGroup_hooks(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_CallbackManager 99
#define com_sun_squawk_CallbackManager_hooks(oop) getObject((oop), 0)
#define com_sun_squawk_CallbackManager_runOnce(oop) getByte((oop), 4)
#define com_sun_squawk_CallbackManager_ran(oop) getByte((oop), 5)
#define set_com_sun_squawk_CallbackManager_hooks(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_CallbackManager_runOnce(oop, value) setByte((oop), 4, value)
#define set_com_sun_squawk_CallbackManager_ran(oop, value) setByte((oop), 5, value)
#define com_sun_squawk_CallbackThread 100
#define com_sun_squawk_CallbackThread_cbg(oop) getObject((oop), 2)
#define set_com_sun_squawk_CallbackThread_cbg(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_CheneyCollector 101
#define com_sun_squawk_CheneyCollector_forwardingRepairMap(oop) getObject((oop), 20)
#define com_sun_squawk_CheneyCollector_forwardingRepairMapTop(oop) getObject((oop), 21)
#define com_sun_squawk_CheneyCollector_fromSpaceStartPointer(oop) getObject((oop), 22)
#define com_sun_squawk_CheneyCollector_fromSpaceEndPointer(oop) getObject((oop), 23)
#define com_sun_squawk_CheneyCollector_toSpaceStartPointer(oop) getObject((oop), 24)
#define com_sun_squawk_CheneyCollector_toSpaceEndPointer(oop) getObject((oop), 25)
#define com_sun_squawk_CheneyCollector_toSpaceAllocationPointer(oop) getObject((oop), 26)
#define com_sun_squawk_CheneyCollector_HashTableKlass(oop) getObject((oop), 27)
#define com_sun_squawk_CheneyCollector_ByteArrayKlass(oop) getObject((oop), 28)
#define com_sun_squawk_CheneyCollector_IsolateKlass(oop) getObject((oop), 29)
#define com_sun_squawk_CheneyCollector_theIsolate(oop) getObject((oop), 30)
#define com_sun_squawk_CheneyCollector_oopMap(oop) getObject((oop), 31)
#define com_sun_squawk_CheneyCollector_decoder(oop) getObject((oop), 32)
#define com_sun_squawk_CheneyCollector_collectionTimings(oop) getObject((oop), 33)
#define com_sun_squawk_CheneyCollector_copyTimings(oop) getObject((oop), 34)
#define com_sun_squawk_CheneyCollector_copyingObjectGraph(oop) getByte((oop), 140)
#define set_com_sun_squawk_CheneyCollector_forwardingRepairMap(oop, value) setObject((oop), 20, value)
#define set_com_sun_squawk_CheneyCollector_forwardingRepairMapTop(oop, value) setObject((oop), 21, value)
#define set_com_sun_squawk_CheneyCollector_fromSpaceStartPointer(oop, value) setObject((oop), 22, value)
#define set_com_sun_squawk_CheneyCollector_fromSpaceEndPointer(oop, value) setObject((oop), 23, value)
#define set_com_sun_squawk_CheneyCollector_toSpaceStartPointer(oop, value) setObject((oop), 24, value)
#define set_com_sun_squawk_CheneyCollector_toSpaceEndPointer(oop, value) setObject((oop), 25, value)
#define set_com_sun_squawk_CheneyCollector_toSpaceAllocationPointer(oop, value) setObject((oop), 26, value)
#define set_com_sun_squawk_CheneyCollector_HashTableKlass(oop, value) setObject((oop), 27, value)
#define set_com_sun_squawk_CheneyCollector_ByteArrayKlass(oop, value) setObject((oop), 28, value)
#define set_com_sun_squawk_CheneyCollector_IsolateKlass(oop, value) setObject((oop), 29, value)
#define set_com_sun_squawk_CheneyCollector_theIsolate(oop, value) setObject((oop), 30, value)
#define set_com_sun_squawk_CheneyCollector_oopMap(oop, value) setObject((oop), 31, value)
#define set_com_sun_squawk_CheneyCollector_decoder(oop, value) setObject((oop), 32, value)
#define set_com_sun_squawk_CheneyCollector_collectionTimings(oop, value) setObject((oop), 33, value)
#define set_com_sun_squawk_CheneyCollector_copyTimings(oop, value) setObject((oop), 34, value)
#define set_com_sun_squawk_CheneyCollector_copyingObjectGraph(oop, value) setByte((oop), 140, value)
#define com_sun_squawk_CheneyCollector_Timings 102
#define com_sun_squawk_CheneyCollector_Timings_setup(oop) getLongAtWord((oop), 0)
#define com_sun_squawk_CheneyCollector_Timings_copyRoots(oop) getLongAtWord((oop), 2)
#define com_sun_squawk_CheneyCollector_Timings_copyNonRoots(oop) getLongAtWord((oop), 4)
#define com_sun_squawk_CheneyCollector_Timings_repair(oop) getLongAtWord((oop), 6)
#define com_sun_squawk_CheneyCollector_Timings_finalize(oop) getLongAtWord((oop), 8)
#define set_com_sun_squawk_CheneyCollector_Timings_setup(oop, value) setLongAtWord((oop), 0, value)
#define set_com_sun_squawk_CheneyCollector_Timings_copyRoots(oop, value) setLongAtWord((oop), 2, value)
#define set_com_sun_squawk_CheneyCollector_Timings_copyNonRoots(oop, value) setLongAtWord((oop), 4, value)
#define set_com_sun_squawk_CheneyCollector_Timings_repair(oop, value) setLongAtWord((oop), 6, value)
#define set_com_sun_squawk_CheneyCollector_Timings_finalize(oop, value) setLongAtWord((oop), 8, value)
#define com_sun_squawk_ClassFileConstantField 103
#define com_sun_squawk_ClassFileConstantField_primitiveConstantValue(oop) getLongAtWord((oop), 4)
#define com_sun_squawk_ClassFileConstantField_stringConstantValue(oop) getObject((oop), 6)
#define set_com_sun_squawk_ClassFileConstantField_primitiveConstantValue(oop, value) setLongAtWord((oop), 4, value)
#define set_com_sun_squawk_ClassFileConstantField_stringConstantValue(oop, value) setObject((oop), 6, value)
#define com_sun_squawk_ClassFileMember 104
#define com_sun_squawk_ClassFileMember_name(oop) getObject((oop), 0)
#define com_sun_squawk_ClassFileMember_modifiers(oop) getInt((oop), 1)
#define com_sun_squawk_ClassFileMember_offset(oop) getInt((oop), 2)
#define set_com_sun_squawk_ClassFileMember_name(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ClassFileMember_modifiers(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_ClassFileMember_offset(oop, value) setInt((oop), 2, value)
#define com_sun_squawk_Concrete1 105
#define com_sun_squawk_CrossIsolateThread 106
#define com_sun_squawk_Debugger_BreakpointEvent 107
#define com_sun_squawk_Debugger_ExceptionEvent 108
#define com_sun_squawk_Debugger_ExceptionEvent_catchLocation(oop) getObject((oop), 5)
#define com_sun_squawk_Debugger_ExceptionEvent_isCaught(oop) getByte((oop), 24)
#define set_com_sun_squawk_Debugger_ExceptionEvent_catchLocation(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_Debugger_ExceptionEvent_isCaught(oop, value) setByte((oop), 24, value)
#define com_sun_squawk_Debugger_LocationEvent 109
#define com_sun_squawk_Debugger_LocationEvent_location(oop) getObject((oop), 4)
#define set_com_sun_squawk_Debugger_LocationEvent_location(oop, value) setObject((oop), 4, value)
#define com_sun_squawk_Debugger_SingleStep 110
#define com_sun_squawk_Debugger_SingleStep_state(oop) getInt((oop), 0)
#define com_sun_squawk_Debugger_SingleStep_startFO(oop) getUWord((oop), 1)
#define com_sun_squawk_Debugger_SingleStep_startBCI(oop) getUWord((oop), 2)
#define com_sun_squawk_Debugger_SingleStep_targetBCI(oop) getInt((oop), 3)
#define com_sun_squawk_Debugger_SingleStep_dupBCI(oop) getInt((oop), 4)
#define com_sun_squawk_Debugger_SingleStep_afterDupBCI(oop) getInt((oop), 5)
#define com_sun_squawk_Debugger_SingleStep_reportedFO(oop) getUWord((oop), 6)
#define com_sun_squawk_Debugger_SingleStep_reportedBCI(oop) getUWord((oop), 7)
#define com_sun_squawk_Debugger_SingleStep_size(oop) getInt((oop), 8)
#define com_sun_squawk_Debugger_SingleStep_depth(oop) getInt((oop), 9)
#define set_com_sun_squawk_Debugger_SingleStep_state(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_Debugger_SingleStep_startFO(oop, value) setUWord((oop), 1, value)
#define set_com_sun_squawk_Debugger_SingleStep_startBCI(oop, value) setUWord((oop), 2, value)
#define set_com_sun_squawk_Debugger_SingleStep_targetBCI(oop, value) setInt((oop), 3, value)
#define set_com_sun_squawk_Debugger_SingleStep_dupBCI(oop, value) setInt((oop), 4, value)
#define set_com_sun_squawk_Debugger_SingleStep_afterDupBCI(oop, value) setInt((oop), 5, value)
#define set_com_sun_squawk_Debugger_SingleStep_reportedFO(oop, value) setUWord((oop), 6, value)
#define set_com_sun_squawk_Debugger_SingleStep_reportedBCI(oop, value) setUWord((oop), 7, value)
#define set_com_sun_squawk_Debugger_SingleStep_size(oop, value) setInt((oop), 8, value)
#define set_com_sun_squawk_Debugger_SingleStep_depth(oop, value) setInt((oop), 9, value)
#define com_sun_squawk_Debugger_SingleStep_REQUESTED 1
#define com_sun_squawk_Debugger_SingleStep_HIT 2
#define com_sun_squawk_Debugger_SingleStep_DEFERRED 3
#define com_sun_squawk_Debugger_SingleStepEvent 111
#define com_sun_squawk_DoBlock 112
#define com_sun_squawk_Driver 113
#define com_sun_squawk_EventHashtable 114
#define com_sun_squawk_EventHashtable_isolate(oop) getObject((oop), 3)
#define set_com_sun_squawk_EventHashtable_isolate(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_ExceptionHandler 115
#define com_sun_squawk_ExceptionHandler_start(oop) getInt((oop), 0)
#define com_sun_squawk_ExceptionHandler_end(oop) getInt((oop), 1)
#define com_sun_squawk_ExceptionHandler_handler(oop) getInt((oop), 2)
#define com_sun_squawk_ExceptionHandler_klass(oop) getObject((oop), 3)
#define set_com_sun_squawk_ExceptionHandler_start(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_ExceptionHandler_end(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_ExceptionHandler_handler(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_ExceptionHandler_klass(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_FullMethodMetadata 116
#define com_sun_squawk_FullMethodMetadata_lvt(oop) getObject((oop), 2)
#define set_com_sun_squawk_FullMethodMetadata_lvt(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_GC 117
#define com_sun_squawk_GC_GC_TRACING_SUPPORTED false
#define com_sun_squawk_GC_TRACE_BASIC 1
#define com_sun_squawk_GC_TRACE_ALLOCATION 2
#define com_sun_squawk_GC_TRACE_COLLECTION 4
#define com_sun_squawk_GC_TRACE_OBJECT_GRAPH_COPYING 8
#define com_sun_squawk_GC_TRACE_HEAP_BEFORE_GC 16
#define com_sun_squawk_GC_TRACE_HEAP_AFTER_GC 32
#define com_sun_squawk_GC_TRACE_HEAP_CONTENTS 64
#define com_sun_squawk_GC_ClassStat 118
#define com_sun_squawk_GC_ClassStat_count(oop) getInt((oop), 0)
#define com_sun_squawk_GC_ClassStat_size(oop) getInt((oop), 1)
#define set_com_sun_squawk_GC_ClassStat_count(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_GC_ClassStat_size(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_GarbageCollector 119
#define com_sun_squawk_GarbageCollector_lastCollectionTime(oop) getLongAtWord((oop), 0)
#define com_sun_squawk_GarbageCollector_maxFullCollectionTime(oop) getLongAtWord((oop), 2)
#define com_sun_squawk_GarbageCollector_maxPartialCollectionTime(oop) getLongAtWord((oop), 4)
#define com_sun_squawk_GarbageCollector_totalFullCollectionTime(oop) getLongAtWord((oop), 6)
#define com_sun_squawk_GarbageCollector_totalPartialCollectionTime(oop) getLongAtWord((oop), 8)
#define com_sun_squawk_GarbageCollector_numBytesLastScanned(oop) getLongAtWord((oop), 10)
#define com_sun_squawk_GarbageCollector_numBytesLastFreed(oop) getLongAtWord((oop), 12)
#define com_sun_squawk_GarbageCollector_numBytesFreedTotal(oop) getLongAtWord((oop), 14)
#define com_sun_squawk_GarbageCollector_totalBytesAllocatedCheckPoint(oop) getLongAtWord((oop), 16)
#define com_sun_squawk_GarbageCollector_references(oop) getObject((oop), 18)
#define com_sun_squawk_GarbageCollector_interpGC(oop) getByte((oop), 76)
#define com_sun_squawk_GarbageCollector_useMicrosecondTimer(oop) getByte((oop), 77)
#define set_com_sun_squawk_GarbageCollector_lastCollectionTime(oop, value) setLongAtWord((oop), 0, value)
#define set_com_sun_squawk_GarbageCollector_maxFullCollectionTime(oop, value) setLongAtWord((oop), 2, value)
#define set_com_sun_squawk_GarbageCollector_maxPartialCollectionTime(oop, value) setLongAtWord((oop), 4, value)
#define set_com_sun_squawk_GarbageCollector_totalFullCollectionTime(oop, value) setLongAtWord((oop), 6, value)
#define set_com_sun_squawk_GarbageCollector_totalPartialCollectionTime(oop, value) setLongAtWord((oop), 8, value)
#define set_com_sun_squawk_GarbageCollector_numBytesLastScanned(oop, value) setLongAtWord((oop), 10, value)
#define set_com_sun_squawk_GarbageCollector_numBytesLastFreed(oop, value) setLongAtWord((oop), 12, value)
#define set_com_sun_squawk_GarbageCollector_numBytesFreedTotal(oop, value) setLongAtWord((oop), 14, value)
#define set_com_sun_squawk_GarbageCollector_totalBytesAllocatedCheckPoint(oop, value) setLongAtWord((oop), 16, value)
#define set_com_sun_squawk_GarbageCollector_references(oop, value) setObject((oop), 18, value)
#define set_com_sun_squawk_GarbageCollector_interpGC(oop, value) setByte((oop), 76, value)
#define set_com_sun_squawk_GarbageCollector_useMicrosecondTimer(oop, value) setByte((oop), 77, value)
#define com_sun_squawk_GarbageCollector_NATIVE_GC_ONLY true
#define com_sun_squawk_GarbageCollector_INTERP_GC_ONLY false
#define com_sun_squawk_GarbageCollector_HEAP_TRACE false
#define com_sun_squawk_GeneralDecoder 120
#define com_sun_squawk_HitBreakpoint 121
#define com_sun_squawk_HitBreakpoint_thread(oop) getObject((oop), 0)
#define com_sun_squawk_HitBreakpoint_state(oop) getInt((oop), 1)
#define com_sun_squawk_HitBreakpoint_hitOrThrowFO(oop) getUWord((oop), 2)
#define com_sun_squawk_HitBreakpoint_hitOrThrowBCI(oop) getUWord((oop), 3)
#define com_sun_squawk_HitBreakpoint_catchFO(oop) getUWord((oop), 4)
#define com_sun_squawk_HitBreakpoint_catchBCI(oop) getUWord((oop), 5)
#define com_sun_squawk_HitBreakpoint_exception(oop) getObject((oop), 6)
#define set_com_sun_squawk_HitBreakpoint_thread(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_HitBreakpoint_state(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_HitBreakpoint_hitOrThrowFO(oop, value) setUWord((oop), 2, value)
#define set_com_sun_squawk_HitBreakpoint_hitOrThrowBCI(oop, value) setUWord((oop), 3, value)
#define set_com_sun_squawk_HitBreakpoint_catchFO(oop, value) setUWord((oop), 4, value)
#define set_com_sun_squawk_HitBreakpoint_catchBCI(oop, value) setUWord((oop), 5, value)
#define set_com_sun_squawk_HitBreakpoint_exception(oop, value) setObject((oop), 6, value)
#define com_sun_squawk_HitBreakpoint_BP_HIT 1
#define com_sun_squawk_HitBreakpoint_BP_REPORTED 2
#define com_sun_squawk_HitBreakpoint_EXC_HIT 3
#define com_sun_squawk_HitBreakpoint_EXC_REPORTING 4
#define com_sun_squawk_HitBreakpoint_EXC_REPORTED 5
#define com_sun_squawk_HookWrapper 122
#define com_sun_squawk_IllegalStoreException 123
#define com_sun_squawk_Isolate_1 124
#define com_sun_squawk_Isolate_1_this_0(oop) getObject((oop), 0)
#define set_com_sun_squawk_Isolate_1_this_0(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_Isolate_Breakpoint 125
#define com_sun_squawk_Isolate_Breakpoint_mp(oop) getObject((oop), 0)
#define com_sun_squawk_Isolate_Breakpoint_ip(oop) getInt((oop), 1)
#define set_com_sun_squawk_Isolate_Breakpoint_mp(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Isolate_Breakpoint_ip(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_Isolate_DelayedURLOutputStream 126
#define com_sun_squawk_Isolate_DelayedURLOutputStream_out(oop) getObject((oop), 0)
#define com_sun_squawk_Isolate_DelayedURLOutputStream_url(oop) getObject((oop), 1)
#define set_com_sun_squawk_Isolate_DelayedURLOutputStream_out(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Isolate_DelayedURLOutputStream_url(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_Isolate_LifecycleListener 127
#define com_sun_squawk_Isolate_LocalListenerWrapper 128
#define com_sun_squawk_Isolate_LocalListenerWrapper_listener(oop) getObject((oop), 0)
#define com_sun_squawk_Isolate_LocalListenerWrapper_eventKind(oop) getInt((oop), 1)
#define set_com_sun_squawk_Isolate_LocalListenerWrapper_listener(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Isolate_LocalListenerWrapper_eventKind(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_Isolate_PropEnumeration 129
#define com_sun_squawk_Isolate_PropEnumeration_realEnum(oop) getObject((oop), 0)
#define com_sun_squawk_Isolate_PropEnumeration_iso(oop) getObject((oop), 1)
#define set_com_sun_squawk_Isolate_PropEnumeration_realEnum(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Isolate_PropEnumeration_iso(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_Isolate_RemoteListenerWrapper 130
#define com_sun_squawk_Isolate_RemoteListenerWrapper_cleanupHook(oop) getObject((oop), 2)
#define com_sun_squawk_Isolate_RemoteListenerWrapper_local(oop) getObject((oop), 3)
#define com_sun_squawk_Isolate_RemoteListenerWrapper_remote(oop) getObject((oop), 4)
#define set_com_sun_squawk_Isolate_RemoteListenerWrapper_cleanupHook(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_Isolate_RemoteListenerWrapper_local(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_Isolate_RemoteListenerWrapper_remote(oop, value) setObject((oop), 4, value)
#define com_sun_squawk_Isolate_RemoteListenerWrapper_RemoteListenerCleanupHook 131
#define com_sun_squawk_Isolate_RemoteListenerWrapper_RemoteListenerCleanupHook_this_0(oop) getObject((oop), 0)
#define set_com_sun_squawk_Isolate_RemoteListenerWrapper_RemoteListenerCleanupHook_this_0(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_Java5Marker 132
#define com_sun_squawk_JavaApplicationManager 133
#define com_sun_squawk_JavaApplicationManager_1 134
#define com_sun_squawk_JavaApplicationManager_1_val_dos(oop) getObject((oop), 0)
#define set_com_sun_squawk_JavaApplicationManager_1_val_dos(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_KlassMetadata_Full 135
#define com_sun_squawk_KlassMetadata_Full_virtualMethodsMetadata(oop) getObject((oop), 3)
#define com_sun_squawk_KlassMetadata_Full_staticMethodsMetadata(oop) getObject((oop), 4)
#define com_sun_squawk_KlassMetadata_Full_sourceFile(oop) getObject((oop), 5)
#define set_com_sun_squawk_KlassMetadata_Full_virtualMethodsMetadata(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_KlassMetadata_Full_staticMethodsMetadata(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_KlassMetadata_Full_sourceFile(oop, value) setObject((oop), 5, value)
#define com_sun_squawk_KlassMetadata_KlassMetadataComparer 136
#define com_sun_squawk_Lisp2Bitmap 137
#define com_sun_squawk_Lisp2Bitmap_Iterator 138
#define com_sun_squawk_ManifestProperty 139
#define com_sun_squawk_ManifestProperty_name(oop) getObject((oop), 0)
#define com_sun_squawk_ManifestProperty_value(oop) getObject((oop), 1)
#define set_com_sun_squawk_ManifestProperty_name(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ManifestProperty_value(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_ManifestProperty_1 140
#define com_sun_squawk_MethodHeader 141
#define com_sun_squawk_MethodHeader_LOCAL_LONG_ORDER_NORMAL false
#define com_sun_squawk_MethodHeader_ENABLE_SPECIFIC_TYPE_TABLES false
#define com_sun_squawk_MethodHeader_FORCE_LARGE_FORMAT false
#define com_sun_squawk_MethodHeader_FMT_LARGE 128
#define com_sun_squawk_MethodHeader_FMT_E 1
#define com_sun_squawk_MethodHeader_FMT_T 2
#define com_sun_squawk_MethodHeader_FMT_I 4
#define com_sun_squawk_MethodMetadata_MethodMetadataComparer 142
#define com_sun_squawk_Mid1 143
#define com_sun_squawk_Modifier 144
#define com_sun_squawk_Modifier_PUBLIC 1
#define com_sun_squawk_Modifier_PRIVATE 2
#define com_sun_squawk_Modifier_PROTECTED 4
#define com_sun_squawk_Modifier_STATIC 8
#define com_sun_squawk_Modifier_FINAL 16
#define com_sun_squawk_Modifier_SYNCHRONIZED 32
#define com_sun_squawk_Modifier_SUPER 32
#define com_sun_squawk_Modifier_VOLATILE 64
#define com_sun_squawk_Modifier_TRANSIENT 128
#define com_sun_squawk_Modifier_NATIVE 256
#define com_sun_squawk_Modifier_INTERFACE 512
#define com_sun_squawk_Modifier_ABSTRACT 1024
#define com_sun_squawk_Modifier_STRICT 2048
#define com_sun_squawk_Modifier_SOURCE_SYNTHETIC 32768
#define com_sun_squawk_Modifier_SYNTHETIC 524288
#define com_sun_squawk_Modifier_METHOD_CONSTRUCTOR 4096
#define com_sun_squawk_Modifier_METHOD_HAS_PRAGMAS 8192
#define com_sun_squawk_Modifier_FIELD_CONSTANT 16384
#define com_sun_squawk_Modifier_KLASS_HAS_DEFAULT_INIT 4096
#define com_sun_squawk_Modifier_KLASS_HAS_CLINIT 8192
#define com_sun_squawk_Modifier_KLASS_HAS_MAIN 16384
#define com_sun_squawk_Modifier_KLASS_MUSTCLINIT 65536
#define com_sun_squawk_Modifier_PRIMITIVE 262144
#define com_sun_squawk_Modifier_DOUBLEWORD 1048576
#define com_sun_squawk_Modifier_ARRAY 2097152
#define com_sun_squawk_Modifier_SQUAWKARRAY 4194304
#define com_sun_squawk_Modifier_SQUAWKPRIMITIVE 8388608
#define com_sun_squawk_Modifier_COMPLETE_RUNTIME_STATICS 16777216
#define com_sun_squawk_Modifier_GLOBAL_STATICS 33554432
#define com_sun_squawk_Modifier_SUITE_PRIVATE 67108864
#define com_sun_squawk_Monitor 145
#define com_sun_squawk_Monitor_owner(oop) getObject((oop), 0)
#define com_sun_squawk_Monitor_monitorQueue(oop) getObject((oop), 1)
#define com_sun_squawk_Monitor_condvarQueue(oop) getObject((oop), 2)
#define com_sun_squawk_Monitor_object(oop) getObject((oop), 3)
#define com_sun_squawk_Monitor_depth(oop) getShort((oop), 8)
#define com_sun_squawk_Monitor_hasHadWaiter(oop) getByte((oop), 18)
#define set_com_sun_squawk_Monitor_owner(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Monitor_monitorQueue(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_Monitor_condvarQueue(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_Monitor_object(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_Monitor_depth(oop, value) setShort((oop), 8, value)
#define set_com_sun_squawk_Monitor_hasHadWaiter(oop, value) setByte((oop), 18, value)
#define com_sun_squawk_ObjectAssociation 146
#define com_sun_squawk_ObjectAssociation_klass(oop) getObject((oop), 0)
#define com_sun_squawk_ObjectAssociation_virtualMethods(oop) getObject((oop), 1)
#define com_sun_squawk_ObjectAssociation_monitor(oop) getObject((oop), 2)
#define com_sun_squawk_ObjectAssociation_hashCode(oop) getInt((oop), 3)
#define set_com_sun_squawk_ObjectAssociation_klass(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ObjectAssociation_virtualMethods(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_ObjectAssociation_monitor(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_ObjectAssociation_hashCode(oop, value) setInt((oop), 3, value)
#define com_sun_squawk_ObjectMemory 147
#define com_sun_squawk_ObjectMemory_hash(oop) getInt((oop), 0)
#define com_sun_squawk_ObjectMemory_uri(oop) getObject((oop), 1)
#define com_sun_squawk_ObjectMemory_start(oop) getObject((oop), 2)
#define com_sun_squawk_ObjectMemory_size(oop) getInt((oop), 3)
#define com_sun_squawk_ObjectMemory_canonicalStart(oop) getObject((oop), 4)
#define com_sun_squawk_ObjectMemory_root(oop) getObject((oop), 5)
#define com_sun_squawk_ObjectMemory_parent(oop) getObject((oop), 6)
#define set_com_sun_squawk_ObjectMemory_hash(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_ObjectMemory_uri(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_ObjectMemory_start(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_ObjectMemory_size(oop, value) setInt((oop), 3, value)
#define set_com_sun_squawk_ObjectMemory_canonicalStart(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_ObjectMemory_root(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_ObjectMemory_parent(oop, value) setObject((oop), 6, value)
#define com_sun_squawk_ObjectMemory_GCDuringRelocationError 148
#define com_sun_squawk_ObjectMemoryEndianessSwapper 149
#define com_sun_squawk_ObjectMemoryEndianessSwapper_om(oop) getObject((oop), 0)
#define com_sun_squawk_ObjectMemoryEndianessSwapper_object(oop) getObject((oop), 1)
#define com_sun_squawk_ObjectMemoryEndianessSwapper_toPlatform(oop) getByte((oop), 8)
#define com_sun_squawk_ObjectMemoryEndianessSwapper_isCanonical(oop) getByte((oop), 9)
#define set_com_sun_squawk_ObjectMemoryEndianessSwapper_om(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ObjectMemoryEndianessSwapper_object(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_ObjectMemoryEndianessSwapper_toPlatform(oop, value) setByte((oop), 8, value)
#define set_com_sun_squawk_ObjectMemoryEndianessSwapper_isCanonical(oop, value) setByte((oop), 9, value)
#define com_sun_squawk_ObjectMemoryFile 150
#define com_sun_squawk_ObjectMemoryFile_minor(oop) getInt((oop), 0)
#define com_sun_squawk_ObjectMemoryFile_major(oop) getInt((oop), 1)
#define com_sun_squawk_ObjectMemoryFile_attributes(oop) getInt((oop), 2)
#define com_sun_squawk_ObjectMemoryFile_parentHash(oop) getInt((oop), 3)
#define com_sun_squawk_ObjectMemoryFile_parentURI(oop) getObject((oop), 4)
#define com_sun_squawk_ObjectMemoryFile_objectMemory(oop) getObject((oop), 5)
#define set_com_sun_squawk_ObjectMemoryFile_minor(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_ObjectMemoryFile_major(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_ObjectMemoryFile_attributes(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_ObjectMemoryFile_parentHash(oop, value) setInt((oop), 3, value)
#define set_com_sun_squawk_ObjectMemoryFile_parentURI(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_ObjectMemoryFile_objectMemory(oop, value) setObject((oop), 5, value)
#define com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_TYPEMAP 1
#define com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_32BIT 2
#define com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_BIGENDIAN 4
#define com_sun_squawk_ObjectMemoryLoader 151
#define com_sun_squawk_ObjectMemoryLoader_reader(oop) getObject((oop), 0)
#define com_sun_squawk_ObjectMemoryLoader_loadIntoReadOnlyMemory(oop) getByte((oop), 4)
#define com_sun_squawk_ObjectMemoryLoader_requiresEndianSwap(oop) getByte((oop), 5)
#define com_sun_squawk_ObjectMemoryLoader_tracing(oop) getByte((oop), 6)
#define set_com_sun_squawk_ObjectMemoryLoader_reader(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ObjectMemoryLoader_loadIntoReadOnlyMemory(oop, value) setByte((oop), 4, value)
#define set_com_sun_squawk_ObjectMemoryLoader_requiresEndianSwap(oop, value) setByte((oop), 5, value)
#define set_com_sun_squawk_ObjectMemoryLoader_tracing(oop, value) setByte((oop), 6, value)
#define com_sun_squawk_ObjectMemoryLoader_NO_SIGNATURE 0
#define com_sun_squawk_ObjectMemoryLoader_SIMPLE_SIGNATURE 1
#define com_sun_squawk_ObjectMemoryLoader_CHAINED_SIGNATURE 2
#define com_sun_squawk_ObjectMemoryLoader_SIGNATURE_SCHEME 2
#define com_sun_squawk_ObjectMemoryLoader_OutputStreamSink 152
#define com_sun_squawk_ObjectMemoryLoader_OutputStreamSink_length(oop) getInt((oop), 0)
#define set_com_sun_squawk_ObjectMemoryLoader_OutputStreamSink_length(oop, value) setInt((oop), 0, value)
#define com_sun_squawk_ObjectMemoryOutputStream 153
#define com_sun_squawk_ObjectMemoryReader 154
#define com_sun_squawk_ObjectMemorySerializer 155
#define com_sun_squawk_ObjectMemorySerializer_CURRENT_MAJOR_VERSION 1
#define com_sun_squawk_ObjectMemorySerializer_CURRENT_MINOR_VERSION 1
#define com_sun_squawk_ObjectMemorySerializer_ControlBlock 156
#define com_sun_squawk_ObjectMemorySerializer_ControlBlock_memory(oop) getObject((oop), 0)
#define com_sun_squawk_ObjectMemorySerializer_ControlBlock_start(oop) getObject((oop), 1)
#define com_sun_squawk_ObjectMemorySerializer_ControlBlock_oopMap(oop) getObject((oop), 2)
#define com_sun_squawk_ObjectMemorySerializer_ControlBlock_root(oop) getInt((oop), 3)
#define set_com_sun_squawk_ObjectMemorySerializer_ControlBlock_memory(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ObjectMemorySerializer_ControlBlock_start(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_ObjectMemorySerializer_ControlBlock_oopMap(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_ObjectMemorySerializer_ControlBlock_root(oop, value) setInt((oop), 3, value)
#define com_sun_squawk_Ref 157
#define com_sun_squawk_Ref_next(oop) getObject((oop), 0)
#define com_sun_squawk_Ref_referent(oop) getObject((oop), 1)
#define set_com_sun_squawk_Ref_next(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Ref_referent(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_ResourceFile 158
#define com_sun_squawk_ResourceFile_name(oop) getObject((oop), 0)
#define com_sun_squawk_ResourceFile_data(oop) getObject((oop), 1)
#define com_sun_squawk_ResourceFile_length(oop) getInt((oop), 2)
#define com_sun_squawk_ResourceFile_isNew(oop) getByte((oop), 12)
#define com_sun_squawk_ResourceFile_isPersistent(oop) getByte((oop), 13)
#define set_com_sun_squawk_ResourceFile_name(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ResourceFile_data(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_ResourceFile_length(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_ResourceFile_isNew(oop, value) setByte((oop), 12, value)
#define set_com_sun_squawk_ResourceFile_isPersistent(oop, value) setByte((oop), 13, value)
#define com_sun_squawk_ScopedLocalVariable 159
#define com_sun_squawk_ScopedLocalVariable_name(oop) getObject((oop), 0)
#define com_sun_squawk_ScopedLocalVariable_type(oop) getObject((oop), 1)
#define com_sun_squawk_ScopedLocalVariable_slot(oop) getInt((oop), 2)
#define com_sun_squawk_ScopedLocalVariable_start(oop) getInt((oop), 3)
#define com_sun_squawk_ScopedLocalVariable_length(oop) getInt((oop), 4)
#define set_com_sun_squawk_ScopedLocalVariable_name(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ScopedLocalVariable_type(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_ScopedLocalVariable_slot(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_ScopedLocalVariable_start(oop, value) setInt((oop), 3, value)
#define set_com_sun_squawk_ScopedLocalVariable_length(oop, value) setInt((oop), 4, value)
#define com_sun_squawk_ServiceOperation 160
#define com_sun_squawk_ServiceOperation_NONE 0
#define com_sun_squawk_ServiceOperation_EXTEND 1
#define com_sun_squawk_ServiceOperation_GARBAGE_COLLECT 2
#define com_sun_squawk_ServiceOperation_COPY_OBJECT_GRAPH 3
#define com_sun_squawk_ServiceOperation_THROW 4
#define com_sun_squawk_ServiceOperation_CHANNELIO 5
#define com_sun_squawk_StandardObjectMemoryLoader 161
#define com_sun_squawk_Test 162
#define com_sun_squawk_Test_1 163
#define com_sun_squawk_Test_2 164
#define com_sun_squawk_Test_AD 165
#define com_sun_squawk_Test_FOO 166
#define com_sun_squawk_Test_FOO_xxx(oop) getObject((oop), 0)
#define set_com_sun_squawk_Test_FOO_xxx(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_Test_FOO_1 167
#define com_sun_squawk_Test_FOO_1_val_printme(oop) getObject((oop), 0)
#define com_sun_squawk_Test_FOO_1_this_0(oop) getObject((oop), 1)
#define set_com_sun_squawk_Test_FOO_1_val_printme(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_Test_FOO_1_this_0(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_Test_HD 168
#define com_sun_squawk_Test_IDictionary 169
#define com_sun_squawk_TestJava5 170
#define com_sun_squawk_ThreadQueue 171
#define com_sun_squawk_ThreadQueue_first(oop) getObject((oop), 0)
#define com_sun_squawk_ThreadQueue_count(oop) getInt((oop), 1)
#define set_com_sun_squawk_ThreadQueue_first(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_ThreadQueue_count(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_TimerQueue 172
#define com_sun_squawk_TimerQueue_first(oop) getObject((oop), 0)
#define set_com_sun_squawk_TimerQueue_first(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_Unsafe 173
#define com_sun_squawk_VM 174
#define com_sun_squawk_VM_STREAM_STDOUT 0
#define com_sun_squawk_VM_STREAM_STDERR 1
#define com_sun_squawk_VM_STREAM_SYMBOLS 2
#define com_sun_squawk_VM_MAX_SYS_PRIORITY 12
#define com_sun_squawk_VM_Stats 175
#define com_sun_squawk_VM_Stats_values(oop) getObject((oop), 0)
#define set_com_sun_squawk_VM_Stats_values(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_VM_Stats_STAT_WALL_TIME 0
#define com_sun_squawk_VM_Stats_STAT_WAIT_TIME 1
#define com_sun_squawk_VM_Stats_STAT_GC_TIME 2
#define com_sun_squawk_VM_Stats_STAT_FULL_GC_TIME 3
#define com_sun_squawk_VM_Stats_STAT_PARTIAL_GC_TIME 4
#define com_sun_squawk_VM_Stats_STAT_LAST_GC_TIME 5
#define com_sun_squawk_VM_Stats_STAT_MAX_FULLGC_TIME 6
#define com_sun_squawk_VM_Stats_STAT_MAX_PARTGC_TIME 7
#define com_sun_squawk_VM_Stats_STAT_FIRST_COUNT_STAT 8
#define com_sun_squawk_VM_Stats_STAT_FULL_GC_COUNT 9
#define com_sun_squawk_VM_Stats_STAT_PARTIAL_GC_COUNT 10
#define com_sun_squawk_VM_Stats_STAT_BYTES_LAST_FREED 11
#define com_sun_squawk_VM_Stats_STAT_BYTES_TOTAL_FREED 12
#define com_sun_squawk_VM_Stats_STAT_BYTES_TOTAL_ALLOCATED 13
#define com_sun_squawk_VM_Stats_STAT_OBJECTS_TOTAL_ALLOCATED 14
#define com_sun_squawk_VM_Stats_STAT_THREADS_ALLOCATED 15
#define com_sun_squawk_VM_Stats_STAT_THREAD_SWITCH_COUNT 16
#define com_sun_squawk_VM_Stats_STAT_CONTENDED_MONITOR_COUNT 17
#define com_sun_squawk_VM_Stats_STAT_MONITORS_ALLOCATED 18
#define com_sun_squawk_VM_Stats_STAT_STACKS_ALLOCATED 19
#define com_sun_squawk_VM_Stats_STAT_MAX_STACK_SIZE 20
#define com_sun_squawk_VM_Stats_STAT_THROW_COUNT 21
#define com_sun_squawk_VM_Stats_STAT_BRANCH_COUNT 22
#define com_sun_squawk_VM_Stats_STAT_HEAP_FREE 23
#define com_sun_squawk_VM_Stats_STAT_HEAP_TOTAL 24
#define com_sun_squawk_VM_Stats_NUM_STAT_VALUES 25
#define com_sun_squawk_VM_WeakIsolateListEntry 176
#define com_sun_squawk_VM_WeakIsolateListEntry_nextIsolateRef(oop) getObject((oop), 2)
#define set_com_sun_squawk_VM_WeakIsolateListEntry_nextIsolateRef(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_VMBufferDecoder 177
#define com_sun_squawk_VMBufferDecoder_oop(oop) getObject((oop), 0)
#define com_sun_squawk_VMBufferDecoder_offset(oop) getInt((oop), 1)
#define set_com_sun_squawk_VMBufferDecoder_oop(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_VMBufferDecoder_offset(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_Vm2c 178
#define com_sun_squawk_io_BufferedReader 179
#define com_sun_squawk_io_BufferedReader_in(oop) getObject((oop), 2)
#define com_sun_squawk_io_BufferedReader_cb(oop) getObject((oop), 3)
#define com_sun_squawk_io_BufferedReader_nChars(oop) getInt((oop), 4)
#define com_sun_squawk_io_BufferedReader_nextChar(oop) getInt((oop), 5)
#define com_sun_squawk_io_BufferedReader_markedChar(oop) getInt((oop), 6)
#define com_sun_squawk_io_BufferedReader_readAheadLimit(oop) getInt((oop), 7)
#define com_sun_squawk_io_BufferedReader_skipLF(oop) getByte((oop), 32)
#define com_sun_squawk_io_BufferedReader_markedSkipLF(oop) getByte((oop), 33)
#define set_com_sun_squawk_io_BufferedReader_in(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_io_BufferedReader_cb(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_io_BufferedReader_nChars(oop, value) setInt((oop), 4, value)
#define set_com_sun_squawk_io_BufferedReader_nextChar(oop, value) setInt((oop), 5, value)
#define set_com_sun_squawk_io_BufferedReader_markedChar(oop, value) setInt((oop), 6, value)
#define set_com_sun_squawk_io_BufferedReader_readAheadLimit(oop, value) setInt((oop), 7, value)
#define set_com_sun_squawk_io_BufferedReader_skipLF(oop, value) setByte((oop), 32, value)
#define set_com_sun_squawk_io_BufferedReader_markedSkipLF(oop, value) setByte((oop), 33, value)
#define com_sun_squawk_io_BufferedReader_INVALIDATED -2
#define com_sun_squawk_io_BufferedReader_UNMARKED -1
#define com_sun_squawk_io_BufferedReader_defaultCharBufferSize 81
#define com_sun_squawk_io_BufferedReader_defaultExpectedLineLength 80
#define com_sun_squawk_io_BufferedWriter 180
#define com_sun_squawk_io_BufferedWriter_out(oop) getObject((oop), 3)
#define com_sun_squawk_io_BufferedWriter_cb(oop) getObject((oop), 4)
#define com_sun_squawk_io_BufferedWriter_nChars(oop) getInt((oop), 5)
#define com_sun_squawk_io_BufferedWriter_nextChar(oop) getInt((oop), 6)
#define com_sun_squawk_io_BufferedWriter_lineSeparator(oop) getObject((oop), 7)
#define set_com_sun_squawk_io_BufferedWriter_out(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_io_BufferedWriter_cb(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_io_BufferedWriter_nChars(oop, value) setInt((oop), 5, value)
#define set_com_sun_squawk_io_BufferedWriter_nextChar(oop, value) setInt((oop), 6, value)
#define set_com_sun_squawk_io_BufferedWriter_lineSeparator(oop, value) setObject((oop), 7, value)
#define com_sun_squawk_io_BufferedWriter_defaultCharBufferSize 8192
#define com_sun_squawk_io_ConnectionBase 181
#define com_sun_squawk_io_MulticastOutputStream 182
#define com_sun_squawk_io_MulticastOutputStream_streams(oop) getObject((oop), 0)
#define set_com_sun_squawk_io_MulticastOutputStream_streams(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_io_MulticastOutputStream_DEFAULT_SIZE 3
#define com_sun_squawk_io_mailboxes_AddressClosedException 183
#define com_sun_squawk_io_mailboxes_AddressClosedException_closedAddress(oop) getObject((oop), 2)
#define set_com_sun_squawk_io_mailboxes_AddressClosedException_closedAddress(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_io_mailboxes_ByteArrayEnvelope 184
#define com_sun_squawk_io_mailboxes_ByteArrayEnvelope_contents(oop) getObject((oop), 1)
#define com_sun_squawk_io_mailboxes_ByteArrayEnvelope_offset(oop) getInt((oop), 2)
#define com_sun_squawk_io_mailboxes_ByteArrayEnvelope_len(oop) getInt((oop), 3)
#define set_com_sun_squawk_io_mailboxes_ByteArrayEnvelope_contents(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_io_mailboxes_ByteArrayEnvelope_offset(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_io_mailboxes_ByteArrayEnvelope_len(oop, value) setInt((oop), 3, value)
#define com_sun_squawk_io_mailboxes_ByteArrayInputStreamEnvelope 185
#define com_sun_squawk_io_mailboxes_ByteArrayInputStreamEnvelope_contents(oop) getObject((oop), 1)
#define set_com_sun_squawk_io_mailboxes_ByteArrayInputStreamEnvelope_contents(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_io_mailboxes_Channel 186
#define com_sun_squawk_io_mailboxes_Channel_inBox(oop) getObject((oop), 0)
#define com_sun_squawk_io_mailboxes_Channel_outBox(oop) getObject((oop), 1)
#define set_com_sun_squawk_io_mailboxes_Channel_inBox(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_io_mailboxes_Channel_outBox(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_io_mailboxes_Envelope 187
#define com_sun_squawk_io_mailboxes_Envelope_toAddress(oop) getObject((oop), 0)
#define set_com_sun_squawk_io_mailboxes_Envelope_toAddress(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_io_mailboxes_ICopiable 188
#define com_sun_squawk_io_mailboxes_Mailbox 189
#define com_sun_squawk_io_mailboxes_Mailbox_name(oop) getObject((oop), 0)
#define com_sun_squawk_io_mailboxes_Mailbox_handler(oop) getObject((oop), 1)
#define com_sun_squawk_io_mailboxes_Mailbox_owner(oop) getObject((oop), 2)
#define com_sun_squawk_io_mailboxes_Mailbox_inbox(oop) getObject((oop), 3)
#define com_sun_squawk_io_mailboxes_Mailbox_closed(oop) getByte((oop), 16)
#define com_sun_squawk_io_mailboxes_Mailbox_registered(oop) getByte((oop), 17)
#define set_com_sun_squawk_io_mailboxes_Mailbox_name(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_io_mailboxes_Mailbox_handler(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_io_mailboxes_Mailbox_owner(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_io_mailboxes_Mailbox_inbox(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_io_mailboxes_Mailbox_closed(oop, value) setByte((oop), 16, value)
#define set_com_sun_squawk_io_mailboxes_Mailbox_registered(oop, value) setByte((oop), 17, value)
#define com_sun_squawk_io_mailboxes_Mailbox_1 190
#define com_sun_squawk_io_mailboxes_Mailbox_AnonymousMailboxHandler 191
#define com_sun_squawk_io_mailboxes_MailboxAddress 192
#define com_sun_squawk_io_mailboxes_MailboxAddress_state(oop) getInt((oop), 0)
#define com_sun_squawk_io_mailboxes_MailboxAddress_owner(oop) getObject((oop), 1)
#define com_sun_squawk_io_mailboxes_MailboxAddress_otherAddress(oop) getObject((oop), 2)
#define com_sun_squawk_io_mailboxes_MailboxAddress_mailbox(oop) getObject((oop), 3)
#define set_com_sun_squawk_io_mailboxes_MailboxAddress_state(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_io_mailboxes_MailboxAddress_owner(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_io_mailboxes_MailboxAddress_otherAddress(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_io_mailboxes_MailboxAddress_mailbox(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_io_mailboxes_MailboxAddress_UNOWNED 0
#define com_sun_squawk_io_mailboxes_MailboxAddress_OPEN 1
#define com_sun_squawk_io_mailboxes_MailboxAddress_CLOSED 2
#define com_sun_squawk_io_mailboxes_MailboxAddress_AddressClosedEnvelope 193
#define com_sun_squawk_io_mailboxes_MailboxClosedException 194
#define com_sun_squawk_io_mailboxes_MailboxHandler 195
#define com_sun_squawk_io_mailboxes_MailboxInUseException 196
#define com_sun_squawk_io_mailboxes_NoSuchMailboxException 197
#define com_sun_squawk_io_mailboxes_ObjectEnvelope 198
#define com_sun_squawk_io_mailboxes_ObjectEnvelope_contents(oop) getObject((oop), 1)
#define set_com_sun_squawk_io_mailboxes_ObjectEnvelope_contents(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_io_mailboxes_ServerChannel 199
#define com_sun_squawk_io_mailboxes_ServerChannel_serverBox(oop) getObject((oop), 0)
#define set_com_sun_squawk_io_mailboxes_ServerChannel_serverBox(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_io_mailboxes_ServerChannel_NewChannelEnvelope 200
#define com_sun_squawk_io_mailboxes_ServerChannel_NewChannelEnvelope_newChannel(oop) getObject((oop), 1)
#define set_com_sun_squawk_io_mailboxes_ServerChannel_NewChannelEnvelope_newChannel(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_io_mailboxes_ServerChannel_ServerChannelMailboxHandler 201
#define com_sun_squawk_io_mailboxes_SharedMailboxHandler 202
#define com_sun_squawk_peripheral_INorFlashSector 203
#define com_sun_squawk_peripheral_INorFlashSector_USER_PURPOSED 1
#define com_sun_squawk_peripheral_INorFlashSector_SYSTEM_PURPOSED 2
#define com_sun_squawk_peripheral_INorFlashSector_RMS_PURPOSED 3
#define com_sun_squawk_peripheral_INorFlashSectorAllocator 204
#define com_sun_squawk_peripheral_IPeripheral 205
#define com_sun_squawk_peripheral_InsufficientFlashMemoryException 206
#define com_sun_squawk_peripheral_PeripheralRegistry 207
#define com_sun_squawk_peripheral_PeripheralRegistry_registeredPeripherals(oop) getObject((oop), 0)
#define com_sun_squawk_peripheral_PeripheralRegistry_peripheralArraysByType(oop) getObject((oop), 1)
#define set_com_sun_squawk_peripheral_PeripheralRegistry_registeredPeripherals(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_peripheral_PeripheralRegistry_peripheralArraysByType(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_peripheral_PeripheralRegistry_DEFAULT_SIZE 3
#define com_sun_squawk_peripheral_SimulatedNorFlashSector 208
#define com_sun_squawk_peripheral_SimulatedNorFlashSector_purpose(oop) getInt((oop), 0)
#define com_sun_squawk_peripheral_SimulatedNorFlashSector_size(oop) getInt((oop), 1)
#define com_sun_squawk_peripheral_SimulatedNorFlashSector_startAddress(oop) getObject((oop), 2)
#define com_sun_squawk_peripheral_SimulatedNorFlashSector_bytes(oop) getObject((oop), 3)
#define com_sun_squawk_peripheral_SimulatedNorFlashSector_fileName(oop) getObject((oop), 4)
#define set_com_sun_squawk_peripheral_SimulatedNorFlashSector_purpose(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_peripheral_SimulatedNorFlashSector_size(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_peripheral_SimulatedNorFlashSector_startAddress(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_peripheral_SimulatedNorFlashSector_bytes(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_peripheral_SimulatedNorFlashSector_fileName(oop, value) setObject((oop), 4, value)
#define com_sun_squawk_peripheral_SimulatedNorFlashSectorAllocator 209
#define com_sun_squawk_peripheral_SimulatedNorFlashSectorAllocator_sectors(oop) getObject((oop), 0)
#define set_com_sun_squawk_peripheral_SimulatedNorFlashSectorAllocator_sectors(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_platform_GCFSockets 210
#define com_sun_squawk_platform_SystemEvents 211
#define com_sun_squawk_pragma_GlobalStaticFields 212
#define com_sun_squawk_pragma_InterpreterInvokedPragma 213
#define com_sun_squawk_pragma_PragmaException 214
#define com_sun_squawk_pragma_PragmaException_HOSTED 1
#define com_sun_squawk_pragma_PragmaException_REPLACEMENT_CONSTRUCTOR 2
#define com_sun_squawk_pragma_PragmaException_INTERPRETER_INVOKED 4
#define com_sun_squawk_pragma_PragmaException_NATIVE 8
#define com_sun_squawk_pragma_PragmaException_ALLOW_INLINED 16
#define com_sun_squawk_pragma_PragmaException_NOT_INLINED 32
#define com_sun_squawk_pragma_PragmaException_FORCE_INLINED_A 64
#define com_sun_squawk_pragma_PragmaException_FORCE_INLINED 80
#define com_sun_squawk_realtime_OffsetOutOfBoundsException 215
#define com_sun_squawk_realtime_RawMemoryAccess 216
#define com_sun_squawk_realtime_RawMemoryAccess_vbase(oop) getObject((oop), 0)
#define com_sun_squawk_realtime_RawMemoryAccess_reachable_size(oop) getUWord((oop), 1)
#define com_sun_squawk_realtime_RawMemoryAccess_state(oop) getByte((oop), 8)
#define set_com_sun_squawk_realtime_RawMemoryAccess_vbase(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_realtime_RawMemoryAccess_reachable_size(oop, value) setUWord((oop), 1, value)
#define set_com_sun_squawk_realtime_RawMemoryAccess_state(oop, value) setByte((oop), 8, value)
#define com_sun_squawk_realtime_RawMemoryAccess_SHARED 0
#define com_sun_squawk_realtime_RawMemoryAccess_MALLOCED 1
#define com_sun_squawk_realtime_RawMemoryAccess_STACK 2
#define com_sun_squawk_realtime_RawMemoryFloatAccess 217
#define com_sun_squawk_realtime_SizeOutOfBoundsException 218
#define com_sun_squawk_realtime_UnsupportedPhysicalMemoryException 219
#define com_sun_squawk_security_CryptoException 220
#define com_sun_squawk_security_CryptoException_reason(oop) getInt((oop), 2)
#define set_com_sun_squawk_security_CryptoException_reason(oop, value) setInt((oop), 2, value)
#define com_sun_squawk_security_CryptoException_ILLEGAL_VALUE 1
#define com_sun_squawk_security_CryptoException_UNINITIALIZED_KEY 2
#define com_sun_squawk_security_CryptoException_INVALID_INIT 4
#define com_sun_squawk_security_CryptoException_ILLEGAL_USE 5
#define com_sun_squawk_security_ECPublicKey 221
#define com_sun_squawk_security_ECPublicKey_keyData(oop) getObject((oop), 0)
#define com_sun_squawk_security_ECPublicKey_bitsize(oop) getInt((oop), 1)
#define com_sun_squawk_security_ECPublicKey_bytesize(oop) getInt((oop), 2)
#define com_sun_squawk_security_ECPublicKey_curve(oop) getObject((oop), 3)
#define com_sun_squawk_security_ECPublicKey_ffa(oop) getObject((oop), 4)
#define com_sun_squawk_security_ECPublicKey_initOk(oop) getByte((oop), 20)
#define set_com_sun_squawk_security_ECPublicKey_keyData(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_security_ECPublicKey_bitsize(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_security_ECPublicKey_bytesize(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_security_ECPublicKey_curve(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_security_ECPublicKey_ffa(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_security_ECPublicKey_initOk(oop, value) setByte((oop), 20, value)
#define com_sun_squawk_security_HexEncoding 222
#define com_sun_squawk_security_ecc_ECCurveFp 223
#define com_sun_squawk_security_ecc_ECCurveFp_t1(oop) getObject((oop), 0)
#define com_sun_squawk_security_ecc_ECCurveFp_t2(oop) getObject((oop), 1)
#define com_sun_squawk_security_ecc_ECCurveFp_t3(oop) getObject((oop), 2)
#define com_sun_squawk_security_ecc_ECCurveFp_t4(oop) getObject((oop), 3)
#define com_sun_squawk_security_ecc_ECCurveFp_field(oop) getObject((oop), 4)
#define com_sun_squawk_security_ecc_ECCurveFp_a(oop) getObject((oop), 5)
#define com_sun_squawk_security_ecc_ECCurveFp_b(oop) getObject((oop), 6)
#define com_sun_squawk_security_ecc_ECCurveFp_h(oop) getInt((oop), 7)
#define com_sun_squawk_security_ecc_ECCurveFp_generator(oop) getObject((oop), 8)
#define com_sun_squawk_security_ecc_ECCurveFp_order(oop) getObject((oop), 9)
#define com_sun_squawk_security_ecc_ECCurveFp_ffa(oop) getObject((oop), 10)
#define com_sun_squawk_security_ecc_ECCurveFp_aIsMinus3(oop) getByte((oop), 44)
#define set_com_sun_squawk_security_ecc_ECCurveFp_t1(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_t2(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_t3(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_t4(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_field(oop, value) setObject((oop), 4, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_a(oop, value) setObject((oop), 5, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_b(oop, value) setObject((oop), 6, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_h(oop, value) setInt((oop), 7, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_generator(oop, value) setObject((oop), 8, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_order(oop, value) setObject((oop), 9, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_ffa(oop, value) setObject((oop), 10, value)
#define set_com_sun_squawk_security_ecc_ECCurveFp_aIsMinus3(oop, value) setByte((oop), 44, value)
#define com_sun_squawk_security_ecc_ECPoint 224
#define com_sun_squawk_security_ecc_ECPoint_x(oop) getObject((oop), 0)
#define com_sun_squawk_security_ecc_ECPoint_y(oop) getObject((oop), 1)
#define com_sun_squawk_security_ecc_ECPoint_z(oop) getObject((oop), 2)
#define com_sun_squawk_security_ecc_ECPoint_curve(oop) getObject((oop), 3)
#define com_sun_squawk_security_ecc_ECPoint_ffa(oop) getObject((oop), 4)
#define set_com_sun_squawk_security_ecc_ECPoint_x(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_security_ecc_ECPoint_y(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_security_ecc_ECPoint_z(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_security_ecc_ECPoint_curve(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_security_ecc_ECPoint_ffa(oop, value) setObject((oop), 4, value)
#define com_sun_squawk_security_ecc_FFA 225
#define com_sun_squawk_security_ecc_FFA_vars(oop) getObject((oop), 0)
#define com_sun_squawk_security_ecc_FFA_varsCount(oop) getInt((oop), 1)
#define com_sun_squawk_security_ecc_FFA_bitLength(oop) getInt((oop), 2)
#define com_sun_squawk_security_ecc_FFA_byteLength(oop) getInt((oop), 3)
#define com_sun_squawk_security_ecc_FFA_len(oop) getInt((oop), 4)
#define com_sun_squawk_security_ecc_FFA_doubleLen(oop) getInt((oop), 5)
#define set_com_sun_squawk_security_ecc_FFA_vars(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_security_ecc_FFA_varsCount(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_security_ecc_FFA_bitLength(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_security_ecc_FFA_byteLength(oop, value) setInt((oop), 3, value)
#define set_com_sun_squawk_security_ecc_FFA_len(oop, value) setInt((oop), 4, value)
#define set_com_sun_squawk_security_ecc_FFA_doubleLen(oop, value) setInt((oop), 5, value)
#define com_sun_squawk_security_ecc_FFA_BITS_PER_WORD 28
#define com_sun_squawk_security_ecc_FFA_BMASK 268435455
#define com_sun_squawk_security_ecc_NIST160PrimeField 226
#define com_sun_squawk_security_ecc_NIST160PrimeField_BMASK 268435455
#define com_sun_squawk_security_ecc_PrimeField 227
#define com_sun_squawk_security_ecc_PrimeField_tmp(oop) getObject((oop), 0)
#define com_sun_squawk_security_ecc_PrimeField_ffa(oop) getObject((oop), 1)
#define com_sun_squawk_security_ecc_PrimeField_p(oop) getObject((oop), 2)
#define set_com_sun_squawk_security_ecc_PrimeField_tmp(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_security_ecc_PrimeField_ffa(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_security_ecc_PrimeField_p(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_util_ArgsUtilities 228
#define com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator 229
#define com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_index(oop) getInt((oop), 0)
#define com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_table(oop) getObject((oop), 1)
#define com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_entry(oop) getObject((oop), 2)
#define com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_keys(oop) getByte((oop), 12)
#define set_com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_index(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_table(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_entry(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_util_ArrayHashtable_ArrayHashtableEnumerator_keys(oop, value) setByte((oop), 12, value)
#define com_sun_squawk_util_ArrayHashtableEntry 230
#define com_sun_squawk_util_ArrayHashtableEntry_hash(oop) getInt((oop), 0)
#define com_sun_squawk_util_ArrayHashtableEntry_key(oop) getObject((oop), 1)
#define com_sun_squawk_util_ArrayHashtableEntry_value(oop) getObject((oop), 2)
#define com_sun_squawk_util_ArrayHashtableEntry_next(oop) getObject((oop), 3)
#define set_com_sun_squawk_util_ArrayHashtableEntry_hash(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_util_ArrayHashtableEntry_key(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_ArrayHashtableEntry_value(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_util_ArrayHashtableEntry_next(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_util_Arrays 231
#define com_sun_squawk_util_Assert 232
#define com_sun_squawk_util_Assert_ASSERTS_ENABLED false
#define com_sun_squawk_util_Assert_SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED true
#define com_sun_squawk_util_Assert_ASSERT_ALWAYS_IS_FATAL true
#define com_sun_squawk_util_ByteArrayInputStreamWithSetBytes 233
#define com_sun_squawk_util_ByteArrayInputStreamWithSetBytes_offset(oop) getInt((oop), 4)
#define set_com_sun_squawk_util_ByteArrayInputStreamWithSetBytes_offset(oop, value) setInt((oop), 4, value)
#define com_sun_squawk_util_ByteArrayOutputStreamWithGetBytes 234
#define com_sun_squawk_util_Comparer 235
#define com_sun_squawk_util_ComputationTimer 236
#define com_sun_squawk_util_ComputationTimer_Computation 237
#define com_sun_squawk_util_ComputationTimer_ComputationException 238
#define com_sun_squawk_util_ComputationTimer_Execution 239
#define com_sun_squawk_util_ComputationTimer_Execution_nestedTimes(oop) getLongAtWord((oop), 0)
#define com_sun_squawk_util_ComputationTimer_Execution_result(oop) getObject((oop), 2)
#define com_sun_squawk_util_ComputationTimer_Execution_exception(oop) getObject((oop), 3)
#define set_com_sun_squawk_util_ComputationTimer_Execution_nestedTimes(oop, value) setLongAtWord((oop), 0, value)
#define set_com_sun_squawk_util_ComputationTimer_Execution_result(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_util_ComputationTimer_Execution_exception(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_util_DataInputUTF8Decoder 240
#define com_sun_squawk_util_DataOutputUTF8Encoder 241
#define com_sun_squawk_util_HashtableEntry 242
#define com_sun_squawk_util_HashtableEntry_hash(oop) getInt((oop), 0)
#define com_sun_squawk_util_HashtableEntry_key(oop) getObject((oop), 1)
#define com_sun_squawk_util_HashtableEntry_value(oop) getObject((oop), 2)
#define com_sun_squawk_util_HashtableEntry_next(oop) getObject((oop), 3)
#define set_com_sun_squawk_util_HashtableEntry_hash(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_util_HashtableEntry_key(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_HashtableEntry_value(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_util_HashtableEntry_next(oop, value) setObject((oop), 3, value)
#define com_sun_squawk_util_IntHashtable_HashtableEnumerator 243
#define com_sun_squawk_util_IntHashtable_HashtableEnumerator_index(oop) getInt((oop), 0)
#define com_sun_squawk_util_IntHashtable_HashtableEnumerator_table(oop) getObject((oop), 1)
#define com_sun_squawk_util_IntHashtable_HashtableEnumerator_entry(oop) getObject((oop), 2)
#define com_sun_squawk_util_IntHashtable_HashtableEnumerator_keys(oop) getByte((oop), 12)
#define set_com_sun_squawk_util_IntHashtable_HashtableEnumerator_index(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_util_IntHashtable_HashtableEnumerator_table(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_IntHashtable_HashtableEnumerator_entry(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_util_IntHashtable_HashtableEnumerator_keys(oop, value) setByte((oop), 12, value)
#define com_sun_squawk_util_IntHashtableEntry 244
#define com_sun_squawk_util_IntHashtableEntry_key(oop) getInt((oop), 0)
#define com_sun_squawk_util_IntHashtableEntry_value(oop) getObject((oop), 1)
#define com_sun_squawk_util_IntHashtableEntry_next(oop) getObject((oop), 2)
#define set_com_sun_squawk_util_IntHashtableEntry_key(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_util_IntHashtableEntry_value(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_IntHashtableEntry_next(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_util_IntHashtableVisitor 245
#define com_sun_squawk_util_IntSet 246
#define com_sun_squawk_util_IntSet_elementData(oop) getObject((oop), 0)
#define com_sun_squawk_util_IntSet_elementCount(oop) getInt((oop), 1)
#define set_com_sun_squawk_util_IntSet_elementData(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_IntSet_elementCount(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_util_LineReader 247
#define com_sun_squawk_util_LineReader_in(oop) getObject((oop), 0)
#define set_com_sun_squawk_util_LineReader_in(oop, value) setObject((oop), 0, value)
#define com_sun_squawk_util_MathUtils 248
#define com_sun_squawk_util_MathUtils_sign_mask JLONG_CONSTANT(-9223372036854775808)
#define com_sun_squawk_util_MathUtils_no_sign_mask JLONG_CONSTANT(9223372036854775807)
#define com_sun_squawk_util_MathUtils_exp_mask JLONG_CONSTANT(9218868437227405312)
#define com_sun_squawk_util_MathUtils_no_exp_mask JLONG_CONSTANT(-9218868437227405313)
#define com_sun_squawk_util_MathUtils_significand_mask JLONG_CONSTANT(4503599627370495)
#define com_sun_squawk_util_MathUtils_implicit_significand_bit JLONG_CONSTANT(4503599627370496)
#define com_sun_squawk_util_MathUtils_one JLONG_CONSTANT(4607182418800017408)
#define com_sun_squawk_util_MathUtils_half JLONG_CONSTANT(4602678819172646912)
#define com_sun_squawk_util_MathUtils_low_bits_mask JLONG_CONSTANT(4294967295)
#define com_sun_squawk_util_MathUtils_high_bits_mask JLONG_CONSTANT(-4294967296)
#define com_sun_squawk_util_MathUtils_two54 1.8014398509481984E16
#define com_sun_squawk_util_MathUtils_two53 9.007199254740992E15
#define com_sun_squawk_util_MathUtils_twom54 5.551115123125783E-17
#define com_sun_squawk_util_MathUtils_two24 1.6777216E7
#define com_sun_squawk_util_MathUtils_twon24 5.9604644775390625E-8
#define com_sun_squawk_util_MathUtils_twon28 3.725290298461914E-9
#define com_sun_squawk_util_MathUtils_twon27 7.450580596923828E-9
#define com_sun_squawk_util_MathUtils_pio2_hi 1.5707963267948966
#define com_sun_squawk_util_MathUtils_pio2_lo 6.123233995736766E-17
#define com_sun_squawk_util_MathUtils_pio4_hi 0.7853981633974483
#define com_sun_squawk_util_MathUtils_pio4_lo 3.061616997868383E-17
#define com_sun_squawk_util_MathUtils_arc_pS0 0.16666666666666666
#define com_sun_squawk_util_MathUtils_arc_pS1 -0.3255658186224009
#define com_sun_squawk_util_MathUtils_arc_pS2 0.20121253213486293
#define com_sun_squawk_util_MathUtils_arc_pS3 -0.04005553450067941
#define com_sun_squawk_util_MathUtils_arc_pS4 7.915349942898145E-4
#define com_sun_squawk_util_MathUtils_arc_pS5 3.479331075960212E-5
#define com_sun_squawk_util_MathUtils_arc_qS1 -2.403394911734414
#define com_sun_squawk_util_MathUtils_arc_qS2 2.0209457602335057
#define com_sun_squawk_util_MathUtils_arc_qS3 -0.6882839716054533
#define com_sun_squawk_util_MathUtils_arc_qS4 0.07703815055590194
#define com_sun_squawk_util_MathUtils_exp_P1 0.16666666666666602
#define com_sun_squawk_util_MathUtils_exp_P2 -0.0027777777777015593
#define com_sun_squawk_util_MathUtils_exp_P3 6.613756321437934E-5
#define com_sun_squawk_util_MathUtils_exp_P4 -1.6533902205465252E-6
#define com_sun_squawk_util_MathUtils_exp_P5 4.1381367970572385E-8
#define com_sun_squawk_util_MathUtils_o_threshold 709.782712893384
#define com_sun_squawk_util_MathUtils_u_threshold -745.1332191019411
#define com_sun_squawk_util_MathUtils_ln2_hi 0.6931471803691238
#define com_sun_squawk_util_MathUtils_ln2_lo 1.9082149292705877E-10
#define com_sun_squawk_util_MathUtils_invln2 1.4426950408889634
#define com_sun_squawk_util_MathUtils_Lg1 0.6666666666666735
#define com_sun_squawk_util_MathUtils_Lg2 0.3999999999940942
#define com_sun_squawk_util_MathUtils_Lg3 0.2857142874366239
#define com_sun_squawk_util_MathUtils_Lg4 0.22222198432149784
#define com_sun_squawk_util_MathUtils_Lg5 0.1818357216161805
#define com_sun_squawk_util_MathUtils_Lg6 0.15313837699209373
#define com_sun_squawk_util_MathUtils_Lg7 0.14798198605116586
#define com_sun_squawk_util_MathUtils_ln2 0.6931471805599453
#define com_sun_squawk_util_MathUtils_twom1000 9.332636185032189E-302
#define com_sun_squawk_util_MathUtils_pow_L1 0.5999999999999946
#define com_sun_squawk_util_MathUtils_pow_L2 0.4285714285785502
#define com_sun_squawk_util_MathUtils_pow_L3 0.33333332981837743
#define com_sun_squawk_util_MathUtils_pow_L4 0.272728123808534
#define com_sun_squawk_util_MathUtils_pow_L5 0.23066074577556175
#define com_sun_squawk_util_MathUtils_pow_L6 0.20697501780033842
#define com_sun_squawk_util_MathUtils_pow_P1 0.16666666666666602
#define com_sun_squawk_util_MathUtils_pow_P2 -0.0027777777777015593
#define com_sun_squawk_util_MathUtils_pow_P3 6.613756321437934E-5
#define com_sun_squawk_util_MathUtils_pow_P4 -1.6533902205465252E-6
#define com_sun_squawk_util_MathUtils_pow_P5 4.1381367970572385E-8
#define com_sun_squawk_util_MathUtils_lg2 0.6931471805599453
#define com_sun_squawk_util_MathUtils_lg2_h 0.6931471824645996
#define com_sun_squawk_util_MathUtils_lg2_l -1.904654299957768E-9
#define com_sun_squawk_util_MathUtils_ovt 8.008566259537294E-17
#define com_sun_squawk_util_MathUtils_cp 0.9617966939259756
#define com_sun_squawk_util_MathUtils_cp_h 0.9617967009544373
#define com_sun_squawk_util_MathUtils_cp_l -7.028461650952758E-9
#define com_sun_squawk_util_MathUtils_ivln2 1.4426950408889634
#define com_sun_squawk_util_MathUtils_ivln2_h 1.4426950216293335
#define com_sun_squawk_util_MathUtils_ivln2_l 1.9259629911266175E-8
#define com_sun_squawk_util_MathUtils_Lp1 0.6666666666666735
#define com_sun_squawk_util_MathUtils_Lp2 0.3999999999940942
#define com_sun_squawk_util_MathUtils_Lp3 0.2857142874366239
#define com_sun_squawk_util_MathUtils_Lp4 0.22222198432149784
#define com_sun_squawk_util_MathUtils_Lp5 0.1818357216161805
#define com_sun_squawk_util_MathUtils_Lp6 0.15313837699209373
#define com_sun_squawk_util_MathUtils_Lp7 0.14798198605116586
#define com_sun_squawk_util_MathUtils_expm1_Q1 -0.03333333333333313
#define com_sun_squawk_util_MathUtils_expm1_Q2 0.0015873015872548146
#define com_sun_squawk_util_MathUtils_expm1_Q3 -7.93650757867488E-5
#define com_sun_squawk_util_MathUtils_expm1_Q4 4.008217827329362E-6
#define com_sun_squawk_util_MathUtils_expm1_Q5 -2.0109921818362437E-7
#define com_sun_squawk_util_NotImplementedYetException 249
#define com_sun_squawk_util_SimpleLinkedList 250
#define com_sun_squawk_util_SimpleLinkedList_header(oop) getObject((oop), 0)
#define com_sun_squawk_util_SimpleLinkedList_size(oop) getInt((oop), 1)
#define set_com_sun_squawk_util_SimpleLinkedList_header(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_SimpleLinkedList_size(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_util_SimpleLinkedList_Entry 251
#define com_sun_squawk_util_SimpleLinkedList_Entry_element(oop) getObject((oop), 0)
#define com_sun_squawk_util_SimpleLinkedList_Entry_next(oop) getObject((oop), 1)
#define com_sun_squawk_util_SimpleLinkedList_Entry_previous(oop) getObject((oop), 2)
#define set_com_sun_squawk_util_SimpleLinkedList_Entry_element(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_SimpleLinkedList_Entry_next(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_SimpleLinkedList_Entry_previous(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_util_SimpleLinkedList_SimpleLinkedListEnumerator 252
#define com_sun_squawk_util_SimpleLinkedList_SimpleLinkedListEnumerator_list(oop) getObject((oop), 0)
#define com_sun_squawk_util_SimpleLinkedList_SimpleLinkedListEnumerator_pointer(oop) getObject((oop), 1)
#define set_com_sun_squawk_util_SimpleLinkedList_SimpleLinkedListEnumerator_list(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_SimpleLinkedList_SimpleLinkedListEnumerator_pointer(oop, value) setObject((oop), 1, value)
#define com_sun_squawk_util_SquawkHashtable_HashtableEnumerator 253
#define com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_table(oop) getObject((oop), 0)
#define com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_entry(oop) getObject((oop), 1)
#define com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_index(oop) getInt((oop), 2)
#define com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_this_0(oop) getObject((oop), 3)
#define com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_keys(oop) getByte((oop), 16)
#define set_com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_table(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_entry(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_index(oop, value) setInt((oop), 2, value)
#define set_com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_this_0(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_util_SquawkHashtable_HashtableEnumerator_keys(oop, value) setByte((oop), 16, value)
#define com_sun_squawk_util_SquawkHashtable_Rehasher 254
#define com_sun_squawk_util_StringTokenizer 255
#define com_sun_squawk_util_StringTokenizer_currentPosition(oop) getInt((oop), 0)
#define com_sun_squawk_util_StringTokenizer_maxPosition(oop) getInt((oop), 1)
#define com_sun_squawk_util_StringTokenizer_str(oop) getObject((oop), 2)
#define com_sun_squawk_util_StringTokenizer_delimiters(oop) getObject((oop), 3)
#define com_sun_squawk_util_StringTokenizer_retTokens(oop) getByte((oop), 16)
#define set_com_sun_squawk_util_StringTokenizer_currentPosition(oop, value) setInt((oop), 0, value)
#define set_com_sun_squawk_util_StringTokenizer_maxPosition(oop, value) setInt((oop), 1, value)
#define set_com_sun_squawk_util_StringTokenizer_str(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_util_StringTokenizer_delimiters(oop, value) setObject((oop), 3, value)
#define set_com_sun_squawk_util_StringTokenizer_retTokens(oop, value) setByte((oop), 16, value)
#define com_sun_squawk_util_StructuredFileInputStream 256
#define com_sun_squawk_util_StructuredFileInputStream_filePath(oop) getObject((oop), 0)
#define com_sun_squawk_util_StructuredFileInputStream_in(oop) getObject((oop), 1)
#define com_sun_squawk_util_StructuredFileInputStream_traceFeature(oop) getObject((oop), 2)
#define com_sun_squawk_util_StructuredFileInputStream_bytesRead(oop) getInt((oop), 3)
#define set_com_sun_squawk_util_StructuredFileInputStream_filePath(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_StructuredFileInputStream_in(oop, value) setObject((oop), 1, value)
#define set_com_sun_squawk_util_StructuredFileInputStream_traceFeature(oop, value) setObject((oop), 2, value)
#define set_com_sun_squawk_util_StructuredFileInputStream_bytesRead(oop, value) setInt((oop), 3, value)
#define com_sun_squawk_util_Tracer 257
#define com_sun_squawk_util_UnexpectedException 258
#define com_sun_squawk_util_UnexpectedException_unexpected(oop) getObject((oop), 2)
#define set_com_sun_squawk_util_UnexpectedException_unexpected(oop, value) setObject((oop), 2, value)
#define com_sun_squawk_util_VectorEnumerator 259
#define com_sun_squawk_util_VectorEnumerator_vector(oop) getObject((oop), 0)
#define com_sun_squawk_util_VectorEnumerator_count(oop) getInt((oop), 1)
#define set_com_sun_squawk_util_VectorEnumerator_vector(oop, value) setObject((oop), 0, value)
#define set_com_sun_squawk_util_VectorEnumerator_count(oop, value) setInt((oop), 1, value)
#define com_sun_squawk_vm_AddressType 260
#define AddressType_UNDEFINED 0
#define AddressType_ANY 1
#define AddressType_BYTECODE 2
#define AddressType_BYTE 3
#define AddressType_SHORT 4
#define AddressType_INT 5
#define AddressType_FLOAT 6
#define AddressType_LONG 7
#define AddressType_LONG2 8
#define AddressType_DOUBLE 9
#define AddressType_DOUBLE2 10
#define AddressType_REF 11
#define AddressType_UWORD 12
#define AddressType_TYPE_MASK 15
#define AddressType_MUTATION_TYPE_SHIFT 4
#define AddressType_UNDEFINED_WORD JLONG_CONSTANT(0)
#define AddressType_ANY_WORD 16843009
#define com_sun_squawk_vm_CID 261
#define CID_NULL 0
#define CID_OBJECT 1
#define CID_STRING 2
#define CID_THROWABLE 3
#define CID_KLASS 4
#define CID_VOID 5
#define CID_BOOLEAN 6
#define CID_BYTE 7
#define CID_CHAR 8
#define CID_SHORT 9
#define CID_INT 10
#define CID_LONG 11
#define CID_LONG2 12
#define CID_FLOAT 13
#define CID_DOUBLE 14
#define CID_DOUBLE2 15
#define CID_OBJECT_ARRAY 16
#define CID_STRING_ARRAY 17
#define CID_BOOLEAN_ARRAY 18
#define CID_BYTE_ARRAY 19
#define CID_CHAR_ARRAY 20
#define CID_SHORT_ARRAY 21
#define CID_INT_ARRAY 22
#define CID_LONG_ARRAY 23
#define CID_FLOAT_ARRAY 24
#define CID_DOUBLE_ARRAY 25
#define CID_STRING_OF_BYTES 26
#define CID_LOCAL 27
#define CID_GLOBAL 28
#define CID_LOCAL_ARRAY 29
#define CID_GLOBAL_ARRAY 30
#define CID_GLOBAL_ARRAYARRAY 31
#define CID_BYTECODE 32
#define CID_BYTECODE_ARRAY 33
#define CID_ADDRESS 34
#define CID_ADDRESS_ARRAY 35
#define CID_UWORD 36
#define CID_UWORD_ARRAY 37
#define CID_OFFSET 38
#define CID_NATIVEUNSAFE 39
#define CID_TOP 40
#define CID_ONE_WORD 41
#define CID_TWO_WORD 42
#define CID_REFERENCE 43
#define CID_UNINITIALIZED 44
#define CID_UNINITIALIZED_THIS 45
#define CID_UNINITIALIZED_NEW 46
#define CID_LAST_SYSTEM_ID 46
#define com_sun_squawk_vm_CS 262
#define CS_klass 0
#define CS_next 1
#define CS_firstVariable 2
#define CS_DEBUG false
#define com_sun_squawk_vm_ChannelConstants 263
#define ChannelConstants_CHANNEL_GENERIC 1
#define ChannelConstants_CHANNEL_LAST_FIXED 1
#define ChannelConstants_RESULT_OK 0
#define ChannelConstants_RESULT_BADCONTEXT -1
#define ChannelConstants_RESULT_EXCEPTION -2
#define ChannelConstants_RESULT_BADPARAMETER -3
#define ChannelConstants_RESULT_MALLOCFAILURE -4
#define ChannelConstants_GLOBAL_CREATECONTEXT 1
#define ChannelConstants_GLOBAL_GETEVENT 2
#define ChannelConstants_GLOBAL_POSTEVENT 3
#define ChannelConstants_GLOBAL_WAITFOREVENT 4
#define ChannelConstants_CONTEXT_DELETE 5
#define ChannelConstants_CONTEXT_HIBERNATE 6
#define ChannelConstants_CONTEXT_GETHIBERNATIONDATA 7
#define ChannelConstants_CONTEXT_GETCHANNEL 8
#define ChannelConstants_CONTEXT_FREECHANNEL 9
#define ChannelConstants_CONTEXT_GETRESULT 10
#define ChannelConstants_CONTEXT_GETRESULT_2 11
#define ChannelConstants_CONTEXT_GETERROR 12
#define ChannelConstants_LAST_BASIC_OPCODE 12
#define ChannelConstants_OPENCONNECTION 13
#define ChannelConstants_CLOSECONNECTION 14
#define ChannelConstants_ACCEPTCONNECTION 15
#define ChannelConstants_OPENINPUT 16
#define ChannelConstants_CLOSEINPUT 17
#define ChannelConstants_WRITEREAD 18
#define ChannelConstants_READBYTE 19
#define ChannelConstants_READSHORT 20
#define ChannelConstants_READINT 21
#define ChannelConstants_READLONG 22
#define ChannelConstants_READBUF 23
#define ChannelConstants_SKIP 24
#define ChannelConstants_AVAILABLE 25
#define ChannelConstants_MARK 26
#define ChannelConstants_RESET 27
#define ChannelConstants_MARKSUPPORTED 28
#define ChannelConstants_OPENOUTPUT 29
#define ChannelConstants_FLUSH 30
#define ChannelConstants_CLOSEOUTPUT 31
#define ChannelConstants_WRITEBYTE 32
#define ChannelConstants_WRITESHORT 33
#define ChannelConstants_WRITEINT 34
#define ChannelConstants_WRITELONG 35
#define ChannelConstants_WRITEBUF 36
#define ChannelConstants_LAST_GENERIC_CONNECTION_OPCODE 36
#define ChannelConstants_PLATFORM_OPCODES 37
#define ChannelConstants_DLOPEN 37
#define ChannelConstants_DLCLOSE 38
#define ChannelConstants_DLERROR 39
#define ChannelConstants_DLSYM 40
#define ChannelConstants_LAST_PLATFORM_TYPE_OPCODE 40
#define ChannelConstants_LAST_GUI_OPCODE 40
#define ChannelConstants_CHANNEL_IRQ 2
#define ChannelConstants_CHANNEL_SPI 3
#define ChannelConstants_CHANNEL_LAST_PLATFORM 3
#define ChannelConstants_PLATFORM_OPCODE 41
#define ChannelConstants_IRQ_WAIT 41
#define ChannelConstants_GET_HARDWARE_REVISION 42
#define ChannelConstants_GET_LAST_DEVICE_INTERRUPT_TIME_ADDR 43
#define ChannelConstants_GET_CURRENT_TIME_ADDR 44
#define ChannelConstants_SPI_OPCODES 45
#define ChannelConstants_SPI_SEND_RECEIVE_8 45
#define ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_SEND_16 46
#define ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_SEND_N 47
#define ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_RECEIVE_16 48
#define ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_VARIABLE_RECEIVE_N 49
#define ChannelConstants_SPI_SEND_AND_RECEIVE_WITH_DEVICE_SELECT 50
#define ChannelConstants_SPI_SEND_AND_RECEIVE 51
#define ChannelConstants_SPI_GET_MAX_TRANSFER_SIZE 52
#define ChannelConstants_SPI_PULSE_WITH_DEVICE_SELECT 53
#define ChannelConstants_I2C_OPCODES 54
#define ChannelConstants_I2C_OPEN 54
#define ChannelConstants_I2C_CLOSE 55
#define ChannelConstants_I2C_SET_CLOCK_SPEED 56
#define ChannelConstants_I2C_READ 57
#define ChannelConstants_I2C_WRITE 58
#define ChannelConstants_I2C_BUSY 59
#define ChannelConstants_I2C_PROBE 60
#define ChannelConstants_SLEEP_OPCODES 61
#define ChannelConstants_DEEP_SLEEP 61
#define ChannelConstants_SHALLOW_SLEEP 62
#define ChannelConstants_WAIT_FOR_DEEP_SLEEP 63
#define ChannelConstants_DEEP_SLEEP_TIME_MILLIS_HIGH 64
#define ChannelConstants_DEEP_SLEEP_TIME_MILLIS_LOW 65
#define ChannelConstants_SET_DEEP_SLEEP_ENABLED 66
#define ChannelConstants_SET_MINIMUM_DEEP_SLEEP_TIME 67
#define ChannelConstants_TOTAL_SHALLOW_SLEEP_TIME_MILLIS_HIGH 68
#define ChannelConstants_TOTAL_SHALLOW_SLEEP_TIME_MILLIS_LOW 69
#define ChannelConstants_SET_SHALLOW_SLEEP_CLOCK_MODE 70
#define ChannelConstants_MISC_OPCODES 71
#define ChannelConstants_FLASH_ERASE 71
#define ChannelConstants_FLASH_WRITE 72
#define ChannelConstants_USB_GET_STATE 73
#define ChannelConstants_GET_SERIAL_CHARS 74
#define ChannelConstants_WRITE_SERIAL_CHARS 75
#define ChannelConstants_AVAILABLE_SERIAL_CHARS 76
#define ChannelConstants_AVR_GET_TIME_HIGH 77
#define ChannelConstants_AVR_GET_TIME_LOW 78
#define ChannelConstants_AVR_GET_STATUS 79
#define ChannelConstants_WRITE_SECURED_SILICON_AREA 80
#define ChannelConstants_READ_SECURED_SILICON_AREA 81
#define ChannelConstants_SET_SYSTEM_TIME 82
#define ChannelConstants_ENABLE_AVR_CLOCK_SYNCHRONISATION 83
#define ChannelConstants_GET_PUBLIC_KEY 84
#define ChannelConstants_COMPUTE_CRC16_FOR_MEMORY_REGION 85
#define ChannelConstants_REPROGRAM_MMU 86
#define ChannelConstants_GET_ALLOCATED_FILE_SIZE 87
#define ChannelConstants_GET_FILE_VIRTUAL_ADDRESS 88
#define ChannelConstants_GET_DMA_BUFFER_SIZE 89
#define ChannelConstants_GET_DMA_BUFFER_ADDRESS 90
#define ChannelConstants_GET_RECORDED_OUTPUT 91
#define ChannelConstants_GET_VAR_ADDR 92
#define ChannelConstants_GET_SYSTEM_CORE_CLOCK 93
#define ChannelConstants_ETHERNET_OP 94
#define ChannelConstants_ETHERNET_INIT 94
#define ChannelConstants_ETHERNET_CONFIG_NETIF 95
#define ChannelConstants_ETHERNET_START_PROCESS 96
#define ChannelConstants_ETHERNET_STOP_PROCESS 97
#define ChannelConstants_ETHERNET_BIND 98
#define ChannelConstants_ETHERNET_LISTEN 99
#define ChannelConstants_ETHERNET_SEND 100
#define ChannelConstants_ETHERNET_CONNECT 101
#define ChannelConstants_ETHERNET_CLOSE 102
#define ChannelConstants_ETHERNET_TCP_NEW 103
#define ChannelConstants_ETHERNET_CHECK_EVENT 104
#define ChannelConstants_ETHERNET_POP_EVENT 105
#define ChannelConstants_ETHERNET_GET_EVENT_OPCODE 106
#define ChannelConstants_ETHERNET_GET_EVENT_PCB 107
#define ChannelConstants_ETHERNET_GET_EVENT_BUF 108
#define ChannelConstants_ETHERNET_GET_EVENT_BUF_PTR 109
#define ChannelConstants_ETHERNET_GET_EVENT_BUF_PUSHTOAPP 110
#define ChannelConstants_ETHERNET_GET_EVENT_BUF_LEN 111
#define ChannelConstants_ETHERNET_GET_EVENT_ERR 112
#define ChannelConstants_ETHERNET_GET_EVENT_ARG 113
#define ChannelConstants_ETHERNET_COPY_PBUF 114
#define ChannelConstants_ETHERNET_FREE_PBUF 115
#define ChannelConstants_ETHERNET_SET_SCKOPT 116
#define ChannelConstants_ETHERNET_GET_SCKOPT 117
#define ChannelConstants_GET_ETHERNET_EVENT 118
#define ChannelConstants_ETHERNET_DNS_SETSERVER 119
#define ChannelConstants_ETHERNET_DNS_GETSERVER 120
#define ChannelConstants_VAR_JAVA_IRQ_GPIO_STATUS 0
#define ChannelConstants_INTERNAL_SETSTREAM 1000
#define ChannelConstants_INTERNAL_OPENSTREAM 1001
#define ChannelConstants_INTERNAL_PRINTCHAR 1002
#define ChannelConstants_INTERNAL_PRINTSTRING 1003
#define ChannelConstants_INTERNAL_PRINTINT 1004
#define ChannelConstants_INTERNAL_PRINTFLOAT 1005
#define ChannelConstants_INTERNAL_PRINTDOUBLE 1006
#define ChannelConstants_INTERNAL_PRINTUWORD 1007
#define ChannelConstants_INTERNAL_PRINTOFFSET 1008
#define ChannelConstants_INTERNAL_PRINTLONG 1009
#define ChannelConstants_INTERNAL_PRINTADDRESS 1010
#define ChannelConstants_INTERNAL_LOW_RESULT 1011
#define ChannelConstants_INTERNAL_GETTIMEMILLIS_HIGH 1012
#define ChannelConstants_INTERNAL_GETTIMEMICROS_HIGH 1013
#define ChannelConstants_INTERNAL_STOPVM 1014
#define ChannelConstants_INTERNAL_NATIVE_PLATFORM_NAME 1015
#define ChannelConstants_INTERNAL_PRINTCONFIGURATION 1016
#define ChannelConstants_INTERNAL_PRINTGLOBALOOPNAME 1017
#define ChannelConstants_INTERNAL_PRINTGLOBALS 1018
#define ChannelConstants_INTERNAL_GETPATHSEPARATORCHAR 1019
#define ChannelConstants_INTERNAL_GETFILESEPARATORCHAR 1020
#define ChannelConstants_INTERNAL_COMPUTE_SHA1_FOR_MEMORY_REGION 1021
#define ChannelConstants_INTERNAL_PRINTBYTES 1022
#define ChannelConstants_RPI_PRINTSTRING 1023
#define ChannelConstants_LAST_OPCODE 1023
#define com_sun_squawk_vm_FP 264
#define FP_parm0 3
#define FP_returnIP 2
#define FP_returnFP 1
#define FP_local0 0
#define FP_method 0
#define FP_FIXED_FRAME_SIZE 3
#define com_sun_squawk_vm_FieldOffsets 265
#define FieldOffsets_CIDSHIFT 32
#define FieldOffsets_OOP JLONG_CONSTANT(4294967296)
#define FieldOffsets_INT JLONG_CONSTANT(42949672960)
#define FieldOffsets_SHORT JLONG_CONSTANT(38654705664)
#define FieldOffsets_BYTE JLONG_CONSTANT(30064771072)
#define FieldOffsets_com_sun_squawk_Klass_self JLONG_CONSTANT(4294967296)
#define FieldOffsets_com_sun_squawk_ObjectAssociation_klass JLONG_CONSTANT(4294967296)
#define FieldOffsets_com_sun_squawk_Klass_virtualMethods JLONG_CONSTANT(4294967297)
#define FieldOffsets_com_sun_squawk_ObjectAssociation_virtualMethods JLONG_CONSTANT(4294967297)
#define FieldOffsets_com_sun_squawk_Klass_staticMethods JLONG_CONSTANT(4294967298)
#define FieldOffsets_com_sun_squawk_Klass_name JLONG_CONSTANT(4294967299)
#define FieldOffsets_com_sun_squawk_Klass_componentType JLONG_CONSTANT(4294967300)
#define FieldOffsets_com_sun_squawk_Klass_superType JLONG_CONSTANT(4294967301)
#define FieldOffsets_com_sun_squawk_Klass_interfaces JLONG_CONSTANT(4294967302)
#define FieldOffsets_com_sun_squawk_Klass_objects JLONG_CONSTANT(4294967304)
#define FieldOffsets_com_sun_squawk_Klass_oopMap JLONG_CONSTANT(4294967305)
#define FieldOffsets_com_sun_squawk_Klass_oopMapWord JLONG_CONSTANT(4294967306)
#define FieldOffsets_com_sun_squawk_Klass_dataMap JLONG_CONSTANT(4294967307)
#define FieldOffsets_com_sun_squawk_Klass_dataMapWord JLONG_CONSTANT(4294967308)
#define FieldOffsets_com_sun_squawk_Klass_modifiers JLONG_CONSTANT(42949672973)
#define FieldOffsets_com_sun_squawk_Klass_dataMapLength JLONG_CONSTANT(38654705692)
#define FieldOffsets_com_sun_squawk_Klass_id JLONG_CONSTANT(38654705693)
#define FieldOffsets_com_sun_squawk_Klass_instanceSizeBytes JLONG_CONSTANT(38654705694)
#define FieldOffsets_com_sun_squawk_Klass_staticFieldsSize JLONG_CONSTANT(38654705695)
#define FieldOffsets_com_sun_squawk_Klass_refStaticFieldsSize JLONG_CONSTANT(38654705696)
#define FieldOffsets_com_sun_squawk_Klass_state JLONG_CONSTANT(30064771138)
#define FieldOffsets_com_sun_squawk_Klass_initModifiers JLONG_CONSTANT(30064771139)
#define FieldOffsets_com_sun_squawk_util_SquawkHashtable_entryTable JLONG_CONSTANT(4294967296)
#define FieldOffsets_java_lang_Thread_vmThread JLONG_CONSTANT(4294967296)
#define FieldOffsets_java_lang_Thread_target JLONG_CONSTANT(4294967297)
#define FieldOffsets_java_lang_Class_klass JLONG_CONSTANT(4294967296)
#define FieldOffsets_com_sun_squawk_VMThread_isolate JLONG_CONSTANT(4294967298)
#define FieldOffsets_com_sun_squawk_VMThread_stack JLONG_CONSTANT(4294967299)
#define FieldOffsets_com_sun_squawk_VMThread_stackSize JLONG_CONSTANT(42949672964)
#define FieldOffsets_java_lang_Throwable_trace JLONG_CONSTANT(4294967297)
#define FieldOffsets_com_sun_squawk_Suite_classes JLONG_CONSTANT(4294967296)
#define FieldOffsets_com_sun_squawk_Suite_name JLONG_CONSTANT(4294967297)
#define FieldOffsets_com_sun_squawk_Suite_metadatas JLONG_CONSTANT(4294967298)
#define FieldOffsets_com_sun_squawk_Suite_type JLONG_CONSTANT(42949672963)
#define FieldOffsets_com_sun_squawk_KlassMetadata_definedClass JLONG_CONSTANT(4294967296)
#define FieldOffsets_com_sun_squawk_KlassMetadata_symbols JLONG_CONSTANT(4294967297)
#define FieldOffsets_com_sun_squawk_KlassMetadata_classTable JLONG_CONSTANT(4294967298)
#define com_sun_squawk_vm_Global 266
#define com_sun_squawk_vm_HDR 267
#define HDR_BYTES_PER_WORD 4
#define HDR_LOG2_BYTES_PER_WORD 2
#define HDR_BITS_PER_BYTE 8
#define HDR_BITS_PER_WORD 32
#define HDR_LOG2_BITS_PER_WORD 5
#define HDR_LOG2_BITS_PER_BYTE 3
#define HDR_klass -1
#define HDR_length -2
#define HDR_methodDefiningClass -3
#define HDR_methodInfoStart -13
#define HDR_basicHeaderSize 4
#define HDR_arrayHeaderSize 8
#define HDR_headerTagBits 2
#define HDR_headerTagMask 3
#define HDR_basicHeaderTag 0
#define HDR_arrayHeaderTag 1
#define HDR_methodHeaderTag 3
#define HDR_forwardPointerBit 2
#define com_sun_squawk_vm_MathOpcodes 268
#define MathOpcodes_SIN 1
#define MathOpcodes_COS 2
#define MathOpcodes_TAN 3
#define MathOpcodes_ASIN 4
#define MathOpcodes_ACOS 5
#define MathOpcodes_ATAN 6
#define MathOpcodes_EXP 7
#define MathOpcodes_LOG 8
#define MathOpcodes_SQRT 9
#define MathOpcodes_CEIL 10
#define MathOpcodes_FLOOR 11
#define MathOpcodes_ATAN2 12
#define MathOpcodes_POW 13
#define MathOpcodes_IEEE_REMAINDER 14
#define MathOpcodes_DUMMY 999
#define com_sun_squawk_vm_MessageBuffer 269
#define MessageBuffer_next 0
#define MessageBuffer_pos 1
#define MessageBuffer_count 2
#define MessageBuffer_buf 3
#define MessageBuffer_HEADERSIZE 12
#define MessageBuffer_BUFFERSIZE 116
#define com_sun_squawk_vm_MessageStruct 270
#define MessageStruct_next 0
#define MessageStruct_status 1
#define MessageStruct_data 2
#define MessageStruct_key 3
#define MessageStruct_HEADERSIZE 12
#define MessageStruct_MAX_MESSAGE_KEY_SIZE 116
#define com_sun_squawk_vm_MethodOffsets 271
#define MethodOffsets_com_sun_squawk_VM_startup 1
#define MethodOffsets_com_sun_squawk_VM_undefinedNativeMethod 2
#define MethodOffsets_com_sun_squawk_VM_callRun 3
#define MethodOffsets_com_sun_squawk_VM_getStaticOop 4
#define MethodOffsets_com_sun_squawk_VM_getStaticInt 5
#define MethodOffsets_com_sun_squawk_VM_getStaticLong 6
#define MethodOffsets_com_sun_squawk_VM_putStaticOop 7
#define MethodOffsets_com_sun_squawk_VM_putStaticInt 8
#define MethodOffsets_com_sun_squawk_VM_putStaticLong 9
#define MethodOffsets_com_sun_squawk_VM_yield 10
#define MethodOffsets_com_sun_squawk_VM_nullPointerException 11
#define MethodOffsets_com_sun_squawk_VM_arrayIndexOutOfBoundsException 12
#define MethodOffsets_com_sun_squawk_VM_arithmeticException 13
#define MethodOffsets_com_sun_squawk_VM_abstractMethodError 14
#define MethodOffsets_com_sun_squawk_VM_arrayStoreException 15
#define MethodOffsets_com_sun_squawk_VM_monitorenter 16
#define MethodOffsets_com_sun_squawk_VM_monitorexit 17
#define MethodOffsets_com_sun_squawk_VM_checkcastException 18
#define MethodOffsets_com_sun_squawk_VM_class_clinit 19
#define MethodOffsets_com_sun_squawk_VM__new 20
#define MethodOffsets_com_sun_squawk_VM_newarray 21
#define MethodOffsets_com_sun_squawk_VM_newdimension 22
#define MethodOffsets_com_sun_squawk_VM__lcmp 23
#define MethodOffsets_com_sun_squawk_VM_reportException 24
#define MethodOffsets_com_sun_squawk_VM_reportBreakpoint 25
#define MethodOffsets_com_sun_squawk_VM_reportStepEvent 26
#define MethodOffsets_virtual_java_lang_Object_toString 8
#define MethodOffsets_virtual_java_lang_Object_abstractMethodError 9
#define MethodOffsets_java_lang_Object_missingMethodError 1
#define com_sun_squawk_vm_Native 272
#define Native_com_sun_squawk_Address_add 0
#define Native_com_sun_squawk_Address_addOffset 1
#define Native_com_sun_squawk_Address_and 2
#define Native_com_sun_squawk_Address_diff 3
#define Native_com_sun_squawk_Address_eq 4
#define Native_com_sun_squawk_Address_fromObject 5
#define Native_com_sun_squawk_Address_fromPrimitive 6
#define Native_com_sun_squawk_Address_hi 7
#define Native_com_sun_squawk_Address_hieq 8
#define Native_com_sun_squawk_Address_isMax 9
#define Native_com_sun_squawk_Address_isZero 10
#define Native_com_sun_squawk_Address_lo 11
#define Native_com_sun_squawk_Address_loeq 12
#define Native_com_sun_squawk_Address_max 13
#define Native_com_sun_squawk_Address_ne 14
#define Native_com_sun_squawk_Address_or 15
#define Native_com_sun_squawk_Address_roundDown 16
#define Native_com_sun_squawk_Address_roundDownToWord 17
#define Native_com_sun_squawk_Address_roundUp 18
#define Native_com_sun_squawk_Address_roundUpToWord 19
#define Native_com_sun_squawk_Address_sub 20
#define Native_com_sun_squawk_Address_subOffset 21
#define Native_com_sun_squawk_Address_toObject 22
#define Native_com_sun_squawk_Address_toUWord 23
#define Native_com_sun_squawk_Address_zero 24
#define Native_com_sun_squawk_UWord_and 25
#define Native_com_sun_squawk_UWord_eq 26
#define Native_com_sun_squawk_UWord_fromPrimitive 27
#define Native_com_sun_squawk_UWord_hi 28
#define Native_com_sun_squawk_UWord_hieq 29
#define Native_com_sun_squawk_UWord_isMax 30
#define Native_com_sun_squawk_UWord_isZero 31
#define Native_com_sun_squawk_UWord_lo 32
#define Native_com_sun_squawk_UWord_loeq 33
#define Native_com_sun_squawk_UWord_max 34
#define Native_com_sun_squawk_UWord_ne 35
#define Native_com_sun_squawk_UWord_or 36
#define Native_com_sun_squawk_UWord_toInt 37
#define Native_com_sun_squawk_UWord_toOffset 38
#define Native_com_sun_squawk_UWord_toPrimitive 39
#define Native_com_sun_squawk_UWord_zero 40
#define Native_com_sun_squawk_Offset_add 41
#define Native_com_sun_squawk_Offset_bytesToWords 42
#define Native_com_sun_squawk_Offset_eq 43
#define Native_com_sun_squawk_Offset_fromPrimitive 44
#define Native_com_sun_squawk_Offset_ge 45
#define Native_com_sun_squawk_Offset_gt 46
#define Native_com_sun_squawk_Offset_isZero 47
#define Native_com_sun_squawk_Offset_le 48
#define Native_com_sun_squawk_Offset_lt 49
#define Native_com_sun_squawk_Offset_ne 50
#define Native_com_sun_squawk_Offset_sub 51
#define Native_com_sun_squawk_Offset_toInt 52
#define Native_com_sun_squawk_Offset_toPrimitive 53
#define Native_com_sun_squawk_Offset_toUWord 54
#define Native_com_sun_squawk_Offset_wordsToBytes 55
#define Native_com_sun_squawk_Offset_zero 56
#define Native_com_sun_squawk_NativeUnsafe_call0 57
#define Native_com_sun_squawk_NativeUnsafe_call1 58
#define Native_com_sun_squawk_NativeUnsafe_call10 59
#define Native_com_sun_squawk_NativeUnsafe_call2 60
#define Native_com_sun_squawk_NativeUnsafe_call3 61
#define Native_com_sun_squawk_NativeUnsafe_call4 62
#define Native_com_sun_squawk_NativeUnsafe_call5 63
#define Native_com_sun_squawk_NativeUnsafe_call6 64
#define Native_com_sun_squawk_NativeUnsafe_cancelTaskExecutor 65
#define Native_com_sun_squawk_NativeUnsafe_charAt 66
#define Native_com_sun_squawk_NativeUnsafe_copyTypes 67
#define Native_com_sun_squawk_NativeUnsafe_createTaskExecutor 68
#define Native_com_sun_squawk_NativeUnsafe_deleteNativeTask 69
#define Native_com_sun_squawk_NativeUnsafe_deleteTaskExecutor 70
#define Native_com_sun_squawk_NativeUnsafe_free 71
#define Native_com_sun_squawk_NativeUnsafe_getAddress 72
#define Native_com_sun_squawk_NativeUnsafe_getAsByte 73
#define Native_com_sun_squawk_NativeUnsafe_getAsInt 74
#define Native_com_sun_squawk_NativeUnsafe_getAsShort 75
#define Native_com_sun_squawk_NativeUnsafe_getAsUWord 76
#define Native_com_sun_squawk_NativeUnsafe_getByte 77
#define Native_com_sun_squawk_NativeUnsafe_getChar 78
#define Native_com_sun_squawk_NativeUnsafe_getInt 79
#define Native_com_sun_squawk_NativeUnsafe_getLong 80
#define Native_com_sun_squawk_NativeUnsafe_getLongAtWord 81
#define Native_com_sun_squawk_NativeUnsafe_getObject 82
#define Native_com_sun_squawk_NativeUnsafe_getShort 83
#define Native_com_sun_squawk_NativeUnsafe_getType 84
#define Native_com_sun_squawk_NativeUnsafe_getUWord 85
#define Native_com_sun_squawk_NativeUnsafe_getUnalignedInt 86
#define Native_com_sun_squawk_NativeUnsafe_getUnalignedLong 87
#define Native_com_sun_squawk_NativeUnsafe_getUnalignedShort 88
#define Native_com_sun_squawk_NativeUnsafe_malloc 89
#define Native_com_sun_squawk_NativeUnsafe_runBlockingFunctionOn 90
#define Native_com_sun_squawk_NativeUnsafe_setAddress 91
#define Native_com_sun_squawk_NativeUnsafe_setArrayTypes 92
#define Native_com_sun_squawk_NativeUnsafe_setByte 93
#define Native_com_sun_squawk_NativeUnsafe_setChar 94
#define Native_com_sun_squawk_NativeUnsafe_setInt 95
#define Native_com_sun_squawk_NativeUnsafe_setLong 96
#define Native_com_sun_squawk_NativeUnsafe_setLongAtWord 97
#define Native_com_sun_squawk_NativeUnsafe_setObject 98
#define Native_com_sun_squawk_NativeUnsafe_setShort 99
#define Native_com_sun_squawk_NativeUnsafe_setType 100
#define Native_com_sun_squawk_NativeUnsafe_setUWord 101
#define Native_com_sun_squawk_NativeUnsafe_setUnalignedInt 102
#define Native_com_sun_squawk_NativeUnsafe_setUnalignedLong 103
#define Native_com_sun_squawk_NativeUnsafe_setUnalignedShort 104
#define Native_com_sun_squawk_NativeUnsafe_swap 105
#define Native_com_sun_squawk_NativeUnsafe_swap2 106
#define Native_com_sun_squawk_NativeUnsafe_swap4 107
#define Native_com_sun_squawk_NativeUnsafe_swap8 108
#define Native_com_sun_squawk_VM_addToClassStateCache 109
#define Native_com_sun_squawk_VM_addressResult 110
#define Native_com_sun_squawk_VM_allocate 111
#define Native_com_sun_squawk_VM_asKlass 112
#define Native_com_sun_squawk_VM_callStaticNoParm 113
#define Native_com_sun_squawk_VM_callStaticOneParm 114
#define Native_com_sun_squawk_VM_copyBytes 115
#define Native_com_sun_squawk_VM_deadbeef 116
#define Native_com_sun_squawk_VM_doubleToLongBits 117
#define Native_com_sun_squawk_VM_executeCIO 118
#define Native_com_sun_squawk_VM_executeCOG 119
#define Native_com_sun_squawk_VM_executeGC 120
#define Native_com_sun_squawk_VM_fatalVMError 121
#define Native_com_sun_squawk_VM_finalize 122
#define Native_com_sun_squawk_VM_floatToIntBits 123
#define Native_com_sun_squawk_VM_getBranchCount 124
#define Native_com_sun_squawk_VM_getFP 125
#define Native_com_sun_squawk_VM_getGlobalAddr 126
#define Native_com_sun_squawk_VM_getGlobalInt 127
#define Native_com_sun_squawk_VM_getGlobalOop 128
#define Native_com_sun_squawk_VM_getGlobalOopCount 129
#define Native_com_sun_squawk_VM_getGlobalOopTable 130
#define Native_com_sun_squawk_VM_getMP 131
#define Native_com_sun_squawk_VM_getPreviousFP 132
#define Native_com_sun_squawk_VM_getPreviousIP 133
#define Native_com_sun_squawk_VM_hasVirtualMonitorObject 134
#define Native_com_sun_squawk_VM_hashcode 135
#define Native_com_sun_squawk_VM_intBitsToFloat 136
#define Native_com_sun_squawk_VM_invalidateClassStateCache 137
#define Native_com_sun_squawk_VM_isBigEndian 138
#define Native_com_sun_squawk_VM_longBitsToDouble 139
#define Native_com_sun_squawk_VM_math 140
#define Native_com_sun_squawk_VM_removeVirtualMonitorObject 141
#define Native_com_sun_squawk_VM_serviceResult 142
#define Native_com_sun_squawk_VM_setBytes 143
#define Native_com_sun_squawk_VM_setGlobalAddr 144
#define Native_com_sun_squawk_VM_setGlobalInt 145
#define Native_com_sun_squawk_VM_setGlobalOop 146
#define Native_com_sun_squawk_VM_setPreviousFP 147
#define Native_com_sun_squawk_VM_setPreviousIP 148
#define Native_com_sun_squawk_VM_threadSwitch 149
#define Native_com_sun_squawk_VM_zeroWords 150
#define Native_com_sun_squawk_CheneyCollector_memoryProtect 151
#define Native_com_sun_squawk_ServiceOperation_cioExecute 152
#define Native_com_sun_squawk_GarbageCollector_collectGarbageInC 153
#define Native_com_sun_squawk_GarbageCollector_hasNativeImplementation 154
#define Native_com_sun_squawk_Lisp2Bitmap_clearBitFor 155
#define Native_com_sun_squawk_Lisp2Bitmap_clearBitsFor 156
#define Native_com_sun_squawk_Lisp2Bitmap_getAddressForBitmapWord 157
#define Native_com_sun_squawk_Lisp2Bitmap_getAddressOfBitmapWordFor 158
#define Native_com_sun_squawk_Lisp2Bitmap_initialize 159
#define Native_com_sun_squawk_Lisp2Bitmap_iterate 160
#define Native_com_sun_squawk_Lisp2Bitmap_setBitFor 161
#define Native_com_sun_squawk_Lisp2Bitmap_setBitsFor 162
#define Native_com_sun_squawk_Lisp2Bitmap_testAndSetBitFor 163
#define Native_com_sun_squawk_Lisp2Bitmap_testBitFor 164
#define Native_com_sun_squawk_VM_lcmp 165
#define Native_com_sun_squawk_VM_jnaPrint 166
#define Native_com_sun_squawk_VM_jnaFetchNewData 167
#define Native_com_sun_squawk_VM_jnaSendAckByte 168
#define Native_com_sun_squawk_VM_jnaFetchAdcData 169
#define Native_com_sun_squawk_VM_jnaSendSpeedPwmData 170
#define Native_com_sun_squawk_VM_jnaSendSteerPwmData 171
#define Native_com_sun_squawk_VM_jnaSendPackageData 172
#define Native_com_sun_squawk_VM_jnaFetchFrontWheelSpeed 173
#define Native_com_sun_squawk_VM_jnaFetchBackWheelSpeed 174
#define Native_com_sun_squawk_VM_jnaCheckIfNewPackage 175
#define Native_com_sun_squawk_VM_jnaFetchByte 176
#define Native_com_sun_squawk_VM_jnaGetLengthPackage 177
#define Native_com_sun_squawk_VM_jnaGetReadStartIndex 178
#define Native_com_sun_squawk_VM_jnaGetReadRearIndex 179
#define Native_com_sun_squawk_VM_jnaReadPosition 180
#define Native_com_sun_squawk_VM_jnaReadUltrasonicData 181
#define Native_com_sun_squawk_VM_jnaWritePluginData2VCU 182
#define Native_com_sun_squawk_VM_jnaReadPluginDataSizeFromSCU 183
#define Native_com_sun_squawk_VM_jnaReadPluginDataByteFromSCU 184
#define Native_com_sun_squawk_VM_jnaResetPluginDataSizeFromSCU 185
#define Native_com_sun_squawk_VM_jnaReadIMUPart1 186
#define Native_com_sun_squawk_VM_jnaReadIMUPart2 187
#define Native_com_sun_squawk_VM_jnaSetLED 188
#define Native_com_sun_squawk_VM_jnaSetSpeedWithSelector 189
#define Native_com_sun_squawk_VM_jnaSetSteerWithSelector 190
#define Native_com_sun_squawk_VM_jnaFetchSpeedFromPirte 191
#define Native_com_sun_squawk_VM_jnaFetchSteerFromPirte 192
#define Native_com_sun_squawk_VM_jnaReadPluginDataSizeFromTCU 193
#define Native_com_sun_squawk_VM_jnaReadPluginDataByteFromTCU 194
#define Native_com_sun_squawk_VM_jnaResetPluginDataSizeFromTCU 195
#define Native_com_sun_squawk_VM_jnaSetSelect 196
#define Native_ENTRY_COUNT 197
#define com_sun_squawk_vm_OPC 273
#define OPC_CONST_0 0
#define OPC_CONST_1 1
#define OPC_CONST_2 2
#define OPC_CONST_3 3
#define OPC_CONST_4 4
#define OPC_CONST_5 5
#define OPC_CONST_6 6
#define OPC_CONST_7 7
#define OPC_CONST_8 8
#define OPC_CONST_9 9
#define OPC_CONST_10 10
#define OPC_CONST_11 11
#define OPC_CONST_12 12
#define OPC_CONST_13 13
#define OPC_CONST_14 14
#define OPC_CONST_15 15
#define OPC_OBJECT_0 16
#define OPC_OBJECT_1 17
#define OPC_OBJECT_2 18
#define OPC_OBJECT_3 19
#define OPC_OBJECT_4 20
#define OPC_OBJECT_5 21
#define OPC_OBJECT_6 22
#define OPC_OBJECT_7 23
#define OPC_OBJECT_8 24
#define OPC_OBJECT_9 25
#define OPC_OBJECT_10 26
#define OPC_OBJECT_11 27
#define OPC_OBJECT_12 28
#define OPC_OBJECT_13 29
#define OPC_OBJECT_14 30
#define OPC_OBJECT_15 31
#define OPC_LOAD_0 32
#define OPC_LOAD_1 33
#define OPC_LOAD_2 34
#define OPC_LOAD_3 35
#define OPC_LOAD_4 36
#define OPC_LOAD_5 37
#define OPC_LOAD_6 38
#define OPC_LOAD_7 39
#define OPC_LOAD_8 40
#define OPC_LOAD_9 41
#define OPC_LOAD_10 42
#define OPC_LOAD_11 43
#define OPC_LOAD_12 44
#define OPC_LOAD_13 45
#define OPC_LOAD_14 46
#define OPC_LOAD_15 47
#define OPC_STORE_0 48
#define OPC_STORE_1 49
#define OPC_STORE_2 50
#define OPC_STORE_3 51
#define OPC_STORE_4 52
#define OPC_STORE_5 53
#define OPC_STORE_6 54
#define OPC_STORE_7 55
#define OPC_STORE_8 56
#define OPC_STORE_9 57
#define OPC_STORE_10 58
#define OPC_STORE_11 59
#define OPC_STORE_12 60
#define OPC_STORE_13 61
#define OPC_STORE_14 62
#define OPC_STORE_15 63
#define OPC_LOADPARM_0 64
#define OPC_LOADPARM_1 65
#define OPC_LOADPARM_2 66
#define OPC_LOADPARM_3 67
#define OPC_LOADPARM_4 68
#define OPC_LOADPARM_5 69
#define OPC_LOADPARM_6 70
#define OPC_LOADPARM_7 71
#define OPC_WIDE_M1 72
#define OPC_WIDE_0 73
#define OPC_WIDE_1 74
#define OPC_WIDE_SHORT 75
#define OPC_WIDE_INT 76
#define OPC_ESCAPE 77
#define OPC_ESCAPE_WIDE_M1 78
#define OPC_ESCAPE_WIDE_0 79
#define OPC_ESCAPE_WIDE_1 80
#define OPC_ESCAPE_WIDE_SHORT 81
#define OPC_ESCAPE_WIDE_INT 82
#define OPC_CATCH 83
#define OPC_CONST_NULL 84
#define OPC_CONST_M1 85
#define OPC_CONST_BYTE 86
#define OPC_CONST_SHORT 87
#define OPC_CONST_CHAR 88
#define OPC_CONST_INT 89
#define OPC_CONST_LONG 90
#define OPC_OBJECT 91
#define OPC_LOAD 92
#define OPC_LOAD_I2 93
#define OPC_STORE 94
#define OPC_STORE_I2 95
#define OPC_LOADPARM 96
#define OPC_LOADPARM_I2 97
#define OPC_STOREPARM 98
#define OPC_STOREPARM_I2 99
#define OPC_INC 100
#define OPC_DEC 101
#define OPC_INCPARM 102
#define OPC_DECPARM 103
#define OPC_GOTO 104
#define OPC_IF_EQ_O 105
#define OPC_IF_NE_O 106
#define OPC_IF_CMPEQ_O 107
#define OPC_IF_CMPNE_O 108
#define OPC_IF_EQ_I 109
#define OPC_IF_NE_I 110
#define OPC_IF_LT_I 111
#define OPC_IF_LE_I 112
#define OPC_IF_GT_I 113
#define OPC_IF_GE_I 114
#define OPC_IF_CMPEQ_I 115
#define OPC_IF_CMPNE_I 116
#define OPC_IF_CMPLT_I 117
#define OPC_IF_CMPLE_I 118
#define OPC_IF_CMPGT_I 119
#define OPC_IF_CMPGE_I 120
#define OPC_IF_EQ_L 121
#define OPC_IF_NE_L 122
#define OPC_IF_LT_L 123
#define OPC_IF_LE_L 124
#define OPC_IF_GT_L 125
#define OPC_IF_GE_L 126
#define OPC_IF_CMPEQ_L 127
#define OPC_IF_CMPNE_L 128
#define OPC_IF_CMPLT_L 129
#define OPC_IF_CMPLE_L 130
#define OPC_IF_CMPGT_L 131
#define OPC_IF_CMPGE_L 132
#define OPC_GETSTATIC_I 133
#define OPC_GETSTATIC_O 134
#define OPC_GETSTATIC_L 135
#define OPC_CLASS_GETSTATIC_I 136
#define OPC_CLASS_GETSTATIC_O 137
#define OPC_CLASS_GETSTATIC_L 138
#define OPC_PUTSTATIC_I 139
#define OPC_PUTSTATIC_O 140
#define OPC_PUTSTATIC_L 141
#define OPC_CLASS_PUTSTATIC_I 142
#define OPC_CLASS_PUTSTATIC_O 143
#define OPC_CLASS_PUTSTATIC_L 144
#define OPC_GETFIELD_I 145
#define OPC_GETFIELD_B 146
#define OPC_GETFIELD_S 147
#define OPC_GETFIELD_C 148
#define OPC_GETFIELD_O 149
#define OPC_GETFIELD_L 150
#define OPC_GETFIELD0_I 151
#define OPC_GETFIELD0_B 152
#define OPC_GETFIELD0_S 153
#define OPC_GETFIELD0_C 154
#define OPC_GETFIELD0_O 155
#define OPC_GETFIELD0_L 156
#define OPC_PUTFIELD_I 157
#define OPC_PUTFIELD_B 158
#define OPC_PUTFIELD_S 159
#define OPC_PUTFIELD_O 160
#define OPC_PUTFIELD_L 161
#define OPC_PUTFIELD0_I 162
#define OPC_PUTFIELD0_B 163
#define OPC_PUTFIELD0_S 164
#define OPC_PUTFIELD0_O 165
#define OPC_PUTFIELD0_L 166
#define OPC_INVOKEVIRTUAL_I 167
#define OPC_INVOKEVIRTUAL_V 168
#define OPC_INVOKEVIRTUAL_L 169
#define OPC_INVOKEVIRTUAL_O 170
#define OPC_INVOKESTATIC_I 171
#define OPC_INVOKESTATIC_V 172
#define OPC_INVOKESTATIC_L 173
#define OPC_INVOKESTATIC_O 174
#define OPC_INVOKESUPER_I 175
#define OPC_INVOKESUPER_V 176
#define OPC_INVOKESUPER_L 177
#define OPC_INVOKESUPER_O 178
#define OPC_INVOKENATIVE_I 179
#define OPC_INVOKENATIVE_V 180
#define OPC_INVOKENATIVE_L 181
#define OPC_INVOKENATIVE_O 182
#define OPC_FINDSLOT 183
#define OPC_EXTEND 184
#define OPC_INVOKESLOT_I 185
#define OPC_INVOKESLOT_V 186
#define OPC_INVOKESLOT_L 187
#define OPC_INVOKESLOT_O 188
#define OPC_RETURN_V 189
#define OPC_RETURN_I 190
#define OPC_RETURN_L 191
#define OPC_RETURN_O 192
#define OPC_TABLESWITCH_I 193
#define OPC_TABLESWITCH_S 194
#define OPC_EXTEND0 195
#define OPC_ADD_I 196
#define OPC_SUB_I 197
#define OPC_AND_I 198
#define OPC_OR_I 199
#define OPC_XOR_I 200
#define OPC_SHL_I 201
#define OPC_SHR_I 202
#define OPC_USHR_I 203
#define OPC_MUL_I 204
#define OPC_DIV_I 205
#define OPC_REM_I 206
#define OPC_NEG_I 207
#define OPC_I2B 208
#define OPC_I2S 209
#define OPC_I2C 210
#define OPC_ADD_L 211
#define OPC_SUB_L 212
#define OPC_MUL_L 213
#define OPC_DIV_L 214
#define OPC_REM_L 215
#define OPC_AND_L 216
#define OPC_OR_L 217
#define OPC_XOR_L 218
#define OPC_NEG_L 219
#define OPC_SHL_L 220
#define OPC_SHR_L 221
#define OPC_USHR_L 222
#define OPC_L2I 223
#define OPC_I2L 224
#define OPC_THROW 225
#define OPC_POP_1 226
#define OPC_POP_2 227
#define OPC_MONITORENTER 228
#define OPC_MONITOREXIT 229
#define OPC_CLASS_MONITORENTER 230
#define OPC_CLASS_MONITOREXIT 231
#define OPC_ARRAYLENGTH 232
#define OPC_NEW 233
#define OPC_NEWARRAY 234
#define OPC_NEWDIMENSION 235
#define OPC_CLASS_CLINIT 236
#define OPC_BBTARGET_SYS 237
#define OPC_BBTARGET_APP 238
#define OPC_INSTANCEOF 239
#define OPC_CHECKCAST 240
#define OPC_ALOAD_I 241
#define OPC_ALOAD_B 242
#define OPC_ALOAD_S 243
#define OPC_ALOAD_C 244
#define OPC_ALOAD_O 245
#define OPC_ALOAD_L 246
#define OPC_ASTORE_I 247
#define OPC_ASTORE_B 248
#define OPC_ASTORE_S 249
#define OPC_ASTORE_O 250
#define OPC_ASTORE_L 251
#define OPC_LOOKUP_I 252
#define OPC_LOOKUP_B 253
#define OPC_LOOKUP_S 254
#define OPC_PAUSE 255
#define OPC_OBJECT_WIDE 256
#define OPC_LOAD_WIDE 257
#define OPC_LOAD_I2_WIDE 258
#define OPC_STORE_WIDE 259
#define OPC_STORE_I2_WIDE 260
#define OPC_LOADPARM_WIDE 261
#define OPC_LOADPARM_I2_WIDE 262
#define OPC_STOREPARM_WIDE 263
#define OPC_STOREPARM_I2_WIDE 264
#define OPC_INC_WIDE 265
#define OPC_DEC_WIDE 266
#define OPC_INCPARM_WIDE 267
#define OPC_DECPARM_WIDE 268
#define OPC_GOTO_WIDE 269
#define OPC_IF_EQ_O_WIDE 270
#define OPC_IF_NE_O_WIDE 271
#define OPC_IF_CMPEQ_O_WIDE 272
#define OPC_IF_CMPNE_O_WIDE 273
#define OPC_IF_EQ_I_WIDE 274
#define OPC_IF_NE_I_WIDE 275
#define OPC_IF_LT_I_WIDE 276
#define OPC_IF_LE_I_WIDE 277
#define OPC_IF_GT_I_WIDE 278
#define OPC_IF_GE_I_WIDE 279
#define OPC_IF_CMPEQ_I_WIDE 280
#define OPC_IF_CMPNE_I_WIDE 281
#define OPC_IF_CMPLT_I_WIDE 282
#define OPC_IF_CMPLE_I_WIDE 283
#define OPC_IF_CMPGT_I_WIDE 284
#define OPC_IF_CMPGE_I_WIDE 285
#define OPC_IF_EQ_L_WIDE 286
#define OPC_IF_NE_L_WIDE 287
#define OPC_IF_LT_L_WIDE 288
#define OPC_IF_LE_L_WIDE 289
#define OPC_IF_GT_L_WIDE 290
#define OPC_IF_GE_L_WIDE 291
#define OPC_IF_CMPEQ_L_WIDE 292
#define OPC_IF_CMPNE_L_WIDE 293
#define OPC_IF_CMPLT_L_WIDE 294
#define OPC_IF_CMPLE_L_WIDE 295
#define OPC_IF_CMPGT_L_WIDE 296
#define OPC_IF_CMPGE_L_WIDE 297
#define OPC_GETSTATIC_I_WIDE 298
#define OPC_GETSTATIC_O_WIDE 299
#define OPC_GETSTATIC_L_WIDE 300
#define OPC_CLASS_GETSTATIC_I_WIDE 301
#define OPC_CLASS_GETSTATIC_O_WIDE 302
#define OPC_CLASS_GETSTATIC_L_WIDE 303
#define OPC_PUTSTATIC_I_WIDE 304
#define OPC_PUTSTATIC_O_WIDE 305
#define OPC_PUTSTATIC_L_WIDE 306
#define OPC_CLASS_PUTSTATIC_I_WIDE 307
#define OPC_CLASS_PUTSTATIC_O_WIDE 308
#define OPC_CLASS_PUTSTATIC_L_WIDE 309
#define OPC_GETFIELD_I_WIDE 310
#define OPC_GETFIELD_B_WIDE 311
#define OPC_GETFIELD_S_WIDE 312
#define OPC_GETFIELD_C_WIDE 313
#define OPC_GETFIELD_O_WIDE 314
#define OPC_GETFIELD_L_WIDE 315
#define OPC_GETFIELD0_I_WIDE 316
#define OPC_GETFIELD0_B_WIDE 317
#define OPC_GETFIELD0_S_WIDE 318
#define OPC_GETFIELD0_C_WIDE 319
#define OPC_GETFIELD0_O_WIDE 320
#define OPC_GETFIELD0_L_WIDE 321
#define OPC_PUTFIELD_I_WIDE 322
#define OPC_PUTFIELD_B_WIDE 323
#define OPC_PUTFIELD_S_WIDE 324
#define OPC_PUTFIELD_O_WIDE 325
#define OPC_PUTFIELD_L_WIDE 326
#define OPC_PUTFIELD0_I_WIDE 327
#define OPC_PUTFIELD0_B_WIDE 328
#define OPC_PUTFIELD0_S_WIDE 329
#define OPC_PUTFIELD0_O_WIDE 330
#define OPC_PUTFIELD0_L_WIDE 331
#define OPC_INVOKEVIRTUAL_I_WIDE 332
#define OPC_INVOKEVIRTUAL_V_WIDE 333
#define OPC_INVOKEVIRTUAL_L_WIDE 334
#define OPC_INVOKEVIRTUAL_O_WIDE 335
#define OPC_INVOKESTATIC_I_WIDE 336
#define OPC_INVOKESTATIC_V_WIDE 337
#define OPC_INVOKESTATIC_L_WIDE 338
#define OPC_INVOKESTATIC_O_WIDE 339
#define OPC_INVOKESUPER_I_WIDE 340
#define OPC_INVOKESUPER_V_WIDE 341
#define OPC_INVOKESUPER_L_WIDE 342
#define OPC_INVOKESUPER_O_WIDE 343
#define OPC_INVOKENATIVE_I_WIDE 344
#define OPC_INVOKENATIVE_V_WIDE 345
#define OPC_INVOKENATIVE_L_WIDE 346
#define OPC_INVOKENATIVE_O_WIDE 347
#define OPC_FINDSLOT_WIDE 348
#define OPC_EXTEND_WIDE 349
#define OPC_FCMPL 350
#define OPC_FCMPG 351
#define OPC_DCMPL 352
#define OPC_DCMPG 353
#define OPC_GETSTATIC_F 354
#define OPC_GETSTATIC_D 355
#define OPC_CLASS_GETSTATIC_F 356
#define OPC_CLASS_GETSTATIC_D 357
#define OPC_PUTSTATIC_F 358
#define OPC_PUTSTATIC_D 359
#define OPC_CLASS_PUTSTATIC_F 360
#define OPC_CLASS_PUTSTATIC_D 361
#define OPC_GETFIELD_F 362
#define OPC_GETFIELD_D 363
#define OPC_GETFIELD0_F 364
#define OPC_GETFIELD0_D 365
#define OPC_PUTFIELD_F 366
#define OPC_PUTFIELD_D 367
#define OPC_PUTFIELD0_F 368
#define OPC_PUTFIELD0_D 369
#define OPC_INVOKEVIRTUAL_F 370
#define OPC_INVOKEVIRTUAL_D 371
#define OPC_INVOKESTATIC_F 372
#define OPC_INVOKESTATIC_D 373
#define OPC_INVOKESUPER_F 374
#define OPC_INVOKESUPER_D 375
#define OPC_INVOKENATIVE_F 376
#define OPC_INVOKENATIVE_D 377
#define OPC_INVOKESLOT_F 378
#define OPC_INVOKESLOT_D 379
#define OPC_RETURN_F 380
#define OPC_RETURN_D 381
#define OPC_CONST_FLOAT 382
#define OPC_CONST_DOUBLE 383
#define OPC_ADD_F 384
#define OPC_SUB_F 385
#define OPC_MUL_F 386
#define OPC_DIV_F 387
#define OPC_REM_F 388
#define OPC_NEG_F 389
#define OPC_ADD_D 390
#define OPC_SUB_D 391
#define OPC_MUL_D 392
#define OPC_DIV_D 393
#define OPC_REM_D 394
#define OPC_NEG_D 395
#define OPC_I2F 396
#define OPC_L2F 397
#define OPC_F2I 398
#define OPC_F2L 399
#define OPC_I2D 400
#define OPC_L2D 401
#define OPC_F2D 402
#define OPC_D2I 403
#define OPC_D2L 404
#define OPC_D2F 405
#define OPC_ALOAD_F 406
#define OPC_ALOAD_D 407
#define OPC_ASTORE_F 408
#define OPC_ASTORE_D 409
#define OPC_GETSTATIC_F_WIDE 410
#define OPC_GETSTATIC_D_WIDE 411
#define OPC_CLASS_GETSTATIC_F_WIDE 412
#define OPC_CLASS_GETSTATIC_D_WIDE 413
#define OPC_PUTSTATIC_F_WIDE 414
#define OPC_PUTSTATIC_D_WIDE 415
#define OPC_CLASS_PUTSTATIC_F_WIDE 416
#define OPC_CLASS_PUTSTATIC_D_WIDE 417
#define OPC_GETFIELD_F_WIDE 418
#define OPC_GETFIELD_D_WIDE 419
#define OPC_GETFIELD0_F_WIDE 420
#define OPC_GETFIELD0_D_WIDE 421
#define OPC_PUTFIELD_F_WIDE 422
#define OPC_PUTFIELD_D_WIDE 423
#define OPC_PUTFIELD0_F_WIDE 424
#define OPC_PUTFIELD0_D_WIDE 425
#define OPC_INVOKEVIRTUAL_F_WIDE 426
#define OPC_INVOKEVIRTUAL_D_WIDE 427
#define OPC_INVOKESTATIC_F_WIDE 428
#define OPC_INVOKESTATIC_D_WIDE 429
#define OPC_INVOKESUPER_F_WIDE 430
#define OPC_INVOKESUPER_D_WIDE 431
#define OPC_INVOKENATIVE_F_WIDE 432
#define OPC_INVOKENATIVE_D_WIDE 433
#define com_sun_squawk_vm_OPC_Properties 274
#define OPC_Properties_NON_FLOAT_BYTECODE_COUNT 350
#define OPC_Properties_FLOAT_BYTECODE_COUNT 84
#define OPC_Properties_BYTECODE_COUNT 434
#define OPC_Properties_WIDE_DELTA 165
#define OPC_Properties_ESCAPE_WIDE_DELTA 56
#define com_sun_squawk_vm_SC 275
#define SC_next 0
#define SC_owner 1
#define SC_lastFP 2
#define SC_lastBCI 3
#define SC_guard 4
#define SC_limit 5
#define java_lang_ArithmeticException 276
#define java_lang_ArrayIndexOutOfBoundsException 277
#define java_lang_ArrayStoreException 278
#define java_lang_AssertionError 279
#define java_lang_Boolean 280
#define java_lang_Boolean_value(oop) getByte((oop), 0)
#define set_java_lang_Boolean_value(oop, value) setByte((oop), 0, value)
#define java_lang_Byte 281
#define java_lang_Byte_value(oop) getByte((oop), 0)
#define set_java_lang_Byte_value(oop, value) setByte((oop), 0, value)
#define java_lang_Byte_MIN_VALUE -128
#define java_lang_Byte_MAX_VALUE 127
#define java_lang_Character 282
#define java_lang_Character_value(oop) getUShort((oop), 0)
#define set_java_lang_Character_value(oop, value) setShort((oop), 0, value)
#define java_lang_Character_MIN_RADIX 2
#define java_lang_Character_MAX_RADIX 36
#define java_lang_Character_MIN_VALUE 0
#define java_lang_Character_MAX_VALUE 65535
#define java_lang_ClassCastException 283
#define java_lang_Double 284
#define java_lang_Double_value(oop) getLongAtWord((oop), 0)
#define set_java_lang_Double_value(oop, value) setLongAtWord((oop), 0, value)
#define java_lang_Double_POSITIVE_INFINITY Infinity
#define java_lang_Double_NEGATIVE_INFINITY -Infinity
#define java_lang_Double_NaN NaN
#define java_lang_Double_MAX_VALUE 1.7976931348623157E308
#define java_lang_Double_MIN_VALUE 4.9E-324
#define java_lang_Enum 285
#define java_lang_Enum_ordinal_(oop) getInt((oop), 0)
#define java_lang_Enum_name_(oop) getObject((oop), 1)
#define set_java_lang_Enum_ordinal_(oop, value) setInt((oop), 0, value)
#define set_java_lang_Enum_name_(oop, value) setObject((oop), 1, value)
#define java_lang_Error 286
#define java_lang_Exception 287
#define java_lang_FDBigInt 288
#define java_lang_FDBigInt_nWords(oop) getInt((oop), 0)
#define java_lang_FDBigInt_data(oop) getObject((oop), 1)
#define set_java_lang_FDBigInt_nWords(oop, value) setInt((oop), 0, value)
#define set_java_lang_FDBigInt_data(oop, value) setObject((oop), 1, value)
#define java_lang_Float 289
#define java_lang_Float_value(oop) getInt((oop), 0)
#define set_java_lang_Float_value(oop, value) setInt((oop), 0, value)
#define java_lang_Float_POSITIVE_INFINITY Infinity
#define java_lang_Float_NEGATIVE_INFINITY -Infinity
#define java_lang_Float_NaN NaN
#define java_lang_Float_MAX_VALUE 3.4028235E38
#define java_lang_Float_MIN_VALUE 1.4E-45
#define java_lang_FloatingDecimal 290
#define java_lang_FloatingDecimal_decExponent(oop) getInt((oop), 0)
#define java_lang_FloatingDecimal_digits(oop) getObject((oop), 1)
#define java_lang_FloatingDecimal_nDigits(oop) getInt((oop), 2)
#define java_lang_FloatingDecimal_bigIntExp(oop) getInt((oop), 3)
#define java_lang_FloatingDecimal_bigIntNBits(oop) getInt((oop), 4)
#define java_lang_FloatingDecimal_roundDir(oop) getInt((oop), 5)
#define java_lang_FloatingDecimal_isExceptional(oop) getByte((oop), 24)
#define java_lang_FloatingDecimal_isNegative(oop) getByte((oop), 25)
#define java_lang_FloatingDecimal_mustSetRoundDir(oop) getByte((oop), 26)
#define set_java_lang_FloatingDecimal_decExponent(oop, value) setInt((oop), 0, value)
#define set_java_lang_FloatingDecimal_digits(oop, value) setObject((oop), 1, value)
#define set_java_lang_FloatingDecimal_nDigits(oop, value) setInt((oop), 2, value)
#define set_java_lang_FloatingDecimal_bigIntExp(oop, value) setInt((oop), 3, value)
#define set_java_lang_FloatingDecimal_bigIntNBits(oop, value) setInt((oop), 4, value)
#define set_java_lang_FloatingDecimal_roundDir(oop, value) setInt((oop), 5, value)
#define set_java_lang_FloatingDecimal_isExceptional(oop, value) setByte((oop), 24, value)
#define set_java_lang_FloatingDecimal_isNegative(oop, value) setByte((oop), 25, value)
#define set_java_lang_FloatingDecimal_mustSetRoundDir(oop, value) setByte((oop), 26, value)
#define java_lang_FloatingDecimal_signMask JLONG_CONSTANT(-9223372036854775808)
#define java_lang_FloatingDecimal_expMask JLONG_CONSTANT(9218868437227405312)
#define java_lang_FloatingDecimal_fractMask JLONG_CONSTANT(4503599627370495)
#define java_lang_FloatingDecimal_expShift 52
#define java_lang_FloatingDecimal_expBias 1023
#define java_lang_FloatingDecimal_fractHOB JLONG_CONSTANT(4503599627370496)
#define java_lang_FloatingDecimal_expOne JLONG_CONSTANT(4607182418800017408)
#define java_lang_FloatingDecimal_maxSmallBinExp 62
#define java_lang_FloatingDecimal_minSmallBinExp -21
#define java_lang_FloatingDecimal_maxDecimalDigits 15
#define java_lang_FloatingDecimal_maxDecimalExponent 308
#define java_lang_FloatingDecimal_minDecimalExponent -324
#define java_lang_FloatingDecimal_bigDecimalExponent 324
#define java_lang_FloatingDecimal_highbyte JLONG_CONSTANT(-72057594037927936)
#define java_lang_FloatingDecimal_highbit JLONG_CONSTANT(-9223372036854775808)
#define java_lang_FloatingDecimal_lowbytes JLONG_CONSTANT(72057594037927935)
#define java_lang_FloatingDecimal_singleSignMask -2147483648
#define java_lang_FloatingDecimal_singleExpMask 2139095040
#define java_lang_FloatingDecimal_singleFractMask 8388607
#define java_lang_FloatingDecimal_singleExpShift 23
#define java_lang_FloatingDecimal_singleFractHOB 8388608
#define java_lang_FloatingDecimal_singleExpBias 127
#define java_lang_FloatingDecimal_singleMaxDecimalDigits 7
#define java_lang_FloatingDecimal_singleMaxDecimalExponent 38
#define java_lang_FloatingDecimal_singleMinDecimalExponent -45
#define java_lang_FloatingDecimal_intDecimalDigits 9
#define java_lang_IllegalAccessException 291
#define java_lang_IllegalArgumentException 292
#define java_lang_IllegalMonitorStateException 293
#define java_lang_IllegalStateException 294
#define java_lang_IllegalThreadStateException 295
#define java_lang_InstantiationException 296
#define java_lang_Integer 297
#define java_lang_Integer_value(oop) getInt((oop), 0)
#define set_java_lang_Integer_value(oop, value) setInt((oop), 0, value)
#define java_lang_Integer_MIN_VALUE -2147483648
#define java_lang_Integer_MAX_VALUE 2147483647
#define java_lang_Iterable 298
#define java_lang_Long 299
#define java_lang_Long_value(oop) getLongAtWord((oop), 0)
#define set_java_lang_Long_value(oop, value) setLongAtWord((oop), 0, value)
#define java_lang_Long_MIN_VALUE JLONG_CONSTANT(-9223372036854775808)
#define java_lang_Long_MAX_VALUE JLONG_CONSTANT(9223372036854775807)
#define java_lang_Math 300
#define java_lang_Math_E 2.718281828459045
#define java_lang_Math_PI 3.141592653589793
#define java_lang_Math_negativeZeroFloatBits JLONG_CONSTANT(-2147483648)
#define java_lang_Math_negativeZeroDoubleBits JLONG_CONSTANT(-9223372036854775808)
#define java_lang_NegativeArraySizeException 301
#define java_lang_NoClassDefFoundError 302
#define java_lang_NullPointerException 303
#define java_lang_NumberFormatException 304
#define java_lang_Override 305
#define java_lang_Runnable 306
#define java_lang_Runtime 307
#define java_lang_SecurityException 308
#define java_lang_Short 309
#define java_lang_Short_value(oop) getShort((oop), 0)
#define set_java_lang_Short_value(oop, value) setShort((oop), 0, value)
#define java_lang_Short_MIN_VALUE -32768
#define java_lang_Short_MAX_VALUE 32767
#define java_lang_StringIndexOutOfBoundsException 310
#define java_lang_SuppressWarnings 311
#define java_lang_System 312
#define java_lang_Thread 313
#define java_lang_Thread_vmThread(oop) getObject((oop), 0)
#define java_lang_Thread_target(oop) getObject((oop), 1)
#define set_java_lang_Thread_vmThread(oop, value) setObject((oop), 0, value)
#define set_java_lang_Thread_target(oop, value) setObject((oop), 1, value)
#define java_lang_Thread_MIN_PRIORITY 1
#define java_lang_Thread_NORM_PRIORITY 5
#define java_lang_Thread_MAX_PRIORITY 10
#define java_lang_VirtualMachineError 314
#define java_lang_Void 315
#define java_lang_annotation_Annotation 316
#define java_lang_annotation_Documented 317
#define java_lang_annotation_ElementType 318
#define java_lang_annotation_ElementType_serialVersionUID JLONG_CONSTANT(0)
#define java_lang_annotation_Inherited 319
#define java_lang_annotation_Retention 320
#define java_lang_annotation_RetentionPolicy 321
#define java_lang_annotation_RetentionPolicy_serialVersionUID JLONG_CONSTANT(0)
#define java_lang_annotation_Target 322
#define java_lang_ref_Reference 323
#define java_lang_ref_Reference_ref(oop) getObject((oop), 0)
#define set_java_lang_ref_Reference_ref(oop, value) setObject((oop), 0, value)
#define java_lang_ref_WeakReference 324
#define com_sun_squawk_microedition_io_FileConnection 466
#define com_sun_squawk_platform_Platform 479
#define com_sun_squawk_platform_Platform_DEBUG false
#define com_sun_squawk_platform_Platform_BARE_METAL 0
#define com_sun_squawk_platform_Platform_DELEGATING 1
#define com_sun_squawk_platform_Platform_NATIVE 2
#define com_sun_squawk_platform_Platform_SOCKET 3
#define com_sun_squawk_platform_Platform_IS_BARE_METAL true
#define com_sun_squawk_platform_Platform_IS_DELEGATING false
#define com_sun_squawk_platform_Platform_IS_NATIVE false
#define com_sun_squawk_platform_Platform_IS_SOCKET false
#define com_sun_squawk_platform_GCFFile 480
const char *AddressType_Mnemonics = "-ZbBSIFLlDdRU";
#if TRACE
char *getOpcodeName(int code) {
    switch(code) {
        case 0: return "const_0";
        case 1: return "const_1";
        case 2: return "const_2";
        case 3: return "const_3";
        case 4: return "const_4";
        case 5: return "const_5";
        case 6: return "const_6";
        case 7: return "const_7";
        case 8: return "const_8";
        case 9: return "const_9";
        case 10: return "const_10";
        case 11: return "const_11";
        case 12: return "const_12";
        case 13: return "const_13";
        case 14: return "const_14";
        case 15: return "const_15";
        case 16: return "object_0";
        case 17: return "object_1";
        case 18: return "object_2";
        case 19: return "object_3";
        case 20: return "object_4";
        case 21: return "object_5";
        case 22: return "object_6";
        case 23: return "object_7";
        case 24: return "object_8";
        case 25: return "object_9";
        case 26: return "object_10";
        case 27: return "object_11";
        case 28: return "object_12";
        case 29: return "object_13";
        case 30: return "object_14";
        case 31: return "object_15";
        case 32: return "load_0";
        case 33: return "load_1";
        case 34: return "load_2";
        case 35: return "load_3";
        case 36: return "load_4";
        case 37: return "load_5";
        case 38: return "load_6";
        case 39: return "load_7";
        case 40: return "load_8";
        case 41: return "load_9";
        case 42: return "load_10";
        case 43: return "load_11";
        case 44: return "load_12";
        case 45: return "load_13";
        case 46: return "load_14";
        case 47: return "load_15";
        case 48: return "store_0";
        case 49: return "store_1";
        case 50: return "store_2";
        case 51: return "store_3";
        case 52: return "store_4";
        case 53: return "store_5";
        case 54: return "store_6";
        case 55: return "store_7";
        case 56: return "store_8";
        case 57: return "store_9";
        case 58: return "store_10";
        case 59: return "store_11";
        case 60: return "store_12";
        case 61: return "store_13";
        case 62: return "store_14";
        case 63: return "store_15";
        case 64: return "loadparm_0";
        case 65: return "loadparm_1";
        case 66: return "loadparm_2";
        case 67: return "loadparm_3";
        case 68: return "loadparm_4";
        case 69: return "loadparm_5";
        case 70: return "loadparm_6";
        case 71: return "loadparm_7";
        case 72: return "wide_m1";
        case 73: return "wide_0";
        case 74: return "wide_1";
        case 75: return "wide_short";
        case 76: return "wide_int";
        case 77: return "escape";
        case 78: return "escape_wide_m1";
        case 79: return "escape_wide_0";
        case 80: return "escape_wide_1";
        case 81: return "escape_wide_short";
        case 82: return "escape_wide_int";
        case 83: return "catch";
        case 84: return "const_null";
        case 85: return "const_m1";
        case 86: return "const_byte";
        case 87: return "const_short";
        case 88: return "const_char";
        case 89: return "const_int";
        case 90: return "const_long";
        case 91: return "object";
        case 92: return "load";
        case 93: return "load_i2";
        case 94: return "store";
        case 95: return "store_i2";
        case 96: return "loadparm";
        case 97: return "loadparm_i2";
        case 98: return "storeparm";
        case 99: return "storeparm_i2";
        case 100: return "inc";
        case 101: return "dec";
        case 102: return "incparm";
        case 103: return "decparm";
        case 104: return "goto";
        case 105: return "if_eq_o";
        case 106: return "if_ne_o";
        case 107: return "if_cmpeq_o";
        case 108: return "if_cmpne_o";
        case 109: return "if_eq_i";
        case 110: return "if_ne_i";
        case 111: return "if_lt_i";
        case 112: return "if_le_i";
        case 113: return "if_gt_i";
        case 114: return "if_ge_i";
        case 115: return "if_cmpeq_i";
        case 116: return "if_cmpne_i";
        case 117: return "if_cmplt_i";
        case 118: return "if_cmple_i";
        case 119: return "if_cmpgt_i";
        case 120: return "if_cmpge_i";
        case 121: return "if_eq_l";
        case 122: return "if_ne_l";
        case 123: return "if_lt_l";
        case 124: return "if_le_l";
        case 125: return "if_gt_l";
        case 126: return "if_ge_l";
        case 127: return "if_cmpeq_l";
        case 128: return "if_cmpne_l";
        case 129: return "if_cmplt_l";
        case 130: return "if_cmple_l";
        case 131: return "if_cmpgt_l";
        case 132: return "if_cmpge_l";
        case 133: return "getstatic_i";
        case 134: return "getstatic_o";
        case 135: return "getstatic_l";
        case 136: return "class_getstatic_i";
        case 137: return "class_getstatic_o";
        case 138: return "class_getstatic_l";
        case 139: return "putstatic_i";
        case 140: return "putstatic_o";
        case 141: return "putstatic_l";
        case 142: return "class_putstatic_i";
        case 143: return "class_putstatic_o";
        case 144: return "class_putstatic_l";
        case 145: return "getfield_i";
        case 146: return "getfield_b";
        case 147: return "getfield_s";
        case 148: return "getfield_c";
        case 149: return "getfield_o";
        case 150: return "getfield_l";
        case 151: return "getfield0_i";
        case 152: return "getfield0_b";
        case 153: return "getfield0_s";
        case 154: return "getfield0_c";
        case 155: return "getfield0_o";
        case 156: return "getfield0_l";
        case 157: return "putfield_i";
        case 158: return "putfield_b";
        case 159: return "putfield_s";
        case 160: return "putfield_o";
        case 161: return "putfield_l";
        case 162: return "putfield0_i";
        case 163: return "putfield0_b";
        case 164: return "putfield0_s";
        case 165: return "putfield0_o";
        case 166: return "putfield0_l";
        case 167: return "invokevirtual_i";
        case 168: return "invokevirtual_v";
        case 169: return "invokevirtual_l";
        case 170: return "invokevirtual_o";
        case 171: return "invokestatic_i";
        case 172: return "invokestatic_v";
        case 173: return "invokestatic_l";
        case 174: return "invokestatic_o";
        case 175: return "invokesuper_i";
        case 176: return "invokesuper_v";
        case 177: return "invokesuper_l";
        case 178: return "invokesuper_o";
        case 179: return "invokenative_i";
        case 180: return "invokenative_v";
        case 181: return "invokenative_l";
        case 182: return "invokenative_o";
        case 183: return "findslot";
        case 184: return "extend";
        case 185: return "invokeslot_i";
        case 186: return "invokeslot_v";
        case 187: return "invokeslot_l";
        case 188: return "invokeslot_o";
        case 189: return "return_v";
        case 190: return "return_i";
        case 191: return "return_l";
        case 192: return "return_o";
        case 193: return "tableswitch_i";
        case 194: return "tableswitch_s";
        case 195: return "extend0";
        case 196: return "add_i";
        case 197: return "sub_i";
        case 198: return "and_i";
        case 199: return "or_i";
        case 200: return "xor_i";
        case 201: return "shl_i";
        case 202: return "shr_i";
        case 203: return "ushr_i";
        case 204: return "mul_i";
        case 205: return "div_i";
        case 206: return "rem_i";
        case 207: return "neg_i";
        case 208: return "i2b";
        case 209: return "i2s";
        case 210: return "i2c";
        case 211: return "add_l";
        case 212: return "sub_l";
        case 213: return "mul_l";
        case 214: return "div_l";
        case 215: return "rem_l";
        case 216: return "and_l";
        case 217: return "or_l";
        case 218: return "xor_l";
        case 219: return "neg_l";
        case 220: return "shl_l";
        case 221: return "shr_l";
        case 222: return "ushr_l";
        case 223: return "l2i";
        case 224: return "i2l";
        case 225: return "throw";
        case 226: return "pop_1";
        case 227: return "pop_2";
        case 228: return "monitorenter";
        case 229: return "monitorexit";
        case 230: return "class_monitorenter";
        case 231: return "class_monitorexit";
        case 232: return "arraylength";
        case 233: return "new";
        case 234: return "newarray";
        case 235: return "newdimension";
        case 236: return "class_clinit";
        case 237: return "bbtarget_sys";
        case 238: return "bbtarget_app";
        case 239: return "instanceof";
        case 240: return "checkcast";
        case 241: return "aload_i";
        case 242: return "aload_b";
        case 243: return "aload_s";
        case 244: return "aload_c";
        case 245: return "aload_o";
        case 246: return "aload_l";
        case 247: return "astore_i";
        case 248: return "astore_b";
        case 249: return "astore_s";
        case 250: return "astore_o";
        case 251: return "astore_l";
        case 252: return "lookup_i";
        case 253: return "lookup_b";
        case 254: return "lookup_s";
        case 255: return "pause";
        case 256: return "object_wide";
        case 257: return "load_wide";
        case 258: return "load_i2_wide";
        case 259: return "store_wide";
        case 260: return "store_i2_wide";
        case 261: return "loadparm_wide";
        case 262: return "loadparm_i2_wide";
        case 263: return "storeparm_wide";
        case 264: return "storeparm_i2_wide";
        case 265: return "inc_wide";
        case 266: return "dec_wide";
        case 267: return "incparm_wide";
        case 268: return "decparm_wide";
        case 269: return "goto_wide";
        case 270: return "if_eq_o_wide";
        case 271: return "if_ne_o_wide";
        case 272: return "if_cmpeq_o_wide";
        case 273: return "if_cmpne_o_wide";
        case 274: return "if_eq_i_wide";
        case 275: return "if_ne_i_wide";
        case 276: return "if_lt_i_wide";
        case 277: return "if_le_i_wide";
        case 278: return "if_gt_i_wide";
        case 279: return "if_ge_i_wide";
        case 280: return "if_cmpeq_i_wide";
        case 281: return "if_cmpne_i_wide";
        case 282: return "if_cmplt_i_wide";
        case 283: return "if_cmple_i_wide";
        case 284: return "if_cmpgt_i_wide";
        case 285: return "if_cmpge_i_wide";
        case 286: return "if_eq_l_wide";
        case 287: return "if_ne_l_wide";
        case 288: return "if_lt_l_wide";
        case 289: return "if_le_l_wide";
        case 290: return "if_gt_l_wide";
        case 291: return "if_ge_l_wide";
        case 292: return "if_cmpeq_l_wide";
        case 293: return "if_cmpne_l_wide";
        case 294: return "if_cmplt_l_wide";
        case 295: return "if_cmple_l_wide";
        case 296: return "if_cmpgt_l_wide";
        case 297: return "if_cmpge_l_wide";
        case 298: return "getstatic_i_wide";
        case 299: return "getstatic_o_wide";
        case 300: return "getstatic_l_wide";
        case 301: return "class_getstatic_i_wide";
        case 302: return "class_getstatic_o_wide";
        case 303: return "class_getstatic_l_wide";
        case 304: return "putstatic_i_wide";
        case 305: return "putstatic_o_wide";
        case 306: return "putstatic_l_wide";
        case 307: return "class_putstatic_i_wide";
        case 308: return "class_putstatic_o_wide";
        case 309: return "class_putstatic_l_wide";
        case 310: return "getfield_i_wide";
        case 311: return "getfield_b_wide";
        case 312: return "getfield_s_wide";
        case 313: return "getfield_c_wide";
        case 314: return "getfield_o_wide";
        case 315: return "getfield_l_wide";
        case 316: return "getfield0_i_wide";
        case 317: return "getfield0_b_wide";
        case 318: return "getfield0_s_wide";
        case 319: return "getfield0_c_wide";
        case 320: return "getfield0_o_wide";
        case 321: return "getfield0_l_wide";
        case 322: return "putfield_i_wide";
        case 323: return "putfield_b_wide";
        case 324: return "putfield_s_wide";
        case 325: return "putfield_o_wide";
        case 326: return "putfield_l_wide";
        case 327: return "putfield0_i_wide";
        case 328: return "putfield0_b_wide";
        case 329: return "putfield0_s_wide";
        case 330: return "putfield0_o_wide";
        case 331: return "putfield0_l_wide";
        case 332: return "invokevirtual_i_wide";
        case 333: return "invokevirtual_v_wide";
        case 334: return "invokevirtual_l_wide";
        case 335: return "invokevirtual_o_wide";
        case 336: return "invokestatic_i_wide";
        case 337: return "invokestatic_v_wide";
        case 338: return "invokestatic_l_wide";
        case 339: return "invokestatic_o_wide";
        case 340: return "invokesuper_i_wide";
        case 341: return "invokesuper_v_wide";
        case 342: return "invokesuper_l_wide";
        case 343: return "invokesuper_o_wide";
        case 344: return "invokenative_i_wide";
        case 345: return "invokenative_v_wide";
        case 346: return "invokenative_l_wide";
        case 347: return "invokenative_o_wide";
        case 348: return "findslot_wide";
        case 349: return "extend_wide";
        case 350: return "fcmpl";
        case 351: return "fcmpg";
        case 352: return "dcmpl";
        case 353: return "dcmpg";
        case 354: return "getstatic_f";
        case 355: return "getstatic_d";
        case 356: return "class_getstatic_f";
        case 357: return "class_getstatic_d";
        case 358: return "putstatic_f";
        case 359: return "putstatic_d";
        case 360: return "class_putstatic_f";
        case 361: return "class_putstatic_d";
        case 362: return "getfield_f";
        case 363: return "getfield_d";
        case 364: return "getfield0_f";
        case 365: return "getfield0_d";
        case 366: return "putfield_f";
        case 367: return "putfield_d";
        case 368: return "putfield0_f";
        case 369: return "putfield0_d";
        case 370: return "invokevirtual_f";
        case 371: return "invokevirtual_d";
        case 372: return "invokestatic_f";
        case 373: return "invokestatic_d";
        case 374: return "invokesuper_f";
        case 375: return "invokesuper_d";
        case 376: return "invokenative_f";
        case 377: return "invokenative_d";
        case 378: return "invokeslot_f";
        case 379: return "invokeslot_d";
        case 380: return "return_f";
        case 381: return "return_d";
        case 382: return "const_float";
        case 383: return "const_double";
        case 384: return "add_f";
        case 385: return "sub_f";
        case 386: return "mul_f";
        case 387: return "div_f";
        case 388: return "rem_f";
        case 389: return "neg_f";
        case 390: return "add_d";
        case 391: return "sub_d";
        case 392: return "mul_d";
        case 393: return "div_d";
        case 394: return "rem_d";
        case 395: return "neg_d";
        case 396: return "i2f";
        case 397: return "l2f";
        case 398: return "f2i";
        case 399: return "f2l";
        case 400: return "i2d";
        case 401: return "l2d";
        case 402: return "f2d";
        case 403: return "d2i";
        case 404: return "d2l";
        case 405: return "d2f";
        case 406: return "aload_f";
        case 407: return "aload_d";
        case 408: return "astore_f";
        case 409: return "astore_d";
        case 410: return "getstatic_f_wide";
        case 411: return "getstatic_d_wide";
        case 412: return "class_getstatic_f_wide";
        case 413: return "class_getstatic_d_wide";
        case 414: return "putstatic_f_wide";
        case 415: return "putstatic_d_wide";
        case 416: return "class_putstatic_f_wide";
        case 417: return "class_putstatic_d_wide";
        case 418: return "getfield_f_wide";
        case 419: return "getfield_d_wide";
        case 420: return "getfield0_f_wide";
        case 421: return "getfield0_d_wide";
        case 422: return "putfield_f_wide";
        case 423: return "putfield_d_wide";
        case 424: return "putfield0_f_wide";
        case 425: return "putfield0_d_wide";
        case 426: return "invokevirtual_f_wide";
        case 427: return "invokevirtual_d_wide";
        case 428: return "invokestatic_f_wide";
        case 429: return "invokestatic_d_wide";
        case 430: return "invokesuper_f_wide";
        case 431: return "invokesuper_d_wide";
        case 432: return "invokenative_f_wide";
        case 433: return "invokenative_d_wide";
        default: return "Unknown opcode";
    }
}
boolean opcodeHasWide(int code) {
    switch(code) {
        case OPC_OBJECT:
        case OPC_LOAD:
        case OPC_LOAD_I2:
        case OPC_STORE:
        case OPC_STORE_I2:
        case OPC_LOADPARM:
        case OPC_LOADPARM_I2:
        case OPC_STOREPARM:
        case OPC_STOREPARM_I2:
        case OPC_INC:
        case OPC_DEC:
        case OPC_INCPARM:
        case OPC_DECPARM:
        case OPC_GOTO:
        case OPC_IF_EQ_O:
        case OPC_IF_NE_O:
        case OPC_IF_CMPEQ_O:
        case OPC_IF_CMPNE_O:
        case OPC_IF_EQ_I:
        case OPC_IF_NE_I:
        case OPC_IF_LT_I:
        case OPC_IF_LE_I:
        case OPC_IF_GT_I:
        case OPC_IF_GE_I:
        case OPC_IF_CMPEQ_I:
        case OPC_IF_CMPNE_I:
        case OPC_IF_CMPLT_I:
        case OPC_IF_CMPLE_I:
        case OPC_IF_CMPGT_I:
        case OPC_IF_CMPGE_I:
        case OPC_IF_EQ_L:
        case OPC_IF_NE_L:
        case OPC_IF_LT_L:
        case OPC_IF_LE_L:
        case OPC_IF_GT_L:
        case OPC_IF_GE_L:
        case OPC_IF_CMPEQ_L:
        case OPC_IF_CMPNE_L:
        case OPC_IF_CMPLT_L:
        case OPC_IF_CMPLE_L:
        case OPC_IF_CMPGT_L:
        case OPC_IF_CMPGE_L:
        case OPC_GETSTATIC_I:
        case OPC_GETSTATIC_O:
        case OPC_GETSTATIC_L:
        case OPC_CLASS_GETSTATIC_I:
        case OPC_CLASS_GETSTATIC_O:
        case OPC_CLASS_GETSTATIC_L:
        case OPC_PUTSTATIC_I:
        case OPC_PUTSTATIC_O:
        case OPC_PUTSTATIC_L:
        case OPC_CLASS_PUTSTATIC_I:
        case OPC_CLASS_PUTSTATIC_O:
        case OPC_CLASS_PUTSTATIC_L:
        case OPC_GETFIELD_I:
        case OPC_GETFIELD_B:
        case OPC_GETFIELD_S:
        case OPC_GETFIELD_C:
        case OPC_GETFIELD_O:
        case OPC_GETFIELD_L:
        case OPC_GETFIELD0_I:
        case OPC_GETFIELD0_B:
        case OPC_GETFIELD0_S:
        case OPC_GETFIELD0_C:
        case OPC_GETFIELD0_O:
        case OPC_GETFIELD0_L:
        case OPC_PUTFIELD_I:
        case OPC_PUTFIELD_B:
        case OPC_PUTFIELD_S:
        case OPC_PUTFIELD_O:
        case OPC_PUTFIELD_L:
        case OPC_PUTFIELD0_I:
        case OPC_PUTFIELD0_B:
        case OPC_PUTFIELD0_S:
        case OPC_PUTFIELD0_O:
        case OPC_PUTFIELD0_L:
        case OPC_INVOKEVIRTUAL_I:
        case OPC_INVOKEVIRTUAL_V:
        case OPC_INVOKEVIRTUAL_L:
        case OPC_INVOKEVIRTUAL_O:
        case OPC_INVOKESTATIC_I:
        case OPC_INVOKESTATIC_V:
        case OPC_INVOKESTATIC_L:
        case OPC_INVOKESTATIC_O:
        case OPC_INVOKESUPER_I:
        case OPC_INVOKESUPER_V:
        case OPC_INVOKESUPER_L:
        case OPC_INVOKESUPER_O:
        case OPC_INVOKENATIVE_I:
        case OPC_INVOKENATIVE_V:
        case OPC_INVOKENATIVE_L:
        case OPC_INVOKENATIVE_O:
        case OPC_FINDSLOT:
        case OPC_EXTEND:
        case OPC_GETSTATIC_F:
        case OPC_GETSTATIC_D:
        case OPC_CLASS_GETSTATIC_F:
        case OPC_CLASS_GETSTATIC_D:
        case OPC_PUTSTATIC_F:
        case OPC_PUTSTATIC_D:
        case OPC_CLASS_PUTSTATIC_F:
        case OPC_CLASS_PUTSTATIC_D:
        case OPC_GETFIELD_F:
        case OPC_GETFIELD_D:
        case OPC_GETFIELD0_F:
        case OPC_GETFIELD0_D:
        case OPC_PUTFIELD_F:
        case OPC_PUTFIELD_D:
        case OPC_PUTFIELD0_F:
        case OPC_PUTFIELD0_D:
        case OPC_INVOKEVIRTUAL_F:
        case OPC_INVOKEVIRTUAL_D:
        case OPC_INVOKESTATIC_F:
        case OPC_INVOKESTATIC_D:
        case OPC_INVOKESUPER_F:
        case OPC_INVOKESUPER_D:
        case OPC_INVOKENATIVE_F:
        case OPC_INVOKENATIVE_D:
                 return true;
        default: return false;
    }
}
const char* getGlobalAddrName(int index) {
    switch(index) {
        case 21: return "com_sun_squawk_VM_argv";
        case 6: return "com_sun_squawk_GC_nvmStart";
        case 24: return "com_sun_squawk_Lisp2Bitmap_Iterator_end";
        case 19: return "com_sun_squawk_VM_bootstrapSuite";
        case 4: return "cheneyEndMemoryProtect";
        case 7: return "com_sun_squawk_GC_nvmEnd";
        case 16: return "com_sun_squawk_VM_romStart";
        case 15: return "com_sun_squawk_GC_allocEnd";
        case 2: return "com_sun_squawk_ServiceOperation_addressResult";
        case 25: return "com_sun_squawk_Lisp2Bitmap_Iterator_next";
        case 1: return "com_sun_squawk_ServiceOperation_o2";
        case 9: return "com_sun_squawk_GC_ramStart";
        case 8: return "com_sun_squawk_GC_nvmAllocationPointer";
        case 0: return "com_sun_squawk_ServiceOperation_o1";
        case 18: return "com_sun_squawk_VM_bootstrapStart";
        case 13: return "com_sun_squawk_GC_allocStart";
        case 3: return "cheneyStartMemoryProtect";
        case 22: return "com_sun_squawk_Lisp2Bitmap_start";
        case 23: return "com_sun_squawk_Lisp2Bitmap_base";
        case 11: return "com_sun_squawk_GC_heapStart";
        case 17: return "com_sun_squawk_VM_romEnd";
        case 14: return "com_sun_squawk_GC_allocTop";
        case 5: return "com_sun_squawk_VMThread_serviceStack";
        case 12: return "com_sun_squawk_GC_heapEnd";
        case 10: return "com_sun_squawk_GC_ramEnd";
        case 20: return "com_sun_squawk_VM_bootstrapEnd";
        default: return "getGlobalAddrName: unknown global index";
    }
}
const char* getGlobalOopName(int index) {
    switch(index) {
        case 6: return "com_sun_squawk_VMThread_timerQueue";
        case 2: return "com_sun_squawk_VMThread_otherThread";
        case 8: return "com_sun_squawk_VMThread_osevents";
        case 21: return "com_sun_squawk_VM_isolates";
        case 27: return "com_sun_squawk_util_Tracer_filter";
        case 23: return "com_sun_squawk_VM_keyedGlobalsMutex";
        case 12: return "com_sun_squawk_GC_heapstats";
        case 0: return "com_sun_squawk_VM_currentIsolate";
        case 10: return "com_sun_squawk_GC_collector";
        case 26: return "com_sun_squawk_util_Tracer__out";
        case 17: return "com_sun_squawk_VM_registeredMailboxes";
        case 3: return "com_sun_squawk_VMThread_serviceThread";
        case 14: return "com_sun_squawk_VM_outOfMemoryError";
        case 4: return "com_sun_squawk_ServiceOperation_pendingException";
        case 11: return "com_sun_squawk_GC_readOnlyObjectMemories";
        case 5: return "com_sun_squawk_VMThread_runnableThreads";
        case 15: return "com_sun_squawk_VM_vmbufferDecoder";
        case 25: return "com_sun_squawk_util_Tracer__features";
        case 22: return "com_sun_squawk_VM_commandLineProperties";
        case 1: return "com_sun_squawk_VMThread_currentThread";
        case 7: return "com_sun_squawk_VMThread_events";
        case 20: return "com_sun_squawk_VM_shutdownHooks";
        case 16: return "com_sun_squawk_VM_isolateInitializer";
        case 18: return "com_sun_squawk_VM_peripheralRegistry";
        case 13: return "com_sun_squawk_VM_pluginObjectMemories";
        case 24: return "com_sun_squawk_VM_keyeGlobals";
        case 9: return "com_sun_squawk_VMThread_systemEvents";
        case 19: return "com_sun_squawk_VM_reportedArray";
        default: return "getGlobalOopName: unknown global index";
    }
}
const char* getGlobalIntName(int index) {
    switch(index) {
        case 50: return "com_sun_squawk_VM_argc";
        case 19: return "com_sun_squawk_ServiceOperation_result";
        case 27: return "runningOnServiceThread";
        case 52: return "com_sun_squawk_VM_reportedIndex";
        case 51: return "com_sun_squawk_VM_isFirstIsolateInitialized";
        case 47: return "com_sun_squawk_VM_exceptionsEnabled";
        case 5: return "com_sun_squawk_GC_monitorReleaseCount";
        case 10: return "com_sun_squawk_ServiceOperation_context";
        case 7: return "com_sun_squawk_GC_newHits";
        case 48: return "com_sun_squawk_VM_nextHashcode";
        case 28: return "currentThreadID";
        case 11: return "com_sun_squawk_ServiceOperation_op";
        case 53: return "com_sun_squawk_VM_throwCount";
        case 29: return "com_sun_squawk_VMThread_contendedEnterCount";
        case 20: return "branchCountHigh";
        case 1: return "com_sun_squawk_VM_usingTypeMap";
        case 46: return "com_sun_squawk_VM_synchronizationEnabled";
        case 22: return "traceStartHigh";
        case 54: return "com_sun_squawk_VM_safePrintToVM";
        case 42: return "com_sun_squawk_GC_allocationEnabled";
        case 9: return "com_sun_squawk_ServiceOperation_code";
        case 59: return "com_sun_squawk_Lisp2Bitmap_size";
        case 26: return "com_sun_squawk_VM_tracing";
        case 6: return "com_sun_squawk_GC_newCount";
        case 44: return "com_sun_squawk_VM_bootstrapHash";
        case 43: return "com_sun_squawk_GC_gcEnabled";
        case 30: return "com_sun_squawk_VMThread_monitorsAllocatedCount";
        case 55: return "com_sun_squawk_VM_timeAdjustmentsLo";
        case 58: return "com_sun_squawk_VM_executingHooks";
        case 49: return "com_sun_squawk_VM_allowUserGC";
        case 35: return "com_sun_squawk_VMThread_systemThreadsOnly";
        case 39: return "com_sun_squawk_GC_partialCollectionCount";
        case 60: return "com_sun_squawk_Lisp2Bitmap_Iterator_inUse";
        case 2: return "com_sun_squawk_GC_traceFlags";
        case 23: return "traceStartLow";
        case 21: return "branchCountLow";
        case 38: return "com_sun_squawk_GC_fullCollectionCount";
        case 36: return "com_sun_squawk_VMThread_stacksAllocatedCount";
        case 33: return "com_sun_squawk_VMThread_waitTimeLo32";
        case 24: return "traceEndHigh";
        case 4: return "com_sun_squawk_GC_monitorExitCount";
        case 45: return "com_sun_squawk_VM_verboseLevel";
        case 32: return "com_sun_squawk_VMThread_waitTimeHi32";
        case 31: return "com_sun_squawk_VMThread_threadSwitchCount";
        case 0: return "com_sun_squawk_VM_extendsEnabled";
        case 41: return "com_sun_squawk_GC_traceThreshold";
        case 8: return "com_sun_squawk_VMThread_nextThreadNumber";
        case 34: return "com_sun_squawk_VMThread_max_wait";
        case 25: return "traceEndLow";
        case 18: return "com_sun_squawk_ServiceOperation_i6";
        case 56: return "com_sun_squawk_VM_timeAdjustmentsHi";
        case 17: return "com_sun_squawk_ServiceOperation_i5";
        case 12: return "com_sun_squawk_ServiceOperation_channel";
        case 16: return "com_sun_squawk_ServiceOperation_i4";
        case 37: return "com_sun_squawk_VMThread_maxStackSize";
        case 3: return "com_sun_squawk_GC_collecting";
        case 15: return "com_sun_squawk_ServiceOperation_i3";
        case 57: return "com_sun_squawk_VM_nextIsolateID";
        case 14: return "com_sun_squawk_ServiceOperation_i2";
        case 40: return "com_sun_squawk_GC_excessiveGC";
        case 13: return "com_sun_squawk_ServiceOperation_i1";
        default: return "getGlobalIntName: unknown global index";
    }
}
#endif
#define com_sun_squawk_VM_argc (Ints[50])
#define com_sun_squawk_ServiceOperation_result (Ints[19])
#define runningOnServiceThread (Ints[27])
#define com_sun_squawk_VM_reportedIndex (Ints[52])
#define com_sun_squawk_VM_isFirstIsolateInitialized (Ints[51])
#define com_sun_squawk_VM_exceptionsEnabled (Ints[47])
#define com_sun_squawk_GC_monitorReleaseCount (Ints[5])
#define com_sun_squawk_ServiceOperation_context (Ints[10])
#define com_sun_squawk_GC_newHits (Ints[7])
#define com_sun_squawk_VM_nextHashcode (Ints[48])
#define currentThreadID (Ints[28])
#define com_sun_squawk_ServiceOperation_op (Ints[11])
#define com_sun_squawk_VM_throwCount (Ints[53])
#define com_sun_squawk_VMThread_contendedEnterCount (Ints[29])
#define branchCountHigh (Ints[20])
#define com_sun_squawk_VM_usingTypeMap (Ints[1])
#define com_sun_squawk_VM_synchronizationEnabled (Ints[46])
#define traceStartHigh (Ints[22])
#define com_sun_squawk_VM_safePrintToVM (Ints[54])
#define com_sun_squawk_GC_allocationEnabled (Ints[42])
#define com_sun_squawk_ServiceOperation_code (Ints[9])
#define com_sun_squawk_Lisp2Bitmap_size (Ints[59])
#define com_sun_squawk_VM_tracing (Ints[26])
#define com_sun_squawk_GC_newCount (Ints[6])
#define com_sun_squawk_VM_bootstrapHash (Ints[44])
#define com_sun_squawk_GC_gcEnabled (Ints[43])
#define com_sun_squawk_VMThread_monitorsAllocatedCount (Ints[30])
#define com_sun_squawk_VM_timeAdjustmentsLo (Ints[55])
#define com_sun_squawk_VM_executingHooks (Ints[58])
#define com_sun_squawk_VM_allowUserGC (Ints[49])
#define com_sun_squawk_VMThread_systemThreadsOnly (Ints[35])
#define com_sun_squawk_GC_partialCollectionCount (Ints[39])
#define com_sun_squawk_Lisp2Bitmap_Iterator_inUse (Ints[60])
#define com_sun_squawk_GC_traceFlags (Ints[2])
#define traceStartLow (Ints[23])
#define branchCountLow (Ints[21])
#define com_sun_squawk_GC_fullCollectionCount (Ints[38])
#define com_sun_squawk_VMThread_stacksAllocatedCount (Ints[36])
#define com_sun_squawk_VMThread_waitTimeLo32 (Ints[33])
#define traceEndHigh (Ints[24])
#define com_sun_squawk_GC_monitorExitCount (Ints[4])
#define com_sun_squawk_VM_verboseLevel (Ints[45])
#define com_sun_squawk_VMThread_waitTimeHi32 (Ints[32])
#define com_sun_squawk_VMThread_threadSwitchCount (Ints[31])
#define com_sun_squawk_VM_extendsEnabled (Ints[0])
#define com_sun_squawk_GC_traceThreshold (Ints[41])
#define com_sun_squawk_VMThread_nextThreadNumber (Ints[8])
#define com_sun_squawk_VMThread_max_wait (Ints[34])
#define traceEndLow (Ints[25])
#define com_sun_squawk_ServiceOperation_i6 (Ints[18])
#define com_sun_squawk_VM_timeAdjustmentsHi (Ints[56])
#define com_sun_squawk_ServiceOperation_i5 (Ints[17])
#define com_sun_squawk_ServiceOperation_channel (Ints[12])
#define com_sun_squawk_ServiceOperation_i4 (Ints[16])
#define com_sun_squawk_VMThread_maxStackSize (Ints[37])
#define com_sun_squawk_GC_collecting (Ints[3])
#define com_sun_squawk_ServiceOperation_i3 (Ints[15])
#define com_sun_squawk_VM_nextIsolateID (Ints[57])
#define com_sun_squawk_ServiceOperation_i2 (Ints[14])
#define com_sun_squawk_GC_excessiveGC (Ints[40])
#define com_sun_squawk_ServiceOperation_i1 (Ints[13])
#define com_sun_squawk_VM_argv (Addrs[21])
#define com_sun_squawk_GC_nvmStart (Addrs[6])
#define com_sun_squawk_Lisp2Bitmap_Iterator_end (Addrs[24])
#define com_sun_squawk_VM_bootstrapSuite (Addrs[19])
#define cheneyEndMemoryProtect (Addrs[4])
#define com_sun_squawk_GC_nvmEnd (Addrs[7])
#define com_sun_squawk_VM_romStart (Addrs[16])
#define com_sun_squawk_GC_allocEnd (Addrs[15])
#define com_sun_squawk_ServiceOperation_addressResult (Addrs[2])
#define com_sun_squawk_Lisp2Bitmap_Iterator_next (Addrs[25])
#define com_sun_squawk_ServiceOperation_o2 (Addrs[1])
#define com_sun_squawk_GC_ramStart (Addrs[9])
#define com_sun_squawk_GC_nvmAllocationPointer (Addrs[8])
#define com_sun_squawk_ServiceOperation_o1 (Addrs[0])
#define com_sun_squawk_VM_bootstrapStart (Addrs[18])
#define com_sun_squawk_GC_allocStart (Addrs[13])
#define cheneyStartMemoryProtect (Addrs[3])
#define com_sun_squawk_Lisp2Bitmap_start (Addrs[22])
#define com_sun_squawk_Lisp2Bitmap_base (Addrs[23])
#define com_sun_squawk_GC_heapStart (Addrs[11])
#define com_sun_squawk_VM_romEnd (Addrs[17])
#define com_sun_squawk_GC_allocTop (Addrs[14])
#define com_sun_squawk_VMThread_serviceStack (Addrs[5])
#define com_sun_squawk_GC_heapEnd (Addrs[12])
#define com_sun_squawk_GC_ramEnd (Addrs[10])
#define com_sun_squawk_VM_bootstrapEnd (Addrs[20])
#define com_sun_squawk_VMThread_timerQueue (Oops[6])
#define com_sun_squawk_VMThread_otherThread (Oops[2])
#define com_sun_squawk_VMThread_osevents (Oops[8])
#define com_sun_squawk_VM_isolates (Oops[21])
#define com_sun_squawk_util_Tracer_filter (Oops[27])
#define com_sun_squawk_VM_keyedGlobalsMutex (Oops[23])
#define com_sun_squawk_GC_heapstats (Oops[12])
#define com_sun_squawk_VM_currentIsolate (Oops[0])
#define com_sun_squawk_GC_collector (Oops[10])
#define com_sun_squawk_util_Tracer__out (Oops[26])
#define com_sun_squawk_VM_registeredMailboxes (Oops[17])
#define com_sun_squawk_VMThread_serviceThread (Oops[3])
#define com_sun_squawk_VM_outOfMemoryError (Oops[14])
#define com_sun_squawk_ServiceOperation_pendingException (Oops[4])
#define com_sun_squawk_GC_readOnlyObjectMemories (Oops[11])
#define com_sun_squawk_VMThread_runnableThreads (Oops[5])
#define com_sun_squawk_VM_vmbufferDecoder (Oops[15])
#define com_sun_squawk_util_Tracer__features (Oops[25])
#define com_sun_squawk_VM_commandLineProperties (Oops[22])
#define com_sun_squawk_VMThread_currentThread (Oops[1])
#define com_sun_squawk_VMThread_events (Oops[7])
#define com_sun_squawk_VM_shutdownHooks (Oops[20])
#define com_sun_squawk_VM_isolateInitializer (Oops[16])
#define com_sun_squawk_VM_peripheralRegistry (Oops[18])
#define com_sun_squawk_VM_pluginObjectMemories (Oops[13])
#define com_sun_squawk_VM_keyeGlobals (Oops[24])
#define com_sun_squawk_VMThread_systemEvents (Oops[9])
#define com_sun_squawk_VM_reportedArray (Oops[19])
#define ROM_BIG_ENDIAN null
#define ROM_REVERSE_PARAMETERS 1
#define ROM_GLOBAL_INT_COUNT  61
#define ROM_GLOBAL_OOP_COUNT  28
#define ROM_GLOBAL_ADDR_COUNT 26
static Address com_sun_squawk_VM_startup;
static Address com_sun_squawk_VM_undefinedNativeMethod;
static Address com_sun_squawk_VM_callRun;
static Address com_sun_squawk_VM_getStaticOop;
static Address com_sun_squawk_VM_getStaticInt;
static Address com_sun_squawk_VM_getStaticLong;
static Address com_sun_squawk_VM_putStaticOop;
static Address com_sun_squawk_VM_putStaticInt;
static Address com_sun_squawk_VM_putStaticLong;
static Address com_sun_squawk_VM_yield;
static Address com_sun_squawk_VM_nullPointerException;
static Address com_sun_squawk_VM_arrayIndexOutOfBoundsException;
static Address com_sun_squawk_VM_arithmeticException;
static Address com_sun_squawk_VM_abstractMethodError;
static Address com_sun_squawk_VM_arrayStoreException;
static Address com_sun_squawk_VM_monitorenter;
static Address com_sun_squawk_VM_monitorexit;
static Address com_sun_squawk_VM_checkcastException;
static Address com_sun_squawk_VM_class_clinit;
static Address com_sun_squawk_VM__new;
static Address com_sun_squawk_VM_newarray;
static Address com_sun_squawk_VM_newdimension;
static Address com_sun_squawk_VM__lcmp;
static Address com_sun_squawk_VM_reportException;
static Address com_sun_squawk_VM_reportBreakpoint;
static Address com_sun_squawk_VM_reportStepEvent;

static void initMethods() {
    com_sun_squawk_VM_startup = lookupStaticMethod(174, 1);
    com_sun_squawk_VM_undefinedNativeMethod = lookupStaticMethod(174, 2);
    com_sun_squawk_VM_callRun = lookupStaticMethod(174, 3);
    com_sun_squawk_VM_getStaticOop = lookupStaticMethod(174, 4);
    com_sun_squawk_VM_getStaticInt = lookupStaticMethod(174, 5);
    com_sun_squawk_VM_getStaticLong = lookupStaticMethod(174, 6);
    com_sun_squawk_VM_putStaticOop = lookupStaticMethod(174, 7);
    com_sun_squawk_VM_putStaticInt = lookupStaticMethod(174, 8);
    com_sun_squawk_VM_putStaticLong = lookupStaticMethod(174, 9);
    com_sun_squawk_VM_yield = lookupStaticMethod(174, 10);
    com_sun_squawk_VM_nullPointerException = lookupStaticMethod(174, 11);
    com_sun_squawk_VM_arrayIndexOutOfBoundsException = lookupStaticMethod(174, 12);
    com_sun_squawk_VM_arithmeticException = lookupStaticMethod(174, 13);
    com_sun_squawk_VM_abstractMethodError = lookupStaticMethod(174, 14);
    com_sun_squawk_VM_arrayStoreException = lookupStaticMethod(174, 15);
    com_sun_squawk_VM_monitorenter = lookupStaticMethod(174, 16);
    com_sun_squawk_VM_monitorexit = lookupStaticMethod(174, 17);
    com_sun_squawk_VM_checkcastException = lookupStaticMethod(174, 18);
    com_sun_squawk_VM_class_clinit = lookupStaticMethod(174, 19);
    com_sun_squawk_VM__new = lookupStaticMethod(174, 20);
    com_sun_squawk_VM_newarray = lookupStaticMethod(174, 21);
    com_sun_squawk_VM_newdimension = lookupStaticMethod(174, 22);
    com_sun_squawk_VM__lcmp = lookupStaticMethod(174, 23);
    com_sun_squawk_VM_reportException = lookupStaticMethod(174, 24);
    com_sun_squawk_VM_reportBreakpoint = lookupStaticMethod(174, 25);
    com_sun_squawk_VM_reportStepEvent = lookupStaticMethod(174, 26);
}
