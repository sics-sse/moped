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

package com.sun.squawk.util;

/*if[JAVA5SYNTAX]*/
import com.sun.squawk.Vm2c;
/*end[JAVA5SYNTAX]*/

/**
 * This class provides mechanisms for manipulating a bit set.
 *
 */

public final class BitSet {

    /**
     * The byte array encoding the bit set.
     */
    private byte[] bits;

    /**
     * The number of bytes in the logical size of this BitSet.
     */
    private int bytesInUse;

    /**
     * Specifies if the underlying bit array is owned by the client and therefore cannot be
     * extended.
     */
    private final boolean bitsAreExternal;

    /**
     * Creates a new BitSet instance whose underlying byte array is controlled by the instance.
     * Only this type of BitSet will grow as necessary and will never throw an IndexOutOfBoundsException
     * if a given bitIndex is greater than the current physical size of the underlying byte array.
     */
    public BitSet() {
        this.bits = new byte[10];
        this.bytesInUse = 0;
        this.bitsAreExternal = false;
    }

    /**
     * Creates a new BitSet instance whose underlying byte array is controlled by the client of the instance.
     * This type of BitSet will throw an IndexOutOfBoundsException if a given bitIndex
     * is greater than the highest bit that can be expressed in the underlying byte array.
     *
     * @param bits  the underlying byte array
     */
    public BitSet(byte[] bits) {
        this.bits = bits;
        this.bitsAreExternal = true;

        // calculate bytesInUse
        for (int i = bits.length - 1; i >= 0; --i) {
            if (bits[i] != 0) {
                bytesInUse = i + 1;
                break;
            }
        }

    }

    public boolean areBitsExternal() {
        return bitsAreExternal;
    }

    /**
     * Determines if a given bit index is valid.
     *
     * @param bitIndex the bit index to test
     * @throws IndexOutOfBoundsException is the given index is negative
     */
    protected void validateIndex(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Copies the bit set representation into a provided byte array. The number of bytes
     * copied is equal to the minimum of length of the provided byte array and the internal
     * byte array.
     *
     * @param bits  the byte array to copy into
     * @throws NullPointerException if the given buffer is null.
     */
    public void copyInto(byte[] bits) {
        int length = bits.length;
        if (length > this.bits.length) {
            length = this.bits.length;
        }
        System.arraycopy(this.bits, 0, bits, 0, length);
    }

    /**
     * Returns the "logical size" of this <code>BitSet</code>: the index of
     * the highest set bit in the <code>BitSet</code> plus one.
     *
     * @return  the logical size of this <code>BitSet</code>.
     */
    public int length() {
        if (bytesInUse == 0) {
            return 0;
        }

        int highestUnit = bits[bytesInUse - 1] & 0xFF;
        int hiBit = 0;
        while (highestUnit != 0) {
            highestUnit >>= 1;
            ++hiBit;
        }
        return 8 * (bytesInUse - 1) + hiBit;
    }


    /**
     * Returns the number of bits of space actually in use by this
     * <code>BitSet</code> to represent bit values.
     *
     * @return  the number of bits currently in this bit set.
     */
    public int size() {
        return bits.length * 8;
    }

    /**
     * Sets the bit at a given index.
     *
     * @param bitIndex  the index of the bit to set
     * @throws IndexOutOfBoundsException if the given bit index is negative or if this is
     *        an {@link #areBitsExternal() external} BitSet instance and <code>bitIndex >= this.size()</code>
     */
    public void set(int bitIndex) {
        validateIndex(bitIndex);

        // calculate the index of the relevant byte in the map
        int byteIndex = bitIndex / 8;

        // extend the byte array if necessary
        ensureCapacity(bitIndex);

        if (byteIndex >= bytesInUse) {
            bytesInUse = byteIndex + 1;
        }

        // set the relevant bit
        byte bit = (byte)(1 << (bitIndex % 8));
        bits[byteIndex] |= bit;
    }

    /**
     * Ensures that the capacity of this bit set is equal to a given minimum.
     *
     * @param bitIndex       the index of the bit being accessed in the caller
     * @throws IndexOutOfBoundsException if this is an {@link #areBitsExternal() external}
     *              BitSet instance and <code>bitIndex >= this.size()</code>
     */
    private void ensureCapacity(int bitIndex) throws IndexOutOfBoundsException {
        int bytesRequired = (bitIndex / 8) + 1;
        if (bits.length < bytesRequired) {
            grow(bitIndex, bytesRequired);
        }
    }

    /**
     * Grows the bit set.
     *
     * @param bitIndex       the index of the bit being accessed that caused the growth
     * @param bytesRequired  the minimum new size
     * @throws IndexOutOfBoundsException if this is an {@link #areBitsExternal() external}
     *              BitSet instance and <code>bitIndex >= this.size()</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="fatalVMError(\"cannot grow bit set\");")
/*end[JAVA5SYNTAX]*/
    private void grow(int bitIndex, int bytesRequired) throws IndexOutOfBoundsException {
        // Cannot grow a bit set whose bits are external
        if (bitsAreExternal) {
            throw new IndexOutOfBoundsException();
        }

        // Allocate larger of doubled size or required size
        int request = Math.max(2 * bits.length, bytesRequired);
        byte newBits[] = new byte[request];
        System.arraycopy(bits, 0, newBits, 0, bytesInUse);
        bits = newBits;
    }

    /**
     * Clears the bit at a given index.
     *
     * @param  bitIndex  the index of the bit to clear
     * @throws IndexOutOfBoundsException if the given bit index is negative
     */
    public void clear(int bitIndex) {
        validateIndex(bitIndex);

        // calculate the index of the relevant byte in the map
        int byteIndex = bitIndex / 8;
        if (byteIndex < bytesInUse) {
            // clear the relevant bit
            byte bit = (byte)(1 << (bitIndex % 8));
            bits[byteIndex] &= ~bit;
        }
    }

    /**
     * Clears all of the bits in this BitSet.
     */
    public void clear() {
        while (bytesInUse > 0) {
            bits[--bytesInUse] = 0;
        }
    }

    /**
     * Returns the value of the bit with the specified index. The value
     * is <code>true</code> if the bit with the index <code>bitIndex</code>
     * is currently set in this <code>BitSet</code>; otherwise, the result
     * is <code>false</code>.
     *
     * @param     bitIndex   the bit index.
     * @return    the value of the bit with the specified index.
     * @throws IndexOutOfBoundsException if the given bit index is negative
     */
    public boolean get(int bitIndex) {
        validateIndex(bitIndex);

        boolean result = false;
        int byteIndex = bitIndex / 8;
        if (byteIndex < bytesInUse) {
            // clear the relevant bit
            byte bit = (byte)(1 << (bitIndex % 8));
            result = (bits[byteIndex] & bit) != 0;
        }
        return result;
    }

    /**
     * Returns the number of bits set to 1 in this <code>BitSet</code>.
     *
     * @return  the number of bits set to 1 in this <code>BitSet</code>.
     */
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < bytesInUse; i++) {
            sum += (int)BIT_COUNT.charAt(((int)bits[i]) & 0xFF);
        }
        return sum;
    }

    /**
     * Performs a logical <b>OR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit at index
     * <i>n</i> in this set is 1 if and only if it was already set
     * or the bit at index <i>n</i> in <code>other</code>
     * was set. The semantics of this operation can be expressed as:
     * <p><hr><blockquote><pre>
     *      this = this | other;
     * </pre></blockquote><hr>
     *
     * @param   other    a bit set
     */
    public void or(BitSet other) {
        or(other, 0);
    }

    /**
     * Performs a logical <b>OR</b> of this bit set with a given bit set.
     * The semantics of this operation can be expressed as:
     * <p><hr><blockquote><pre>
     *      this = this | (other >= 0 ? other << offset : other >> offset);
     * </pre></blockquote><hr>
     *
     * @param   other  a bit set
     * @param   shift  the amount by which other should be logically shifted before being or'ed with this bit set
     */
    public void or(BitSet other, int shift) {
        if (this == other) {
            return;
        }

        if (shift >= 0) {
            ensureCapacity(other.bytesInUse + ( (shift + 7) / 8));
            for (int bitIndex = other.nextSetBit(0); bitIndex != -1; bitIndex = other.nextSetBit(bitIndex + 1)) {
                set(bitIndex + shift);
            }
        } else {
            shift = -shift;
            ensureCapacity(other.bytesInUse - ( (shift + 7) / 8));
            for (int bitIndex = other.nextSetBit(shift); bitIndex != -1; bitIndex = other.nextSetBit(bitIndex + 1)) {
                set(bitIndex - shift);
            }
        }
    }

    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and is a <code>Bitset</code> object that has
     * exactly the same set of bits set to <code>true</code> as this bit
     * set. That is, for every nonnegative <code>int</code> index <code>k</code>,
     * <pre>((BitSet)obj).get(k) == this.get(k)</pre>
     * must be true. The current sizes of the two bit sets are not compared.
     * <p>Overrides the <code>equals</code> method of <code>Object</code>.
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     * @see     java.util.BitSet#size()
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof BitSet)) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        BitSet set = (BitSet)obj;
        int minBytesInUse = Math.min(bytesInUse, set.bytesInUse);

        // Check bytes in use by both BitSets
        for (int i = 0; i < minBytesInUse; i++) {
            if (bits[i] != set.bits[i]) {
                return false;
            }
        }

        // Check any bytes in use by only one BitSet (must be 0 in other)
        if (bytesInUse > minBytesInUse) {
            for (int i = minBytesInUse; i < bytesInUse; i++) {
                if (bits[i] != 0) {
                    return false;
                }
            }
        } else {
            for (int i = minBytesInUse; i < set.bytesInUse; i++) {
                if (set.bits[i] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns a hash code value for this bit set.
     *
     * @return  a hash code value for this bit set.
     */
    public int hashCode() {
        int h = 1234;
        for (int i = bits.length; --i >= 0;) {
            h ^= bits[i] * (i + 1);
        }
        return h;
    }

    /**
     * Returns the index of the first bit that is set to <code>true</code>
     * that occurs on or after the specified starting index. If no such
     * bit exists then -1 is returned.
     *
     * To iterate over the <code>true</code> bits use the following loop:
     *
     * <p><blockquote><pre>
     * for (int i = oopMap.nextSetBit(0); i >= 0; i = oopMap.nextSetBit(i + 1)) {
     *     // operate on index i here
     * }
     * </pre></blockquote>
     *
     * @param   fromIndex the index to start checking from (inclusive). This must be positive
     * @return  the index of the next set bit.
     * @throws  IndexOutOfBoundsException if the specified index is negative.
     */
    public int nextSetBit(int fromIndex) {
        validateIndex(fromIndex);

        int byteIndex = fromIndex / 8;
        if (byteIndex >= bytesInUse) {
            return -1;
        }

        int bitIndex = fromIndex % 8;
        while (byteIndex != bytesInUse) {
            byte bitSetUnit = bits[byteIndex];

            if ((int)BIT_COUNT.charAt((bitSetUnit) & 0xFF) != 0) {
                while (bitIndex != 8) {
                    if ((bitSetUnit & (1 << bitIndex)) != 0) {
                        return (byteIndex * 8) + bitIndex;
                    }
                    ++bitIndex;
                }
            }
            ++byteIndex;
            bitIndex = 0;
        }

        return -1;
    }
    /**
     * Returns a string representation of this bit set. For every index
     * for which this <code>BitSet</code> contains a bit in the set
     * state, the decimal representation of that index is included in
     * the result. Such indices are listed in order from lowest to
     * highest, separated by ",&nbsp;" (a comma and a space) and
     * surrounded by braces, resulting in the usual mathematical
     * notation for a set of integers.<p>
     * Overrides the <code>toString</code> method of <code>Object</code>.
     * <p>Example:
     * <pre>
     * BitSet drPepper = new BitSet();</pre>
     * Now <code>drPepper.toString()</code> returns "<code>{}</code>".<p>
     * <pre>
     * drPepper.set(2);</pre>
     * Now <code>drPepper.toString()</code> returns "<code>{2}</code>".<p>
     * <pre>
     * drPepper.set(4);
     * drPepper.set(10);</pre>
     * Now <code>drPepper.toString()</code> returns "<code>{2, 4, 10}</code>".
     *
     * @return  a string representation of this bit set.
     */
    public String toString() {
        int numBits = bytesInUse * 8;
        StringBuffer buffer = new StringBuffer(8 * numBits + 2);
        String separator = "";
        buffer.append('{');

        for (int i = 0; i < numBits; i++) {
            if (get(i)) {
                buffer.append(separator);
                separator = ", ";
                buffer.append(i);
            }
        }

        buffer.append('}');
        return buffer.toString();
    }

/*if[DEBUG_CODE_ENABLED]*/
    public static void main(String[] args) {
        BitSet bs = new BitSet();
        for (int i = 0; i != 256; ++i) {
            Assert.that(!bs.get(i));
            Assert.that(bs.cardinality() == i);
            bs.set(i);
            Assert.that(bs.length() == i + 1);
            Assert.that(bs.get(i));
            Assert.that(bs.cardinality() == i + 1);
        }

        bs = new BitSet(new byte[] { -1 });
        try {
            bs.set(8);
            Assert.shouldNotReachHere();
        } catch (IndexOutOfBoundsException e) {
        }

        bs = new BitSet(); /* 00001111 */
        bs.set(0);
        bs.set(1);
        bs.set(2);
        bs.set(3);

        BitSet other = new BitSet(new byte[] { 7 /* 00000111 */} );

        bs.or(other, 5);
        BitSet expected = new BitSet(new byte[] { (byte)239 /* 11101111 */} );
        Assert.that(bs.equals(expected), "bs = " + bs + ", expected = " + expected);
    }
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * A table to enable fast counting of the bits set in a byte value.
     * This is represented as a private String constant so that this class does not
     * have a static initializer. This table was
     * generated by capturing the output of the following code:
     *
     * <p><hr><blockquote><pre>
        System.out.println("    static private final String BIT_COUNT =");
        for (int i = 0; i != 256; ++i) {
            int bitCount;
            int n = i;
            for (bitCount = 0; n != 0; bitCount++) {
                n &= n - 1;
            }

            if ((i % 4) == 0) {
                System.out.println();
                System.out.print("        ");
            }

            System.out.print("/*" + i + "*" + "/\"\\u000" + bitCount + "\"");
            if (i != 255) {
                System.out.print(" + ");
            }

        }
     * </pre></blockquote><hr>
     */
    private static final String BIT_COUNT =
        /*0*/"\u0000" + /*1*/"\u0001" + /*2*/"\u0001" + /*3*/"\u0002" +
        /*4*/"\u0001" + /*5*/"\u0002" + /*6*/"\u0002" + /*7*/"\u0003" +
        /*8*/"\u0001" + /*9*/"\u0002" + /*10*/"\u0002" + /*11*/"\u0003" +
        /*12*/"\u0002" + /*13*/"\u0003" + /*14*/"\u0003" + /*15*/"\u0004" +
        /*16*/"\u0001" + /*17*/"\u0002" + /*18*/"\u0002" + /*19*/"\u0003" +
        /*20*/"\u0002" + /*21*/"\u0003" + /*22*/"\u0003" + /*23*/"\u0004" +
        /*24*/"\u0002" + /*25*/"\u0003" + /*26*/"\u0003" + /*27*/"\u0004" +
        /*28*/"\u0003" + /*29*/"\u0004" + /*30*/"\u0004" + /*31*/"\u0005" +
        /*32*/"\u0001" + /*33*/"\u0002" + /*34*/"\u0002" + /*35*/"\u0003" +
        /*36*/"\u0002" + /*37*/"\u0003" + /*38*/"\u0003" + /*39*/"\u0004" +
        /*40*/"\u0002" + /*41*/"\u0003" + /*42*/"\u0003" + /*43*/"\u0004" +
        /*44*/"\u0003" + /*45*/"\u0004" + /*46*/"\u0004" + /*47*/"\u0005" +
        /*48*/"\u0002" + /*49*/"\u0003" + /*50*/"\u0003" + /*51*/"\u0004" +
        /*52*/"\u0003" + /*53*/"\u0004" + /*54*/"\u0004" + /*55*/"\u0005" +
        /*56*/"\u0003" + /*57*/"\u0004" + /*58*/"\u0004" + /*59*/"\u0005" +
        /*60*/"\u0004" + /*61*/"\u0005" + /*62*/"\u0005" + /*63*/"\u0006" +
        /*64*/"\u0001" + /*65*/"\u0002" + /*66*/"\u0002" + /*67*/"\u0003" +
        /*68*/"\u0002" + /*69*/"\u0003" + /*70*/"\u0003" + /*71*/"\u0004" +
        /*72*/"\u0002" + /*73*/"\u0003" + /*74*/"\u0003" + /*75*/"\u0004" +
        /*76*/"\u0003" + /*77*/"\u0004" + /*78*/"\u0004" + /*79*/"\u0005" +
        /*80*/"\u0002" + /*81*/"\u0003" + /*82*/"\u0003" + /*83*/"\u0004" +
        /*84*/"\u0003" + /*85*/"\u0004" + /*86*/"\u0004" + /*87*/"\u0005" +
        /*88*/"\u0003" + /*89*/"\u0004" + /*90*/"\u0004" + /*91*/"\u0005" +
        /*92*/"\u0004" + /*93*/"\u0005" + /*94*/"\u0005" + /*95*/"\u0006" +
        /*96*/"\u0002" + /*97*/"\u0003" + /*98*/"\u0003" + /*99*/"\u0004" +
        /*100*/"\u0003" + /*101*/"\u0004" + /*102*/"\u0004" + /*103*/"\u0005" +
        /*104*/"\u0003" + /*105*/"\u0004" + /*106*/"\u0004" + /*107*/"\u0005" +
        /*108*/"\u0004" + /*109*/"\u0005" + /*110*/"\u0005" + /*111*/"\u0006" +
        /*112*/"\u0003" + /*113*/"\u0004" + /*114*/"\u0004" + /*115*/"\u0005" +
        /*116*/"\u0004" + /*117*/"\u0005" + /*118*/"\u0005" + /*119*/"\u0006" +
        /*120*/"\u0004" + /*121*/"\u0005" + /*122*/"\u0005" + /*123*/"\u0006" +
        /*124*/"\u0005" + /*125*/"\u0006" + /*126*/"\u0006" + /*127*/"\u0007" +
        /*128*/"\u0001" + /*129*/"\u0002" + /*130*/"\u0002" + /*131*/"\u0003" +
        /*132*/"\u0002" + /*133*/"\u0003" + /*134*/"\u0003" + /*135*/"\u0004" +
        /*136*/"\u0002" + /*137*/"\u0003" + /*138*/"\u0003" + /*139*/"\u0004" +
        /*140*/"\u0003" + /*141*/"\u0004" + /*142*/"\u0004" + /*143*/"\u0005" +
        /*144*/"\u0002" + /*145*/"\u0003" + /*146*/"\u0003" + /*147*/"\u0004" +
        /*148*/"\u0003" + /*149*/"\u0004" + /*150*/"\u0004" + /*151*/"\u0005" +
        /*152*/"\u0003" + /*153*/"\u0004" + /*154*/"\u0004" + /*155*/"\u0005" +
        /*156*/"\u0004" + /*157*/"\u0005" + /*158*/"\u0005" + /*159*/"\u0006" +
        /*160*/"\u0002" + /*161*/"\u0003" + /*162*/"\u0003" + /*163*/"\u0004" +
        /*164*/"\u0003" + /*165*/"\u0004" + /*166*/"\u0004" + /*167*/"\u0005" +
        /*168*/"\u0003" + /*169*/"\u0004" + /*170*/"\u0004" + /*171*/"\u0005" +
        /*172*/"\u0004" + /*173*/"\u0005" + /*174*/"\u0005" + /*175*/"\u0006" +
        /*176*/"\u0003" + /*177*/"\u0004" + /*178*/"\u0004" + /*179*/"\u0005" +
        /*180*/"\u0004" + /*181*/"\u0005" + /*182*/"\u0005" + /*183*/"\u0006" +
        /*184*/"\u0004" + /*185*/"\u0005" + /*186*/"\u0005" + /*187*/"\u0006" +
        /*188*/"\u0005" + /*189*/"\u0006" + /*190*/"\u0006" + /*191*/"\u0007" +
        /*192*/"\u0002" + /*193*/"\u0003" + /*194*/"\u0003" + /*195*/"\u0004" +
        /*196*/"\u0003" + /*197*/"\u0004" + /*198*/"\u0004" + /*199*/"\u0005" +
        /*200*/"\u0003" + /*201*/"\u0004" + /*202*/"\u0004" + /*203*/"\u0005" +
        /*204*/"\u0004" + /*205*/"\u0005" + /*206*/"\u0005" + /*207*/"\u0006" +
        /*208*/"\u0003" + /*209*/"\u0004" + /*210*/"\u0004" + /*211*/"\u0005" +
        /*212*/"\u0004" + /*213*/"\u0005" + /*214*/"\u0005" + /*215*/"\u0006" +
        /*216*/"\u0004" + /*217*/"\u0005" + /*218*/"\u0005" + /*219*/"\u0006" +
        /*220*/"\u0005" + /*221*/"\u0006" + /*222*/"\u0006" + /*223*/"\u0007" +
        /*224*/"\u0003" + /*225*/"\u0004" + /*226*/"\u0004" + /*227*/"\u0005" +
        /*228*/"\u0004" + /*229*/"\u0005" + /*230*/"\u0005" + /*231*/"\u0006" +
        /*232*/"\u0004" + /*233*/"\u0005" + /*234*/"\u0005" + /*235*/"\u0006" +
        /*236*/"\u0005" + /*237*/"\u0006" + /*238*/"\u0006" + /*239*/"\u0007" +
        /*240*/"\u0004" + /*241*/"\u0005" + /*242*/"\u0005" + /*243*/"\u0006" +
        /*244*/"\u0005" + /*245*/"\u0006" + /*246*/"\u0006" + /*247*/"\u0007" +
        /*248*/"\u0005" + /*249*/"\u0006" + /*250*/"\u0006" + /*251*/"\u0007" +
        /*252*/"\u0006" + /*253*/"\u0007" + /*254*/"\u0007" + /*255*/"\u0008";

}
