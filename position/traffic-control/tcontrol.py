import time
import math

from Tkinter import *

#from tcontrol_comm import *
import tcontrol_comm

from tcontrol_car import Car, cars

from tcontrol_colours import colours
from tcontrol_globals import g
#import tcontrol_globals

totalcars = 0

occupied = dict()
waiting = dict()

g.show_markpos = True
g.show_markpos1 = False

currentcar = None
currentcarcircle = None

currentmark = None
currentmarkpos = None

global markedpoint
markedpoint = None

areaobjs = []

pathlist = []
global currentpath

obstacles = dict()

sp1 = 15
sp2 = -20

sp1 = 7
sp2 = -10

sp1 = 25
sp2 = -30

sp1 = 35
sp2 = -40

sp1 = 40
sp2 = -45

sp1 = 30
sp2 = -35

sp1 = 15
sp1 = 20

path1_1 = [('go', sp1, 1.8, 16.2),
         ('speak', "#"),
         ('go', sp1, 2.3, 17.5),
         ('go', sp1, 1.4, 19),
         ('speak', 'loading'),
         ('stop',),
         ('speak', 'loading done'),
         ('go', sp1, 0.6, 18),
         ('go', sp1, 0.8, 14.0),
         ('stop', 0.5),
         ('go', sp2, 2.5, 14.6),
         ('speak', 'dumping'),
         ('stop',),
         ('speak', 'dumping done')]

path1_2 = [('go', sp1, 1.8, 16.2),
         ('speak', "#"),
         ('go', sp1, 2.3, 17.7),
         ('go', sp1, 1.6, 19),
         ('speak', 'loading'),
         ('stop',),
         ('speak', 'loading done'),
         ('go', sp1, 0.6, 17),
         ('stop',),
         ('go', sp1, 0.8, 12.0),
         #('stop', 1.0),
         ('stop', 4.0),
         ('go', sp2, 2.5, 14.4),
         ('speak', 'dumping'),
         ('stop',),
         ('speak', 'dumping done')]

path1 = path1_2

path2 = [('go', sp1, 2.5, 13.4),
         ('stop', 0.5),
         ('go', sp1, 2.5, 14.8),
         ('stop', 0.5),
         ('go', sp1, 2.5, 16.2),
         ('stop', 0.5),
         ('go', sp1, 2.5, 17.4),
         ('stop', 0.5),
         ('go', sp1, 2.0, 18.7),
         ('stop', 0.5),
         ('go', sp1, 0.7, 17.4),
         ('stop', 0.5),
         ('go', sp1, 0.7, 16.2),
         ('stop', 0.5),
         ('go', sp1, 0.7, 14.8),
         ('stop', 0.5),
         ('go', sp1, 0.7, 13.4),
         ('stop', 0.5),
         ('go', sp1, 1.2, 12.3),
         ('stop', 0.5)]

path3 = [('go', sp1, 2.3, 13.4),
         ('stop', 0.5),
         ('go', sp1, 2.3, 15.4),
         ('stop', 0.5),
         ('go', sp1, 2.5, 17.4),
         ('stop', 0.5),
         ('go', sp1, 2.1, 18.7),
         ('stop', 0.5),
         ('go', sp1, 0.8, 17.4),
         ('stop', 0.5),
         ('go', sp1, 0.8, 15.4),
         ('stop', 0.5),
         ('go', sp1, 0.8, 13.4),
         ('stop', 0.5),
         ('go', sp1, 1.2, 12.3),
         ('stop', 0.5)]

path3bis = [('go', sp1, 2.3, 13.4),
         ('go', sp1, 2.3, 15.4),
         ('go', sp1, 2.5, 17.4),
         ('go', sp1, 2.1, 18.7),
         ('go', sp1, 0.8, 17.4),
         ('go', sp1, 0.8, 15.4),
         ('go', sp1, 0.8, 13.4),
         ('go', sp1, 1.2, 12.3),
            ]

path3bis2 = [('go', sp1, 2.3, 13.4),
         ('go', sp1, 2.3, 14.4),
         ('go', sp1, 2.3, 15.4),
         ('go', sp1, 2.4, 16.4),
         ('go', sp1, 2.5, 17.4),
         ('go', sp1, 2.4, 18.0),
         ('go', sp1, 2.1, 18.7),
         ('go', sp1, 1.2, 18.8),
         ('go', sp1, 0.8, 17.4),
         ('go', sp1, 0.8, 16.4),
         ('go', sp1, 0.8, 15.4),
         ('go', sp1, 0.8, 14.4),
         ('go', sp1, 0.8, 13.4),
         ('go', sp1, 0.9, 12.7),
         ('go', sp1, 1.2, 12.3),
         ('go', sp1, 2.2, 12.2),
            ]

path4bis = [('go', sp1, 2.3, 13.4),
         ('go', sp1, 2.3, 15.4),
         ('go', sp1, 2.5, 17.4),
         ('go', sp1, 2.1, 18.7),
         #('go', sp1, 0.8, 17.4),
         #('go', sp1, 0.8, 15.4),
         ('go', sp1, 0.8, 13.4),
         ('go', sp1, 1.2, 12.3),
            ]

def draw_path(p):
    first = True

    for cmd in p:
        if cmd[0] == 'go':
            x = cmd[2]
            y = cmd[3]

            if not first:
                l = addline2(g.w, x1, y1, x, y)
                pathlist.append(l)
            else:
                (x0, y0) = (x, y)
                first = False
            (x1, y1) = (x, y)

    l = addline2(g.w, x1, y1, x0, y0)
    pathlist.append(l)

def list_obstacles():
    print obstacles.keys()

def add_obstacle(x, y):
    px = xcoord(x)
    py = ycoord(y)
    g.w.create_oval(px-1, py-1, px+1, py+1)

def save_obstacle():
    if currentmark:
        (x, y) = currentmarkpos
        obstacles[currentmarkpos] = True
        add_obstacle(x, y)

scale = 30

xoffset = 0
yoffset = 0

# minx and miny should also be involved here, but they are 0 right now
def coordx(x):
    return float(x-10-xoffset)/scale

def coordy(y):
    return float(winh-y-10-yoffset)/scale

def xcoord(x):
    return scale*x + 10 + xoffset

def ycoord(y):
    return winh - (scale*y + 10 + yoffset)


def addline(w, x1, y1, x2, y2):
    px1 = xcoord(x1)
    px2 = xcoord(x2)
    py1 = ycoord(y1)
    py2 = ycoord(y2)
    return w.create_line(px1, py1, px2, py2)

def addline2(w, x1, y1, x2, y2):
    px1 = xcoord(x1)
    px2 = xcoord(x2)
    py1 = ycoord(y1)
    py2 = ycoord(y2)
    return w.create_line(px1, py1, px2, py2, fill="#bbffbb", width=8)

def addcircle(w, x0, y0, r, colour="black"):
    x1 = x0 - r*math.sqrt(2)
    x2 = x0 + r*math.sqrt(2)
    y1 = y0 - r*math.sqrt(2)
    y2 = y0 + r*math.sqrt(2)
    px1 = xcoord(x1)
    px2 = xcoord(x2)
    py1 = ycoord(y1)
    py2 = ycoord(y2)
    return w.create_oval(px1, py1, px2, py2, outline=colour)

def draw_lines(w, lst, closed):
    global areaobjs

    if lst == []:
        return
    (x1, y1) = lst[0]
    for (x2, y2) in lst[1:]:
        l = addline(w, x1, y1, x2, y2)
        areaobjs.append(l)
        (x1, y1) = (x2, y2)
    if closed:
        l = addline(w,
                    lst[-1][0], lst[-1][1],
                    lst[0][0], lst[0][1])
        areaobjs.append(l)

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
    d = 0.72-0.28
    # flower pot:
    #addcircle(w, 3-(0.72+0.28)/2, 9.5, d/2)
    if False:
        addcircle(w, 2.0, 16.6, 0.6)
        addcircle(w, 1.8, 16.0, 0.6)
        addcircle(w, 1.8, 15.6, 0.6)

    if False:
        addcircle(w, 1.7, 17.05, 0.04)
        addcircle(w, 1.48, 16.6, 0.04)
        addcircle(w, 1.35, 16.1, 0.04)
        addcircle(w, 1.48, 15.1, 0.04)

    draw_path(currentpath)

def send_go(carno):
    c = cars[carno-1]
    if currentmark:
        (x, y) = currentmarkpos
        c.conn.send("go %f %f\n" % (x, y))

def send_path(carno, p):
    c = cars[carno-1]
    c.conn.send("path " + str(p) + "\n")

def toggle_show_markpos():
    g.show_markpos = not g.show_markpos
    print("toggle show_markpos -> %s" % str(g.show_markpos))

def toggle_show_markpos1():
    g.show_markpos1 = not g.show_markpos1
    print("toggle show_markpos1 -> %s" % str(g.show_markpos1))

def clear():
    for ci in cars:
        c = cars[ci]
        for win in c.objs:
            g.w.delete(win)
        # can objects be put in the list while we are doing this?
        c.objs = []

def redraw_area():
    global areaobjs

    for obj in areaobjs:
        g.w.delete(obj)
    areaobjs = []

    for obj in pathlist:
        g.w.delete(obj)
    areaobjs = []

    clear()
    draw_area(w)

def zoom(amount):
    global scale
    global xoffset, yoffset

    p = math.pow(1.5, amount)
    scale *= p

    yoffset = winh-uppermargin-p*(winh-uppermargin-yoffset)

    redraw_area()

def shift(x, y):
    global xoffset, yoffset

    xoffset += x*50
    yoffset += y*50
    redraw_area()

def clear_occupation():
    global occupied, waiting

    occupied = dict()
    waiting = dict()
    v5.set("")


def key_event(event):
    global currentcar

    if event.keysym == "Left":
        shift(-1, 0)
    elif event.keysym == "Right":
        shift(1, 0)
    elif event.keysym == "Up":
        shift(0, 1)
    elif event.keysym == "Down":
        shift(0, -1)
    elif event.char == 'q':
        exit(0)
    elif event.char == 'c':
        clear()
    elif event.char == 'C':
        clear_occupation()
    elif event.char == '+':
        zoom(1)
    elif event.char == '-':
        zoom(-1)
    elif event.char == 'r':
        remove_markedpoint()
    elif event.char == 'n':
        new_pathpoint()
    elif event.char == 'g':
        send_go(currentcar)
    elif event.char == 'm':
        toggle_show_markpos()
    elif event.char == 'M':
        toggle_show_markpos1()
    elif event.char == 'p':
        send_path(currentcar, currentpath)
    elif event.char == 'o':
        save_obstacle()
    elif event.char == 'O':
        list_obstacles()
    elif event.char == '1':
        select_car(1)
    elif event.char == '2':
        select_car(2)
    elif event.char == '3':
        select_car(3)
    elif event.char == '4':
        select_car(4)
    elif event.char == '>':
        increase_parameter(1)
    elif event.char == '<':
        increase_parameter(-1)
    elif event.char == '':
        pass
    else:
        print "unknown command (%s) (%s)" % (event.char, event.keysym)

def increase_parameter(amount):
    c = cars[currentcar-1]
    c.parameter += amount
    c.v6.set("%d" % c.parameter)
    c.conn.send("parameter %d\n" % c.parameter)

def select_car(i):
    global currentcar, currentcarcircle

    if i == currentcar:
        return

    if currentcarcircle != None:
        g.w.delete(currentcarcircle)

    currentcar = i
    cx = g.graphw + 20
    cy = 20
    currentcarcircle = g.w.create_oval(cx-10, cy-10, cx+10, cy+10,
                                     fill=colours[i-1],
                                     outline=colours[i-1])

def button1_event(event):
    (px, py) = (event.x, event.y)
    print (coordx(px), coordy(py))
    update_mark(coordx(px), coordy(py))

def dist(x1, y1, x2, y2):
    return math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))

def button2_event(event):
    global markedpoint

    (px, py) = (event.x, event.y)
    (x, y) = (coordx(px), coordy(py))
    d = 100000
    cmd1 = None
    i = 0
    for cmd in currentpath:
        if cmd[0] == 'go':
            x1 = cmd[2]
            y1 = cmd[3]
            if d > dist(x, y, x1, y1):
                d = dist(x, y, x1, y1)
                markedpoint = i
        i += 1
    print "markedpoint %d" % markedpoint

def button3_event(event):
    global markedpoint

    (px, py) = (event.x, event.y)
    (x, y) = (coordx(px), coordy(py))
    if markedpoint == None:
        return

    cmd1 = currentpath[markedpoint]
    cmd2 = ('go', cmd1[1], x, y)
    currentpath[markedpoint] = cmd2
    markedpoint = None
    redraw_area()    

def new_pathpoint():
    global markedpoint

    # NOT READY

    i = 0
    last = None
    for cmd in currentpath:
        if cmd[0] == 'go':
            x1 = cmd[2]
            y1 = cmd[3]
            if d > dist(x, y, x1, y1):
                d = dist(x, y, x1, y1)
                markedpoint = i
        i += 1

    sp = 7

    cmd = ('go', sp, x, y)
    currentpath[markedpoint:markedpoint] = [cmd]

    markedpoint = None
    redraw_area()

def remove_markedpoint():
    global markedpoint

    currentpath[markedpoint:markedpoint+1] = []

    markedpoint = None
    redraw_area()

def double_event(event):
    print event
    exit(0)

def carpos_event(event):
    global currentmark, currentmarkpos

    i = event.x
    ev = g.d[i]
    del g.d[i]
    
    if ev[0] == "pos":
        (type, x0, y0, ang, c) = ev
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
        r1 = g.w.create_line(x1, y1, x2, y2, fill=c.colour)
        r2 = g.w.create_line(x2, y2, x3, y3, fill=c.colour)
        r3 = g.w.create_line(x3, y3, x4, y4, fill=c.colour)
        r4 = g.w.create_line(x4, y4, x1, y1, fill=c.colour)
        r5 = g.w.create_line((x2+x3)/2,(y2+y3)/2,(x1+x2)/2,(y1+y2)/2, fill=c.colour)
        r6 = g.w.create_line((x1+x4)/2,(y1+y4)/2,(x1+x2)/2,(y1+y2)/2, fill=c.colour)
        if c.currentpos != None:
            (or1, or2, or3, or4, or5, or6) = c.currentpos
            g.w.delete(or1)
            g.w.delete(or2)
            g.w.delete(or3)
            g.w.delete(or4)
            g.w.delete(or5)
            g.w.delete(or6)
        c.currentpos = (r1, r2, r3, r4, r5, r6)
#        r1 = g.w.create_line(x-2, y-2, x+2, y+2)
#        r2 = g.w.create_line(x-2, y+2, x+2, y-2)
        win = g.w.create_line(x, y, x+1, y, fill=c.colour)
        c.objs.append(win)

    elif ev[0] == "mpos":
        (type, x, y, c, adj) = ev
        if adj:
            col = "blue"
        else:
            col = "yellow"
        win = addcircle(w, x, y, 2.0/scale, col)
        c.objs.append(win)

    elif ev[0] == "badmarker":
        (type, x, y, c) = ev
        win = addcircle(g.w, x, y, 2.0/scale, "red")
        c.objs.append(win)

    elif ev[0] == "mark":
        (type, x0, y0) = ev
        x = xcoord(x0)
        y = ycoord(y0)
        r1 = g.w.create_line(x-2, y-2, x+2, y+2)
        r2 = g.w.create_line(x-2, y+2, x+2, y-2)
        if currentmark != None:
            (or1, or2) = currentmark
            g.w.delete(or1)
            g.w.delete(or2)
        currentmark = (r1, r2)
        currentmarkpos = (x0, y0)
    else:
        print "unknown event %s" % ev

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


uppermargin = 20

winh = scale*(maxy-miny) + uppermargin
g.graphw = scale*(maxx-minx) + 20
winw = g.graphw + 200

g.w = Canvas(width=winw, height=winh, bg='white')
g.w.pack(expand=YES, fill=BOTH)


currentpath = path4bis
draw_area(g.w)


#widget = Label(g.w, text='AAA', fg='white', bg='black')
#widget.pack()
#g.w.create_window(100, 100, window=widget)

g.w.bind("<Button-1>", button1_event)
g.w.bind("<Button-2>", button2_event)
g.w.bind("<Button-3>", button3_event)
g.w.bind("<Key>", key_event)
g.w.bind("<<CarPos>>", carpos_event)
#g.w.bind("<Double-1>", double_event)

TABLE_W = 0.40
TABLE_L = 1.13

start_obstacles = [(1.26, 19.7-9.30),
                   (1.26+TABLE_W, 19.7-9.30),
                   (1.26, 19.7-9.30-TABLE_L),
                   (1.26+TABLE_W, 19.7-9.30-TABLE_L),
                   (2.10, 19.7-8.09),
                   (2.60, 19.7-7.89),
                   (3.00-0.18, 19.7-8.19-0.14),
                   (3.00-0.64, 19.7-8.39-0.14),]
#start_obstacles = []

for point in start_obstacles:
    obstacles[point] = True
    (x, y) = point
    add_obstacle(x, y)
    

g.w.focus_set()

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


select_car(1)

v5 = StringVar()
v5.set("")
tx5 = Label(g.w, textvariable=v5, bg="white", fg="black")
g.w.create_window(g.graphw+20, 300, window=tx5)

mainloop()
