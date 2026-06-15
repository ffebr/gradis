#!/usr/bin/env bash
# Installs the latest Gradis release binary for Linux or macOS.
#
#   curl -fsSL https://raw.githubusercontent.com/ffebr/gradis/main/scripts/install.sh | bash
#
set -euo pipefail

REPO="ffebr/gradis"
INSTALL_DIR="${INSTALL_DIR:-/usr/local/bin}"
BIN_NAME="gradis"

os="$(uname -s)"
arch="$(uname -m)"

case "$os" in
  Linux)  asset="gradis-linux-x64" ;;
  Darwin) asset="gradis-macos-arm64" ;;
  *) echo "Unsupported OS: $os" >&2; exit 1 ;;
esac

echo "Resolving latest release of $REPO..."
url="$(curl -fsSL "https://api.github.com/repos/$REPO/releases/latest" \
  | grep -o "https://github.com/$REPO/releases/download/[^\"]*/$asset" \
  | head -n1)"

if [ -z "$url" ]; then
  echo "Could not find asset '$asset' in the latest release." >&2
  exit 1
fi

tmp="$(mktemp)"
echo "Downloading $asset..."
curl -fsSL "$url" -o "$tmp"
chmod +x "$tmp"

target="$INSTALL_DIR/$BIN_NAME"
if [ -w "$INSTALL_DIR" ]; then
  mv "$tmp" "$target"
else
  echo "Installing to $target (requires sudo)..."
  sudo mv "$tmp" "$target"
fi

echo "Installed: $target"

# macOS: the binary is unsigned, so strip the Gatekeeper quarantine flag (if any)
# to avoid the "cannot be opened" block on first run.
if [ "$os" = "Darwin" ]; then
  echo "Removing Gatekeeper quarantine flag..."
  xattr -d com.apple.quarantine "$target" 2>/dev/null \
    || sudo xattr -d com.apple.quarantine "$target" 2>/dev/null \
    || true
fi

# rsvg-convert is required at runtime to render the PNG.
if ! command -v rsvg-convert >/dev/null 2>&1; then
  echo
  echo "WARNING: 'rsvg-convert' was not found. Gradis needs it to render PNGs."
  case "$os" in
    Linux)  echo "  Install it with: sudo apt install librsvg2-bin   (or: sudo dnf install librsvg2-tools)" ;;
    Darwin) echo "  Install it with: brew install librsvg" ;;
  esac
fi

echo "Run 'gradis --help' to get started."
