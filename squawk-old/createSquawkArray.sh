#/bin/bash

# Get the size of the byte array
len=`ls -l squawk.suite | awk '{printf("%s", $5)}'`

# Create the byte array
echo "unsigned char squawkSuiteArray["$((len+1))"] = {" > tmp.c
./printForC squawk.suite >> tmp.c

# Close the byte array
sed 's/-1,/(unsigned char) -1 };/g' tmp.c > vmcore/src/rts/gcc-rpi/squawkSuiteArray.c 

#Remove the temporary file
rm tmp.c

echo "squawkSuiteArray.c is generated!"
