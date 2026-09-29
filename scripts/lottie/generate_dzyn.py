#!/usr/bin/env python3
"""Генератор Lottie-анимации Дзыня: app-modules/product-core/ui/src/main/res/raw/anim_dzyn.json.

Запуск из корня репозитория: python3 scripts/lottie/generate_dzyn.py

Композиция 200×190 (как прежний Canvas в PetView), земля на y = 160, 30 fps.
Каждая эмоция PetMood — маркер длиной SEGMENT кадров; внутри — бесшовная петля (первый и последний кадр в покое).
Цвет шёрстки задаётся в приложении dynamic property по keyPath ("**", "Fur"). Убор — один из слоёв
"Bowler" (котелок, по умолчанию), "Cap" (кепка) и "Headphones" (наушники): приложение включает нужный через
прозрачность слоя (keyPath — имя слоя), в каждом уборе есть монетка-звезда.

Движение по правилам motion-design: спокойный Playful, дыхание ±2% за 4 с, overshoot не больше 8%,
без тряски и тревожных эффектов. Эмоция COLD — укутан в плед, а не болеет.
"""
import json
import math
import re
from pathlib import Path

OUT = Path(__file__).resolve().parents[2] / "app-modules/product-core/ui/src/main/res/raw/anim_dzyn.json"

W, H, FPS = 200, 190, 30
SEGMENT = 120  # длина маркера эмоции, кадры
LOOP = SEGMENT - 1  # последний кадр петли = покой; маркер не заходит в соседний сегмент
MOODS = ["neutral", "happy", "joy", "proud", "shock", "thinking", "cold"]
TOTAL = SEGMENT * len(MOODS)
GROUND = (100, 160)


def seg(mood):
    return MOODS.index(mood) * SEGMENT


# ---------- цвета ----------

def rgb(hex_color, alpha=1.0):
    h = hex_color.lstrip("#")
    return [int(h[i:i + 2], 16) / 255 for i in (0, 2, 4)] + [alpha]


FUR = "#AA7CEE"  # лиловая шёрстка по умолчанию; в приложении перекрашивается
INK = "#1A1130"
WHITE = "#FFFFFF"
SCLERA_SHADE = "#D9D0EA"
IRIS_LIGHT, IRIS_DARK = "#D08A45", "#6E3812"
BLUSH = "#FF8FB8"
HAT, HAT_DARK, HAT_BAND, HAT_LIGHT = "#2E3D99", "#1F2A73", "#18205C", "#5A6CD6"
CAP, CAP_DARK, CAP_LIGHT = "#E0445A", "#A8283D", "#FF9DAB"
PHONES, PHONES_LIGHT = "#4DC9FF", "#A8E6FF"
COIN_OUTER, COIN_INNER = "#FDD631", "#FBA91C"
BAG, BAG_DARK, BAG_STRING = "#9A5A2E", "#5E3316", "#E0A86A"
PLAID, PLAID_STRIPE, PLAID_WARM = "#F6EFE4", "#7FBFA4", "#FFB347"
GLOW, SHADOW = "#8B5CF6", "#07040E"


# ---------- примитивы Lottie ----------

def static(v):
    return {"a": 0, "k": v}


SINE_OUT = {"x": [0.37], "y": [0]}
SINE_IN = {"x": [0.63], "y": [1]}
BACK_IN = {"x": [0.32], "y": [1.2]}  # мягкий overshoot ~6%


def anim(frames, ease_in=SINE_IN, ease_out=SINE_OUT):
    """frames: список (кадр, значение[, 'hold'|'back']). Значения — числа или списки."""
    keys = []
    for i, f in enumerate(frames):
        t, v = f[0], f[1]
        mode = f[2] if len(f) > 2 else None
        k = {"t": t, "s": v if isinstance(v, list) else [v]}
        if i < len(frames) - 1:
            if mode == "hold":
                k["h"] = 1
            else:
                k["o"] = ease_out
                k["i"] = BACK_IN if frames[i + 1][2:3] == ("back",) else ease_in
        keys.append(k)
    return {"a": 1, "k": keys}


def per_mood(builder, moods=MOODS):
    """Склеивает покадровые треки эмоций в один; builder(mood) -> список (кадр от начала сегмента, значение...)."""
    frames = []
    for m in moods:
        s = seg(m)
        frames += [(s + f[0],) + tuple(f[1:]) for f in builder(m)]
    return anim(frames)


def tr(p=(0, 0), a=(0, 0), s=(100, 100), r=0, o=100):
    return {
        "ty": "tr",
        "p": p if isinstance(p, dict) else static(list(p)),
        "a": static(list(a)),
        "s": s if isinstance(s, dict) else static(list(s)),
        "r": r if isinstance(r, dict) else static(r),
        "o": static(o),
        "sk": static(0),
        "sa": static(0),
    }


def group(name, items, transform=None):
    """items: фигуры, затем заливка — или вложенные группы снизу вверх (в Lottie верхняя идёт первой)."""
    if all(item["ty"] == "gr" for item in items):
        items = list(reversed(items))
    return {"ty": "gr", "nm": name, "it": items + [transform or tr()]}


def ellipse(cx, cy, w, h):
    return {"ty": "el", "d": 1, "p": static([cx, cy]), "s": static([w, h])}


def rect(cx, cy, w, h, r=0):
    return {"ty": "rc", "d": 1, "p": static([cx, cy]), "s": static([w, h]), "r": static(r)}


def star(cx, cy, points, outer, inner, rotation=0):
    return {"ty": "sr", "sy": 1, "d": 1, "pt": static(points), "p": static([cx, cy]), "r": static(rotation),
            "ir": static(inner), "is": static(0), "or": static(outer), "os": static(0)}


def path(svg):
    """Подмножество SVG path: M, L, C, Q, Z с абсолютными координатами."""
    tokens = re.findall(r"[MLCQZ]|-?\d+(?:\.\d+)?", svg)
    verts, ins, outs = [], [], []
    closed = False
    i = 0
    cmd = None
    cur = None

    def add(pt, in_tan=(0, 0)):
        verts.append(list(pt))
        ins.append(list(in_tan))
        outs.append([0, 0])

    while i < len(tokens):
        t = tokens[i]
        if t in "MLCQZ":
            cmd = t
            i += 1
            if cmd == "Z":
                closed = True
                continue
        nums = lambda n: [float(x) for x in tokens[i:i + n]]
        if cmd == "M" or cmd == "L":
            x, y = nums(2)
            i += 2
            add((x, y))
            cur = (x, y)
        elif cmd == "C":
            x1, y1, x2, y2, x, y = nums(6)
            i += 6
            outs[-1] = [x1 - cur[0], y1 - cur[1]]
            add((x, y), (x2 - x, y2 - y))
            cur = (x, y)
        elif cmd == "Q":
            qx, qy, x, y = nums(4)
            i += 4
            c1 = (cur[0] + 2 / 3 * (qx - cur[0]), cur[1] + 2 / 3 * (qy - cur[1]))
            c2 = (x + 2 / 3 * (qx - x), y + 2 / 3 * (qy - y))
            outs[-1] = [c1[0] - cur[0], c1[1] - cur[1]]
            add((x, y), (c2[0] - x, c2[1] - y))
            cur = (x, y)
    if closed and len(verts) > 1 and verts[0] == verts[-1]:
        ins[0] = ins.pop()
        verts.pop()
        outs.pop()
    return {"ty": "sh", "ks": static({"v": verts, "i": ins, "o": outs, "c": closed})}


def fill(color, alpha=1.0, name="Fill"):
    return {"ty": "fl", "nm": name, "c": static(rgb(color)), "o": static(alpha * 100), "r": 1}


def fur():
    return fill(FUR, name="Fur")


def stroke(color, width, alpha=1.0):
    return {"ty": "st", "nm": "Stroke", "c": static(rgb(color)), "o": static(alpha * 100), "w": static(width),
            "lc": 2, "lj": 2}


def radial(center, radius, stops):
    """stops: [(позиция, цвет, прозрачность)]."""
    colors, alphas = [], []
    for pos, color, alpha in stops:
        colors += [pos] + rgb(color)[:3]
        alphas += [pos, alpha]
    return {"ty": "gf", "nm": "Gradient", "o": static(100), "r": 1, "t": 2,
            "s": static(list(center)), "e": static([center[0] + radius, center[1]]),
            "h": static(0), "a": static(0), "g": {"p": len(stops), "k": static(colors + alphas)}}


def shade(center, radius, strength=0.28, start=0.6):
    return radial(center, radius, [(0, SHADOW, 0), (start, SHADOW, 0), (1, SHADOW, strength)])


def shine(center, radius, strength=0.35):
    return radial(center, radius, [(0, WHITE, strength), (1, WHITE, 0)])


_ind = [0]


def layer(name, groups, parent=None, ks=None, ip=0, op=TOTAL, null=False, opacity=100):
    _ind[0] += 1
    ks = ks or {}
    lay = {
        "ddd": 0, "ind": _ind[0], "ty": 3 if null else 4, "nm": name, "sr": 1,
        "ks": {
            "a": ks.get("a", static([0, 0, 0])),
            "p": ks.get("p", static([0, 0, 0])),
            "s": ks.get("s", static([100, 100, 100])),
            "r": ks.get("r", static(0)),
            "o": static(opacity),
        },
        "ao": 0, "ip": ip, "op": op, "st": 0, "bm": 0,
    }
    if not null:
        lay["shapes"] = list(reversed(groups))
    if parent is not None:
        lay["parent"] = parent["ind"]
    return lay


# ---------- движение ----------

def breath(amount=1.2):
    """Вдох: чуть выше и уже. Один цикл на сегмент."""
    return [(0, [100, 100]), (LOOP // 2, [100 - amount, 100 + amount * 1.6]), (LOOP, [100, 100])]


def rig_scale(m):
    if m == "joy":
        # Предвкушение → прыжок → приземление с мягким overshoot → дыхание.
        return [(0, [100, 100]), (10, [104, 96]), (20, [97, 104]), (32, [100, 100]), (44, [104.5, 95.5]),
                (54, [99, 101.5], "back"), (64, [100, 100]), (92, [99, 101.6]), (LOOP, [100, 100])]
    if m == "proud":
        return [(0, [100, 100]), (18, [104, 104], "back"), (90, [104, 104]), (LOOP, [100, 100])]
    if m == "shock":
        return [(0, [100, 100]), (6, [96, 105]), (18, [100, 100], "back"), (70, [99, 101.6]), (LOOP, [100, 100])]
    if m == "cold":
        return breath(0.8)
    return breath()


def rig_position(m):
    x, y = GROUND
    if m == "joy":
        return [(0, [x, y]), (10, [x, y]), (28, [x, y - 8]), (44, [x, y]), (LOOP, [x, y])]
    if m == "happy":
        return [(0, [x, y]), (14, [x, y - 4]), (28, [x, y]), (42, [x, y - 4]), (56, [x, y]), (LOOP, [x, y])]
    return [(0, [x, y]), (LOOP, [x, y])]


def rig_rotation(m):
    if m == "thinking":
        return [(0, 0), (24, -4), (96, -4), (LOOP, 0)]
    if m == "cold":
        return [(0, 0), (30, 1.2), (90, -1.2), (LOOP, 0)]
    return [(0, 0), (LOOP, 0)]


HAT_PIVOT = (100, 52)
HAT_TILT = 6


def hat_position(m):
    x, y = HAT_PIVOT
    if m == "joy":
        # Котелок запаздывает за прыжком и догоняет при приземлении.
        return [(0, [x, y]), (20, [x, y + 3]), (34, [x, y - 2]), (46, [x, y - 3]), (56, [x, y + 1]), (66, [x, y]),
                (LOOP, [x, y])]
    if m == "shock":
        return [(0, [x, y]), (8, [x, y - 5]), (24, [x, y], "back"), (LOOP, [x, y])]
    if m == "happy":
        return [(0, [x, y]), (18, [x, y + 1.5]), (32, [x, y - 1]), (46, [x, y + 1.5]), (60, [x, y]), (LOOP, [x, y])]
    return [(0, [x, y]), (70, [x, y + 1]), (LOOP, [x, y])]


def hat_rotation(m):
    if m == "proud":
        return [(0, HAT_TILT), (22, HAT_TILT + 4, "back"), (90, HAT_TILT + 4), (LOOP, HAT_TILT)]
    return [(0, HAT_TILT), (66, HAT_TILT + 1.5), (LOOP, HAT_TILT)]


def bag_rotation(m):
    amp = 1 if m == "cold" else (4 if m == "joy" else 2.5)
    return [(0, -amp), (LOOP // 2, amp), (LOOP, -amp)]


def blink(m):
    """Моргание один раз за сегмент; в эмоциях с закрытыми глазами — без него."""
    at = 90 if m == "shock" else 70
    return [(0, [100, 100]), (at, [100, 100]), (at + 3, [100, 8]), (at + 6, [100, 100]), (LOOP, [100, 100])]


# ---------- рисунок ----------

BODY = "M100 48 C142 48 176 76 176 108 C176 142 142 160 100 160 C58 160 24 142 24 108 C24 76 58 48 100 48 Z"
EYES = [(78, 80), (122, 80)]


def ground():
    return layer("Ground", [
        group("Glow", [ellipse(100, 160, 168, 44), radial((100, 160), 84, [(0, GLOW, 0.34), (1, GLOW, 0)])]),
        group("Shadow", [ellipse(100, 164, 108, 14), fill(SHADOW, 0.55)]),
    ])


def feet(rig):
    groups = []
    for x in (72, 128):
        groups.append(group("Foot", [
            group("FurShape", [ellipse(x, 156, 36, 16), fur()]),
            group("Shade", [ellipse(x, 156, 36, 16), shade((x, 150), 20, 0.3, 0.2)]),
        ]))
    return layer("Feet", groups, rig)


def body(rig):
    return layer("Body", [
        group("FurShape", [path(BODY), fur()]),
        group("Shade", [path(BODY), shade((100, 96), 92, 0.3, 0.55)]),
        group("Shine", [path(BODY), shine((68, 72), 86, 0.38)]),
        group("Rim", [path("M40 84 C48 64 70 52 96 50"), stroke(WHITE, 4, 0.4)]),
    ], rig)


def eye(cx, cy, look=(0, 0), sclera=(32, 36), iris=11, pupil=5.5, blink_track=None):
    lx, ly = look
    ix, iy = cx + lx, cy + ly
    items = [
        group("Sclera", [ellipse(cx, cy, *sclera), fill(WHITE)]),
        group("ScleraShade", [ellipse(cx, cy, *sclera),
                              radial((cx, cy - 6), sclera[1] * 0.7, [(0, SCLERA_SHADE, 0), (0.7, SCLERA_SHADE, 0),
                                                                     (1, SCLERA_SHADE, 1)])]),
        group("Iris", [ellipse(ix, iy, iris * 2, iris * 2),
                       radial((ix - 2, iy - 3), iris * 1.2, [(0, IRIS_LIGHT, 1), (1, IRIS_DARK, 1)])]),
        group("Pupil", [ellipse(ix, iy, pupil * 2, pupil * 2), fill(INK)]),
        group("Glint", [ellipse(ix - iris * 0.4, iy - iris * 0.4, 6.4, 6.4), fill(WHITE)]),
        group("Glint", [ellipse(ix + iris * 0.35, iy + iris * 0.4, 3.2, 3.2), fill(WHITE, 0.9)]),
    ]
    s = blink_track if blink_track is not None else static([100, 100])
    return group("Eye", items, tr(p=(cx, cy), a=(cx, cy), s=s))


def open_eyes(m, look=None, **kw):
    start = seg(m)
    track = anim([(start + f[0], f[1]) for f in blink(m)])
    looks = look or [(3, 3), (-3, 3)]  # как на референсе: взгляд чуть к центру
    return [eye(x, y, looks[i], blink_track=track, **kw) for i, (x, y) in enumerate(EYES)]


def arc_eyes(up=True):
    d = -12 if up else 7
    return [group("ClosedEye", [path(f"M{x - 12} {y + 4} Q{x} {y + 4 + d} {x + 12} {y + 4}"), stroke(INK, 4)])
            for x, y in EYES]


def blush():
    return [group("Blush", [ellipse(x, 99, 16, 9), fill(BLUSH, 0.5)]) for x in (62, 138)]


def mouth_line(svg, width=3.2):
    return group("Mouth", [path(svg), stroke(INK, width)])


def face(m, rig):
    if m == "neutral":
        items = open_eyes(m) + [mouth_line("M93 100 Q100 105 107 100")]
    elif m == "happy":
        items = open_eyes(m) + blush() + [mouth_line("M90 98 Q100 108 110 98")]
    elif m == "joy":
        items = arc_eyes() + blush() + [
            group("Mouth", [path("M88 98 Q100 116 112 98 Z"), fill(INK)]),
            group("Tongue", [ellipse(100, 105, 10, 5), fill(BLUSH)]),
        ]
    elif m == "proud":
        items = arc_eyes() + blush() + [mouth_line("M89 98 Q100 110 111 98", 3.6)]
    elif m == "shock":
        items = open_eyes(m, look=[(0, 0), (0, 0)], sclera=(36, 40), iris=9, pupil=4) + [
            group("Mouth", [ellipse(100, 103, 11, 13), fill(INK)])]
    elif m == "thinking":
        items = open_eyes(m, look=[(4, -6), (4, -6)]) + [mouth_line("M94 102 L106 100")]
    else:  # cold
        items = arc_eyes(up=False) + [mouth_line("M92 102 Q96 99 100 102 Q104 105 108 102")]
    s = seg(m)
    return layer(f"Face {m}", items, rig, ip=s, op=s + SEGMENT)


def plaid(rig):
    s = seg("cold")
    items = [group("Plaid", [rect(100, 138, 128, 44, 22), fill(PLAID)])]
    items += [group("Stripe", [path(f"M{x} 118 L{x} 158"), stroke(PLAID_STRIPE, 5)]) for x in (62, 88, 112, 138)]
    items += [group("Stripe", [path(f"M40 {y} L160 {y}"), stroke(PLAID_WARM, 5, 0.75)]) for y in (130, 148)]
    return layer("Plaid", items, rig, ip=s, op=s + SEGMENT)


def bow(rig):
    left = "M100 117 C92 110 84 108 83 112 C82 115 82 119 83 122 C84 126 92 124 100 117 Z"
    right = "M100 117 C108 110 116 108 117 112 C118 115 118 119 117 122 C116 126 108 124 100 117 Z"
    return layer("Bow", [
        group("Wing", [path(left), fill(HAT)]),
        group("Wing", [path(right), fill(HAT)]),
        group("Shine", [path(left), shine((88, 112), 12, 0.25)]),
        group("Shine", [path(right), shine((112, 112), 12, 0.25)]),
        group("Knot", [ellipse(100, 117, 9, 10), fill(HAT_DARK)]),
    ], rig)


def bag(rig):
    sack = "M92 134 C78 138 72 150 76 157 C80 165 120 165 124 157 C128 150 122 138 108 134 Z"
    ruffle = "M92 134 C87 128 89 123 94 125 C96 121 104 121 106 125 C111 123 113 128 108 134 Z"
    track = {"r": per_mood(bag_rotation)}
    return layer("Bag", [
        group("Sack", [path(sack), fill(BAG)]),
        group("SackShade", [path(sack), shade((96, 146), 30, 0.45, 0.3)]),
        group("Ruffle", [path(ruffle), fill(BAG)]),
        group("RuffleShade", [path(ruffle), shade((100, 124), 14, 0.35, 0.4)]),
        group("String", [path("M90 134 Q100 137 110 134"), stroke(BAG_STRING, 3)]),
        group("Shine", [ellipse(88, 146, 7, 12), fill(WHITE, 0.14)]),
    ], rig, ks={"a": static([100, 134, 0]), "p": static([100, 134, 0]), **track})


def paws(rig):
    groups = []
    for x, angle in ((74, 22), (126, -22)):
        t = tr(p=(x, 144), a=(x, 144), r=angle)
        groups.append(group("Paw", [
            group("FurShape", [ellipse(x, 144, 22, 30), fur()]),
            group("Shade", [ellipse(x, 144, 22, 30), shade((x, 138), 18, 0.25, 0.3)]),
        ], t))
    return layer("Paws", groups, rig)


def coin(name, cx, cy, r):
    """Монетка-звезда — фирменная деталь любого убора."""
    return [
        group(name, [ellipse(cx, cy, r * 2, r * 2), fill(COIN_OUTER)]),
        group(f"{name}Inner", [ellipse(cx, cy, r * 1.45, r * 1.45), fill(COIN_INNER)]),
        group(f"{name}Star", [star(cx, cy + r * 0.05, 5, r * 0.56, r * 0.24), fill(COIN_OUTER)]),
    ]


def headwear_ks(tilt=True):
    ks = {"a": static([*HAT_PIVOT, 0]), "p": per_mood(hat_position)}
    if tilt:
        ks["r"] = per_mood(hat_rotation)
    return ks


def bowler(rig):
    crown = "M54 50 C54 28 72 14 100 14 C128 14 146 28 146 50 Z"
    return layer("Bowler", [
        group("Brim", [ellipse(100, 52, 132, 20), fill(HAT_DARK)]),
        group("BrimTop", [ellipse(100, 50, 124, 14), fill(HAT)]),
        group("Crown", [path(crown), fill(HAT)]),
        group("CrownShine", [path(crown), shine((78, 24), 60, 0.28)]),
        group("CrownShade", [path(crown), shade((100, 30), 52, 0.3, 0.5)]),
        group("Band", [rect(100, 44, 92, 10, 3), fill(HAT_BAND)]),
        group("BrimEdge", [path("M38 55 Q100 68 162 55"), stroke(HAT_LIGHT, 2.5, 0.45)]),
        *coin("Coin", 128, 33, 11),
    ], rig, ks=headwear_ks())


def cap(rig):
    crown = "M56 56 C56 30 76 16 100 16 C124 16 144 30 144 56 Z"
    visor = "M58 54 C76 62 124 62 142 54 C140 67 60 67 58 54 Z"
    return layer("Cap", [
        group("Crown", [path(crown), fill(CAP)]),
        group("CrownShine", [path(crown), shine((80, 26), 56, 0.3)]),
        group("CrownShade", [path(crown), shade((100, 34), 50, 0.3, 0.55)]),
        *[group("Seam", [path(d), stroke(CAP_DARK, 1.6, 0.6)])
          for d in ("M100 17 L100 56", "M76 21 C70 32 68 44 68 56", "M124 21 C130 32 132 44 132 56")],
        group("Button", [ellipse(100, 17, 9, 5), fill(CAP_DARK)]),
        group("Visor", [path(visor), fill(CAP_DARK)]),
        group("VisorEdge", [path("M62 58 C78 64 122 64 138 58"), stroke(CAP_LIGHT, 2, 0.5)]),
        *coin("Coin", 100, 38, 10),
    ], rig, ks=headwear_ks(), opacity=0)


def headphones(rig):
    band = "M34 86 C30 20 170 20 166 86"
    groups = [
        group("Band", [path(band), stroke(HAT, 9)]),
        group("BandShine", [path("M44 50 C58 28 142 28 156 50"), stroke(HAT_LIGHT, 2.5, 0.5)]),
    ]
    for x, pad in ((30, 42), (170, 158)):
        groups += [
            group("Pad", [rect(pad, 90, 8, 30, 4), fill(HAT)]),
            group("Cup", [rect(x, 90, 22, 40, 10), fill(PHONES)]),
            group("CupShine", [rect(x - 5, 84, 4, 20, 2), fill(PHONES_LIGHT, 0.6)]),
        ]
    groups += coin("Coin", 170, 92, 8)
    return layer("Headphones", groups, rig, ks=headwear_ks(tilt=False), opacity=0)


def sparkle(hat_layer):
    """Искорка у монетки котелка: один раз за сегмент, в самом конце петли её уже нет."""
    def scale(m):
        return [(0, [0, 0]), (78, [0, 0]), (86, [100, 100], "back"), (98, [0, 0]), (LOOP, [0, 0])]

    def rotation(m):
        return [(0, 0), (78, 0), (98, 45), (LOOP, 45)]

    cx, cy = 146, 22
    return layer("Sparkle", [group("Sparkle", [star(cx, cy, 4, 6, 1.4), fill(COIN_OUTER)])], hat_layer,
                 ks={"a": static([cx, cy, 0]), "p": static([cx, cy, 0]), "s": per_mood(scale),
                     "r": per_mood(rotation)})


def build():
    rig = layer("Rig", [], null=True, ks={
        "a": static([*GROUND, 0]),
        "p": per_mood(rig_position),
        "s": per_mood(rig_scale),
        "r": per_mood(rig_rotation),
    })
    bowler_layer = bowler(rig)
    # Снизу вверх.
    layers = [ground(), rig, feet(rig), body(rig)] + [face(m, rig) for m in MOODS] + [
        plaid(rig), bow(rig), bag(rig), paws(rig), headphones(rig), cap(rig), bowler_layer, sparkle(bowler_layer)]
    return {
        "v": "5.7.4", "fr": FPS, "ip": 0, "op": TOTAL, "w": W, "h": H, "nm": "Dzyn", "ddd": 0, "assets": [],
        "layers": list(reversed(layers)),
        "markers": [{"tm": seg(m), "cm": m, "dr": LOOP} for m in MOODS],
    }


def _round(o):
    if isinstance(o, float):
        r = round(o, 3)
        return int(r) if r == int(r) else r
    if isinstance(o, list):
        return [_round(x) for x in o]
    if isinstance(o, dict):
        return {k: _round(v) for k, v in o.items()}
    return o


if __name__ == "__main__":
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(_round(build()), ensure_ascii=False, separators=(",", ":")) + "\n")
    print(f"{OUT} ({OUT.stat().st_size // 1024} KB)")
