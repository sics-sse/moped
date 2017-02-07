from math import cos, sin, pi, atan2, sqrt

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

def fillinlist(l):
    n = 3
    for j in range(2*n, 0, -1):
        l[j:j] = [100+l[j]]
    return l

interleave = 1
#interleave = 2

if interleave == 2:
    piece1 = fillinlist(piece1)
    piece2 = fillinlist(piece2)
    piece3 = fillinlist(piece3)
    piece4 = fillinlist(piece4)

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

def makepath(offset, path):
    path1 = []
    x1 = None
    y1 = None
    n = 0
    i1 = None
    for (i, (x0, y0)) in path:
        
        if x1 == None:
            pass
        else:
            dx = x0-x1
            dy = y0-y1
            angle = atan2(dx, -dy)

            x = x1
            y = y1

            path1.append(('go', 40, i1,
                          x+offset*cos(angle),
                          y+offset*sin(angle)))

        i1 = i
        x1 = x0
        y1 = y0
        n += 1

    # use the same angle as for the previous point
    path1.append(('go', 40, i1,
                  x1+offset*cos(angle),
                  y1+offset*sin(angle)))

    return path1

def piece2path(p, dir, offset):
    path1 = [(i, nodes[i]) for i in p]
    path = makepath(dir*offset, path1)
    return path

# also in nav.py
def dist(x1, y1, x2, y2):
    return sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))

# A position of a car in the road network is indicated by what two nodes
# A and B it is between, and how far as a fraction from A.
# From this it is easy to get coordinates, and which piece it is.
def plan(p0, p1):
    return False

def paths(n0, n1, n2=None, nz=None):
    extendpath([n0], n1, 0.0, n2, nz)

def extendpath(p, goaln, d0, n2, nz):
    nlast1 = p[-1]

    if nlast1 == goaln:
        if nz == None or p[-2] == nz:
            print("%f %s" % (d0, str(p)))
            return

    for n in neighbours[nlast1]:
        if n in p:
            continue

        if len(p) == 1 and n2 != None and n != n2:
            continue

        newp = p + [n]
        if len(newp) >= 3:
            newp3 = newp[-3:]
            if newp3 == [19, 23, 16]:
                continue
            if newp3 == [16, 23, 19]:
                continue
            if newp3 == [26, 23, 27]:
                continue
            if newp3 == [27, 23, 26]:
                continue
            if newp3 == [5, 6, 13]:
                continue
            if newp3 == [6, 5, 10]:
                continue
            if newp3 == [34, 35, 32]:
                continue
            if newp3 == [35, 34, 29]:
                continue
            if newp3 == [3, 4, 12]:
                continue
            if newp3 == [12, 4, 3]:
                continue

        extendpath(p + [n], goaln, d0 + distances[(nlast1, n)], n2, nz)

# for the selected segment, the biggest of di and dj must be minimal
def findpos(x, y, ang):
    minq = 1000
    mindidjmax = 1000
    found = None
    for (i, j) in distances:
        d = distances[(i, j)]
        (xi, yi) = nodes[i]
        (xj, yj) = nodes[j]
        di = dist(xi, yi, x, y)
        dj = dist(xj, yj, x, y)
        p = (di+dj)/d
        didjmax = max(di,dj)

        a = atan2(xj-xi, yj-yi)*180/pi

        da = a-ang
        da = da%360
        if da > 180:
            da -= 360

        da1 = abs(da)
        if da1 > 180-30:
            da1 = 180-da1
        q = didjmax/0.5 + da1/30 + (di+dj)

        #print((i, j, q, di, dj, d, (a,ang%360), (xi, yi), (x, y), (xj, yj)))

        if ((found == None or minq > q) and
#            di < 1.2*d and dj < 1.2*d and
            dj*dj < di*di+d*d and di*di < dj*dj+d*d and
            (abs(da) < 45 or abs(da) > 180-45)):
            minq = q
            found = (i, j, (i, j, di, dj, d, di+dj, di/(di+dj)))

    if not found:
        return None
    (i, j, p2) = found
    (xi, yi) = nodes[i]
    (xj, yj) = nodes[j]
    a = atan2(xj-xi, yj-yi)*180/pi

    da = a-ang
    da = da%360
    if da > 180:
        da -= 360
    if abs(da) < 45:
        return (i, j, p2)
    elif abs(da) > 180-45:
        return (j, i, p2)
    else:
        return (i, j, "unknown", da)

global distances
global neightbours

def eightinit():
    global distances, neighbours

    eightpath(19.2,15.4,12.5)

    neighbours = dict()

    pieces = [[6,7,11,17,24,28,30,36,35],
              [5,4,12,18,22,25,31,33,34],
              [35, 32, 27, 23],
              [23, 19, 13, 6],
              [5, 10, 16, 23],
              [23, 26, 29, 34],
              [35, 34],
              [5, 6],
              [3, 4]]

    distances = dict()

    for piece in pieces:
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
            lastn = n

if __name__ == "__main__":
    eightinit()
