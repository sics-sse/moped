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

n="PluginCreationTest3"
v="1.0"
print upload(s,n,v)

x=s.compileApp(n, v)
print x

x=base64.b64encode(readfile("/home/arndt/moped/moped/simulator/configs/system1.xml"))
x = client.service.addVehicleConfig(x)
print x

x = client.service.addVehicle("minMOPED", "20UYA31581L000000", "MOPED")
print x

x=client.service.addUserVehicleAssociation(33, "20UYA31581L000000", True)
print x

x = client.service.installApp("20UYA31581L000000", 200)
print x

ack(client, 200)

n="PluginCreationTest2"
v="1.8"
print upload(s,n,v)

x=s.compileApp(n, v)
print x

x = client.service.installApp("20UYA31581L000000", 201)
print x

ack(client, 201)


#x = client.service.uninstallApp("20UYA31581L000000", 200)
#print x

#x = client.service.uninstallApp("20UYA31581L000000", 201)
#print x

EOF

for t in Vehicle VehiclePlugin VehicleConfig Ecu Port Link PluginPortConfig PluginLinkConfig AppConfig; do
    echo "*** $t *******"
    echo "use fresta2; select * from $t;" | mysql -uroot -proot
done
