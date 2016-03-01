from Tkinter import *

import thread
import time
import math

d = dict()

global event_nr

event_nr = 0

def update_carpos():
    global event_nr
    t = 0.0

    while True:
        event_nr += 1
        x = 1.5 + 1.0*math.cos(30*t*math.pi/180)
        y = 7.5 + 1.0*math.sin(30*t*math.pi/180)
        #print (x, y)
        d[event_nr] = (x, y)
        w.event_generate("<<CarPos>>", when="tail", x=event_nr)
        time.sleep(0.1)
        t += 0.1

def update_carpos1(x, y):
    global event_nr
    event_nr += 1
    d[event_nr] = (x, y)
    w.event_generate("<<CarPos>>", when="tail", x=event_nr)

scale = 30

# minx and miny should also be involved here, but they are 0 right now
def coordx(x):
    return float(x-10)/scale

def coordy(y):
    return float(winh-y-10)/scale

def xcoord(x):
    return scale*x + 10

def ycoord(y):
    return winh - (scale*y + 10)


def addline(w, x1, y1, x2, y2):
    px1 = xcoord(x1)
    px2 = xcoord(x2)
    py1 = ycoord(y1)
    py2 = ycoord(y2)
    w.create_line(px1, py1, px2, py2)

def draw_lines(w, lst, closed):
    if lst == []:
        return
    (x1, y1) = lst[0]
    for (x2, y2) in lst[1:]:
        addline(w, x1, y1, x2, y2)
        (x1, y1) = (x2, y2)
    if closed:
        addline(w,
                lst[-1][0], lst[-1][1],
                lst[0][0], lst[0][1])

x2 = 3.0
y2 = 19.7

area1 = [(7, 1),
         (7, 4.3),
         (x2, 4.3),
         (x2, 0),
         (0, 0),
         (0, y2),
         (x2, y2),
         (x2, 19.0),
         (7, 19.0),
         (7, 20.0)]

area2 = [(x2, 17.2),
         (7, 17.2),
         (7, 13.8),
         (3.4, 13.8),
         (3.4, 13.5),
         (x2, 13.5)]

area3 = [(x2, 12.0),
         (7, 12.0),
         (7, 6.0),
         (x2, 6.0)]

area = [(area1, False),
        (area2, True),
        (area3, True)]

def draw_area(w):
    for (a, closed) in area:
        draw_lines(w, a, closed)

def key_event(event):
    if event.char == 'q':
        exit(0)

def button_event(event):
    (px, py) = (event.x, event.y)
    print (coordx(px), coordy(py))

def double_event(event):
    print event
    exit(0)

currentpos = None

def carpos_event(event):
    global currentpos

    i = event.x
    pos = d[i]
    del d[i]
    (x0, y0) = pos
    x = xcoord(x0)
    y = ycoord(y0)
    r = w.create_oval(x-1, y-1, x+1, y+1)
    if currentpos != None:
        w.delete(currentpos)
    currentpos = r
    #print pos

minx = None
maxx = None
miny = None
maxy = None

for (a, closed) in area:
    for (x, y) in a:
        if minx == None or minx > x:
            minx = x
        if maxx == None or maxx < x:
            maxx = x
        if miny == None or miny > y:
            miny = y
        if maxy == None or maxy < y:
            maxy = y

winh = scale*(maxy-miny) + 20
winw = scale*(maxx-minx) + 20

w = Canvas(width=winw, height=winh, bg='white')
w.pack(expand=YES, fill=BOTH)


draw_area(w)


#widget = Label(w, text='AAA', fg='white', bg='black')
#widget.pack()
#w.create_window(100, 100, window=widget)

w.bind("<Button-1>", button_event)
w.bind("<Key>", key_event)
w.bind("<<CarPos>>", carpos_event)
#w.bind("<Double-1>", double_event)

w.focus_set()

import socket, time

HOST = ''                 # Symbolic name meaning all available interfaces
PORT = 50008              # Arbitrary non-privileged port

def run():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen(1)
    while True:
        print 'Listening (at %f)' % time.time()
        (conn, addr) = s.accept()
        print 'Connected %s (at %f)' % (addr, time.time())
        while 1:
            try:
                data = conn.recv(1024)
            except socket.error as e:
                print "recv error: %s" % str(e.args)
                break

            if not data:
                break

            print "received (%s)" % data
            l = data.split(" ")
            x = float(l[1])
            y = float(l[2])
            update_carpos1(x, y)

        conn.close()


thread.start_new_thread(run, ())

mainloop()
