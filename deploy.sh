#!/usr/bin/env bash

# Token lives in prd vault, "token Pairing Tool Upload"
TOKEN="secret"
VERSION="3.1.$(date +"%s")"
# Blank channel is the main release channel
CHANNEL=

./gradlew \
  clean \
  licenseFormat \
  patchPluginXml \
  buildPlugin \
  publishPlugin \
    -Pversion=$VERSION \
    -Ptoken=$TOKEN \
    -Pchannels=$CHANNEL

gh release create ${VERSION} build/distributions/* \
  -t ${VERSION}
