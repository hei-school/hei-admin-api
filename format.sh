#!/bin/sh
find src -name "*.java" -print0 | xargs -0 -n 500 java -jar google-java-format-1.23.0-all-deps.jar --replace
