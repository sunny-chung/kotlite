#!/bin/bash

# Please execute this script inside the `/doc` directory

pushd ../
./gradlew :kotlite-apidoc:generateStdlibApiDoc
popd

rm -Rf output
docker run --rm -it -v "$(pwd)/..:/wd" uwebarthel/asciidoctor asciidoctor \
  -r asciidoctor-diagram \
  -D "/wd/doc/output" \
  /wd/doc/usermanual/index.adoc
cp -R usermanual/img output/img
