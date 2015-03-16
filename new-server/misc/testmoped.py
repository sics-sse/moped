import json

def ack(client, app):
    vin = "20UYA31581L000000"
    while True:
        y = client.service.listInstalledApps()
        y = json.loads(y)
        for t in y['result']:
            if t['vin'] == vin:
                if int(t['appid']) == app:
                    return

#        x = client.service.get_ack_status(vin, app)
#        print x
#        if x == True:
#            break

def readfile(name):
    f = open(name)
    s = f.read()
    f.close()
    return s
