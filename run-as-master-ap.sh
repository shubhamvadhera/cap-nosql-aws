#!/bin/bash
sudo rm -r build/*
javac -cp lib/\* -d build src/restpackage/*.java
javac -cp lib/\* -d build src/paulpackage/*.java
java -cp build:lib/\* restpackage/SimpleRestServiceApplication master ap