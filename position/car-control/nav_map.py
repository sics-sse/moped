from math import cos, sin, pi, atan2, sqrt

from nav_util import rev, dist

import eight

def roaddist(x0, y0):
    dmin = None
    for (x, y) in eight.roadpoints:
        d = dist(x, y, x0, y0)
        if dmin == None or dmin > d:
            dmin = d
    return dmin

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

            path1.append((i1,
                          x+offset*cos(angle),
                          y+offset*sin(angle)))

        i1 = i
        x1 = x0
        y1 = y0
        n += 1

    # use the same angle as for the previous point
    path1.append((i1,
                  x1+offset*cos(angle),
                  y1+offset*sin(angle)))

    return path1

def piece2path(p, offset):
    path1 = [(i, eight.nodes[i]) for i in p]
    path = makepath(offset, path1)
    return path

# A position of a car in the road network is indicated by what two nodes
# A and B it is between, and how far as a fraction from A.
# From this it is easy to get coordinates, and which piece it is.
def plan(p0, p1):
    return False

def isdecisionpoint(n):
    for (a, b) in eight.pieces:
        if a == n:
            return True
    return False

# A piece goes from a over the list l to b
# n0 is in l
# Sum the distances from a to n0, and from n0 to b
def partdist(n0, a, b, l):
    da = 0
    db = eight.distances[(b, l[-1])]
    before_n0 = True
    lastn = a
    for n in l:
        if before_n0:
            da += eight.distances[(lastn, n)]
        else:
            db += eight.distances[(lastn, n)]
        lastn = n
        if n == n0:
            before_n0 = False
    return (da, db)

# Bug: going only within one piece doesn't work.

# If n0 or n1 are not decision points, n2 and nz are still to be
# decision points.
def paths_p(n0, n1, n2=None, nz=None):
    extra0 = None
    n0x = n0
    for (a, b) in eight.pieces:
        (l, dtot) = eight.pieces[(a, b)]
        if n0 in l:
            (da, db) = partdist(n0, a, b, l)
            #print(("dist", da, db, dtot))
            extra0 = (a, b, da, db)
            n0x = a
            break

    extra1 = None
    n1x = n1
    for (a, b) in eight.pieces:
        (l, dtot) = eight.pieces[(a, b)]
        if n1 in l:
            (da, db) = partdist(n1, a, b, l)
            #print(("dist", da, db, dtot))
            extra1 = (a, b, da, db)
            n1x = b
            break

    #print (extra0, extra1, n0x, n1x)

    pl0 = extendpath_p([n0x], n1x, 0.0, n2, nz, [])
    pl = []
    for (d, l) in pl0:
        if extra0:
            (a, b, da, db) = extra0
            if l[1] == b:
                l = [n0] + l[1:]
                d -= da
            else:
                # we should make sure that (b,a) is also possible
                l = [n0] + l
                d += da
        if extra1:
            (a, b, da, db) = extra1
            if l[-2] == a:
                l = l[:-1] + [n1]
                d -= db
            else:
                l = l + [n1]
                d += db
        pl.append((d, l))

    return pl

def neighbours_p(n):
    l = []
    for (a, b) in eight.pieces:
        if a == n:
            (_, d) = eight.pieces[(a, b)]
            l.append((b, d))
    return l

# for the selected segment, the biggest of di and dj must be minimal
def findpos(x, y, ang, knownnodes = None):
    minq = 1000
    mindidjmax = 1000
    found = None
    if x == None or y == None:
        return None

    if knownnodes != None and knownnodes != []:
        distances1 = knownnodes
        #print("knownnodes = %s" % str(knownnodes))
    else:
        distances1 = eight.distances

    for (i, j) in distances1:
        d = eight.distances[(i, j)]
        (xi, yi) = eight.nodes[i]
        (xj, yj) = eight.nodes[j]
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
            (
                (dj*dj < di*di+d*d and di*di < dj*dj+d*d) or
                (dj < 0.5 or di < 0.5)
                ) and
            (abs(da) < 45 or abs(da) > 180-45)):
            minq = q
            found = (i, j, (i, j, di, dj, d, di+dj, di/(di+dj)))

    if not found:
        return None
    (i, j, p2) = found
    (xi, yi) = eight.nodes[i]
    (xj, yj) = eight.nodes[j]
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

# a and b are known to be in the same piece
# Return a list l of waypoints where l[0] == a and l[-1] == b
def insert_waypoints(a, b):
    if (a, b) in eight.pieces:
        (l, _) = eight.pieces[(a, b)]
        l = [a] + l + [b]
        return l

    for (a1, b1) in eight.pieces:
        (l, _) = eight.pieces[(a1, b1)]
        l = [a1] + l + [b1]
        if a not in l:
            continue
        if b not in l:
            continue
        ia = l.index(a)
        ib = l.index(b)
        if ia > ib:
            continue
        return l[ia:ib+1]

    # what should we do? throw exception?
    return None

# Apply insert_waypoints to successive pairs in l0
def insert_waypoints_l(l0):
    lastn = l0[0]
    l = [lastn]
    for n in l0[1:]:
        l1 = insert_waypoints(lastn, n)
        l += l1[1:]
        lastn = n

    return l

# We are between nodes a and b, coming from a, proportion q from a
# Return the endpoints of the piece and at what proportion from the
# starting point we are.
def findpiece(a, b, q):
    found = None
    for (a1, b1) in eight.pieces:
        (l, _) = eight.pieces[(a1, b1)]
        l = [a1] + l + [b1]
        if a in l and b in l:
            ia = l.index(a)
            ib = l.index(b)
            d = 0
            if ia < ib:
                lastn = l[0]
                for n in l[1:]:
                    if lastn == a:
                        d += q * eight.distances[(lastn,n)]
                        break
                    d += eight.distances[(lastn,n)]
                    lastn = n
                found = ((a1, b1), d)
            else:
                l = rev(l)
                lastn = l[0]
                for n in l[1:]:
                    if lastn == a:
                        d += q * eight.distances[(lastn,n)]
                        break
                    d += eight.distances[(lastn,n)]
                    lastn = n
                found = ((b1, a1), d)
            break
    return found

