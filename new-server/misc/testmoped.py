
def ack(client, app):
    while True:
        x = client.service.get_ack_status("20UYA31581L000000", app)
        if x == True:
            break

def readfile(name):
    f = open(name)
    s = f.read()
    f.close()
    return s
