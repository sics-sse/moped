#! /bin/bash

# We should be inside moped/new-server/misc.
# You need the python module suds somewhere; modify PYTHONPATH below
# accordingly.
# There should be a newly started server and simulator running.

# For expected answers, see init-moped.txt

SQL="mysql -uroot -proot"

if true; then
    $SQL <<EOF
    drop database fresta2;
EOF

    $SQL <<EOF
    create database fresta2 DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
    grant all privileges on fresta2 . * to 'fresta'@'%';
    flush privileges;
EOF

    $SQL < ../fresta2-schema

    (echo "use fresta2";cat ~/moped/wp_users.dump) | mysql -uroot -proot

fi

PYTHONPATH=~/moped python <<EOF
from suds.client import Client
url='http://localhost:9990/moped/pws?wsdl'
client = Client(url)
client.options.cache.clear()

s = client.service

from testmoped import *
import base64

x=base64.b64encode(readfile("/home/arndt/moped/moped/plugins/PluginCreationTest3/target/PluginCreationTest3-1.0.jar"))
s.uploadApp(x, "PluginCreationTest3", "1.0")

x=base64.b64encode(readfile("/home/arndt/moped/moped/simulator/configs/system1.xml"))
x = client.service.addVehicleConfig(x)
print x

x = client.service.addVehicle("minMOPED", "20UYA31581L000000", "MOPED")
print x

x=client.service.addUserVehicleAssociation(33, "20UYA31581L000000", True)
print x

x = client.service.installApp("20UYA31581L000000", 200, "jdk")
print x

ack(client, 200)

x=base64.b64encode(readfile("/home/arndt/moped/moped/plugins/PluginCreationTest2/target/PluginCreationTest2-1.8.jar"))
s.uploadApp(x, "PluginCreationTest2", "1.8")

#x = client.service.installApp("20UYA31581L000000", 201, "jdk")
#print x

#ack(client, 201)


#x = client.service.uninstallApp("20UYA31581L000000", 200)
#print x

#x = client.service.uninstallApp("20UYA31581L000000", 201)
#print x

EOF

for t in Vehicle VehiclePlugin VehicleConfig Ecu Port Link PluginPortConfig PluginLinkConfig AppConfig; do
    echo "*** $t *******"
    echo "use fresta2; select * from $t;" | mysql -uroot -proot
done
