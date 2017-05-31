import socket

def open_socket2():
    ECMHOST = 'localhost'
    ECMPORT = 9002

    for res in socket.getaddrinfo(ECMHOST, ECMPORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
        af, socktype, proto, canonname, sa = res
        #print("res %s" % (res,))
        try:
            s = socket.socket(af, socktype, proto)
        except Exception as e:
            print("socket %s" % e)
            s = None
            continue

        try:
            s.connect(sa)
        except Exception as e:
            print("connect %s" % e)
            #(socket.error, msg):
            s.close()
            s = None
            continue
        break
    if s is None:
        print('could not open socket2')
        return False

    return s

def open_socket3():
    H112HOST = "appz-ext.sics.se"
    H112PORT = 6892

    for res in socket.getaddrinfo(H112HOST, H112PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
        af, socktype, proto, canonname, sa = res
        #print("res %s" % (res,))
        try:
            s = socket.socket(af, socktype, proto)
        except Exception as e:
            print("socket %s" % e)
            s = None
            continue

        try:
            s.connect(sa)
        except Exception as e:
            print("connect %s" % e)
            #(socket.error, msg):
            s.close()
            s = None
            continue
        break
    if s is None:
        print('could not open socket2')
        return False

    return s

