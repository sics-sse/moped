/**
 * RTE Types Header File
 *
 * @req SWS_Rte_01161
 * @req SWS_Rte_01160
 */

#ifndef RTE_TYPE_H_
#define RTE_TYPE_H_

/** --- Includes ----------------------------------------------------------------------------------
 * @req SWS_Rte_01163
 */
#include <Rte.h>

/**
 * @req SWS_Rte_02648
 * @req SWS_Rte_06709
 * @req SWS_Rte_06710
 * @req SWS_Rte_06712
 * 
 * @req SWS_Rte_07104
 * @req SWS_Rte_07114
 * @req SWS_Rte_07116 
 * @req SWS_Rte_07110
 * @req SWS_Rte_07111
 * !req SWS_Rte_06706
 * !req SWS_Rte_06707
 * !req SWS_Rte_06708
 * !req SWS_Rte_07112
 * !req SWS_Rte_07113
 */

/* Primitive type UInt16 */
typedef uint16 UInt16;

/* Primitive type UInt32 */
typedef uint32 UInt32;

/* Primitive type UInt8 */
typedef uint8 UInt8;

/* Array type Array2 */
typedef UInt8 Array2[2];

/* Array type Array8 */
typedef UInt8 Array8[8];

// No support for pointer types (for ConstVoidPtr) yet

// No support for pointer types (for ConstVoidPtr) yet

// No support for pointer types (for VoidPtr) yet

// No support for pointer types (for VoidPtr) yet

#endif /* RTE_TYPE_H_ */
