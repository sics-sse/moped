//============================================================================
// Name        : OptiposLib.h
// Author      : Jakob Axelsson
// Version     :
// Copyright   : 
// Description : Optical positioning system
//============================================================================

// Notes on setting up compilation under Windows with Mingw:
// - Add a user PATH variable (not a system PATH!) that includes paths to Mingw binaries and the Open CV bin directory containing libraries.
// - When instructing the linker about what libraries to include, do not use the full library name.
//   If the full name is libxxx.dll, just write xxx.

using namespace std;
using namespace cv;


class ParameterFile {

protected:

	void readParameterDouble(string fileName, int lineNumber, string line, double& x, string name);

	void readParameterInt(string fileName, int lineNumber, string line, int& x, string name);

	virtual void readContent(string fileName, int lineNumber, string line) = 0;

	virtual bool done() = 0;

public:

	void readFile(String fileName);
};


class MarkerMap : public ParameterFile {

// MarkerMap contains the data from a map file which describes the environment where the system is used.

public:
	// Approximate expected size of a marker (in meters)
	double ceilingHeight;
	double markerSize;

	vector<double> x;  // x-position
	vector<double> y;  // y-position
	vector<double> o;  // orientation
	vector<int> t;     // type

	MarkerMap();

protected:

	int step;

	void readContent(string fileName, int lineNumber, string line);

	bool done();

};


class Settings : public ParameterFile {

// Settings contains the data from a settings file.
// It consists of various calibration parameters related to the equipment used.

public:

	// Parameters describing equipment used
	int cameraFieldOfView; 	// Degrees. RPi normal lens = 40, wide angle = 60, Logitech c270 = 32.

	// Calibration parameters for camera offset relative to center of mobile device (in meters)
	double cameraOffsetX; // Offset in test installation is -0.021;
	double cameraOffsetY;

	// Image size, should be set after reading the image
	int cols;
	int rows;

	// Derived parameters
	double pixelsPerMeter;
	int markerSizePixels;

	// Color settings. Hues are in the range [0..720], saturation and value are [0..100]
	int hueMin[4], hueMax[4], saturationMin[4], saturationMax[4], valueMin[4], valueMax[4];

	// The hue table classifies each hue as one of the colors 0 (background), 1 (yellow), 2 (cyan), or 3 (magenta).
	uchar hueTable[180];

	// Parameters for the Canny edge detection algorithm and other routines
	double cannyThreshold1, cannyThreshold2;
	int cannyKernelSize;
	double findMarkersThreshold, selectAndRankMarkersThreshold;

	Settings();

	void setImageSize(int w, int h, MarkerMap map);

protected:

	void initHueTable();

	int step;
	int color;

	void readContent(string fileName, int lineNumber, string line);

	bool done();
};


class CircleCandidate {
// This class represent the location of a potential circle in the image.

public:
	int x, y; 	// x, y position of the circle center in image coordinates.
	int votes; 	// Number of votes given to it by the Hough circle detection algorithm

	CircleCandidate(int xPos, int yPos, int v);
};


class MarkerCandidate {
	// This class represents the location of a potential marker in the image.

public:
	int x, y; // x, y position of the marker in image coordinates
	int orientation; // orientation in degrees
	double qualityFactor;

	// fieldColors represents the number of pixels of each of the four colours in each field of the marker,
	// where field[0][c] is the surroundings around the circle, and fields 1..4 are each half circle in the
	// marker, counted from the top ("north") end.
	int fieldColors[5][4];

	int markerType;

	MarkerCandidate(int xPos, int yPos);
};


// TODO: Does this need to be visible to the outside? It is only needed because accumulator is protected in Optipos...
class CircleAccumulator {

// The class CircleAccumulator is used by the Hough circle detection algorithm.

public:
	Mat_<ushort> m;  	// The internal accumulator matrix
	static int markerRadius; 	// Marker radius in pixels

	void addCircle(int cx, int cy);

	static void initCircleStamp(int r);

	void buildCircleAccumulator(Mat& edgeImage);

	void buildCircleCandidateSet(vector<CircleCandidate>& lst);

	int maxValue();

	void image(Mat& accumulatorImageColor);

protected:

	static vector<Point> circleStamp;

};


class Optipos {

protected:
	MarkerMap map;
	Settings settings;
	Point_<double> previousPosition;
	vector<CircleCandidate> circles;

// Images at different stages of the processing, to allow visualization
	Mat blurredImage;
	Mat_<uchar> filteredHueMatrix;
	Mat cannyImage;
	CircleAccumulator accumulator;

public:

	Optipos(Settings& s, MarkerMap& m);

	void processImage(Mat& originalImage, vector<MarkerCandidate>& lst);

	void calculatePosition(vector<MarkerCandidate>& lst, Point_<double>& newPos, int& orientation);

	void getHSVImage(Mat& outputImage);

	void getCannyImage(Mat& outputImage);

	void getAccumulatorImage(Mat& outputImage);

	void overlayCirclesOnImage(Mat& originalImage, Mat& overlayImage);

	void overlayMarkersOnImage(Mat& originalImage, Mat& overlayImage, vector<MarkerCandidate>& lst, char result[80]);

	void trace(bool flag);
};
