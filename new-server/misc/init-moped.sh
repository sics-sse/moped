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

import testmoped

x = client.service.insertPluginInDb("/lhome/sse/moped_plugins/PluginCreationTest2/1.7/", "PluginCreationTest2")
print x

x = client.service.parseVehicleConfiguration("/home/arndt/moped/system1.xml")
print x

x = client.service.install("20UYA31581L000000", 1, "jdk")
print x

testmoped.ack(client)

EOF

for t in Vehicle VehiclePlugin Ecu Port Link PluginPortConfig PluginLinkConfig AppConfig; do
    echo "*** $t *******"
    echo "use fresta2; select * from $t;" | mysql -uroot -proot
done
