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
@rem

@echo off

@rem ----------------------------------------------------------
@rem              Setup environment
@rem ----------------------------------------------------------

@rem ----------------------------------------------------------
@rem              Go ahead and build build.jar
@rem ----------------------------------------------------------
if exist classes rmdir /S /Q classes
mkdir classes
"%JAVA_HOME%\bin\javac" -target 1.5 -source 1.5 -d classes -g src\com\sun\squawk\builder\launcher\*.java
"%JAVA_HOME%\bin\jar" cfm ..\build.jar build-manifest.mf -C classes .
rmdir /S /Q classes
mkdir classes
cd classes
"%JAVA_HOME%\bin\jar" xf ..\..\tools\retroweaver-all-squawk.jar
cd ..
"%JAVA_HOME%\bin\javac" -target 1.5 -source 1.5 -d classes -g -cp classes;..\vm2c\lib\openjdk-javac-6-b12.jar;%JAVA_HOME%\lib\tools.jar src\com\sun\cldc\jna\*.java   src\com\sun\cldc\jna\ptr\*.java src\com\sun\squawk\builder\*.java src\com\sun\squawk\builder\bytecodespec\*.java src\com\sun\squawk\builder\ccompiler\*.java src\com\sun\squawk\builder\commands\*.java src\com\sun\squawk\builder\gen\*.java src\com\sun\squawk\builder\launcher\*.java src\com\sun\squawk\builder\platform\*.java src\com\sun\squawk\builder\util\*.java
"%JAVA_HOME%\bin\jar" cfm ..\build-commands.jar build-commands-manifest.mf -C classes .
rmdir /S /Q classes
