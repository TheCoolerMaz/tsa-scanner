#!/bin/bash
# Test keyboard navigation in the jam-template
# Requires: wtype, hyprctl

set -e

export WAYLAND_DISPLAY=wayland-1
export XDG_RUNTIME_DIR=/run/user/1000
export HYPRLAND_INSTANCE_SIGNATURE=$(ls /run/user/1000/hypr/ | head -1)

cd "$(dirname "$0")/.."

echo "Launching game..."
./gradlew lwjgl3:run &
GRADLE_PID=$!

# Wait for window to appear
sleep 8

# Find and focus the game window
WINDOW=$(hyprctl clients -j | jq -r '.[] | select(.class == "com.jamtemplate.lwjgl3.Lwjgl3Launcher" or .title | contains("Jam")) | .address' | head -1)
if [ -n "$WINDOW" ]; then
    echo "Focusing window: $WINDOW"
    hyprctl dispatch focuswindow "address:$WINDOW"
    sleep 0.5
fi

echo "Sending inputs..."

# Skip splash
wtype -k space
sleep 1

# Navigate menu
echo "Tab to Settings..."
wtype -k Tab
sleep 0.3
wtype -k Return
sleep 1

echo "Escape to go back..."
wtype -k Escape
sleep 0.5

echo "Navigate to Quit..."
wtype -k Down
wtype -k Down
sleep 0.3

echo "Pressing Enter on Quit..."
wtype -k Return

# Wait for gradle to exit
wait $GRADLE_PID 2>/dev/null || true
echo "Done!"
