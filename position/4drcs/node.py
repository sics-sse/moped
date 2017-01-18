class Node:
    def __init__(self, vehicle):
        print("Node")
        self.lower = None
        self.upper = None
        self.vehicle = vehicle
        self.wm = vehicle.wm
