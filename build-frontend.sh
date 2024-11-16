#!/bin/bash

# Step 1: Build the Angular project
cd frontend
ng build || { echo "Angular build failed"; exit 1; }
cd ..

# Step 2: Move the built files to the backend's static folder
rm -rf backend/src/main/resources/static/*
cp -r frontend/dist/frontend/browser/* backend/src/main/resources/static/
rm -rf frontend/dist

# Step 3: Replace '/api/' with '/' in JavaScript files
# Compatible sed command for both macOS and Linux
if [[ "$OSTYPE" == "darwin"* ]]; then
  find backend/src/main/resources/static -type f -name "*.js" -exec sed -i '' 's|/api/|/|g' {} +
else
  find backend/src/main/resources/static -type f -name "*.js" -exec sed -i 's|/api/|/|g' {} +
fi

echo "Build, move, and replace operations completed successfully."
