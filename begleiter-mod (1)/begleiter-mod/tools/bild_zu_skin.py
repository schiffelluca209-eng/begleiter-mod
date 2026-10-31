#!/usr/bin/env python3
"""Wandelt ein Ganzkörperbild einer Person in einen 64x64 Minecraft-Skin um.

Benutzung:
    python3 bild_zu_skin.py foto.png begleiter.png

Die Datei begleiter.png kommt nach
    src/main/resources/assets/begleiter/textures/entity/begleiter.png
und die Mod wird neu gebaut.

Das ist eine Pixel-Annaeherung (Farben von Haut, Haaren, Kleidung), kein echtes 3D-Scannen.
Am besten: Person von vorn, ganz im Bild, ruhiger Hintergrund.
"""
import sys
from PIL import Image


def auto_crop(img):
    """Schneidet den ruhigen Hintergrund ab (Farbe der Bildecken)."""
    rgb = img.convert("RGB")
    w, h = rgb.size
    corners = [rgb.getpixel(p) for p in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]]
    bg = tuple(sum(c[i] for c in corners) // 4 for i in range(3))
    mask = Image.new("L", (w, h), 0)
    px, mp = rgb.load(), mask.load()
    for y in range(h):
        for x in range(w):
            d = sum(abs(px[x, y][i] - bg[i]) for i in range(3))
            mp[x, y] = 255 if d > 60 else 0
    box = mask.getbbox()
    return rgb.crop(box) if box else rgb


def avg(img):
    return img.resize((1, 1), Image.BOX).getpixel((0, 0))


def paste_region(skin, fig, src, dst, size):
    """Kopiert den Bereich src (x,y,w,h) der 16x32-Figur an dst (x,y) im Skin, Groesse size=(w,h)."""
    x, y, w, h = src
    part = fig.crop((x, y, x + w, y + h)).resize(size, Image.NEAREST)
    skin.paste(part, dst)


def fill(skin, box, color):
    skin.paste(Image.new("RGBA", (box[2], box[3]), color + (255,)), (box[0], box[1]))


def main(src_path, out_path):
    img = auto_crop(Image.open(src_path))
    # Auf Minecraft-Proportionen bringen: 16 breit x 32 hoch (Kopf 8, Koerper 12, Beine 12)
    fig = img.resize((16, 32), Image.LANCZOS).convert("RGB")

    head = fig.crop((4, 0, 12, 8))
    hair = avg(fig.crop((4, 0, 12, 2)))
    body_c = avg(fig.crop((4, 8, 12, 20)))
    arm_c = avg(fig.crop((0, 8, 4, 20)))
    arm2_c = avg(fig.crop((12, 8, 16, 20)))
    leg_c = avg(fig.crop((4, 20, 8, 32)))
    leg2_c = avg(fig.crop((8, 20, 12, 32)))
    head_c = avg(head)

    skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    # Kopf (Layout: top 8,0 | bottom 16,0 | right 0,8 | front 8,8 | left 16,8 | back 24,8)
    fill(skin, (8, 0, 8, 8), hair)
    fill(skin, (16, 0, 8, 8), head_c)
    for x in (0, 16, 24):
        fill(skin, (x, 8, 8, 8), hair)
    skin.paste(head.resize((8, 8), Image.NEAREST), (8, 8))
    # Haare oben ueber der Stirn
    fill(skin, (8, 8, 8, 2), hair)

    # Torso: front 20,20 (8x12), Seiten/Rueckseite in Koerperfarbe
    fill(skin, (16, 16, 24, 4), body_c)
    fill(skin, (16, 20, 4, 12), body_c)
    fill(skin, (28, 20, 4, 12), body_c)
    fill(skin, (32, 20, 8, 12), body_c)
    paste_region(skin, fig, (4, 8, 8, 12), (20, 20), (8, 12))

    # Rechter Arm (Bereich 40,16), linker Arm (32,48)
    fill(skin, (40, 16, 16, 4), arm_c)
    fill(skin, (40, 20, 16, 12), arm_c)
    paste_region(skin, fig, (0, 8, 4, 12), (44, 20), (4, 12))
    fill(skin, (32, 48, 16, 4), arm2_c)
    fill(skin, (32, 52, 16, 12), arm2_c)
    paste_region(skin, fig, (12, 8, 4, 12), (36, 52), (4, 12))

    # Rechtes Bein (0,16), linkes Bein (16,48)
    fill(skin, (0, 16, 16, 4), leg_c)
    fill(skin, (0, 20, 16, 12), leg_c)
    paste_region(skin, fig, (4, 20, 4, 12), (4, 20), (4, 12))
    fill(skin, (16, 48, 16, 4), leg2_c)
    fill(skin, (16, 52, 16, 12), leg2_c)
    paste_region(skin, fig, (8, 20, 4, 12), (20, 52), (4, 12))

    skin.save(out_path)
    print(f"Skin gespeichert: {out_path}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(1)
    main(sys.argv[1], sys.argv[2])
