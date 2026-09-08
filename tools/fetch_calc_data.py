#!/usr/bin/env python3
"""Download the OSRS Wiki DPS calculator's data files and write the plugin's trimmed copies.

Standard library only. Run from anywhere:  python tools/fetch_calc_data.py [resource-dir]
"""
import json
import sys
import urllib.request
from pathlib import Path

CDN = "https://raw.githubusercontent.com/weirdgloop/osrs-dps-calc/main/cdn/json/"
USER_AGENT = "BestAvailableDamage-RuneLite-plugin/1.0 (data refresh script; contact: propagating@protonmail.com)"
DEFAULT_OUT = Path(__file__).resolve().parents[1] / "src/main/resources/com/bestavailabledamage"

# Standard spellbook level requirements, keyed by (element word in the name, tier).
SPELL_LEVELS = {
    ("Wind", "Strike"): 1, ("Water", "Strike"): 5, ("Earth", "Strike"): 9, ("Fire", "Strike"): 13,
    ("Wind", "Bolt"): 17, ("Water", "Bolt"): 23, ("Earth", "Bolt"): 29, ("Fire", "Bolt"): 35,
    ("Wind", "Blast"): 41, ("Water", "Blast"): 47, ("Earth", "Blast"): 53, ("Fire", "Blast"): 59,
    ("Wind", "Wave"): 62, ("Water", "Wave"): 65, ("Earth", "Wave"): 70, ("Fire", "Wave"): 75,
    ("Wind", "Surge"): 81, ("Water", "Surge"): 85, ("Earth", "Surge"): 90, ("Fire", "Surge"): 95,
}


def fetch(name):
    req = urllib.request.Request(CDN + name, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def normalise_category(category):
    """The source has one lowercase 'blunt'; the calc's canonical spelling is title case."""
    if not category:
        return ""
    return category[0].upper() + category[1:]


def trim_equipment(raw, aliases):
    out = []
    for e in raw:
        item_id = int(e["id"])
        if item_id <= 0:
            continue
        bonuses = e["bonuses"]
        offensive = e["offensive"]
        out.append({
            "id": item_id,
            "baseId": int(aliases.get(str(item_id), item_id)),
            "name": e["name"],
            "version": e.get("version") or "",
            "slot": e["slot"],
            "category": normalise_category(e.get("category")),
            "twoHanded": bool(e.get("isTwoHanded")),
            "speed": int(e.get("speed") or 0),
            "str": int(bonuses["str"]),
            "rangedStr": int(bonuses["ranged_str"]),
            "magicStr": int(bonuses["magic_str"]),
            "stab": int(offensive["stab"]),
            "slash": int(offensive["slash"]),
            "crush": int(offensive["crush"]),
            "magic": int(offensive["magic"]),
            "ranged": int(offensive["ranged"]),
            "prayer": int(bonuses.get("prayer") or 0),
        })
    return out


def trim_monsters(raw):
    out = []
    for m in raw:
        monster_id = int(m["id"])
        if monster_id <= 0:
            continue
        weakness = m.get("weakness") or {}
        element = weakness.get("element")
        severity = int(weakness.get("severity") or 0)
        if not element or element == "none" or severity <= 0:
            element = None
            severity = 0
        out.append({
            "id": monster_id,
            "name": m["name"],
            "version": m.get("version") or "",
            "level": int(m.get("level") or 0),
            "weaknessElement": element,
            "weaknessSeverity": severity,
            "attributes": list(m.get("attributes") or []),
            "slayerMonster": bool(m.get("is_slayer_monster")),
            "magicLevel": int((m.get("skills") or {}).get("magic") or 0),
        })
    return out


def trim_spells(raw):
    out = []
    for s in raw:
        if s.get("spellbook") != "standard" or not s.get("element"):
            continue
        parts = s["name"].split(" ")
        if len(parts) != 2 or (parts[0], parts[1]) not in SPELL_LEVELS:
            continue
        out.append({
            "name": s["name"],
            "maxHit": int(s["max_hit"]),
            "element": s["element"],
            "level": SPELL_LEVELS[(parts[0], parts[1])],
        })
    return out


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)
        fh.write("\n")


def main(argv):
    out_dir = Path(argv[1]) if len(argv) > 1 else DEFAULT_OUT
    equipment = trim_equipment(fetch("equipment.json"), fetch("equipment_aliases.json"))
    monsters = trim_monsters(fetch("monsters.json"))
    spells = trim_spells(fetch("spells.json"))
    write_json(out_dir / "equipment.json", equipment)
    write_json(out_dir / "monsters.json", monsters)
    write_json(out_dir / "spells.json", spells)
    print(f"equipment: {len(equipment)} rows, monsters: {len(monsters)} rows, spells: {len(spells)} rows -> {out_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
