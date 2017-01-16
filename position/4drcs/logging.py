print0 = print

f = None

def print(s):
    global f
    if f == None:
        f = open("rcslog", "w")

    print0(s)
    f.write(s + "\n")

def done():
    f.close()
