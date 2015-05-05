#! /bin/bash

set -e

P=$1
V=$2

#ROOT=~/moped/newmoped/moped
ROOT=~/moped/moped
ROOT1=~/moped/moped

PLUGINS=$ROOT/plugins
PLUGINSTORAGE=$ROOT1/plugins/storage
SQUAWK=$ROOT/squawk

cd $PLUGINS/$P
mvn clean install

cd $PLUGINS/$P
rm -rf classes classes.jar j2meclasses weaved preprocessed
cd $SQUAWK
./d.sh user-compile $PLUGINS/$P

cd $PLUGINS/$P
rm -rf tmp
mkdir tmp
cd tmp
jar xf ../target/$P-$V.jar
cp ../j2meclasses/plugins/* plugins
jar cvfm $P-$V.jar META-INF/MANIFEST.MF $P.xml plugins/
cd ..
cp tmp/$P-$V.jar $PLUGINSTORAGE
