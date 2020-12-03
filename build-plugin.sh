#!/usr/bin/env bash

./gradlew clean licenseFormat patchPluginXml buildPlugin

echo "=============================================

It's now built. You can find it at:

$(find $PWD/build/distributions | grep ".zip")
"
