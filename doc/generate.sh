#!/bin/bash

set -e

# Please execute this script inside the `/doc` directory

pushd ../
./gradlew :kotlite-apidoc:generateStdlibApiDoc
popd

rm -Rf output
cp ../apidoc/build/apidoc/API.adoc ./usermanual/
docker run --rm -it -v "$(pwd)/..:/wd" uwebarthel/asciidoctor asciidoctor \
  -r asciidoctor-diagram \
  -D "/wd/doc/output" \
  /wd/doc/usermanual/index.adoc
cp -R usermanual/media output/media
