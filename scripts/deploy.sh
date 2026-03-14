#!/bin/bash
# Deploy to itch.io using butler
# 
# Prerequisites:
#   - Install butler: https://itch.io/docs/butler/
#   - Login: butler login
#   - Set ITCH_USER and ITCH_GAME below

set -e

ITCH_USER="your-username"
ITCH_GAME="your-game"

# Build WebGL
echo "Building WebGL..."
cd "$(dirname "$0")/.."
./gradlew html:dist

# Upload
echo "Uploading to itch.io..."
butler push html/build/dist "${ITCH_USER}/${ITCH_GAME}:html"

echo "Done! Visit https://${ITCH_USER}.itch.io/${ITCH_GAME}"
