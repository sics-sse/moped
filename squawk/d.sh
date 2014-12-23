#!/bin/bash
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

if [ ! -f d ]; then
    if [ -z "`uname | grep 'Windows'`" ]; then
        ln -s d.sh d
        ln -s d_g.sh d_g
        chmod +x `find . -name '*.sh'`
    fi
fi

if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=`which java`
  JAVA_HOME=`dirname $JAVA_HOME`
  JAVA_HOME=`dirname $JAVA_HOME`
fi

#TEMP
JAVA_HOME='/usr/local/lib/java/jdk1.6.0_45'

echo "JAVA_HOME=$JAVA_HOME"
#-client -XX:+PrintCompilation
#builder="${JAVA_HOME}/bin/java $EXTRA_BUILDER_VMFLAGS -XX:CompileCommand=exclude,com/sun/squawk/Method.getParameterTypes -XX:CompileCommand=exclude,com/sun/squawk/SymbolParser.getSignatureTypeAt -XX:CompileCommand=exclude,com/sun/squawk/SymbolParser.stripMethods -Xms128M -Xmx384M  -jar build.jar $BUILDER_FLAGS"
builder="${JAVA_HOME}/bin/java $EXTRA_BUILDER_VMFLAGS -Xms128M -Xmx384M  -jar build.jar $BUILDER_FLAGS"
echo $builder

#----------------------------------------------------------#
#              Rebuild the builder                         #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xbuilder" ]; then 
    cd builder;
    ./bld.sh $JAVA_HOME
    cd ..
    exit
fi

#----------------------------------------------------------#
#              Rebuild the CSystem.dll                     #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xcsystem" ]; then 
    cl "/I${JAVA_HOME}\include" "/I${JAVA_HOME}\include\win32" /c \
        prototypecompiler/src/com/sun/squawk/compiler/jni/CSystem.c \
        prototypecompiler/src/com/sun/squawk/compiler/jni/dispatch_x86.c
    link /nologo /debug /dll /out:CSystem.dll CSystem.obj dispatch_x86.obj
    exit
fi

#----------------------------------------------------------#
#              Launch Squawk in SDA                        #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xsda" ]; then 
    shift
    eval squawk -verbose com.sun.squawk.debugger.sda.SDA $*
    exit
fi

#----------------------------------------------------------#
#           Run CLDC TCK 1.0a Static Signature Test        #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xsigtest10a" ]; then 
    if [ "X$TCK10a_DIR" = "X" ]; then
        echo "Need to set TCK10a_DIR variable to base dir of CLDC TCK 1.0a"
        exit 1
    fi
    cmd="java -cp $TCK10a_DIR/javatest.jar:$TCK10a_DIR/sigtest.jar javasoft.sqe.tests.api.signaturetest.cldc.CLDCSignatureTest -TestURL file:$TCK10a_DIR/tests/api/signaturetest/ -Classpath cldc/j2meclasses"
    echo $cmd
    eval $cmd
    exit
fi

#----------------------------------------------------------#
#           Run CLDC TCK 1.1 Static Signature Test         #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xsigtest11" ]; then 
    if [ "X$TCK11_DIR" = "X" ]; then
        TCK11_DIR=../Squawk-tck/CLDC-TCK_11
        if [ ! -d $TCK11_DIR ]; then
            echo "Need to set TCK11_DIR variable to base dir of CLDC TCK 1.1"
            exit 1
        fi
    fi
    cmd="java -jar $TCK11_DIR/lib/sigtest.jar -TCK_ROOT file:$TCK11_DIR -Classpath cldc/j2meclasses"
    echo $cmd
    eval $cmd
    exit
fi

#----------------------------------------------------------#
#           Start the JavaTest  UI                         #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xjavatest11" ]; then 
    if [ "X$TCK11_DIR" = "X" ]; then
        TCK11_DIR=../Squawk-tck/CLDC-TCK_11
        if [ ! -d $TCK11_DIR ]; then
            echo "Need to set TCK11_DIR variable to base dir of CLDC TCK 1.1"
            exit 1
        fi
    fi
    cmd="java -jar $TCK11_DIR/lib/javatest.jar"
    echo $cmd
    eval $cmd
    exit
fi

#----------------------------------------------------------#
#           Run IMP TCK 1.0 Static Signature Teste         #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Ximpsigtest10" ]; then 
    TCK_DIR=../Squawk-tck/IMP-TCK_10
    if [ ! -d $DIR ]; then
        echo "Cannot find $TCK_DIR"
        exit 1
    fi
    cmd="java -jar $TCK_DIR/lib/sigtest.jar -TCK_ROOT file:$TCK_DIR -Classpath cldc/j2meclasses -Package javax"
    echo $cmd
    eval $cmd
    exit
fi

#----------------------------------------------------------#
#              Rebuild the native methods                  #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xnmethods" ]; then 
    eval $builder cldc
    cd builder;
    ./nbld.sh
    cd ..
    eval $builder clean
    eval $builder cldc
    exit
fi


#----------------------------------------------------------#
#             Macros for Andrew's demo                     #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xserver" ]; then 
    cmd="squawk -verbose -cp:samples/j2meclasses example.shell.LookupServer -verbose -loadbalance:ManyBalls"
    echo $cmd
    $cmd
    exit
fi
	
if [ $# -gt 0 -a "X$1" = "Xshell" ]; then 
    localhost=""
    if [ $# -gt 2 ]; then
        localhost=",$3"
    fi
    cmd="squawk -verbose -cp:samples/j2meclasses example.shell.Main -verbose -register:$2$localhost"
    echo $cmd
    $cmd
    exit
fi
	
#----------------------------------------------------------#
#              Start builder with spot plugins             #
#----------------------------------------------------------#

if [ $# -gt 0 -a "X$1" = "Xspotplugin" ]; then
	echo "=== Building javatest-spot-builder..."
    ant -f javatest-spot-builder/build.xml build-plugin
	echo "=== Building javatest-mbed-device..."
	cd javatest-mbed-device
	ant clean compile
	cd ..
   # $builder -verbose -plugins:javatest-spot-builder/javatest-spot-builder.properties -verbose javatest-mbed-device
    shift
	echo "=== Running JamSpot javatest-mbed-device..."
    exec $builder -plugins:javatest-spot-builder/javatest-spot-builder.properties $*
fi

if [ $# -gt 0 -a "X$1" = "Xspot" ]; then
    shift
    exec $builder -plugins:javatest-spot-builder/javatest-spot-builder.properties $*
fi

#----------------------------------------------------------#
#              Fall through to build.jar                   #
#----------------------------------------------------------#

echo $builder $*
exec $builder $*
