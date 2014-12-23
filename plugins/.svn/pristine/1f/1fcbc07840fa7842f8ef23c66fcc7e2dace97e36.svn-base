//============================================================================
// Name        : Optipos.cpp
// Author      : Jakob Axelsson
// Version     :
// Copyright   : 
// Description : Optical positioning system
//============================================================================

// Notes on setting up compilation under Windows with Mingw:
// - Add a user PATH variable (not a system PATH!) that includes paths to Mingw binaries and the Open CV bin directory containing libraries.
// - When instructing the linker about what libraries to include, do not use the full library name.
//   If the full name is libxxx.dll, just write xxx.

// TODO Status 140911:
// - Create some small markers which are suitable for testing at arms length using web camera.
// - Why is it not possible to declare traceOutput in both OptiposLib.cpp (but not in Optipos.h) and Optipos.cpp?
// - Modify synthetic image generator to allow markers of different direction
// - Vary experiment by running a set of samples for each setting using different markers and direction
// (- Replan experiment, so that only files with same marker size, lens, map are used in same batch.)
// - Separate ceiling height (in map file) and camera mounting height (in parameters file)

// TODO for validation:
// - Time stamp images using real-time (OptiposCam)
// - Automatically start the program on booting (see http://www.tldp.org/HOWTO/HighQuality-Apps-HOWTO/boot.html)
// - Determine suitable marker size (based on experiment), preferably for wide angle lens
// - Determine where to place markers in the corridor, creating map file
// - Produce markers, and place them in corridor
// - Check camera calibration
// - Add the possibility to write log file and save image for each sample
// - Install RPi with camera in model car (disconnected from rest of car)
// - Run tests, and inspect log files
// - Connect the node, and pass position data to VCU
// - Make position data available in PIRTE, and write app to send it to cloud service for telemetrics

#include <iostream>
#include <fstream>
#include <cv.h>
#include <highgui.h>
#include <time.h>

#include "OptiposLib.h"

// The preprocessor macro RPI should be defined when compiling on Raspberry Pi.
// If not defined, Windows is assumed.

#ifndef RPI
#include <windows.h>
#endif

// TODO: Can this be removed?
//#ifdef RPI
//#include <raspicam/raspicam_cv.h>
//#endif

using namespace std;
using namespace cv;


// Initial values for global parameters read from the command line

bool traceFlag = false;
bool visualize = false;
bool outputStatistics = false;
String statisticsFileName = "";

enum InputSource {CAMERA, IMAGEFILE, DIRECTORY, RANDOM, CALIBRATION};
InputSource input = CAMERA;
int cameraDeviceNumber = 0;
String imageFileName = "";
String imageDirectoryName = "";
String mapFileName = "";
String settingsFileName = "";


void parseFlagDouble(int& i, int argc, char** argv, double& var, String traceVarName)
// Parses a command flag argument into a double variable
{
	if (i + 1 < argc) {
		double value;
		int result = sscanf(argv[i+1], "%lf", &value);
		if (result > 0)  {
			var = value;
			if (traceFlag)
				cout << traceVarName << " = " << var << endl;
		}
		else {
			cerr << argv[i] << ": Invalid argument [" << argv[i+1] << "]. Please supply floating point number" << endl;
			exit(-1);
		}
		i++;
	}
	else {
		cerr << argv[i] << ": Missing value" << endl;
	}
}


void parseFlagInt(int& i, int argc, char** argv, int& var, String traceVarName)
// Parses a command flag argument into an int variable
{
	if (i + 1 < argc) {
		int value;
		int result = sscanf(argv[i+1], "%d", &value);
		if (result > 0)  {
			var = value;
			if (traceFlag)
				cout << traceVarName << " = " << var << endl;
		}
		else {
			cerr << argv[i] << ": Invalid argument [" << argv[i+1] << "]. Please supply integer number" << endl;
			exit(-1);
		}
		i++;
	}
	else {
		cerr << argv[i] << ": Missing value" << endl;
	}
}


void parseFlagString(int& i, int argc, char** argv, String& var, String traceVarName)
// Parses a command flag argument into a string variable
{
	if (i + 1 < argc) {
		var = argv[i+1];
		if (traceFlag)
			cout << traceVarName << " = " << var << endl;
		i++;
	}
	else {
		cerr << argv[i] << ": Missing value" << endl;
	}
}


void parseCommandLineArguments(int argc, char** argv)
// Parses the command line arguments.
{
	if (traceFlag)
		cout << "*** Entering parseCommandLineArguments ***" << endl;
	if ((argc == 2) && (strcmp(argv[1], "-h") == 0)) {
		cout << "Usage: " << endl << endl;
		cout << "Optipos -h" << endl;
		cout << "Optipos [-c deviceNo][-f cameraFieldOfView] [-x cameraOffsetX] [-y cameraOffsetY][-s statisticsFilePath]." << endl;
		cout << "[-t] [-v] [-i imageFilePath | -d imageDirectoryPath | -r | -k deviceNo] -m mapFilePath -p parameterSettingsFilePath." << endl << endl;
		cout << "The flags have the following meaning:" << endl;
		cout << "-h: print this help text." << endl;
		cout << "-c: camera device number." << endl;
		cout << "-s: creates a file of comma separated parameters for each image which can be used with statistics (normally combined with a -d flag)." << endl;
		cout << "-t: traceOutput flag, providing traceOutput printouts from the different stages of the algorithms. Default is false." << endl;
		cout << "-v: visualizes the different stages of the algorithms using images. Default is false." << endl;
		cout << "-i: provides a single image to be processed." << endl;
		cout << "-d: provides a directory in which all image files will be processed one after the other." << endl;
		cout << "-r: generates random synthetic images that are processed one after the other." << endl;
		cout << "-k: reads one image from the camera and outputs hue, saturation, and value histograms for the image." << endl;
		cout << "-m: gives a file path to the map file, containing information on marker positions, and ceiling height." << endl << endl;
		cout << "-p: gives a file path to the settings file, containing information on equipment related calibration settings." << endl << endl;
		cout << "Note that either the -i or -d parameters can be supplied. If none of them is provided, the algorithm reads images directly from the camera." << endl;
		cout << "In all modes, a summary of each image is sent to the standard output as one line with space separated items." << endl;
		exit(0);
	}
	for (int i = 1; i < argc; i++) {
		if (strcmp(argv[i], "-c") == 0)
			parseFlagInt(i, argc, argv, cameraDeviceNumber, "cameraDeviceNumber");
		else if (strcmp(argv[i], "-s") == 0) {
			parseFlagString(i, argc, argv, statisticsFileName, "statisticsFileName");
			outputStatistics = true;
		}
		else if (strcmp(argv[i], "-t") == 0) {
			traceFlag = true;
			if (traceFlag)
				cout << "traceOutput = true" << endl;
		}
		else if (strcmp(argv[i], "-v") == 0) {
			visualize= true;
			if (traceFlag)
				cout << "visualize = true" << endl;
		}
		else if (strcmp(argv[i], "-i") == 0) {
			if (input == CAMERA) {
				parseFlagString(i, argc, argv, imageFileName, "imageFileName");
				input = IMAGEFILE;
			}
			else {
				cerr << "Please provide either -i, -d, -k, or -r." << endl;
				exit(-1);
			}
		}
		else if (strcmp(argv[i], "-d") == 0) {
			if (input == CAMERA) {
				parseFlagString(i, argc, argv, imageDirectoryName, "imageDirectoryName");
				input = DIRECTORY;
			}
			else {
				cerr << "Please provide either -i, -d, -k, or -r." << endl;
				exit(-1);
			}
		}
		else if (strcmp(argv[i], "-r") == 0) {
			if (input == CAMERA) {
				input = RANDOM;
			}
			else {
				cerr << "Please provide either -i, -d, -k, or -r." << endl;
				exit(-1);
			}
		}
		else if (strcmp(argv[i], "-k") == 0) {
			if (input == CAMERA) {
				input = CALIBRATION;
				parseFlagInt(i, argc, argv, cameraDeviceNumber, "cameraDeviceNumber");
			}
			else {
				cerr << "Please provide either -i, -d, -k, or -r." << endl;
				exit(-1);
			}
		}
		else if (strcmp(argv[i], "-m") == 0)
			parseFlagString(i, argc, argv, mapFileName, "mapFileName");
		else if (strcmp(argv[i], "-p") == 0)
			parseFlagString(i, argc, argv, settingsFileName, "settingsFileName");
		else {
			cerr << "Undefined argument: " << argv[i] << ". Use -h to display command line arguments." << endl;
			exit(-1);
		}
	}
	if (mapFileName == "") {
		cerr << "No map file provided. Use -m <file name> to specify the map file." << endl;
		exit(-1);
	}
	if (traceFlag)
		cout << "*** Leaving parseCommandLineArguments ***" << endl;
}


void visualizeStep(int delay, int& visualizationMode, Mat originalImage, Optipos optipos, vector<MarkerCandidate>& lst, char* text)
// Visualizes the algorithm in one of five possible output modes.
{
	Mat image;
	int keyCode;
	namedWindow("Display Image", CV_WINDOW_AUTOSIZE);
	do {
		switch (visualizationMode) {
			case 0:
				image = originalImage;
				break;
			case 1:
				optipos.getHSVImage(image);
				break;
			case 2:
				optipos.getCannyImage(image);
				break;
			case 3:
				optipos.getAccumulatorImage(image);
				optipos.overlayCirclesOnImage(image, image);
				imshow("Display Image", image);
				break;
			case 4:
				optipos.overlayMarkersOnImage(originalImage, image, lst, text);
				break;
			default:
				cerr << "Undefined visualization mode: " << visualizationMode << endl;
				exit(1);
		}
		imshow("Display Image", image);
		keyCode = waitKey(delay);
		if (keyCode == '+')
			visualizationMode = (visualizationMode + 1) % 5;
		else if (keyCode == '-')
			visualizationMode = (visualizationMode - 1 + 5) % 5;
		else if ('0' <= keyCode && keyCode < '5')
			visualizationMode = keyCode - '0';
	} while (delay <= 0 && keyCode != ' ');
}


void printResult(char output[80], Point_<double>& position, int orientation, vector<MarkerCandidate>& lst)
// Output final result to cout for use by other programs.
// If used in camera mode, with no other flags supplied, this is the only output from the program.
{
	if (lst.size() == 0)
		sprintf(output, " ");  // If no marker found, output empty line.
	else {
		MarkerCandidate* best = &lst[0];
		sprintf(output, "%f %f %d %d %f", position.x, position.y, orientation, best->markerType, best->qualityFactor);
	}
}


String statisticsFileHeader =
"ImageFileName, MarkerSize, CameraFieldOfView, ImageWidth, ImageHeight, MarkerSizePixels, NoMarkersFound, \
MarkerX, MarkerY, MarkerOrientation, MarkerType, QualityFactor";


void generateStatistics(Settings settings, ofstream& statisticsFile, MarkerMap map, Point_<double> pos, int orientation, vector<MarkerCandidate>& lst)
// Prints some statistics into a comma separated file, which can be used for statistical analysis.
{
	if (outputStatistics) {
		statisticsFile << imageFileName.c_str() << ", "		// Image file name
				<< map.markerSize << ","					// Marker size (meters)
				<< settings.cameraFieldOfView << ", "		// Camera field of view (degrees)
				<< settings.cols << ", "					// Image width
				<< settings.rows << ", "					// Image height
				<< settings.markerSizePixels << ", "		// Marker size (pixels)
				<< lst.size() << ", ";						// Number of markers found
		if (lst.size() > 0) {
			MarkerCandidate* m = &lst[0];
			statisticsFile
				<< pos.x << ", "
				<< pos.y << ", "
				<< orientation << ", "
				<< m->markerType << ", "
				<< m->qualityFactor;
		}
		else
			statisticsFile << "NA, NA, NA, NA, NA";
		statisticsFile << endl;
	}
}


Point rotate(Point pos, double angle, Point delta)
// Calculates the position of pos in a coordinate system which is a translation by delta and a rotation of angle (in radians).
{
	return Point(
			round(pos.x * cos(angle) - pos.y * sin(angle) + delta.x),
			round(pos.x * sin(angle) + pos.y * cos(angle) + delta.y));
}


void drawSynthesizedMarker(Mat& image, int markerType, int size, int x, int y, int orientation)
// Draws a marker of markerType and with center in x, y and orientation 0 or 180 degrees, with size given in pixels
// TO DO: Draw with the given orientation instead; currently that argument is ignored.
{
	Scalar whiteColor   = Scalar(255, 255, 255);
	Scalar yellowColor  = Scalar(000, 170, 255);
	Scalar cyanColor    = Scalar(255, 042, 000);
	Scalar magentaColor = Scalar(170, 000, 255);

	Scalar color[3] = { yellowColor, cyanColor, magentaColor };

	int c1 = markerType / 27;
	int c2 = (markerType - c1 * 27) / 9;
	int c3 = (markerType - c1 * 27  - c2 * 9) / 3;
	int c4 = markerType - c1 * 27  - c2 * 9 - c3 * 3;

/*
// Embryo of code for rotating markers.
// TODO: It appears that OpenCV rectangle function cannot be used for drawing rotated rectangles...
// Use ellipse instead for the background...? (easist using the RotatedBox representation of an ellipse).

	// Create a white background
	Point p1 = Point(-size / 2 - 5, -size * 5 / 4 - 5);
//	rotate(p1, orientation, Point(x, y));
	Point p2 = Point(size / 2 + 5, size * 5 / 4 + 5);
//	rotate(p2, orientation, Point(x, y));
	rectangle(image, p1, p2, whiteColor, CV_FILLED);

	// Draw the fields
	ellipse(image, Point(x, y - size * 3 / 4), Size(size / 2, size / 2), 0.0, 180.0, 360.0, color[c1], CV_FILLED);
	ellipse(image, Point(x, y - size * 3 / 4), Size(size / 2, size / 2), 0.0, 0.0, 180.0, color[c2], CV_FILLED);
	ellipse(image, Point(x, y + size * 3 / 4), Size(size / 2, size / 2), 0.0, 180.0, 360.0, color[c3], CV_FILLED);
	ellipse(image, Point(x, y + size * 3 / 4), Size(size / 2, size / 2), 0.0, 0.0, 180.0, color[c4], CV_FILLED);
*/

	// Create a white background
	Point p1 = Point(x - size / 2 - 5, y - size * 5 / 4 - 5);
	Point p2 = Point(x + size / 2 + 5, y + size * 5 / 4 + 5);
	rectangle(image, p1, p2, whiteColor, CV_FILLED);

	// Draw the fields
	ellipse(image, Point(x, y - size * 3 / 4), Size(size / 2, size / 2), 0.0, 180.0, 360.0, color[c1], CV_FILLED);
	ellipse(image, Point(x, y - size * 3 / 4), Size(size / 2, size / 2), 0.0, 0.0, 180.0, color[c2], CV_FILLED);
	ellipse(image, Point(x, y + size * 3 / 4), Size(size / 2, size / 2), 0.0, 180.0, 360.0, color[c3], CV_FILLED);
	ellipse(image, Point(x, y + size * 3 / 4), Size(size / 2, size / 2), 0.0, 0.0, 180.0, color[c4], CV_FILLED);
}


void addNoise(Mat& image, double noiseLevel)
// Adds noise to an image, with the parameter noiseLevel indicating the proportion of pixels affected.
// Noise is in marker colors.
{
	Vec3b yellowColor  = Vec3b(000, 170, 255);
	Vec3b cyanColor    = Vec3b(255, 042, 000);
	Vec3b magentaColor = Vec3b(170, 000, 255);

	Vec3b color[3] = { yellowColor, cyanColor, magentaColor };

	for (int y = 0; y < image.rows; y++)
		for (int x = 0; x < image.cols; x++)
			if (rand() % 100 < noiseLevel * 100)
				image.at<Vec3b>(y, x) = color[rand() % 3];
}


void createSynthesizedImage(Mat& image, int size, int markerType, int x, int y, int orientation, int addShapes, double noise)
// The createSynthesizedImage function returns a synthesized image that can be used for testing the image processing.
// The argument size is the marker size in pixels, and markerType says which marker type to use.
// x, y determine the position in the image, and orientation is the direction of the marker.
// addShapes says how many random shapes should be added as a maximum,
// and noise gives the maximum noise ratio to be used.
{
	Scalar whiteColor = Scalar(255, 255, 255);
	Scalar yellowColor  = Scalar(000, 170, 255);
	Scalar cyanColor    = Scalar(255, 042, 000);
	Scalar magentaColor = Scalar(170, 000, 255);

	Scalar color[3] = { yellowColor, cyanColor, magentaColor };

	// Create a white background
	rectangle(image, Point(0,0), Point(image.rows, image.cols), whiteColor, CV_FILLED);

	// Draw random number of random shapes
	int noShapes = rand() % (addShapes + 1);
	for (int i = 0; i < noShapes; i++) {
		if (rand() % 2 == 0) // Draw a circle
			ellipse(image,
					Point(rand() % image.cols, rand() % image.rows),
					Size(rand() % size  + size / 2, rand() % size  + size / 2),
					0.0, 0.0, 360.0, color[rand() % 3], CV_FILLED);
		else { // Draw a rectangle
			Point c = Point(rand() % image.cols, rand() % image.rows);
			rectangle(image, c, c + Point(rand() % size  + size / 2, rand() % size  + size / 2), color[rand() % 3], CV_FILLED);
		}
	}

	// Add marker
	drawSynthesizedMarker(image, markerType, size, x, y, orientation);

	// Add noise
	addNoise(image, noise);
}


void printHistogram(Mat image)
// Prints hue, saturation, value histograms for the image to stdout
{
	Mat hsvImage;
    cvtColor(image, hsvImage, CV_BGR2HSV);
	vector<cv::Mat> hsvChannels;
    split(hsvImage, hsvChannels);
	Mat_<uchar> hue, saturation, value;
    hue = hsvChannels[0];
    saturation = hsvChannels[1];
    value = hsvChannels[2];

    int hueCount[256];
    int saturationCount[256];
    int valueCount[256];
    for (int i = 0; i < 256; i++) {
    	hueCount[i] = 0;
    	saturationCount[i] = 0;
    	valueCount[i] = 0;
    }

    uchar* hptr;
	uchar* sptr;
	uchar* vptr;
	for (int y = 0; y < image.rows; ++y) {
		hptr = hue.ptr<uchar>(y);
		sptr = saturation.ptr<uchar>(y);
		vptr = value.ptr<uchar>(y);
		for (int x = 0; x < image.cols; ++x) {
			hueCount[hptr[x]]++;
			saturationCount[sptr[x]]++;
			valueCount[vptr[x]]++;
		}
	}

	cout << "Hue: effective range is [0, 180[, as used by Open CV library." << endl;
	for (int i = 0; i < 180; i++)
		cout << hueCount[i] << " ";
	cout << endl;

	cout << "Saturation: effective range is [0, 256[, as used by Open CV library." << endl;
	for (int i = 0; i < 256; i++)
		cout << saturationCount[i] << " ";
	cout << endl;

	cout << "Value: effective range is [0, 256[, as used by Open CV library." << endl;
	for (int i = 0; i < 256; i++)
		cout << valueCount[i] << " ";
	cout << endl;
}


int main(int argc, char** argv)
{
	// Vector used for results
	vector<MarkerCandidate> lst;

	// Get command line arguments
	parseCommandLineArguments(argc, argv);

	// Read map and settings files
	MarkerMap map;
	map.readFile(mapFileName);
	Settings settings;
	settings.readFile(settingsFileName);

	Optipos optipos(settings, map);
	optipos.trace(traceFlag);

	// Open statistics file if needed, and output table header
	ofstream statisticsFile;
	if (outputStatistics) {
		statisticsFile.open(statisticsFileName.c_str());
		statisticsFile << statisticsFileHeader << endl;
	}

	Point_<double> position = Point(0.0, 0.0);
	int orientation;

#ifndef RPI
	int visualizationMode = 4;
#else
	int loopCount = 0;
#endif



// Using camera input ///////////////////////////////////////////////////

	if (input == CAMERA) {
		// If on the RPI, the raspicam camera is used, and otherwise a web camera is assumed

		VideoCapture cap(cameraDeviceNumber); // open the video camera with the provided device number
		if (!cap.isOpened()) {  // if not success, exit program
			cerr << "Cannot open the video camera no. " << cameraDeviceNumber << endl;
			return -1;
		}

		while (1) {
//			clock_t time1 = clock();
			Mat frame;

#ifdef RPI
			// Skip frames to get to the latest, to avoid lag caused by buffered frames
			// This does not work properly...
			for (int i = 0; i < 10; i++)
				cap.grab();
#endif

			cap.grab();
			bool bSuccess = cap.retrieve(frame); // read a new frame from video
			if (!bSuccess) { //if not success, break loop
				cerr << "Cannot read a frame from video stream" << endl;
				break;
			}

//			clock_t time2 = clock();
			optipos.processImage(frame, lst);
//			clock_t time3 = clock();
			optipos.calculatePosition(lst, position, orientation);
//			clock_t time4 = clock();

//			cout << "Read frame:         " << (double) (time2 - time1) / CLOCKS_PER_SEC << endl;
//			cout << "Process image:      " << (double) (time3 - time2) / CLOCKS_PER_SEC << endl;
//			cout << "Calculate position: " << (double) (time4 - time3) / CLOCKS_PER_SEC << endl;

			char result[80];
			printResult(result, position, orientation, lst);
			cout << result << endl;

			generateStatistics(settings, statisticsFile, map, position, orientation, lst);

#ifndef RPI
			if (visualize)
				visualizeStep(30, visualizationMode, frame, optipos, lst, result);

#else
			// On RPi, save each image
			Mat overlayFrame;
			optipos.overlayMarkersOnImage(frame, overlayFrame, lst, result);
			char filename[80];
			sprintf(filename, "Images/image%i.jpg", loopCount);
			imwrite(filename, overlayFrame);
			loopCount++;
#endif
		}
	}


// Using input from image file ///////////////////////////////////////////////////

	else if (input == IMAGEFILE) {
		String originalImageFileName = imageFileName;

		// Read and display original image
		if (traceFlag)
			cout << "Read file: " << originalImageFileName << endl;
		Mat originalImage = imread(originalImageFileName);
		if (!originalImage.data)
		{
			cerr << "No image data" << endl;
			exit(-1);
		}

		optipos.processImage(originalImage, lst);
		optipos.calculatePosition(lst, position, orientation);

		char result[80];
		printResult(result, position, orientation, lst);
		cout << result << endl;
		generateStatistics(settings, statisticsFile, map, position, orientation, lst);
	}


// Using input from image file directory ///////////////////////////////////////////////////

	else if (input == DIRECTORY) {
		// This code just reads the files in the directory and prints their names.
		// Instead, read in the file as above, and process each file....
		// It is Windows specific, due to the lack of OS neutral functions for this.
		// For RPi, this option produces an error.
#ifndef RPI
		String originalImageFileName = imageFileName;

		HANDLE hFind;
		WIN32_FIND_DATA data;

		String fileFilter = imageDirectoryName.c_str();
		fileFilter.append("\\*.jpg");
		hFind = FindFirstFile(fileFilter.c_str(), &data);
		if (hFind != INVALID_HANDLE_VALUE) {
			do {
				originalImageFileName = imageDirectoryName.c_str();
				originalImageFileName.append("\\");
				originalImageFileName.append(data.cFileName);
				// Read and display original image
				if (traceFlag)
					cout << "Read file: " << originalImageFileName << endl;
				Mat originalImage = imread(originalImageFileName);
				if (!originalImage.data)
				{
					cerr << "No image data" << endl;
					exit(-1);
				}

				optipos.processImage(originalImage, lst);
				optipos.calculatePosition(lst, position, orientation);

				char result[80];
				printResult(result, position, orientation, lst);
				cout << result << endl;
				generateStatistics(settings, statisticsFile, map, position, orientation, lst);

				if (visualize)
					visualizeStep(0, visualizationMode, originalImage, optipos, lst, result);
			} while (FindNextFile(hFind, &data));
			FindClose(hFind);
		}
#else
		cerr << "Image processing from directory currently not supported for Raspberry Pi" << endl;
		exit(1);
#endif
	}


// Using random synthetic image input ///////////////////////////////////////////////////

	else if (input == RANDOM) {
		switch (2) { // Change the number here to select which experiment to run.
		case 0:
			// This code is for an experiment to check how the size of markers affect marker identification.

/*
 			namedWindow("Overlay Image", CV_WINDOW_AUTOSIZE);
			namedWindow("Canny Image", CV_WINDOW_AUTOSIZE);
			namedWindow("Accumulator Image", CV_WINDOW_AUTOSIZE);
*/
			for (int i = 5; i < 100; i++) {
				// i is the marker size in pixels
				Mat synthesizedImage(480, 480, CV_8UC3);
				createSynthesizedImage(synthesizedImage, 16, i, 240, 240, 0, 0, 0.0);

				// Set up
				MarkerMap m;
				m.readFile(mapFileName);
				m.ceilingHeight = 480 * m.markerSize / (i * 2.0 * tan(((double) settings.cameraFieldOfView) / 180.0 * CV_PI / 2.0));
				Settings s;
				s.readFile(settingsFileName);

				Optipos opti(s, m);
				opti.trace(false);

				opti.processImage(synthesizedImage, lst);
				if (lst.size() == 0)
					cout << i << " 0.0" << endl;
				else
					cout << i << " " << lst[0].qualityFactor << endl;
/*
				Mat cannyImage, overlayImage, accumulatorImage;
				opti.overlayMarkersOnImage(synthesizedImage, overlayImage, lst, NULL);
				opti.getAccumulatorImage(accumulatorImage);
				opti.overlayCirclesOnImage(accumulatorImage, accumulatorImage);

				opti.getCannyImage(cannyImage);
				imshow("Overlay Image", overlayImage);
				imshow("Canny Image", cannyImage);
				imshow("Accumulator Image", accumulatorImage);
				waitKey(0);
*/
			}
			break;
		case 1:
			// This code is for checking the effects of additional objects in the image.
					for (int i = 0; i < 1000000; i++) {
						if (i % 100 == 0)
							cout << "i = " << i << endl;
						Mat synthesizedImage(480, 480, CV_8UC3);
						createSynthesizedImage(synthesizedImage, 16, 240, 240, 0, 20, 30, 0.0);
						optipos.processImage(synthesizedImage, lst);
			//			calculatePosition(pixelsPerMeter, 480, 480, lst, map, position, position, orientation);
			//			printResult(position, orientation, lst);
						generateStatistics(settings, statisticsFile, map, position, orientation, lst);
						if (lst.size() == 0) {
							cout << "No marker found!" << endl;
							char fileName[50];
							sprintf(fileName, "c:\\OptiposImages\\NoMarkerFound%d.jpg", i);
							imwrite(fileName, synthesizedImage);
						}
						else {
							MarkerCandidate* best = &lst[0];
							if ((best->x - 240) * (best->x - 240) + (best->y - 240) * (best->y - 240) > 9) {
							// If marker center is more than 3 pixels away from image center, it is incorrect identification
								cout << "Marker found in wrong position: (x, y) == (" << best->x << ", " << best->y << ")" << endl;
								char fileName[50];
								sprintf(fileName, "c:\\OptiposImages\\WrongMarkerFound%d.jpg", i);
								imwrite(fileName, synthesizedImage);
							}
							if (lst.size() > 1)
								cout << "Warning: multiple markers found. lst.size() == " << lst.size() << endl;
						}
					}
			break;
		case 2:
			// This code is for an experiment to check how the amount of noise affects marker identification.
			// TODO: Run several samples per noise ratio, and take average.
			// TODO: Run at several different marker sizes, to see combination effects.


 			namedWindow("Overlay Image", CV_WINDOW_AUTOSIZE);
			namedWindow("Canny Image", CV_WINDOW_AUTOSIZE);
			namedWindow("Accumulator Image", CV_WINDOW_AUTOSIZE);

			for (int i = 0; i < 50; i++) {
				// i is the noise level in percent

				// Set up
				int markerSize = 20;
				MarkerMap m;
				m.readFile(mapFileName);
				m.ceilingHeight = 480 * m.markerSize / (markerSize * 2.0 * tan(((double) settings.cameraFieldOfView) / 180.0 * CV_PI / 2.0));
				Settings s;
				s.readFile(settingsFileName);

				Optipos opti(s, m);
				opti.trace(false);

				Mat synthesizedImage(480, 480, CV_8UC3);
				createSynthesizedImage(synthesizedImage, markerSize, 16, 240, 240, 0, 0, ((double) i) / 100.0);

				opti.processImage(synthesizedImage, lst);
				if (lst.size() == 0)
					cout << i << " 0.0" << endl;
				else
					cout << i << " " << lst[0].qualityFactor << endl;

				Mat cannyImage, overlayImage, accumulatorImage;
				opti.overlayMarkersOnImage(synthesizedImage, overlayImage, lst, NULL);
				opti.getAccumulatorImage(accumulatorImage);
				opti.overlayCirclesOnImage(accumulatorImage, accumulatorImage);

				opti.getCannyImage(cannyImage);
				imshow("Overlay Image", overlayImage);
				imshow("Canny Image", cannyImage);
				imshow("Accumulator Image", accumulatorImage);
				waitKey(0);

			}
			break;
		}
	}


// Using image input to generate calibration data ///////////////////////////////////////////////////

	else if (input == CALIBRATION) {
		// This mode takes one picture with the camera, and outputs H, S, V histograms for that image
		VideoCapture cap(cameraDeviceNumber); // open the video camera with the provided device number

		if (!cap.isOpened()) {  // if not success, exit program
			cerr << "Cannot open the video camera no. " << cameraDeviceNumber << endl;
			return -1;
		}

//		for (int i = 0; i < 10; i++)
//			cap.grab();

		Mat frame;
//		bool bSuccess = cap.retrieve(frame); // read a new frame from video
		bool bSuccess = cap.read(frame); // read a new frame from video
		if (!bSuccess) { //if not success, break loop
			cerr << "Cannot read a frame from video stream" << endl;
			return -1;
		}

#ifndef RPI
		// On PC, show the  frame in a window
		namedWindow("MyVideo",CV_WINDOW_AUTOSIZE); //create a window called "MyVideo"
		imshow("MyVideo", frame); //show the frame in "MyVideo" window
		waitKey(0);
#else
		// On RPi, save frame to a file
		imwrite("Images/calibrationimage.jpg", frame);
#endif

		printHistogram(frame);
	}
	if (outputStatistics)
		statisticsFile.close();
}
