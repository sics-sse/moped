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
#----------------------------------------------------------#
#              Setup environment                           #
#----------------------------------------------------------#

if [ $# -gt 0 ]; then
    JAVA_HOME=$1
elif [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=`which java`
  JAVA_HOME=`dirname $JAVA_HOME`
  JAVA_HOME=`dirname $JAVA_HOME`
fi

#echo "JAVA_HOME=$JAVA_HOME"
JAVAC=$JAVA_HOME/bin/javac
JAR=$JAVA_HOME/bin/jar
#echo JAVAC=$JAVAC
#echo JAR=$JAR

#----------------------------------------------------------#
#              Go ahead and build build.jar                #
#----------------------------------------------------------#
rm -rf classes
mkdir classes
$JAVAC -target 1.5 -source 1.5 -d classes -g src/com/sun/squawk/builder/launcher/*.java
$JAR cfm ../build.jar build-manifest.mf -C classes .
rm -fr classes
mkdir classes
cd classes
$JAR xf ../../tools/retroweaver-all-squawk.jar
cd ..
$JAVAC -cp classes:../vm2c/lib/openjdk-javac-6-b12.jar:$JAVA_HOME/lib/tools.jar -target 1.5 -source 1.5 -d classes -g `find src -name '*.java'`
$JAR cfm ../build-commands.jar build-commands-manifest.mf -C classes .
rm -fr classes
