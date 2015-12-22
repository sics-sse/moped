#! /bin/bash

NAME=$1
ID=$2

# we should check that both args are there
# we should check that the directory doesn't exist
# we should check that we are in the plugin directory

cp -rp template $NAME
cd $NAME

find . -name \*.xml | xargs sed -i "s/PLUGINNAME/$NAME/g"
find . -name \*.java | xargs sed -i "s/PLUGINNAME/$NAME/g"

find . -name \*.xml | xargs sed -i "s/ECUID/$ID/g"

cd src/main
mv java/plugins/PLUGINNAME.java java/plugins/$NAME.java
mv resources/PLUGINNAME.xml resources/$NAME.xml
