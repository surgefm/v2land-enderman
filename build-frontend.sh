#!/bin/bash

rm -rf src/main/resources/static

echo 'Building frontend...'
cd enderman-frontend
yarn
yarn build
cd ..

echo 'Copying files...'
cp -r enderman-frontend/dist server/src/main/resources/static
echo 'Show files'
ls -al src/main/resources/static

