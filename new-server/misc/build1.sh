#/bin/bash

set -e

# Ensure that JAVA_HOME is set correctly,
# and LD_LIBRARY_PATH according to the output from "d.sh jvmenv".
# Also, ensure that PATH is set so the cross compiler is found
# (Sourcery_CodeBench_Lite_for_ARM_EABI or something similar).

ROOT=~/moped/moped

HERE=$ROOT/new-server/misc
PLUGINS=$ROOT/plugins
SQUAWK=$ROOT/squawk-zeni
AUTOSAR=$ROOT/autosar

cd $SQUAWK
./startScript.sh

cd $AUTOSAR
./build.sh clean
./build.sh

cd $HERE
#./build2.sh AutoBrake 1.0
#./build2.sh UltraSonicReader 1.0
./build2.sh Comm1 1.0
./build2.sh Comm2 1.0
