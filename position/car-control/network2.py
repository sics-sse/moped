from math import cos, sin, pi, atan2, sqrt

from nav_util import rev, dist

# 'ways' is not used in nav.py
ways = dict()

nodes = dict()

# n = 3 is not correct here
def fillinlist(l, add):
    n = 3
    for j in range(2*n, 0, -1):
        l[j:j] = [add+l[j]]
    return l

interleave = 1
#interleave = 2

x0 = 1.8
y0 = 12.0
y1 = 18.0
n = 15

piece1 = []

for i in range(0, n):
    id = 101 + i
    piece1.append(id)
    nodes[id] = (x0, y0 + i*(y1-y0)/(n-1))

if interleave == 2:
    piece1 = fillinlist(piece1, 100)


ways[1] = piece1

nodenumbers = piece1

roadpoints = dict()

def makepathpoints(offset, path):
    path1 = []
    x1 = None
    y1 = None
    n = 0
    i1 = None
    gran = 10

    # make sure our areas overlap
    extra = 2

    for (i, (x0, y0)) in path:
        
        if x1 == None:
            pass
        else:
            dx = x0-x1
            dy = y0-y1
            angle = atan2(dx, -dy)

            for k in range(-extra, gran+1+extra):
                for j in range(-gran, gran+1):
                    px = x1 + k*dx/gran + offset*cos(angle)*j/gran
                    py = y1 + k*dy/gran + offset*sin(angle)*j/gran
                    roadpoints[(px,py)] = True

        i1 = i
        x1 = x0
        y1 = y0
        n += 1

    # use the same angle as for the previous point

    dx = x0-x1
    dy = y0-y1

    for k in range(-extra, gran+1+extra):
        for j in range(-gran, gran+1):
            px = x1 + k*dx/gran + offset*cos(angle)*j/gran
            py = y1 + k*dy/gran + offset*sin(angle)*j/gran
            roadpoints[(px,py)] = True

def piece2pathpoints(p, offset):
    path1 = [(i, nodes[i]) for i in p]
    makepathpoints(offset, path1)

global distances
global neighbours
global pieces

def eightinit():
    global distances, neighbours, pieces

    neighbours = dict()

    piecelist = [piece1]

    distances = dict()
    pieces = dict()

    for piece in piecelist:
        piece2pathpoints(piece, 0.35)

        dtot = 0
        lastn = None
        for n in piece:
            if lastn != None:
                if not n in neighbours:
                    neighbours[n] = []
                neighbours[n] = neighbours[n] + [lastn]
                if not lastn in neighbours:
                    neighbours[lastn] = []
                neighbours[lastn] = neighbours[lastn] + [n]
                d = dist(nodes[n][0], nodes[n][1],
                         nodes[lastn][0], nodes[lastn][1])
                distances[(n, lastn)] = d
                distances[(lastn, n)] = d
                dtot += d
            lastn = n

        pieces[(piece[0],piece[-1])] = (piece[1:-1], dtot)
        pieces[(piece[-1],piece[0])] = (rev(piece[1:-1]), dtot)

def getnextpiece(i10, i2):
    return []

#    for (x, y) in roadpoints.keys():
#        print("%f %f" % (x, y))

if __name__ == "__main__":
    eightinit()
