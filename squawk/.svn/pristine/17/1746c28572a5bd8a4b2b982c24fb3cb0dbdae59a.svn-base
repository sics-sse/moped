#!/bin/bash

# Build the suite
./d.sh user-clean tests/HelloWorld
./d.sh user-suite tests/HelloWorld

# Run the suite
./squawk -suite:tests/HelloWorld/HelloWorld
