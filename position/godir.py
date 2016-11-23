from Tkinter import *

from math import *

sys.path.append("car-control")

from godircalc import godir

scale = 60

xoffset = 0
yoffset = 0

maxy = 19.7
maxx = 3.0
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

objs = []

def addobj(obj):
    global objs

    objs.append(obj)
    return obj

def addline(w, x1, y1, x2, y2, **kargs):
    px1 = xcoord(x1)
    px2 = xcoord(x2)
    py1 = ycoord(y1)
    py2 = ycoord(y2)
    return addobj(w.create_line(px1, py1, px2, py2, **kargs))

def draw_area(w):
    addline(w, 0, 12, 0, 19.7)
    addline(w, 3, 12, 3, 19.7)
    addline(w, 0, 19.7, 3, 19.7)
    addline(w, 0, 12, 3, 12)

def draw_car(w, x, y, ang):
    s = 0.5
    s2 = 0.2
    x1 = x - sin(ang*pi/180)*s/2
    y1 = y - cos(ang*pi/180)*s/2
    x2 = x + sin(ang*pi/180)*s/2
    y2 = y + cos(ang*pi/180)*s/2
    addline(w, x1, y1, x2, y2)
    x3 = x2 - sin((ang+30)*pi/180)*s2
    y3 = y2 - cos((ang+30)*pi/180)*s2
    addline(w, x2, y2, x3, y3)
    x4 = x2 - sin((ang-30)*pi/180)*s2
    y4 = y2 - cos((ang-30)*pi/180)*s2
    addline(w, x2, y2, x4, y4)

def addpoint(w, x, y, **kargs):
    px = xcoord(x)
    py = ycoord(y)
    s = 2
    return addobj(w.create_oval(px-s, py-s, px+s, py+s, **kargs))

def addcircle(w, x0, y0, r, colour="black"):
    x1 = x0 - r
    x2 = x0 + r
    y1 = y0 - r
    y2 = y0 + r
    px1 = xcoord(x1)
    px2 = xcoord(x2)
    py1 = ycoord(y1)
    py2 = ycoord(y2)
    return addobj(w.create_oval(px1, py1, px2, py2, outline=colour))

def key_event(event):
    global ang

    if event.char == 'q':
        exit(0)
    if event.keysym == 'Left':
        ang -= 5
        redraw(w)
    if event.keysym == 'Right':
        ang += 5
        redraw(w)

def button1_event(event):
    global ppx, ppy
    (px, py) = (event.x, event.y)
    print (coordx(px), coordy(py))
    ppx = coordx(px)
    ppy = coordy(py)
    redraw(w)

class MyCanvas(Canvas):
    def __init__(self, *args, **kargs):
        Canvas.__init__(self, *args, **kargs)

    def draw_car(self, *args, **kargs):
        return draw_car(self, *args, **kargs)

    def addcircle(self, *args, **kargs):
        return addcircle(self, *args, **kargs)

    def addline(self, *args, **kargs):
        return addline(self, *args, **kargs)

w = MyCanvas(width=winw, height=winh, bg='white')
w.pack(expand=YES, fill=BOTH)

w.bind("<Key>", key_event)
w.bind("<Button-1>", button1_event)

w.focus_set()

draw_area(w)

objs = []

ppx = 0.7
ppy = 17.0
ang = -110

def redraw(w):
    global objs

    for win in objs:
        w.delete(win)
    objs = []
    l = godir(ppx, ppy, ang, x1, y1, ang1, w)
    if l == False:
        return

    first = True
    for (x, y) in l:
        print("goto %f %f" % (x, y))
        addpoint(w, x, y)
        if not first:
            addline(w, xstart, ystart, x, y)
        first = False
        xstart = x
        ystart = y

x1 = 2.0
y1 = 12.0
ang1 = 0

redraw(w)

mainloop()
