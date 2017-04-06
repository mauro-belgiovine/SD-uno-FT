#!/bin/bash
mkdir -p out/production/SD-uno
cp -rf src/cards out/production/SD-uno/.
cp -rf src/icons out/production/SD-uno/.
make
