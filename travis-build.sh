#!/bin/bash
set -e
rm -rf *.zip
./gradlew clean check assemble

filename=$(find build/libs -name "*.jar" | head -1)
filename=$(basename "$filename")

EXIT_STATUS=0

exit $EXIT_STATUS