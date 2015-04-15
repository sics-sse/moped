import base64
import json

def ack(client, app):
    vin = "20UYA31581L000000"
    while True:
        y = client.service.listInstalledApps()
        y = json.loads(y)
        for t in y['result']:
            if t['vin'] == vin:
                if int(t['appId']) == app:
                    print t['installationState']
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

def upload(s, n, v):
#    x=base64.b64encode(readfile("/home/arndt/moped/moped/plugins/%s/target/%s-%s.jar" % (n,n,v)))
    x=base64.b64encode(readfile("/home/arndt/moped/moped/plugins/storage/%s-%s.jar" % (n,v)))
    x=s.uploadApp(x, n, v)
    return x

def uploadplus(s, n, v):
    x = upload(s, n, v)
    x=s.compileApp(n, v)
