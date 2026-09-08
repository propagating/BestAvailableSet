import unittest

import fetch_calc_data as f


class TrimEquipmentTest(unittest.TestCase):
    def test_folds_alias_and_normalises_category(self):
        raw = [
            {"name": "Avernic defender", "id": 22322, "version": "Normal", "slot": "shield",
             "category": "", "speed": 0, "isTwoHanded": False,
             "bonuses": {"str": 8, "ranged_str": 0, "magic_str": 0, "prayer": 0},
             "offensive": {"stab": 30, "slash": 29, "crush": 28, "magic": -5, "ranged": -4},
             "defensive": {}},
            {"name": "Avernic defender", "id": 24186, "version": "Locked", "slot": "shield",
             "category": "blunt", "speed": 0, "isTwoHanded": False,
             "bonuses": {"str": 8, "ranged_str": 0, "magic_str": 0, "prayer": 0},
             "offensive": {"stab": 30, "slash": 29, "crush": 28, "magic": -5, "ranged": -4},
             "defensive": {}},
            {"name": "Bad", "id": 0, "version": "", "slot": "head", "category": "", "speed": 0,
             "isTwoHanded": False, "bonuses": {"str": 0, "ranged_str": 0, "magic_str": 0, "prayer": 0},
             "offensive": {"stab": 0, "slash": 0, "crush": 0, "magic": 0, "ranged": 0}, "defensive": {}},
        ]
        aliases = {"24186": 22322}
        out = f.trim_equipment(raw, aliases)
        self.assertEqual([e["id"] for e in out], [22322, 24186])
        self.assertEqual(out[0]["baseId"], 22322)
        self.assertEqual(out[1]["baseId"], 22322)
        self.assertEqual(out[1]["category"], "Blunt")
        self.assertEqual(out[0]["twoHanded"], False)
        self.assertEqual(out[0]["str"], 8)
        self.assertEqual(out[0]["stab"], 30)
        self.assertEqual(out[0]["prayer"], 0)
        self.assertEqual(set(out[0]), {"id", "baseId", "name", "version", "slot", "category",
                                       "twoHanded", "speed", "str", "rangedStr", "magicStr",
                                       "stab", "slash", "crush", "magic", "ranged", "prayer"})

    def test_prayer_bonus_is_kept(self):
        raw = [{"name": "Rada's blessing 4", "id": 22947, "version": "", "slot": "ammo",
                "category": "", "speed": 0, "isTwoHanded": False,
                "bonuses": {"str": 0, "ranged_str": 0, "magic_str": 0, "prayer": 2},
                "offensive": {"stab": 0, "slash": 0, "crush": 0, "magic": 0, "ranged": 0},
                "defensive": {}}]
        self.assertEqual(f.trim_equipment(raw, {})[0]["prayer"], 2)


class TrimMonstersTest(unittest.TestCase):
    def test_weakness_none_becomes_null(self):
        raw = [
            {"id": 8058, "name": "Vorkath", "version": "Dragon Slayer II", "level": 392,
             "weakness": {"element": "fire", "severity": 40}, "attributes": ["dragon"],
             "is_slayer_monster": True},
            {"id": 496, "name": "Kraken", "version": "Whirlpool", "level": 0,
             "weakness": {"element": "none", "severity": 0}, "attributes": [],
             "is_slayer_monster": True},
            {"id": 415, "name": "Abyssal demon", "version": "Standard", "level": 124,
             "weakness": None, "attributes": ["demon"], "is_slayer_monster": True},
        ]
        out = f.trim_monsters(raw)
        self.assertEqual(out[0]["weaknessElement"], "fire")
        self.assertEqual(out[0]["weaknessSeverity"], 40)
        self.assertIsNone(out[1]["weaknessElement"])
        self.assertIsNone(out[2]["weaknessElement"])
        self.assertEqual(out[2]["slayerMonster"], True)
        self.assertEqual(out[2]["attributes"], ["demon"])


class TrimSpellsTest(unittest.TestCase):
    def test_only_standard_elemental_with_levels(self):
        raw = [
            {"name": "Fire Surge", "max_hit": 24, "spellbook": "standard", "element": "fire"},
            {"name": "Wind Strike", "max_hit": 2, "spellbook": "standard", "element": "air"},
            {"name": "Ice Barrage", "max_hit": 30, "spellbook": "ancient", "element": None},
            {"name": "Crumble Undead", "max_hit": 15, "spellbook": "standard", "element": None},
        ]
        out = f.trim_spells(raw)
        self.assertEqual([(s["name"], s["level"], s["element"]) for s in out],
                         [("Fire Surge", 95, "fire"), ("Wind Strike", 1, "air")])


if __name__ == "__main__":
    unittest.main()
