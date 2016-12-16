import thread
import socket
import time
import math
import json

from tcontrol_car import Car, cars

from tcontrol_globals import g

HOST = ''                 # Symbolic name meaning all available interfaces
PORT = 50008              # Arbitrary non-privileged port

g.d = dict()

global event_nr

event_nr = 0

def update_carpos1(x, y, ang, c):
    global event_nr
    event_nr += 1
    g.d[event_nr] = ("pos", x, y, ang, c)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)

def set_markerpos(x, y, c, adj):
    global event_nr
    event_nr += 1
    g.d[event_nr] = ("mpos", x, y, c, adj)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)

def set_badmarkerpos(x, y, c):
    global event_nr
    event_nr += 1
    g.d[event_nr] = ("badmarker", x, y, c)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)

def update_mark(x, y):
    global event_nr
    event_nr += 1
    g.d[event_nr] = ("mark", x, y)
    g.w.event_generate("<<CarPos>>", when="tail", x=event_nr)



# put in a tcontrol_util instead
def dist(x1, y1, x2, y2):
    return math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))



def linesplit(socket):
    buffer = socket.recv(4096)
    buffering = True
    while buffering:
        if "\n" in buffer:
            (line, buffer) = buffer.split("\n", 1)
            #print "yielding %s from %s" % (line, str(socket))
            yield line + "\n"
        else:
            more = socket.recv(4096)
            if not more:
                buffering = False
            else:
                buffer += more
    if buffer:
        yield buffer


def check_other_cars1(c):
    l = []
    for ci in cars:
        c2 = cars[ci]
        if c2 == c:
            continue
        if c2.x == None:
            continue
        d = dist(c.x, c.y, c2.x, c2.y)
        if d > 2.0:
            continue
        other = 180/math.pi*math.atan2(c2.y-c.y, c2.x-c.x)
        other = 90-other
        angdiff = other - c.ang%360
        angdiff = angdiff%360
        if angdiff > 180:
            angdiff -= 360
        #print (c2.y-c.y, c2.x-c.x)
        #print (c.ang%360, other)
        if False:
            print "%d (%.2f,%.2f): other car %d (%.2f,%.2f) dist %.2f dir %.2f" % (
                c.n, c.x, c.y,
                c2.n, c2.x, c2.y,
                d, angdiff)
        if angdiff > -45 and angdiff < 45:
            l = l + [(angdiff, d, c2.x, c2.y, c2.n)]

    fronts = "carsinfront %d" % len(l)
    for tup in l:
        fronts = fronts + " " + ("%f %f %f %f %d" % tup)
    c.conn.send(fronts + "\n")

def converging(c1, c2):
    n1 = c1.nextnode
    n2 = c2.nextnode
    l1 = c1.lastnode
    l2 = c2.lastnode
    if n1 != n2:
        return False
    if n1 == 5:
        return (l1 != 4 and l2 != 4)
    if n1 == 6:
        return (l1 != 7 and l2 != 7)
    if n1 == 35:
        return (l1 != 36 and l2 != 36)
    if n1 == 34:
        return (l1 != 33 and l2 != 33)
    return False

global doprint
doprint = None

def check_other_cars(c):
    global doprint
    l = []

    for ci in cars:
        c2 = cars[ci]
        if c2 == c:
            continue

        if (c.lastnode == -1 or c2.lastnode == -1 or
            c.nextnode == -1 or c2.nextnode == -1):
            continue

        cpair = (c.lastnode, c.nextnode)
        c2pair = (c2.lastnode, c2.nextnode)

        if (c.nextnode == c2.lastnode and c.nextnode != -1 and
            c2.nextnode != c.lastnode and c.nextnode2 == c2.nextnode) or (
            ((cpair == (33, 34) and c.nextnode == 29) or
             cpair == (34, 29)) and (c2pair == (36, 35) or
                                     c2pair == (35, 32) or
                                     c2pair == (35, 34) or
                                     c2pair == (32, 27) or
                                     c2pair == (27, 23))) or (
            ((cpair == (7, 6) and c.nextnode == 13) or
             cpair == (6, 13)) and (c2pair == (4, 5) or
                                    c2pair == (5, 10) or
                                    c2pair == (5, 6) or
                                    c2pair == (10, 16) or
                                    c2pair == (16, 23))) or (
            # when we will turn right, this makes us unnecessarily stop:
            # (and we should replace this with a distance comparison,
            # and also not always brake when entering 36-35 when a car
            # is in 35-34)
            # (in our crossing, the one turning left doesn't need to
            # give way to the one turning right, but in a real crossing,
            # it does)
            (cpair == (36, 35) or
             cpair == (35, 32) or
             cpair == (35, 34)) and (c2pair == (7, 6) or
                                     c2pair == (6, 13) or
                                     c2pair == (13, 19) or
                                     c2pair == (19, 23) or
                                     c2pair == (23, 27) or
                                     c2pair == (23, 26))) or (
            (cpair == (36, 35) and c.nextnode == 34 or
             cpair == (35, 34)) and (c2pair == (4, 5) or
                                     c2pair == (5, 10) or
                                     c2pair == (10, 16) or
                                     c2pair == (16, 23) or
                                     c2pair == (23, 27) or
                                     c2pair == (27, 32) or
                                     c2pair == (23, 26) or
                                     c2pair == (26, 29))) or (
            (cpair == (4, 5) or
             cpair == (5, 6) or
             cpair == (5, 10)) and (c2pair == (33, 34) or
                                    c2pair == (34, 29) or
                                    c2pair == (29, 26) or
                                    c2pair == (26, 23) or
                                    c2pair == (23, 16) or
                                    c2pair == (23, 19))) or (
            (cpair == (4, 5) and c.nextnode == 6 or
             cpair == (5, 6)) and (c2pair == (36, 35) or
                                   c2pair == (35, 32) or
                                   c2pair == (32, 27) or
                                   c2pair == (27, 23) or
                                   c2pair == (23, 16) or
                                   c2pair == (16, 10) or
                                   c2pair == (23, 19) or
                                   c2pair == (19, 13))) or (
            # left turn, crossing - this must get a better solution:
            cpair == (19, 23) and c2pair == (34, 29) and c.info < c2.info) or (
            cpair == (26, 23) and c2pair == (6, 13) and c.info < c2.info):
            d = 0.2
            angdiff = 0
            stri = "car in front of car %s: %s: %s" % (
                c.info, c2.info,
                str(((c.lastnode, c.nextnode),
                     (c2.lastnode, c2.nextnode))))
            if doprint != stri:
                print(stri)
                doprint = stri
            l = l + [(angdiff, d, c2.x, c2.y, c2.n)]

    fronts = "carsinfront %d" % len(l)
    for tup in l:
        fronts = fronts + " " + ("%f %f %f %f %d" % tup)
    c.conn.send(fronts + "\n")

def handlebatterytimeout(c):
    while True:
        if time.time() > c.battery_seen + 20:
            c.v4.set("battery unknown")
        time.sleep(1)

def deletecar(c):
    c.alive = False

    if c.currentpos != None:
        (or1, or2, or3, or4, or5, or6) = c.currentpos
        g.w.delete(or1)
        g.w.delete(or2)
        g.w.delete(or3)
        g.w.delete(or4)
        g.w.delete(or5)
        g.w.delete(or6)
    for win in c.windows:
        g.w.delete(win)
    del cars[c.n]

def handleheart(c, conn):
    while c.alive:
        if time.time() > c.heart_seen + c.timeout:
            print("timed out: " + c.info)
            deletecar(c)
            return
        time.sleep(5)

def esend_continue(c):
    c.conn.send("continue\n")

def arravg(l):
    n = len(l)
    sum = 0.0
    for v in l:
        sum += v
    return sum/n

def handlerun(conn, addr):
    dataf = linesplit(conn)
    print 'Connected %s (at %f)' % (addr, time.time())

    data = dataf.next()
    if data[-1] == "\n":
        data = data[:-1]
    if data[-1] == "\r":
        data = data[:-1]
    l = data.split(" ")

    print l

    warnbattery = 0

    if l[0] == "info":
        c = Car()

        c.conn = conn

        thread.start_new_thread(handlebatterytimeout, (c,))
        thread.start_new_thread(handleheart, (c, conn))

        # in case the car waited for us to start
        esend_continue(c)

        car = l[1]
        c.v3.set("car %s" % car)
        c.info = car
        c.addr = addr
        print("car %s" % car)
        if len(l) > 2:
            c.timeout = int(l[2])
    elif l[0] == "list":
        iplist = []
        for car in cars.values():
            iplist.append(car.addr[0])
        conn.send(json.dumps(iplist) + "\n")
        conn.close()
        return
    elif l[0] == "cargoto":
        carn = l[1]
        found = None
        for car in cars:
            c = cars[car]
            if carn == c.info:
                found = c
                break
        if not found:
            conn.send("{\"error\": \"no running car named %s\"}\n" % l[1])
        else:
            x = float(l[2])
            y = float(l[3])
            if x < 1.5:
                x += 0.6
            else:
                x -= 0.6
            l[2] = str(x)
            c.conn.send("%s\n" % " ".join(l))

        conn.close()
        return
    else:
        conn.send("{\"error\": \"expected keyword 'info', 'list' or 'cargoto', got '%s'\"}\n" % l[0])
        conn.close()
        return

    spavg = []
    spavgn = 50

    t0 = time.time()

    for data in dataf:

        if not c.alive:
            conn.close()
            return

        if data[-1] == "\n":
            data = data[:-1]

        #print "received (%s)" % data
        l = data.split(" ")
        # mpos = from marker; d = from dead reckoning
        #print (c, l)
        if l[0] == "mpos" or l[0] == "dpos":
            x = float(l[1])
            y = float(l[2])
            ang = float(l[3])
            c.x = x
            c.y = y
            c.ang = ang
            time1 = float(l[4])
            adj = int(l[5])
            # comes in as a float, but has only integer accuracy
            insp = float(l[6])
            spavg = [insp] + spavg
            if len(spavg) > spavgn:
                spavg = spavg[0:spavgn]
            spavg1 = arravg(spavg)

            c.v2.set("time %.1f" % time1)

            t = time.time()

            #c.v5.set("speed %d" % insp)
            if t-t0 > 1.0:
                c.v5.set("speed %d" % round(spavg1))
                t0 = t

            if l[0] == "dpos" or l[0] == "mpos" and g.show_markpos:
                update_carpos1(x, y, ang, c)
            if l[0] == "mpos" and g.show_markpos1:
                set_markerpos(x, y, c, adj)
            check_other_cars(c)

            g.logf.write("%s %f %f %s\n" % (c.info, x, y, l[0]))

        elif l[0] == "badmarker":
            x = float(l[1])
            y = float(l[2])
            set_badmarkerpos(x, y, c)
        elif l[0] == "odometer":
            odo = int(l[1])
            c.v.set("%d pulse%s = %.2f m" % (
                    odo, "" if odo == 1 else "s",
                    float(odo)/5*math.pi*10.2/100))
        elif l[0] == "heart":
            c.heart_seen = time.time()
            c.heart_t = float(l[1])
            c.heart_n = int(l[2])
            c.conn.send("heartecho %.3f %.3f %d\n" % (
                    c.heart_seen - c.t0, c.heart_t, c.heart_n))
        elif l[0] == "message":
            s = " ".join(l[1:])
            c.v8.set(s)
        elif l[0] == "stopat":
            i = int(l[1])
            i -= 1
            print "%s stopped at %d" % (c.info, i)
#            if i == 4 or i == 7 or i == 9 or i == 12:
#            if i == 2 or i == 4 or i == 6 or i == 8 or i == 10 or i == 12 or i == 14 or i == 16 or i == 18 or i == 0:
            # fits path3:
            if i == 2 or i == 4 or i == 6 or i == 8 or i == 10 or i == 12 or i == 14 or i == 0:
                if False:
                    if i == 4:
                        j = 7
                    elif i == 7:
                        j = 9
                    elif i == 9:
                        j = 12
                    elif i == 12:
                        j = 4
                else:
                    j = i+2
                    if i == 14:
                        j = 0

                print "occupied: %s" % str(occupied)
                print "waiting: %s" % str(waiting)
                for ci in cars:
                    carx = cars[ci]
                    print "%s %s" % (carx.info, str(carx.waitingat))

                if j not in occupied:
                    print " %s not occupied" % str(j)
                    print " continuing %s" % c.info
                    esend_continue(c)
                    occupied[j] = c

                    if i in occupied:
                        c1 = occupied[i]
                        print " %d was occupied by %s" % (i, c1.info)
                        del occupied[i]
                        wi = i
                        while wi in waiting:
                            print " %d was waited for" % wi
                            c2 = waiting[wi]
                            print " %s waited for %d" % (c2.info, wi)
                            print " continuing %s" % c2.info
                            occupied[wi] = c2
                            wi2 = c2.waitingat
                            if wi2 in occupied:
                                del occupied[wi2]
                            c2.waitingat = None
                            esend_continue(c2)
                            del waiting[wi]
                            wi = wi2
                else:
                    print " %s occupied, waiting" % str(j)
                    waiting[j] = c
                    print " waitingat %d" % i
                    c.waitingat = i

                s1 = ""
                if len(occupied.keys()) != 0:
                    s1 += " occupied: "
                    print(occupied)
                    for k in occupied:
                        c1 = occupied[k]
                        s1 += " %s@%d" % (c1.info, k)
                if len(waiting.keys()) != 0:
                    s1 += " waiting: "
                    for k in waiting:
                        c1 = waiting[k]
                        s1 += " %s@%d" % (c1.info, k)
                v5.set(s1)

            else:
                esend_continue(c)

        elif l[0] == "battery":
            b = float(l[1])
            c.battery_seen = time.time()
            if b < 6.8:
                warnbattery = (warnbattery + 1) % 2
            else:
                warnbattery = 0
            if warnbattery == 0:
                c.v4.set("battery %.2f" % b)
            else:
                c.v4.set("")
        elif l[0] == "markers":
            s = " ".join(l[1:])
            if False:
                delim = "/-\\|"[c.markern]
                c.markern = (c.markern + 1) % 4
            else:
                delim = "- "[c.markern]
                c.markern = (c.markern + 1) % 2
            s = delim + " " + s
            c.v7.set(s)
        elif l[0] == "between":
            i1 = int(l[1])
            i2 = int(l[2])
            c.lastnode = i1
            c.nextnode = i2
            if len(l) > 3:
                c.nextnode2 = int(l[3])
            else:
                c.nextnode2 = -1
            
            print("%s between %d and %d (then %d)" % (
                    c.info, i1, i2, c.nextnode2))
        else:
            print "received (%s)" % data

    conn.close()
    print("connection closed %d %s" % (c.n, c.info))
    deletecar(c)

def run():
    totdata = ""

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen(1)
    while True:
        print 'Listening (at %f)' % time.time()
        (conn, addr) = s.accept()
        conn.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)
        thread.start_new_thread(handlerun, (conn, addr))

thread.start_new_thread(run, ())
