#! /bin/bash

set -e

P=$1
V=$2

ROOT=~/moped/moped

HERE=$ROOT/new-server/misc
PLUGINS=$ROOT/plugins
SQUAWK=$ROOT/squawk-zeni
SERVERPLUGINS=$ROOT/webportal/moped_plugins

cd $PLUGINS/$P
mvn clean install
cp target/$P-$V.jar ../storage

#Needed if the manifest or xml files were changed, or classes removed
#cd $HERE
#rm -rf $SERVERPLUGINS/$P/$V
#./init-moped4.sh

cd $PLUGINS/$P
rm -rf classes classes.jar j2meclasses weaved preprocessed
cd $SQUAWK
./d.sh user-compile $PLUGINS/$P
cp $PLUGINS/$P/j2meclasses/plugins/* $SERVERPLUGINS/$P/$V/$P/j2meclasses/plugins/

cd $SQUAWK
rm $SERVERPLUGINS/$P/$V/$P/$P.suite
./d.sh user-suite $SERVERPLUGINS/$P/$V/$P
