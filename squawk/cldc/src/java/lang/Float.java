//if[FLOATS]   /* This will selectively exclude the entire file from the build */
/*
 * Copyright 1994-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package java.lang;

import com.sun.squawk.*;

/**
 * The Float class provides an object wrapper for Float data values, and serves as
 * a place for float-oriented operations.  A wrapper is useful because most of Java's
 * utility classes require the use of objects.  Since floats are not objects in
 * Java, they need to be "wrapped" in a Float instance.
 * @version     1.32, 22 Dec 1995
 */
public final
class Float /*extends Number*/ {
    /**
     * Positive infinity.
     */
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

    /**
     * Negative infinity.
     */
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

    /**
     * Not-a-Number. Is not equal to anything, including
     * itself.
     */
    public static final float NaN = 0.0f / 0.0f;


    /**
     * The maximum value a float can have.  The largest maximum value possible is
     * 3.40282346638528860e+38.
     */
    public static final float MAX_VALUE = 3.40282346638528860e+38f;

    /**
     * The minimum value a float can have.  The lowest minimum value possible is
     * 1.40129846432481707e-45.
     */
    public static final float MIN_VALUE = 1.40129846432481707e-45f;

    /**
     * Returns a String representation for the specified float value.
     * @param f the float to be converted
     */
    public static String toString(float f){
        return new FloatingDecimal(f).toJavaFormatString();
    }

    /**
     * Returns the floating point value represented by the specified String.
     * @param s     the String to be parsed
     * @exception   NumberFormatException If the String does not contain a parsable
     * Float.
     */
    public static Float valueOf(String s) throws NumberFormatException {
        return new Float(FloatingDecimal.readJavaFormatString(s).floatValue());
    }

    /**
     * Returns true if the specified number is the special Not-a-Number (NaN) value.
     * @param v the value to be tested
     */
    static public boolean isNaN(float v) {
        return (v != v);
    }

    /**
     * Returns true if the specified number is infinitely large in magnitude.
     * @param v the value to be tested
     */
    static public boolean isInfinite(float v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }

    /**
     * The value of the Float.
     */
    private float value;

    /**
     * Constructs a Float wrapper for the specified float value.
     * @param value the value of the Float
     */
    public Float(float value) {
        this.value = value;
    }

    /**
     * Constructs a Float wrapper for the specified double value.
     * @param value the value of the Float
     */
    public Float(double value) {
        this.value = (float)value;
    }

    /**
     * Returns a new float initialized to the value represented by the
     * specified <code>String</code>, as performed by the <code>valueOf</code>
     * method of class <code>Double</code>.
     *
     * @param      s   the string to be parsed.
     * @return     the float value represented by the string argument.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable float.
     * @see        java.lang.Double#valueOf(String)
     * @since      JDK1.2
     */
    public static float parseFloat(String s) throws NumberFormatException {
        return FloatingDecimal.readJavaFormatString(s).floatValue();
    }

    /**
     * Returns true if this Float value is Not-a-Number (NaN).
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Returns true if this Float value is infinitely large in magnitude.
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Returns a String representation of this Float object.
     */
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Returns the value of this Float as a byte (by casting to a byte).
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * Returns the value of this Float as a short (by casting to a short).
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * Returns the integer value of this Float (by casting to an int).
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * Returns the long value of this Float (by casting to a long).
     */
    public long longValue() {
        return (long)value;
    }

/*if[FLOATS]*/
    /**
     * Returns the float value of this Float object.
     */
    public float floatValue() {
        return value;
    }

    /**
     * Returns the double value of this Float.
     */
    public double doubleValue() {
        return (double)value;
    }
/*end[FLOATS]*/

    /**
     * Returns a hashcode for this Float.
     */
    public int hashCode() {
        return VM.floatToIntBits(value);
    }

    /**
     * Compares this object against some other object.
     * To be useful in hashtables this method
     * considers two Nan floating point values to be equal. This
     * is not according to IEEE specification.
     *
     * @param obj       the object to compare with
     * @return      true if the objects are the same; false otherwise.
     */
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Float) && (VM.floatToIntBits(((Float)obj).value) == VM.floatToIntBits(value));
    }

    /**
     * Returns the bit represention of a single-float value
     */
    public static int floatToIntBits(float value) {
        return VM.floatToIntBits(value);
    }

    /**
     * Returns the single-float corresponding to a given bit represention.
     */
    public static float intBitsToFloat(int bits) {
        return VM.intBitsToFloat(bits);
    }

/*if[JAVA5SYNTAX]*/
    @Java5Marker
/*end[JAVA5SYNTAX]*/
    public static Float valueOf(final float val) {
        return new Float(val);
    }

}
