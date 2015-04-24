/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk;

import javax.microedition.rms.RecordStoreException;

import org.jmock.InAnyOrder;
import org.jmock.InThisOrder;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.sun.squawk.flash.INorFlashSectorState;

public class JMock2Test extends MockObjectTestCase {
    
    InThisOrder expectationBuilder = new InThisOrder();
    
    static {
        System.out.println("static Hello");
    }

    static {
        System.out.println("static Hello again");
    }

    {
        System.out.println("Hello");
    }
    
    {
        System.out.println("Hello again");
    }
    
    public void testExpecsGroups() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        
        expectationBuilder.one(sector).erase(0L);
        expectationBuilder.one(sector).erase(0L);
        expects(expectationBuilder);
        
        sector.erase(0L);
        sector.erase(0L);
    }

    public void testExpecsGroups2() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        
        expects(new InAnyOrder() {{
            one(sector).erase(0L);
        }});

        expects(new InAnyOrder() {{
            one(sector).erase(0L);
        }});
        
        sector.erase(0L);
        sector.erase(0L);
    }

}
