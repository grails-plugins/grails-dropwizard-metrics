#!/bin/bash
set -e
rm -rf *.zip
./gradlew clean check assemble

filename=$(find build/libs -name "*.jar" | head -1)
filename=$(basename "$filename")

EXIT_STATUS=0
echo "Publishing artifact for branch $TRAVIS_BRANCH?"
if [[ -n $TRAVIS_TAG ]] || [[ $TRAVIS_BRANCH == 'master' && $TRAVIS_PULL_REQUEST == 'false' ]]; then


  if [[ -n $TRAVIS_TAG ]]; then
      echo "Publishing artifact"
      ./gradlew bintrayUpload || EXIT_STATUS=$?
  else
      echo "No tag present. Not publishing"
  fi
fi

exit $EXIT_STATUS