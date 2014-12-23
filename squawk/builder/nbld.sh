#!/bin/sh
#
# Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version
# 2 only, as published by the Free Software Foundation.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License version 2 for more details (a copy is
# included at /legal/license.txt).
# 
# You should have received a copy of the GNU General Public License
# version 2 along with this work; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, CA 95054 or visit www.sun.com if you need additional
# information or have any questions.
#
# This script should be run as follows:
# 1, bld cldc
# 2, run this script
# 3, bld cldc a second time

java -cp ../cldc/classes:../build-commands.jar com.sun.squawk.builder.gen.NativeGen 0 > tmp && chmod -w tmp && mv -vf tmp ../cldc/src/com/sun/squawk/vm/Native.java
#java -cp ../cldc/classes:../build-commands.jar com.sun.squawk.builder.gen.NativeGen 1 > tmp && chmod -w tmp && mv -vf tmp ../vmgen/src/com/sun/squawk/vm/InterpreterNative.java
java -cp ../cldc/classes:../build-commands.jar com.sun.squawk.builder.gen.NativeGen 2 > tmp && chmod -w tmp && mv -vf tmp ../translator/src/com/sun/squawk/translator/ir/verifier/NativeVerifierHelper.java
