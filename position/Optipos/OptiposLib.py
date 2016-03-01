'''
OptiposLib is the library for the Optical Positioning system.

Created on 6 nov. 2015

@author: Jakob Axelsson
'''

import cv2
import datetime
import json
import math
import numpy as np
import os

# Auxiliary functions

import time

class Timer(object):
    def __init__(self, verbose=False, key=""):
        self.verbose = verbose
        self.key = key

    def __enter__(self):
        self.start = time.time()
        return self

    def __exit__(self, *args):
        self.end = time.time()
        self.secs = self.end - self.start
        self.msecs = self.secs * 1000  # millisecs
        if self.verbose:
            print(self.key + " - Elapsed time: %f ms" % self.msecs)


traceFlag = False

def trace(functionName):
    """
    This is a decorator function to trace calls to a function.
    Usage: Before the definition of a function foo put the line @trace("foo").
    """
    def decorator(function):
        def traceWrapper(*args, **kwargs):
            print("Calling " + functionName + "(" + str(args) + ", " + str(kwargs) + ")")
            result = function(*args, **kwargs)
            print("Leaving " + functionName)
            return result
        if traceFlag:
            return traceWrapper
        else:
            return function
    return decorator


def rotateFloat(pos, angle, delta):
    # Calculates the position of pos in a coordinate system which is a translation by delta
    # and a rotation of angle (in degrees). The result is a float.
    angleRad = math.radians(angle)
    return (pos[0] * math.cos(angleRad) - pos[1] * math.sin(angleRad) + delta[0],
            pos[0] * math.sin(angleRad) + pos[1] * math.cos(angleRad) + delta[1])


def markerFieldValues(numberOfFieldsPerCircle, mType):
    """
    Returns a list of ints where the i:th element corresponds to the i:th field color
    of a marker of the given mType. Assumes black and white images.
    """
    result = [1] # Background is white
    remainder = mType
    for i in range(numberOfFieldsPerCircle * 2, 0, -1):
        result.append(remainder // 2 ** (i - 1) + 1)
        remainder = remainder % 2 ** (i - 1)
    return result


def markerType(markerFields):
    """
    Returns the marker type represented by a certain set of marker fields.
    Field colors are 1, 2, and the first field is the background.
    """
    result = 0
    weight = 1
    for f in markerFields[1:][::-1]:
        result += (f - 1) * weight
        weight *= 2
    return result


class MarkerMap(object):

    """
    MarkerMap is a class containing the data from a map file, which is in json format.
    """

    def __init__(self):
        self.ceilingHeight = 2.40  # Ceiling height in meters
        self.markerSize = 0.08  # Marker size (diameter of a circle) in meters
        # A list of markers, where each marker is a tuple (x, y, o, t), indicating x-position, y-position, orientation, and type
        self.markers = []
        # The number of fields in each circle. These are referred to as field1, field2, etc. field0 is always the background.
        self.numberOfFieldsPerCircle = 1 
        # The number of colors used in markers. These are referred to as color1, color2, etc. color0 is always the background.
        self.numberOfMarkerColors = 2
        # The regions are a list of rectangles representing areas that are allowed.
        self.regions = []


    def load(self, fileName):
        """
        load reads a json-file containing a marker map.
        """
        with open(fileName, "r") as file:
            fileData = file.read()
        mapData = json.loads(fileData)
        try:
            self.ceilingHeight = mapData["ceilingHeight"]
            self.markerSize = mapData["markerSize"]
            self.numberOfFieldsPerCircle = mapData["numberOfFieldsPerCircle"]
            self.numberOfMarkerColors = mapData["numberOfMarkerColors"]
            # Build the list of markers. The last tuple item contains the marker field values corresponding to the type.
            self.markers = [(d["x"], d["y"], d["o"], d["t"], markerFieldValues(self.numberOfFieldsPerCircle, d["t"])) for d in mapData["markers"]]
            # Also include the "flipped" representation of each marker, indicated by a type < 0.
            self.markers += [(m[0], m[1], m[2], -m[3], [m[4][0]] + m[4][1:][::-1]) for m in self.markers]
            # Build the list of regions
            self.regions = [(d["x1"], d["y1"], d["x2"], d["y2"]) for d in mapData["regions"]]
        except Exception as e:
            print("Error reading map file: " + str(e))


class Settings(object):
    
    def __init__(self):
        self.cameraFieldOfView = 0
        self.cameraOffsetX = 0.0
        self.cameraOffsetY = 0.0
        self.cameraOffsetZ = 0.0
        self.cameraRotation = 0.0
        self.cols = 0
        self.rows = 0
        self.pixelsPerMeter = 0
        self.markerSizePixels = 0
        self.maxSpeed = 0  # Maximum speed of the vehicle, in m/s

        self.circleRankWeight = 0.1
        self.minNumberOfCircles = 5
        self.maxNumberOfCircles = 10
        
        # This setting is not read from the file, but only initialized here
        self.cannyThreshold = 60.0
        
        
    def load(self, fileName):
        """
        load reads a json-file containing settings describing the car and the algorithms to be used.
        """
        with open(fileName, "r") as file:
            fileData = file.read()
        settingsData = json.loads(fileData)
        try:
            # Initialize camera parameters
            self.cameraFieldOfView = settingsData["cameraFieldOfView"]
            self.cameraOffsetX = settingsData["cameraOffsetX"]
            self.cameraOffsetY = settingsData["cameraOffsetY"]
            self.cameraOffsetZ = settingsData["cameraOffsetZ"]
            self.cameraRotation = settingsData["cameraRotation"]
            self.maxSpeed = settingsData["maxSpeed"]
            self.circleRankWeight = settingsData["circleRankWeight"]
            self.minNumberOfCircles = settingsData["minNumberOfCircles"]
            self.maxNumberOfCircles = settingsData["maxNumberOfCircles"]
        except Exception as e:
            print("Error reading settings file: " + str(e))


    def setImageSize(self, w, h, m):
        """
        setImageSize is used to capture the size of the image. It also calculates the size of a marker.
        """
        self.cols = w
        self.rows = h
        self.pixelsPerMeter = self.rows / (2.0 * (m.ceilingHeight - self.cameraOffsetZ) * math.tan(math.radians(self.cameraFieldOfView / 2.0)))
        self.markerSizePixels = round(self.pixelsPerMeter * m.markerSize)

    
class CircleCandidate(object):
    
    def __init__(self, x, y, r, rank):
        """
        Creates a CircleCandidate object representing a circle with center in (x, y) and radius r.
        The rank indicates how good the circle identification was, and a lower rank is better than a higher.
        """
        self.x = x
        self.y = y
        self.radius = r
        self.rank = rank


class MarkerCandidate(object):
    
    # Create a dictionary for caching field masks, to avoid recomputing them 
    fieldMaskCache = dict()
    
    def __init__(self, size, c1, c2):
        """
        Returns a MarkerCandidate object, consisting of the circles c1, c2, where the circle size is given in pixels.
        The number of valid marker types (t) can be calculated from the number of fields per circle (m) and the number of colors (n) as follows:
        t = (n ^ 2m - n ^ m) / 2 = n ^ m (n ^ m - 1) / 2. For instance, m = 2, n = 3 -> 36; m = 1, n = 2 -> 1; m = 3, n = 2 -> 28; m = 4, n = 2 -> 120. 
        """
        self.markerSizePixels = size
        self.circle1 = c1
        self.circle2 = c2
        self.x = (c1.x + c2.x) / 2
        self.y = (c1.y + c2.y) / 2
        self.orientation = 0
        self.qualityFactor = 0.0
        self.markerType = 0


    def circleRank(self):
        """
        Returns a metric calculated from the ranks of the marker circles. If the marker consists of the best two circles, the function returns 0.
        If it is the two following circles, it returns 1, etc.
        """
        return (self.circle1.rank + self.circle2.rank - 1) / 4


    def fieldMask(self, field, numberOfFieldsPerCircle):
        """
        Returns a square matrix of size 3 * self.markerSizePixels, where the elements corresponding to the given field are 1, and all other elements are 0.
        """
        
        if (field, numberOfFieldsPerCircle) in self.fieldMaskCache:
            return self.fieldMaskCache[(field, numberOfFieldsPerCircle)]
        else:
            halfSize = 3 * self.markerSizePixels // 2
            result = np.zeros((halfSize * 2, halfSize * 2), dtype = np.uint8)
            fillColor = 255
            if field == 0: # Background field, return a rectangle around the circles 
                result = np.bitwise_not(self.markerMask())
            elif 0 < field and field <= 2 * numberOfFieldsPerCircle:
                if field <= numberOfFieldsPerCircle:
                    # First circle
                    y = - 3 * self.markerSizePixels // 4
                    rotationAngle = (-90 + (field - 1) * 360 // numberOfFieldsPerCircle) % 360
                else:
                    # Second circle
                    y = 3 * self.markerSizePixels // 4
                    rotationAngle = (90 - (field - numberOfFieldsPerCircle) * 360 // numberOfFieldsPerCircle) % 360
                cv2.ellipse(result, (halfSize, halfSize + y), (self.markerSizePixels // 2, self.markerSizePixels // 2), 
                            rotationAngle, 0, 360 // numberOfFieldsPerCircle, fillColor, cv2.FILLED)
            else:
                raise Exception("MarkerCandidate.fieldMask: invalid field: " + str(field))
            self.fieldMaskCache[(field, numberOfFieldsPerCircle)] = result
            return result
        
        
    def markerMask(self):
        """
        markerMask returns a mask for all the fields in the marker. This is just the union of the masks for the individual fields.
        """
        halfSize = 3 * self.markerSizePixels // 2
        result = np.zeros((halfSize * 2, halfSize * 2), dtype = np.uint8)
        fillColor = 255
        # First circle
        cv2.ellipse(result, (halfSize, halfSize - 3 * self.markerSizePixels // 4), (self.markerSizePixels // 2, self.markerSizePixels // 2), 
                    0, 0, 360, fillColor, cv2.FILLED)
        cv2.ellipse(result, (halfSize, halfSize + 3 * self.markerSizePixels // 4), (self.markerSizePixels // 2, self.markerSizePixels // 2), 
                    0, 0, 360, fillColor, cv2.FILLED)
        return result


class Optipos(object):
    
    def __init__(self, s, m):
        """
        The parameters s and m are the settings and marker map to use. If they are strings, the actual objects are created from files.
        Otherwise, they are used as is.
        """
        if isinstance(m, str):
            markerMapObject = MarkerMap()
            markerMapObject.load(os.path.normpath(m))
        else:
            markerMapObject = m
        self.markerMap = markerMapObject

        if isinstance(s, str):
            settingsObject = Settings()
            settingsObject.load(os.path.normpath(s))
        else:
            settingsObject = s
        self.settings = settingsObject

        self.previousPosition = None
        self.circles = []
        self.markerCandidates = []


    def setMarkerMap(self, m):
        """
        Changes the marker map used. If the map changes, the coordinate system also changes, and the previousPosition does not make any sense.
        """
        if isinstance(m, str):
            markerMapObject = MarkerMap()
            markerMapObject.load(os.path.normpath(m))
        else:
            markerMapObject = m
        self.markerMap = markerMapObject
        self.previousPosition = None


    def processImage(self, image, time = None):
        """
        processImage takes an image, and returns a the position based on the best marker found in it.
        If image is a string, the actual image is loaded from the file with that name.
        If time is provided, it should be an object of the class datetime, indicating when the image was captured.
        """

        if isinstance(image, str):
            image = cv2.imread(os.path.normpath(image))

        self.originalImage = image
        self.bwImage = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        (rows, cols) = image.shape[:2]
        if (self.settings.cols != cols) or (self.settings.rows != rows):
            self.settings.setImageSize(cols, rows, self.markerMap)
            
        with Timer(True, "identifyCircles") as _:
            self.circles = self.identifyCircles(self.bwImage)        
        with Timer(True, "findMarkers") as _:
            self.markerCandidates = self.findMarkers(self.circles)
        with Timer(True, "classifyMarker") as _:
            print("Number of markers = " + str(len(self.markerCandidates)))
            self.markerCandidates = [self.classifyMarker(self.bwImage, mc, time) for mc in self.markerCandidates]
        with Timer(True, "selectAndRankValidMarkers") as _:
            self.markerCandidates = self.selectAndRankValidMarkers(self.markerCandidates)
        result = self.calculatePosition(self.markerCandidates, time)
        
        # If the quality of the new position is sufficient, set previousPosition to the new one
        # TODO: Introduce a parameter for the threshold instead of a hard coded value.
        if result and result[3] > 0.2:
            self.previousPosition = (result[0], time)
        return result


    def identifyCircles(self, image):
        """
        Identify circles using Hough algorithm in an image, returning a list of (x, y) positions indicating circle centers. 
        """
        # Iterate through different parameter settings, being increasingly more permissive, until at least 5 circles have been found
#        for param2 in range(25, 0, -5):
        for param2 in range(5, 0, -5):
            circles = cv2.HoughCircles(
                                image = image, 
                                method = cv2.HOUGH_GRADIENT, 
                                dp = 1, # Use accumulator same size as image
                                minDist = self.settings.markerSizePixels * 1.2, # In a marker, the circle centers are 1.5 * markerSizePixels apart
                                param1 = self.settings.cannyThreshold, 
                                param2 = param2, 
                                minRadius = round(self.settings.markerSizePixels * 0.3), # In a marker, the circle radius is 0.5 * markerSizePixels
                                maxRadius = round(self.settings.markerSizePixels * 0.7))
            if circles != None:
                if len(circles[0]) >= self.settings.minNumberOfCircles:  # If at least 5 circles found, stop trying
                    return [CircleCandidate(c[0], c[1], c[2], rank) for (c, rank) in zip(np.round(circles[0, :]).astype("int"), range(0, len(circles[0, :])))][0:self.settings.maxNumberOfCircles]

        # No circles found
        return []


    def findMarkers(self, circles):
        """
        The findMarkers function searches a vector of circles trying to find pairs of circles whose centers are at a distance 
        1.5 * markerSizePixels.  For each such pair, a quality metric is calculated which is based on the the quality of each 
        circle and the distance between them. Pairs with high enough quality are included in the resulting list of marker 
        candidates.
        """
        markerCandidates = []
        for i in range(0, len(circles)):
            for j in range(i + 1, len(circles)):
                dx = circles[i].x - circles[j].x
                dy = circles[i].y - circles[j].y
                distance = math.sqrt(dx * dx + dy * dy)
                # Distance should be around 1.5 * markerSizePixels
                if self.settings.markerSizePixels < distance and distance < self.settings.markerSizePixels * 2: 
                    m = MarkerCandidate(self.settings.markerSizePixels, circles[i], circles[j])
                    if circles[i].y == circles[j].y:
                        m.orientation = 90
                    else:
                        m.orientation = -round(math.degrees(math.atan(dx / dy)))
                    markerCandidates = markerCandidates + [m]
        return markerCandidates

                
    def markerImage(self, image, mc):
        """
        markerImage returns a smaller image which contains the marker mc in a standardized position.
        The resulting image is divided into its HSV components.
        """
        halfSize = 3 * self.settings.markerSizePixels // 2
        # Add padding to make sure that it is always possible to extract a region of interest
        image = cv2.copyMakeBorder(image, halfSize, halfSize, halfSize, halfSize, cv2.BORDER_CONSTANT, value = [0, 0, 0])
        # Due to padding, halfSize has to be added to (mc.x, mc.y)
        markerImage = image[halfSize + mc.y - halfSize: halfSize + mc.y + halfSize, halfSize + mc.x - halfSize : halfSize + mc.x + halfSize]
        rotationMatrix = cv2.getRotationMatrix2D((halfSize, halfSize), mc.orientation, 1.0)
        rotatedMarkerImage = cv2.warpAffine(markerImage, rotationMatrix, (halfSize * 2, halfSize * 2))
        return rotatedMarkerImage
        

    def possibleMarkers(self, time):
        """
        possibleMarkers returns the list of markers which could possibly be seen from a position which is reachable from the previous
        position given the time that has passed and the maximum speed of the vehicle. If no previous position exists, or if no time
        is given, the list of all markers is returned.
        """
        return self.markerMap.markers

    def classifyMarker(self, image, mc, time):
        """
        The classifyMarker function calculates the probability that each of the fields has each color.
        Based on this, it classifies the marker, and determines a quality factor for the identification. 
        It also adjusts marker orientation.
        TODO: It could also be generalized to multiple colors.
        """

        # Extract the field values for each field in the marker
        # Get the image of the marker. It is transformed into uint32 to increase efficiency when extracting fields.
        # TODO: For some reason, the uint32 optimization works with images read from file (as in OptiposTools) 
        # but not with images decoded from a buffer (as in OptiposCloud). Why?
#        valueMatrix = self.markerImage(image, mc).view("uint32")
        valueMatrix = self.markerImage(image, mc)
        fieldValues = []
        for field in range(0, self.markerMap.numberOfFieldsPerCircle * 2 + 1):
            # The operation below is performed using uint32 instead of uint8 to speed up processing.
#            values = np.bitwise_and(valueMatrix, mc.fieldMask(field, self.markerMap.numberOfFieldsPerCircle).view("uint32")).view("uint8")
            values = np.bitwise_and(valueMatrix, mc.fieldMask(field, self.markerMap.numberOfFieldsPerCircle))
            fieldValues += [values[np.nonzero(values)]] # Remove pixels outside marker
        
        # Calculate the quality factor based on coefficient of determination (https://en.wikipedia.org/wiki/Coefficient_of_determination)
        # TODO: The for loop below is the most costly part of the algorithm. Can it be optimized further?
        qualityFactors = []
        markers = self.possibleMarkers(time)
        for (_, _, _, t, m) in markers:
            whiteFields = [fieldValues[i] for (i, v) in enumerate(m) if v == 1]
            blackFields = [fieldValues[i] for (i, v) in enumerate(m) if v == 2]

            whiteMean = sum([np.sum(f) for f in whiteFields]) / sum([f.size for f in whiteFields])
            blackMean = sum([np.sum(f) for f in blackFields]) / sum([f.size for f in blackFields])
            totalMean = sum([np.sum(f) for f in whiteFields + blackFields]) / sum([f.size for f in whiteFields + blackFields])

            whiteResidualSS = sum([np.sum((m - whiteMean) ** 2) for m in whiteFields])
            blackResidualSS = sum([np.sum((m - blackMean) ** 2) for m in blackFields])
            totalSS = sum([np.sum((m - totalMean) ** 2) for m in whiteFields + blackFields])
            q = 1 - (whiteResidualSS + blackResidualSS) / totalSS
            qualityFactors = [(t, q)] + qualityFactors
        # Sort them in rank order
        qualityFactors = sorted(qualityFactors, key = lambda p: p[1], reverse = True)

        # Adjust the quality factor of the best alternative by taking the circle rank into account
        # TODO: The weight should be a parameter in the settings file.
        mc.qualityFactor = max(0, qualityFactors[0][1] - mc.circleRank() * 0.1)

        # Determine orientation of marker        
        if qualityFactors[0][0] < 0:
            mc.orientation = (mc.orientation + 180) % 360
            mc.markerType = -qualityFactors[0][0]
        else:
            mc.markerType = qualityFactors[0][0]
        return mc


    def selectAndRankValidMarkers(self, markers):
        """
        selectAndRankValidMarkers first filters out marker candidates that do not represent valid markers.
        Next, it sorts markers in order of decreasing quality.
        Finally, if there are markers of the same type, only the one with the highest quality is kept.
        TODO: Is m.markerType ever != 0? If not, this function can be simplified to:
        return sorted(markers, key = lambda m: m.qualityFactor, reverse = True)
        """
        # Select only those candidates that represent valid markers
        validMarkers = [m for m in markers if m.markerType != 0]

        # Order the markers so that the best marker is first in the list, based on quality factor
        sortedValidMarkers = sorted(validMarkers, key = lambda m: m.qualityFactor, reverse = True)
        return sortedValidMarkers


    def allowedPosition(self, x, y):
        """
        Allowed position returns True if (x, y) is within one of the regions defined in the map.
        """
        return any(x1 <= x and x <= x2 and y1 <= y and y <= y2 for (x1, y1, x2, y2) in self.markerMap.regions)


    def calculatePosition(self, markerCandidates, time = None):
        """
        calculatePosition calculates the global position indicated by a set of marker candidates on a given map,
        taking the previous position into account. It also calculates the camera orientation.
        The current implementation only looks at the best marker, and matches that against the marker on the map
        with the same type, which is closest to the previous position.
        If time is provided, it should be of class datetime.
        
        TODO: A more elaborate procedure could look at all identified markers in the image, and try to combine them
        by finding a region on the map where all of them should be visible.
        Also, it would make sense to look at the previous position, and the max speed of the vehicle, to filter out the region of possible positions,
        and only consider markers within that region. 
        """
        best = None
        if markerCandidates:
            # Find the appropriate marker on the map
            m = markerCandidates[0]
            dist = float("inf")  # Infinity
            for (x, y, o, t, _) in self.markerMap.markers:
                if m.markerType == t:
                    if self.previousPosition:
                        ((prevX, prevY), prevTime) = self.previousPosition
                        if time:
                            print("Time delta is " + str(time - prevTime))
                        dx = x - prevX
                        dy = y - prevY
                        d = math.sqrt(dx * dx + dy * dy)
                    else:
                        d = 10e10 # A very large but finite number
                    if d < dist:
                        dist = d
                        best = (x, y, o, t)

        # Derive the global position of the marker
        if best:
            # Initialize values. (x, y) is initially the position of the marker relative to the camera in the image
            (x, y) = (m.x, m.y)
            orientation = m.orientation
            
            # Move origo from the top left corner of the image to the center
            (x, y) = (m.x - self.settings.cols // 2, m.y - self.settings.rows // 2)
            
            # Translate from pixels to meters
            (x, y) = (x / self.settings.pixelsPerMeter, y / self.settings.pixelsPerMeter)
            
            # Change the direction on the y-axis. In the image, larger y's are lower down in the image, but in the map, larger y's are higher up.
            y = -y
            
            # Flip the image, so that we look upon it from above rather than from below
            x = -x
            
            # Compensate orientation for camera rotation
            (x, y) = rotateFloat((x, y), -self.settings.cameraRotation, (0, 0))
            orientation = (orientation - self.settings.cameraRotation + 360) % 360
            
            # Compensate for the distance between the camera center and the vehicle center
            # (x, y) is now the position of the marker relative to the camera expressed in meters and with directions as in the map
            (x, y) = (x - self.settings.cameraOffsetX, y - self.settings.cameraOffsetY)
            
            # Calculate the distance and direction from the car to the marker
            distance = math.sqrt(x * x + y * y)
            direction = math.atan2(x, y) # In radians
            
            # Calculate the deltaX, deltaY from the marker to the car
            direction = direction + math.radians(orientation) # In radians
            (deltaX, deltaY) = (-math.sin(direction) * distance, -math.cos(direction) * distance)

            # Add the relative position to the actual position of the marker
            (x, y) = (deltaX + best[0], deltaY + best[1])
            
            newPos = (x, y)
            if self.allowedPosition(x, y):
                return (newPos, int(orientation), m.markerType, m.qualityFactor)
            else:
                print("Position " + str(newPos) + " is not in a region in the map.")
                return None
        else:
            return None
