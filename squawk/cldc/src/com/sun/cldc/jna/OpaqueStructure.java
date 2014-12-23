/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.							  */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in $(WIND_BASE)/WPILib.  */
/*----------------------------------------------------------------------------*/

package com.sun.cldc.jna;

/**
 * This is a "Structure" that we'll never look at the fields of.
 * @author dw29446
 */
public class OpaqueStructure extends Structure {

    public int size() {
        return 0;
    }

    public void read() {
    }

    public void write() {
    }
}
