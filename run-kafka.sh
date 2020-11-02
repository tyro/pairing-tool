#!/usr/bin/env bash


docker run \
  --rm -d \
  --net=host \
  --name kafka \
  -e ADV_HOST=`hostname` \
  landoop/fast-data-dev

set +x

echo "========================================================="
echo ""
echo "To kill the container, just run "
echo "docker stop kafka"
echo ""
echo "========================================================="

set -x
