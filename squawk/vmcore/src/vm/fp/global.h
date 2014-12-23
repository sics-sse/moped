/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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

/* 
 * This file is a replacement for the KVM file global.h, defining the 
 * symbols needed for the floating point functions defined in this directory.
 */

#ifndef true
#warning "must include platform .h before global.h"
#endif

#include <jni.h>
#include <math.h>

#include "os_math.h" /* add platform specific method definitions */

/* Macros for NaN (Not-A-Number) and Infinity for floats and doubles */
#define F_POS_INFINITY    0x7F800000L
#define F_NEG_INFINITY    0xFF800000L
#define F_L_POS_NAN       0x7F800001L
#define F_H_POS_NAN       0x7FFFFFFFL
#define F_L_NEG_NAN       0xFF800001L
#define F_H_NEG_NAN       0xFFFFFFFFL

#define D_POS_INFINITY    0x7FF0000000000000LL
#define D_NEG_INFINITY    0xFFF0000000000000LL
#define D_L_POS_NAN       0x7FF0000000000001LL
#define D_H_POS_NAN       0x7FFFFFFFFFFFFFFFLL
#define D_L_NEG_NAN       0xFFF0000000000001LL
#define D_H_NEG_NAN       0xFFFFFFFFFFFFFFFFLL

union  uu1   { int i; float f; };
union  uu2   { jlong l; unsigned int lParts[2]; double d;        };

// This version is intended to allow compiler optimizations to be turned on 
// for x86 machines. It is vetter than the fdlibm code on gcc-x86, but
// still fails on MVC (with optimizations on).
#if (PLATFORM_BIG_ENDIAN || ARM_FPA)
#define __HI(x) (((union uu2*)&x)->lParts[0])
#define __LO(x) (((union uu2*)&x)->lParts[1])
#define __HIp(x) (((union uu2*)x)->lParts[0])
#define __LOp(x) (((union uu2*)x)->lParts[1])
#else
#define __HI(x) (((union uu2*)&x)->lParts[1])
#define __LO(x) (((union uu2*)&x)->lParts[0])
#define __HIp(x) (((union uu2*)x)->lParts[1])
#define __LOp(x) (((union uu2*)x)->lParts[0])
#endif


INLINE float ib2f(int i)                { union  uu1 x; x.i = i; return x.f;         }
INLINE int   f2ib(float f)              { union  uu1 x; x.f = f; return x.i;         }


#if ARM_FPA
INLINE double lb2d(jlong l)                { union uu2 x; x.lParts[0] = (unsigned int)((ujlong)l >> 32); x.lParts[1] = l; return x.d;         }

INLINE jlong  d2lb(const double d) {
	union uu2 x;
    unsigned int y; 
	x.d = d; y = x.lParts[0]; x.lParts[0]= x.lParts[1]; x.lParts[1] = y; 
    return x.l;
}
#else /* ARM_FPA */
INLINE double lb2d(jlong l)                { union uu2 x; x.l = l; return x.d;         }
INLINE jlong  d2lb(double d)               { union uu2 x; x.d = d; return x.l;         }
#endif /* ARM_FPA */

extern int    __ieee754_rem_pio2(double x, double *y);
extern double __ieee754_sqrt(double x);
extern double __kernel_sin(double x, double y, int iy);
extern double __kernel_cos(double x, double y);
extern double __kernel_tan(double x, double y, int iy);
extern int __kernel_rem_pio2(double *x, double *y, int e0, int nx, int prec, const int *ipio2);

extern double JFP_lib_sin(double x);
extern double JFP_lib_cos(double x);
extern double JFP_lib_tan(double x);
extern double JFP_lib_floor(double x);
extern double JFP_lib_ceil(double x);
extern double JFP_lib_copysign(double x, double y);
extern double JFP_lib_scalbn (double x, int n);
extern double JFP_lib_sqrt(double x);
extern double JFP_lib_fabs(double x);

#if PROCESSOR_ARCHITECTURE_X86 && !defined(__SSE2_MATH__ )
// x86 requires a much more complicated solution. See fp_bytecodes.c.
extern double JFP_lib_muld(double x, double y);
extern double JFP_lib_divd(double x, double y);
extern double JFP_lib_remd(double x, double y);
extern float  JFP_lib_remf(float x, float y);

#else
INLINE double  JFP_lib_muld(double x, double y)       { return x * y;      }
INLINE double  JFP_lib_divd(double x, double y)       { return x / y;      }
INLINE double  JFP_lib_remd(double x, double y)       { return fmod(x, y);}
INLINE float   JFP_lib_remf(float x, float y)         { return fmodf(x, y);}

#endif

