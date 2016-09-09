#!/usr/bash

set -e
echo Preparing Release
mvn --batch-mode release:prepare -Pit
echo Releasing
mvn release:perform
mvn release:clean -Pit