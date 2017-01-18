# python 3 (for python 2, use Tkinter)
from tkinter import *

"""
We read car name, x, y, and angle from a file given on the command line,
then send them with some time apart to the graphic window, which draws
the current position as a little car, and keeps old positions as circles,
with colour depending on the car's name.
"""

import threading
import math
import sys
import time

fname = sys.argv[1]

f = open(fname)

data = []

for line0 in f:
    line = line0[:-1]

    l = line.split(" ")
    data.append((l[1], float(l[2]), float(l[3]), float(l[4]), float(l[0])))

currentpos = dict()

scale = 60

xoffset = 0
yoffset = 0

maxy = 10.0
maxx = 10.0
miny = 0.0
minx = 0.0

uppermargin = 20

winh = scale*(maxy-miny) + uppermargin
graphw = scale*(maxx-minx) + 20
winw = graphw + 200

# minx and miny should also be involved here, but they are 0 right now
def coordx(x):
    return float(x-10-xoffset)/scale

def coordy(y):
    return float(winh-y-10-yoffset)/scale

def xcoord(x):
    return scale*x + 10 + xoffset

def ycoord(y):
    return winh - (scale*y + 10 + yoffset)

def addline(w, x1, y1, x2, y2, **kargs):
    px1 = xcoord(x1)
    px2 = xcoord(x2)
    py1 = ycoord(y1)
    py2 = ycoord(y2)
    print((px1, py1, px2, py2))
    return w.create_line(px1, py1, px2, py2, **kargs)

def draw_area(w):
    addline(w, 0, 12, 0, 19.7)
    addline(w, 3, 12, 3, 19.7)
    addline(w, 0, 19.7, 3, 19.7)

def key_event(event):
    if event.char == 'q':
        exit(0)


colours = {"1": "blue",
           "2": "green"};

event_nr = 0
d = dict()

def carpos_event(event):

    i = event.x
    ev = d[i]
    del d[i]
    x = ev[1]
    y = ev[2]
    ang = ev[3]
    name = ev[4]
    t = ev[5]

    px = xcoord(x)
    py = ycoord(y)
    col = colours[name]
    w.create_oval(px-1, py-1, px+1, py+1, outline=col)

    x0 = x
    y0 = y

    dx1 = 0.2*math.cos(math.pi/180*ang) + 0.3*math.sin(math.pi/180*ang)
    dy1 = -0.2*math.sin(math.pi/180*ang) + 0.3*math.cos(math.pi/180*ang)

    dx2 = -0.2*math.cos(math.pi/180*ang) + 0.3*math.sin(math.pi/180*ang)
    dy2 = 0.2*math.sin(math.pi/180*ang) + 0.3*math.cos(math.pi/180*ang)

    x01 = x0+dx1
    y01 = y0+dy1

    x02 = x0+dx2
    y02 = y0+dy2

    x03 = x0-dx1
    y03 = y0-dy1

    x04 = x0-dx2
    y04 = y0-dy2

    x = xcoord(x0)
    y = ycoord(y0)
    x1 = xcoord(x01)
    y1 = ycoord(y01)
    x2 = xcoord(x02)
    y2 = ycoord(y02)
    x3 = xcoord(x03)
    y3 = ycoord(y03)
    x4 = xcoord(x04)
    y4 = ycoord(y04)
    r1 = w.create_line(x1, y1, x2, y2, fill=col)
    r2 = w.create_line(x2, y2, x3, y3, fill=col)
    r3 = w.create_line(x3, y3, x4, y4, fill=col)
    r4 = w.create_line(x4, y4, x1, y1, fill=col)
    r5 = w.create_line((x2+x3)/2,(y2+y3)/2,(x1+x2)/2,(y1+y2)/2, fill=col)
    r6 = w.create_line((x1+x4)/2,(y1+y4)/2,(x1+x2)/2,(y1+y2)/2, fill=col)
    if name in currentpos:
        (or1, or2, or3, or4, or5, or6) = currentpos[name]
        w.delete(or1)
        w.delete(or2)
        w.delete(or3)
        w.delete(or4)
        w.delete(or5)
        w.delete(or6)
    currentpos[name] = (r1, r2, r3, r4, r5, r6)
#        r1 = w.create_line(x-2, y-2, x+2, y+2)
#        r2 = w.create_line(x-2, y+2, x+2, y-2)
    win = w.create_line(x, y, x+1, y, fill=col)
#    c.objs.append(win)





def playdata():
    global event_nr

    lastt = 0

    for tup in data:
        (name, x, y, ang, t) = tup
        if t > lastt:
            time.sleep(t-lastt)
            lastt = t
        event_nr += 1
        d[event_nr] = ("mpos", x, y, ang, name, t)
        w.event_generate("<<CarPos>>", when="tail", x=event_nr)


w = Canvas(width=winw, height=winh, bg='white')
w.pack(expand=YES, fill=BOTH)

w.bind("<Key>", key_event)
w.bind("<<CarPos>>", carpos_event)

w.focus_set()

draw_area(w)

threading.Thread(target=playdata, args=()).start()

mainloop()
