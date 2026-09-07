# Best Available Damage

A RuneLite plugin that remembers the gear you own (inventory, equipment, bank
and group storage), builds up to six loadouts for an attack type and a target
monster, and opens them in the
[OSRS Wiki DPS calculator](https://tools.runescape.wiki/osrs-dps/) where the
real damage numbers are computed.

## How it works

- Every time the client shows a container we record which item ids it holds,
  per account, in `.runelite/best-available-damage/`. Open your bank once and
  the snapshot survives logging out.
- Pick an attack type (stab, slash, crush, ranged, magic) and search for a
  target. **Build loadouts** ranks the weapons you own that can use that type
  by `(strength bonus + 64) / attack speed` and gives each of the top six its
  own loadout, filling the other slots with your best item by strength then
  accuracy. Two-handed weapons empty the shield slot; bows get your best
  arrows, crossbows your best bolts, a blowpipe your best darts.
- For magic against a target with an element weakness, at least three
  loadouts use your non-powered staves with the strongest standard-spellbook
  spell of that element you can cast (and a matching tome if you own one),
  and the remaining slots (up to six in total) use powered staves; if you own
  fewer than three of one kind, the other kind fills the gap.
- **Open in wiki DPS calc** sends the loadouts, your base levels, the best
  offensive prayer you can use and the target to the calculator's share-link
  service and opens the link. This is off by default; enable *Export to wiki
  calc* in the plugin settings. Prayers, potions and boss-specific inputs can
  be adjusted on the site.

The plugin does not compute damage itself. It only decides which loadouts are
worth comparing.

## Known limits

- Gear whose value is situational and not in its listed bonuses (Void, Slayer
  helmet, Salve amulet) is never picked. Use the calculator's own toggles.
- Ammunition is matched by kind only (arrows, bolts, darts), not by what the
  weapon can actually fire.
- Rune ownership and non-standard spellbooks are not considered.
- Piety and Chivalry are assumed unlocked when your Prayer level allows.

## Refreshing the wiki data

```
python tools/fetch_calc_data.py
python -m unittest discover -s tools
```

Standard library only. Commit the regenerated JSON files under
`src/main/resources/com/bestavailabledamage/`.

## Developing

```
./gradlew test      # unit tests
./gradlew run       # dev client with the plugin loaded
```

## Configuration

- **Export to wiki calc** (default: off) - allow the export button to create
  a share link on tools.runescape.wiki. Submits your IP address to that server.
