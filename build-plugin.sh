#!/usr/bin/env bash

VERSION="3.1.$(date +"%s")"

./gradlew clean licenseFormat patchPluginXml buildPlugin -Pversion=$VERSION

echo "=============================================

It's now built. You can find it at:

$(find $PWD/build/distributions | grep ".zip")
"
