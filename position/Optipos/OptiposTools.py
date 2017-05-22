'''
Created on 26 nov. 2015

@author: Jakob Axelsson
'''


from OptiposLib import *

import datetime
import sys
import matplotlib.pyplot as plt


def rotateInt(pos, angle, delta):
    # Calculates the position of pos in a coordinate system which is a translation by delta and a rotation of angle (in degrees).
    # The result is an int.
    angleRad = math.radians(angle)
    return (int(round(pos[0] * math.cos(angleRad) - pos[1] * math.sin(angleRad) + delta[0])),
            int(round(pos[0] * math.sin(angleRad) + pos[1] * math.cos(angleRad) + delta[1])))


class OptiposExtended(Optipos):
    """
    This class extends Optipos with various routines for visualization etc.
    """
    
    def getColorProbabilityImage(self, color):
        """
        Returns an image showing the probabilities of each pixel being of the color
        """
        hsvImage = cv2.cvtColor(self.originalImage, cv2.COLOR_BGR2HSV) 
        (hueMatrix, saturationMatrix, valueMatrix) = cv2.split(hsvImage)
        hueProbability = fuzzyIsMemberMatrix(hueMatrix, self.markerMap.hue[color]) + fuzzyIsMemberMatrix(hueMatrix + 180, self.markerMap.hue[color])
        saturationProbability = fuzzyIsMemberMatrix(saturationMatrix, self.markerMap.saturation[color])
        valueProbability = fuzzyIsMemberMatrix(valueMatrix, self.markerMap.value[color])
        # The probability is given by the fuzzy AND operator, which is the same as minimum, as an average for all pixels in the field.
        hsvProbability = np.minimum(hueProbability, np.minimum(saturationProbability, valueProbability))
#        hsvImage = np.around(hsvProbability * 255).astype(np.uint8)
        hsvImage = cv2.merge((saturationMatrix * 0, saturationMatrix * 0, np.around(hsvProbability * 255).astype(np.uint8)))
        hsvImage = cv2.cvtColor(hsvImage, cv2.COLOR_HSV2BGR)
        return hsvImage


    def getHueImage(self):
        """
        Returns an image showing pixel hues
        """
        hsvImage = cv2.cvtColor(self.originalImage, cv2.COLOR_BGR2HSV) 
        (hueMatrix, _, _) = cv2.split(hsvImage)
        hsvImage = cv2.merge((hueMatrix, hueMatrix * 0 + 255, hueMatrix * 0 + 255))
        hsvImage = cv2.cvtColor(hsvImage, cv2.COLOR_HSV2BGR)
        return hsvImage
    

    def getSaturationImage(self):
        """
        Returns an image showing pixel saturation
        """
        hsvImage = cv2.cvtColor(self.originalImage, cv2.COLOR_BGR2HSV) 
        (_, saturationMatrix, _) = cv2.split(hsvImage)
        hsvImage = cv2.merge((saturationMatrix * 0, saturationMatrix * 0, saturationMatrix))
        hsvImage = cv2.cvtColor(hsvImage, cv2.COLOR_HSV2BGR)
        return hsvImage
    

    def getValueImage(self):
        """
        Returns an image showing pixel value, i.e. a grayscale image
        """
        hsvImage = cv2.cvtColor(self.originalImage, cv2.COLOR_BGR2HSV) 
        (_, _, valueMatrix) = cv2.split(hsvImage)
        hsvImage = cv2.merge((valueMatrix * 0, valueMatrix * 0, valueMatrix))
        hsvImage = cv2.cvtColor(hsvImage, cv2.COLOR_HSV2BGR)
        return hsvImage


    def getCannyImage(self):
        """
        Returns an image showing the result of the Canny edge detection.
        """
        grayImage = cv2.cvtColor(self.originalImage, cv2.COLOR_BGR2GRAY)
        cannyImage = cv2.Canny(grayImage, self.settings.cannyThreshold / 2, self.settings.cannyThreshold)
        hsvImage = cv2.merge((cannyImage * 0, cannyImage * 0, cannyImage))
        hsvImage = cv2.cvtColor(hsvImage, cv2.COLOR_HSV2BGR)
        return hsvImage


    def overlayCirclesOnImage(self, originalImage, overlayImage):
        """
        Output the original image with the circles centers marked on it
        """
        green = (0, 255, 0)
        for c in self.circles:
            cv2.circle(overlayImage, (c.x, c.y), c.radius, green)
            text = str(c.rank)
            ((height, width), _) = cv2.getTextSize(text, cv2.FONT_HERSHEY_SCRIPT_SIMPLEX, 0.4, 1)
            cv2.putText(overlayImage, text, (c.x - width // 2, c.y + height // 2), cv2.FONT_HERSHEY_SCRIPT_SIMPLEX, 0.4, green, 1)


    def overlayMarkersOnImage(self, originalImage, overlayImage, markerCandidates):
        
        # Colors to use for drawing
        red = (0, 0, 255)
        green = (0, 255, 0)
        color = green

        # Draw a cross at the center of the image
        (height, width) = originalImage.shape[:2]
        cv2.line(overlayImage, (width // 2, height // 2 - 10), (width // 2, height // 2 + 10), green)
        cv2.line(overlayImage, (width // 2 - 10, height // 2), (width // 2 + 10 , height // 2), green)

        # Draw the markers in reverse order, to ensure that the green marker is drawn on top, if there are duplicates
        for (i, mc) in reversed(list(enumerate(markerCandidates))):
            if i == 0:
                color = green
            else:
                color = red

            # Draw the marker outline
            p1 = rotateInt((0, -self.settings.markerSizePixels * 3 // 4), mc.orientation, (mc.x, mc.y))
            cv2.circle(overlayImage, p1, self.settings.markerSizePixels // 2, color)
    
            p1 = rotateInt((0, self.settings.markerSizePixels * 3 // 4), mc.orientation, (mc.x, mc.y))
            cv2.circle(overlayImage, p1, self.settings.markerSizePixels // 2, color)

            # Draw the fields. For each field, draw a line from the center p1 to the perimeter p2.
            # First circle
            p1 = rotateInt((0, -self.settings.markerSizePixels * 3 // 4), mc.orientation, (mc.x, mc.y))
            for field in range(1, self.markerMap.numberOfFieldsPerCircle + 1):
# This is the correct code for the general case:
                p2 = rotateInt((0, -self.settings.markerSizePixels // 2), (field - 1) * -360 // self.markerMap.numberOfFieldsPerCircle, (0, 0))
# This code compensates for the previous marker design for 2 fields per markers.
#                p2 = rotateInt((0, -self.settings.markerSizePixels // 2), (field - 1) * -360 // self.markerMap.numberOfFieldsPerCircle + 90, (0, 0))
                cv2.line(overlayImage, p1, rotateInt(p2, mc.orientation, p1), color)
            # Second circle
            p1 = rotateInt((0, self.settings.markerSizePixels * 3 // 4), mc.orientation, (mc.x, mc.y))
            for field in range(1, self.markerMap.numberOfFieldsPerCircle + 1):
# This is the correct code for the general case:
                p2 = rotateInt((0, -self.settings.markerSizePixels // 2), 180 + (field - 1) * 360 // self.markerMap.numberOfFieldsPerCircle, (0, 0))
# This code compensates for the previous marker design for 2 fields per markers.
#                p2 = rotateInt((0, -self.settings.markerSizePixels // 2), 180 + (field - 1) * 360 // self.markerMap.numberOfFieldsPerCircle + 90, (0, 0))
                cv2.line(overlayImage, p1, rotateInt(p2, mc.orientation, p1), color)

            # Draw indication of direction
    
            p1 = rotateInt((-self.settings.markerSizePixels // 2, -self.settings.markerSizePixels * 5 / 4), mc.orientation, (mc.x, mc.y))
            p2 = rotateInt((0, -self.settings.markerSizePixels * 7 // 4), mc.orientation, (mc.x, mc.y))
            cv2.line(overlayImage, p1, p2, color);
    
            p1 = rotateInt((0, -self.settings.markerSizePixels * 7 // 4), mc.orientation, (mc.x, mc.y))
            p2 = rotateInt((self.settings.markerSizePixels // 2, -self.settings.markerSizePixels * 5 // 4), mc.orientation, (mc.x, mc.y))
            cv2.line(overlayImage, p1, p2, color)
    

    def overlayTextOnImage(self, originalImage, overlayImage, text):
        green = (0, 255, 0)
        cv2.putText(overlayImage, text, (2, 11), cv2.FONT_HERSHEY_SCRIPT_SIMPLEX, 0.4, green);


    def printResult(self, result):
        """
        printResult returns a string containing the results from the position calculation.
        TODO: Maybe output CAN data directly here?
        """
        if result:
            ((x, y), orientation, markerType, qualityFactor) = result
            return str(x) + " " + str(y) + " " + str(orientation) + " " + str(markerType) + " " + str(qualityFactor)
        else:
            # If no result, return empty line
            return " "

        
    def plotBestMarkerHistogram(self):
        image = cv2.cvtColor(self.originalImage, cv2.COLOR_BGR2GRAY)
        mc = self.markerCandidates[0]

        valueMatrix = self.markerImage(image, mc)
    
        fieldValues = []
        for field in range(0, self.markerMap.numberOfFieldsPerCircle * 2 + 1):
            values = np.bitwise_and(valueMatrix, mc.fieldMask(field, self.markerMap.numberOfFieldsPerCircle))
            fieldValues += [values[np.nonzero(values)]] # Remove pixels outside marker
        
        # Plot data, one for each marker field and one for overall image
        (_, axarr) = plt.subplots(self.markerMap.numberOfFieldsPerCircle * 2 + 2)
        fig = plt.gcf()
        fig.canvas.set_window_title("Marker centered in (" + str(mc.x) + ", " + str(mc.y) + ")")
    
        for field in range(0, self.markerMap.numberOfFieldsPerCircle * 2 + 1):
            axarr[field].set_title("Field " + str(field))
            axarr[field].hist(fieldValues[field], 256, range = [0, 256], normed = 1, alpha = 0.3) # histogram
            
        # Histogram of overall image
        axarr[self.markerMap.numberOfFieldsPerCircle * 2 + 1].set_title("Overall image")
        axarr[self.markerMap.numberOfFieldsPerCircle * 2 + 1].hist(image.ravel(), 256, range = [0, 256], normed = 1, alpha = 0.3)
        plt.show(block = False)
    

        
def visualizeStep(delay, visualizationMode, optipos, text):
    """
    Visualize the algorithm in a selected output mode. It returns the last visualizationMode state.
    """
    (view, showCircles, showMarkers, showText) = visualizationMode
    while True:
        cv2.namedWindow("Display Image", cv2.WINDOW_AUTOSIZE)
        if view == 1:
            image = optipos.originalImage.copy()
            cv2.setWindowTitle("Display Image", "Original image")
        elif view in range(2, optipos.markerMap.numberOfMarkerColors + 2): 
            image = optipos.getColorProbabilityImage(view - 1)
            cv2.setWindowTitle("Display Image", "Probability of color " + str(view - 1))
        elif view == 2 + optipos.markerMap.numberOfMarkerColors:
            image = optipos.getCannyImage()
            cv2.setWindowTitle("Display Image", "Canny edge detection")
        elif view == 3 + optipos.markerMap.numberOfMarkerColors:
            image = optipos.getHueImage()
            cv2.setWindowTitle("Display Image", "Hue")
        elif view == 4 + optipos.markerMap.numberOfMarkerColors:
            image = optipos.getSaturationImage()
            cv2.setWindowTitle("Display Image", "Saturation")
        elif view == 5 + optipos.markerMap.numberOfMarkerColors:
            image = optipos.getValueImage()
            cv2.setWindowTitle("Display Image", "Value")

        # Overlay circles and markers
        if showCircles:
            optipos.overlayCirclesOnImage(image, image)
        if showMarkers:
            optipos.overlayMarkersOnImage(image, image, optipos.markerCandidates)
        if showText:
            optipos.overlayTextOnImage(image, image, text)

        cv2.imshow("Display Image", image)
        keyCode = cv2.waitKey(delay)
        if keyCode == ord('+'):
            if view == 5 + optipos.markerMap.numberOfMarkerColors:
                view = 1
            else:
                view += 1
        elif keyCode == ord('-'):
            if view == 1:
                view = 5 + optipos.markerMap.numberOfMarkerColors
            else:
                view -=1
        elif (ord('1') <= keyCode and keyCode <= ord('8')):
            view = keyCode - ord('0')
        elif keyCode == ord('c'):
            showCircles = not showCircles
        elif keyCode == ord('m'):
            showMarkers = not showMarkers
        elif keyCode == ord('h'):
            optipos.plotBestMarkerHistogram()
        elif keyCode == ord('t'):
            showText = not showText
        elif keyCode == ord('q'):
            sys.exit(0)
        if delay <= 0 or keyCode == ord(' '):
            break
    return (view, showCircles, showMarkers, showText)


def createColorHistogram(optipos, imageFileNames):
    """
    createColorHistogram creates a series of histograms showing the actual values for H, S, V for each of the three marker colors
    for marker fields in the images that have been classified as being of the corresponding color.
    """
    # Initialize histogram bins
    hueBins = [None] + [np.zeros(256) for _ in range(1, 4)]
    saturationBins = [None] + [np.zeros(256) for _ in range(1, 4)]
    valueBins = [None] + [np.zeros(256) for _ in range(1, 4)]
    
    for fileName in imageFileNames:
        # Read and process the image to get the best marker
        print("Reading " + fileName)
        image = cv2.imread(os.path.normpath(fileName))
        optipos.processImage(image)
        if optipos.markerCandidates != []: 
            # Do similar processing as in classifyMarker to determine the color of each field
            mc = optipos.markerCandidates[0]
            halfSize = 3 * optipos.settings.markerSizePixels // 2
            # Add padding to make sure that it is always possible to extract a region of interest
            image = cv2.copyMakeBorder(image, halfSize, halfSize, halfSize, halfSize, cv2.BORDER_CONSTANT, value = [0, 0, 0])
            # Due to padding, halfSize has to be added to (mc.x, mc.y)
            markerImage = image[halfSize + mc.y - halfSize: halfSize + mc.y + halfSize, halfSize + mc.x - halfSize : halfSize + mc.x + halfSize]
            rotationMatrix = cv2.getRotationMatrix2D((halfSize, halfSize), mc.orientation, 1.0)
            rotatedMarkerImage = cv2.warpAffine(markerImage, rotationMatrix, (halfSize * 2, halfSize * 2))
            # Extracted H, S, V
            hsvImage = cv2.cvtColor(rotatedMarkerImage, cv2.COLOR_BGR2HSV) 
            (hueMatrix, saturationMatrix, valueMatrix) = cv2.split(hsvImage)
        
            # Calculate the probability for each color that each pixel is of that color
            colorProbability = [None] * (optipos.markerMap.numberOfFieldsPerCircle * 2 + 1)
            for color in range (1, optipos.markerMap.numberOfMarkerColors + 1):
                # Calculate the probability of hue, saturation and value for the color given the fuzzy membership functions
                hueProbability = fuzzyIsMemberMatrix(hueMatrix, optipos.markerMap.hue[color]) + fuzzyIsMemberMatrix(hueMatrix + 180, optipos.markerMap.hue[color])
                saturationProbability = fuzzyIsMemberMatrix(saturationMatrix, optipos.markerMap.saturation[color])
                valueProbability = fuzzyIsMemberMatrix(valueMatrix, optipos.markerMap.value[color])
                # Calculate the pixel color probability by taking the fuzzy set intersection, i.e. minimum
                colorProbability[color] = np.minimum(hueProbability, np.minimum(saturationProbability, valueProbability))

            # Calculate the pixel color probability of the background color as the complement of the union of the marker colors
            union = colorProbability[1]
            for color in range(2, optipos.markerMap.numberOfMarkerColors + 1):
                union = np.maximum(union, colorProbability[color])
            colorProbability[0] = 1 - union

            # Normalize the pixel color probabilities so that the sum of probabilities for a given pixel is 1
            s = colorProbability[0]
            for color in range(1, optipos.markerMap.numberOfMarkerColors + 1):
                s = np.add(s, colorProbability[color])
            colorProbability = [colorProbability[c] / s for c in range(0, optipos.markerMap.numberOfMarkerColors + 1)]
            # Convert probabilities from 0..1 float to 0..255 uint8.
            colorProbability = [np.around(colorProbability[c] * 255).astype(np.uint8) for c in range(0, optipos.markerMap.numberOfMarkerColors + 1)]
            
            # The probability of a field to be of a color is obtained by summing the pixel probabilities for pixels in the field, and dividing it by the number of pixels in the field
            fieldColorProbability = [[0.0 for color in range(0, optipos.markerMap.numberOfMarkerColors + 1)] for field in range(0, optipos.markerMap.numberOfFieldsPerCircle * 2 + 1)]
            for field in range(0, optipos.markerMap.numberOfFieldsPerCircle * 2 + 1):
                for color in range (0, optipos.markerMap.numberOfMarkerColors):
                    fieldColorProbability[field][color] = np.sum(cv2.bitwise_and(colorProbability[color], mc.fieldMask(field, optipos.markerMap.numberOfFieldsPerCircle))) / np.sum(mc.fieldMask(field, optipos.markerMap.numberOfFieldsPerCircle))
        
            # Enumerate all valid markers regardless of orientation, i.e. all non-palindrome markers
            markers = [[0]]
            for field in range(1, optipos.markerMap.numberOfFieldsPerCircle * 2 + 1):
                markers = [m + [c] for m in markers for c in range(1, optipos.markerMap.numberOfMarkerColors + 1)]
            markers = [m for m in markers if m[1:] != (m[1:])[::-1]]

            # Calculate their probabilities and sort them in rank order
            print(fieldColorProbability)
            print(markers)
            
            markerProbabilities = sorted([(m, sum([fieldColorProbability[i][m[i]] for i in range(0, optipos.markerMap.numberOfFieldsPerCircle * 2 + 1)]) / 
                                           (optipos.markerMap.numberOfFieldsPerCircle * 2 + 1)) for m in markers], key = lambda p: p[1], reverse = True)
            mc.qualityFactor = max(0, markerProbabilities[0][1] - mc.circleRank() * 0.1)
            
            # Determine marker type of the most likely marker
            selectedColorPerField = list(markerProbabilities[0][0])
            
            # Now add the data to the histogram accumulator
            for field in range(1, 5):
                color = selectedColorPerField[field]
                hueBins[color] = np.add(hueBins[color], mc.qualityFactor * np.bincount(cv2.bitwise_and(hueMatrix, mc.fieldMask(field, optipos.markerMap.numberOfFieldsPerCircle)).ravel(), minlength = 256))
                saturationBins[color] = np.add(saturationBins[color], mc.qualityFactor * np.bincount(cv2.bitwise_and(saturationMatrix, mc.fieldMask(field, optipos.markerMap.numberOfFieldsPerCircle)).ravel(), minlength = 256))
                valueBins[color] = np.add(valueBins[color], mc.qualityFactor * np.bincount(cv2.bitwise_and(valueMatrix, mc.fieldMask(field, optipos.markerMap.numberOfFieldsPerCircle)).ravel(), minlength = 256))

    # Plot histograms
    (_, axarr) = plt.subplots(optipos.markerMap.numberOfMarkerColors, 3)
    majorLocator = ticker.MultipleLocator(50)
    minorLocator = ticker.MultipleLocator(10)
    
    for i in range(0, optipos.markerMap.numberOfMarkerColors):
        # Create plot for each color i for H, S, V
        hueBins[i + 1][0] = 0
        saturationBins[i + 1][0] = 0
        valueBins[i + 1][0] = 0
        
        axarr[i][0].xaxis.set_major_locator(majorLocator)
        axarr[i][0].xaxis.set_minor_locator(minorLocator)
        axarr[i][0].plot(hueBins[i + 1])
        # For hues, the mean calculation needs to take into account the circular nature of the hue scale
        # First, determine the minimum point in the data and shift the array to start at that point
        # Then calculate the mean and stdev, and shift the mean result back to the original scale
        hueMin = np.argmin(hueBins[i + 1][1:]) + 1  # Ignore index 0, since this has been set to 0 above
        print("Color %i hueMin = %i" % (i + 1, hueMin))
        shiftedHues = np.concatenate((hueBins[i + 1][hueMin:], hueBins[i + 1][0:hueMin]))
        shiftedMean = sum(shiftedHues * np.arange(0, 256)) / sum(shiftedHues)
        print("Color %i hueMin = %i -> shifted mean = %.2f" % (i + 1, hueMin, shiftedMean))
        mean = (shiftedMean + hueMin) % 256
        stdev = math.sqrt(sum((np.arange(0, 256) - shiftedMean) * (np.arange(0, 256) - shiftedMean) * shiftedHues) / sum(shiftedHues))
        axarr[i][0].set_title("Color %i hue (mean = %.2f, stdev = %.2f)" % (i + 1, mean, stdev))
        
        axarr[i][1].xaxis.set_major_locator(majorLocator)
        axarr[i][1].xaxis.set_minor_locator(minorLocator)
        axarr[i][1].plot(saturationBins[i + 1])
        mean = sum(saturationBins[i + 1] * np.arange(0, 256)) / sum(saturationBins[i + 1])
        stdev = math.sqrt(sum((np.arange(0, 256) - mean) * (np.arange(0, 256) - mean) * saturationBins[i + 1]) / sum(saturationBins[i + 1]))
        axarr[i][1].set_title("Color %i saturation (mean = %.2f, stdev = %.2f)" % (i + 1, mean, stdev))
        
        axarr[i][2].xaxis.set_major_locator(majorLocator)
        axarr[i][2].xaxis.set_minor_locator(minorLocator)
        axarr[i][2].plot(valueBins[i + 1])
        mean = sum(valueBins[i + 1] * np.arange(0, 256)) / sum(valueBins[i + 1])
        stdev = math.sqrt(sum((np.arange(0, 256) - mean) * (np.arange(0, 256) - mean) * valueBins[i + 1]) / sum(valueBins[i + 1]))
        axarr[i][2].set_title("Color %i value (mean = %.2f, stdev = %.2f)" % (i + 1, mean, stdev))
    plt.show()


def saveStatistics(outputFileName, imageFileNames, optipos):
    """
    Creates a CSV file which contains the result of processing each of the input files.
    The columns are: file name, x-position, y-position, orientation, marker type, and quality factor.
    """
    with open(outputFileName, "w") as file:
        for fileName in imageFileNames:
            image = cv2.imread(os.path.normpath(fileName))
            result = optipos.processImage(image)
            if result:
                ((x, y), orientation, markerType, qualityFactor) = result
                file.write("%s, %f, %f, %f, %i, %f\n" % (os.path.normpath(fileName), x, y, orientation, markerType, qualityFactor))
            else:
                file.write("%s,,,,,\n" % (fileName,))

def showFieldMask(m, n):
    """
    Visualizes the location of field m in a marker with n fields per circle.
    """
    cv2.namedWindow("Field mask", cv2.WINDOW_AUTOSIZE)
    mc = MarkerCandidate(40, CircleCandidate(40, 50, 20, 1), CircleCandidate(40, 110, 20, 2))
    image = mc.fieldMask(m, n)
    cv2.imshow("Field mask", image)
    

def markerTypes(numberOfFieldsPerCircle, numberOfMarkerColors):
    """
    markerTypes returns the list of valid markers for given values on numberOfMarkerColors and numberOfFieldsPerCircle.
    """
    
    def markerType(colorList):
        return sum([numberOfMarkerColors ** (numberOfFieldsPerCircle * 2 - i - 1) * (colorList[i] - 1) 
                    for i in range(0, numberOfFieldsPerCircle * 2)])
    
    def markerTypeBuilder(prefix):
#        print("markerTypeBuilder(" + str(prefix) + ")")
        if len(prefix) >= numberOfFieldsPerCircle * 2:
#            print("%i: markerType(%s) = %i; markerType(%s) = %i" % (markerType(prefix), str(prefix), markerType(prefix), str(list(reversed(prefix))), markerType(list(reversed(prefix))))) 
            if markerType(prefix) < markerType(list(reversed(prefix))):
                return [[markerType(prefix), 0] + prefix]
            else:
                return []
        else:
            return sum([markerTypeBuilder(prefix + [i]) for i in range(1, numberOfMarkerColors + 1)], [])
        
#    print("markerTypes(" + str(numberOfMarkerColors) + ", " + str(numberOfFieldsPerCircle) + ")")
    return [tuple(res) for res in markerTypeBuilder([])]
        

def createMarkerFile(fileName, markerSize, numberOfFieldsPerCircle, numberOfMarkerColors, colors):
    """
    Generates a postscript file containing all the possible markers as based on the parameters.
    colors is a list containing triples with the RGB values for each color.
    
    TODO:
    Call drawmarker with a vector of the color fields in addition to the marker type.
    """
    fileTemplate = """%%!PS-Adobe-3.0

%% This postscript file contains all valid markers for the Optipos algorithm, one per page.
%% To use different marker sizes, just change the value of the variable diameter below.
%% To print, open the ps-file in Ghostview, then select File > Convert, and choose pdfwrite.
%% This generates a pdf file, that can then be printed. (For some reason, Ghostview does not print in color.)
%% For printing, ensure that printer is set to color, no duplex, and no scaling.

%% Size of A4 paper is 595 x 842 pt, 1 pt = 0.352778 mm
%% setcmykcolor takes parameters cyan magenta yellow key

%% Setting parameters

%% Set the diameter in cm
/diameter %.1f def

%% Calculate the radius in mm
/radius diameter 0.2 div def 

%% Calculate the radius in pt
/radiuspt radius 0.352778 div def

%% Number of fields per circle
/nofieldspercircle %i def

%% Number of colors
/nocolors %i def

%% RGB values for the different colors
%% The length of this array should equal the value of nocolors
/colors %s def


%% Procedure for setting the field color
%% Assumes that the color value (0..2) is on the stack
/setfieldcolor {
/c exch def
/v colors c get def
v 0 get
v 1 get
v 2 get
setrgbcolor
} def



/drawmarker {

%% Read parameters from the stack
/fieldcolor exch def
/markertype exch def

%% Set border color to white
1.0 setgray

%% Do the first circle fields
/y 1.5 radiuspt mul 421 add def
%% Loop through the fields
1 1 nofieldspercircle {
/field exch def  %% Store the field number

%% Calculate start and end angles of the field
/startangle 90 field 360 nofieldspercircle idiv mul sub 360 mod def
/endangle startangle 360 nofieldspercircle idiv add 360 mod def

%% Draw the field
newpath
297 y moveto
297 y radiuspt startangle endangle arc
closepath
gsave
fieldcolor field get setfieldcolor
fill
grestore
0 setlinewidth
stroke
} for

%% Do the second circle fields
/y -1.5 radiuspt mul 421 add def
%% Loop through the fields
1 1 nofieldspercircle {
/field exch def  %% Store the field number

%% Calculate start and end angles of the field
/startangle -90 field 1 sub 360 nofieldspercircle idiv mul add 360 mod def
/endangle startangle 360 nofieldspercircle idiv add 360 mod def

%% Draw the field
newpath
297 y moveto
297 y radiuspt startangle endangle arc
closepath
gsave
fieldcolor nofieldspercircle field add get setfieldcolor
fill
grestore
0 setlinewidth
stroke
} for

%% Draw a thick black border
0.0 setgray
15 setlinewidth
/y 1.5 radiuspt mul 421 add def
297 y radiuspt 0 360 arc
stroke
/y -1.5 radiuspt mul 421 add def
297 y radiuspt 0 360 arc
stroke

%% Arrow
297 441 moveto
314 411 lineto
280 411 lineto
closepath
0.7 setgray
1 setlinewidth
stroke

%% Large text for marker type
/Times-Roman findfont
70 scalefont
setfont
500 750 moveto
0.0 setgray
markertype 3 string cvs show

showpage
} def %% End of drawmarker

%%%%BeginDocument

%%%%Pages: %i

%s

%%%%EndDocument
    """
    
    markers = markerTypes(numberOfFieldsPerCircle, numberOfMarkerColors)
    with open(fileName, "w") as file:
        file.write(fileTemplate % (markerSize * 100.0, numberOfFieldsPerCircle, numberOfMarkerColors,
                                   "".join(["["] + ["[%s %s %s]" % (str(c[0]), str(c[1]), str(c[2])) for c in colors] + ["]"]),
                                   len(markers),
                                   "".join(["%% Marker %i\n%%%%Page: %i %i\n%i %s drawmarker\n\n" % 
                                            (markers[i][0], i + 1, i + 1, markers[i][0], "[" + " ".join([str(c - 1) for c in markers[i][1:]]) + "]") 
                                            for i in range(0, len(markers))])))


def plotImageValueHistogram(image):
    """
    Plots a histogram of the values in an image, including a smooth curve.
    """
    if len(image.shape) == 3:
        # Convert to grayscale image
        image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    values = image.flatten()
    
    # Get the probability distribution function from the values using a kernel density estimator (KDE) which smooths the data
    pdf = gaussian_kde(values)
    x = np.linspace(0, 255, 256)
    smoothValues = pdf(x)
    
    plt.plot(x, smoothValues,"r") # distribution function
    plt.hist(values, 128, normed = 1, alpha = 0.3) # histogram
    plt.show()


def mostDistantMarkers(markerSet, numberOfFieldsPerCircle, usedMarkers):
    """
    Returns a list of the markers that are most distant from the markers in the markerSet.
    Assumes black and white images.
    """
    def dist(x, y):
        return min(sum([abs(x_i - y_i) for (x_i, y_i) in zip(x, y)]), 
                   sum([abs(x_i - y_i) for (x_i, y_i) in zip(x, y[::-1])]))
    
    validMarkers = [[1] + list(m[2:]) for m in markerTypes(numberOfFieldsPerCircle, 2)]
    validMarkers = [m for m in validMarkers if markerType(m) not in usedMarkers]
    markerSet = [markerFieldValues(numberOfFieldsPerCircle, m) for m in markerSet]
    maxDist = max([min([dist(x, y) for y in markerSet]) for x in validMarkers])
    bestMarkers = [markerType(x) for x in validMarkers if min([dist(x, y) for y in markerSet]) == maxDist]
    return (bestMarkers, maxDist)


def main():
#    print(mostDistantMarkers([22, 47], 3, [1, 3, 4, 6, 7, 9, 10, 14, 17, 19, 22, 25, 29, 35, 39, 43, 47, 55]))
#    exit(0)
#    showFieldMask(2, 2)

    # Visualization mode parameters are: view, toggleCircles, toggleMarkers, toggleText
    visualizationMode = (1, True, True, True)
    
    # Set up input file names
#    mapFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Maps/JakobsOfficeMap.json"
    mapFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Maps/SSECorridorMap.json"
    settingsFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Settings/b827eb31395c.json"
#    settingsFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Settings/b827eba6c2dc.json"
#    imageFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Images/160201/b827eb31395c-2016-02-01T172342.616454.jpg"
    imageFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Images/170317/b827ebc55559-2017-03-17T072222.086857.jpg"
#    imageFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Images/b827eba6c2dc-2015-12-10T171122.917748.jpg"

    # Output file name for statistics
    statisticsFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/statistics.csv"
    # Output file name for generated marker files
    markerFileName = "C:/Users/Jakob Axelsson/Documents/Arbetsdokument/Eclipse workspace/OptiposCloud/Maps/markers.ps"
    
    # Load map and settings and create Optipos object  
    m = MarkerMap()
    m.load(os.path.normpath(mapFileName))
    s = Settings()
    s.load(os.path.normpath(settingsFileName))
#    print("markerMap.hue = " + str(m.hue))
#    print("markerMap.saturation = " + str(m.saturation))
#    print("markerMap.value = " + str(m.value))
    optipos = OptiposExtended(s, m)
    
#    createMarkerFile(os.path.normpath(markerFileName), optipos.markerMap.markerSize, optipos.markerMap.numberOfFieldsPerCircle, optipos.markerMap.numberOfMarkerColors, 
#                     [(0.5, 1, 0), (0, 1, 1), (1, 0, 0.64)])
#    createMarkerFile(os.path.normpath(markerFileName), optipos.markerMap.markerSize, 3, 2, [(1, 1, 1), (0, 0, 0)])
    
    # If image file indicates a directory, iterate over the files, and otherwise just process the given file
    if (os.path.isdir(imageFileName)):
        imageFileNames = [os.path.join(imageFileName, f) for f in os.listdir(imageFileName) if os.path.isfile(os.path.join(imageFileName, f))]
    else:
        imageFileNames = [imageFileName]

#    saveStatistics(statisticsFileName, imageFileNames, optipos)

#    histogramData = [f for f in imageFileNames if os.path.normpath(f) not in [os.path.normpath(p) for (p, _) in problemFiles]]
    """
    histogramData = imageFileNames
    print(len(histogramData))
    createColorHistogram(optipos, histogramData)
    """
    
    print("settings.pixelsPerMeter = " + str(s.pixelsPerMeter))
    print("settings.markerSizePixels = " + str(s.markerSizePixels))
    for fileName in imageFileNames:
#    for fileName in [f for (f, _) in problemFiles]:
        print("\n\n-----------------------------------------------------------------\n\n")
        print("Reading " + fileName)
        image = cv2.imread(os.path.normpath(fileName))
        timeStamp = datetime.datetime.strptime(os.path.basename(fileName)[13:], "%Y-%m-%dT%H%M%S.%f.jpg")
        result = optipos.processImage(image, timeStamp)
        print("Circles = " + str([(c.x, c.y, c.radius, c.rank) for c in optipos.circles]))
        print("Marker candidates = " + str([(m.x, m.y, m.orientation, m.markerType, m.qualityFactor) for m in optipos.markerCandidates]))
        print(optipos.printResult(result))
# This is really slow for large images:
#        plotImageValueHistogram(image)
        visualizationMode = visualizeStep(30, visualizationMode, optipos, optipos.printResult(result))
    cv2.destroyAllWindows()


if __name__ == '__main__':
    main()