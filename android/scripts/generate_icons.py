#!/usr/bin/env python3
"""Generate Android mipmap launcher icons from Veyla brand assets."""

from PIL import Image
import os

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_MASTER = os.path.join(BASE, "design", "veyla-brand", "03_APP_ICON_MASTER_1024.png")
SRC_FOREGROUND = os.path.join(BASE, "design", "veyla-brand", "02_ANDROID_ADAPTIVE_FOREGROUND_1024.png")
RES = os.path.join(BASE, "app", "src", "main", "res")
DRAWABLE = os.path.join(RES, "drawable")

# Adaptive icon: 108dp viewport. Safe zone = central 66dp (66.6%).
# Source foreground is 1024x1024 designed for this purpose.

MIPMAP_DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

DENSITY_FOREGROUND_SIZES = {
    "drawable-mdpi": 108,
    "drawable-hdpi": 162,
    "drawable-xhdpi": 216,
    "drawable-xxhdpi": 324,
    "drawable-xxxhdpi": 432,
}


def ensure_dir(path):
    os.makedirs(path, exist_ok=True)


def main():
    master = Image.open(SRC_MASTER).convert("RGBA")
    print(f"Master icon: {master.size[0]}x{master.size[1]}")
    fg_src = Image.open(SRC_FOREGROUND).convert("RGBA")
    print(f"Foreground source: {fg_src.size[0]}x{fg_src.size[1]}")

    # Generate mipmap launcher icons (full rounded-square for pre-API 26)
    for density, size in MIPMAP_DENSITIES.items():
        dir_path = os.path.join(RES, density)
        ensure_dir(dir_path)

        icon = master.resize((size, size), Image.LANCZOS)
        icon.save(os.path.join(dir_path, "ic_launcher.png"), "PNG")
        icon.save(os.path.join(dir_path, "ic_launcher_round.png"), "PNG")
        print(f"  {density}/ic_launcher.png: {size}x{size}")

    # Generate adaptive foreground at density-specific sizes
    for density, size in DENSITY_FOREGROUND_SIZES.items():
        dir_path = os.path.join(RES, density)
        ensure_dir(dir_path)
        fg = fg_src.resize((size, size), Image.LANCZOS)
        fg.save(os.path.join(dir_path, "ic_launcher_foreground.png"), "PNG")
        print(f"  {density}/ic_launcher_foreground.png: {size}x{size}")

    print("\nIcon generation complete.")


if __name__ == "__main__":
    main()
