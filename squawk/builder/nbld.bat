@rem 	
@rem
@rem Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
@rem DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
@rem 
@rem This program is free software; you can redistribute it and/or
@rem modify it under the terms of the GNU General Public License version
@rem 2 only, as published by the Free Software Foundation.
@rem 
@rem This program is distributed in the hope that it will be useful, but
@rem WITHOUT ANY WARRANTY; without even the implied warranty of
@rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
@rem General Public License version 2 for more details (a copy is
@rem included at /legal/license.txt).
@rem 
@rem You should have received a copy of the GNU General Public License
@rem version 2 along with this work; if not, write to the Free Software
@rem Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
@rem 02110-1301 USA
@rem 
@rem Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
@rem Clara, CA 95054 or visit www.sun.com if you need additional
@rem information or have any questions.

@rem This script should be run as follows:
@rem 1, bld cldc
@rem 2, run this script
@rem 3, bld cldc a second time

java -cp ../build-commands.jar:../cldc/classes com.sun.squawk.builder.gen.NativeGen 0 > tmp && chmod -w tmp && mv -vf tmp ../cldc/src/com/sun/squawk/vm/Native.java
#java -cp ../build-commands.jar:../cldc/classes com.sun.squawk.builder.gen.NativeGen 1 > tmp && chmod -w tmp && mv -vf tmp ../vmgen/src/com/sun/squawk/vm/InterpreterNative.java
java -cp ../build-commands.jar:../cldc/classes com.sun.squawk.builder.gen.NativeGen 2 > tmp && chmod -w tmp && mv -vf tmp ../translator/src/com/sun/squawk/translator/ir/verifier/NativeVerifierHelper.java