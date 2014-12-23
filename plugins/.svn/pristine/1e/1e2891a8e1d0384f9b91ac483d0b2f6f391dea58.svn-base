//============================================================================
// Name        : OptiposLib.cpp
// Author      : Jakob Axelsson
// Version     :
// Copyright   : 
// Description : Optical positioning system
//============================================================================

// Notes on setting up compilation under Windows with Mingw:
// - Add a user PATH variable (not a system PATH!) that includes paths to Mingw binaries and the Open CV bin directory containing libraries.
// - When instructing the linker about what libraries to include, do not use the full library name.
//   If the full name is libxxx.dll, just write xxx.


#include <iostream>
#include <fstream>
// TODO: time.h can be removed, unless timing measures are to be made.
#include <time.h>

#include <cv.h>
#include <highgui.h>

#include "OptiposLib.h"

using namespace std;
using namespace cv;


bool traceOutput = false;


// Class ParameterFile /////////////////////////////////////////////////////

void ParameterFile::readParameterDouble(string fileName, int lineNumber, string line, double& x, string name)

// readParameterDouble reads a double parameter x from a string representing a line in the parameter file.
// The file name, line number, and a string representing the parameter's name are used for error messages.

{
	if (sscanf(line.c_str(), "%lf", &x) != 1) {
		cerr << "Syntax error in " << fileName << ", line " << lineNumber << ", expecting double for " << name << endl;
		exit(1);
	}
	if (traceOutput)
		cout << "Read " << name << ": " << x << endl;
}


void ParameterFile::readParameterInt(string fileName, int lineNumber, string line, int& x, string name)

// readParameterInt reads an int parameter x from a string representing a line in the parameter file.
// The file name, line number, and a string representing the parameter's name are used for error messages.

{
	if (sscanf(line.c_str(), "%d", &x) != 1) {
		cerr << "Syntax error in " << fileName << ", line " << lineNumber << ", expecting int for " << name << endl;
		exit(1);
	}
	if (traceOutput)
		cout << "Read " << name << ": " << x << endl;
}


void ParameterFile::readFile(String fileName)
//	Reads the file, which it assumes to have the following syntax:
//	- Every line that starts with // is treated as a comment line, and is skipped.
//	- Blank lines are allowed anywhere.
// - All other lines are parsed through a call to readContent, which is implemented in subclasses
{
	if (traceOutput)
		cout << "*** Entering readFile ***" << endl;

	int lineNumber = 1;
	string line;

	ifstream file(fileName.c_str());
	if (file.is_open()) {
		while (getline(file,line)) {
			if (traceOutput)
				cout << lineNumber << ": " << line << '\n';
			if (line.empty()) {
				// Skip empty line
			}
			else if ((line.length() >= 2) && (line[0] == '/') && (line[1] == '/')) {
				// Skip comment line
			}
			// Else, parse the line as implemented in each subclass of ParameterFile
			else
				readContent(fileName, lineNumber, line);
			lineNumber++;
		}
		if (!done()) {
			cerr << "Unexpected end of file " << fileName << " at line " << lineNumber << endl;
			exit(1);
		}
		file.close();
	}
	else {
		cerr << "Unable to open file" << fileName << endl;
		exit(1);
	}

	if (traceOutput)
		cout << "*** Leaving readFile ***" << endl;
}


// Class MarkerMap /////////////////////////////////////////////////////

MarkerMap::MarkerMap()
{
	ceilingHeight = 2.40;
	markerSize = 0.08;
	step = 0;
}


void MarkerMap::readContent(string fileName, int lineNumber, string line)
// The map file is assumed to contain the following data:
// - Line 1: Float number indicating ceiling height (in meters)
// - Line 2: Float number indicating marker size (in meters)
// - Line 3..n: Defines a marker, by giving x-position (float), y-position (float), and type (int) with spaces inbetween.
{
	if (step == 0) { // Read ceiling height
		readParameterDouble(fileName, lineNumber, line, ceilingHeight, "ceiling height");
		step++;
	}
	else if (step == 1) { // Read marker size
		readParameterDouble(fileName, lineNumber, line, markerSize, "marker size");
		step++;
	}
	else if (step > 1) { // Read marker
		double xPos, yPos, orientation;
		int markerType;
		if (sscanf(line.c_str(), "%lf %lf %lf %d", &xPos, &yPos, &orientation, &markerType) != 4) {
			cerr << "Syntax error in " << fileName << ", line " << lineNumber << ", expecting double double int for marker x, y, and type" << endl;
			exit(1);
		}
		if (traceOutput)
			cout << "Read marker: x = " << xPos << ", y = " << yPos
			<< ", orientation = " << orientation << ", type = " << markerType << endl;
		x.push_back(xPos);
		y.push_back(yPos);
		o.push_back(orientation);
		t.push_back(markerType);
	}
}


bool MarkerMap::done()
{
	return (step >= 2);
}


// Class Settings /////////////////////////////////////////////////////

Settings::Settings()
{
	step = 0;
	color = 1;

	cameraFieldOfView = 0;
	cameraOffsetX = 0.0;
	cameraOffsetY = 0.0;
	cols = 0;
	rows = 0;
	pixelsPerMeter = 0.0;
	markerSizePixels = 0;
	cannyThreshold1 = 20.0;
	cannyThreshold2 = 60.0;
	cannyKernelSize = 3;
	findMarkersThreshold = 0.0;
	selectAndRankMarkersThreshold = 0.0;
}


void Settings::setImageSize(int w, int h, MarkerMap map)
// setImageSize is used to capture the size of the image. It also calculates the size of a marker.
{
	cols = w;
	rows = h;
	pixelsPerMeter = rows / (2.0 * map.ceilingHeight * tan(((double) cameraFieldOfView) / 180.0 * CV_PI / 2.0));
	markerSizePixels = round(pixelsPerMeter * map.markerSize);
}


void Settings::initHueTable()
// Initializes the hueTable with proper values depending on the hue as specified in the settings file.
// The table is used when filtering the image, and maps each hue in the spectrum to a color 0..3.
{
	for (int hue = 0; hue < 180; hue++)
		hueTable[hue] = 0;

	for (int hue = 0; hue < 360; hue++) {
		for (uchar color = 1; color < 4; color++)
			if (hueMin[color] <= hue && hue <= hueMax[color])
				hueTable[hue % 180] = color;
	}

	if (traceOutput) {
		cout << "Hue table: " << endl;
		for (int hue = 0; hue < 180; hue++)
			cout << (int) hueTable[hue] << " ";
		cout << endl;
	}
}


void Settings::readContent(string fileName, int lineNumber, string line)
{
	switch (step) {
		case 0:  // Camera field of view
			readParameterInt(fileName, lineNumber, line, cameraFieldOfView, "camera field of view");
			break;
		case 1:  // Camera offset x
			readParameterDouble(fileName, lineNumber, line, cameraOffsetX, "camera offset x");
			break;
		case 2:  // Camera offset y
			readParameterDouble(fileName, lineNumber, line, cameraOffsetY, "camera offset y");
			break;
		case 3: // Read color 1, consisting of hue min, hue max, sat min, sat max, val min, val max on a single line
		case 4: // Read color 2
		case 5: // Read color 3
			if (sscanf(line.c_str(), "%d %d %d %d %d %d", &hueMin[color], &hueMax[color], &saturationMin[color], &saturationMax[color], &valueMin[color], &valueMax[color]) != 6) {
				cerr << "Syntax error in " << fileName << ", line " << lineNumber << ", expecting 6 int values for color settings" << endl;
				exit(1);
			}
			// Change settings to open cv values. Note that hues are in the range [0..180] in open cv!
			hueMin[color] = hueMin[color] / 2;
			hueMax[color] = hueMax[color] / 2;
			saturationMin[color] = saturationMin[color] * 255 / 100;
			saturationMax[color] = saturationMax[color] * 255 / 100;
			valueMin[color] = valueMin[color] * 255 / 100;
			valueMax[color] = valueMax[color] * 255 / 100;
			if (traceOutput)
				cout << "Read color " << color << " (after conversion): " << hueMin[color] << ", " << hueMax[color] << ", " << saturationMin[color] << ", " << saturationMax[color] << ", " << valueMin[color] << ", " << valueMax[color] << endl;
			color++;
			break;
		case 6:
			// Since all colors are now read, the hue table can be initialized.
			initHueTable();
			// Read Canny parameters
			if (sscanf(line.c_str(), "%lf %lf %d", &cannyThreshold1, &cannyThreshold2, &cannyKernelSize) != 3) {
				cerr << "Syntax error in " << fileName << ", line " << lineNumber << ", expecting double double int values for Canny settings" << endl;
				exit(1);
			}
			break;
		case 7:
			readParameterDouble(fileName, lineNumber, line, findMarkersThreshold, "findMarkersThreshold");
			break;
		case 8:
			readParameterDouble(fileName, lineNumber, line, selectAndRankMarkersThreshold, "selectAndRankMarkersThreshold");
			break;
	}
	step++;
}


bool Settings::done()
{
	return (step >= 9);
}


// The following functions provide O(1) calculation of sin and cos for given an angle in degrees.
// The table has to be initialized before any of the functions are called.

double sinTable[360];

void initSinTable()
{
	for (int i = 0; i < 360; i++)
		sinTable[i] = sin(i * CV_PI / 180.0);
}

double sinDegrees(int angle) {
	return sinTable[(360 + angle) % 360];
}

double cosDegrees(int angle) {
	return sinTable[(360 + 90 - angle) % 360];
}


Point rotate(Point pos, int angle, Point delta)
// Calculates the position of pos in a coordinate system which is a translation by delta and a rotation of angle (in degrees).
{
	return Point(
			round(pos.x * cosDegrees(angle) - pos.y * sinDegrees(angle) + delta.x),
			round(pos.x * sinDegrees(angle) + pos.y * cosDegrees(angle) + delta.y));
}


bool arrayIndexOk(int i, int max)
// The arrayIndexOk function checks that the index i is ok for an array with max elements
{
	return ((0 <= i) && (i < max));
}


// Class CircleCandidate /////////////////////////////////////////////////////

CircleCandidate::CircleCandidate(int xPos = 0, int yPos = 0, int v = 0)
{
	x = xPos;
	y = yPos;
	votes = v;
}


MarkerCandidate::MarkerCandidate(int xPos = 0, int yPos = 0)
{
	x = xPos;
	y = yPos;
	orientation = 0;
	for (int i = 0; i < 5; i++)
		for (int j = 0; j < 4; j++)
			fieldColors[i][j] = 0;
	qualityFactor = 0.0;
	markerType = 0;
}


void addCircleCandidate(vector<CircleCandidate>& lst, CircleCandidate c, int markerSizePixels)
{
	// When adding a candidate, ensure that there are no other candidate within an x, y-distance of
	// circle size in pixels, so that only local maxima are in the list of candidates.

	for (int i = lst.size() - 1; i >= 0; i--) {
		if (c.y - lst[i].y > markerSizePixels)
			// The remaining circles are not close to this one, so a new one should be added.
			break;
		if (abs(c.x - lst[i].x) <= markerSizePixels) {
			// The new circle is close to another one
			if (c.votes > lst[i].votes) {
				// The new circle has more votes than the existing, so replace it
				if (traceOutput)
					cout << "Replace circle x = " << lst[i].x << ", y = " << lst[i].y
						<< " by circle x = " << c.x << ", y = " << c.y << ", votes = " << c.votes << ". count = " << lst.size() << endl;
				lst[i] = c;
			}
			return;
		}
	}
	// No candidate close to the new one exists, so add the new one.
	lst.push_back(c);
	if (traceOutput)
		cout << "Add circle x = " << c.x << ", y = " << c.y << ", votes = " << c.votes << ". count = " << lst.size() << endl;
}


void filterHSV(Settings settings, Mat& originalImage, Mat_<uchar>& filteredHueMatrix)

// The filterHSV filters an image for yellow, cyan, and magenta colors, based on constant parameters defining these colors.
// It also filters pixels with a saturation and value within a certain range.
// The result is the a matrix with range 0..3 indicating background (0), yellow (1), cyan (2), magenta (3).
// Note that OpenCV maps hues to the range [0..180[.

{
	if (traceOutput)
		cout << "*** Entering filterHueSaturation ***" << endl;

	Mat hsvImage;
    cvtColor(originalImage, hsvImage, CV_BGR2HSV);
	vector<cv::Mat> hsvChannels;
    split(hsvImage, hsvChannels);
	Mat_<uchar> hue, saturation, value;
    hue = hsvChannels[0];
    saturation = hsvChannels[1];
    value = hsvChannels[2];
	filteredHueMatrix = Mat::zeros(originalImage.size(), CV_8U);

    // These are pointers to rows in the hue, saturation and filteredHueMatrix matrices.
	uchar* hptr;
	uchar* sptr;
	uchar* vptr;
	uchar* fptr;
	for (int y = 0; y < originalImage.rows; ++y) {
		hptr = hue.ptr<uchar>(y);
		sptr = saturation.ptr<uchar>(y);
		vptr = value.ptr<uchar>(y);
		fptr = filteredHueMatrix.ptr<uchar>(y);
		for (int x = 0; x < originalImage.cols; ++x) {
			int hue = settings.hueTable[hptr[x]];
			if (settings.saturationMin[hue] <= sptr[x] &&
					sptr[x] <= settings.saturationMax[hue] &&
					settings.valueMin[hue] <= vptr[x] &&
					vptr[x] <= settings.valueMax[hue])
				fptr[x] = hue;
		}
	}

	if (traceOutput)
		cout << "*** Leaving filterHueSaturation ***" << endl;
}


// Class CircleAccumulator /////////////////////////////////////////////////////

void CircleAccumulator::addCircle(int cx, int cy)
// addCircle adds a circle to an accumulator array using a predefined matrix containing the circle pixels
{
	ushort* mptr; // This is a pointer to a row in the matrix m.
	for (unsigned int i = 0; i < circleStamp.size(); i++) {
		if (arrayIndexOk(cy + circleStamp[i].y, m.rows)) {
			mptr = m.ptr<ushort>(cy + circleStamp[i].y);
			if (arrayIndexOk(cx + circleStamp[i].x, m.cols))
				mptr[cx + circleStamp[i].x]++;
		}
	}
}


//static
void CircleAccumulator::initCircleStamp(int r)
// Initializes a circle stamp with radius r, and its center in origo.
// Algorithm from http://webstaff.itn.liu.se/~stegu/circle/circlealgorithm.pdf as implemented in (same directory)/Circles.java.
{
	if (traceOutput)
		cout << "CircleAccumulator::initCircleStamp(" << r << ") called" << endl;
	circleStamp.clear();
	markerRadius = r;
	int x = 0;
	int y = r;
	int d = 5 - (r<<2);
	while (x <= y) {
		// Add pixels in all eight octants
		circleStamp.push_back(Point( y,  x));  // Second octant
		circleStamp.push_back(Point(-y,  x));  // Seventh octant
		circleStamp.push_back(Point( y, -x));  // Third octant
		circleStamp.push_back(Point(-y, -x));  // Sixth octant
		circleStamp.push_back(Point( x,  y));  // First octant
		circleStamp.push_back(Point(-x,  y));  // Eighth octant
		circleStamp.push_back(Point( x, -y));  // Fourth octant
		circleStamp.push_back(Point(-x, -y));  // Fifth octant
		x++; // Always take one step to the right
		if (d > 0) {
			d += (x<<3) - (y<<3) + 12;
			y--; // Also take one step down
		}
		else
			d += (x<<3) + 4;
	}
}


void CircleAccumulator::buildCircleAccumulator(Mat& edgeImage)
{
	// First, ensure that the accumulator has the right size and is empty
	m = Mat::zeros(edgeImage.rows, edgeImage.cols, CV_16U);

	// Draw a circle of radius markerSizePixels / 2 in the accumulator, centered in the coordinates of each edge pixel in the image
	// thereby voting for all pixels on the perimeter of that circle as potential centers.

	uchar* ptr; // This is a pointer to a row in the edgeImage matrix.
	for (int y = 0; y < edgeImage.rows; ++y) {
		ptr = edgeImage.ptr<uchar>(y);
		for (int x = 0; x < edgeImage.cols; ++x) {
			if (ptr[x] != 0) // Is it an edge pixel?
				addCircle(x, y);
		}
	}
}


void CircleAccumulator::buildCircleCandidateSet(vector<CircleCandidate>& lst)
{
	// Build the set of candidates
	double maxCount = 0;
	minMaxLoc(m, NULL, &maxCount, NULL, NULL);
	int houghTreshold = round(maxCount * 0.5);

	ushort* aptr; // This is a pointer to a row in the accumulator matrix.

	for (int y = 0; y < m.rows; ++y) {
		aptr = m.ptr<ushort>(y);
		for (int x = 0; x < m.cols; ++x) {
			if (aptr[x] > houghTreshold) {
				CircleCandidate cc(x, y, aptr[x]);
				addCircleCandidate(lst, cc, markerRadius * 2);
			}
		}
	}
}


int CircleAccumulator::maxValue()
// maxValue returns the largest value in the accumulator matrix.
{
	int maxVal = 0;
	ushort* aptr; // This is a pointer to a row in the accumulator matrix.
	for (int y = 0; y < m.rows; ++y) {
		aptr = m.ptr<ushort>(y);
		for (int x = 0; x < m.cols; ++x) {
			if (aptr[x] > maxVal)
				maxVal = aptr[x];
		}
	}
	return maxVal;
}


void CircleAccumulator::image(Mat& accumulatorImageColor)
// image returns an image of the accumulator
{
	if (traceOutput)
		cout << "*** Entering CircleAccumulator::image ***" << endl;

	int maxVal = maxValue();
	Mat acc = m * 256 / maxVal;
	Mat accumulatorImage;
	acc.convertTo(accumulatorImage, CV_8U);
	cvtColor(accumulatorImage, accumulatorImageColor, CV_GRAY2RGB);

	if (traceOutput)
		cout << "*** Leaving CircleAccumulator::image ***" << endl;
}


// Define static member variables of CircleAccumulator
vector<Point>CircleAccumulator::circleStamp;
int CircleAccumulator::markerRadius = 0;


void findMarkers(Settings settings, vector<CircleCandidate>& circles, vector<MarkerCandidate>& lst)

// The findMarkers function searches a vector of circles trying to find pairs of circles whose centers are at a distance 1.5 * markerSizePixels.
// For each such pair, a quality metric is calculated which is based on the the quality of each circle and the distance between them.
// Pairs with high enough quality are included in the resulting vector of marker candidates.

// TODO: This function can be made smarter, since the quality drops quickly with distance. Thus, it can stop looking at potential pairing once the
// y distance between them becomes too large (given that the list of circles is sorted).
// Also, what is the motivation for the treshold quality > 3? Make it a parameter!
{
	if (traceOutput)
		cout << "*** Entering findMarkers ***" << endl;
	for (unsigned int i = 0; i < circles.size(); i++) {
		for (unsigned int j = i + 1; j < circles.size(); j++) {
			double dx = circles[i].x - circles[j].x;
			double dy = circles[i].y - circles[j].y;
			double distance = sqrt(dx * dx + dy * dy);
			int quality = round((circles[i].votes + circles[j].votes) / (1 + pow(distance - settings.markerSizePixels * 1.5, 2.0)));
			if (quality > settings.findMarkersThreshold) {
				MarkerCandidate m((circles[i].x + circles[j].x) / 2, (circles[i].y + circles[j].y) / 2);
				if (dy == 0.0)
					m.orientation = 90;
				else
					m.orientation = -round(180.0 / CV_PI * atan(dx / dy));  // Orientation in degrees
				lst.push_back(m);
			}
		}
	}

	if (traceOutput)
		cout << "Input is " << circles.size() << " circles, output is " << lst.size() << " marker candidates" << endl;

	if (traceOutput)
		cout << "*** Leaving findMarkers ***" << endl;
}


void classifyMarker(int markerSizePixels, Mat_<uchar>& hueMatrix, MarkerCandidate* m)
// The classifyMarker function counts the number of pixels of each marker color that are within
// each of the four fields of the identified marker. Based on this, it classifies the marker,
// and determines a quality factor for the identification. It also calculates marker orientation.
{
	if (traceOutput)
		cout << "*** Entering classifyMarker ***" << endl;

	// Count the number of pixels in each quadrant of each of the four colors.
	for (int y = -3 * markerSizePixels; y < 3 * markerSizePixels; y++) {
		for (int x = -3 * markerSizePixels / 2; x < 3 * markerSizePixels / 2; x++) {
		// Determine the field of the current pixel
			int field = 0;
			if (sqrt(x * x + (y + 0.75 * markerSizePixels) * (y + 0.75 * markerSizePixels)) < markerSizePixels / 2
					&& y < -0.75 * markerSizePixels)
				field = 1;
			else if (sqrt(x * x + (y + 0.75 * markerSizePixels) * (y + 0.75 * markerSizePixels)) < markerSizePixels / 2
					&& y > -0.75 * markerSizePixels)
				field = 2;
			else if (sqrt(x * x + (y - 0.75 * markerSizePixels) * (y - 0.75 * markerSizePixels)) < markerSizePixels / 2
					&& y < 0.75 * markerSizePixels)
				field = 3;
			else if (sqrt(x * x + (y - 0.75 * markerSizePixels) * (y - 0.75 * markerSizePixels)) < markerSizePixels / 2
					&& y > 0.75 * markerSizePixels)
				field = 4;

		// Determine the color of that field
			Point rot = rotate(Point(x, y), m->orientation, Point(m->x, m->y));
			int color = 0;
			if (arrayIndexOk(rot.x, hueMatrix.cols) && arrayIndexOk(rot.y, hueMatrix.rows)) {
				color = hueMatrix.at<uchar>(rot);
			}
			m->fieldColors[field][color]++;
		}
	}

	if (traceOutput) {
		for (int i = 0; i < 5; i++) {
			cout << "Field " << i << " colors:";
			for (int j = 0; j < 4; j++)
				cout << " " << m->fieldColors[i][j];
			cout << endl;
		}
	}


	// Based on the colors, calculate a probability that each marker type is seen, and select the one with the highest score (which is also the quality factor).

	double maxScore = 0.0;
	int maxType = 0;

	for (int i = 1; i < 4; i++)
		for (int j = 1; j < 4; j++)
			for (int k = 1; k < 4; k++)
				for (int n = 1; n < 4; n++) {
					double score = 1.0;
					score = score * m->fieldColors[1][i] / (m->fieldColors[1][1] + m->fieldColors[1][2] + m->fieldColors[1][3]);
					score = score * m->fieldColors[2][j] / (m->fieldColors[2][1] + m->fieldColors[2][2] + m->fieldColors[2][3]);
					score = score * m->fieldColors[3][k] / (m->fieldColors[3][1] + m->fieldColors[3][2] + m->fieldColors[3][3]);
					score = score * m->fieldColors[4][n] / (m->fieldColors[4][1] + m->fieldColors[4][2] + m->fieldColors[4][3]);

					if (score > maxScore) {
						int markerType1 = (i - 1) * 27 + (j - 1) * 9 + (k - 1) * 3 + (n - 1);
						int markerType2 = (n - 1) * 27 + (k - 1) * 9 + (j - 1) * 3 + (i - 1);
						if (markerType1 < markerType2) {
							maxType = markerType1;
							maxScore = score;
						}
						else if (markerType1 > markerType2) {
							maxType = -markerType2;
							maxScore = score;
						}
						// The case markerType1 == markerType2 represents a symmetric marker, which is not allowed.
					}
				}
	// The quality factor represents the score of the most likely marker, weighted with the amount of background pixels in the background area
	m->qualityFactor = min(maxScore, 1.0) * m->fieldColors[0][0] / (m->fieldColors[0][0] + m->fieldColors[0][1] + m->fieldColors[0][2] + m->fieldColors[0][3]);
	if (maxType < 0) {
		m->orientation = (m->orientation + 180) % 360;
		m->markerType = - maxType;
	}
	else
		m->markerType = maxType;

	if (traceOutput)
		cout << "*** Leaving classifyMarker ***" << endl;
}


void classifyMarkerList(int markerSizePixels, Mat_<uchar> hueMatrix, vector<MarkerCandidate>& lst)
// classifyMarkerList applies classifyMarker to each marker candidate in the list lst
{
	for (unsigned int i = 0; i < lst.size(); i++) {
		if (traceOutput)
			cout << endl << endl << "Analyzing candidate " << i + 1 << "(" << lst.size() << ")" << endl;
		MarkerCandidate* m = &lst[i];
		if (traceOutput)
			cout << "Hough marker center point (pixels) = (" << m->x << ", " << m->y << ")" << endl;

		// Classify the marker, while updating the orientation and calculating quality factors
		classifyMarker(markerSizePixels, hueMatrix, m);
		if (traceOutput)
			cout
				<< "Marker type = " << m->markerType << endl
				<< "Quality factor = " << m->qualityFactor << endl
				<< "Marker final orientation = " << m->orientation << endl
				<< "Final marker center point (pixels) = (" << m->x << ", " << m->y << ")" << endl;
	}
}


void selectAndRankValidMarkers(Settings settings, vector<MarkerCandidate>& lst)
// selectAndRankValidMarkers first filters out marker candidates that do not represent valid markers.
// Next, it sorts markers in order of decreasing quality.
// Finally, if there are markers of the same type, only the one with the highest quality is kept.
{
	if (traceOutput)
		cout << "*** Entering selectAndRankValidMarkers ***" << endl;

	// Select only those candidates that represent valid markers

	if (traceOutput)
		cout << "Input was " << lst.size() << " markers" << endl;
	for (unsigned int i = 0; i < lst.size();) {
		if ((lst[i].markerType == 0) || (lst[i].qualityFactor < settings.selectAndRankMarkersThreshold)) {
			// If this condition is true, and a marker is removed, i should not be increased,
			// since there will be a new marker at i after removal.
			if (traceOutput)
				cout << "Remove marker at x = " << lst[i].x << ", y = " << lst[i].y
						<< " with market type = " << lst[i].markerType
						<< " and  qualityFactor= " << lst[i].qualityFactor << endl;
			lst.erase(lst.begin() + i);
		}
		else
			i++;
	}
	if (traceOutput)
		cout << "After selection, " << lst.size() << " markers remain" << endl;

	// Order the markers (using bubble sort) so that the best marker is first in the list.
	// Base the comparison on the quality factor.

	bool swapped = true;
	unsigned int j = 0;
	while (swapped) {
		swapped = false;
		j++;
		// The for loop below is equivalent to "for (unsigned int i = 0; i < lst.size() - j; i++)",
		// except that for unsigned int's, lst.size() - j would become negative.
		for (unsigned int i = 0; i + j < lst.size(); i++) {
			if (lst[i].qualityFactor < lst[i+1].qualityFactor) {
				MarkerCandidate m = lst[i];
				lst[i] = lst[i+1];
				lst[i+1] = m;
				swapped = true;
			}
		}
    }

	// If there are several markers of the same type, only keep the one with highest quality

	bool m[81];
	for (int i = 0; i < 81; i++)
		m[i] = false;

	for (unsigned int i = 0; i < lst.size(); ) {
		if (!m[lst[i].markerType]) {
			m[lst[i].markerType] = true;
			i++;
		}
		else
			lst.erase(lst.begin()+i);
	}

	if (traceOutput)
		cout << "*** Leaving selectAndRankValidMarkers ***" << endl;
}


// Class Optipos /////////////////////////////////////////////////////

Optipos::Optipos(Settings& s, MarkerMap& m)
{
	initSinTable();
	map = m;
	settings = s;
	// Set up the accumulator
	CircleAccumulator::initCircleStamp(settings.markerSizePixels / 2);

	previousPosition = Point(0.0, 0.0);
}


void Optipos::processImage(Mat& originalImage, vector<MarkerCandidate>& lst)
// processImage is the main method of the image processing. It has the following steps:
// - Apply gaussian blur to the image to reduce noise.
// - Classify each image pixel as one of the three marker colors, or background, based on its hue, saturation, and value.
// - Identify the edges of that image.
// - Identify circles in the edge image.
// - Find pairs of circles at suitable distances to form marker candidates.
// - Classify the marker candidates according to their type and quality.
// - Select suitable candidates and rank them.
{

	if ((settings.cols != originalImage.cols) || (settings.rows != originalImage.rows)) {
		settings.setImageSize(originalImage.cols, originalImage.rows, map);
		CircleAccumulator::initCircleStamp(settings.markerSizePixels / 2);
	}

	if (traceOutput)
	{
		cout << endl << endl << endl << endl << endl << "Image size = (";
		cout << originalImage.cols << ", " << originalImage.rows << ")" << endl;
		cout << "Marker size in pixels = " << settings.markerSizePixels << endl;
	}

	// Clear the vector of previous results, and reserve initial space
	lst.clear();
	lst.reserve(1000);

	// Apply noise reduction based on Gaussian blur, using a 5x5 kernel.
	GaussianBlur(originalImage, blurredImage, Size(5, 5), 0, 0);

	// Filter Hue, Saturation, Value
	filterHSV(settings, blurredImage, filteredHueMatrix);

	// Edge detection using Canny algorithm
	threshold(filteredHueMatrix, cannyImage, 0, 255, 0);
	Canny(cannyImage, cannyImage, settings.cannyThreshold1, settings.cannyThreshold2, settings.cannyKernelSize);

	// Hough algorithm for circle detection
	circles.clear();
	circles.reserve(1000);
	accumulator.buildCircleAccumulator(cannyImage);
	accumulator.buildCircleCandidateSet(circles);

/*
// **** Alternative approach, using contour analysis.
	// Based on C:\OpenCV249PC\opencv\sources\samples\cpp\fitellipse.cpp.
	circles.clear();
	circles.reserve(1000);
	vector<vector<Point> > contours;
	Mat contourImage;
	cannyImage.copyTo(contourImage);
	findContours(contourImage, contours, CV_RETR_LIST, CV_CHAIN_APPROX_NONE);
	if (traceOutput)
		cout << "Found " << contours.size() << " contours" << endl;
	for(size_t i = 0; i < contours.size(); i++) {
		if (contours[i].size() >= 5) { // If it has less than 5 points, it cannot be fitted to a circle using fitEllipse
			Mat contourPoints;
			Mat(contours[i]).convertTo(contourPoints, CV_32F);
			RotatedRect box = fitEllipse(contourPoints);
			if (traceOutput)
				cout << "Contour " << i << ": " << contours[i].size() << " points, "
					 << " width = " << box.size.width << ", height = " << box.size.height << ", x = " << box.center.x
					 << ", y = " << box.center.y << ", angle = " << box.angle << endl;

			// Check the size of the ellipse, to see if it can be a circle candidate
			if ((abs(box.size.width - settings.markerSizePixels) < 5) && (abs(box.size.height - settings.markerSizePixels) < 5)) {
				// TODO: Change 100 to some quality metric!
				// Is it possible to determine how good the fit was from fitEllipse? Maybe modify fitEllipse source code?
				CircleCandidate cc(box.center.x, box.center.y, 100);
				circles.push_back(cc);
			}
		}
	}
// **** End of alternative approach.
*/

	findMarkers(settings, circles, lst);

	classifyMarkerList(settings.markerSizePixels, filteredHueMatrix, lst);

	selectAndRankValidMarkers(settings, lst);
}


void Optipos::calculatePosition(vector<MarkerCandidate>& lst, Point_<double>& newPos, int& orientation)
// calculatePosition calculates the global position indicated by a set of marker candidates on a given map,
// taking the previous position into account. It also calculates the camera orientation.
// The current implementation only looks at the best marker, and matches that against the marker on the map
// with the same type, which is closest to the previous position.

// TODO: A more elaborate procedure could look at all identified markers in the image, and try to combine them
// by finding a region on the map where all of them should be visible.
{
	if (lst.size() != 0) {
		// Find the appropriate marker on the map.
		MarkerCandidate* m = &lst[0];
		int best = -1;
		double dist = INFINITY;
		for (unsigned int i = 0; i < map.x.size(); i++) {
			if (m->markerType == map.t[i]) {
				int dx = map.x[i] - previousPosition.x;
				int dy = map.y[i] - previousPosition.y;
				double d = sqrt(dx * dx + dy * dy);
				if (d < dist) {
					dist = d;
					best = i;
				}
			}
		}

		// Derive the global position of the marker.

		if (best != -1) {
			orientation = (m->orientation + 360) % 360;
			int deltaX = m->x - settings.cols  / 2 - round(settings.cameraOffsetX / settings.pixelsPerMeter);
			int deltaY = m->y - settings.rows / 2 - round(settings.cameraOffsetY / settings.pixelsPerMeter);
			Point cameraPositionPixels = rotate(Point(deltaX, deltaY), -orientation, Point(0,0));
			newPos = Point_<double>(
					((double) cameraPositionPixels.x) / ((double) settings.pixelsPerMeter) + map.x[best],
					((double) cameraPositionPixels.y) / ((double) settings.pixelsPerMeter) + map.y[best]);
			previousPosition = newPos;
			if (traceOutput)
				cout << "Camera position (meters) = (" << newPos.x << ", " << newPos.y << ")" << endl
					<< "Camera orientation (degrees) = " << orientation << endl;
		}
		else {
			newPos = Point_<double>(0.0, 0.0);
			orientation = 0;
		}
	}
}


void Optipos::getHSVImage(Mat& outputImage)
// drawHSVImage creates an image which shows the effects of the hue, saturation, and value filtering
// of the original image.
{
	Mat_<uchar> hue, saturation, value;
	hue = Mat::zeros(filteredHueMatrix.size(), CV_8U);
	saturation = Mat::zeros(filteredHueMatrix.size(), CV_8U);
	value = Mat::zeros(filteredHueMatrix.size(), CV_8U);
	for (int y = 1; y < filteredHueMatrix.rows - 1; y++)
		for (int x = 1; x < filteredHueMatrix.cols - 1; x++) {
			int color = filteredHueMatrix.at<uchar>(y, x);
			if (color == 0)
				value.at<uchar>(y, x) = 0;
			else {
				hue.at<uchar>(y, x) = (settings.hueMin[color] + settings.hueMax[color]) / 2;
				saturation.at<uchar>(y, x) = 255;
				value.at<uchar>(y, x) = 255;
			}
		}
	vector<cv::Mat> hsvChannels;
	hsvChannels.push_back(hue);
	hsvChannels.push_back(saturation);
	hsvChannels.push_back(value);
	merge(hsvChannels, outputImage);
	cvtColor(outputImage, outputImage, CV_HSV2BGR);
}


void Optipos::getCannyImage(Mat& outputImage)
{
	cannyImage.copyTo(outputImage);
}


void Optipos::getAccumulatorImage(Mat& outputImage)
{
	accumulator.image(outputImage);
}


void Optipos::overlayCirclesOnImage(Mat& originalImage, Mat& overlayImage)
{
	// Output the original image with the circles centers marked on it
	originalImage.copyTo(overlayImage);

	// Draw a cross at each candidate
	Scalar green = Scalar(0, 255, 0);  // Color to use for drawing
	for (unsigned int i = 0; i < circles.size(); i++) {
		line(overlayImage, Point(circles[i].x, circles[i].y - 10), Point(circles[i].x, circles[i].y + 10), green);
		line(overlayImage, Point(circles[i].x - 10, circles[i].y), Point(circles[i].x + 10, circles[i].y), green);
	}
}


void Optipos::overlayMarkersOnImage(Mat& originalImage, Mat& overlayImage, vector<MarkerCandidate>& lst, char result[80])
// overlayMarkersOnImage returns a copy where the original image is overlayed with
// graphics illustrating the markers as well as an optional text string at the bottom.
{
	// Output the original image with the marker drawn on it
	originalImage.copyTo(overlayImage);

	// Colors to use for drawing
	Scalar red = Scalar(0, 0, 255);
	Scalar green = Scalar(0, 255, 0);
	Scalar color = green;

	// Draw a cross at the center of the image
	int height = overlayImage.rows;
	int width = overlayImage.cols;
	line(overlayImage, Point(width/2, height/2 - 10), Point(width/2, height/2 + 10), green);
	line(overlayImage, Point(width/2 - 10, height/2), Point(width/2 + 10, height/2), green);

	for (int i = lst.size() - 1; i >= 0; i--) {
		// Draw the markers in reverse order, to ensure that the green marker is drawn on top, if there are duplicates.
		// Draw the marker outline

		if (i == 0)
			color = green;
		else
			color = red;

		MarkerCandidate m = lst[i];
		Point markerCenterPoint = Point(m.x, m.y);
		int theta = m.orientation;

		// Draw the outline
		Point p1, p2;

		p1 = rotate(Point(0, -settings.markerSizePixels * 3 / 4), theta, markerCenterPoint);
		circle(overlayImage, p1, settings.markerSizePixels / 2, color);

		p1 = rotate(Point(0, settings.markerSizePixels * 3 / 4), theta, markerCenterPoint);
		circle(overlayImage, p1, settings.markerSizePixels / 2, color);

		// Draw the fields

		p1 = rotate(Point(-settings.markerSizePixels / 2, -settings.markerSizePixels * 3 / 4), theta, markerCenterPoint);
		p2 = rotate(Point(settings.markerSizePixels / 2, -settings.markerSizePixels * 3 / 4), theta, markerCenterPoint);
		line(overlayImage, p1, p2, color);
		p1 = rotate(Point(-settings.markerSizePixels / 2, settings.markerSizePixels * 3 / 4), theta, markerCenterPoint);
		p2 = rotate(Point(settings.markerSizePixels / 2, settings.markerSizePixels * 3 / 4), theta, markerCenterPoint);
		line(overlayImage, p1, p2, color);

		// Draw indication of direction

		p1 = rotate(Point(-settings.markerSizePixels / 2, -settings.markerSizePixels * 5 / 4), theta, markerCenterPoint);
		p2 = rotate(Point(0, -settings.markerSizePixels * 7 / 4), theta, markerCenterPoint);
		line(overlayImage, p1, p2, color);

		p1 = rotate(Point(0, -settings.markerSizePixels * 7 / 4), theta, markerCenterPoint);
		p2 = rotate(Point(settings.markerSizePixels / 2, -settings.markerSizePixels * 5 / 4), theta, markerCenterPoint);
		line(overlayImage, p1, p2, color);
	}
	if (result != NULL)
		putText(overlayImage, result, Point(2, overlayImage.rows - 2), FONT_HERSHEY_SCRIPT_SIMPLEX, 0.5, green);
}


void Optipos::trace(bool flag)
{
	traceOutput = flag;
}
