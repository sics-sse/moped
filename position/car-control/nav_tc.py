import socket
import time
import ast
import queue

from nav_log import tolog, tolog0, tolog2
from nav_util import start_new_thread

import nav_signal
import nav2

prevprint1s = None

def print1(g, s):
    global prevprint1s
    if prevprint1s != s:
        print(("%f: " % (time.time()-g.t0)) + s)
    prevprint1s = s

def connect_to_ground_control():
    while True:
        if not g.ground_control:
            g.s = open_socket()
            if not g.s:
                #print("no connection")
                pass
            else:
                print("connection opened")
                g.ground_control = g.s
                start_new_thread(from_ground_control, ())
                now = time.time() - g.t0
                send_to_ground_control("info %s %f" % (g.VIN, now))
        time.sleep(5)

# almost the same as in tcontrol_comm.py
def linesplit(socket):
    buffer = socket.recv(4096)
    buffer = buffer.decode("ascii")
    buffering = True
    while buffering:
        if "\n" in buffer:
            (line, buffer) = buffer.split("\n", 1)
            yield line
        else:
            more = socket.recv(4096)
            more = more.decode("ascii")
            if not more:
                buffering = False
            else:
                buffer += more
    if buffer:
        yield buffer
    return

def from_ground_control():
    lastreportclosest = False

    prevl = None

    while True:
        if g.ground_control:
            for data in linesplit(g.ground_control):
                #print(data)
                l = data.split(" ")
                #print(l)
                #print(data)

                g.tctime = time.time()

                if l[0] == "go":
                    x = float(l[1])
                    y = float(l[2])
                    print(("goto is not implemented", x, y))
                elif l[0] == "path":
                    path = ast.literal_eval(data[5:])
                    print("Received path from traffic control:")
                    print(path)
                    # currently, we don't do anything with this path
                elif l[0] == "continue":
                    g.paused = False
                elif l[0] == "carsinfront":
                    if l != prevl:
                        pass
                        #print("traffic %s" % str(l[1:]))
                    prevl = l
                    n = int(l[1])
                    closest = None
                    for i in range(0, n):
                        relation = l[6*i+2]
                        dist = float(l[6*i+3])
                        #x = float(l[6*i+4])
                        #y = float(l[6*i+5])
                        othercar = l[6*i+6]
                        onlyif = float(l[6*i+7])
                        if closest == None or closest > dist:
                            closest = dist
                    if closest != None:
                        if (onlyif != 0
                            and g.nextdecisionpoint != 0
                            and onlyif != g.nextdecisionpoint):
                            tolog("onlyif %d %d" % (
                                    onlyif, g.nextdecisionpoint))
                            continue
                        # a car length
                        closest = closest - 0.5
                        closest0 = closest
                        # some more safety:
                        closest = closest - 0.5
                        if closest < 0:
                            closest = 0
                        # 4 is our safety margin and should make for
                        # a smoother ride
                        if g.limitspeed == None:
                            pass
                            #print("car in front")
                        tolog("car in front")
                        #g.limitspeed = 100*closest/0.85/4
                        g.limitspeed = 100*closest/0.85
                        if g.limitspeed < 11:
                            #print("setting limitspeed to 0")
                            g.limitspeed = 0
                            if g.outspeedcm != None and g.outspeedcm != 0:
                                nav_signal.warningblink(True)
                        else:
                            #print("reduced limitspeed")
                            pass

                        if g.limitspeed < g.outspeedcm:
                            print1(g, "%s %s %.2f m speed %.1f -> %.1f (%.1f)" % (
                                    relation,
                                    othercar, closest0,
                                    g.finspeed,
                                    g.limitspeed,
                                    g.outspeedcm))
                        if "crash" in relation and g.simulate:
                            g.crash = True
                            g.simulmaxacc = 0.0

                        ff = tolog
                        if g.finspeed != 0 and g.finspeed >= g.limitspeed:
                            if relation == "givewayto":
                                ff = tolog2
                                ff = tolog
                        ff("closest car in front2: %s dist %f limitspeed %f" % (
                                relation, closest, g.limitspeed))
                        lastreportclosest = True
                    else:
                        if not g.crash:
                            g.limitspeed = None
                            if lastreportclosest:
                                tolog0("no close cars")
                                pass
                            lastreportclosest = False
                    if g.outspeedcm:
                        # neither 0 nor None
                        if g.limitspeed == 0:
                            send_to_ground_control("message stopping for obstacle")
                        elif g.limitspeed != None and g.limitspeed < g.outspeedcm:
                            send_to_ground_control("message slowing for obstacle %.1f" % g.limitspeed)
                        else:
                            send_to_ground_control("message ")
                    else:
                        send_to_ground_control("message ")
                elif l[0] == "parameter":
                    g.parameter = int(l[1])
                    print("parameter %d" % g.parameter)
                # can be used so we don't have to stop if the next
                # section is free
                elif l[0] == "waitallcarsdone":
                    g.queue.put(1)
                elif l[0] == "cargoto":
                    x = float(l[2])
                    y = float(l[3])
                    nav2.goto(x, y, l[4])
                elif l[0] == "sync":
                    flag = int(l[1])
                    if flag == 1:
                        tctime = float(l[2])
                        print("syncing to %f" % (tctime))
                        tolog("sync1")
                        g.t0 = time.time() - tctime
                        tolog("sync2")
                elif l[0] == "heartecho":
                    t1 = float(l[1])
                    t2 = float(l[2])
                    g.heartn_r = int(l[3])
                    #print("heartecho %.3f %.3f %.3f %d" % (time.time() - g.t0, t1, t2, g.heartn_r))
                else:
                    print("unknown control command %s" % data)
        time.sleep(1)


def send_to_ground_control(str):
    if not g.ground_control:
        return

    try:
        str1 = str + "\n"
        g.ground_control.send(str1.encode('ascii'))
    except Exception as e:
        print("send1 %s" % e)
        g.ground_control = None
#        connect_to_ground_control()

def open_socket():
    HOST = 'localhost'    # The remote host
    HOST = '192.168.43.73'	# merkur on my hotspot
    HOST = '193.10.66.250'  # merkur on the SICS wifi net
    HOST = '193.10.67.166'  # bubbla on the SICS wifi net
    PORT = 50009              # The same port as used by the server

    for res in socket.getaddrinfo(HOST, PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
        af, socktype, proto, canonname, sa = res
        #print("res %s" % (res,))
        try:
            s = socket.socket(af, socktype, proto)
        except Exception as e:
            #print("socket %s" % e)
            s = None
            continue

        try:
            s.connect(sa)
        except Exception as e:
            #print("connect %s" % e)
            #(socket.error, msg):
            s.close()
            s = None
            continue
        break
    if s is None:
        #print('could not open socket')
        return False

    return s

def tcinit():
    g.ground_control = None
    g.queue = queue.Queue(5)
