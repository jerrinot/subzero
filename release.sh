#!/usr/bash

set -e
echo Preparing Release
mvn --batch-mode release:prepare -Pit -DdryRun=true
echo Releasing
mvn release:perform