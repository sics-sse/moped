from math import cos, sin, pi, atan2, sqrt

from nav_util import rev, dist

def eightpoint(cy, ang):
    cx = 1.5
    R = 1.0
    x = cx + R*sin(ang*pi/180)
    y = cy + R*cos(ang*pi/180)
    return (x, y)

piece1 = [7, 11, 17, 24, 28, 30, 36]
piece2 = [35, 32, 27, 23, 19, 13, 6]
piece3 = [5, 10, 16, 23, 26, 29, 34]
piece4 = [33, 31, 25, 22, 18, 12, 4]

piece5 = [35, 34]
piece6 = [5, 6]

piece7 = [3, 4]

# 'ways' is not used in nav.py
ways = dict()

nodes = dict()

def fillinlist(l, add):
    n = 3
    for j in range(2*n, 0, -1):
        l[j:j] = [add+l[j]]
    return l

#interleave = 1
interleave = 2

if interleave == 2:
    piece1 = fillinlist(piece1, 100)
    piece2 = fillinlist(piece2, 200)
    piece3 = fillinlist(piece3, 300)
    piece4 = fillinlist(piece4, 400)

piece2a = [35, 32, 27]
piece2a = piece2[0:3*interleave]
# via 23
piece2b = [19, 13, 6]
piece2b = piece2[3*interleave+1:]

piece3a = [5, 10, 16]
piece3a = piece3[0:3*interleave]
# via 23
piece3b = [26, 29, 34]
piece3b = piece3[3*interleave+1:]

ways[1] = piece1 + [35, 34] + piece4 + [5, 6] + [piece1[0]]
ways[3] = piece2
ways[4] = piece3
ways[2] = piece7



# for the geometric 8 only
nodenumbers = piece1 + piece2 + piece3 + piece4

def eightarc(nodenumbers, cy, angleoffset):
    # assume len(nodenumbers) == 7, 2*n+1 == 7
    n = 3
    k = interleave
    for i in range(-n*k, n*k+1):
        ang = 90.0/(n*k)*i
        (x, y) = eightpoint(cy, ang+angleoffset)
        nr = nodenumbers[0]
        nodenumbers = nodenumbers[1:]
        if nr not in nodes:
            nodes[nr] = (x, y)
        else:
            ox = nodes[nr][0]
            oy = nodes[nr][1]
            if abs(x-ox) > 0.01 or abs(y-oy) > 0.01:
                print("node %d already exists: new (%f,%f) old (%f,%f)" % (
                        nr, x, y, ox, oy))
    return nodenumbers

def eightpath(y1, y2, y3):
    R = 1.0

    eightarc(piece1, y1 - R, 0)
    eightarc(piece2, y2 + R, 180)
    eightarc(piece3, y2 - R, 0)
    eightarc(piece4, y3 + R, 180)

    # 0.5 fits with the constants in 'eightpoint'
    nodes[3] = (0.5, 8.0)

    for nr in nodes:
#        print("%d %f %f" % (nr, nodes[nr][0], nodes[nr][1]))
        pass



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

    eightpath(19.2,15.4,12.5)

    neighbours = dict()

    piecelist = [[6] + piece1 + [35],
                 [5] + rev(piece4) + [34],
                 piece2a + [23],
                 [23] + piece2b,
                 piece3a + [23],
                 [23] + piece3b,
                 [35, 34], # piece5
                 [5, 6], # piece6
                 [3, 4]] # piece7

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

#    for (x, y) in roadpoints.keys():
#        print("%f %f" % (x, y))

if __name__ == "__main__":
    eightinit()
