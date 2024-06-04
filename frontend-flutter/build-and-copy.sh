#!/bin/sh
echo "Remove ./build folder"
rm -rf ./build

echo "Build for web. You need flutter for this"
flutter build web --web-renderer html

echo "Copy ./build/web/ to ./../backend/src/main/resources/static"
cp -r ./build/web/ ./../backend/src/main/resources/static
