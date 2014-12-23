//if[JAVA5SYNTAX]*/
package java.lang;

import com.sun.squawk.Java5Marker;

/**
 * A mutable sequence of characters.  This class provides an API compatible
 * with <code>StringBuffer</code>, but with no guarantee of synchronization.
 * This class is designed for use as a drop-in replacement for
 * <code>StringBuffer</code> in places where the string buffer was being
 * used by a single thread (as is generally the case).   Where possible,
 * it is recommended that this class be used in preference to
 * <code>StringBuffer</code> as it will be faster under most implementations.
 * 
 * <p>The principal operations on a <code>StringBuilder</code> are the 
 * <code>append</code> and <code>insert</code> methods, which are 
 * overloaded so as to accept data of any type. Each effectively 
 * converts a given datum to a string and then appends or inserts the 
 * characters of that string to the string builder. The 
 * <code>append</code> method always adds these characters at the end 
 * of the builder; the <code>insert</code> method adds the characters at 
 * a specified point. 
 * <p>
 * For example, if <code>z</code> refers to a string builder object 
 * whose current contents are "<code>start</code>", then 
 * the method call <code>z.append("le")</code> would cause the string 
 * builder to contain "<code>startle</code>", whereas 
 * <code>z.insert(4, "le")</code> would alter the string builder to 
 * contain "<code>starlet</code>". 
 * <p>
 * In general, if sb refers to an instance of a <code>StringBuilder</code>, 
 * then <code>sb.append(x)</code> has the same effect as 
 * <code>sb.insert(sb.length(),&nbsp;x)</code>.
 *
 * Every string builder has a capacity. As long as the length of the 
 * character sequence contained in the string builder does not exceed 
 * the capacity, it is not necessary to allocate a new internal 
 * buffer. If the internal buffer overflows, it is automatically made larger.
 *
 * <p>Instances of <code>StringBuilder</code> are not safe for
 * use by multiple threads. If such synchronization is required then it is
 * recommended that {@link java.lang.StringBuffer} be used. 
 * <p>
 * This Java Card class is a subset of the JDK 1.5 StringBuilder class. Some
 * interfaces, methods and/or variables have been pruned, and/or other methods
 * simplified, in an effort to reduce the size of this class and/or eliminate
 * dependencies on unsupported features.
 * 
 * @author  Michael McCloskey
 * @version     1.9, 07/16/04
 * @see         java.lang.StringBuffer
 * @see         java.lang.String
 * @since   JDK 1.5, Java Card 3.0
 */
@Java5Marker
public final class StringBuilder {
    /**
     * The value is used for character storage.
     */
    char value[];

    /** 
     * The count is the number of characters used.
     */
    int count;

    /**
     * Constructs a string builder with no characters in it and an 
     * initial capacity of 16 characters. 
     */
    public StringBuilder() {
        this(16);
    }

    /**
     * Constructs a string builder with no characters in it and an 
     * initial capacity specified by the <code>capacity</code> argument. 
     *
     * @param      capacity  the initial capacity.
     * @throws     NegativeArraySizeException  if the <code>capacity</code>
     *               argument is less than <code>0</code>.
     */
    public StringBuilder(int capacity) {
        value = new char[capacity];
    }

    /**
     * Constructs a string builder initialized to the contents of the 
     * specified string. The initial capacity of the string builder is 
     * <code>16</code> plus the length of the string argument.  
     *
     * @param   str   the initial contents of the buffer.
     * @throws    NullPointerException if <code>str</code> is <code>null</code>
     */
    public StringBuilder(String str) {
        this(str.length() + 16);
        append(str);
    }

    /**
     * Returns the length (character count).
     *
     * @return  the length of the sequence of characters currently 
     *          represented by this object
     */
    public int length() {
        return count;
    }

    /**
     * Returns the current capacity. The capacity is the amount of storage 
     * available for newly inserted characters, beyond which an allocation 
     * will occur.
     *
     * @return  the current capacity
     */
    public int capacity() {
        return value.length;
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum.
     * If the current capacity is less than the argument, then a new internal
     * array is allocated with greater capacity. The new capacity is the 
     * larger of: 
     * <ul>
     * <li>The <code>minimumCapacity</code> argument. 
     * <li>Twice the old capacity, plus <code>2</code>. 
     * </ul>
     * If the <code>minimumCapacity</code> argument is nonpositive, this
     * method takes no action and simply returns.
     *
     * @param   minimumCapacity   the minimum desired capacity.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > value.length) {
            expandCapacity(minimumCapacity);
        }
    }

    /**
     * This implements the expansion semantics of ensureCapacity with no
     * size check or synchronization.
     */
    private void expandCapacity(int minimumCapacity) {
        int newCapacity = (value.length + 1) * 2;
        if (newCapacity < 0) {
            newCapacity = Integer.MAX_VALUE;
        } else if (minimumCapacity > newCapacity) {
            newCapacity = minimumCapacity;
        }
        char newValue[] = new char[newCapacity];
        System.arraycopy(value, 0, newValue, 0, count);
        value = newValue;
    }

    /**
     * Attempts to reduce storage used for the character sequence.
     * If the buffer is larger than necessary to hold its current sequence of 
     * characters, then it may be resized to become more space efficient. 
     * Calling this method may, but is not required to, affect the value 
     * returned by a subsequent call to the {@link #capacity()} method.
     */
    public void trimToSize() { 
        if (count < value.length) {
            char[] newValue = new char[count];
            System.arraycopy(value, 0, newValue, 0, count);
            this.value = newValue;
        }
    }

    /**
     * Sets the length of the character sequence.
     * The sequence is changed to a new character sequence 
     * whose length is specified by the argument. For every nonnegative 
     * index <i>k</i> less than <code>newLength</code>, the character at 
     * index <i>k</i> in the new character sequence is the same as the 
     * character at index <i>k</i> in the old sequence if <i>k</i> is less 
     * than the length of the old character sequence; otherwise, it is the 
     * null character <code>'&#92;u0000'</code>. 
     *  
     * In other words, if the <code>newLength</code> argument is less than 
     * the current length, the length is changed to the specified length.
     * <p>
     * If the <code>newLength</code> argument is greater than or equal 
     * to the current length, sufficient null characters 
     * (<code>'&#92;u0000'</code>) are appended so that 
     * length becomes the <code>newLength</code> argument. 
     * <p>
     * The <code>newLength</code> argument must be greater than or equal 
     * to <code>0</code>. 
     *
     * @param      newLength   the new length
     * @throws     IndexOutOfBoundsException  if the
     *               <code>newLength</code> argument is negative.
     */
    public void setLength(int newLength) {
        if (newLength < 0)
            throw new StringIndexOutOfBoundsException(newLength);
        if (newLength > value.length)
            expandCapacity(newLength);

        if (count < newLength) {
            for (; count < newLength; count++)
                value[count] = '\0';
        } else {
            count = newLength;
        }
    }

    /**
     * Returns the <code>char</code> value in this sequence at the specified index.
     * The first <code>char</code> value is at index <code>0</code>, the next at index
     * <code>1</code>, and so on, as in array indexing.
     * <p>
     * The index argument must be greater than or equal to
     * <code>0</code>, and less than the length of this sequence.
     *
     * @param      index   the index of the desired <code>char</code> value.
     * @return     the <code>char</code> value at the specified index.
     * @throws     IndexOutOfBoundsException  if <code>index</code> is 
     *             negative or greater than or equal to <code>length()</code>.
     */
    public char charAt(int index) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        return value[index];
    }

    /**
     * Characters are copied from this sequence into the 
     * destination character array <code>dst</code>. The first character to 
     * be copied is at index <code>srcBegin</code>; the last character to 
     * be copied is at index <code>srcEnd-1</code>. The total number of 
     * characters to be copied is <code>srcEnd-srcBegin</code>. The 
     * characters are copied into the subarray of <code>dst</code> starting 
     * at index <code>dstBegin</code> and ending at index:
     * <p><blockquote><pre>
     * dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param      srcBegin   start copying at this offset.
     * @param      srcEnd     stop copying at this offset.
     * @param      dst        the array to copy the data into.
     * @param      dstBegin   offset into <code>dst</code>.
     * @throws     NullPointerException if <code>dst</code> is 
     *             <code>null</code>.
     * @throws     IndexOutOfBoundsException  if any of the following is true:
     *             <ul>
     *             <li><code>srcBegin</code> is negative
     *             <li><code>dstBegin</code> is negative
     *             <li>the <code>srcBegin</code> argument is greater than 
     *             the <code>srcEnd</code> argument.
     *             <li><code>srcEnd</code> is greater than 
     *             <code>this.length()</code>.
     *             <li><code>dstBegin+srcEnd-srcBegin</code> is greater than 
     *             <code>dst.length</code>
     *             </ul>
     */
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        if (srcBegin < 0)
            throw new StringIndexOutOfBoundsException(srcBegin);
        if ((srcEnd < 0) || (srcEnd > count))
            throw new StringIndexOutOfBoundsException(srcEnd);
        if (srcBegin > srcEnd)
            throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    /**
     * The character at the specified index is set to <code>ch</code>. This 
     * sequence is altered to represent a new character sequence that is 
     * identical to the old character sequence, except that it contains the 
     * character <code>ch</code> at position <code>index</code>. 
     * <p>
     * The index argument must be greater than or equal to 
     * <code>0</code>, and less than the length of this sequence. 
     *
     * @param      index   the index of the character to modify.
     * @param      ch      the new character.
     * @throws     IndexOutOfBoundsException  if <code>index</code> is 
     *             negative or greater than or equal to <code>length()</code>.
     */
    public void setCharAt(int index, char ch) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        value[index] = ch;
    }

    /**
     * @see     java.lang.String#valueOf(java.lang.Object)
     * @see     #append(java.lang.String)
     */
    public StringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    /**
     * Appends the specified string to this character sequence.
     * <p>
     * The characters of the <code>String</code> argument are appended, in 
     * order, increasing the length of this sequence by the length of the 
     * argument. If <code>str</code> is <code>null</code>, then the four 
     * characters <code>"null"</code> are appended.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to 
     * execution of the <code>append</code> method. Then the character at 
     * index <i>k</i> in the new character sequence is equal to the character 
     * at index <i>k</i> in the old character sequence, if <i>k</i> is less 
     * than <i>n</i>; otherwise, it is equal to the character at index 
     * <i>k-n</i> in the argument <code>str</code>.
     *
     * @param   str   a string.
     * @return  a reference to this object.
     */
    public StringBuilder append(String str) {
        if (str == null)
            str = "null";
        int len = str.length();
        if (len == 0)
            return this;
        int newCount = count + len;
        if (newCount > value.length)
            expandCapacity(newCount);
        str.getChars(0, len, value, count);
        count = newCount;
        return this;
    }

    /**
     * Appends the specified <tt>StringBuffer</tt> to this sequence.
     * <p>
     * The characters of the <tt>StringBuffer</tt> argument are appended, 
     * in order, to this sequence, increasing the 
     * length of this sequence by the length of the argument. 
     * If <tt>sb</tt> is <tt>null</tt>, then the four characters 
     * <tt>"null"</tt> are appended to this sequence.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to 
     * execution of the <tt>append</tt> method. Then the character at index 
     * <i>k</i> in the new character sequence is equal to the character at 
     * index <i>k</i> in the old character sequence, if <i>k</i> is less than 
     * <i>n</i>; otherwise, it is equal to the character at index <i>k-n</i> 
     * in the argument <code>sb</code>.
     *
     * @param   sb   the <tt>StringBuffer</tt> to append.
     * @return  a reference to this object.
     */
    public StringBuilder append(StringBuffer sb) { 
        if (sb == null)
            return append("null");
        int len = sb.length();
        int newCount = count + len;
        if (newCount > value.length)
            expandCapacity(newCount);
        sb.getChars(0, len, value, count);
        count = newCount;
        return this;
    }

    /**
     * Appends the string representation of the <code>char</code> array 
     * argument to this sequence. 
     * <p>
     * The characters of the array argument are appended, in order, to 
     * the contents of this sequence. The length of this sequence
     * increases by the length of the argument. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char[])} and the 
     * characters of that string were then {@link #append(String) appended} 
     * to this character sequence.
     *
     * @param   str   the characters to be appended.
     * @return  a reference to this object.
     */
    public StringBuilder append(char str[]) {
        int newCount = count + str.length;
        if (newCount > value.length)
            expandCapacity(newCount);
        System.arraycopy(str, 0, value, count, str.length);
        count = newCount;
        return this;
    }

    /**
     * Appends the string representation of a subarray of the
     * <code>char</code> array argument to this sequence.
     * <p>
     * Characters of the <code>char</code> array <code>str</code>, starting at
     * index <code>offset</code>, are appended, in order, to the contents
     * of this sequence. The length of this sequence increases
     * by the value of <code>len</code>.
     * <p>
     * The overall effect is exactly as if the arguments were converted to
     * a string by the method {@link String#valueOf(char[],int,int)} and the
     * characters of that string were then {@link #append(String) appended}
     * to this character sequence.
     *
     * @param   str      the characters to be appended.
     * @param   offset   the index of the first <code>char</code> to append.
     * @param   len      the number of <code>char</code>s to append.
     * @return  a reference to this object.
     */
    public StringBuilder append(char str[], int offset, int len) {
        int newCount = count + len;
        if (newCount > value.length)
            expandCapacity(newCount);
        System.arraycopy(str, offset, value, count, len);
        count = newCount;
        return this;
    }

    /**
     * Appends the string representation of the <code>boolean</code> 
     * argument to the sequence.
     * <p>
     * The argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then appended to this sequence. 
     *
     * @param   b   a <code>boolean</code>.
     * @return  a reference to this object.
     */
    public StringBuilder append(boolean b) {
        if (b) {
            int newCount = count + 4;
            if (newCount > value.length)
                expandCapacity(newCount);
            value[count++] = 't';
            value[count++] = 'r';
            value[count++] = 'u';
            value[count++] = 'e';
        } else {
            int newCount = count + 5;
            if (newCount > value.length)
                expandCapacity(newCount);
            value[count++] = 'f';
            value[count++] = 'a';
            value[count++] = 'l';
            value[count++] = 's';
            value[count++] = 'e';
        }
        return this;
    }

    /**
     * Appends the string representation of the <code>char</code> 
     * argument to this sequence. 
     * <p>
     * The argument is appended to the contents of this sequence. 
     * The length of this sequence increases by <code>1</code>. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char)} and the character 
     * in that string were then {@link #append(String) appended} to this 
     * character sequence.
     *
     * @param   c   a <code>char</code>.
     * @return  a reference to this object.
     */
    public StringBuilder append(char c) {
        int newCount = count + 1;
        if (newCount > value.length)
            expandCapacity(newCount);
        value[count++] = c;
        return this;
    }

    /**
     * Appends the string representation of the <code>int</code> 
     * argument to this sequence. 
     * <p>
     * The argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then appended to this sequence. 
     *
     * @param   i   an <code>int</code>.
     * @return  a reference to this object.
     */
    public StringBuilder append(int i) {
        if (i == Integer.MIN_VALUE) {
            append("-2147483648");
            return this;
        }
        // JC-FIXME: Original code
        //        int appendedLength = (i < 0) ? stringSizeOfInt(-i) + 1 : stringSizeOfInt(i);
        //        int spaceNeeded = count + appendedLength;
        //        if (spaceNeeded > value.length)
        //            expandCapacity(spaceNeeded);
        //        Integer.getChars(i, spaceNeeded, value);
        //        count = spaceNeeded;
        append(Integer.toString(i));
        return this;
    }

    final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

    // Requires positive x
    static int stringSizeOfInt(int x) {
        for (int i = 0;; i++)
            if (x <= sizeTable[i])
                return i + 1;
    }

    /**
     * Appends the string representation of the <code>long</code> 
     * argument to this sequence.
     * <p>
     * The argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then appended to this sequence.
     *
     * @param   l   a <code>long</code>.
     * @return  a reference to this object.
     */
    public StringBuilder append(long l) {
        if (l == Long.MIN_VALUE) {
            append("-9223372036854775808");
            return this;
        }
        // JC-FIXME: Original code
        //        int appendedLength = (l < 0) ? stringSizeOfLong(-l) + 1 : stringSizeOfLong(l);
        //        int spaceNeeded = count + appendedLength;
        //        if (spaceNeeded > value.length)
        //            expandCapacity(spaceNeeded);
        //        Long.getChars(l, spaceNeeded, value);
        //        count = spaceNeeded;
        append(Long.toString(l));
        return this;
    }

    // Requires positive x
    static int stringSizeOfLong(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p)
                return i;
            p = 10 * p;
        }
        return 19;
    }

    /**
     * Removes the characters in a substring of this sequence.
     * The substring begins at the specified <code>start</code> and extends to
     * the character at index <code>end - 1</code> or to the end of the
     * sequence if no such character exists. If
     * <code>start</code> is equal to <code>end</code>, no changes are made.
     *
     * @param      start  The beginning index, inclusive.
     * @param      end    The ending index, exclusive.
     * @return     This object.
     * @throws     StringIndexOutOfBoundsException  if <code>start</code>
     *             is negative, greater than <code>length()</code>, or
     *         greater than <code>end</code>.
     */
    public StringBuilder delete(int start, int end) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (end > count)
            end = count;
        if (start > end)
            throw new StringIndexOutOfBoundsException();
        int len = end - start;
        if (len > 0) {
            System.arraycopy(value, start + len, value, start, count - end);
            count -= len;
        }
        return this;
    }

    /**
     * Removes the <code>char</code> at the specified position in this
     * sequence. This sequence is shortened by one <code>char</code>.
     *
     * <p>Note: If the character at the given index is a supplementary
     * character, this method does not remove the entire character. If
     * correct handling of supplementary characters is required,
     * determine the number of <code>char</code>s to remove by calling
     * <code>Character.charCount(thisSequence.codePointAt(index))</code>,
     * where <code>thisSequence</code> is this sequence.
     *
     * @param       index  Index of <code>char</code> to remove
     * @return      This object.
     * @throws      StringIndexOutOfBoundsException  if the <code>index</code>
     *          is negative or greater than or equal to
     *          <code>length()</code>.
     */
    public StringBuilder deleteCharAt(int index) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        System.arraycopy(value, index + 1, value, index, count - index - 1);
        count--;
        return this;
    }

    /**
     * Replaces the characters in a substring of this sequence
     * with characters in the specified <code>String</code>. The substring
     * begins at the specified <code>start</code> and extends to the character
     * at index <code>end - 1</code> or to the end of the
     * sequence if no such character exists. First the
     * characters in the substring are removed and then the specified
     * <code>String</code> is inserted at <code>start</code>. (This 
     * sequence will be lengthened to accommodate the
     * specified String if necessary.)
     * 
     * @param      start    The beginning index, inclusive.
     * @param      end      The ending index, exclusive.
     * @param      str   String that will replace previous contents.
     * @return     This object.
     * @throws     StringIndexOutOfBoundsException  if <code>start</code>
     *             is negative, greater than <code>length()</code>, or
     *         greater than <code>end</code>.
     */
    public StringBuilder replace(int start, int end, String str) { 
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (start > count)
            throw new StringIndexOutOfBoundsException("start > length()");
        if (start > end)
            throw new StringIndexOutOfBoundsException("start > end");
        if (end > count)
            end = count;

        if (end > count)
            end = count;
        int len = str.length();
        int newCount = count + len - (end - start);
        if (newCount > value.length)
            expandCapacity(newCount);

        System.arraycopy(value, end, value, start + len, count - end);
        // JC-FIXME: original code
        //        str.getChars(value, start);
        str.getChars(0, str.length(), value, start);
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of a subarray of the <code>str</code>
     * array argument into this sequence. The subarray begins at the
     * specified <code>offset</code> and extends <code>len</code> <code>char</code>s.
     * The characters of the subarray are inserted into this sequence at
     * the position indicated by <code>index</code>. The length of this
     * sequence increases by <code>len</code> <code>char</code>s.
     *
     * @param      index    position at which to insert subarray.
     * @param      str       A <code>char</code> array.
     * @param      offset   the index of the first <code>char</code> in subarray to
     *             be inserted.
     * @param      len      the number of <code>char</code>s in the subarray to
     *             be inserted.
     * @return     This object
     * @throws     StringIndexOutOfBoundsException  if <code>index</code>
     *             is negative or greater than <code>length()</code>, or
     *             <code>offset</code> or <code>len</code> are negative, or
     *             <code>(offset+len)</code> is greater than
     *             <code>str.length</code>.
     */
    public StringBuilder insert(int index, char str[], int offset, int len) { 
        if ((index < 0) || (index > length()))
            throw new StringIndexOutOfBoundsException(index);
        if ((offset < 0) || (len < 0) || (offset > str.length - len))
            throw new StringIndexOutOfBoundsException("offset " + offset + ", len " + len + ", str.length "
                    + str.length);
        int newCount = count + len;
        if (newCount > value.length)
            expandCapacity(newCount);
        System.arraycopy(value, index, value, index + len, count - index);
        System.arraycopy(str, offset, value, index, len);
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>Object</code> 
     * argument into this character sequence.
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this sequence at the indicated 
     * offset. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * sequence.
     *
     * @param      offset   the offset.
     * @param      obj      an <code>Object</code>.
     * @return     a reference to this object.
     * @throws     StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public StringBuilder insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    /**
     * Inserts the string into this character sequence.
     * <p>
     * The characters of the <code>String</code> argument are inserted, in 
     * order, into this sequence at the indicated offset, moving up any 
     * characters originally above that position and increasing the length 
     * of this sequence by the length of the argument. If 
     * <code>str</code> is <code>null</code>, then the four characters 
     * <code>"null"</code> are inserted into this sequence.
     * <p>
     * The character at index <i>k</i> in the new character sequence is 
     * equal to:
     * <ul>
     * <li>the character at index <i>k</i> in the old character sequence, if 
     * <i>k</i> is less than <code>offset</code> 
     * <li>the character at index <i>k</i><code>-offset</code> in the 
     * argument <code>str</code>, if <i>k</i> is not less than 
     * <code>offset</code> but is less than <code>offset+str.length()</code> 
     * <li>the character at index <i>k</i><code>-str.length()</code> in the 
     * old character sequence, if <i>k</i> is not less than 
     * <code>offset+str.length()</code>
     * </ul><p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * sequence.
     *
     * @param      offset   the offset.
     * @param      str      a string.
     * @return     a reference to this object.
     * @throws     StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public StringBuilder insert(int offset, String str) {
        if ((offset < 0) || (offset > length()))
            throw new StringIndexOutOfBoundsException(offset);
        if (str == null)
            str = "null";
        int len = str.length();
        int newCount = count + len;
        if (newCount > value.length)
            expandCapacity(newCount);
        System.arraycopy(value, offset, value, offset + len, count - offset);
        //JC-FIXME: Original code
        //        str.getChars(value, offset);
        str.getChars(0, str.length(), value, offset);
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>char</code> array 
     * argument into this sequence.
     * <p>
     * The characters of the array argument are inserted into the 
     * contents of this sequence at the position indicated by 
     * <code>offset</code>. The length of this sequence increases by 
     * the length of the argument. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char[])} and the 
     * characters of that string were then 
     * {@link #insert(int,String) inserted} into this 
     * character sequence at the position indicated by
     * <code>offset</code>.
     *
     * @param      offset   the offset.
     * @param      str      a character array.
     * @return     a reference to this object.
     * @throws     StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public StringBuilder insert(int offset, char str[]) {
        if ((offset < 0) || (offset > length()))
            throw new StringIndexOutOfBoundsException(offset);
        int len = str.length;
        int newCount = count + len;
        if (newCount > value.length)
            expandCapacity(newCount);
        System.arraycopy(value, offset, value, offset + len, count - offset);
        System.arraycopy(str, 0, value, offset, len);
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the <code>boolean</code> 
     * argument into this sequence. 
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this sequence at the indicated 
     * offset. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * sequence. 
     *
     * @param      offset   the offset.
     * @param      b        a <code>boolean</code>.
     * @return     a reference to this object.
     * @throws     StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public StringBuilder insert(int offset, boolean b) {
        return insert(offset, String.valueOf(b));
    }

    /**
     * Inserts the string representation of the <code>char</code> 
     * argument into this sequence. 
     * <p>
     * The second argument is inserted into the contents of this sequence
     * at the position indicated by <code>offset</code>. The length 
     * of this sequence increases by one. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char)} and the character 
     * in that string were then {@link #insert(int, String) inserted} into 
     * this character sequence at the position indicated by
     * <code>offset</code>.
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * sequence. 
     *
     * @param      offset   the offset.
     * @param      c        a <code>char</code>.
     * @return     a reference to this object.
     * @throws     IndexOutOfBoundsException  if the offset is invalid.
     */
    public StringBuilder insert(int offset, char c) {
        int newCount = count + 1;
        if (newCount > value.length)
            expandCapacity(newCount);
        System.arraycopy(value, offset, value, offset + 1, count - offset);
        value[offset] = c;
        count = newCount;
        return this;
    }

    /**
     * Inserts the string representation of the second <code>int</code> 
     * argument into this sequence. 
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this sequence at the indicated 
     * offset. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * sequence. 
     *
     * @param      offset   the offset.
     * @param      i        an <code>int</code>.
     * @return     a reference to this object.
     * @throws     StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public StringBuilder insert(int offset, int i) {
        return insert(offset, String.valueOf(i));
    }

    /**
     * Inserts the string representation of the <code>long</code> 
     * argument into this sequence. 
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this sequence at the position 
     * indicated by <code>offset</code>. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * sequence. 
     *
     * @param      offset   the offset.
     * @param      l        a <code>long</code>.
     * @return     a reference to this object.
     * @throws     StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public StringBuilder insert(int offset, long l) {
        return insert(offset, String.valueOf(l));
    }

    /**
     * Causes this character sequence to be replaced by the reverse of
     * the sequence.
     *
     * Let <i>n</i> be the character length of this character sequence
     * (not the length in <code>char</code> values) just prior to
     * execution of the <code>reverse</code> method. Then the
     * character at index <i>k</i> in the new character sequence is
     * equal to the character at index <i>n-k-1</i> in the old
     * character sequence.
     *
     * @return  a reference to this object.
     */
    public StringBuilder reverse() {
        //JC-FIXME: code taken from StringBuffer
        int n = count - 1;
        for (int j = n - 1 >> 1; j >= 0; --j) {
            char temp = value[j];
            value[j] = value[n - j];
            value[n - j] = temp;
        }
        return this;
    }

    /**
     * Returns a string representing the data in this sequence.
     * A new <code>String</code> object is allocated and initialized to 
     * contain the character sequence currently represented by this 
     * object. This <code>String</code> is then returned. Subsequent 
     * changes to this sequence do not affect the contents of the 
     * <code>String</code>.
     *
     * @return  a string representation of this sequence of characters.
     */
    public String toString() {
        // Create a copy, don't share the array
        return new String(value, 0, count);
    }

}
