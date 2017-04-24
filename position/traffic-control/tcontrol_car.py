import time

from Tkinter import *

from tcontrol_colours import colours

from tcontrol_globals import g

cars = dict()

class Car:
    def __init__(self):
        
        ns = []
        for ci in cars:
            c = cars[ci]
            ns.append(c.n)
        i = 0
        while i in ns:
            i += 1
        self.n = i

        self.oldposlist = []

        self.objs = []

        self.x = None
        self.y = None
        self.ang = None

        self.currentpos = None
        self.info = None
        self.colour = colours[self.n]
        self.battery_seen = 0
        self.heart_seen = time.time()
        self.heart_t = None

        self.t0 = time.time()

        self.alive = True

        self.waitingat = None

        print("self.n = %d" % self.n)
        cars[self.n] = self

        self.windows = []

        #self.parameter = 164.0
        self.parameter = 100.0
        self.parameter = 152.0
        self.parameter = 120.0

        self.lastnode = -1
        self.nextnode = -1
        self.nextnode2 = -1

        self.markern = 0

        self.timeout = 60

        self.betweenlist = []

        self.following = False

        carinfo_h = 140

        v = StringVar()
        v.set("hej")
        tx = Label(g.w, textvariable=v, bg="white", fg="black")
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+50, window=tx)
        v.set("hopp")
        self.v = v
        self.windows.append(ww)

        v2 = StringVar()
        v2.set("hej")
        tx2 = Label(g.w, textvariable=v2, bg="white", fg="black")
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+65, window=tx2)
        v2.set("hopp")
        self.v2 = v2
        self.windows.append(ww)

        v3 = StringVar()
        v3.set("hej")
        tx3 = Label(g.w, textvariable=v3, bg="white", fg=colours[i])
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+80, window=tx3)
        v3.set("hopp")
        self.v3 = v3
        self.windows.append(ww)

        v4 = StringVar()
        v4.set("hej")
        tx4 = Label(g.w, textvariable=v4, bg="white", fg="black")
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+95, window=tx4)
        v4.set("unknown")
        self.v4 = v4
        self.windows.append(ww)

        v5 = StringVar()
        v5.set("hej")
        tx5 = Label(g.w, textvariable=v5, bg="white", fg="black")
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+110, window=tx5)
        v5.set("unknown")
        self.v5 = v5
        self.windows.append(ww)

        v6 = StringVar()
        v6.set("hej")
        tx6 = Label(g.w, textvariable=v6, bg="white", fg="black")
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+125, window=tx6)
        v6.set("%d" % self.parameter)
        self.v6 = v6
        self.windows.append(ww)

        v7 = StringVar()
        v7.set("hej")
        tx7 = Label(g.w, textvariable=v7, bg="white", fg="black")
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+140, window=tx7)
        v7.set("%d" % self.parameter)
        self.v7 = v7
        self.windows.append(ww)

        v8 = StringVar()
        v8.set("hej")
        tx8 = Label(g.w, textvariable=v8, bg="white", fg="black")
        ww = g.w.create_window(g.graphw+20, carinfo_h*i+155, window=tx8)
        v8.set("")
        self.v8 = v8
        self.windows.append(ww)

