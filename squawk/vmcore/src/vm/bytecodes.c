/**** Created by Squawk builder from "vmcore/src/vm/bytecodes.c.spp.preprocessed" ****/ /*
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

/**
 * Check that slot clearing is being done correctly.
 *
 * One small compromise occurs with this option because the this_getfield
 * and this_putfield bytecodes are really loadparm0_getfield and loadparm0_putfield
 * bytecodes, and are used for both virtual methods and static methods.
 * In the case where they are used in a virtual method the translator knows
 * not to clear all the initialized reference slots because a null pointer
 * exeception cannot occur (see com.sun.squawk.translator.ir.GetField#mayCauseGC()).
 * In the case of a static the null pointer check must be made and so any
 * uninitialized slots will be cleared. Because it is not easy to know if the
 * currently executing method is static or virtual a conservative approach is
 * taken where the this_getfield and this_putfield bytecodes never performs
 * a slot clearing check.
 */



  #define CHECK_SLOT_CLEARING false


#ifndef SILLYADDBUG
#define NOSILLYADDBUG 1
#else
#define NOSILLYADDBUG 0
#endif


#define   nextbytecode() continue







static int             MethodHeader_minfoValue1_L(Address);
static int             MethodHeader_minfoValue2_L(Address);
static int             MethodHeader_minfoValue3_L(Address);
static int             MethodHeader_getOffsetToLastMinfoByte0_LII(Address oop, int p, int b0);

static int             VM_lookup_b(int, Address);
static int             VM_lookup_i(int, Address);
static int             VM_lookup_s(int, Address);
static int             VM_arrayOopStoreCheck(Address, int, Address);
static int             VM_findSlot(Address, Address, int);
static boolean         VM_instanceof(Address, Address);
static Address         Isolate_getClassStateForInterpreter_L(Address, Address);

        /*-----------------------------------------------------------------------*\
         *                      Class state cache managemant                     *
         *                                                                       *
         *   Even when compiling the class state lookup code into C, this cache  *
         *   adds about 10% performance for static variable accesses in common   *
         *   case.                                                               *
        \*-----------------------------------------------------------------------*/

#ifdef INTERPRETER_STATS
#define updateCachedClassAccesses() cachedClassAccesses++
#define updateCachedClassHits() cachedClassHits++
#else
#define updateCachedClassAccesses()
#define updateCachedClassHits()
#endif /* INTERPRETER_STATS */

        /**
         * Add a cached class state association.
         *
         * @param klass the klass
         * @param state the klass state

         * @return its class state or null if not found
         */
#define  addClassState(klass_238, state_240) { Address  klass_239 = klass_238;  Address  state_241 = state_240;  \
            int i; \
            for (i = CLASS_CACHE_SIZE-1 ; i > 0  ; --i) { \
                cachedClass[i]      = cachedClass[i-1]; \
                cachedClassState[i] = cachedClassState[i-1]; \
            } \
            cachedClass[0]      = (klass_239); \
            cachedClassState[0] = (state_241); \
        }

        /**
         * Get a cached class state.
         *
         * @param klass the klass
         * @return its class state or null if not found
         */
        Address getClassState(Address klass) {
            int i;
            updateCachedClassAccesses();
            for (i = 0 ; i < CLASS_CACHE_SIZE ; i++) {
                if (cachedClass[i] == klass) {
                    updateCachedClassHits();
                    return cachedClassState[i];
                }
            }
            return null;
        }

        INLINE Address VM_getClassStateREAL(Address klass) {
            Address cs = getClassState(klass);
            if (cs != null) {
                return cs;
            }
            return Isolate_getClassStateForInterpreter_L(com_sun_squawk_VM_currentIsolate, klass);
        }

        /**
         * Test to see if a class needs initializing.
         *
         * @param klass the klass
         * @return true if it does.
         */
INLINE boolean needsInitializing(Address klass_242) {
            if (com_sun_squawk_Klass_modifiers((klass_242)) & com_sun_squawk_Modifier_KLASS_MUSTCLINIT) {
                return VM_getClassStateREAL((klass_242)) == null;
            }
            return false;
        }

        /**
         * Invalidate the class state cache.
         *
         * @return true if it was already invalid.
         */
INLINE boolean invalidateClassStateCache() {
            int i;
            UWord res = 0;
            for (i = 0 ; i < CLASS_CACHE_SIZE ; i++) {
                res |= (UWord)cachedClass[i];
                cachedClass[i] = null;
            }
            return res == 0;
        }

        /*-----------------------------------------------------------------------*\
         *                           Instruction decoding                        *
        \*-----------------------------------------------------------------------*/

        /**
         * Fetch a byte from ip.
         *
         * @return the value
         */
#define  fetchByte() (  \
             getByteTyped(ip++, 0, AddressType_BYTECODE)  \
        )

        /**
         * Fetch an unsigned byte from from ip.
         *
         * @return the value
         */
#define  fetchUByte() (  \
             getUByteTyped(ip++, 0, AddressType_BYTECODE)  \
        )

        /**
         * Fetch a short from ip and place in fparm.
         */
#define  fetchShort() {  \
            if (PLATFORM_UNALIGNED_LOADS) { \
                fparm = getShortTyped(ip, 0, AddressType_BYTECODE); \
                ip += sizeof(short); \
            } else { \
                if (PLATFORM_BIG_ENDIAN) { \
                    int b1 = fetchByte(); \
                    int b2 = fetchUByte(); \
                    fparm = (b1 << 8) | b2; \
                } else { \
                    int b1 = fetchUByte(); \
                    int b2 = fetchByte(); \
                    fparm = (b2 << 8) | b1; \
                } \
            } \
        }

        /**
         * Fetch an unsigned short from ip and place in fparm.
         */
#define  fetchUShort() {  \
            if (PLATFORM_UNALIGNED_LOADS) { \
                fparm = getUShortTyped(ip, 0, AddressType_BYTECODE); \
                ip += sizeof(unsigned short); \
            } else { \
                int b1 = fetchUByte(); \
                int b2 = fetchUByte(); \
                if (PLATFORM_BIG_ENDIAN) { \
                    fparm = (b1 << 8) | b2; \
                } else { \
                    fparm = (b2 << 8) | b1; \
                } \
            } \
        }

        /**
         * Fetch an int from ip and place in fparm.
         */
#define  fetchInt() {  \
            if (PLATFORM_UNALIGNED_LOADS) { \
                fparm = getIntTyped(ip, 0, AddressType_BYTECODE); \
                ip += sizeof(int); \
            } else { \
                int b1 = fetchUByte(); \
                int b2 = fetchUByte(); \
                int b3 = fetchUByte(); \
                int b4 = fetchUByte(); \
                if (PLATFORM_BIG_ENDIAN) { \
                    fparm = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4; \
                } else { \
                    fparm = (b4 << 24) | (b3 << 16) | (b2 << 8) | b1; \
                } \
            } \
        }

        /**
         * Fetch a long from ip and place in flparm.
         *
         * @return the value
         */
#define  fetchLong() {  \
            if (PLATFORM_UNALIGNED_64_LOADS) { \
                flparm = getLongTyped(ip, 0, AddressType_BYTECODE); \
                ip += sizeof(jlong); \
            } else { \
                flparm = getUnalignedLong(ip, 0); \
                ip += sizeof(jlong); \
            } \
        }


    /*-----------------------------------------------------------------------*\
     *                          Operand stack                                *
    \*-----------------------------------------------------------------------*/

#if ASSUME
        /**
         * Asserts that the stack pointer is within the current stack and decrementing it
         * won't overflow the stack limit.
         */
#define  checkPush() {  \
 \
            /* if (loeq(sp, sl)) fprintf(stderr, format("sp=%A, sl=%A, ss=%A\n"), sp, sl, ss); */ \
            assume(sp > sl); \
            assume(getUWord(ss, SC_guard) == 0); \
            if (ss == com_sun_squawk_VMThread_serviceStack) { \
                assume(loeq(sp, Address_add(com_sun_squawk_VMThread_serviceStack, SERVICE_CHUNK_SIZE))); \
            } else { \
                assume(loeq(sp, &ss[getArrayLength(ss)])); \
            } \
 \
 \
        }
#else
#define checkPush()
#endif /* ASSUME */

        /**
         * Pushes an int value onto the runtime stack.
         */
#define  pushInt(value_243) { int  value_244 = value_243;  \
 \
            checkPush(); \
            setUWordTyped(--sp, 0, AddressType_INT, (UWord)(value_244)); \
 \
 \
 \
 \
        }

        /**
         * Pops an int value from the runtime stack.
         */
#define  popInt() (  \
 \
             (int)getUWordTyped(sp++, 0, AddressType_INT)  \
 \
 \
 \
        )

        /**
         * Pushes an address onto the runtime stack -- always downwards.
         */
#define  downPushAddress(value_245) { Address  value_246 = value_245;  \
            checkPush(); \
            setObject(--sp, 0, (value_246)); \
        }

        /**
         * Pushes an address onto the runtime stack.
         */
#define  pushAddress(value_247) { Address  value_248 = value_247;  \
 \
            checkPush(); \
            setObject(--sp, 0, (value_248)); \
 \
 \
 \
 \
        }

        /**
         * Pops an address from the runtime stack.
         */
#define  popAddress() (  \
 \
             getObject(sp++, 0)  \
 \
 \
 \
        )

        /**
         * Peeks the value on the top of the runtime stack.
         */
#define  peek() (  \
             getUWordTyped(sp, 0, AddressType_ANY)  \
        )

        /**
         * Pushes a jlong value onto the runtime stack.
         */
#if SQUAWK_64
#define  pushLong(value_249) { jlong  value_250 = value_249;  \
 \
            checkPush(); \
            setLong(--sp, 0, (value_250)); \
            assumeInterp((value_250) == getLong(sp, 0)); \
 \
 \
 \
 \
 \
        }
#else
#define  pushLong(value_251) { jlong  value_252 = value_251;  \
 \
            checkPush(); \
            --sp; \
            setLongAtWord(--sp, 0, (value_252)); \
            assumeInterp((value_252) == getLongAtWord(sp, 0)); \
 \
 \
 \
 \
 \
 \
        }
#endif

        /**
         * Pops a jlong value from the runtime stack.
         */
#if SQUAWK_64
#define  popLong() (  \
 \
             getLong(sp++, 0)  \
 \
 \
 \
        )
#else
#define  popLong() (  \
 \
             getLongAtWord(sp = sp + 2, -2)  \
 \
 \
 \
        )
#endif

        /**
         * Pops a UWord from the runtime stack.
         */
#define  popWord() (  \
 \
             getUWord(sp++, 0)  \
 \
 \
 \
        )

        /**
         * Pushes a UWord to the runtime stack.
         */
#define  pushWord(value_253) { UWord  value_254 = value_253;  \
 \
            checkPush(); \
            setUWord(--sp, 0, (value_254)); \
 \
 \
 \
 \
        }

#if TYPEMAP
        /**
         * Pushes a UWord onto the runtime stack, recording the type of the value pushed.
         */
#define  pushAsType(value_255, type_257) { UWord  value_256 = value_255;  char  type_258 = type_257;  \
 \
            checkPush(); \
            setUWordTyped(--sp, 0, (type_258), (value_256)); \
 \
 \
 \
 \
        }

        /**
         * Pops a UWord from the runtime stack, checking that its type matches a given type.
         */
#define  popAsType(type_259) (  \
 \
             getUWordTyped(sp++, 0, (type_259))  \
 \
 \
 \
        )
#else
#define pushAsType(value, type) pushWord(value)
#define popAsType(type) popWord()
#endif /* TYPEMAP */

        /*-----------------------------------------------------------------------*\
         *                          Bytecode dispatching                         *
        \*-----------------------------------------------------------------------*/

        /**
         * Prefix for bytecode with no parameter.
         */
#define  iparmNone() {  \
        }

        /**
         * Prefix for bytecode with a byte parameter.
         */
#define  iparmByte() {  \
            iparm = fetchByte(); \
        }

        /**
         * Prefix for bytecode with an unsigned byte parameter.
         */
#define  iparmUByte() {  \
            iparm = fetchUByte(); \
        }

        /**
         * Add 256 to the next unsigned byte and jump to that bytecode execution.
         */
#define  do_escape() {  \
            opcode = fetchUByte() + 256; \
            goto next; \
        }

        /**
         * Or the (parameter<<8) into the value of the next bytecode and then
         * dispatch to the wide version of the opcode.
         */
#define  do_wide_n(n_260) { int  n_261 = n_260;  \
            opcode = fetchUByte() + OPC_Properties_WIDE_DELTA; \
            iparm  = fetchUByte() | ((n_261)<<8); \
            goto next; \
        }

        /**
         * Load the inlined short as the value of the next bytecode and then
         * dispatch to the wide version of the opcode.
         */
#define  do_wide_short() {  \
            int fparm; \
            opcode = fetchUByte() + OPC_Properties_WIDE_DELTA; \
            fetchShort(); \
            iparm = fparm; \
            goto next; \
        }

        /**
         * Load the inlined int as the value of the next bytecode and then
         * dispatch to the wide version of the opcode.
         */
#define  do_wide_int() {  \
            int fparm; \
            opcode = fetchUByte() + OPC_Properties_WIDE_DELTA; \
            fetchInt(); \
            iparm = fparm; \
            goto next; \
        }

        /**
         * Or the (parameter<<8) in to the value of the next bytecode and then
         * dispatch to the wide version of the opcode.
         */
#define  do_escape_wide_n(n_262) { int  n_263 = n_262;  \
            opcode = fetchUByte() + 256 + OPC_Properties_ESCAPE_WIDE_DELTA; \
            iparm  = fetchUByte() | ((n_263)<<8); \
            goto next; \
        }

        /**
         * Load the inlined short as the value of the next bytecode and then
         * dispatch to the wide version of the opcode.
         */
#define  do_escape_wide_short() {  \
            int fparm; \
            opcode = fetchUByte() + 256 + OPC_Properties_ESCAPE_WIDE_DELTA; \
            fetchShort(); \
            iparm = fparm; \
            goto next; \
        }

        /**
         * Load the inlined int as the value of the next bytecode and then
         * dispatch to the wide version of the opcode.
         */
#define  do_escape_wide_int() {  \
            int fparm; \
            opcode = fetchUByte() + 256 + OPC_Properties_ESCAPE_WIDE_DELTA; \
            fetchInt(); \
            iparm = fparm; \
            goto next; \
        }


        /*-----------------------------------------------------------------------*\
         *                             Access to data                            *
        \*-----------------------------------------------------------------------*/

        /**
         * Gets a local variable ignoring the typemap.
         *
         * @param n int index to the local variable
         * @return the value
         */
#define  peekLocal(n_264) (  \
             getUWordTyped(fp, FP_local0 - (n_264), AddressType_ANY)  \
        )

        /**
         * Gets a local variable.
         *
         * @param n int index to the local variable
         * @return the value
         */
#define  getLocal(n_265) (  \
             getUWordTyped(fp, FP_local0 - (n_265), getMutationType())  \
        )

        /**
         * Sets a local variable.
         *
         * @param n int index to the local variable
         * @param value the value to set
         */
#define  setLocal(n_266, value_268) { int  n_267 = n_266;  UWord  value_269 = value_268;  \
            setUWordTyped(fp, FP_local0 - (n_267), getMutationType(), (value_269)); \
        }

        /**
         * Gets a local jlong variable.
         *
         * @param n int index to the local variable
         * @return the value
         */
#define  getLocalLong(n_270) (  \
             getLongAtWord(fp, FP_local0 - (n_270))  \
        )

        /**
         * Sets a local variable.
         *
         * @param n int index to the local variable
         * @param value the value to set
         */
#define  setLocalLong(n_271, value_273) { int  n_272 = n_271;  jlong  value_274 = value_273;  \
            setLongAtWord(fp, FP_local0 - (n_272), (value_274)); \
        }

        /**
         * Gets a parameter word.
         *
         * @param n int index to the local variable
         * @return the value
         */
#define  getParmTyped(n_275, type_276) (  \
             getUWordTyped(fp, FP_parm0 + (n_275), (type_276))  \
        )

        /**
         * Gets a parameter word.
         *
         * @param n int index to the local variable
         * @return the value
         */
#define  getParm(n_277) (  \
             getParmTyped((n_277), getMutationType())  \
        )

        /**
         * Sets a parameter word.
         *
         * @param n int index to the local variable
         * @param value the value to set
         */
#define  setParm(n_278, value_280) { int  n_279 = n_278;  UWord  value_281 = value_280;  \
            setUWordTyped(fp, FP_parm0 + (n_279), getMutationType(), (value_281)); \
        }

        /**
         * Gets a jlong parameter word.
         *
         * @param n int index to the local variable
         * @return the value
         */
#define  getParmLong(n_282) (  \
             getLongAtWord(fp, FP_parm0 + (n_282))  \
 \
        )

        /**
         * Sets a jlong parameter word.
         *
         * @param n int index to the local variable
         * @param value the value to set
         */
#define  setParmLong(n_283, value_285) { int  n_284 = n_283;  jlong  value_286 = value_285;  \
            setLongAtWord(fp, FP_parm0 + (n_284), (value_286)); \
        }

#if ASSUME
#define  boundsAssume(oop_287, index_289) { Address  oop_288 = oop_287;  int  index_290 = index_289;  \
            assume((oop_288)); \
            /* use the unsigned bounds check trick: */ \
            assume((unsigned int) (index_290) < (unsigned int)getArrayLength((oop_288))); \
        }
#else
#define  boundsAssume(oop_291, index_293) { Address  oop_292 = oop_291;  int  index_294 = index_293;  \
        }
#endif /* ASSUME */


#define  boundsAssumeAlways(oop_295, index_297) { Address  oop_296 = oop_295;  int  index_298 = index_297;  \
            assumeAlways((oop_296)); \
            /* use the unsigned bounds check trick: */ \
            assumeAlways((unsigned int) (index_298) < (unsigned int)getArrayLength((oop_296))); \
        }

        /*-----------------------------------------------------------------------*\
         *                             Utility code                              *
        \*-----------------------------------------------------------------------*/

        /**
         * Gets the current method pointer.
         */
#define  getMP() (  \
             getObject(fp, FP_method)  \
        )

        /**
         * Gets the current class pointer.
         */
#define  getCP() (  \
             getObject(getMP(), HDR_methodDefiningClass)  \
        )

        /**
         * Gets the object specified by an index from the class of the currently executing method.
         *
         * @param  index the index of the object
         * @return the object
         */
#define  getKlassObject(index_299) (  \
             ((Address *)com_sun_squawk_Klass_objects(getCP()))[(index_299)]  \
        )

        /**
         * Gets the length of an array object.
         *
         * @param  oop the pointer to the array.
         * @return the length
         */
INLINE int getArrayLength(Address oop_300) {
            assume((int)(getUWord((oop_300), HDR_length) >> 2) >= 0);
            return (int)(getUWord((oop_300), HDR_length) >> 2);
        }

        /**
         * Gets the static method specified by an index from the class.
         *
         * @param  index the index of the method
         * @return the method
         */
INLINE Address getStaticMethod(Address cls_301, int index_302) {
            Address stable;
            assume((cls_301));
            stable = com_sun_squawk_Klass_staticMethods((cls_301));
            assume(stable);
        assume((unsigned int)(index_302) < (unsigned int)getArrayLength(stable));
            return ((Address *)stable)[(index_302)];
        }

        /**
         * Gets the virtual method specified by an index from the class.
         *
         * @param index the index of the method
         * @return the method
         */
INLINE Address getVirtualMethod(Address cls_303, int index_304) {
            Address vtable;
            assume((cls_303));
            vtable = com_sun_squawk_Klass_virtualMethods((cls_303));
        assume((unsigned int)(index_304) < (unsigned int)getArrayLength(vtable));
            return ((Address *)vtable)[(index_304)];
        }

        /**
         * Sets the length of an array object.
         *
         * @param oop the pointer to the array
         * @param size the length
         * @return false if length was too large
         */
INLINE boolean setArrayLength(Address oop_305, int size_306) {
    assume((size_306) >= 0);
            if ((size_306) > 0x3FFFFFF) {
                return false;
            }
            setUWord((oop_305), HDR_length, ((size_306) << HDR_headerTagBits) | HDR_arrayHeaderTag);
            return true;
        }

        /**
         * Gets the Klass or the ObjectAssociation of an object.
         *
         * @param  oop the pointer to the object.
         * @return the Klass ot the ObjectAssociation
         */
#if ASSUME
INLINE Address getClassOrAssociation(Address oop_307) {
            /* Catches an attempt to dereference a forwarding pointer */
            assume(com_sun_squawk_GC_collecting || ((UWord)getObject((oop_307), HDR_klass) & HDR_headerTagMask) == 0);
            return getObject((oop_307), HDR_klass);
        }
#else
#define  getClassOrAssociation(oop_308) (  \
             getObject((oop_308), HDR_klass)  \
        )
#endif
         /**
         * Given a klass or an association, deerefence the self field
         * to return the actual Klass.
         *
         * @param the Klass or the ObjectAssociation
         * @return the Klass, or the klass refered to by the association
         */
#define  associationToKlass(oop_309) (  \
             com_sun_squawk_Klass_self((oop_309))  \
        )

        /**
         * Gets the class of an object.
         *
         * @param  oop the pointer to the object.
         * @return the class
         */
#define  getClass(oop_310) (  \
             com_sun_squawk_Klass_self(getClassOrAssociation((oop_310)))  \
        )

        /*
         * Decodes a counter from the minfo area.
         *
         * The routines are translated from Java to C, and are definedin in vm2c.c.spp
         */

        /**
         * Gets the b0 byte of the method header.
         *
         * @param mp the method pointer
         * @return the value
         */
#define  getb0(mp_311) (  \
             getUByte((mp_311), HDR_methodInfoStart)  \
        )

        /**
         * Gets the b1 byte of the method header.
         *
         * @param mp the method pointer
         * @return the value
         */
#define  getb1(mp_312) (  \
             getUByte((mp_312), HDR_methodInfoStart - 1)  \
        )

        /**
         * Decode the stack count.
         *
         * @param b0 the first byte
         * @param b1 the second byte
         * @return the value
         */
#define  decodeStackCount(b0_313, b1_314) (  \
             (b1_314) & 0x1F  \
        )

        /**
         * Decode the stack count.
         *
         * @param b0 the first byte
         * @param b1 the second byte
         * @return the value
         */
#define  decodeLocalCount(b0_315, b1_316) (  \
             ((((b0_315) << 8) | (b1_316)) >> 5) & 0x1F  \
        )

        /**
         * Decode the Parm count.
         *
         * @param b0 the first byte
         * @return the value
         */
#define  decodeParmCount(b0_317) (  \
             (b0_317) >> 2  \
        )

        /**
         * Gets the number of stack words used by a method.
         *
         * @param mp the method pointer
         * @return the count
         */
INLINE int getStackCount(Address mp_318) {
            int b0 = getb0((mp_318));
            if (b0 < 128) {
                int b1 = getb1((mp_318));
                return decodeStackCount(b0, b1);
            } else {
                return MethodHeader_minfoValue1_L((mp_318));
            }
        }

        /**
         * Gets the number of local words used by a method.
         *
         * @param mp the method pointer
         * @return the count
         */
INLINE int getLocalCount(Address mp_319) {
            int b0 = getb0((mp_319));
            if (b0 < 128) {
                int b1 = getb1((mp_319));
                return decodeLocalCount(b0, b1);
            } else {
                return MethodHeader_minfoValue2_L((mp_319));
            }
        }

        /**
         * Gets the number of parameter words used by a method.
         *
         * @param mp the method pointer
         * @return the count
         */
INLINE int getParmCount(Address mp_320) {
            int b0 = getb0((mp_320));
            if (b0 < 128) {
                return decodeParmCount(b0);
            } else {
                return MethodHeader_minfoValue3_L((mp_320));
            }
        }

        /**
         * Get the offset to the last byte of the Minfo area.
         *
         * @param np the pointer to the method
         * @return the length in bytes
         */
INLINE int getOffsetToLastMinfoByte(Address mp_321) {
            int p = HDR_methodInfoStart;
            int b0 = getUByte((mp_321), p--);
            if (b0 < 128) {
                return p;
            } else {
                return MethodHeader_getOffsetToLastMinfoByte0_LII((mp_321), p, b0);
            }
        }

#ifdef _MSC_VER
#pragma auto_inline(off)
#endif

        /**
         * Gets the number of local words plus stack words used by a method.
         *
         * @param mp the method pointer
         * @return the count
         */
        int getLocalCountPlusStackCountIfNotReversingParms(Address mp) {
            int res = getLocalCount(mp);




            return res;
        }

#ifdef _MSC_VER
#pragma auto_inline(on)
#endif

        /**
         * Clears the operand stack given the number of locals in the current method.
         *
         * @param delta the number of words to adjust by
         */
#define  resetStackPointerFromDelta(delta_322) { int  delta_323 = delta_322;  \
            sp = fp - (delta_323) + 1; /* + 1 so sp points one word before first stack word */ \
        }

        /**
         * Clears the operand stack.
         */
#define  resetStackPointer() {  \
            Address mp = getMP(); \
            int delta; \
            int b0 = getb0(mp); \
            if (b0 < 128) { \
                int b1 = getb1(mp); \
 \
                delta = decodeLocalCount(b0, b1); \
 \
 \
 \
            } else { \
                delta = getLocalCountPlusStackCountIfNotReversingParms(mp); \
            } \
            resetStackPointerFromDelta(delta); \
        }

        /**
         * Clears the operand stack when REVERSE_PARAMETERS is true.
         */
#define  resetStackPointerIfRevParms() {  \
 \
            resetStackPointer(); \
 \
        }

        /**
         * Clears the operand stack when REVERSE_PARAMETERS is false.
         */
#define  resetStackPointerIfNotRevParms() {  \
 \
 \
 \
 \
        }


        /*-----------------------------------------------------------------------*\
         *                      Java String printing                             *
        \*-----------------------------------------------------------------------*/

/**
 * A macro defining whether or not the printJavaString function should
 * handle Strings with non-ascii characters.
 */


        /**
         * Prints the contents of a Java String object to a given stream.
         * No trailing '\0' is printed/appended.
         *
         * @param str  the address of a Java String object
         * @param out  where to print the string's contents
         * @return the number of characters printed
         */
        int printJavaString(Address str, FILE* out) {
            int i;
            int written = 0;
            if (str == null) {
                written = fprintf(out, "null");
            } else {
                int length = getArrayLength(str);
#ifdef UNICODE
                Address cls = getClass(str);
				assume(((UWord)getObject(str, HDR_klass) & HDR_headerTagMask) == 0);
                if (com_sun_squawk_Klass_id(cls) == com_sun_squawk_StringOfBytes) {
#endif
                    unsigned char *chars = (unsigned char *)str;
                    fprintf(out, "%.*s", length, chars);
                    written = length;
#ifdef UNICODE
                } else {
                    unsigned short *chars = (unsigned short *)str;
                    if (com_sun_squawk_Klass_id(cls) != com_sun_squawk_String) {
                        fatalVMError("com_sun_squawk_VM_printString was not passed a string");
                    }
                    for (i = 0; i < length; i++) {
                        fprintf(out, "%lc", chars[i]);
                    }
                    written = length;
                }
#endif
            }
            fflush(out);
            return written;
        }

        /**
         * Appends the contents of a Java String object
         * to a given buffer. No trailing '\0' is printed/appended.
         *
         * @param str  the address of a Java String object
         * @param buf  where to append the string's contents
         * @param bufLength the length of 'buf'
         * @return the number of characters appended
         */
        int printJavaStringBuf(Address str, char *buf, int bufLength) {
            int i;
            int written = 0;
            if (str == null) {
                int min = 4;
                if (min > bufLength) {
                    min = bufLength;
                }
                memmove(buf, "null", min);
                written = min;
            } else {
                int length = getArrayLength(str);
#ifdef UNICODE
                Address cls = getClass(str);
				assume(((UWord)getObject(str, HDR_klass) & HDR_headerTagMask) == 0);
                if (com_sun_squawk_Klass_id(cls) == com_sun_squawk_StringOfBytes) {
#endif
                    unsigned char *chars = (unsigned char *)str;
                    int min = length;
                    if (min > bufLength) {
                        min = bufLength;
                    }
                    memmove(buf, chars, min);
                    written = min;
#ifdef UNICODE
                } else {
                    unsigned short *chars = (unsigned short *)str;
                    if (com_sun_squawk_Klass_id(cls) != com_sun_squawk_String) {
                        fatalVMError("com_sun_squawk_VM_printString was not passed a string");
                    }
                    for (i = 0; i < length && i < bufLength; i++) {
                        buf[i] = (char)chars[i];
                    }
                    written = i;
                }
#endif
            }
            return written;
        }


        /*-----------------------------------------------------------------------*\
         *                                Upcalls                                *
        \*-----------------------------------------------------------------------*/

        /**
         * Causes the Java method at 'mth' to be invoked without resetting the stack pointer.
         *
         * @param mth the address of the method to be invoked
         */
#define  callNoReset(mth_324) { Address  mth_325 = mth_324;  \
            assumeInterp((mth_325) != 0); \
assumeInterp(((UWord)getObject((mth_325), HDR_klass) & HDR_headerTagMask) == 0); \
            assumeInterp(com_sun_squawk_Klass_id(getClass((mth_325))) == CID_BYTECODE_ARRAY); \
            assumeInterp(inCode((mth_325))); \
            checkReferenceSlots(); \
            downPushAddress(ip); \
            ip = (mth_325); \
        }

        /**
         * Causes the Java method at 'mth' to be invoked, resetting the stack pointer first if necessary.
         *
         * @param mth the address of the method to be invoked
         */
#define  call(mth_326) { Address  mth_327 = mth_326;  \
            resetStackPointerIfNotRevParms(); \
            callNoReset((mth_327)); \
        }

       /*
        * Call exception reporting method - and leave stack as if thrower called.
        * This call returns by rethrowing the original exception. VM.throwException notices the re-throw,
        * clears these magic flags, and sets up SC_lastFP (etc) to the catch block (see below).
        *
        * Using code fragment macro becuase of interaction of #ifdef and MACRO processing
        */
#if SDA_DEBUGGER
#define SDA_HANDLE_BREAKPOINT_CODE_FRAG                                                                      \
                if (!runningOnServiceThread &&                                                               \
                    (hbp = com_sun_squawk_VMThread_hitBreakpoint(newThread)) != null &&                      \
                    com_sun_squawk_HitBreakpoint_state(hbp) == com_sun_squawk_HitBreakpoint_EXC_HIT) {       \
                    set_com_sun_squawk_HitBreakpoint_state(hbp, com_sun_squawk_HitBreakpoint_EXC_REPORTING); \
                    /* fprintf(stderr, format("Calling do_reportException. newMP: %A, ipOffset: %O, ip: %A, fp: %A.\n", newMP, ipOffset, ip, fp); */ \
                    call(com_sun_squawk_VM_reportException);                                                 \
                } else {                                                                                     \
                    /*fprintf(stderr, format("fp = %d newMP = %d ipoffset = %d ip=%d tid=%d returnFP=%A\n"), fp, newMP, ipOffset, ip, com_sun_squawk_VMThread_threadNumber(newThread), getObject(fp, FP_returnFP));*/ \
                }
#else
#define SDA_HANDLE_BREAKPOINT_CODE_FRAG FALSE
#endif /*SDA_DEBUGGER */

        /**
         * Switch to the 'other' thread.
         */
#define  threadswitchmain() {  \
            Address oldThread  = com_sun_squawk_VMThread_currentThread; \
            Address oldStack   = (Address)com_sun_squawk_VMThread_stack(oldThread); \
            UWord   oldMP      = (UWord)getObject(fp, FP_method); \
            UWord   ipOffset   = ((UWord)ip) - oldMP; \
            Address newThread  = com_sun_squawk_VMThread_otherThread; \
            Address newStack   = (Address)com_sun_squawk_VMThread_stack(newThread); \
            assumeInterp(newStack != null); \
            assumeInterp(!com_sun_squawk_GC_collecting); \
 \
            /*fprintf(stderr, format("%%%%%%%%%%%%%% fp = %A oldMP = %A ipoffset = %d ip=%A tid=%d oldThread=%A oldStack=%A newThread=%A newStack=%A\n"), fp, oldMP, ipOffset, ip, com_sun_squawk_VMThread_threadNumber(oldThread), oldThread, oldStack, newThread, newStack);*/ \
 \
            /* \
             * Save current VM state in the current thread. \
             */ \
            if (oldStack == null) { \
                /* handle case from VMThread.abandonThread()0 oldThread has died, and will not come back.*/ \
                assumeInterp(com_sun_squawk_VMThread_state(oldThread) == com_sun_squawk_VMThread_DEAD); \
            } else { \
                assumeInterp(oldStack == (Address)ss); \
                setObject(oldStack, SC_lastFP, fp); \
                setUWord(oldStack, SC_lastBCI, ipOffset); \
            } \
 \
            /* \
             * Swap the threads and setup the current isolate. \
             */ \
            com_sun_squawk_VMThread_otherThread = oldThread; \
            com_sun_squawk_VMThread_currentThread = newThread; \
            if (TRACE) { \
                currentThreadID = com_sun_squawk_VMThread_threadNumber(newThread); \
            } \
 \
            /* \
             * If the new thread is not the service thread then switch the \
             * the current isolate to the new thread's isolate. This test means \
             * that code run on the service thread will run in the isolate \
             * context of the caller. \
             */ \
            if (newThread != com_sun_squawk_VMThread_serviceThread) { \
                int oldRunningOnServiceThread = runningOnServiceThread; \
                Address newIsolate = (Address)com_sun_squawk_VMThread_isolate(newThread); \
 \
                runningOnServiceThread = false; \
                if (com_sun_squawk_VM_currentIsolate != newIsolate) { \
                    com_sun_squawk_VM_currentIsolate = newIsolate; \
                    invalidateClassStateCache(); \
                    set_sda_bp_set_or_stepping(newIsolate); /* changed isolate */ \
                } else if (oldRunningOnServiceThread) { \
                    set_sda_bp_set_or_stepping(newIsolate); /* coming off of service thread */ \
                } \
 \
                /* \
                 * If not simply switching back from the service thread to its caller \
                 * then check that the number of pending monitor enter operations is zero. \
                 */ \
                assumeInterp(oldThread == com_sun_squawk_VMThread_serviceThread || pendingMonitorStackPointer == 0); \
 \
            } else { \
                runningOnServiceThread = true; \
                set_sda_bp_set_or_stepping(null); /* coming on to service thread */ \
            } \
 \
            /* \
             * Switch to the new context. \
             */ \
            setStack(newStack); \
            fp = getObject(ss, SC_lastFP); \
            if (fp == null) {   /* New thread                   */ \
                fp = null;      /* The return FP should be zero */ \
                ip = null;      /* The return IP should be zero */ \
                sp = &ss[getArrayLength(ss)]; \
 \
                call(com_sun_squawk_VM_callRun); \
 \
 \
 \
 \
                /*fprintf(stderr, "callRun: fp = %d sp=%d ip=%d tid = %d\n", fp, sp, ip, com_sun_squawk_VMThread_threadNumber(newThread));*/ \
            } else { \
                Address hbp; \
 \
                assumeInterp(getMP() != null); \
                ipOffset = getUWord(ss, SC_lastBCI); \
                resetStackPointer(); \
                ip = (ByteAddress)getMP() + ipOffset; \
                assumeInterp(inCode(ip)); \
 \
                SDA_HANDLE_BREAKPOINT_CODE_FRAG; \
            } \
        }

        /**
         * Switch to the 'other' thread.
         *
         * @param code the service operation code
         */
#define  threadSwitch(code_328) { int  code_329 = code_328;  \
            /* \
             * Set the service operation code. \
             */ \
            com_sun_squawk_ServiceOperation_code = (code_329); \
 \
            goto threadswitchstart; \
 \
 \
 \
        }

        /**
         * Switch to the service thread.
         */
#define  threadSwitchFor(code_330) { int  code_331 = code_330;  \
            assumeInterp(!runningOnServiceThread); \
            com_sun_squawk_VMThread_otherThread = com_sun_squawk_VMThread_serviceThread; \
            threadSwitch((code_331)); \
        }

        /**
         * Execute a service operation for channel I/O.
         */
#define  executeCIO(context_332, op_334, channel_336, i1_338, i2_340, i3_342, i4_344, i5_346, i6_348, o1_350, o2_352) { int  context_333 = context_332;  int  op_335 = op_334;  int  channel_337 = channel_336;  int  i1_339 = i1_338;  int  i2_341 = i2_340;  int  i3_343 = i3_342;  int  i4_345 = i4_344;  int  i5_347 = i5_346;  int  i6_349 = i6_348;  Address  o1_351 = o1_350;  Address  o2_353 = o2_352;  \
            com_sun_squawk_ServiceOperation_context = (context_333); \
            com_sun_squawk_ServiceOperation_op      = (op_335); \
            com_sun_squawk_ServiceOperation_channel = (channel_337); \
            com_sun_squawk_ServiceOperation_i1      = (i1_339); \
            com_sun_squawk_ServiceOperation_i2      = (i2_341); \
            com_sun_squawk_ServiceOperation_i3      = (i3_343); \
            com_sun_squawk_ServiceOperation_i4      = (i4_345); \
            com_sun_squawk_ServiceOperation_i5      = (i5_347); \
            com_sun_squawk_ServiceOperation_i6      = (i6_349); \
            com_sun_squawk_ServiceOperation_o1      = (o1_351); \
            com_sun_squawk_ServiceOperation_o2      = (o2_353); \
            if (runningOnServiceThread) { \
                void cioExecute(void); \
                cioExecute(); \
            } else { \
                threadSwitchFor(com_sun_squawk_ServiceOperation_CHANNELIO); \
            } \
        }

        /*-----------------------------------------------------------------------*\
         *                               Debugging                               *
        \*-----------------------------------------------------------------------*/

#if SDA_DEBUGGER
#define SDA_DEBUG_PRINT false

INLINE Address frameOffsetAsPointer(Address stack_354, Offset fpOffset_355) {
            return Address_sub(Address_add((stack_354), getArrayLength((stack_354)) * HDR_BYTES_PER_WORD), (fpOffset_355));
        }

INLINE Offset framePointerAsOffset(Address stack_356, Address fpPointer_357) {
            return Address_diff(Address_add((stack_356), getArrayLength((stack_356)) * HDR_BYTES_PER_WORD), (fpPointer_357));
        }

#define  sda_checkStepPrim(actual_ip_358, actual_fp_360, actual_sp_362) { ByteAddress  actual_ip_359 = actual_ip_358;  UWordAddress  actual_fp_361 = actual_fp_360;  UWordAddress  actual_sp_363 = actual_sp_362;  \
            Address thread = com_sun_squawk_VMThread_currentThread; \
            Address step = com_sun_squawk_VMThread_step(thread); \
            Offset currentFO = framePointerAsOffset(ss, (actual_fp_361)); \
            Address startFP = frameOffsetAsPointer(ss, com_sun_squawk_Debugger_SingleStep_startFO(step)); \
            int state = com_sun_squawk_Debugger_SingleStep_state(step); \
 \
            if (!inSystemFrame(startFP, (Address)(actual_fp_361))) { \
                switch (state) { \
                    case com_sun_squawk_Debugger_SingleStep_DEFERRED: { \
                        /* A couple of optimizations for determining when we should go from DEFERRED to REQUESTED. \
                         * \
                         * 1) The last 'hit' was cancelled because it was in a class excluded by step request (via a ClassExclude modifier)0 \
                         * Each successive hit will fail until the current frame changes. \
                         * \
                         * Once the current frame changes, the state is reset to REQUESTED. \
                         * \
                         * 2) If the class of the method where the step was cancelled is the same as the \
                         * current method's class, break. \
                         */ \
 \
                        Offset reportedFO = com_sun_squawk_Debugger_SingleStep_reportedFO(step); \
                        if (currentFO == reportedFO) { \
                            /* Still in a method of an excluded class */ \
                            break; \
                        } else if (currentFO < reportedFO) { \
                            Address reportedFP = frameOffsetAsPointer(ss, reportedFO); \
                            Address reportedMP = getObject(reportedFP, FP_method); \
                            Address currentMP = getObject((actual_fp_361), FP_method); \
assumeInterp(((UWord)getObject(reportedMP, HDR_klass) & HDR_headerTagMask) == 0); \
assumeInterp(((UWord)getObject(currentMP, HDR_klass) & HDR_headerTagMask) == 0); \
                            { \
                                Address classOfReportedMP = getClass(reportedMP); \
                                Address classOfCurrentMP = getClass(currentMP); \
 \
                                if (classOfReportedMP == classOfCurrentMP) { \
                                    break; \
                                } \
                            } \
                        } \
 \
                        set_com_sun_squawk_Debugger_SingleStep_state(step, com_sun_squawk_Debugger_SingleStep_REQUESTED); \
                        set_com_sun_squawk_Debugger_SingleStep_reportedFO(step, (Offset) 0); \
                        set_com_sun_squawk_Debugger_SingleStep_reportedBCI(step, (Offset) 0); \
                        /* intentionally falls through to case com_sun_squawk_Debugger_SingleStep_REQUESTED */ \
                    } \
                    case com_sun_squawk_Debugger_SingleStep_REQUESTED: { \
                        Address mp = getObject((actual_fp_361), FP_method); \
                        boolean stepped = sda_handleSingleStep((actual_ip_359), (actual_fp_361), (actual_sp_363), mp, step); \
                        /* Report the step event */ \
                        if (stepped) { \
                            Offset currentBCI = Address_diff((actual_ip_359), mp); \
                            /*fprintf(stderr, format("Signaling STEP event hit @ currentFO: %A, currentBCI: %A\n"), currentFO, currentBCI);*/ \
                            /* We set the state here - otherwise the this method will check for step events when the handler is running */ \
                            set_com_sun_squawk_Debugger_SingleStep_state(step, com_sun_squawk_Debugger_SingleStep_HIT); \
                            set_com_sun_squawk_Debugger_SingleStep_reportedFO(step, currentFO); \
                            set_com_sun_squawk_Debugger_SingleStep_reportedBCI(step, currentBCI); \
 \
                            pushWord(currentFO); \
                            pushWord(currentBCI); \
                            callNoReset(com_sun_squawk_VM_reportStepEvent); \
                        } \
                        break; \
                    } \
                    default: { \
                        fatalInterpreterError("sda_checkStep(): Should not reach here\n"); \
                    } \
                } \
           } \
       }

        /**
         * Checks for stepping events.
         */
#define  sda_checkStep(actual_ip_364, actual_fp_366, actual_sp_368) { ByteAddress  actual_ip_365 = actual_ip_364;  UWordAddress  actual_fp_367 = actual_fp_366;  UWordAddress  actual_sp_369 = actual_sp_368;  \
            Address thread = com_sun_squawk_VMThread_currentThread; \
            Address step = com_sun_squawk_VMThread_step(thread); \
            int state; \
 \
            /* We proceed if: \
             *      1) step is not null and state is not STATE_HIT \
             *      2) operand stack is empty \
             *      3) opcode != EXTEND, EXTEND0, EXTEND_WIDE, CLASS_CLINIT, BBTARGET_APP, BBTARGET_SYS \
             */ \
            if (step != null && (state = com_sun_squawk_Debugger_SingleStep_state(step)) != com_sun_squawk_Debugger_SingleStep_HIT && \
                sda_isOperandStackEmpty((actual_fp_367), (actual_sp_369))) \
            { \
                int thisOpcode = getUByteTyped((actual_ip_365), 0, AddressType_BYTECODE); \
                switch (thisOpcode) { \
                    case OPC_EXTEND: \
                    case OPC_EXTEND0: \
                    case OPC_EXTEND_WIDE: \
                    case OPC_CLASS_CLINIT: \
                    case OPC_BBTARGET_APP: \
                    case OPC_BBTARGET_SYS: break; \
                    default: \
                        sda_checkStepPrim((actual_ip_365), (actual_fp_367), (actual_sp_369)); \
                        break; \
                } \
            } \
        }
/**
 * Handles the logic for determining if a step event has occurred when the frames are different.
 * We want to know if the startFO is still on the stack - this tells us that we are in a frame
 * that the startFO has called.
 *
 * If we are in STEP_OVER or STEP_OUT, we don't want this to cause an event, so we return false, otherwise we don't.
 * If we are in STEP_IN, the above test doesn't apply so we return true.
 */
boolean sda_handleSingleStepWhenFrameIsDifferent(UWordAddress actual_fp, Offset startFO, int depth) {
    boolean sendEvent = true;

    if (depth == com_sun_squawk_debugger_JDWP_StepDepth_OVER || depth == com_sun_squawk_debugger_JDWP_StepDepth_OUT) {
        UWordAddress prevFP = (UWordAddress)getObject(actual_fp, FP_returnFP);
        assume(prevFP > actual_fp);
        while (prevFP != null) {
            if (framePointerAsOffset(ss, prevFP) == startFO) {
#if SDA_DEBUG_PRINT
                fprintf(stderr, format("sda_handleSingleStep(): Found the frame where stepping was initiated\n"));
#endif
                sendEvent = false;
                break;
            }
            prevFP = (UWordAddress)getObject(prevFP, FP_returnFP);
        }
    }
    return sendEvent;
}

/**
 * Determines whether or not a requested step event has been completed and should be reported to
 * the attached debugger.
 */
boolean sda_handleSingleStep(ByteAddress actual_ip, UWordAddress actual_fp, UWordAddress actual_sp, Address mp, Address step) {
    Offset currentFO = framePointerAsOffset(ss, actual_fp);
    Offset startFO = com_sun_squawk_Debugger_SingleStep_startFO(step);
    int targetBCI = com_sun_squawk_Debugger_SingleStep_targetBCI(step);
    int size = com_sun_squawk_Debugger_SingleStep_size(step);
    int depth = com_sun_squawk_Debugger_SingleStep_depth(step);

    /* Squawk does -not- support com_sun_squawk_debugger_JDWP_StepSize_MIN */
    assume(size == com_sun_squawk_debugger_JDWP_StepSize_LINE);
    /* We're assuming that the current method pointer is not one invoked by the interpreter */
    assume(!isInterpreterInvoked(mp));

#if SDA_DEBUG_PRINT
    fprintf(stderr, format("sda_handleSingleStep(): targetBCI: %d, currentFO: %O, startFO: %O, size: %d, depth: %d\n"), targetBCI, currentFO, startFO, size, depth);
#endif

    if (targetBCI == -1) {
        /* Wait for the frame to change and that will mean we popped up to
         * the calling frame. (Or we called down to another frame, maybe...
         * should look into that possibility)
         */
        boolean sendEvent = false;
        if (currentFO != startFO) {
            sendEvent = sda_handleSingleStepWhenFrameIsDifferent(actual_fp, startFO, depth);
        }
#if SDA_DEBUG_PRINT
        if (sendEvent) {
            fprintf(stderr, format("sda_handleSingleStep(): targetBCI == -1 and step event occurred\n"));
        }
#endif
        return sendEvent;
    } else {
        Offset currentBCI = Address_diff(actual_ip, mp);
        if (currentFO == startFO) {
            Offset startBCI = com_sun_squawk_Debugger_SingleStep_startBCI(step);
            int dupBCI = com_sun_squawk_Debugger_SingleStep_dupBCI(step);
            int afterDupBCI = com_sun_squawk_Debugger_SingleStep_afterDupBCI(step);
#if SDA_DEBUG_PRINT
            dumpSteppingInfo(currentBCI, currentFO, step);
#endif
            /* if the frame is the same and we are not running to the
             * end of a function we need to see if we have reached
             * the location we are looking for
             */
            if (depth != com_sun_squawk_debugger_JDWP_StepDepth_OUT) {
                if (currentBCI == targetBCI) {
#if SDA_DEBUG_PRINT
                    fprintf(stderr, format("handleSingleStep() - CASE 1: we are at the target offset\n"));
#endif
                    /* At the target offset */
                    return true;
                } else if (currentBCI < startBCI && (dupBCI == -1 || afterDupBCI == -1)) {
#if SDA_DEBUG_PRINT
                    fprintf(stderr, format("handleSingleStep() - CASE 2: we are before the start offset \n"));
#endif
                    /* Reached an offset before the offset where stepping was initiated - there's also
                     * no duplicate information
                     */
                    return true;
                } else if (currentBCI > targetBCI && currentBCI > startBCI) {
                    /* Past the target offset and the offset where stepping was initiated */
                    if (dupBCI == -1 || afterDupBCI == -1) {
#if SDA_DEBUG_PRINT
                        fprintf(stderr, format("handleSingleStep() - CASE 3: we are after the target and start offsets and there is no duplicate offset\n"));
#endif
                        /* there's no duplicate information (like there would be in a for/while loop)
                         * Sometimes caused by a break in a switch/case.  We checked if the starting
                         * offset is equal to our current - if so don't issue an event otherwise we'll
                         * be in an infinite loop.
                         */
                        return true;
                    } else if (currentBCI >= dupBCI && currentBCI >= afterDupBCI) {
#if SDA_DEBUG_PRINT
                        fprintf(stderr, format("handleSingleStep() - CASE 4: we are after the target and start offsets and either at or past the duplicate offset\n"));
#endif
                        /* we are at or beyond the end of a for/while loop (doing the loop test). */
                        return true;
                    }
                }
            }
        } else {
            /* the frame has changed but we don't know whether we have gone into a function or gone up one.
             * if we look up the call stack and find the frame that originally set the request then we stepped
             * into a function, otherwise we stepped out
             */
            boolean sendEvent = sda_handleSingleStepWhenFrameIsDifferent(actual_fp, startFO, depth);

#if SDA_DEBUG_PRINT
            dumpSteppingInfo(currentBCI, currentFO, step);
            if (sendEvent) {
                fprintf(stderr, format("sda_handleSingleStep(): frame has changed, causing a step event\n"));
            }
#endif
            return sendEvent;
        }
    }
    return false;
}

        /**
         * Determines if the interpreter is at a breakpoint or end of a step and calls
         * into the appropriate Java routine if it is.
         */
#define  sda_checkBreakOrStep(actual_ip_370, actual_fp_372, actual_sp_374) { ByteAddress  actual_ip_371 = actual_ip_370;  UWordAddress  actual_fp_373 = actual_fp_372;  UWordAddress  actual_sp_375 = actual_sp_374;  \
            if (unlikely(sda_bp_set_or_stepping)) { \
                Address thread = com_sun_squawk_VMThread_currentThread; \
                Address hbp = com_sun_squawk_VMThread_hitBreakpoint(thread); \
                if (hbp == null) { \
                    boolean atBreakpoint = false; \
                    ByteAddress bp_ip; \
                    if (sda_breakpoints[0] != NULL) { \
                        int i = 0; \
                        while ((bp_ip = sda_breakpoints[i++]) != null) { \
                            if (bp_ip == (actual_ip_371)) { \
                                /* \
                                 * Call breakpoint reporting method - and leave stack as if current method called. \
                                 * com_sun_squawk_VM_reportBreakpoint()0 \
                                 */ \
                                Address mp      = getObject((actual_fp_373), FP_method); \
                                Offset currentBCI = Address_diff((actual_ip_371), mp); \
                                Offset currentFO = framePointerAsOffset(ss, (actual_fp_373)); \
                                /*fprintf(stderr, format("Hit breakpoint #%d: in method: %A, currentBCI: %O ip: %A currentFO: %O fp: %A\n"),  i, mp, currentBCI, (actual_ip_371), currentFO, (actual_fp_373));*/ \
 \
                                if (!sda_isOperandStackEmpty((actual_fp_373), (actual_sp_375))) { \
                                    fatalInterpreterError("Tried to report a breakpoint when operand stack was not empty.\n"); \
                                } \
 \
                                pushWord(currentFO); \
                                pushWord(currentBCI); \
                                call(com_sun_squawk_VM_reportBreakpoint); \
                                atBreakpoint = true; \
                                break; \
                            } \
                        } \
                    } \
                    if (!atBreakpoint) { \
                        sda_checkStep((actual_ip_371), (actual_fp_373), (actual_sp_375)); \
                    } \
                } else if (com_sun_squawk_HitBreakpoint_state(hbp) == com_sun_squawk_HitBreakpoint_BP_REPORTED) { \
                    /* \
                     * In the middle of reporting the breakpoint. If the current frame and BCI \
                     * is equal to the thread's saved frame and BCI, then we are done reporting the \
                     * breakpoint, and we clean up reporting, and execute the next instruction normally. \
                     */ \
                    if (framePointerAsOffset(ss, (actual_fp_373)) == com_sun_squawk_HitBreakpoint_hitOrThrowFO(hbp)) { \
                        Address mp      = getObject((actual_fp_373), FP_method); \
                        Offset currentBCI = Address_diff((actual_ip_371), mp); \
                        if (currentBCI == com_sun_squawk_HitBreakpoint_hitOrThrowBCI(hbp)) { \
                            set_com_sun_squawk_VMThread_hitBreakpoint(thread, null); \
                            /*fprintf(stderr, format("Done reporting breakpoint in method: %A, offset: %O\n"), mp, currentBCI);*/ \
                        } \
                    } \
                } \
            } \
        }
#endif /* SDA_DEBUGGER */

        /*-----------------------------------------------------------------------*\
         *                               Constants                               *
        \*-----------------------------------------------------------------------*/
        /**
         * Pushes a constant value.
         *
         * <p>
         * Java Stack: ... -> ..., INT
         * <p>
         *
         * @param n the integer value
         */
#define  do_const_n(n_376) { Offset  n_377 = n_376;  \
            pushInt((n_377)); \
        }

        /**
         * Pushes a constant null value.
         *
         * <p>
         * Java Stack: ... -> ..., INT
         * <p>
         *
         * @param n the integer value
         */
#define  do_const_null() {  \
            pushAddress(0); \
        }

        /**
         * Pushes a constant byte value.
         *
         * <p>
         * Java Stack: ... -> ..., INT
         * <p>
         */
#define  do_const_byte() {  \
            pushInt(fetchByte()); \
        }

        /**
         * Pushes a constant short value.
         *
         * <p>
         * Java Stack: ... -> ..., INT
         * <p>
         */
#define  do_const_short() {  \
            int fparm; \
            fetchShort(); \
            pushInt(fparm); \
        }

        /**
         * Pushes a constant char value.
         *
         * <p>
         * Java Stack: ... -> ..., INT
         * <p>
         */
#define  do_const_char() {  \
            int fparm; \
            fetchUShort(); \
            pushInt(fparm); \
        }

        /**
         * Pushes a constant int value.
         *
         * <p>
         * Java Stack: ... -> ..., INT
         * <p>
         */
#define  do_const_int() {  \
            int fparm; \
            fetchInt(); \
            pushInt(fparm); \
        }

        /**
         * Pushes a constant long value.
         *
         * <p>
         * Java Stack: ... -> ..., LONG
         * <p>
         */
#define  do_const_long() {  \
            jlong flparm; \
            fetchLong(); \
            pushLong(flparm); \
        }

        /**
         * Pushes a constant floa value.
         *
         * <p>
         * Java Stack: ... -> ..., FLOAT
         * <p>
         */
#define  do_const_float() {  \
            int fparm; \
            fetchInt(); \
            pushInt(fparm); \
        }

        /**
         * Pushes a constant double value.
         *
         * <p>
         * Java Stack: ... -> ..., DOUBLE
         * <p>
         */
#define  do_const_double() {  \
            jlong flparm; \
            fetchLong(); \
            pushLong(flparm); \
        }

        /**
         * Pushes a constant object value.
         *
         * <p>
         * Java Stack: ... -> ..., OOP
         * <p>
         *
         * @param n the index into the class object table
         */
#define  do_object_n(n_378) { int  n_379 = n_378;  \
            pushAddress(getKlassObject((n_379))); \
        }

        /**
         * Pushes a constant object value.
         *
         * <p>
         * Java Stack: ... -> ..., OOP
         * <p>
         */
#define  do_object() {  \
            do_object_n(iparm); \
        }


        /*-----------------------------------------------------------------------*\
         *                          Access to locals                             *
        \*-----------------------------------------------------------------------*/

        /**
         * Pushes a single word local.
         *
         * <p>
         * Java Stack: ... -> ..., VALUE
         * <p>
         *
         * @param n the index to local
         */
#define  do_load_n(n_380) { int  n_381 = n_380;  \
            pushAsType(getLocal((n_381)), getMutationType()); \
        }

        /**
         * Pushes a single word local.
         *
         * <p>
         * Java Stack: ... -> ..., VALUE
         * <p>
         */
#define  do_load() {  \
            do_load_n(iparm); \
        }

        /**
         * Pops a single word local.
         *
         * <p>
         * Java Stack: ..., VALUE -> ...
         * <p>
         *
         * @param n the index to local
         */
#define  do_store_n(n_382) { int  n_383 = n_382;  \
            setLocal((n_383), popAsType(getMutationType())); \
        }

        /**
         * Pops a single word local.
         *
         * <p>
         * Java Stack: ..., VALUE -> ...
         * <p>
         */
#define  do_store() {  \
            do_store_n(iparm); \
        }

        /**
         * Pushes a double word local.
         *
         * <p>
         * Java Stack: ... -> ..., LONG
         * <p>
         */
#define  do_load_i2() {  \
            if (TYPEMAP & SQUAWK_64) { \
                if (getMutationType() == AddressType_REF) { \
                    pushAsType(getLocal(iparm), AddressType_REF); \
                } else if (getMutationType() == AddressType_UWORD) { \
                    pushAsType(getLocal(iparm), AddressType_UWORD); \
                } else { \
                    pushLong(getLocalLong(iparm)); \
                } \
            } else { \
                pushLong(getLocalLong(iparm)); \
            } \
        }

        /**
         * Pops a double word local.
         *
         * <p>
         * Java Stack: ..., LONG -> ...
         * <p>
         */
#define  do_store_i2() {  \
            if (TYPEMAP & SQUAWK_64) { \
                if (getMutationType() == AddressType_REF) { \
                    setLocal(iparm, popAsType(AddressType_REF)); \
                } else if (getMutationType() == AddressType_UWORD) { \
                    setLocal(iparm, popAsType(AddressType_UWORD)); \
                } else { \
                    setLocalLong(iparm, popLong()); \
                } \
            } else { \
                setLocalLong(iparm, popLong()); \
            } \
        }

        /**
         * Increment a single word local.
         *
         * <p>
         * Java Stack: ... -> ...
         * <p>
         */
#define  do_inc() {  \
            setLocal(iparm, getLocal(iparm) + 1); \
        }

        /**
         * Decrement a single word local.
         *
         * <p>
         * Java Stack: ... -> ...
         * <p>
         */
#define  do_dec() {  \
            setLocal(iparm, getLocal(iparm) - 1); \
        }


        /*-----------------------------------------------------------------------*\
         *                         Access to parameters                          *
        \*-----------------------------------------------------------------------*/

        /**
         * Pushes a single word parm.
         *
         * <p>
         * Java Stack: ... -> ..., VALUE
         * <p>
         *
         * @param n the index to local
         */
#define  do_loadparm_n(n_384) { int  n_385 = n_384;  \
            pushAsType(getParm((n_385)), getMutationType()); \
        }

        /**
         * Pushes a single word parm.
         *
         * <p>
         * Java Stack: ... -> ..., VALUE
         * <p>
         */
#define  do_loadparm() {  \
            do_loadparm_n(iparm); \
        }

        /**
         * Pops a single word parm.
         *
         * <p>
         * Java Stack: ..., VALUE -> ...
         * <p>
         */
#define  do_storeparm_n(n_386) { int  n_387 = n_386;  \
            setParm((n_387), popAsType(getMutationType())); \
        }

        /**
         * Pops a single word parm.
         *
         * <p>
         * Java Stack: ..., VALUE -> ...
         * <p>
         */
#define  do_storeparm() {  \
            do_storeparm_n(iparm); \
        }

        /**
         * Pushes a double word parm.
         *
         * <p>
         * Java Stack: ... -> ..., LONG
         * <p>
         */
#define  do_loadparm_i2() {  \
            if (TYPEMAP & SQUAWK_64) { \
                if (getMutationType() == AddressType_REF) { \
                    pushAsType(getParm(iparm), AddressType_REF); \
                } else if (getMutationType() == AddressType_UWORD) { \
                    pushAsType(getParm(iparm), AddressType_UWORD); \
                } else { \
                    pushLong(getParmLong(iparm)); \
                } \
            } else { \
                pushLong(getParmLong(iparm)); \
            } \
        }

        /**
         * Pops a double word parm.
         *
         * <p>
         * Java Stack: ..., VALUE -> ...
         * <p>
         *
         */
#define  do_storeparm_i2() {  \
            if (TYPEMAP & SQUAWK_64) { \
                if (getMutationType() == AddressType_REF) { \
                    setParm(iparm, popAsType(AddressType_REF)); \
                } else if (getMutationType() == AddressType_UWORD) { \
                    setParm(iparm, popAsType(AddressType_UWORD)); \
                } else { \
                    setParmLong(iparm, popLong()); \
                } \
            } else { \
                setParmLong(iparm, popLong()); \
            } \
        }

        /**
         * Increment a single word parm.
         *
         * <p>
         * Java Stack: ... -> ...
         * <p>
         */
#define  do_incparm() {  \
            setParm(iparm, getParm(iparm) + 1); \
        }

        /**
         * Decrement a single word parm.
         *
         * <p>
         * Java Stack: ... -> ...
         * <p>
         */
#define  do_decparm() {  \
            setParm(iparm, getParm(iparm) - 1); \
        }


    /*-----------------------------------------------------------------------*\
     *                               Branching                               *
    \*-----------------------------------------------------------------------*/

        /**
         * Modifies the global 'tracing' flag if a user specified backward branch threshold
         * has been met. This function also stops the VM if another user specified
         * threshold has been met.
         */
INLINE void bbtarget_trace() {
#if TRACE
            jlong count;
            int low = (int)((branchCountLow + 1) & 0xFFFFFFFF);
            branchCountLow = low;
            if (low == 0) {
                UWord high = branchCountHigh + 1;
                branchCountHigh = high;
            }
            count = getBranchCount();
            if (statsFrequency != 0 && (count % statsFrequency) == 0) {
                printCacheStats();
            }
            if (count >= getTraceStart()) {
                com_sun_squawk_VM_tracing = true;
            }
            if (count >= getTraceEnd()) {
                fprintf(stderr, format("\n** Reached branch count limit %L **\n"), getBranchCount());
                stopVM(-1);
            }
#endif
        }


        /**
         * Backward branch target in system code.
         *
         * <p>
         * Java Stack:  _  ->  _
         * <p>
         */
#define  do_bbtarget_sys() {  \
            osbackbranch(); \
            bbtarget_trace(); \
            checkReferenceSlots(); \
        }

        /**
         * Backward branch target in application code.
         *
         * <p>
         * Java Stack:  _  ->  _
         * <p>
         */
#define  do_bbtarget_app() {  \
            do_bbtarget_sys(); \
            if (unlikely(bc++ >= 0)) { \
                bc = -TIMEQUANTA; \
                call(com_sun_squawk_VM_yield); \
            } \
        }

#ifdef FLASH_MEMORY
        /**
         * Force back branch count to zero so that reschedule will occur.
         * NB. This function is forced into a special section so that it can be put in RAM.
         */
		__attribute__ ((section (".textinram"))) void force_bb_expired() {
            bc = 0;
        }
#endif

        /**
         * Unconditional branch.
         *
         * <p>
         * Java Stack: ... -> ...  (Forward branches);
         * <p>
         * Java Stack:  _  ->  _   (Backward branches);
         * <p>
         */
#define  do_goto() {  \
            ip += iparm; \
        }

        /**
         * Gets the right hand operand for the comparison in an 'if...' instruction.
         *
         * @param zero  specifies if the right hand operand is 0 or is on the stack
         */
#define  rhs_o(zero_388) (   (zero_388) ? 0 : popAddress()  )
#define      rhs_i(zero_389) (   (zero_389) ? 0 : popInt()      )
#define    rhs_l(zero_390) (   (zero_390) ? 0 : popLong()     )

        /**
         * Conditional branch based on a comparison between object values.
         *
         * <p>
         * Java Stack: ..., LEFT_VALUE, [RIGHT_VALUE] -> ...  (Forward branches);
         * <p>
         * Java Stack:      LEFT_VALUE, [RIGHT_VALUE] ->  _   (Backward branches);
         * <p>
         *
         * @param zero  if true, then RIGHT_VALUE is given the value 0 (and thus not popped from the stack)
         */
#define  do_if_eq_o(zero_391) { boolean  zero_392 = zero_391;  Address rhs = rhs_o((zero_392)); if (popAddress() == rhs) do_goto(); }
#define  do_if_ne_o(zero_393) { boolean  zero_394 = zero_393;  Address rhs = rhs_o((zero_394)); if (popAddress() != rhs) do_goto(); }

        /**
         * Conditional branch based on a comparison between int values.
         *
         * <p>
         * Java Stack: ..., LEFT_VALUE, [RIGHT_VALUE] -> ...  (Forward branches);
         * <p>
         * Java Stack:      LEFT_VALUE, [RIGHT_VALUE] ->  _   (Backward branches);
         * <p>
         *
         * @param zero  if true, then RIGHT_VALUE is given the value 0 (and thus not popped from the stack)
         */
#define  do_if_eq_i(zero_395) { boolean  zero_396 = zero_395;  int rhs = rhs_i((zero_396)); if (popInt() == rhs) do_goto(); }
#define  do_if_ne_i(zero_397) { boolean  zero_398 = zero_397;  int rhs = rhs_i((zero_398)); if (popInt() != rhs) do_goto(); }
#define  do_if_lt_i(zero_399) { boolean  zero_400 = zero_399;  int rhs = rhs_i((zero_400)); if (popInt() <  rhs) do_goto(); }
#define  do_if_le_i(zero_401) { boolean  zero_402 = zero_401;  int rhs = rhs_i((zero_402)); if (popInt() <= rhs) do_goto(); }
#define  do_if_gt_i(zero_403) { boolean  zero_404 = zero_403;  int rhs = rhs_i((zero_404)); if (popInt() >  rhs) do_goto(); }
#define  do_if_ge_i(zero_405) { boolean  zero_406 = zero_405;  int rhs = rhs_i((zero_406)); if (popInt() >= rhs) do_goto(); }

        /**
         * Conditional branch based on a comparison between long values.
         *
         * <p>
         * Java Stack: ..., LEFT_VALUE, [RIGHT_VALUE] -> ...  (Forward branches);
         * <p>
         * Java Stack:      LEFT_VALUE, [RIGHT_VALUE] ->  _   (Backward branches);
         * <p>
         *
         * @param zero  if true, then RIGHT_VALUE is given the value 0 (and thus not popped from the stack)
         */
#define  do_if_eq_l(zero_407) { boolean  zero_408 = zero_407;  jlong rhs = rhs_l((zero_408)); if (popLong() == rhs) do_goto(); }
#define  do_if_ne_l(zero_409) { boolean  zero_410 = zero_409;  jlong rhs = rhs_l((zero_410)); if (popLong() != rhs) do_goto(); }
#define  do_if_lt_l(zero_411) { boolean  zero_412 = zero_411;  jlong rhs = rhs_l((zero_412)); if (popLong() <  rhs) do_goto(); }
#define  do_if_le_l(zero_413) { boolean  zero_414 = zero_413;  jlong rhs = rhs_l((zero_414)); if (popLong() <= rhs) do_goto(); }
#define  do_if_gt_l(zero_415) { boolean  zero_416 = zero_415;  jlong rhs = rhs_l((zero_416)); if (popLong() >  rhs) do_goto(); }
#define  do_if_ge_l(zero_417) { boolean  zero_418 = zero_417;  jlong rhs = rhs_l((zero_418)); if (popLong() >= rhs) do_goto(); }

        /**
         * Gets a table switch parameter.
         */
#define  getSwitchEntry(size_419) { int  size_420 = size_419;  \
            if ((size_420) == 2) { \
                fetchShort(); \
            } else { \
                fetchInt(); \
            } \
        }

        /**
         * General table switch.
         *
         * <p>
         * Java Stack: KEY ->  _
         * <p>
         *
         * @param  size  the size (in bytes) of an entry in the jump table
         */
#define  do_tableswitch(size_421) { int  size_422 = size_421;  \
            int fparm; \
            int key; \
            int low; \
            int high; \
 \
            /* \
             @TODO: FIX to not use mod (%)0 Very slow on ARM. \
             */ \
            /* \
             * Skip the padding. \
             */ \
            while ((((UWord)ip) % (size_422)) != 0) { \
                fetchByte(); \
            } \
 \
            /* \
             * Read the low and high bound and the default case. \
             */ \
            getSwitchEntry((size_422)); \
            low   = fparm; \
            getSwitchEntry((size_422)); \
            high  = fparm; \
            getSwitchEntry((size_422)); \
            iparm = fparm; \
 \
            /* \
             * Get the key. \
             */ \
            key = popInt(); \
 \
            /* \
             * Calculate the new IP. \
             */ \
            if (key >= low && key <= high) { \
                if ((size_422) == 4) { \
                    iparm = getIntTyped(ip, key-low, AddressType_ANY); \
                } else { \
                    iparm = getShortTyped(ip, key-low, AddressType_ANY); \
                } \
            } \
 \
            /* \
             * Update the IP. \
             */ \
            do_goto(); \
        }


        /*-----------------------------------------------------------------------*\
         *                          Static field loads                           *
        \*-----------------------------------------------------------------------*/

        /**
         * Gets the class that a putstatic or getstatic instruction operates on.
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         * @return the class whose static field is being accessed
         */
#define  getStaticFieldClass(inCP_423) (  \
             (inCP_423) ? getCP() : popAddress()  \
        )

        /**
         * Loads a value from a static int field.
         *
         * <p>
         * Java Stack: CLASS -> VALUE
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_getstatic_i(inCP_424) { boolean  inCP_425 = inCP_424;  \
            Address klass = getStaticFieldClass((inCP_425)); \
            Address state = VM_getClassStateREAL(klass); \
            if (state != null) { \
                checkReferenceSlots(); \
                boundsAssume(state, iparm); \
                pushInt(getUWord(state, iparm)); \
            } else { \
                pushAddress(klass); \
                pushInt(iparm); \
                call(com_sun_squawk_VM_getStaticInt); \
            } \
        }

        /**
         * Loads a value from a static long field.
         *
         * <p>
         * Java Stack: CLASS -> VALUE
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_getstatic_l(inCP_426) { boolean  inCP_427 = inCP_426;  \
            Address klass = getStaticFieldClass((inCP_427)); \
            Address state = VM_getClassStateREAL(klass); \
            if (state != null) { \
                checkReferenceSlots(); \
                boundsAssume(state, iparm); \
                pushLong(getLongAtWord(state, iparm)); \
            } else { \
                pushAddress(klass); \
                pushInt(iparm); \
                call(com_sun_squawk_VM_getStaticLong); \
            } \
        }

        /**
         * Loads a value from a static object field.
         *
         * <p>
         * Java Stack: CLASS -> VALUE
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_getstatic_o(inCP_428) { boolean  inCP_429 = inCP_428;  \
            Address klass = getStaticFieldClass((inCP_429)); \
            Address state = VM_getClassStateREAL(klass); \
            if (state != null) { \
                checkReferenceSlots(); \
                boundsAssume(state, iparm); \
                pushAddress(getObject(state, iparm)); \
            } else { \
                pushAddress(klass); \
                pushInt(iparm); \
                call(com_sun_squawk_VM_getStaticOop); \
            } \
        }


        /**
         * Loads a value from a static float field.
         *
         * <p>
         * Java Stack: CLASS -> VALUE
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_getstatic_f(inCP_430) { boolean  inCP_431 = inCP_430;  \
            do_getstatic_i((inCP_431)); \
        }

        /**
         * Loads a value from a static double field.
         *
         * <p>
         * Java Stack: CLASS -> VALUE
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_getstatic_d(inCP_432) { boolean  inCP_433 = inCP_432;  \
            do_getstatic_l((inCP_433)); \
        }


        /*-----------------------------------------------------------------------*\
         *                          Static field stores                          *
        \*-----------------------------------------------------------------------*/

        /**
         * Stores a value to a static int field.
         *
         * <p>
         * Java Stack: VALUE, CLASS -> _
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_putstatic_i(inCP_434) { boolean  inCP_435 = inCP_434;  \
            Address klass = getStaticFieldClass((inCP_435)); \
            Address state = VM_getClassStateREAL(klass); \
            if (state != null) { \
                checkReferenceSlots(); \
                boundsAssume(state, iparm); \
                setUWord(state, iparm, popInt()); \
            } else { \
                pushAddress(klass); \
                pushInt(iparm); \
                call(com_sun_squawk_VM_putStaticInt); \
            } \
        }

        /**
         * Stores a value to a static long field.
         *
         * <p>
         * Java Stack: VALUE, CLASS -> _
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_putstatic_l(inCP_436) { boolean  inCP_437 = inCP_436;  \
            Address klass = getStaticFieldClass((inCP_437)); \
            Address state = VM_getClassStateREAL(klass); \
            if (state != null) { \
                checkReferenceSlots(); \
                boundsAssume(state, iparm); \
                setLongAtWord(state, iparm, popLong()); \
            } else { \
                pushAddress(klass); \
                pushInt(iparm); \
                call(com_sun_squawk_VM_putStaticLong); \
            } \
        }

        /**
         * Stores a value to a static object field.
         *
         * <p>
         * Java Stack: VALUE, CLASS -> _
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_putstatic_o(inCP_438) { boolean  inCP_439 = inCP_438;  \
            Address klass = getStaticFieldClass((inCP_439)); \
            Address state = VM_getClassStateREAL(klass); \
            if (state != null) { \
                checkReferenceSlots(); \
                boundsAssume(state, iparm); \
                setObjectAndUpdateWriteBarrier(state, iparm, popAddress()); \
            } else { \
                pushAddress(klass); \
                pushInt(iparm); \
                call(com_sun_squawk_VM_putStaticOop); \
            } \
        }


        /**
         * Stores a value to a static float field.
         *
         * <p>
         * Java Stack: VALUE, CLASS -> _
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_putstatic_f(inCP_440) { boolean  inCP_441 = inCP_440;  \
            do_putstatic_i((inCP_441)); \
        }

        /**
         * Stores a value to a static double field.
         *
         * <p>
         * Java Stack: VALUE, CLASS -> _
         * <p>
         *
         * @param inCP    specifies if the class whose field to be accessed is
         *                the current class or is on the stack
         */
#define  do_putstatic_d(inCP_442) { boolean  inCP_443 = inCP_442;  \
            do_putstatic_l((inCP_443)); \
        }


        /*-----------------------------------------------------------------------*\
         *                         Instance field access                         *
        \*-----------------------------------------------------------------------*/

        /**
         * Check for a null pointer.
         *
         * @param oop the pointer
         * @param checkSlots true if slot checking should be performed
         */
#define  nullCheckPrim(oop_444, checkSlots_446) { Address  oop_445 = oop_444;  boolean  checkSlots_447 = checkSlots_446;  \
            if ((checkSlots_447)) { \
                checkReferenceSlots(); \
            } \
            if ((oop_445) == 0) { \
 \
/*              --- TEST: fatalInterpreterError("NPE, time to freak out!");*/ \
                goto throw_nullPointerException; \
 \
 \
 \
 \
 \
            } \
        }

        /**
         * Check for a null pointer.
         *
         * @param oop the pointer
         */
#define  nullCheck(oop_448) { Address  oop_449 = oop_448;  \
            nullCheckPrim((oop_449), true); \
        }

        /**
         * Check for a null pointer or an array bounds overflow.
         *
         * @param oop the array
         * @param index the index to check
         */
#define  boundsCheck(oop_450, index_452) { Address  oop_451 = oop_450;  int  index_453 = index_452;  \
            int lth; \
            nullCheck((oop_451)); \
            lth = (int)getArrayLength((oop_451)); \
            /* use the unsigned bounds check trick: */ \
            if ((unsigned int) (index_453) >= (unsigned int)lth) { \
                /*  ((index_453) < 0 || (index_453) >= lth) */ \
 \
                com_sun_squawk_VM_reportedIndex = (index_453); \
                com_sun_squawk_VM_reportedArray = (oop_451); \
                goto throw_arrayIndexOutOfBoundsException; \
 \
 \
 \
 \
 \
            } \
        }

        /**
         * Gets the object that a putfield or getfield instruction operates on.
         *
         * @param oopIn0  specifies if the object whose field to be accessed is
         *                in parameter 0 or is on the stack
         * @return the object
         */
#define  getInstanceFieldOop(oopIn0_454) (  \
             (oopIn0_454) ? (Address)getParmTyped(0, AddressType_REF) : popAddress()  \
        )

        /*-----------------------------------------------------------------------*\
         *                         Instance field loads                          *
        \*-----------------------------------------------------------------------*/

        /**
         * Loads a value from an int or long instance field that is being used
         * as an Address, UWord or Offset.
         *
         * @param oop   the object containing the field
         * @param flag  return immediately if false
         */
#define  getfield_ref_or_uword(oop_455, flag_457) { Address  oop_456 = oop_455;  boolean  flag_458 = flag_457;  \
            if (TYPEMAP && (flag_458)) { \
                if (getMutationType() == AddressType_REF) { \
                    pushAddress(getObject((oop_456), iparm)); \
                    nextbytecode(); /* completes instruction execution */ \
                } \
                if (getMutationType() == AddressType_UWORD) { \
                    pushWord(getUWord((oop_456), iparm)); \
                    nextbytecode(); /* completes instruction execution */ \
                } \
            } \
            /* \
             * The field really stores an int or long and so fall back to the \
             * properly typed way of loading these values \
             */ \
        }

        /**
         * Loads a value from a byte instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_b(oopIn0_459) { boolean  oopIn0_460 = oopIn0_459;  \
            Address oop = getInstanceFieldOop((oopIn0_460)); \
            nullCheckPrim(oop, true); \
            pushInt(getByte(oop, iparm)); \
        }

        /**
         * Loads a value from a short instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_s(oopIn0_461) { boolean  oopIn0_462 = oopIn0_461;  \
            Address oop = getInstanceFieldOop((oopIn0_462)); \
            nullCheckPrim(oop, true); \
            pushInt(getShort(oop, iparm)); \
        }

        /**
         * Loads a value from a char instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_c(oopIn0_463) { boolean  oopIn0_464 = oopIn0_463;  \
            Address oop = getInstanceFieldOop((oopIn0_464)); \
            nullCheckPrim(oop, true); \
            pushInt(getUShort(oop, iparm)); \
        }

        /**
         * Loads a value from an int instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_i(oopIn0_465) { boolean  oopIn0_466 = oopIn0_465;  \
            Address oop = getInstanceFieldOop((oopIn0_466)); \
            nullCheckPrim(oop, true); \
            getfield_ref_or_uword(oop, !SQUAWK_64); \
            pushInt(getInt(oop, iparm)); \
        }

        /**
         * Loads a value from an object instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_o(oopIn0_467) { boolean  oopIn0_468 = oopIn0_467;  \
            Address oop = getInstanceFieldOop((oopIn0_468)); \
            nullCheckPrim(oop, true); \
            pushAddress(getObject(oop, iparm)); \
        }

        /**
         * Loads a value from a long instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_l(oopIn0_469) { boolean  oopIn0_470 = oopIn0_469;  \
            Address oop = getInstanceFieldOop((oopIn0_470)); \
            nullCheckPrim(oop, true); \
            getfield_ref_or_uword(oop, SQUAWK_64); \
            pushLong(getLongAtWord(oop, iparm)); \
        }


        /**
         * Loads a value from a float instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_f(oopIn0_471) { boolean  oopIn0_472 = oopIn0_471;  \
            Address oop = getInstanceFieldOop((oopIn0_472)); \
            nullCheckPrim(oop, true); \
            pushInt(getInt(oop, iparm)); \
        }

        /**
         * Loads a value from a double instance field.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., VALUE  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., -> ..., VALUE  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_getfield_d(oopIn0_473) { boolean  oopIn0_474 = oopIn0_473;  \
            Address oop = getInstanceFieldOop((oopIn0_474)); \
            nullCheckPrim(oop, true); \
            pushLong(getLongAtWord(oop, iparm)); \
        }


        /*-----------------------------------------------------------------------*\
         *                         Instance field stores                         *
        \*-----------------------------------------------------------------------*/

        /**
         * Stores a value to an int or long instance field that is being used
         * as an Address, UWord or Offset.
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  putfield_ref_or_uword(oopIn0_475, flag_477) { boolean  oopIn0_476 = oopIn0_475;  boolean  flag_478 = flag_477;  \
            if (TYPEMAP && (flag_478)) { \
                if (getMutationType() == AddressType_REF) { \
                    Address value = popAddress(); \
                    Address oop = getInstanceFieldOop((oopIn0_476)); \
                    nullCheckPrim(oop, !(oopIn0_476)); \
                    setObject(oop, iparm, value); \
                    nextbytecode(); \
                } else if (getMutationType() == AddressType_UWORD) { \
                    UWord value = popWord(); \
                    Address oop = getInstanceFieldOop((oopIn0_476)); \
                    nullCheckPrim(oop, !(oopIn0_476)); \
                    setUWord(oop, iparm, value); \
                    nextbytecode(); \
                } \
            } \
        }

        /**
         * Stores a value to a byte instance field.
         *
         * <p>
         * Java Stack: ..., OOP, VALUE -> ...,  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., VALUE -> ...,  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_putfield_b(oopIn0_479) { boolean  oopIn0_480 = oopIn0_479;  \
            int value = popInt(); \
            Address oop = getInstanceFieldOop((oopIn0_480)); \
            nullCheckPrim(oop, !(oopIn0_480)); \
            setByte(oop, iparm, value); \
        }

        /**
         * Stores a value to a short or char instance field.
         *
         * <p>
         * Java Stack: ..., OOP, VALUE -> ...,  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., VALUE -> ...,  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_putfield_s(oopIn0_481) { boolean  oopIn0_482 = oopIn0_481;  \
            int value = popInt(); \
            Address oop = getInstanceFieldOop((oopIn0_482)); \
            nullCheckPrim(oop, !(oopIn0_482)); \
            setShort(oop, iparm, value); \
        }

        /**
         * Stores a value to an int instance field.
         *
         * <p>
         * Java Stack: ..., OOP, VALUE -> ...,  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., VALUE -> ...,  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_putfield_i(oopIn0_483) { boolean  oopIn0_484 = oopIn0_483;  \
            putfield_ref_or_uword((oopIn0_484), !SQUAWK_64); \
            { \
                int value = popInt(); \
                Address oop = getInstanceFieldOop((oopIn0_484)); \
                nullCheckPrim(oop, !(oopIn0_484)); \
                setInt(oop, iparm, value); \
            } \
        }

        /**
         * Stores a value to an object instance field.
         *
         * <p>
         * Java Stack: ..., OOP, VALUE -> ...,  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., VALUE -> ...,  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_putfield_o(oopIn0_485) { boolean  oopIn0_486 = oopIn0_485;  \
            Address value = popAddress(); \
            Address oop = getInstanceFieldOop((oopIn0_486)); \
            nullCheckPrim(oop, !(oopIn0_486)); \
            setObjectAndUpdateWriteBarrier(oop, iparm, value); \
        }

        /**
         * Stores a value to a long instance field.
         *
         * <p>
         * Java Stack: ..., OOP, VALUE -> ...,  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., VALUE -> ...,  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_putfield_l(oopIn0_487) { boolean  oopIn0_488 = oopIn0_487;  \
            putfield_ref_or_uword((oopIn0_488), SQUAWK_64); \
            { \
                jlong value = popLong(); \
                Address oop = getInstanceFieldOop((oopIn0_488)); \
                nullCheckPrim(oop, !(oopIn0_488)); \
                setLongAtWord(oop, iparm, value); \
            } \
        }


        /**
         * Stores a value to a float instance field.
         *
         * <p>
         * Java Stack: ..., OOP, VALUE -> ...,  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., VALUE -> ...,  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_putfield_f(oopIn0_489) { boolean  oopIn0_490 = oopIn0_489;  \
            int value = popInt(); \
            Address oop = getInstanceFieldOop((oopIn0_490)); \
            nullCheckPrim(oop, !(oopIn0_490)); \
            setInt(oop, iparm, value); \
        }

        /**
         * Stores a value to a double instance field.
         *
         * <p>
         * Java Stack: ..., OOP, VALUE -> ...,  (if oopIn0 is false)
         * <p>
         * Java Stack: ..., VALUE -> ...,  (if oopIn0 is true)
         * <p>
         *
         * @param oopIn0  specifies if the object whose field to be loaded is
         *                in parameter 0 or is on the stack
         */
#define  do_putfield_d(oopIn0_491) { boolean  oopIn0_492 = oopIn0_491;  \
            jlong value = popLong(); \
            Address oop = getInstanceFieldOop((oopIn0_492)); \
            nullCheckPrim(oop, !(oopIn0_492)); \
            setLongAtWord(oop, iparm, value); \
        }


        /*-----------------------------------------------------------------------*\
         *                           Array loads                                 *
        \*-----------------------------------------------------------------------*/

        /**
         * Loads a value from an int or long array that is being used to store Address,
         * UWord or Offset values.
         *
         * @param index  the index of the element to load
         * @param oop    the address of the array
         * @param flag   return immediately if false
         */
#define  aload_ref_or_uword(index_493, oop_495, flag_497) { int  index_494 = index_493;  Address  oop_496 = oop_495;  boolean  flag_498 = flag_497;  \
            if (TYPEMAP && (flag_498)) { \
                if (getMutationType() == AddressType_REF) { \
                    pushAddress(getObject((oop_496), (index_494))); \
                    nextbytecode(); \
                } \
                if (getMutationType() == AddressType_UWORD) { \
                    pushWord(getUWord((oop_496), (index_494))); \
                    nextbytecode(); \
                } \
            } \
        }

        /**
         * Loads an element from a byte array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_b() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            pushInt(getByte(oop, index)); \
        }

        /**
         * Loads an element from a short array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_s() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            pushInt(getShort(oop, index)); \
        }

        /**
         * Loads an element from a char array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_c() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            pushInt(getUShort(oop, index)); \
        }

        /**
         * Loads an element from an integer array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_i() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            aload_ref_or_uword(index, oop, !SQUAWK_64); \
            pushInt(getInt(oop, index)); \
        }

        /**
         * Loads an element from an object array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_o() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            pushAddress(getObject(oop, index)); \
        }

        /**
         * Loads an element from a long array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_l() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            aload_ref_or_uword(index, oop, SQUAWK_64); \
            pushLong(getLong(oop, index)); \
        }


        /**
         * Loads an element from a float array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_f() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            pushInt(getInt(oop, index)); \
        }

        /**
         * Loads an element from a double array.
         *
         * <p>
         * Java Stack: ..., OOP, INT -> ..., VALUE
         * <p>
         */
#define  do_aload_d() {  \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            pushLong(getLong(oop, index)); \
        }


        /*-----------------------------------------------------------------------*\
         *                           Array stores                                *
        \*-----------------------------------------------------------------------*/

#define  astore_ref_or_word() {  \
            if (TYPEMAP) { \
                if (getMutationType() == AddressType_REF) { \
                    Address value = popAddress(); \
                    int index   = popInt(); \
                    Address oop = popAddress(); \
                    boundsCheck(oop, index); \
                    setObject(oop, index, value); \
                    nextbytecode(); \
                } else if (getMutationType() == AddressType_UWORD) { \
                    UWord value = popWord(); \
                    int index   = popInt(); \
                    Address oop = popAddress(); \
                    boundsCheck(oop, index); \
                    setUWord(oop, index, value); \
                    nextbytecode(); \
                } \
            } \
        }

        /**
         * Stores an element to a byte array.
         *
         * <p>
         * Java Stack: ..., OOP, INT, VALUE -> ...
         * <p>
         */
#define  do_astore_b() {  \
            int value   = popInt(); \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            setByte(oop, index, value); \
        }

        /**
         * Stores an element to a short array.
         *
         * <p>
         * Java Stack: ..., OOP, INT, VALUE -> ...
         * <p>
         */
#define  do_astore_s() {  \
            int value   = popInt(); \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            setShort(oop, index, value); \
        }

        /**
         * Stores an element to an int array.
         *
         * <p>
         * Java Stack: ..., OOP, INT, VALUE -> ...
         * <p>
         */
#define  do_astore_i() {  \
            astore_ref_or_word(); \
            { \
                int value   = popInt(); \
                int index   = popInt(); \
                Address oop = popAddress(); \
                boundsCheck(oop, index); \
                setInt(oop, index, value); \
            } \
        }

        /**
         * Stores an element to a long array.
         *
         * <p>
         * Java Stack: ..., OOP, INT, VALUE -> ...
         * <p>
         */
#define  do_astore_l() {  \
            astore_ref_or_word(); \
            { \
                jlong value = popLong(); \
                int index   = popInt(); \
                Address oop = popAddress(); \
                boundsCheck(oop, index); \
                setLong(oop, index, value); \
            } \
        }

        /**
         * Stores an element to an object array.
         *
         * <p>
         * Java Stack:      OOP, INT, VALUE -> _
         * <p>
         */
#define  do_astore_o() {  \
            Address value  = popAddress(); \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            if ((value != 0) \
                 && (com_sun_squawk_Klass_id(getClass(oop)) != CID_OBJECT_ARRAY) \
                 && VM_arrayOopStoreCheck(oop, index, value)) { \
                    call(com_sun_squawk_VM_arrayStoreException); \
            } else { \
                setObjectAndUpdateWriteBarrier(oop, index, value); \
            } \
        }


        /**
         * Stores an element to a float array.
         *
         * <p>
         * Java Stack: ..., OOP, INT, VALUE -> ...
         * <p>
         */
#define  do_astore_f() {  \
            int value   = popInt(); \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            setInt(oop, index, value); \
        }

        /**
         * Stores an element to a double array.
         *
         * <p>
         * Java Stack: ..., OOP, INT, VALUE -> ...
         * <p>
         */
#define  do_astore_d() {  \
            jlong value = popLong(); \
            int index   = popInt(); \
            Address oop = popAddress(); \
            boundsCheck(oop, index); \
            setLong(oop, index, value); \
        }


        /*-----------------------------------------------------------------------*\
         *                           Invoke instructions                         *
        \*-----------------------------------------------------------------------*/

        /**
         * invokestatic.
         *
         * Java Stack: [[... arg2], arg1], CLASS -> [VALUE]
         * <p>
         */
#define  do_invokestatic() {  \
            Address cls = popAddress(); \
            call(getStaticMethod(cls, iparm)); \
        }

        /**
         * invokesuper.
         *
         * Java Stack: [[... arg2], arg1], OOP, CLASS -> [VALUE]
         * <p>
         */
#define  do_invokesuper() {  \
            Address cls = popAddress(); \
            Address obj; \
            resetStackPointerIfNotRevParms(); \
            obj = (Address)peek(); \
            nullCheck(obj); \
            callNoReset(getVirtualMethod(cls, iparm)); \
        }

        /**
         * invokevirtual.
         *
         * Java Stack: [[... arg2], arg1], OOP -> [VALUE]
         * <p>
         */
#define  do_invokevirtual() {  \
            Address obj; \
            Address cls; \
            resetStackPointerIfNotRevParms(); \
            obj = (Address)peek(); \
            nullCheck(obj); \
            assumeInterp(((UWord)getObject(obj, HDR_klass) & HDR_headerTagMask) == 0); \
            cls = getClassOrAssociation(obj); \
            callNoReset(getVirtualMethod(cls, iparm)); \
        }
        
        /**
         * findslot.
         *
         * <p>
         * Java Stack: OOP, CLASS -> VSLOT
         * <p>
         */
#define  do_findslot() {  \
            Address interfaceKlass = popAddress(); \
            Address oop = popAddress(); \
            int slot; \
 \
            nullCheck(oop); \
            slot = VM_findSlot(oop, interfaceKlass, iparm); \
            if (likely(slot >= 0)) { \
                pushInt(slot); \
            } else { \
                call(com_sun_squawk_VM_abstractMethodError); \
            } \
        }

        /**
         * invokeslot.
         *
         * Java Stack: [[... arg2], arg1], OOP, VSLOT -> [VALUE] (Stack grows down)
         */
#define  do_invokeslot() {  \
            iparm = popInt(); \
            do_invokevirtual(); \
        }


INLINE void incExtends(int slots_500) {
#if TRACE
            total_extends++;
            total_slots += (slots_500);
#endif
        }


        /**
         * Extends the call stack if necessary.
         *
         * <p>
         * Java Stack: _ -> _
         * <p>
         */
#define  extendStack(mp_501, slotsToClear_503) { Address  mp_502 = mp_501;  int  slotsToClear_504 = slotsToClear_503;  \
            Address mp = (mp_502); \
            int nlocals, nstack; \
            assumeInterp(com_sun_squawk_VM_extendsEnabled); \
assumeInterp(inCode(mp)); \
            downPushAddress(fp);                        /* Save caller's frame pointer. */ \
            downPushAddress(mp);                        /* Method address.              */ \
            fp = sp;                                    /* Setup new frame pointer.     */ \
            assumeInterp(getMP() == mp); \
            nlocals = getLocalCount(mp); \
            nstack  = getStackCount(mp); \
            assumeInterp((slotsToClear_504) < nlocals); \
            incExtends((slotsToClear_504)); \
            if (likely((fp - nlocals - nstack) > (sl + FP_FIXED_FRAME_SIZE))) { \
                UWordAddress oldsp = sp; \
                int delta = nlocals; \
 \
 \
 \
 \
                resetStackPointerFromDelta(delta); \
                if (CHECK_SLOT_CLEARING) { \
                    int slotsToZap = nlocals - 1; \
                    UWordAddress oldsp2 = oldsp; \
                    while (slotsToZap > 0) { \
                        setUWord(--oldsp2, 0, 0xDEAFBEEF);            /* Write a bad value */ \
                        setType(oldsp2, AddressType_ANY, sizeof(UWord)); \
                        slotsToZap = slotsToZap - 1; \
                    } \
                } \
                while ((slotsToClear_504) > 0) { \
                    setUWord(--oldsp, 0, 0);            /* zero local variables that need clearing */ \
                    setType(oldsp, AddressType_ANY, sizeof(UWord)); \
                    (slotsToClear_504) = (slotsToClear_504) - 1; \
                } \
            } else { \
                int overflow = (sl + FP_FIXED_FRAME_SIZE) - (fp - nlocals - nstack); \
                assumeInterp(getUWord(ss, SC_guard) == 0); \
                if (com_sun_squawk_GC_GC_TRACING_SUPPORTED && com_sun_squawk_GC_traceFlags > 1) { \
                    fprintf(stderr, format( \
                                            "*** Extending stack *** (stack size=%d, remaining stack=%d, nlocals=%d, nstack=%d, slotsToClear=%d, mp=0x%x)\n"), \
                                            getArrayLength(ss), \
                                            sp - sl, \
                                            nlocals, \
                                            nstack, \
                                            (slotsToClear_504), \
                                            (int)mp \
                                          ); \
                } \
                if (usingServiceStack()) { \
                    fatalInterpreterError("cannot extend service stack"); \
                } \
                if (!com_sun_squawk_VM_extendsEnabled) { \
                    fatalInterpreterError("trying to extend stack when extension disabled"); \
                } \
 \
                /* Pass the minimum amount (in words) by which the stack must grow. */ \
                com_sun_squawk_ServiceOperation_i1 = overflow; \
 \
                if (TRACE) { \
                    printStackTracePrim(-1, ip, fp, "extending stack", null); \
                } \
 \
                threadSwitchFor(com_sun_squawk_ServiceOperation_EXTEND); \
            } \
        }

        /**
         * Check that there are no 0xDEAFBEEF words in the local variables.
         */
#define  checkReferenceSlots() {  \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
        }

        /**
         * Extend the activation record.
         *
         * <p>
         * Java Stack: _ -> _
         * <p>
         */
#define  do_extend() {  \
            Address mp = Address_sub(ip, 2); \
            extendStack(mp, iparm); \
        }

        /**
         * Extend the activation record.
         *
         * <p>
         * Java Stack: _ -> _
         * <p>
         */
#define  do_extend0() {  \
            Address mp = Address_sub(ip, 1); \
            extendStack(mp, 0); \
        }

        /*-----------------------------------------------------------------------*\
         *                               Return                                  *
        \*-----------------------------------------------------------------------*/

        /**
         * Executes a method return where the int or long value being returned is being used
         * as an Address, UWord or Offset.
         *
         * @param flag  return immediately if false
         */
#define  return_ref_or_uword(flag_505) { boolean  flag_506 = flag_505;  \
            if (TYPEMAP && (flag_506)) { \
                if (getMutationType() == AddressType_REF) { \
                    Address res; \
                    ip = (ByteAddress)getObject(fp, FP_returnIP); \
                    res = popAddress(); \
                    fp = (UWordAddress)getObject(fp, FP_returnFP); \
                    resetStackPointer(); \
                    pushAddress(res); \
                    nextbytecode(); \
                } \
                if (getMutationType() == AddressType_UWORD) { \
                    UWord res; \
                    ip = (ByteAddress)getObject(fp, FP_returnIP); \
                    res = popWord(); \
                    fp = (UWordAddress)getObject(fp, FP_returnFP); \
                    resetStackPointer(); \
                    pushWord(res); \
                    nextbytecode(); \
                } \
            } \
        }

        /**
         * Executes a return from a method that returns an int value.
         *
         * <p>
         * Java Stack: [VALUE] -> [VALUE]
         * <p>
         */
#define  do_return_i() {  \
            return_ref_or_uword(!SQUAWK_64); \
            { \
                int res = popInt(); \
                ip = (ByteAddress)getObject(fp, FP_returnIP); \
                fp = (UWordAddress)getObject(fp, FP_returnFP); \
                assumeInterp(inCode(ip)); \
                resetStackPointer(); \
                pushInt(res); \
            } \
        }

        /**
         * Executes a return from a method that returns a long value.
         *
         * <p>
         * Java Stack: [VALUE] -> [VALUE]
         * <p>
         */
#define  do_return_l() {  \
            return_ref_or_uword(SQUAWK_64); \
            { \
                jlong res = popLong(); \
                ip = (ByteAddress)getObject(fp, FP_returnIP); \
                fp = (UWordAddress)getObject(fp, FP_returnFP); \
                assumeInterp(inCode(ip)); \
                resetStackPointer(); \
                pushLong(res); \
            } \
        }

        /**
         * Executes a return from a method that returns an object value.
         *
         * <p>
         * Java Stack: [VALUE] -> [VALUE]
         * <p>
         */
#define  do_return_o() {  \
            Address res = popAddress(); \
            ip = (ByteAddress)getObject(fp, FP_returnIP); \
            fp = (UWordAddress)getObject(fp, FP_returnFP); \
            assumeInterp(inCode(ip)); \
            resetStackPointer(); \
            pushAddress(res); \
        }

        /**
         * Executes a return from a method that does not return a value.
         *
         * <p>
         * Java Stack: [VALUE] -> _
         * <p>
         */
#define  do_return_v() {  \
            ip = (ByteAddress)getObject(fp, FP_returnIP); \
            fp = (UWordAddress)getObject(fp, FP_returnFP); \
            assumeInterp(inCode(ip)); \
            resetStackPointer(); \
        }


        /**
         * Executes a return from a method that returns a float value.
         *
         * <p>
         * Java Stack: [VALUE] -> [VALUE]
         * <p>
         */
#define  do_return_f() {  \
            do_return_i(); \
        }

        /**
         * Executes a return from a method that returns a double value.
         *
         * <p>
         * Java Stack: [VALUE] -> [VALUE]
         * <p>
         */
#define  do_return_d() {  \
            do_return_l(); \
        }


        /*-----------------------------------------------------------------------*\
         *                               Pop                                     *
        \*-----------------------------------------------------------------------*/

        /**
         * Pops one word from the Java stack.
         *
         * <p>
         * Java Stack: ..., INT -> ...
         * <p>
         */
#define  do_pop_n(n_507) { int  n_508 = n_507;  \
            popAsType(AddressType_ANY); \
            if (!SQUAWK_64 && (n_508) == 2) { \
                popAsType(AddressType_ANY); \
            } \
        }

        /*-----------------------------------------------------------------------*\
         *                           Native methods                              *
        \*-----------------------------------------------------------------------*/


        /**
         * These forward declarations have to be here to deal with the
         * different behaviour of GCC on the Mac with respect to forward
         * declarations of static methods.
         */
        static Address copyObjectGraph(Address, Address, Address, Address);
        static int collectGarbage(Address, Address, int);


#if PLATFORM_TYPE_BARE_METAL
#define checkNativeCall(_ptr_) fatalInterpreterError("checkNativeCall")
#define endCheckNativeCall()
#else
#define checkNativeCall(_ptr_) nativeFuncPtr = _ptr_
#define endCheckNativeCall() nativeFuncPtr = NULL
#endif

        /**
         * invokenativemain.
         */
#define  invokenativemain() {  \
            checkReferenceSlots(); \
            switch(iparm) { \
                case Native_com_sun_squawk_Offset_eq: \
                case Native_com_sun_squawk_UWord_eq: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(value1 == value2); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_ne: \
                case Native_com_sun_squawk_UWord_ne: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(value1 != value2); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_add: { \
                    int delta = popInt(); \
                    Offset offset = (Offset)popWord(); \
                    pushWord(offset + delta); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_sub: { \
                    int delta = popInt(); \
                    Offset offset = (Offset)popWord(); \
                    pushWord(offset - delta); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_bytesToWords: { \
                    Offset offset = (Offset)popWord(); \
                    assumeInterp((offset % HDR_BYTES_PER_WORD) == 0); \
                    pushWord(offset >> HDR_LOG2_BYTES_PER_WORD); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_wordsToBytes: { \
                    Offset offset = (Offset)popWord(); \
                    pushWord(offset << HDR_LOG2_BYTES_PER_WORD); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_fromPrimitive: { \
                    if (TYPEMAP) { \
                        UWord value = SQUAWK_64 ? popLong() : popInt(); \
                        pushAddress((Address)value); \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_fromPrimitive: \
                case Native_com_sun_squawk_UWord_fromPrimitive: { \
                    if (TYPEMAP) { \
                        UWord value = SQUAWK_64 ? popLong() : popInt(); \
                        pushWord(value); \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_toPrimitive: \
                case Native_com_sun_squawk_UWord_toPrimitive: { \
                    if (TYPEMAP) { \
                        UWord value = popWord(); \
                        if (SQUAWK_64) { \
                            /* this catches a (false) assumption that a given offset won't loose its sign or magnitude when converted to a long */ \
                            if (ASSUME && (jlong)value != value) { \
                                fatalInterpreterError("cast to long changes sign and/or magnitude"); \
                            } \
                            pushLong(value); \
                        } else { \
                            /* this catches a (false) assumption that a given offset won't loose its sign or magnitude when converted to an int */ \
                            if (ASSUME && (int)value != value) { \
                                fatalInterpreterError("cast to int changes sign and/or magnitude"); \
                            } \
                            pushInt(value); \
                        } \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_toInt: \
                case Native_com_sun_squawk_UWord_toInt: { \
                    if (TYPEMAP || SQUAWK_64 || ASSUME) { \
                        UWord value = popWord(); \
                        /* this catches a (false) assumption that a given offset won't loose its sign or magnitude when converted to an int */ \
                        if (ASSUME && (int)value != value) { \
                            fatalInterpreterError("cast to int changes sign and/or magnitude"); \
                        } \
                        pushInt((int)value); \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_toOffset: \
                case Native_com_sun_squawk_Offset_toUWord: { \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_max: { \
                    pushWord(WORD_MAX); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_zero: \
                case Native_com_sun_squawk_UWord_zero: { \
                    pushWord(0); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_and: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushWord(value1 & value2); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_or: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushWord(value1 | value2); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_loeq: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(loeq(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_hieq: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(hieq(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_hi: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(hi(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_lo: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(lo(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_le: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(le(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_ge: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(ge(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_gt: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(gt(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_lt: { \
                    UWord value2 = popWord(); \
                    UWord value1 = popWord(); \
                    pushInt(lt(value1, value2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Offset_isZero: \
                case Native_com_sun_squawk_UWord_isZero: { \
                    UWord value = popWord(); \
                    pushInt(value == 0); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_UWord_isMax: { \
                    UWord value = popWord(); \
                    pushInt(value == WORD_MAX); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getAsByte: \
                    if (TYPEMAP) { \
                        int     off = popInt(); \
                        Address base = popAddress(); \
                        pushInt(getByteTyped(base, off, AddressType_ANY)); \
                        break; \
                    } else { \
                        /* fall-through... */ \
                    } \
                case Native_com_sun_squawk_NativeUnsafe_getByte: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushInt(getByte(base, off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setByte: { \
                    int     val = popInt(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setByte(base, off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getAsShort: \
                    if (TYPEMAP) { \
                        int     off = popInt(); \
                        Address base = popAddress(); \
                        pushInt(getShortTyped(base, off, AddressType_ANY)); \
                        break; \
                    } else { \
                         /* fall-through... */ \
                    } \
                case Native_com_sun_squawk_NativeUnsafe_getShort: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushInt(getShort(base, off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getChar: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushInt(getUShort(base, off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_charAt: { \
                    int     off = popInt(); \
                    Address str = popAddress(); \
                    Address cls = getClass(str); \
                    boundsAssume(str, off); \
                    if (com_sun_squawk_Klass_id(cls) == com_sun_squawk_StringOfBytes) { \
                        pushInt(getUByte(str, off)); \
                    } else { \
                        pushInt(getUShort(str, off)); \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setShort: \
                case Native_com_sun_squawk_NativeUnsafe_setChar: { \
                    int     val = popInt(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setShort(base,  off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getAddress: \
                case Native_com_sun_squawk_NativeUnsafe_getObject: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushAddress(getObject(base, off)); \
                    break; \
                } \
 \
               case Native_com_sun_squawk_NativeUnsafe_getAsUWord: \
                    if (TYPEMAP) { \
                        int     off = popInt(); \
                        Address base = popAddress(); \
                        pushWord(getUWordTyped(base, off, AddressType_ANY)); \
                        break; \
                    } else { \
                         /* fall-through... */ \
                    } \
               case Native_com_sun_squawk_NativeUnsafe_getUWord: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushWord(getUWord(base, off)); \
                    break; \
                } \
 \
               case Native_com_sun_squawk_NativeUnsafe_getAsInt: \
                    if (TYPEMAP) { \
                        int     off = popInt(); \
                        Address base = popAddress(); \
                        pushInt(getIntTyped(base, off, AddressType_ANY)); \
                        break; \
                    } else { \
                         /* fall-through... */ \
                    } \
                case Native_com_sun_squawk_NativeUnsafe_getInt: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushInt(getInt(base, off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getUnalignedInt: { \
                    int     byte_off = popInt(); \
                    Address base = popAddress(); \
                    pushInt(getUnalignedInt(base, byte_off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getUnalignedLong: { \
                    int     byte_off = popInt(); \
                    Address base = popAddress(); \
                    pushLong(getUnalignedLong(base, byte_off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getUnalignedShort: { \
                    int     byte_off = popInt(); \
                    Address base = popAddress(); \
                    pushInt(getUnalignedShort(base, byte_off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_malloc: { \
                    int     size = popWord(); \
                    pushAddress(malloc(size)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_free: { \
                    Address base = popAddress(); \
                    free(base); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setAddress: { \
                    Address val = popAddress(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setObject(base,  off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setObject: { \
                    Address val = popAddress(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setObjectAndUpdateWriteBarrier(base,  off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setInt: { \
                    int     val = popInt(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setInt(base,  off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setUWord: { \
                    UWord    val = popWord(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setUWord(base,  off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getLong: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushLong(getLong(base, off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setLong: { \
                    jlong   val = popLong(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setLong(base,  off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_getLongAtWord: { \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    pushLong(getLongAtWord(base, off)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setLongAtWord: { \
                    jlong   val = popLong(); \
                    int     off = popInt(); \
                    Address base = popAddress(); \
                    setLongAtWord(base,  off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setUnalignedShort: { \
                    int     val = popInt(); \
                    int     byte_off = popInt(); \
                    Address base = popAddress(); \
                    setUnalignedShort(base, byte_off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setUnalignedInt: { \
                    int     val = popInt(); \
                    int     byte_off = popInt(); \
                    Address base = popAddress(); \
                    setUnalignedInt(base, byte_off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_setUnalignedLong: { \
                    jlong   val = popLong(); \
                    int     byte_off = popInt(); \
                    Address base = popAddress(); \
                    setUnalignedLong(base, byte_off, val); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_swap: { \
                    int dataSize = popInt(); \
                    Address address = popAddress(); \
                    swap(address, dataSize); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_swap2: { \
                    Address address = popAddress(); \
                    swap2(address); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_swap4: { \
                    Address address = popAddress(); \
                    swap4(address); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_swap8: { \
                    Address address = popAddress(); \
                    swap8(address); \
                    break; \
                } \
 \
/* DO WE WANT TO SUPPORT NATIVEUNSAFE_CALLS IN BARE_METAL SYSTEMS???? */ \
                case Native_com_sun_squawk_NativeUnsafe_call0: { \
                    Address address = popAddress(); \
                    funcPtr0 fptr = (funcPtr0)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_call1: { \
                    int i1 = popInt(); \
                    Address address = popAddress(); \
                    funcPtr1 fptr = (funcPtr1)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(i1); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_call2: { \
                    int i2 = popInt(); \
                    int i1 = popInt(); \
                    Address address = popAddress(); \
                    funcPtr2 fptr = (funcPtr2)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(i1, i2); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_call3: { \
                    int i3 = popInt(); \
                    int i2 = popInt(); \
                    int i1 = popInt(); \
                    Address address = popAddress(); \
                    funcPtr3 fptr = (funcPtr3)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(i1, i2, i3); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_call4: { \
                    int i4 = popInt(); \
                    int i3 = popInt(); \
                    int i2 = popInt(); \
                    int i1 = popInt(); \
                    Address address = popAddress(); \
                    funcPtr4 fptr = (funcPtr4)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(i1, i2, i3, i4); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_call5: { \
                    int i5 = popInt(); \
                    int i4 = popInt(); \
                    int i3 = popInt(); \
                    int i2 = popInt(); \
                    int i1 = popInt(); \
                    Address address = popAddress(); \
                    funcPtr5 fptr = (funcPtr5)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(i1, i2, i3, i4, i5); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_NativeUnsafe_call6: { \
                    int i6 = popInt(); \
                    int i5 = popInt(); \
                    int i4 = popInt(); \
                    int i3 = popInt(); \
                    int i2 = popInt(); \
                    int i1 = popInt(); \
                    Address address = popAddress(); \
                    funcPtr6 fptr = (funcPtr6)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(i1, i2, i3, i4, i5, i6); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                /* yes, jump straight to 1000 */ \
                case Native_com_sun_squawk_NativeUnsafe_call10: { \
                    int i10 = popInt(); \
                    int i9 = popInt(); \
                    int i8 = popInt(); \
                    int i7 = popInt(); \
                    int i6 = popInt(); \
                    int i5 = popInt(); \
                    int i4 = popInt(); \
                    int i3 = popInt(); \
                    int i2 = popInt(); \
                    int i1 = popInt(); \
                    Address address = popAddress(); \
                    funcPtr10 fptr = (funcPtr10)address; \
                    int result; \
                    checkNativeCall(fptr); \
                    result = (*fptr)(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10); \
                    set_com_sun_squawk_VMThread_errno(com_sun_squawk_VMThread_currentThread, errno); \
                    endCheckNativeCall(); \
                    pushInt(result); \
                    break; \
                } \
 \
                /* BARE_METAL platforms don't support native threads, so can't support TaskExecutors */ \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
                case Native_com_sun_squawk_Address_add: { \
                    int offset = popInt(); \
                    Address addr = popAddress(); \
                    pushAddress(Address_add(addr, offset)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_addOffset: { \
                    Offset offset = popWord(); \
                    Address addr = popAddress(); \
                    pushAddress(Address_add(addr, offset)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_sub: { \
                    int offset = popInt(); \
                    Address addr = popAddress(); \
                    pushAddress(Address_sub(addr, offset)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_subOffset: { \
                    Offset offset = popWord(); \
                    Address addr = popAddress(); \
                    pushAddress(Address_sub(addr, offset)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_and: { \
                    UWord word = popWord(); \
                    UWord addr = (UWord)popAddress(); \
                    pushAddress((Address)(addr & word)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_or: { \
                    UWord word = popWord(); \
                    UWord addr = (UWord)popAddress(); \
                    pushAddress((Address)(addr | word)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_diff: { \
                    Address addr2 = popAddress(); \
                    Address addr1 = popAddress(); \
                    pushWord(Address_diff(addr1, addr2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_roundUp: { \
                    int alignment = popInt(); \
                    UWord addr = (UWord)popAddress(); \
                    pushAddress((Address)roundUp(addr, alignment)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_roundUpToWord: { \
                    UWord addr = (UWord)popAddress(); \
                    pushAddress((Address)roundUpToWord(addr)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_roundDown: { \
                    int alignment = popInt(); \
                    UWord addr = (UWord)popAddress(); \
                    pushAddress((Address)roundDown(addr, alignment)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_roundDownToWord: { \
                    UWord addr = (UWord)popAddress(); \
                    pushAddress((Address)roundDownToWord(addr)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_isZero: { \
                    Address addr = popAddress(); \
                    pushInt(addr == 0); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_isMax: { \
                    Address addr = popAddress(); \
                    pushInt(addr == ADDRESS_MAX); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_zero: { \
                    pushAddress(0); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_max: { \
                    pushAddress(ADDRESS_MAX); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_toUWord: { \
                    if (TYPEMAP) { \
                        Address value = popAddress(); \
                        pushWord((UWord)value); \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_toObject: \
                case Native_com_sun_squawk_Address_fromObject: { \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_eq: { \
                    Address addr2 = popAddress(); \
                    Address addr1 = popAddress(); \
                    pushInt(addr1 == addr2); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_ne: { \
                    Address addr2 = popAddress(); \
                    Address addr1 = popAddress(); \
                    pushInt(addr1 != addr2); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_lo: { \
                    Address addr2 = popAddress(); \
                    Address addr1 = popAddress(); \
                    pushInt(lo(addr1, addr2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_loeq: { \
                    Address addr2 = popAddress(); \
                    Address addr1 = popAddress(); \
                    pushInt(loeq(addr1, addr2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_hi: { \
                    Address addr2 = popAddress(); \
                    Address addr1 = popAddress(); \
                    pushInt(hi(addr1, addr2)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_Address_hieq: { \
                    Address addr2 = popAddress(); \
                    Address addr1 = popAddress(); \
                    pushInt(hieq(addr1, addr2)); \
                    break; \
                } \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
                case Native_com_sun_squawk_VM_allocate: { \
                    int     alth   = popInt(); \
                    Address klass  = popAddress(); \
                    int     size   = popInt(); \
                    Address res    = allocate(size, klass, alth); \
                    pushAddress(res); \
                    break; \
                } \
 \
/* static void copyBytes(Object src, int srcPos, Object dst, int dstPos, int length, boolean nvmDst);*/ \
                case Native_com_sun_squawk_VM_copyBytes: { \
                    int     nvmDst   = popInt(); \
                    int     length   = popInt(); \
                    int     dstPos   = popInt(); \
                    Address dst      = popAddress(); \
                    int     srcPos   = popInt(); \
                    Address src      = popAddress(); \
                    assumeInterp(src != NULL); \
                    assumeInterp(dst != NULL); \
                    assumeInterp(srcPos >= 0); \
                    assumeInterp(dstPos >= 0); \
                    assumeInterp(length >= 0); \
                    copyBytes(src, srcPos, dst, dstPos, length, nvmDst); \
                    break; \
                } \
 \
/* static void setBytes(Address src, byte value, int length); */ \
                case Native_com_sun_squawk_VM_setBytes: { \
                    int     length   = popInt(); \
                    int     value    = popInt(); \
                    Address dst      = popAddress(); \
                    assumeInterp(dst != NULL); \
                    assumeInterp(length >= 0); \
                    memset(dst, value, length); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_zeroWords: { \
                    UWordAddress end   = (UWordAddress)popAddress(); \
                    UWordAddress start = (UWordAddress)popAddress(); \
                    zeroWords(start, end); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_deadbeef: { \
                    UWordAddress end   = (UWordAddress)popAddress(); \
                    UWordAddress start = (UWordAddress)popAddress(); \
                    if (ASSUME || TYPEMAP) { \
                        while (start < end) { \
                            if (ASSUME) { \
                                *start = DEADBEEF; \
                            } \
                            setType(start, AddressType_UNDEFINED, HDR_BYTES_PER_WORD); \
                            start++; \
                        } \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getFP: { \
                    pushAddress(fp); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getMP: { \
                    Address afp = popAddress(); \
                    pushAddress(getObject(afp, FP_method)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getPreviousFP: { \
                    Address afp = popAddress(); \
                    pushAddress(getObject(afp, FP_returnFP)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getPreviousIP: { \
                    Address afp = popAddress(); \
                    pushAddress(getObject(afp, FP_returnIP)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_setPreviousFP: { \
                    Address pfp = popAddress(); \
                    Address afp = popAddress(); \
                    setObject(afp, FP_returnFP, pfp); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_setPreviousIP: { \
                    Address pip = popAddress(); \
                    Address afp = popAddress(); \
                    assumeInterp(inCode(ip)); \
                    setObject(afp, FP_returnIP, pip); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getGlobalOopCount: { \
                    pushInt(GLOBAL_OOP_COUNT); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getGlobalInt: { \
                    int index = popInt(); \
                    assumeInterp(index < GLOBAL_INT_COUNT); \
                    pushInt(Ints[index]); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getGlobalAddr: { \
                    int index = popInt(); \
                    assumeInterp(index < GLOBAL_ADDR_COUNT); \
                    pushAddress(Addrs[index]); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getGlobalOop: { \
                    int index = popInt(); \
                    assumeInterp(index < GLOBAL_OOP_COUNT); \
                    pushAddress(Oops[index]); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getGlobalOopTable: { \
                    pushAddress(Oops); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_setGlobalInt: { \
                    int index  = popInt(); \
                    int value = popInt(); \
                    assumeInterp(index < GLOBAL_INT_COUNT); \
                    Ints[index] = (int)value; \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_setGlobalAddr: { \
                    int index  = popInt(); \
                    Address value = popAddress(); \
                    assumeInterp(index < GLOBAL_ADDR_COUNT); \
                    Addrs[index] = value; \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_setGlobalOop: { \
                    int index  = popInt(); \
                    Address value = popAddress(); \
                    assumeInterp(index < GLOBAL_OOP_COUNT); \
                    Oops[index] = value; \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_lcmp: { \
                    call(com_sun_squawk_VM__lcmp); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_callStaticNoParm: { \
                    int     slot = popInt(); \
                    Address cls  = popAddress(); \
                    call(getStaticMethod(cls, slot)); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_callStaticOneParm: { \
                    Address parm = popAddress(); \
                    int     slot = popInt(); \
                    Address cls  = popAddress(); \
                    pushAddress(parm); \
                    call(getStaticMethod(cls, slot)); \
                    break; \
                } \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
                  case Native_com_sun_squawk_VM_hashcode: \
                  case Native_com_sun_squawk_VM_asKlass: { \
                      break; \
                  } \
 \
 \
                case Native_com_sun_squawk_VM_fatalVMError: { \
                    fatalInterpreterError("VM.fatalVMError"); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_getBranchCount: { \
                    pushLong(getBranchCount()); \
                    break; \
                } \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
                case Native_com_sun_squawk_VM_threadSwitch: { \
                    if (com_sun_squawk_VMThread_currentThread != \
                        com_sun_squawk_VMThread_otherThread) { /* this actually happens in tight producer/consumer loops */ \
                        threadSwitch(com_sun_squawk_ServiceOperation_NONE); \
                    } \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_executeCIO: { \
                    Address o2  = popAddress(); \
                    Address o1  = popAddress(); \
                    int i6      = popInt(); \
                    int i5      = popInt(); \
                    int i4      = popInt(); \
                    int i3      = popInt(); \
                    int i2      = popInt(); \
                    int i1      = popInt(); \
                    int channel = popInt(); \
                    int op      = popInt(); \
                    int context = popInt(); \
                    executeCIO(context, op, channel, i1, i2, i3, i4, i5, i6, o1, o2); \
                    break; \
                } \
 \
case Native_com_sun_squawk_VM_jnaPrint: { \
                Address fn  = popAddress(); \
                void jnaPrint(Address fn); \
                jnaPrint(fn); \
                break; \
                } \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
                case Native_com_sun_squawk_VM_jnaSendAckByte: { \
char data = (char) popInt(); \
                void jnaSendAckByte(char data); \
                jnaSendAckByte(data); \
                break; \
                } \
 \
 \
 \
                case Native_com_sun_squawk_VM_jnaSendSpeedPwmData: { \
                int speed = popInt(); \
                void jnaSendSpeedPwmData(int speed); \
                jnaSendSpeedPwmData(speed); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaSendSteerPwmData: { \
                int servo  = popInt(); \
                void jnaSendSteerPwmData(int servo); \
                jnaSendSteerPwmData(servo); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaFetchAdcData: { \
                long jnaFetchAdcData(); \
                jlong res = jnaFetchAdcData(); \
pushLong(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaFetchFrontWheelSpeed: { \
                int jnaFetchFrontWheelSpeed(); \
                int res = jnaFetchFrontWheelSpeed(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaFetchBackWheelSpeed: { \
                int jnaFetchBackWheelSpeed(); \
                int res = jnaFetchBackWheelSpeed(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaSendPackageData: { \
                Address data  = popAddress(); \
                int size = popInt(); \
                void jnaSendPackageData(int size, Address data); \
                jnaSendPackageData(size, data); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaFetchNewData: { \
                int rearSign = popInt(); \
                int startSign = popInt(); \
                Address jnaFetchNewData(int startSign, int rearSign); \
                Address res = jnaFetchNewData(startSign, rearSign); \
                pushAddress(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaCheckIfNewPackage: { \
                int jnaCheckIfNewPackage(); \
                int res = jnaCheckIfNewPackage(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaGetLengthPackage: { \
                int jnaGetLengthPackage(); \
                int res = jnaGetLengthPackage(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaGetReadStartIndex: { \
                int jnaGetReadStartIndex(); \
                int res = jnaGetReadStartIndex(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaGetReadRearIndex: { \
                int jnaGetReadRearIndex(); \
                int res = jnaGetReadRearIndex(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaFetchByte: { \
                int rearIndex = popInt(); \
                char jnaFetchByte(int rearIndex); \
                char res = jnaFetchByte(rearIndex); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadPosition: { \
                jlong jnaReadPosition(); \
                jlong res = jnaReadPosition(); \
                pushLong(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadUltrasonicData: { \
                int jnaReadUltrasonicData(); \
                int res = jnaReadUltrasonicData(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaWritePluginData2VCU: { \
                Address data  = popAddress(); \
                int size = popInt(); \
                void jnaWritePluginData2VCU(int size, Address data); \
                jnaWritePluginData2VCU(size, data); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadPluginDataSizeFromSCU: { \
                int jnaReadPluginDataSizeFromSCU(); \
                int res = jnaReadPluginDataSizeFromSCU(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadPluginDataByteFromSCU: { \
                int index = popInt(); \
                char jnaReadPluginDataByteFromSCU(int index); \
                char res = jnaReadPluginDataByteFromSCU(index); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaResetPluginDataSizeFromSCU: { \
                void jnaResetPluginDataSizeFromSCU(); \
                jnaResetPluginDataSizeFromSCU(); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadIMUPart1: { \
                jlong jnaReadIMUPart1(); \
                jlong res = jnaReadIMUPart1(); \
                pushLong(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadIMUPart2: { \
                jlong jnaReadIMUPart2(); \
                jlong res = jnaReadIMUPart2(); \
                pushLong(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaSetLED: { \
                int val = popInt(); \
                int pin = popInt(); \
                void jnaSetLED(int pin, int val); \
                jnaSetLED(pin, val); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaFetchSpeedFromPirte: { \
                int jnaFetchSpeedFromPirte(); \
                int res = jnaFetchSpeedFromPirte(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaFetchSteerFromPirte: { \
                int jnaFetchSteerFromPirte(); \
                int res = jnaFetchSteerFromPirte(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadPluginDataSizeFromTCU: { \
                int jnaReadPluginDataSizeFromTCU(); \
                int res = jnaReadPluginDataSizeFromTCU(); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaReadPluginDataByteFromTCU: { \
                int index = popInt(); \
                char jnaReadPluginDataByteFromTCU(int index); \
                char res = jnaReadPluginDataByteFromTCU(index); \
                pushInt(res); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaResetPluginDataSizeFromTCU: { \
                void jnaResetPluginDataSizeFromTCU(); \
                jnaResetPluginDataSizeFromTCU(); \
                break; \
                } \
 \
                case Native_com_sun_squawk_VM_jnaSetSelect: { \
                int selector = popInt(); \
                void jnaSetSelect(int selector); \
                jnaSetSelect(selector); \
                break; \
                } \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
                case Native_com_sun_squawk_VM_executeGC: { \
                    boolean forceFullGC = popInt(); \
                    com_sun_squawk_ServiceOperation_i1 = forceFullGC; \
 if (TRACE) { \
                    printStackTracePrim(-1, ip, fp, "GARBAGE_COLLECT", null); \
                } \
                    threadSwitchFor(com_sun_squawk_ServiceOperation_GARBAGE_COLLECT); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_serviceResult: { \
                    int res = com_sun_squawk_ServiceOperation_result; \
                    com_sun_squawk_ServiceOperation_result = 0xDEADBEEF; \
                    pushInt(res); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_addressResult: { \
                    Address res = com_sun_squawk_ServiceOperation_addressResult; \
                    com_sun_squawk_ServiceOperation_addressResult = null; \
                    pushAddress(res); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_ServiceOperation_cioExecute: { \
                    void cioExecute(void); \
                    cioExecute(); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_isBigEndian: { \
                    pushInt(PLATFORM_BIG_ENDIAN); \
                    break; \
                } \
 \
 \
                case Native_com_sun_squawk_CheneyCollector_memoryProtect: { \
                    cheneyEndMemoryProtect   = popAddress(); \
                    cheneyStartMemoryProtect = popAddress(); \
/*fprintf(stderr, "*** cheneyStartMemoryProtect=%d, cheneyEndMemoryProtect=%d\n", cheneyStartMemoryProtect, cheneyEndMemoryProtect);*/ \
                    break; \
                } \
 \
 \
                case Native_com_sun_squawk_VM_addToClassStateCache: { \
                    Address state = popAddress(); \
                    Address klass = popAddress(); \
                    addClassState(klass, state); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_invalidateClassStateCache: { \
                    pushInt(invalidateClassStateCache()); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_removeVirtualMonitorObject: { \
                    Address res = null; \
 \
                    if (pendingMonitorStackPointer > 0) { \
                        res = pendingMonitors[--pendingMonitorStackPointer]; \
                        pendingMonitors[pendingMonitorStackPointer] = null; \
                    } \
                    pushAddress(res); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_hasVirtualMonitorObject: { \
                    Address obj = popAddress(); \
                    boolean res = false; \
                    int i; \
                    for (i = 0 ; i < pendingMonitorStackPointer ; i++) { \
                        if (pendingMonitors[i] == obj) { \
                            res = true; \
                            break; \
                        } \
                    } \
                    pushInt(res); \
                    break; \
                } \
 \
 \
                case Native_com_sun_squawk_VM_doubleToLongBits: \
                case Native_com_sun_squawk_VM_floatToIntBits: \
                case Native_com_sun_squawk_VM_longBitsToDouble: \
                case Native_com_sun_squawk_VM_intBitsToFloat: { \
                    /* simply leave bits on the stack: */ \
                    break; \
                } \
 \
                case Native_com_sun_squawk_VM_math: { \
                    jlong value2 = popLong(); \
                    jlong value1 = popLong(); \
                    int op = popInt(); \
                    jlong res = math(op, value1, value2); \
                    pushLong(res); \
                    break; \
                } \
 \
 \
 \
                case Native_com_sun_squawk_GarbageCollector_hasNativeImplementation: { \
                    pushInt(true); \
                    break; \
                } \
 \
                case Native_com_sun_squawk_GarbageCollector_collectGarbageInC: { \
                    int forceFullGC = popInt(); \
                    Address allocTop = popAddress(); \
                    Address collector = popAddress(); \
                    pushInt(collectGarbage(collector, allocTop, forceFullGC)); \
                    break; \
                } \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
                default: { \
                    /*fprintf(stderr, "*** Undefined native method: *** %d\n", iparm);*/ \
                    pushInt(iparm); \
                    call(com_sun_squawk_VM_undefinedNativeMethod); \
                    break; \
                } \
            } \
        }

        /**
         * invokenative.
         *
         * Java Stack: [[... arg2], arg1] -> [VALUE]
         * <p>
         */
#define  do_invokenative() {  \
 \
            goto invokenativestart; \
 \
 \
 \
        }

        /*-----------------------------------------------------------------------*\
         *                             ALU instructions                          *
        \*-----------------------------------------------------------------------*/

        /**
         * Adds two integer values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1+VALUE2
         * <p>
         */
#define  do_add_i() {  \
            if (NOSILLYADDBUG) { \
                int r = popInt(); \
                int l = popInt(); \
                pushInt(l+r); \
            } \
        }

        /**
         * Adds long integer values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1+VALUE2
         * <p>
         */
#define  do_add_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(l+r); \
        }


        /**
         * Adds two float values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1+VALUE2
         * <p>
         */
#define  do_add_f() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(addf(l, r)); \
        }

        /**
         * Adds two double values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1+VALUE2
         * <p>
         */
#define  do_add_d() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(addd(l, r)); \
        }


        /**
         * Subtracts two integer values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1-VALUE2
         * <p>
         */
#define  do_sub_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(l-r); \
        }

        /**
         * Subtracts two long values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1-VALUE2
         * <p>
         */
#define  do_sub_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(l-r); \
        }


        /**
         * Subtracts two floats values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1-VALUE2
         * <p>
         */
#define  do_sub_f() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(subf(l, r)); \
        }

        /**
         * Subtracts two double values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1-VALUE2
         * <p>
         */
#define  do_sub_d() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(subd(l, r)); \
        }


        /**
         * Logically ANDs two integer values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1&VALUE2
         * <p>
         */
#define  do_and_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(l&r); \
        }

        /**
         * Logically ANDs two long values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1&VALUE2
         * <p>
         */
#define  do_and_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(l&r); \
        }

        /**
         * Logically ORs two integer values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1|VALUE2
         * <p>
         */
#define  do_or_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(l|r); \
        }

        /**
         * Logically ORs two long values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1|VALUE2
         * <p>
         */
#define  do_or_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(l|r); \
        }

        /**
         * Logically XORs two integer values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1^VALUE2
         * <p>
         */
#define  do_xor_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(l^r); \
        }

        /**
         * Logically XORs two long values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1^VALUE2
         * <p>
         */
#define  do_xor_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(l^r); \
        }

        /**
         * Signed left shifts an integer value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1<<(VALUE2&1f)
         * <p>
         */
#define  do_shl_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(sll(l, r)); \
        }

        /**
         * Signed left shifts a long value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1<<(VALUE2&1f)
         * <p>
         */
#define  do_shl_l() {  \
            int   r = popInt(); \
            jlong l = popLong(); \
            pushLong(slll(l, r)); \
        }

        /**
         * Right shifts an integer value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1>>(VALUE2&1f)
         * <p>
         */
#define  do_shr_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(sra(l, r)); \
        }

        /**
         * Right shifts a long value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1>>(VALUE2&1f)
         * <p>
         */
#define  do_shr_l() {  \
            int   r = popInt(); \
            jlong l = popLong(); \
            pushLong(sral(l, r)); \
        }

        /**
         * Unsigned right shifts an integer value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1>>>(VALUE2&1f)
         * <p>
         */
#define  do_ushr_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(srl(l, r)); \
        }

        /**
         * Unsigned right shifts a long value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1>>>(VALUE2&1f)
         * <p>
         */
#define  do_ushr_l() {  \
            int r = popInt(); \
            jlong l = popLong(); \
            pushLong(srll(l, r)); \
        }

        /**
         * Multiplies two values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1*VALUE2
         * <p>
         */
#define  do_mul_i() {  \
            int r = popInt(); \
            int l = popInt(); \
/* \
if (l != 0 && r > (java_lang_Integer_MAX_VALUE/l)) { \
    fatalInterpreterError("int mult overflow"); \
} \
*/ \
            pushInt(l*r); \
        }

        /**
         * Multiplies two values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1*VALUE2
         * <p>
         */
#define  do_mul_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
/* \
if (l != 0 && r > (java_lang_Long_MAX_VALUE/l)) { \
    fatalInterpreterError("int mult overflow"); \
} \
*/ \
            pushLong(l*r); \
        }


        /**
         * Multiplies two values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1*VALUE2
         * <p>
         */
#define  do_mul_f() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(mulf(l, r)); \
        }

        /**
         * Multiplies two values.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1*VALUE2
         * <p>
         */
#define  do_mul_d() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(muld(l, r)); \
        }


        /**
         * Divides a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1/VALUE2
         * <p>
         */
#define  do_div_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            checkReferenceSlots(); \
            if (r == 0) { \
                resetStackPointerIfRevParms(); \
                call(com_sun_squawk_VM_arithmeticException); \
            } else if (l == 0x80000000 && r == -1) { \
                pushInt(l); \
            } else { \
                pushInt(l / r); \
            } \
        }

        /**
         * Divides a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1/VALUE2
         * <p>
         */
#define  do_div_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            checkReferenceSlots(); \
            if (r == 0) { \
                resetStackPointerIfRevParms(); \
                call(com_sun_squawk_VM_arithmeticException); \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
            } else { \
                pushLong(l / r); \
            } \
        }


        /**
         * Divides a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1/VALUE2
         * <p>
         */
#define  do_div_f() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(divf(l, r)); \
        }

        /**
         * Divides a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1/VALUE2
         * <p>
         */
#define  do_div_d() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(divd(l, r)); \
        }


        /**
         * Rem a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1%VALUE2
         * <p>
         */
#define  do_rem_i() {  \
            int r = popInt(); \
            int l = popInt(); \
            checkReferenceSlots(); \
            if (r == 0) { \
                resetStackPointerIfRevParms(); \
                call(com_sun_squawk_VM_arithmeticException); \
            } else if (l == 0x80000000 && r == -1) { \
                pushInt(l % 1); \
            } else { \
                pushInt(l % r); \
            } \
        }

        /**
         * Rem a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1%VALUE2
         * <p>
         */
#define  do_rem_l() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            checkReferenceSlots(); \
            if (r == 0) { \
                resetStackPointerIfRevParms(); \
                call(com_sun_squawk_VM_arithmeticException); \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
            } else { \
                pushLong(l % r); \
            } \
        }


        /**
         * Rem a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1%VALUE2
         * <p>
         */
#define  do_rem_f() {  \
            int r = popInt(); \
            int l = popInt(); \
            pushInt(remf(l, r)); \
        }

        /**
         * Rem a value.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., VALUE1%VALUE2
         * <p>
         */
#define  do_rem_d() {  \
            jlong r = popLong(); \
            jlong l = popLong(); \
            pushLong(remd(l, r)); \
        }


        /**
         * Negates a value.
         *
         * <p>
         * Java Stack: ..., VALUE -> ..., -VALUE1
         * <p>
         */
#define  do_neg_i() {  \
            /* \
             * Note: Due to a bug in the Solaris cc compiler when both \
             *       -o3 and -mac are enabled, this was changed from: \
             * \
             *     int r = popInt(); \
             *     pushInt(0 - r); \
             */ \
            int r = 0 - popInt(); \
            pushInt(r); \
        }

        /**
         * Negates a value.
         *
         * <p>
         * Java Stack: ..., VALUE -> ..., -VALUE1
         * <p>
         */
#define  do_neg_l() {  \
            jlong r = 0 - popLong(); \
            pushLong(r); \
        }


        /**
         * Negates a value.
         *
         * <p>
         * Java Stack: ..., VALUE -> ..., -VALUE1
         * <p>
         */
#define  do_neg_f() {  \
            int r = popInt(); \
            pushInt(negf(r)); \
        }

        /**
         * Negates a value.
         *
         * <p>
         * Java Stack: ..., VALUE -> ..., -VALUE1
         * <p>
         */
#define  do_neg_d() {  \
            jlong r = popLong(); \
            pushLong(negd(r)); \
        }

       /**
         * Compare two floats.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., int
         * <p>
         */
#define  do_fcmpl() {  \
            float r = ib2f(popInt()); \
            float l = ib2f(popInt()); \
            int result = -1; \
 \
            if (l >  r) { \
                result = 1; \
            } else if (l == r) { \
                result = 0; \
            } \
 \
            pushInt(result); \
        }

       /**
         * Compare two floats.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., int
         * <p>
         */
#define  do_fcmpg() {  \
            float r = ib2f(popInt()); \
            float l = ib2f(popInt()); \
            int result = 1; \
 \
            if (l <  r) { \
                result = -1; \
            } else if (l == r) { \
                result = 0; \
            } \
 \
            pushInt(result); \
        }

       /**
         * Compare two doubles.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., int
         * <p>
         */
#define  do_dcmpl() {  \
            double r = lb2d(popLong()); \
            double l = lb2d(popLong()); \
            int result = -1; \
 \
            if (l > r) { \
                result = 1; \
             } else if (l == r) { \
                result = 0; \
            } \
 \
            pushInt(result); \
        }

       /**
         * Compare two doubles.
         *
         * <p>
         * Java Stack: ..., VALUE1, VALUE2 -> ..., int
         * <p>
         */
#define  do_dcmpg() {  \
            double r = lb2d(popLong()); \
            double l = lb2d(popLong()); \
            int result = 1; \
 \
            if (l < r) { \
                result = -1; \
            } else if (l == r) { \
                result = 0; \
            } \
 \
            pushInt(result); \
        }




        /*-----------------------------------------------------------------------*\
         *                              Convertions                              *
        \*-----------------------------------------------------------------------*/

        /**
         * Convert int to byte.
         *
         * <p>
         * Java Stack: ..., INT -> ..., INT
         * <p>
         */
#define  do_i2b() {  \
            int r = popInt(); \
            pushInt((byte)r); \
        }

        /**
         * Convert int to short.
         *
         * <p>
         * Java Stack: ..., INT -> ..., INT
         * <p>
         */
#define  do_i2s() {  \
            int r = popInt(); \
            pushInt((short)r); \
        }

        /**
         * Convert int to char.
         *
         * <p>
         * Java Stack: ..., INT -> ..., INT
         * <p>
         */
#define  do_i2c() {  \
            int r = popInt(); \
            pushInt((unsigned short)r); \
        }

        /**
         * Convert long to int.
         *
         * <p>
         * Java Stack: ..., LONG -> ..., INT
         * <p>
         */
#define  do_l2i() {  \
            jlong r = popLong(); \
            pushInt((int)r); \
        }

        /**
         * Convert int to long.
         *
         * <p>
         * Java Stack: ..., INT -> ..., LONG
         * <p>
         */
#define  do_i2l() {  \
            int r = popInt(); \
            pushLong(r); \
        }

        /**
         * Convert int to float.
         *
         * <p>
         * Java Stack: ..., INT -> ..., FLOAT
         * <p>
         */
#define  do_i2f() {  \
            int r = popInt(); \
            pushInt(i2f(r)); \
        }

        /**
         * Convert long to float.
         *
         * <p>
         * Java Stack: ..., LONG -> ..., FLOAT
         * <p>
         */
#define  do_l2f() {  \
            jlong r = popLong(); \
            pushInt(l2f(r)); \
        }

        /**
         * Convert float to int.
         *
         * <p>
         * Java Stack: ..., FLOAT -> ..., INT
         * <p>
         */
#define  do_f2i() {  \
            int r = popInt(); \
            pushInt(f2i(r)); \
        }

        /**
         * Convert float to long.
         *
         * <p>
         * Java Stack: ..., FLOAT -> ..., LONG
         * <p>
         */
#define  do_f2l() {  \
            int r = popInt(); \
            pushLong(f2l(r)); \
        }

        /**
         * Convert int to double.
         *
         * <p>
         * Java Stack: ..., INT -> ..., DOUBLE
         * <p>
         */
#define  do_i2d() {  \
            int r = popInt(); \
            pushLong(i2d(r)); \
        }

        /**
         * Convert long to double.
         *
         * <p>
         * Java Stack: ..., LONG -> ..., DOUBLE
         * <p>
         */
#define  do_l2d() {  \
            jlong r = popLong(); \
            pushLong(l2d(r)); \
        }

        /**
         * Convert float to double.
         *
         * <p>
         * Java Stack: ..., FLOAT -> ..., DOUBLE
         * <p>
         */
#define  do_f2d() {  \
            int r = popInt(); \
            pushLong(f2d(r)); \
        }

        /**
         * Convert double to int.
         *
         * <p>
         * Java Stack: ..., DOUBLE -> ..., INT
         * <p>
         */
#define  do_d2i() {  \
            jlong r = popLong(); \
            pushInt(d2i(r)); \
        }

        /**
         * Convert double to long.
         *
         * <p>
         * Java Stack: ..., DOUBLE -> ..., LONG
         * <p>
         */
#define  do_d2l() {  \
            jlong r = popLong(); \
            pushLong(d2l(r)); \
        }

        /**
         * Convert double to float.
         *
         * <p>
         * Java Stack: ..., DOUBLE -> ..., FLOAT
         * <p>
         */
#define  do_d2f() {  \
            jlong r = popLong(); \
            pushInt(d2f(r)); \
        }



        /*-----------------------------------------------------------------------*\
         *        Complex instructions implemented with external functions       *
        \*-----------------------------------------------------------------------*/

#if TRACE
#define  PRINTSTACK() {  \
            Address cls = getClass(exception); \
            Address name = com_sun_squawk_Klass_name(cls); \
            char buf[1000]; \
            int pos = getArrayLength(name); \
            if (pos >= 991) { \
                pos = 990; \
            } \
            memmove(buf, name, pos); \
            buf[pos++] = ':'; \
            buf[pos++] = ' '; \
            pos += printJavaStringBuf(java_lang_Throwable_detailMessage(exception), buf + pos, 999 - pos); \
            buf[pos] = 0; \
            printStackTracePrim(-1, ip, fp, buf, null); \
        }
#else
#define PRINTSTACK() /**/
#endif


        /**
         * Throw an exception.
         *
         * <p>
         * Java Stack: OOP -> _
         * <p>
         */
#define  do_throw() {  \
            if (usingServiceStack()) { \
                Address exception = popAddress(); \
                fprintf(stderr, "do_throw on service stack. current code=%d:\n", com_sun_squawk_ServiceOperation_code); \
                assumeInterp(exception == null || ((UWord)getObject(exception, HDR_klass) & HDR_headerTagMask) == 0); \
                if (exception != NULL && getClass(exception) != NULL) { \
                    printJavaStrSafely(com_sun_squawk_Klass_name(getClass(exception)), "class"); \
                    printJavaStrSafely(java_lang_Throwable_detailMessage(exception), "detail message"); \
                } \
                fatalInterpreterError(""); \
            } else { \
                UWord oldip = (UWord)ip; \
                Address exception = popAddress(); \
                nullCheck((Address)exception); \
                PRINTSTACK(); \
                if (com_sun_squawk_ServiceOperation_pendingException != 0) { \
                    fatalInterpreterError("do_throw with pending exception"); \
                } \
                com_sun_squawk_ServiceOperation_pendingException = exception; \
                threadSwitchFor(com_sun_squawk_ServiceOperation_THROW); \
            } \
        }

        /**
         * Start an exception handler.
         *
         * <p>
         * Compiler Stack: _ -> OOP
         * <p>
         */
#define  do_catch() {  \
            Address exception = (Address)com_sun_squawk_ServiceOperation_pendingException; \
            assumeInterp(exception != null); \
            com_sun_squawk_ServiceOperation_pendingException = 0; \
            pushAddress(exception); \
        }

        /**
         * Execute a monitor enter.
         *
         * <p>
         * Java Stack: OOP -> _
         * <p>
         */
#define  do_monitorenter() {  \
            Address obj = popAddress(); \
            Address assn; \
 \
            nullCheck(obj); \
            assumeInterp(((UWord)getObject(obj, HDR_klass) & HDR_headerTagMask) == 0); \
            assn = getClassOrAssociation(obj); \
            if (unlikely(MONITOR_CACHE_SIZE == 0 || associationToKlass(assn) != assn || pendingMonitorStackPointer == MONITOR_CACHE_SIZE || \
 !(hi(obj, com_sun_squawk_GC_ramStart) && loeq(obj, com_sun_squawk_GC_ramEnd)))) { \
                pushAddress(obj); \
                call(com_sun_squawk_VM_monitorenter); \
            } else { \
                pendingMonitors[pendingMonitorStackPointer++] = obj; \
            } \
        }

#ifdef INTERPRETER_STATS
#define updatePendingMonitorAccesses() pendingMonitorAccesses++
#define updatePendingMonitorHits() pendingMonitorHits++
#else
#define updatePendingMonitorAccesses()
#define updatePendingMonitorHits()
#endif /* INTERPRETER_STATS */

        /**
         * Execute a monitor exit.
         *
         * <p>
         * Java Stack: OOP -> _
         * <p>
         */
#define  do_monitorexit() {  \
            Address obj = popAddress(); \
            Address assn; \
 \
            nullCheck(obj); \
            assumeInterp(((UWord)getObject(obj, HDR_klass) & HDR_headerTagMask) == 0); \
            assn = getClassOrAssociation(obj); \
            updatePendingMonitorAccesses(); \
            if (unlikely(MONITOR_CACHE_SIZE == 0 || associationToKlass(assn) != assn || pendingMonitorStackPointer == 0 || \
 !(hi(obj, com_sun_squawk_GC_ramStart) && loeq(obj, com_sun_squawk_GC_ramEnd)))) { \
                pushAddress(obj); \
                call(com_sun_squawk_VM_monitorexit); \
            } else { \
                Address obj2 = pendingMonitors[--pendingMonitorStackPointer]; \
                assumeInterp(obj == obj2); \
                pendingMonitors[pendingMonitorStackPointer] = null; \
                updatePendingMonitorHits(); \
            } \
        }

        /**
         * Execute a monitor enter.
         *
         * <p>
         * Java Stack: _ -> _
         * <p>
         */
#define  do_class_monitorenter() {  \
            pushAddress(getCP()); \
            do_monitorenter(); \
        }

        /**
         * Execute a monitor exit.
         *
         * <p>
         * Java Stack: _ -> _
         * <p>
         */
#define  do_class_monitorexit() {  \
            pushAddress(getCP()); \
            do_monitorexit(); \
        }

        /**
         * Initialize class if needed.
         *
         * <p>
         * Java Stack: _ -> _
         * <p>
         */
#define  do_class_clinit() {  \
            checkReferenceSlots(); \
            if (unlikely(needsInitializing(getCP()))) { \
                pushAddress(getCP()); \
                call(com_sun_squawk_VM_class_clinit); \
            } \
        }

        /**
         * Get the length of an array.
         *
         * <p>
         * Java Stack: ..., OOP -> ..., INT
         * <p>
         */
#define  do_arraylength() {  \
            Address oop = popAddress(); \
            nullCheck(oop); \
            pushInt(getArrayLength(oop)); \
        }

#ifdef INTERPRETER_STATS
#define updateNewHits() com_sun_squawk_GC_newHits++
#else
#define updateNewHits()
#endif /* INTERPRETER_STATS */

        /**
         * Allocate an object.
         *
         * <p>
         * Java Stack: ..., CLASS -> ..., OOP
         * <p>
         */
#define  do_new() {  \
            checkReferenceSlots(); \
            if (FASTALLOC) { \
                Address klass = popAddress(); \
                if (likely(!needsInitializing(klass))) { \
                    boolean hasFinalizer = false; \
 \
 \
 \
                    if (!hasFinalizer) { \
                        int allocSize    = roundUpToWord(com_sun_squawk_Klass_instanceSizeBytes(klass)) + HDR_basicHeaderSize; \
                        Address oop      = allocateFast(allocSize, klass, -1); \
                        if (oop != null) { \
                            pushAddress(oop); \
                            updateNewHits(); \
                            nextbytecode(); \
                        } \
                    } \
                } \
                pushAddress(klass); \
            } \
            call(com_sun_squawk_VM__new); \
        }

        /**
         * Allocate a new array.
         *
         * <p>
         * Java Stack: SIZE, CLASS -> ..., OOP
         * <p>
         */
#define  do_newarray() {  \
            checkReferenceSlots(); \
            if (FASTALLOC) { \
                Address klass = popAddress(); \
                int length    = popInt(); \
                if (likely(length >= 0)) { \
                    Address ctype = com_sun_squawk_Klass_componentType(klass); \
                    int dataSize = getDataSize(ctype); \
                    int bodySize = length * dataSize; \
                    if (likely(bodySize >= 0)) { \
                        int allocSize = roundUpToWord(HDR_arrayHeaderSize + bodySize); \
                        Address oop   = allocateFast(allocSize, klass, length); \
                        if (oop != null) { \
                            pushAddress(oop); \
                            updateNewHits(); \
                            nextbytecode(); \
                        } \
                    } \
                } \
                pushInt(length); \
                pushAddress(klass); \
            } \
            call(com_sun_squawk_VM_newarray); \
        }

        /**
         * Allocate a new array dimension.
         *
         * <p>
         * Java Stack: OOP, SIZE -> ..., OOP
         * <p>
         */
#define  do_newdimension() {  \
            call(com_sun_squawk_VM_newdimension); \
        }

        /**
         * Instanceof.
         *
         * <p>
         * Java Stack: ..., OOP, CLASS -> ..., INT
         * <p>
         */
#define  do_instanceof() {  \
            Address klass = popAddress(); \
            Address obj   = popAddress(); \
            checkReferenceSlots(); \
            if (obj == null || klass == null) { \
                pushInt(false); \
            } else { \
                pushInt(VM_instanceof(obj, klass)); \
            } \
        }

        /**
         * Checkcast.
         *
         * <p>
         * Java Stack: ..., OOP, CLASS -> ..., OOP
         * <p>
         */
#define  do_checkcast() {  \
            Address klass = popAddress(); \
            Address obj   = popAddress(); \
            checkReferenceSlots(); \
            if (obj != null && !VM_instanceof(obj, klass)) { \
                pushAddress(obj); \
                pushAddress(klass); \
                call(com_sun_squawk_VM_checkcastException); \
            } else { \
                pushAddress(obj); \
            } \
        }

        /**
         * Lookup.
         *
         * <p>
         * Java Stack: KEY, ARRAY -> VALUE
         * <p>
         */
#define  do_lookup_b() {  \
            Address barray = popAddress(); \
            int  key = popInt(); \
            pushInt(VM_lookup_b(key, barray)); \
        }
#define  do_lookup_s() {  \
            Address sarray = popAddress(); \
            int  key = popInt(); \
            pushInt(VM_lookup_s(key, sarray)); \
        }
#define  do_lookup_i() {  \
            Address iarray = popAddress(); \
            int  key = popInt(); \
            pushInt(VM_lookup_i(key, iarray)); \
        }

        /**
         * Reserved.
         *
         * <p>
         * Compiler Stack: ... -> ...
         * <p>
         *
         * @param n ignored parameter
         */
#define  do_res(n_509) { int  n_510 = n_509;  \
            shouldNotReachHere(); \
        }
        
