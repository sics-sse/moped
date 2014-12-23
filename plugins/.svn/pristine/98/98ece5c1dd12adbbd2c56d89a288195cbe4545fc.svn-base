//============================================================================
// Name        : OptiposCam.cpp
// Author      : Jakob Axelsson
// Version     :
// Copyright   : 
// Description : Optical positioning system
// This is a minimal version which uses the camera and no visualization.
//============================================================================


// This code is intended only for the Raspberry Pi.

#ifdef RPI

#include <cv.h>
#include <highgui.h>

#include <time.h>

// This appears to be included by cv.h or highgui.h
#include <iostream>
#include <cstdlib>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/inotify.h>


#include "OptiposLib.h"


#include <unistd.h>
#include <signal.h>
#include <errno.h>

#define EVENT_SIZE  (sizeof (struct inotify_event))
#define EVENT_BUF_LEN     (1024 * (EVENT_SIZE + 16))


// The preprocessor macro RPI should be defined when compiling on Raspberry Pi.
// If not defined, Windows is assumed.

using namespace std;
using namespace cv;


// Initial values for global parameters read from the command line
int cameraDeviceNumber = 0;
String mapFileName = "";
String settingsFileName = "";
int numberOfFramesToStore = 0;
String frameStorageDirectory = "";


void readCommandLineArguments(int argc, char** argv, int& d, String& s, String& m, int& n, String& f)
// Reads the command line arguments, assuming syntax OptiposCam d s m, where d is
// the device number, s is the settings file name, and m is the map file name.
{
	if (argc != 6) {
		cerr << "Usage: OptiposCam deviceNumber settingsFileName mapFileName numberOfFramesToStore frameStorageDirectory" << endl;
		if (argc > 1) {  // Try to stop the camera process before exiting, if possible
			sscanf(argv[1], "%d", &d);
			kill(d, SIGTERM);
		}
		exit(1);
	}
	sscanf(argv[1], "%d", &d);
	s = argv[2];
	m = argv[3];
	sscanf(argv[4], "%d", &n);
	f = argv[5];
}


void printResult(char output[80], Point_<double>& position, int orientation, vector<MarkerCandidate>& lst, double age)
// Output final result to cout for use by other programs.
// If used in camera mode, with no other flags supplied, this is the only output from the program.
{
	if (lst.size() == 0)
		sprintf(output, " ");  // If no marker found, output empty line.
	else {
		MarkerCandidate* best = &lst[0];
		sprintf(output, "%f %f %d %d %f %f", position.x, position.y, orientation, best->markerType, best->qualityFactor, age);
	}
}


void sendCANFrame(Point_<double>& position, int orientation, vector<MarkerCandidate>& lst, double age)
// Output final result to a CAN frame, with CAN id 1028.

{
	// Variables representing data to be put in CAN frame
	signed short vehiclePositionX;  			// (2 bytes), measured in cm; use 32767 if no marker is found
	signed short vehiclePositionY;  			// (2 bytes), measured in cm; use 32767 if no marker is found
	unsigned char vehicleOrientation;			// (1 byte), values in range 0..255; use 0 if no marker is found
	unsigned char vehiclePositionQualityFactor;	// (1 byte), values in range 0..100%; use 0 if no marker is found
	unsigned short vehiclePositionAge;			// (2 bytes), measured in ms

	// Set variables to be put in data field of CAN frame
	if (lst.size() == 0){  // No marker found
		vehiclePositionX = 32767;
		vehiclePositionY = 32767;
		vehicleOrientation = 0;
		vehiclePositionQualityFactor = 0;
		vehiclePositionAge = round(1000 * age);
	}
	else { // Marker found
		MarkerCandidate* best = &lst[0];
		vehiclePositionX = (signed short) round(100.0 * position.x);
		vehiclePositionY = (signed short) round(100.0 * position.y);
		vehicleOrientation = (unsigned char) round((orientation * 256) / 360);
		vehiclePositionQualityFactor = (unsigned char) round(100.0 * best->qualityFactor);
		vehiclePositionAge = (unsigned short) round(1000.0 * age);
	}

// TODO:
// Add code for writing to CAN here

	printf("CAN frame written: %d %d %u %u %u\n",
		(int) vehiclePositionX,
		(int) vehiclePositionY,
		(int) vehicleOrientation,
		(int) vehiclePositionQualityFactor,
		(int) vehiclePositionAge);
}


void getImage(int cameraPID, Mat& image)
{
	// Trigger camera to make image
	kill(cameraPID, SIGUSR1);

	// Wait for the camera to capture the image.
	// The solution is based on: http://741mhz.com/inotify-wait/

	// Create notification
	int fd;
	int wd;

	fd = inotify_init();
	if (fd < 0) {
		cerr << "Error: Cannot initialize camera image notification" << endl;
		kill(cameraPID, SIGTERM);  // Stop the camera process before exiting
		exit(1);
	}

	// Adding the image file into watch list. Here, the suggestion is to validate the existence of the directory before adding into monitoring list.
	wd = inotify_add_watch(fd, "/dev/shm/optiposimage.jpg", IN_ALL_EVENTS);
	if (wd < 0) {
		cerr << "Error: Cannot add watch for camera image" << endl;
		kill(cameraPID, SIGTERM);  // Stop the camera process before exiting
		exit(1);
	}

	int length, i = 0;
	char buffer[EVENT_BUF_LEN];

	// Read waits for the change event to occur.
	length = read(fd, buffer, EVENT_BUF_LEN);

	if (length < 0) {
		cerr << "Error in get image: cannot receive image from camera." << endl;
		kill(cameraPID, SIGTERM);  // Stop the camera process before exiting
		exit(1);
	}

	// Read the image

	image = imread("/dev/shm/optiposimage.jpg");
    if(!image.data )                              // Check for invalid input
    {
        cerr <<  "Could not open or find the image";
		kill(cameraPID, SIGTERM);  // Stop the camera process before exiting
        exit(1);
    }

    // Remove notification
	inotify_rm_watch(fd, wd);
	close(fd);
}


int frameCount;

void saveImage(Mat& image)
// Saves the latest numberOfFramesToStore images to file for debugging purposes.
// Images are saved in the directory indicated by frameStorageDirectory, under the name
// image0.jpg, image1.jpg, .... When numberOfFramesToStore has been reached, the file names
// are reused, i.e., older images are overwritten.
{
	if (numberOfFramesToStore > 0) {
		char fileName[1000];
		sprintf(fileName, "%s/image%u.jpg", frameStorageDirectory.c_str(), frameCount);
		imwrite(fileName, image);
		frameCount = (frameCount + 1) % numberOfFramesToStore;
	}
}


int main(int argc, char** argv)
{
	sleep(1); // Wait 1 s for camera to warm up...

	// Vector used for results
	vector<MarkerCandidate> lst;

	// Get command line arguments
	readCommandLineArguments(argc, argv, cameraDeviceNumber, settingsFileName, mapFileName, numberOfFramesToStore, frameStorageDirectory);
	cout << "Camera process id: " << cameraDeviceNumber << endl;

	// Read map and settings files, and create image processor
	MarkerMap map;
	map.readFile(mapFileName);
	Settings settings;
	settings.readFile(settingsFileName);
	Optipos optipos(settings, map);

	Point_<double> position = Point(0.0, 0.0);
	int orientation;

	frameCount = 0;

	while (1) {
		Mat image;
		clock_t time1 = clock();
		getImage(cameraDeviceNumber, image);
		saveImage(image);
		clock_t time2 = clock();
		optipos.processImage(image, lst);
		optipos.calculatePosition(lst, position, orientation);
		clock_t time3 = clock();
		char result[80];
		double age = (double) (time3 - time1) / CLOCKS_PER_SEC;
//		printResult(result, position, orientation, lst, age);
//		cout << result << endl;
		sendCANFrame(position, orientation, lst, age);
	}
}

#endif
