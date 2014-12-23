#!/bin/bash
if [ 2 != $# ]; then
  echo "usage: compileApp.sh package(a/b/c) classfile"
  exit 1
fi

# Assign parameters
package=$1
classfile=$2

# Keep current directory
currDir=`pwd`

# Split package into array
IFS='/' read -a dirArray <<< "$package"
numOfDir=${#dirArray[@]}

# Remove folder from classfile
pureClassName=`echo $classfile | awk -F"/" '{print $NF}'`

# Extract classname 
classname=`echo $pureClassName | cut -d'.' -f 1`

# Create project folder reusing classfile name 
mkdir $classname
cd $classname

# Create folder j2meclasses
mkdir j2meclasses
cd j2meclasses

# Create folders for package
for (( i=0; i<$numOfDir; i++ ));
do
  mkdir ${dirArray[$i]}
  cd ${dirArray[$i]}
done

# Change back original folder
cd "$currDir"

# Move classfile into specific folder
mv $classfile $classname/j2meclasses/$package

# Generate zip file
zip -r $classname.zip $classname
 
# Delete intermediate temperary files
rm -r $classname
 
