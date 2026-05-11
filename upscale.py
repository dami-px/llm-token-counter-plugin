#!/usr/bin/env python3
"""Upscale PNGs to meet JetBrains Marketplace minimum (1200x760)."""
import sys
from pathlib import Path
from PIL import Image

MIN_W, MIN_H = 1280, 800

def upscale(path: Path) -> None:
    img = Image.open(path)
    w, h = img.size
    scale = max(MIN_W / w, MIN_H / h, 1.0)
    if scale <= 1.0:
        print(f"{path.name}: already {w}x{h}, skipping")
        return
    new_w, new_h = int(w * scale), int(h * scale)
    up = img.resize((new_w, new_h), Image.LANCZOS)
    out = path.with_name(f"{path.stem}_hires.png")
    up.save(out, "PNG", optimize=True)
    print(f"{path.name}: {w}x{h} -> {new_w}x{new_h}  ({out.name})")

if __name__ == "__main__":
    for arg in sys.argv[1:]:
        upscale(Path(arg).expanduser())
