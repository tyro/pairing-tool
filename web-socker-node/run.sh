#!/usr/bin/env sh

docker build . -t pairing-tool-server && docker run -it -p 4001:4001 pairing-tool-server
