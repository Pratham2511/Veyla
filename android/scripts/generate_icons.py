#!/usr/bin/env python3
"""Generate Android launcher icons from approved Veyla brand assets.

Reads:
  - android/design/veyla-brand/02_ANDROID_ADAPTIVE_FOREGROUND_1024.png (adaptive foreground)
  - android/design/veyla-brand/03_APP_ICON_MASTER_1024.png (full app icon)
  - android/design/veyla-brand/01_VEYLA_MASTER_MARK_TRANSPARENT_2048.png (in-app logo)

Writes:
  - drawable-{density}/ic_launcher_foreground.png  (adaptive foreground layers)
  - mipmap-{density}/ic_launcher.png             (legacy full icons)
  - mipmap-{density}/ic_launcher_round.png        (legacy round icons)
  - drawable/veyla_logo_mark.png                   (in-app logo mark)
"""

import os
import sys
from PIL import Image

# Base paths
BASE = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
BRAND_DIR = os.path.join(BASE, "design", "veyla-brand")
RES_DIR = os.path.join(BASE, "app", "src", "main", "res")

# Source assets
ADAPTIVE_FG_SRC = os.path.join(BRAND_DIR, "02_ANDROID_ADAPTIVE_FOREGROUND_1024.png")
APP_ICON_SRC = os.path.join(BRAND_DIR, "03_APP_ICON_MASTER_1024.png")
IN_APP_LOGO_SRC = os.path.join(BRAND_DIR, "01_VEYLA_MASTER_MARK_TRANSPARENT_2048.png")

# Android adaptive icon: foreground is 108dp, safe zone is 66dp (centered)
# The foreground layer is 108x108dp within a 108x108dp canvas.
# The SAFE zone (66dp) is centered: offset = (108-66)/2 = 21dp from each edge.
# For the 1024px source at xxxhdpi (4x), 108dp = 432px, safe = 264px, margin = 84px
# Scale ratios for adaptive foreground (targeting 108dp at each density):
ADAPTIVE_SIZES = {
    "mdpi": 108,       # 108dp * 1x
    "hdpi": 162,       # 108dp * 1.5x
    "xhdpi": 216,      # 108dp * 2x
    "xxhdpi": 324,     # 108dp * 3x
    "xxxhdpi": 432,    # 108dp * 4x
}

# Legacy launcher icon sizes (48dp standard launcher icon)
LEGACY_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# In-app logo target size
IN_APP_LOGO_SIZE = (512, 512)  # Reasonable in-app size from 2048px source


def ensure_dir(path):
    os.makedirs(path, exist_ok=True)


def generate_adaptive_foregrounds():
    """Generate density-specific adaptive icon foregrounds."""
    print(f"Source: {ADAPTIVE_FG_SRC}")
    if not os.path.exists(ADAPTIVE_FG_SRC):
        print(f"ERROR: Adaptive foreground source not found: {ADAPTIVE_FG_SRC}")
        return False

    src = Image.open(ADAPTIVE_FG_SRC).convert("RGBA")
    print(f"Source size: {src.size}")

    for density, size in ADAPTIVE_SIZES.items():
        out_dir = os.path.join(RES_DIR, f"drawable-{density}")
        ensure_dir(out_dir)
        out_path = os.path.join(out_dir, "ic_launcher_foreground.png")

        resized = src.resize((size, size), Image.LANCZOS)
        resized.save(out_path, "PNG")
        print(f"  {density}: {size}x{size} -> {out_path}")

    return True


def generate_legacy_icons():
    """Generate density-specific legacy launcher icons (full icon with background)."""
    print(f"Source: {APP_ICON_SRC}")
    if not os.path.exists(APP_ICON_SRC):
        print(f"ERROR: App icon source not found: {APP_ICON_SRC}")
        return False

    src = Image.open(APP_ICON_SRC).convert("RGBA")
    print(f"Source size: {src.size}")

    for density, size in LEGACY_SIZES.items():
        out_dir = os.path.join(RES_DIR, f"mipmap-{density}")
        ensure_dir(out_dir)

        # Standard launcher icon
        out_path = os.path.join(out_dir, "ic_launcher.png")
        resized = src.resize((size, size), Image.LANCZOS)
        resized.save(out_path, "PNG")
        print(f"  {density}: {size}x{size} -> {out_path}")

        # Round launcher icon
        out_round = os.path.join(out_dir, "ic_launcher_round.png")
        resized.save(out_round, "PNG")
        print(f"  {density}: {size}x{size} (round) -> {out_round}")

    return True


def generate_in_app_logo():
    """Generate the in-app logo mark for use in Compose Image painters."""
    print(f"Source: {IN_APP_LOGO_SRC}")
    if not os.path.exists(IN_APP_LOGO_SRC):
        print(f"ERROR: In-app logo source not found: {IN_APP_LOGO_SRC}")
        return False

    out_dir = os.path.join(RES_DIR, "drawable")
    ensure_dir(out_dir)
    out_path = os.path.join(out_dir, "veyla_logo_mark.png")

    src = Image.open(IN_APP_LOGO_SRC).convert("RGBA")
    print(f"Source size: {src.size}")

    resized = src.resize(IN_APP_LOGO_SIZE, Image.LANCZOS)
    resized.save(out_path, "PNG")
    print(f"  In-app logo: {IN_APP_LOGO_SIZE} -> {out_path}")

    return True


def main():
    print("=== Generating Android launcher icons from Veyla brand assets ===")
    print()

    ok = True

    print("--- Adaptive icon foregrounds ---")
    ok = generate_adaptive_foregrounds() and ok
    print()

    print("--- Legacy launcher icons ---")
    ok = generate_legacy_icons() and ok
    print()

    print("--- In-app logo mark ---")
    ok = generate_in_app_logo() and ok
    print()

    if ok:
        print("=== All icons generated successfully ===")
    else:
        print("=== ERRORS occurred during icon generation ===")
        sys.exit(1)


if __name__ == "__main__":
    main()
