//if[FLOATS]
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

package com.sun.squawk.util;


/* This is the new Math Library in Java
 * mostly copied from the Sun Math library in C
 * follows ieee standards
 * Ben Adida(ben.adida@east.sun.com)
 * PERMANENT email address : ben@mit.edu
 * started May 28th, 1996
 * Christine H. Flood
 * started October 1, 1996
 *
 * arc trig functions extracted February 24, 2007 by Ron Goldman
 */


/**
 * The class <code>MathUtils</code> contains some of the Java SE Math routines that are not present in the CLDC 1.1 version of {@link java.lang.Math}: <p>
 * {@link MathUtils#asin}, {@link MathUtils#acos}, {@link MathUtils#atan} & {@link MathUtils#atan2}.
 *<p>
 *
 * The methods in this class are directly substitutable for the corresponding methods in Java SE java.lang.Math (circa JDK 1.2).
 *
 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/lang/Math.html">java.lang.Math in Java SE</a> 
 * @see  java.lang.Math CLDC 1.1's  java.lang.Math
 */
public class MathUtils {
    
    private MathUtils() {
    }
    
    /* Masks */
    
    private static final long sign_mask = 0x8000000000000000L;
    private static final long no_sign_mask = 0x7FFFFFFFFFFFFFFFL;
    private static final long exp_mask = 0x7FF0000000000000L;
    private static final long no_exp_mask = 0x800FFFFFFFFFFFFFL;
    private static final long significand_mask = 0x000FFFFFFFFFFFFFL;
    private static final long implicit_significand_bit = 0x0010000000000000L;
    private static final long one = 0x3ff0000000000000L;
    private static final long half = 0x3fe0000000000000L;
    private static final long low_bits_mask = 0x00000000ffffffffL;
    private static final long high_bits_mask = 0xffffffff00000000L;
    
    /* general variables */
    
    private static final double two54 = (double) (1L << 54);
    private static final double two53 = (double) (1L << 53);
    private static final double twom54 = 1.0 / (double) (1L << 54);
    private static final double two24 = (double) (1L << 24);
    private static final double twon24 = 1.0 / (double) (1L << 24);
    private static final double twon28 = 1.0 / (double) (1L << 28);
    private static final double twon27 = 1.0 / (double) (1L << 27);
    
    /* 0x3FF921FB, 0x54442D18 */
    private static final double pio2_hi =  1.57079632679489655800e+00;
    
    /* 0x3C900000, 0x00000000 */
    private static final double pio2_lo =  6.12323399573676603587e-17;
    
    /* 0x3FE921FB, 0x54442D18 */
    private static final double pio4_hi =  7.85398163397448278999e-01;
    
    /* 0x3C81A626, 0x33145C07 */
    private static final double pio4_lo =  3.06161699786838301793e-17;
    
    private static final double
            arc_pS0 =  1.66666666666666657415e-01, /* 0x3FC55555, 0x55555555 */
            arc_pS1 = -3.25565818622400915405e-01, /* 0xBFD4D612, 0x03EB6F7D */
            arc_pS2 =  2.01212532134862925881e-01, /* 0x3FC9C155, 0x0E884455 */
            arc_pS3 = -4.00555345006794114027e-02, /* 0xBFA48228, 0xB5688F3B */
            arc_pS4 =  7.91534994289814532176e-04, /* 0x3F49EFE0, 0x7501B288 */
            arc_pS5 =  3.47933107596021167570e-05, /* 0x3F023DE1, 0x0DFDF709 */
            arc_qS1 = -2.40339491173441421878e+00, /* 0xC0033A27, 0x1C8A2D4B */
            arc_qS2 =  2.02094576023350569471e+00, /* 0x40002AE5, 0x9C598AC8 */
            arc_qS3 = -6.88283971605453293030e-01, /* 0xBFE6066C, 0x1B8D0159 */
            arc_qS4 =  7.70381505559019352791e-02; /* 0x3FB3B8C5, 0xB12E9282 */
    
    /**
     * Return {@code d} &times;
     * 2<sup>{@code scaleFactor}</sup> rounded as if performed
     * by a single correctly rounded floating-point multiply to a
     * member of the double value set.  See the Java
     * Language Specification for a discussion of floating-point
     * value sets.  If the exponent of the result is between {@link
     * Double#MIN_EXPONENT} and {@link Double#MAX_EXPONENT}, the
     * answer is calculated exactly.  If the exponent of the result
     * would be larger than {@code Double.MAX_EXPONENT}, an
     * infinity is returned.  Note that if the result is subnormal,
     * precision may be lost; that is, when {@code scalb(x, n)}
     * is subnormal, {@code scalb(scalb(x, n), -n)} may not equal
     * <i>x</i>.  When the result is non-NaN, the result has the same
     * sign as {@code d}.
     *
     * <p>Special cases:
     * <ul>
     * <li> If the first argument is NaN, NaN is returned.
     * <li> If the first argument is infinite, then an infinity of the
     * same sign is returned.
     * <li> If the first argument is zero, then a zero of the same
     * sign is returned.
     * </ul>
     *
     * @param x number to be scaled by a power of two.
     * @param n power of 2 used to scale {@code d}
     * @return {@code d} &times; 2<sup>{@code scaleFactor}</sup>
     */
  public static double scalbn(double x, int n) {

    long lx= Double.doubleToLongBits(x);
    long k = (lx & exp_mask) >> 52;

    /* extract exponent */

    if (Double.isNaN(x)) return(Double.NaN);
    if (Double.isInfinite(x)) return(x);

    if (k == 0) {   // 0 or subnormal x
      if (x == 0.0) return x; // x==0.0 or x==-0.0
      x *= two54;
      k= ((Double.doubleToLongBits(x) & exp_mask)>>52) - 54;
      if (n< -50000) return 0.0;
    }

    k = k + (long) n;
    if (k>0x7fe) return(x);     

    if (k> 0) {       /* normal result */
      x = Double.longBitsToDouble((lx & no_exp_mask) | (k<<52));
      return(x);
    }

    if (k <= -54) {
      if (n>50000) {       /* in case integer overflow in n+k */
	return(Double.POSITIVE_INFINITY);
      } else {
	return(0.0);
      }
    }

    k += 54;
    x = (lx & no_exp_mask) | (k<<52);
    return(x*twom54);
  }

    /**
     * Returns the closest <code>int</code> to the argument. The 
     * result is rounded to an integer by adding 1/2, taking the 
     * floor of the result, and casting the result to type <code>int</code>. 
     * In other words, the result is equal to the value of the expression:
     * <p><pre>(int)Math.floor(a + 0.5f)</pre>
     * <p>
     * Special cases:
     * <ul><li>If the argument is NaN, the result is 0.
     * <li>If the argument is negative infinity or any value less than or 
     * equal to the value of <code>Integer.MIN_VALUE</code>, the result is 
     * equal to the value of <code>Integer.MIN_VALUE</code>. 
     * <li>If the argument is positive infinity or any value greater than or 
     * equal to the value of <code>Integer.MAX_VALUE</code>, the result is 
     * equal to the value of <code>Integer.MAX_VALUE</code>.</ul> 
     *
     * @param   a   a floating-point value to be rounded to an integer.
     * @return  the value of the argument rounded to the nearest
     *          <code>int</code> value.
     * @see     java.lang.Integer#MAX_VALUE
     * @see     java.lang.Integer#MIN_VALUE
     */
  public static int round(float a) {
    return (int)Math.floor(a + 0.5f);
  }
  
    /**
     * Returns the closest <code>long</code> to the argument. The result 
     * is rounded to an integer by adding 1/2, taking the floor of the 
     * result, and casting the result to type <code>long</code>. In other 
     * words, the result is equal to the value of the expression:
     * <p><pre>(long)Math.floor(a + 0.5d)</pre>
     * <p>
     * Special cases:
     * <ul><li>If the argument is NaN, the result is 0.
     * <li>If the argument is negative infinity or any value less than or 
     * equal to the value of <code>Long.MIN_VALUE</code>, the result is 
     * equal to the value of <code>Long.MIN_VALUE</code>. 
     * <li>If the argument is positive infinity or any value greater than or 
     * equal to the value of <code>Long.MAX_VALUE</code>, the result is 
     * equal to the value of <code>Long.MAX_VALUE</code>.</ul> 
     *
     * @param   a   a floating-point value to be rounded to a 
     *		<code>long</code>.
     * @return  the value of the argument rounded to the nearest
     *          <code>long</code> value.
     * @see     java.lang.Long#MAX_VALUE
     * @see     java.lang.Long#MIN_VALUE
     */
  public static long round(double a) {
    return (long)Math.floor(a + 0.5d);
  }
    
    /* exp function vars */

  private static final double exp_P1   =  1.66666666666666019037e-01; /* 0x3FC55555, 0x5555553E */
  private static final double exp_P2   = -2.77777777770155933842e-03; /* 0xBF66C16C, 0x16BEBD93 */
  private static final double exp_P3   =  6.61375632143793436117e-05; /* 0x3F11566A, 0xAF25DE2C */
  private static final double exp_P4   = -1.65339022054652515390e-06; /* 0xBEBBBD41, 0xC5D26BF1 */
  private static final double exp_P5   =  4.13813679705723846039e-08; /* 0x3E663769, 0x72BEA4D0 */

  /* exp variables */
  private static final double halF[]   = {0.5,-0.5};
  private static final double o_threshold=  7.09782712893383973096e+02;  /* 0x40862E42, 0xFEFA39EF */
  private static final double u_threshold= -7.45133219101941108420e+02;  /* 0xc0874910, 0xD52D3051 */
  private static final double ln2_hi  =  6.93147180369123816490e-01;	/* 3fe62e42 fee00000 */
  private static final double ln2_lo  =  1.90821492927058770002e-10;	/* 3dea39ef 35793c76 */

  private static final double ln2HI[]   = { 
    6.93147180369123816490e-01,  /* 0x3fe62e42, 0xfee00000 */
   -6.93147180369123816490e-01
  }; /* 0xbfe62e42, 0xfee00000 */

  private static final double ln2LO[]   ={ 
    1.90821492927058770002e-10,  /* 0x3dea39ef, 0x35793c76 */
   -1.90821492927058770002e-10
  };/* 0xbdea39ef, 0x35793c76 */

  private static final double invln2 =  1.44269504088896338700e+00; /* 0x3ff71547, 0x652b82fe */
  private static final double Lg1 = 6.666666666666735130e-01;  /* 3FE55555 55555593 */
  private static final double Lg2 = 3.999999999940941908e-01;  /* 3FD99999 9997FA04 */
  private static final double Lg3 = 2.857142874366239149e-01;  /* 3FD24924 94229359 */
  private static final double Lg4 = 2.222219843214978396e-01;  /* 3FCC71C5 1D8E78AF */
  private static final double Lg5 = 1.818357216161805012e-01;  /* 3FC74664 96CB03DE */
  private static final double Lg6 = 1.531383769920937332e-01;  /* 3FC39A09 D078C69F */
  private static final double Lg7 = 1.479819860511658591e-01;  /* 3FC2F112 DF3E5244 */
  
  private static final double ln2 = 0.69314718055994530942;

   /* 2**-1000=0x01700000,0  */
  private static final double twom1000 = 9.33263618503218878990e-302;

  
    /**
     * Returns Euler's number <i>e</i> raised to the power of a
     * <code>double</code> value.  Special cases:
     * <ul><li>If the argument is NaN, the result is NaN.
     * <li>If the argument is positive infinity, then the result is 
     * positive infinity.
     * <li>If the argument is negative infinity, then the result is 
     * positive zero.</ul>
     * 
     * <p>A result must be within 1 ulp of the correctly rounded
     * result.  Results must be semi-monotonic.
     *
     * @param   a   the exponent to raise <i>e</i> to.
     * @return  the value <i>e</i><sup><code>a</code></sup>, 
     *          where <i>e</i> is the base of the natural logarithms.
     */
  public static double exp(double a) {
    
    double y,hi=0.0,lo=0.0,c,t, absa;
    int k,xsb;
    
    xsb = (int)((Double.doubleToLongBits(a) & sign_mask) >>> 63);
    absa = Math.abs(a);
    
    /* Filter out non finite arguments */
    if (absa >= 709.78) {
      if (Double.isInfinite(a)) {
	if (a > 0) 
	  return(a);
	else return(0.0);
      }
      if (Double.isNaN(a)) {
	return(a);
      }

      if (a > o_threshold) return Double.POSITIVE_INFINITY;      /* overflow */
      if (a < u_threshold) return 0.0;    /* underflow */
    }
    
    /* Argument Reduction */
    /* fdlibm uses 0.5 * ln2 in 32 bits, this is equivalent */
    if (absa > Double.longBitsToDouble(0x3fd62e42FFFFFFFFL)) {
    /* fdlibm uses 1.5 * ln2 in 32 bits, this is equivalent */
      if (absa < Double.longBitsToDouble(0x3FF0A2B200000000L)) {
	hi= a - ln2HI[xsb];
	lo= ln2LO[xsb];
	k=1-xsb-xsb;
      } else {
	k= (int)(invln2*a+halF[xsb]);
	t=k;
	hi= a - t*ln2HI[0];
	lo= t* ln2LO[0];
      }
      a= hi -lo;
    }
    else if (absa < twon28) {
      return(1.0+a);
    }
    else {
      k=0;
    }
    
    /* x is now in primary range */
    t= a*a;
    c= a - t * (exp_P1+ t * (exp_P2 + t * (exp_P3 + t * (exp_P4 + t * exp_P5))));
    if (k==0) {
      return(1.0 - ((a*c)/(c-2.0)-a));
    } else {
      y=1.0 - ((lo - (a*c)/(2.0-c)) - hi);
    }
    if (k>= -1021) {
      y= Double.longBitsToDouble(Double.doubleToLongBits(y) + ((long)k<<52));
      return(y);
    } else {
      y= Double.longBitsToDouble(Double.doubleToLongBits(y) + (((long)k+1000)<<52));
      return(y*twom1000);
    }
  }
  
    /**
     * Returns the natural logarithm (base <i>e</i>) of a <code>double</code>
     * value.  Special cases:
     * <ul><li>If the argument is NaN or less than zero, then the result 
     * is NaN.
     * <li>If the argument is positive infinity, then the result is 
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the 
     * result is negative infinity.</ul>
     * 
     * <p>A result must be within 1 ulp of the correctly rounded
     * result.  Results must be semi-monotonic.
     *
     * @param   a   a value
     * @return  the value ln&nbsp;<code>a</code>, the natural logarithm of
     *          <code>a</code>.
     */
  public static double log(double a) 
  {
    double hfsq,f,s,z,R,w,t1,t2,dk;
    long lo;
    int k,hx,i,j,lx;
    
    k=0;
    lo= Double.doubleToLongBits(a);
    hx= (int)(lo>>>32);

    if (Double.isInfinite(a)) return(Double.POSITIVE_INFINITY);
    if (Double.isNaN(a)) return(a);
    if (hx < 0x00100000) {			/* x < 2**-1022  */
      if (Math.abs(a) == 0) {
	/* log(+-0) = -inf */
	return(Double.NEGATIVE_INFINITY);
      }
      if (a<0) {
	return(Double.NaN); 
      }
    }

    k+= (hx>>>20)-1023;
    hx &= 0x000fffff;
    i= (hx+0x95f64) & 0x100000;
    a= Double.longBitsToDouble((Double.doubleToLongBits(a)&low_bits_mask)+(((long)(hx|(i^0x3ff00000)))<<32));
    k+= (i>>>20);


    f= a-1.0;
    /* if |f| < 2^-20 */
    if((0x000fffff&(2+hx))<3) {	/* |f| < 2**-20 */
      if (f==0.0) {
	if (k==0) {
	  return(0.0);
	} else {
	  dk= (double)k;
	  return(dk*ln2_hi+dk*ln2_lo);
	}
      }
      R= f*f*(0.5-f/3.0);
      if (k==0){
	return(f-R);
      } else {
	dk= (double)k;
	return(dk*ln2_hi- ((R-dk*ln2_lo)-f));
      }
    }
    s= f/(2.0+f);
    dk=(double)k;
    z=s*s;
    i=hx-0x6147a;
    w=z*z;
    j=0x6b851-hx;
    t1= w*(Lg2+w*(Lg4+w*Lg6));
    t2= z*(Lg1+w*(Lg3+w*(Lg5+w*Lg7)));
    i |= j;
    R= t2+t1;
    if (i>0) {
      hfsq=0.5*f*f;
      if (k==0) {
	return(f-(hfsq-s*(hfsq+R)));
      } else {
	return(dk*ln2_hi-((hfsq-(s*(hfsq+R)+dk*ln2_lo))-f));
      }
    } else {
      if (k==0) {
	return(f-s*(f-R));
      } else {
	return(dk*ln2_hi-((s*(f-R)-dk*ln2_lo)-f));
      }
    }
  }
  
  /* constants for pow */
  
  /* poly coefs for (3/2)*(log(x)-2s-2/3*s**3 */
  private static final double
    pow_L1  =  5.99999999999994648725e-01, /* 0x3FE33333, 0x33333303 */
    pow_L2  =  4.28571428578550184252e-01, /* 0x3FDB6DB6, 0xDB6FABFF */
    pow_L3  =  3.33333329818377432918e-01, /* 0x3FD55555, 0x518F264D */
    pow_L4  =  2.72728123808534006489e-01, /* 0x3FD17460, 0xA91D4101 */
    pow_L5  =  2.30660745775561754067e-01, /* 0x3FCD864A, 0x93C9DB65 */
    pow_L6  =  2.06975017800338417784e-01, /* 0x3FCA7E28, 0x4A454EEF */
    pow_P1   =  1.66666666666666019037e-01, /* 0x3FC55555, 0x5555553E */
    pow_P2   = -2.77777777770155933842e-03, /* 0xBF66C16C, 0x16BEBD93 */
    pow_P3   =  6.61375632143793436117e-05, /* 0x3F11566A, 0xAF25DE2C */
    pow_P4   = -1.65339022054652515390e-06, /* 0xBEBBBD41, 0xC5D26BF1 */
    pow_P5   =  4.13813679705723846039e-08, /* 0x3E663769, 0x72BEA4D0 */
    lg2  =  6.93147180559945286227e-01, /* 0x3FE62E42, 0xFEFA39EF */
    lg2_h  =  6.93147182464599609375e-01, /* 0x3FE62E43, 0x00000000 */
    lg2_l  = -1.90465429995776804525e-09, /* 0xBE205C61, 0x0CA86C39 */
    ovt =  8.0085662595372944372e-17, /* -(1024-log2(ovfl+.5ulp)) */
    cp    =  9.61796693925975554329e-01, /* 0x3FEEC709, 0xDC3A03FD =2/(3ln2) */
    cp_h  =  9.61796700954437255859e-01, /* 0x3FEEC709, 0xE0000000 =(float)cp */
    cp_l  = -7.02846165095275826516e-09, /* 0xBE3E2FE0, 0x145B01F5 =tail of cp_h*/
    ivln2    =  1.44269504088896338700e+00, /* 0x3FF71547, 0x652B82FE =1/ln2 */
    ivln2_h  =  1.44269502162933349609e+00, /* 0x3FF71547, 0x60000000 =24b 1/ln2*/
    ivln2_l  =  1.92596299112661746887e-08; /* 0x3E54AE0B, 0xF85DDF44 =1/ln2 tail*/
  


    /**
     * Returns the value of the first argument raised to the power of the
     * second argument. Special cases:
     *
     * <ul><li>If the second argument is positive or negative zero, then the 
     * result is 1.0. 
     * <li>If the second argument is 1.0, then the result is the same as the 
     * first argument.
     * <li>If the second argument is NaN, then the result is NaN. 
     * <li>If the first argument is NaN and the second argument is nonzero, 
     * then the result is NaN. 
     *
     * <li>If
     * <ul>
     * <li>the absolute value of the first argument is greater than 1
     * and the second argument is positive infinity, or
     * <li>the absolute value of the first argument is less than 1 and
     * the second argument is negative infinity,
     * </ul>
     * then the result is positive infinity. 
     *
     * <li>If 
     * <ul>
     * <li>the absolute value of the first argument is greater than 1 and 
     * the second argument is negative infinity, or 
     * <li>the absolute value of the 
     * first argument is less than 1 and the second argument is positive 
     * infinity,
     * </ul>
     * then the result is positive zero. 
     *
     * <li>If the absolute value of the first argument equals 1 and the 
     * second argument is infinite, then the result is NaN. 
     *
     * <li>If 
     * <ul>
     * <li>the first argument is positive zero and the second argument
     * is greater than zero, or
     * <li>the first argument is positive infinity and the second
     * argument is less than zero,
     * </ul>
     * then the result is positive zero. 
     *
     * <li>If 
     * <ul>
     * <li>the first argument is positive zero and the second argument
     * is less than zero, or
     * <li>the first argument is positive infinity and the second
     * argument is greater than zero,
     * </ul>
     * then the result is positive infinity.
     *
     * <li>If 
     * <ul>
     * <li>the first argument is negative zero and the second argument
     * is greater than zero but not a finite odd integer, or
     * <li>the first argument is negative infinity and the second
     * argument is less than zero but not a finite odd integer,
     * </ul>
     * then the result is positive zero. 
     *
     * <li>If 
     * <ul>
     * <li>the first argument is negative zero and the second argument
     * is a positive finite odd integer, or
     * <li>the first argument is negative infinity and the second
     * argument is a negative finite odd integer,
     * </ul>
     * then the result is negative zero. 
     *
     * <li>If
     * <ul>
     * <li>the first argument is negative zero and the second argument
     * is less than zero but not a finite odd integer, or
     * <li>the first argument is negative infinity and the second
     * argument is greater than zero but not a finite odd integer,
     * </ul>
     * then the result is positive infinity. 
     *
     * <li>If 
     * <ul>
     * <li>the first argument is negative zero and the second argument
     * is a negative finite odd integer, or
     * <li>the first argument is negative infinity and the second
     * argument is a positive finite odd integer,
     * </ul>
     * then the result is negative infinity. 
     *
     * <li>If the first argument is finite and less than zero
     * <ul>
     * <li> if the second argument is a finite even integer, the
     * result is equal to the result of raising the absolute value of
     * the first argument to the power of the second argument
     *
     * <li>if the second argument is a finite odd integer, the result
     * is equal to the negative of the result of raising the absolute
     * value of the first argument to the power of the second
     * argument
     *
     * <li>if the second argument is finite and not an integer, then
     * the result is NaN.
     * </ul>
     *
     * <li>If both arguments are integers, then the result is exactly equal 
     * to the mathematical result of raising the first argument to the power 
     * of the second argument if that result can in fact be represented 
     * exactly as a <code>double</code> value.</ul>
     * 
     * <p>(In the foregoing descriptions, a floating-point value is
     * considered to be an integer if and only if it is finite and a
     * fixed point of the method {@link java.lang.Math#ceil <tt>ceil</tt>} or,
     * equivalently, a fixed point of the method {@link java.lang.Math#floor
     * <tt>floor</tt>}. A value is a fixed point of a one-argument
     * method if and only if the result of applying the method to the
     * value is equal to the value.)
     *
     * <p>A result must be within 1 ulp of the correctly rounded
     * result.  Results must be semi-monotonic.
     *
     * @param   x   the base.
     * @param   y   the exponent.
     * @return  the value <code>a<sup>b</sup></code>.
     */
  public static double pow(double x, double y) 
  {
    
    double bp[]= {1.0,1.5};
    double dp_h[] = { 0.0, 5.84962487220764160156e-01}; /* 0x3FE2B803, 0x40000000 */
    double dp_l[] = { 0.0, 1.35003920212974897128e-08}; /* 0x3E4CFDEB, 0x43CFD006 */
    
    double z,ax,z_h,z_l,p_h,p_l;
    double y1,t1,t2,r,s,t,u,v,w;
    int k,yisint,n;
    long i,j,hx,hy,ix,iy;
    
    /* Get those bits */
    hx= Double.doubleToLongBits(x);
    hy= Double.doubleToLongBits(y);
    ix= hx&no_sign_mask;
    iy= hy&no_sign_mask;
    
    /* Now we go through a monster load of special cases */
    
    /* y==0, then return 1 */
    if (iy==0) return 1.0;
    
    /* +-NaN return x+y */
    if ((Double.isNaN(x))||(Double.isNaN(y))) return(x+y);
    
    /* determine if y is an integer, and odd or even */
    yisint=0;
    if (((int)(hx>>32))<0) {
      /* even integer y */
      if (iy>=0x4340000000000000L) yisint=2;
      else if (iy>=one) {
	/* exponent */
	k= ((int)(iy>>52))-0x3ff;
	j= (iy>>(52-k));
	if ((j<<(52-k))==iy) yisint= 2-(((int)j)&0x1);
      }
    }
    
    /* special value of y */
    if (((int)iy)==0) {

      /* y= +- Inf */
      if (Double.isInfinite(y)) {
	if (ix==one) {
	  /* +-1 ^ +-Inf = NaN */
	  return(y-y);
	}
	else if (ix>= one) {
	  /* (|x|>1)** +- Inf= inf, 0 */
	  if (hy>=0) return(y);
	  else return(0.0);
	} else {
	  /* (|x|<1)** +- Inf= inf,0 */
	  if (hy<0) return(-y);
	  else return(0.0);
	}
      }
      
      /* y= +-1 */
      if (Math.abs(y)==1) {
	if (hy<0) return(1.0/x);
	else return(x);
      }
      
      /* y= 2 */
      if (y==2) return(x*x);
      
      /* y=0.5 */
      if (y==0.5) {
	if (hx>=0) return(Math.sqrt(x));
      }
    }
    
    ax= Math.abs(x);

    /* special value of x */
    if (((int)ix)==0) {
      /* x is +-0,+-inf,+-1 */
      if ((ix==0)||(Double.isInfinite(x))||(ax==1)) {
	z= ax;
	if (hy<0) z=(1.0/z);
	if (hx<0) {
	  if ((Math.abs(x)==1)&&(yisint==0)) {
	    z= (z-z)/(z-z);       /* (-1)^non-int is NaN */
	  }
	  else if (yisint==1) z= -z;
	}
	return(z);
      }
    }
    
    
    /* (x<0)^(non-int) is NaN */
    if ((hx<0)&(yisint==0)) return ((x-x)/(x-x));
    
    /* |y| is huge */
    /* if |y| > 2^31 */
    if (iy>0x41e00000ffffffffL) {
      /* if |y| > 2^64, must over/underflow */
      if (iy>0x43f00000ffffffffL) {
	if (ix<=0x3fefffffffffffffL) {
	  if (hy<0) return(Double.POSITIVE_INFINITY);
	  else return(0.0);
	}
	if (ix>=one) {
	  if (hy>0) return(Double.POSITIVE_INFINITY);
	  else return(0.0);
	}
      }
      /* over/underflow if x is not close to one */
      if (ix<0x3fefffff00000000L) {
	if (hy<0) return(Double.POSITIVE_INFINITY);
	else return(0.0);
      }
      if (ix>0x3ff00000ffffffffL) {
	if (hy>0) return(Double.POSITIVE_INFINITY);
	else return(0.0);
      }
      /* now |1-x| is tiny <=2^20, suffice to compute log(x) */
      t= x-1;     /* t has 20 trailing zeros */
      w= (t*t)*(0.5-t*(1.0/3.0-t*0.25));
      u= ivln2_h*t;
      v= t*ivln2_l-w*ivln2;
      t1= u+v;
      t1= Double.longBitsToDouble(Double.doubleToLongBits(t1)&high_bits_mask);
      t2= v-(t1-u);
    } else {
      double s2,s_h,s_l,t_h,t_l;
      n=0;
      
      /* take care of subnormal number */
      if (ix<implicit_significand_bit) {
	ax *= two53;
	n-= 53;
	ix= Double.doubleToLongBits(ax);
      }
      n+= ((int)((ix)>>>52)) - 0x3ff;
      j= (ix & 0xfffffffffffffL);
      
      /* determine interval */
      /* normalize ix */
      ix= (j|one);
      if (((int)(j>>32))<=0x3988e) {
	k=0;       /* |x| < sqrt(3/2) */
      }
      else if(((int)(j>>32))<0xbb67a) {
	k=1;   /* |x| < sqrt(3) */
      }
      else {
	k=0;
	n+=1;
	ix -= 0x10000000000000L;
      }

      ax= Double.longBitsToDouble((Double.doubleToLongBits(ax)&0xffffffffL) + (ix&high_bits_mask));
      
      /* compute stuff */
      u= ax-bp[k];
      v= 1.0/(ax+bp[k]);

      s= u*v;
      s_h=s;
      s_h= Double.longBitsToDouble(Double.doubleToLongBits(s_h)&high_bits_mask);
      
     /* t_h=ax+bp[k] High */
      t_h= Double.longBitsToDouble((((ix>>>1)|0x2000000000000000L) + 0x8000000000000L + (((long)k)<<50))&high_bits_mask);
      
      t_l= ax - (t_h-bp[k]);
      s_l= v*((u-s_h*t_h)-s_h*t_l);
      
      /* compute log(ax) */
      s2= s*s;
      r = s2*s2*(pow_L1+s2*(pow_L2+s2*(pow_L3+s2*(pow_L4+s2*(pow_L5+s2*pow_L6)))));
      r += s_l*(s_h+s);
      s2 = s_h*s_h;
      t_h= 3.0+s2+r;
      t_h= Double.longBitsToDouble(Double.doubleToLongBits(t_h)&high_bits_mask);
      t_l= r-((t_h-3.0)-s2);
      
      /* u+v= s*(1+...) */
      u= s_h*t_h;
      v= s_l*t_h + t_l*s;
      
      /* 2/(3log2)*(s+...) */
      p_h= u+v;
      p_h= Double.longBitsToDouble(Double.doubleToLongBits(p_h)&high_bits_mask);
      p_l= v-(p_h-u);
      z_h= cp_h*p_h;
      z_l= cp_l*p_h + p_l*cp+dp_l[k];
      
      /* log2(ax) = (s+...)*2/(2loge2)= n +dp_h + z_h +z_l */

      t= (double)n;
      t1= (((z_h+z_l)+dp_h[k])+t);
      t1= Double.longBitsToDouble(Double.doubleToLongBits(t1)&high_bits_mask);
      t2= z_l-(((t1-t)-dp_h[k])-z_h);
    }
    
    s=1.0;
    if ((((hx>>63)+1)==0) && (yisint==1)) s= -1.0;
    
    /* split up y into y1+y2 and compute (y1+y2)*(t1+t2) */
    y1=y;
    y1= Double.longBitsToDouble(Double.doubleToLongBits(y1)&high_bits_mask);

    p_l= (y-y1)*t1 + y*t2;
    p_h= y1*t1;

    z= p_l + p_h;
    j= Double.doubleToLongBits(z);
    if (j>=0x4090000000000000L) {
      /* z>= 1024 */
      if ((j-0x4090000000000000L)!=0) {
	/* z> 1024 */
	return(s*Double.POSITIVE_INFINITY);
      } else {
	/* overflow */
	if ((p_l+ovt)>(z-p_h)) return(s*Double.POSITIVE_INFINITY);
      }
    }
    else if ((j&no_sign_mask)>=0x4090cc0000000000L) {
      /* z<= -1075 */
      if ((j-0x4090cc0000000000L)!=0) {
	/* z < -1075 */
	return(s*0.0);     /* underflow */
      } else {
	if (p_l<=(z-p_h)) return(s*0.0);
	/* underflow */
      }
      
    }
    
    /* compute 2^(p_h+p_l) */

    i= j&no_sign_mask;
    k= ((int)(i>>52))-0x3ff;
    n=0;
    /* if |z| > 0.5 set n =[z+0.5] */
    if (i>0x3fe00000ffffffffL) {
      n= (int)((j>>32)+(0x100000L>>(k+1)));
      k= ((n&0x7fffffff)>>>20) -0x3ff;
      t= 0.0;
      t= Double.longBitsToDouble((Double.doubleToLongBits(t)&low_bits_mask)+(((long)(n&~(0x000fffff>>k)))<<32));
      n= ((n&0x000fffff)|0x00100000)>>(20-k);
      if (j<0) n=-n;
      p_h -= t;
    }
    t= p_l+p_h;
    t= Double.longBitsToDouble(Double.doubleToLongBits(t)&high_bits_mask);
    u= t*lg2_h;
    v= (p_l-(t-p_h))*lg2+t*lg2_l;

    z=u+v;
    w= v-(z-u);
    t=z*z;
    t1  = z - t*(pow_P1+t*(pow_P2+t*(pow_P3+t*(pow_P4+t*pow_P5))));
    r= (z*t1)/(t1-2.0)-(w+z*w);
    z= 1.0 - (r-z);
    
    j= Double.doubleToLongBits(z)&high_bits_mask;
    j += ((long)n)<<52;
    if ((j>>52)<=0) {
      z=scalbn(z,n);
    }
    else z= Double.longBitsToDouble(Double.doubleToLongBits(z) + (((long)n)<<52));
    return(s*z);
  }
  
  /**
     * Returns the arc sine of an angle, in the range of -<i>pi</i>/2 through <i>pi</i>/2. 
     * Special cases:
     * <ul>
     *  <li>If the argument is NaN or its absolute value is greater than 1, then the result is NaN.
     *  <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     *
     * @param a the value whose arc sine is to be returned.
     * @return the arc sine of the argument.
     */
    public static double asin(double a) {
        double t, w, p, q, c, r, s;
        long hx = Double.doubleToLongBits(a);
        long ix = hx & no_sign_mask;
        
        if (Math.abs(a) >= 1) {
            if (Math.abs(a) == 1)  return(a * pio2_hi + a * pio2_lo);
            if (Math.abs(a) > 1) return(Double.NaN);
        } else if (ix < half) {     /* |x| < 0.5 */
            /* |x| < 2^-27 */
            if (ix < 0x3e40000000000000L) {
                return(a);
            } else {
                t = a * a;
                p = t * (arc_pS0 + t * (arc_pS1 + t * (arc_pS2 + t * (arc_pS3 + t * (arc_pS4 + t * arc_pS5)))));
                q = 1.0 + t * (arc_qS1 + t * (arc_qS2 + t * (arc_qS3 + t * arc_qS4)));
                w = p / q;
                return(a + a * w);
            }
        }
        
        /* 1 > |x| > 0.5 */
        w = 1.0 - Math.abs(a);
        t = w * 0.5;
        p = t * (arc_pS0 + t * (arc_pS1 + t * (arc_pS2 + t * (arc_pS3 + t * (arc_pS4 + t * arc_pS5)))));
        q = 1.0 + t * (arc_qS1 + t * (arc_qS2 + t * (arc_qS3 + t * arc_qS4)));
        s = Math.sqrt(t);

        /* if |x| >0.975 */
        if (ix >= 0x3fef333300000000L) {
            w = p / q;
            t = pio2_hi - (2.0 * (s + s * w) - pio2_lo);
        } else {
            w = s;
            w = Double.longBitsToDouble(Double.doubleToLongBits(w) & high_bits_mask);
            c = (t - w * w) / (s + w);
            r = p / q;
            p = 2.0 * s * r - (pio2_lo - 2.0 * c);
            q = pio4_hi - 2.0 * w;
            t = pio4_hi - (p - q);
        }
        
        if (hx > 0) return(t);
        else return(-t);
    }
    
    /**
     * Returns the arc cosine of an angle, in the range of 0 through <i>pi</i>. 
     * Special cases:
     * <ul>
     *  <li>If the argument is NaN or its absolute value is greater than 1, then the result is NaN.
     * </ul>
     *
     * @param a the value whose arc cosine is to be returned.
     * @return the arc cosine of the argument.
     */
    public static double acos(double a) {
        double z, p, q, r, w, s, c, df;
        long hx = Double.doubleToLongBits(a);
        long ix = hx & no_sign_mask;
        
        /* |x| >= 1 */
        if (Math.abs(a)  >= 1) {
            /* |x| == 1*/
            if (ix == one) {
                if (hx > 0) return(0.0);
                else return(Math.PI + 2.0 * pio2_lo);
            }
            /* acos(|x| > 1) is NaN */
            return((a - a) / (a - a));
        }
        
        /* |x| < 0.5 */
        if (ix < half) {
            /* if |x| < 2^-57 */
            if (ix <= 0x3c600000ffffffffL) return(pio2_hi + pio2_lo);
            z = a * a;
            p = z * (arc_pS0 + z * (arc_pS1 + z * (arc_pS2 + z * (arc_pS3 + z * (arc_pS4 + z * arc_pS5)))));
            q = 1.0 + z * (arc_qS1 + z * (arc_qS2 + z * (arc_qS3 + z * arc_qS4)));
            r = p / q;
            return(pio2_hi - (a - (pio2_lo - a * r)));
        }
        /* x < -0.5 */
        else if (hx < 0) {
            z = (1.0 + a) * 0.5;
            p = z * (arc_pS0 + z * (arc_pS1 + z * (arc_pS2 + z * (arc_pS3 + z * (arc_pS4 + z * arc_pS5)))));
            q = 1.0 + z * (arc_qS1 + z * (arc_qS2 + z * (arc_qS3 + z * arc_qS4)));
            s = Math.sqrt(z);
            r = p / q;
            w = r * s - pio2_lo;
            return(Math.PI - 2.0 * (s + w));
        }
        /* x > 0.5 */
        else {
            z = (1.0 - a) * 0.5;
            s = Math.sqrt(z);
            df = s;
            df = Double.longBitsToDouble(Double.doubleToLongBits(df) & high_bits_mask);
            c = (z - df * df) / (s + df);
            p = z * (arc_pS0 + z * (arc_pS1 + z * (arc_pS2 + z * (arc_pS3 + z * (arc_pS4 + z * arc_pS5)))));
            q = 1.0 + z * (arc_qS1 + z * (arc_qS2 + z * (arc_qS3 + z * arc_qS4)));
            r = p / q;
            w = r * s + c;
            return(2.0 * (df + w));
        }
    }
    
    
    /* arctan */
    
    private static final double atanhi[]= {
        4.63647609000806093515e-01, /* atan(0.5)hi 0x3FDDAC67, 0x0561BB4F */
        7.85398163397448278999e-01, /* atan(1.0)hi 0x3FE921FB, 0x54442D18 */
        9.82793723247329054082e-01, /* atan(1.5)hi 0x3FEF730B, 0xD281F69B */
        1.57079632679489655800e+00  /* atan(inf)hi 0x3FF921FB, 0x54442D18 */
    };
    
    private static final double atanlo[]= {
        2.26987774529616870924e-17, /* atan(0.5)lo 0x3C7A2B7F, 0x222F65E2 */
        3.06161699786838301793e-17, /* atan(1.0)lo 0x3C81A626, 0x33145C07 */
        1.39033110312309984516e-17, /* atan(1.5)lo 0x3C700788, 0x7AF0CBBD */
        6.12323399573676603587e-17  /* atan(inf)lo 0x3C91A626, 0x33145C07 */
    };
    
    private static final double aT[]= {
         3.33333333333329318027e-01, /* 0x3FD55555, 0x5555550D */
        -1.99999999998764832476e-01, /* 0xBFC99999, 0x9998EBC4 */
         1.42857142725034663711e-01, /* 0x3FC24924, 0x920083FF */
        -1.11111104054623557880e-01, /* 0xBFBC71C6, 0xFE231671 */
         9.09088713343650656196e-02, /* 0x3FB745CD, 0xC54C206E */
        -7.69187620504482999495e-02, /* 0xBFB3B0F2, 0xAF749A6D */
         6.66107313738753120669e-02, /* 0x3FB10D66, 0xA0D03D51 */
        -5.83357013379057348645e-02, /* 0xBFADDE2D, 0x52DEFD9A */
         4.97687799461593236017e-02, /* 0x3FA97B4B, 0x24760DEB */
        -3.65315727442169155270e-02, /* 0xBFA2B444, 0x2C6A6C2F */
         1.62858201153657823623e-02, /* 0x3F90AD3A, 0xE322DA11 */
    };
    
    /**
     * Returns the arc tangent of an angle, in the range of -<i>pi</i>/2 through <i>pi</i>/2. 
     * Special cases:
     * <ul>
     *  <li>If the argument is NaN, then the result is NaN.
     *  <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     *
     * @param a the value whose arc tangent is to be returned.
     * @return the arc tangent of the argument.
     */
    public static double atan(double a) {
        double w,s1,s2,z;
        int id;
        long hx = Double.doubleToLongBits(a);
        long ix = hx & no_sign_mask;
        
        /* |a| >= 2^66 */
        if (ix >= 0x4410000000000000L) {
            /* NaN */
            if (ix > exp_mask) {
                return(a + a);
            }
            if (hx > 0) return(atanhi[3] + atanlo[3]);
            else return(-atanhi[3] - atanlo[3]);
        }
        
        /* |a| < 0.4375 */
        if (ix < 0x3fdc000000000000L) {
            /* |a| < 2^-29 */
            if (ix < 0x3e20000000000000L) {
                return(a);
            }
            id = -1;
        } else {
            a = Math.abs(a);
            /* |a| < 1.1875 */
            if (ix < 0x3ff3000000000000L) {
                /* 7/16 <= |a| < 11/16 */
                if (ix < 0x3fe6000000000000L) {
                    id = 0;
                    a = (2.0 * a - 1.0) / (2.0 + a);
                }
                /* 11/16 <= |a| < 19/16 */
                else {
                    id = 1;
                    a = (a - 1.0) / (a + 1.0);
                }
            } else {
                /* |x| < 2.4375 */
                if (ix < 0x4003800000000000L) {
                    id = 2;
                    a = (a - 1.5) / (1.0 + 1.5 * a);
                } else {
                    id = 3;
                    a = -1.0 / a;
                }
            }
        }
        
        /* end of argument reduction */
        z = a * a;
        w = z * z;
        
        /* break sum from i=0 to 10 into odd and even poly */
        s1 = z * (aT[0] + w * (aT[2] + w * (aT[4] + w * (aT[6] + w * (aT[8] + w * aT[10])))));
        s2 = w * (aT[1] + w * (aT[3] + w * (aT[5] + w * (aT[7] + w * aT[9]))));
        if (id < 0) return (a - a * (s1 + s2));
        else {
            z = atanhi[id] - ((a * (s1 + s2) - atanlo[id]) - a);
            if (hx < 0) return(-z);
            else return(z);
        }
    }
    
    
    /**
     * Converts rectangular coordinates (<i>x</i>, <i>y</i>) to polar (<i>r</i>, <i>theta</i>).
     * This method computes the phase <i>theta</i> by computing an arc tangent
     * of <i>y</i>/<i>x</i> in the range of -<i>pi</i> to <i>pi</i>. Special cases:
     * <ul>
     *  <li>If either argument is NaN, then the result is NaN.
     *  <li>If the first argument is positive zero and the second argument is positive, 
     *      or the first argument is positive and finite and the second argument is positive infinity,
     *      then the result is positive zero.
     *  <li>If the first argument is negative zero and the second argument is positive,
     *      or the first argument is negative and finite and the second argument is positive infinity,
     *      then the result is negative zero.
     *  <li>If the first argument is positive zero and the second argument is negative,
     *      or the first argument is positive and finite and the second argument is negative infinity,
     *      then the result is the double value closest to <i>pi</i>.
     *  <li>If the first argument is negative zero and the second argument is negative,
     *      or the first argument is negative and finite and the second argument is negative infinity,
     *      then the result is the double value closest to -<i>pi</i>.
     *  <li>If the first argument is positive and the second argument is positive zero or negative zero,
     *      or the first argument is positive infinity and the second argument is finite,
     *      then the result is the double value closest to <i>pi</i>/2.
     *  <li>If the first argument is negative and the second argument is positive zero or negative zero,
     *      or the first argument is negative infinity and the second argument is finite,
     *      then the result is the double value closest to -<i>pi</i>/2.
     *  <li>If both arguments are positive infinity, then the result is the double value closest to <i>pi</i>/4.
     *  <li>If the first argument is positive infinity and the second argument is negative infinity,
     *      then the result is the double value closest to 3*<i>pi</i>/4.
     *  <li>If the first argument is negative infinity and the second argument is positive infinity,
     *      then the result is the double value closest to -<i>pi</i>/4.
     *  <li>If both arguments are negative infinity, then the result is the double value closest to -3*<i>pi</i>/4.
     * </ul>
     *
     * @param y the ordinate coordinate
     * @param x the abscissa coordinate
     *
     * @return the <i>theta</i> component of the point (<i>r</i>, <i>theta</i>) in polar coordinates
     *         that corresponds to the point (<i>x</i>, <i>y</i>) in Cartesian coordinates.
     */
    public static double atan2(double y, double x) {
        double pi_o_4  = 7.8539816339744827900E-01; /* 0x3FE921FB, 0x54442D18 */
        double pi_o_2  = 1.5707963267948965580E+00; /* 0x3FF921FB, 0x54442D18 */
        double pi      = 3.1415926535897931160E+00; /* 0x400921FB, 0x54442D18 */
        double pi_lo   = 1.2246467991473531772E-16; /* 0x3CA1A626, 0x33145C07 */
        double z;
        int k,m;
        long hx = Double.doubleToLongBits(x);
        long hy = Double.doubleToLongBits(y);
        long ix = (hx & no_sign_mask);
        long iy = (hy & no_sign_mask);
        
        
        /* x or y is NaN */
        if ((Double.isNaN(x)) || (Double.isNaN(y))) {
            return(x + y);
        }
        
        /* x = 1.0 */
        if (hx == one) return(atan(y));
        
        /* 2 * sign(x) + sign(y) */
        m = (int)(((hy >> 63) & 1) | ((hx >> 62) & 2));
        
        /* when y = 0 */
        if (iy == 0) {
            switch(m) {
                case 0:
                case 1: return(y);             /* atan(+-0, +anything) = +- 0 */
                case 2: return(pi);            /* atan( +0, -anything) =  pi */
                case 3: return(-pi);           /* atan( -0, -anything) = -pi */
            }
        }
        
        /* when x = 0 */
        if (ix == 0) {
            if (hy < 0) return(-pi_o_2);
            else return(pi_o_2);
        }
        
        /* when x is INF */
        if (Double.isInfinite(x)) {
            if (Double.isInfinite(y)) {
                switch(m) {
                    case 0: return(pi_o_4);           /* atan(+inf, +inf) */
                    case 1: return(-pi_o_4);          /* atan(-inf, +inf) */
                    case 2: return(3.0 * pi_o_4);     /* atan(+inf, -inf) */
                    case 3: return(-3.0 * pi_o_4);    /* atan(-inf, -inf) */
                }
            } else {
                switch(m) {
                    case 0: return(0.0);
                    case 1: return(-0.0);
                    case 2: return(pi);
                    case 3: return(-pi);
                }
            }
        }
        
        /* when y is inf */
        if (Double.isInfinite(y)) {
            if (hy < 0) return(-pi_o_2);
            else return(pi_o_2);
        }
        
        /* compute y / x */
        k = (int)((iy - ix) >> 52);
        if (k > 60) z = pi_o_2 + 0.5 * pi_lo;       /* |y/x| > 2^60 */
        else if ((hx < 0) && (k < -60)) z = 0.0;    /* |y/x| < -2^60 */
        else z = atan(Math.abs(y / x));             /* safe to do y/x */
        
        switch(m) {
            case 0: return(z);                      /* atan(+,+) */
            case 1: return(-z);                     /* atan(-,+) */
            case 2: return(pi - (z - pi_lo));       /* atan(+,-) */
            default: /*case 3*/
                return((z - pi_lo) - pi);           /* atan(-,-) */
        }
    }

    /**
     * Returns the <code>double</code> value that is closest in value
     * to the argument and is equal to a mathematical integer. If two
     * <code>double</code> values that are mathematical integers are
     * equally close, the result is the integer value that is
     * even. Special cases:
     * <ul><li>If the argument value is already equal to a mathematical 
     * integer, then the result is the same as the argument. 
     * <li>If the argument is NaN or an infinity or positive zero or negative 
     * zero, then the result is the same as the argument.</ul>
     *
     * @param   a   a <code>double</code> value.
     * @return  the closest floating-point value to <code>a</code> that is
     *          equal to a mathematical integer.
     */
    public static double rint(double a) {
        double fl, ce, up, down;
        fl = Math.floor(a);
        ce = Math.ceil(a);
        if ((fl == a) || (ce == a)) {
            return (a);
        }

        up = ce - a;
        down = a - fl;

        if (up > down) {
            return (fl);
        } else if (up == down) {
            if ((Math.ceil(ce / 2) * 2) == ce) {
                return (ce);
            } else {
                return (fl);
            }
        } else {
            return (ce);
        }
    }

    /* log1p vars */
    private static final double Lp1 = 6.666666666666735130e-01, /* 3FE55555 55555593 */  Lp2 = 3.999999999940941908e-01, /* 3FD99999 9997FA04 */  Lp3 = 2.857142874366239149e-01, /* 3FD24924 94229359 */  Lp4 = 2.222219843214978396e-01, /* 3FCC71C5 1D8E78AF */  Lp5 = 1.818357216161805012e-01, /* 3FC74664 96CB03DE */  Lp6 = 1.531383769920937332e-01, /* 3FC39A09 D078C69F */  Lp7 = 1.479819860511658591e-01;  /* 3FC2F112 DF3E5244 */


    /**
     * Returns the natural logarithm of the sum of the argument and 1.
     * Note that for small values <code>x</code>, the result of
     * <code>log1p(x)</code> is much closer to the true result of ln(1
     * + <code>x</code>) than the floating-point evaulation of
     * <code>log(1.0+x)</code>.
     *
     * <p>Special cases:
     *
     * <ul>
     *
     * <li>If the argument is NaN or less than -1, then the result is
     * NaN.
     *
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     *
     * <li>If the argument is negative one, then the result is
     * negative infinity.
     *
     * <li>If the argument is zero, then the result is a zero with the
     * same sign as the argument.
     *
     * </ul>
     *
     * <p>A result must be within 1 ulp of the correctly rounded
     * result.  Results must be semi-monotonic.
     *
     * @param   a   a value
     * @return the value ln(<code>x</code>&nbsp;+&nbsp;1), the natural
     * log of <code>x</code>&nbsp;+&nbsp;1
     */
    public static double log1p(double a) {
        double hfsq, f = 0.0, c = 0.0, s, z, R, u;
        int k;
        long hx, hu = 0, ax;

        hx = Double.doubleToLongBits(a);
        ax = hx & no_sign_mask;

        k = 1;
        /* a < 0.41422 */
        if (hx < 0x3fda827a00000000L) {
            /* a<= -1.0 */
            if (ax >= one) {
                if (a == -1.0) {
                    return (Double.NEGATIVE_INFINITY);
                } else {
                    return (Double.NaN);
                }
            }
            /* |a| < 2^-29 */
            if (ax < 0x3e20000000000000L) {
                /* |a| < 2^-54 */
                if (ax < 0x3c90000000000000L) {
                    return (a);
                } else {
                    return (a - a * a * 0.5);
                }
            }
            if ((hx > 0) || (hx <= 0xbfd2bec3ffffffffL)) {
                k = 0;
                f = a;
                hu = 1;
            }
        }

        if (hx >= exp_mask) {
            return (a + a);
        }
        if (k != 0) {
            if (hx < 0x4340000000000000L) {
                u = 1.0 + a;
                hu = Double.doubleToLongBits(u);
                k = ((int) (hu >>> 52)) - 1023;
                /* correction term */
                if (k > 0) {
                    c = (1.0 - (u - a));
                } else {
                    c = (a - (u - 1.0));
                }
                c /= u;
            } else {
                u = a;
                hu = Double.doubleToLongBits(u);
                k = ((int) (hu >>> 52)) - 1023;
                c = 0;
            }

            hu &= significand_mask;
            if (hu < 0x6a09e00000000L) {
                /* normalize u */
                u = Double.longBitsToDouble(hu | one);
            } else {
                /* normalize u/2 */
                k += 1;
                u = Double.longBitsToDouble(hu | half);
                hu = (implicit_significand_bit - hu) >>> 2;
            }
            f = u - 1.0;
        }
        hfsq = 0.5 * f * f;

        /* |f| < 2^-20 */
        if (hu == 0) {
            if (f == 0.0) {
                if (k == 0) {
                    return (0.0);
                } else {
                    c += k * ln2_lo;
                    return (k * ln2_hi + c);
                }
            }
            R = hfsq * (1.0 - 0.66666666666666666 * f);
            if (k == 0) {
                return (f - R);
            } else {
                return (k * ln2_hi - ((R - (k * ln2_lo + c) - f)));
            }
        }
        s = f / (2.0 + f);
        z = s * s;
        R = z * (Lp1 + z * (Lp2 + z * (Lp3 + z * (Lp4 + z * (Lp5 + z * (Lp6 + z * Lp7))))));
        if (k == 0) {
            return (f - (hfsq - s * (hfsq + R)));
        } else {
            return (k * ln2_hi - ((hfsq - (s * (hfsq + R) + (k * ln2_lo + c))) - f));
        }
    }
    private static final double expm1_Q1 = -3.33333333333331316428e-02, /* BFA11111 111110F4 */  expm1_Q2 = 1.58730158725481460165e-03, /* 3F5A01A0 19FE5585 */  expm1_Q3 = -7.93650757867487942473e-05, /* BF14CE19 9EAADBB7 */  expm1_Q4 = 4.00821782732936239552e-06, /* 3ED0CFCA 86E65239 */  expm1_Q5 = -2.01099218183624371326e-07; /* BE8AFDB7 6E09C32D */


    /**
     * Returns <i>e</i><sup>x</sup>&nbsp;-1.  Note that for values of
     * <i>x</i> near 0, the exact sum of
     * <code>expm1(x)</code>&nbsp;+&nbsp;1 is much closer to the true
     * result of <i>e</i><sup>x</sup> than <code>exp(x)</code>.
     *
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, the result is NaN.
     *
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     *
     * <li>If the argument is negative infinity, then the result is
     * -1.0.
     *
     * <li>If the argument is zero, then the result is a zero with the
     * same sign as the argument.
     *
     * </ul>
     *
     * <p>A result must be within 1 ulp of the correctly rounded
     * result.  Results must be semi-monotonic.  The result of
     * <code>expm1</code> for any finite input must be greater than or
     * equal to <code>-1.0</code>.  Note that once the exact result of
     * <i>e</i><sup><code>x</code></sup>&nbsp;-&nbsp;1 is within 1/2
     * ulp of the limit value -1, <code>-1.0</code> should be
     * returned.
     *
     * @param   a   the exponent to raise <i>e</i> to in the computation of
     *              <i>e</i><sup><code>x</code></sup>&nbsp;-1.
     * @return  the value <i>e</i><sup><code>x</code></sup>&nbsp;-&nbsp;1.
     */
    public static double expm1(double a) {
        double y, hi, lo, c = 0.0, t, e, hxs, hfx, r1;
        int k, xsb;
        long hx;

        hx = Double.doubleToLongBits(a);
        xsb = ((int) (hx >>> 63));
        y = Math.abs(a);
        hx &= no_sign_mask;

        /* huge and non-finite arguments */

        /* |x|>=56*ln2 */
        if (hx >= 0x4043687a00000000L) {
            /* |x|>=709.78 */
            if (hx >= 0x40862e4200000000L) {
                if (hx >= exp_mask) {
                    if ((hx & 0xfffffffffffffL) == 0) {
                        return (a + a);      /*NaN*/
                    } else {
                        if (xsb == 0) {
                            return (a);
                        } else {
                            return (-1.0);
                        }
                    }
                }
                /* overflow */
                if (a > o_threshold) {
                    return (Double.POSITIVE_INFINITY);
                }
            }
            if (xsb != 0) {
                return (-1.0);
            }
        }

        /* argument reduction */
        /* |a| > 0.5 ln2 */
        if (hx > 0x3fd62e42ffffffffL) {
            /* |a| < 1.5 ln2 */
            if (hx < 0x3ff0a2b200000000L) {
                if (xsb == 0) {
                    hi = a - ln2_hi;
                    lo = ln2_lo;
                    k = 1;
                } else {
                    hi = a + ln2_hi;
                    lo = -ln2_lo;
                    k = -1;
                }
            } else {
                k = (int) (invln2 * a + halF[xsb]);
                t = (double) k;
                hi = a - t * ln2_hi;
                lo = t * ln2_lo;
            }
            a = hi - lo;
            c = (hi - a) - lo;
        } else if (hx < 0x3c90000000000000L) {
            /* when |x|<2^-54 */
            return (a);
        } else {
            k = 0;
        }

        /* x is now in primary range */
        hfx = 0.5 * a;
        hxs = a * hfx;
        r1 = 1.0 + hxs * (expm1_Q1 + hxs * (expm1_Q2 + hxs * (expm1_Q3 + hxs * (expm1_Q4 + hxs * expm1_Q5))));
        t = 3.0 - (r1 * hfx);
        e = hxs * ((r1 - t) / (6.0 - a * t));
        if (k == 0) {
            return (a - (a * e - hxs));
        } else {
            e = (a * (e - c) - c);
            e -= hxs;
            if (k == -1) {
                return (0.5 * (a - e) - 0.5);
            }
            if (k == 1) {
                if (a < -0.25) {
                    return (-2.0 * (e - (a + 0.5)));
                } else {
                    return (1.0 + 2.0 * (a - e));
                }
            }
            if ((k <= -2) || (k > 56)) {
                y = 1.0 - (e - a);
                y = Double.longBitsToDouble(Double.doubleToLongBits(y) + (((long) k) << 52));
                return (y - 1.0);
            }
            t = 1.0;
            if (k < 20) {
                t = Double.longBitsToDouble((Double.doubleToLongBits(t) & low_bits_mask) + one - (0x20000000000000L >>> k));
                y = t - (e - a);
                y = Double.longBitsToDouble(Double.doubleToLongBits(y) + (((long) k) << 52));
            } else {
                t = Double.longBitsToDouble((Double.doubleToLongBits(t) & low_bits_mask) + (((long) (0x3ff - k)) << 52));
                y = a - (e + t);
                y += 1.0;
                y = Double.longBitsToDouble(Double.doubleToLongBits(y) + (((long) k) << 52));
            }
        }
        return (y);
    }

    /* 
     * __ieee754_fmod(x,y)
     * Return x mod y in exact arithmetic
     * Method: shift and subtract
     */
//  static double __ieee754_fmod(double x, double y) {
//    long lx = Double.doubleToLongBits(x);
//    long sx = lx & sign_mask;
//    long ly = Double.doubleToLongBits(y) & no_sign_mask;
//    double absx = Math.abs(x);
//    double absy = Math.abs(y);
//
//    lx = lx & no_sign_mask;
//
//    long temp;
//    long ix, iy, n;
//
//    /* purge off exception values */
//    if ((y == 0) || Double.isInfinite(x) || Double.isNaN(y)) 
//      return Double.NaN;
//    
//    if (absx <= absy) {
//      if (absx < absy) return(x);
//      if (absx == absy) 
//	if (sx == sign_mask) return -0.0; else return 0.0;
//    }
//
//    /* determine ix = ilogb(x) */
//
//    if (lx < implicit_significand_bit) { // subnormal x
//      ix = -1043;
//      temp = lx;
//      while (temp < sign_mask) { // Until a 1 shifts into the sign bit
//	temp = temp<<1;
//	ix =ix -1;
//      }
//    } else ix = (lx>>52) - 1023;
//
//    if (ly < implicit_significand_bit) { // subnormal y
//      iy = -1043;
//      temp = ly;
//      while (temp < sign_mask) { // Until a 1 shifts into the sign bit
//	temp = temp<<1;
//	iy =iy -1;
//      }
//    } else iy = (ly>>52) - 1023;
//
//    if(ix >= -1022) 
//      lx = implicit_significand_bit | (significand_mask & lx);
//    else {		/* subnormal x, shift x to normal */
//      n = -1022 - ix;
//      lx = lx << n;
//    }
//
//    if(iy >= -1022) 
//      ly = implicit_significand_bit | (significand_mask & ly);
//    else {		/* subnormal y, shift y to normal */
//      n = -1022- iy;
//      ly = ly << n;
//    }
//
//    /* fix point fmod */
//    n = (ix - iy);
//
//    for (int i = 0; i < n; i++) {
//      if (lx < ly) lx = lx << 1;
//      else if (lx == ly) 
//	if (sx == sign_mask) return -0.0; else return 0.0;
//      else lx = (lx - ly)<< 1;
//    }
//    
//    if (lx >= ly) lx = lx - ly;
//
//    /* convert back to floating value and restore the sign */
//
//    if((lx)==0) 			/* return sign(x)*0 */
//      if ((long)sx>>>31==0) return(0.0); else return(-0.0);
//
//    while(lx<implicit_significand_bit) {		/* normalize x */
//      lx = lx<<1;
//      iy -= 1;
//    }
//
//    if(iy>= -1022) {	/* normalize output */
//      lx = ((lx - implicit_significand_bit) | ((iy + 1023) << 52));
//      lx = lx | sx;
//      x = Double.longBitsToDouble(lx);
//
//    } else {		/* subnormal output */
//      n = -1022 - (int)iy;
//      lx = lx >>> n;
//      lx = lx | sx;
//      x = Double.longBitsToDouble(lx);
//    }
//    return(x);
//  }
    
    /**
     * Computes the remainder operation on two arguments as prescribed 
     * by the IEEE 754 standard.
     * The remainder value is mathematically equal to 
     * <code>f1&nbsp;-&nbsp;f2</code>&nbsp;&times;&nbsp;<i>n</i>,
     * where <i>n</i> is the mathematical integer closest to the exact 
     * mathematical value of the quotient <code>f1/f2</code>, and if two 
     * mathematical integers are equally close to <code>f1/f2</code>, 
     * then <i>n</i> is the integer that is even. If the remainder is 
     * zero, its sign is the same as the sign of the first argument. 
     * Special cases:
     * <ul><li>If either argument is NaN, or the first argument is infinite, 
     * or the second argument is positive zero or negative zero, then the 
     * result is NaN.
     * <li>If the first argument is finite and the second argument is 
     * infinite, then the result is the same as the first argument.</ul>
     *
     * @param   x   the dividend.
     * @param   p   the divisor.
     * @return  the remainder when <code>f1</code> is divided by
     *          <code>f2</code>.
     */
    public static double IEEEremainder(double x, double p) {
        long lx, lp, sx;
        double p_half;

        lx = Double.doubleToLongBits(x) & no_sign_mask;
        lp = Double.doubleToLongBits(p) & no_sign_mask;
        sx = Double.doubleToLongBits(x) & sign_mask;

        /* purge off exception values */
        if (p == 0) {
            return (Double.NaN);
        }
        if (Double.isInfinite(x)) {
            return (Double.NaN);
        }
        if (Double.isNaN(x)) {
            return (Double.NaN);
        }
        if (Double.isNaN(p)) {
            return (Double.NaN);
        }

        if (lp < 0x7fdfffff00000000L) {
            x = x % (p + p);
        } //__ieee754_fmod(x,p+p);
        if ((lx - lp) == 0) {
            return 0.0 * x;
        }
        x = Math.abs(x);
        p = Math.abs(p);
        if (lp < (2 * implicit_significand_bit)) {
            if ((x + x) > p) {
                x -= p;
                if (x + x >= p) {
                    x -= p;
                }
            }
        } else {
            p_half = 0.5 * p;
            if (x > p_half) {
                x -= p;
                if (x >= p_half) {
                    x -= p;
                }
            }
        }
        if (sx == 0) {
            return x;
        } else {
            return -x;
        }
    }


}


