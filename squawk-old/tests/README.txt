These are simple tests for Squawk. They are not (yet)
suitable for automated testing.

EXAMPLE BUILD AND RUN
All commands run From main Squawk directory, and squawk build with
metada:
> d && d -prod -mac -o2 rom -metadata cldc imp debugger


To build test:
> d user-suite tests/HelloWorld

or:
> d user-suite tests/HelloWorldMain
> d user-suite tests/Simple
> d user-suite tests/IsoTest

To run a test midlet:
> squawk -suite:tests/HelloWorld/HelloWorld

To run a test midlet that isn't listed in the manifest:
> squawk -suite:tests/Simple/Simple -testMIDlet:tests.TestDestroyApp

To run a test that is not a midlet:
> squawk -suite:tests/HelloWorldMain/HelloWorldMain tests.HelloWorldMain


javac -source 1.4 -target 1.4 -cp ~/workspace/squawk/cldc/classes.jar tests/ClassLoaderInput.java



