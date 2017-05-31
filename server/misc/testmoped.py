import base64
import json
import re

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
    x=base64.b64encode(readfile("../../plugins/%s/target/%s-%s.jar" % (n,n,v)))
    x=s.uploadApp(x, n, v)
    return x

def uploadplus(s, n, v):
    x = upload(s, n, v)
    print x
    res=s.compileApp(n, v)
    m = re.search("Romizer processed ([0-9]+) class", res, re.MULTILINE)
    if m:
        if m.group(1) == "0":
            print "Romizer processed 0 classes!"
            print res
    else:
        print "Couldn't find how many classes were processed"
    y = json.loads(x)
    return y['result']

