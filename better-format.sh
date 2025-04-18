#!/bin/sh
format ()
{
  echo "Formating: $1"
  java -jar ./google-java-format-1.23.0-all-deps.jar --replace $1
  echo "Done formating: $1"
}

export -f format
find src -name "*.java" | parallel format
