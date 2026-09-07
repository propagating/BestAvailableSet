/*
 * Copyright (c) 2026, propagating <propagating@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.bestavailabledamage.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import static com.bestavailabledamage.data.AttackType.CRUSH;
import static com.bestavailabledamage.data.AttackType.MAGIC;
import static com.bestavailabledamage.data.AttackType.RANGED;
import static com.bestavailabledamage.data.AttackType.SLASH;
import static com.bestavailabledamage.data.AttackType.STAB;

/** Which attack types each weapon category offers, and under which style name and stance. */
public final class WeaponStyles
{
	private static final Map<String, List<CombatStyle>> TABLE = new HashMap<>();

	private static CombatStyle s(String name, AttackType type, String stance)
	{
		return new CombatStyle(name, type, stance);
	}

	private static void put(List<CombatStyle> styles, String... categories)
	{
		for (String c : categories)
		{
			TABLE.put(c.toLowerCase(Locale.ROOT), styles);
		}
	}

	static
	{
		put(List.of(s("Chop", SLASH, "Accurate"), s("Slash", SLASH, "Aggressive"),
			s("Smash", CRUSH, "Aggressive"), s("Block", SLASH, "Defensive")), "2h Sword");
		put(List.of(s("Chop", SLASH, "Accurate"), s("Hack", SLASH, "Aggressive"),
			s("Smash", CRUSH, "Aggressive"), s("Block", SLASH, "Defensive")), "Axe");
		put(List.of(s("Lunge", STAB, "Accurate"), s("Swipe", SLASH, "Aggressive"),
			s("Pound", CRUSH, "Controlled"), s("Block", STAB, "Defensive")), "Banner");
		put(List.of(s("Jab", STAB, "Accurate"), s("Swipe", SLASH, "Aggressive"),
			s("Fend", CRUSH, "Defensive"), s("Spell", MAGIC, "Defensive Autocast"),
			s("Spell", MAGIC, "Autocast")), "Bladed Staff");
		put(List.of(s("Flare", RANGED, "Accurate"), s("Blaze", RANGED, "Rapid"),
			s("Blast", RANGED, "Longrange")), "Blaster");
		put(List.of(s("Pound", CRUSH, "Aggressive"), s("Pummel", CRUSH, "Aggressive"),
			s("Smash", CRUSH, "Aggressive")), "Bludgeon");
		put(List.of(s("Pound", CRUSH, "Accurate"), s("Pummel", CRUSH, "Aggressive"),
			s("Block", CRUSH, "Defensive")), "Blunt", "Polestaff");
		put(List.of(s("Accurate", RANGED, "Accurate"), s("Rapid", RANGED, "Rapid"),
			s("Longrange", RANGED, "Longrange")), "Bow", "Crossbow", "Thrown");
		put(List.of(s("Pummel", CRUSH, "Accurate")), "Bulwark");
		put(List.of(s("Short fuse", RANGED, "Accurate"), s("Medium fuse", RANGED, "Rapid"),
			s("Long fuse", RANGED, "Longrange")), "Chinchompas");
		put(List.of(s("Chop", SLASH, "Accurate"), s("Slash", SLASH, "Aggressive"),
			s("Lunge", STAB, "Controlled"), s("Block", SLASH, "Defensive")), "Claw", "Slash Sword");
		put(List.of(s("Stab", STAB, "Accurate"), s("Lunge", STAB, "Aggressive"),
			s("Slash", SLASH, "Aggressive"), s("Block", STAB, "Defensive")), "Dagger", "Stab Sword");
		put(List.of(s("Pound", CRUSH, "Accurate"), s("Pummel", CRUSH, "Aggressive"),
			s("Spike", STAB, "Controlled"), s("Block", CRUSH, "Defensive")), "Flail", "Spiked");
		put(List.of(s("Kick", CRUSH, "Aggressive")), "Gun");
		put(List.of(s("Poke", STAB, "Accurate"), s("Slash", SLASH, "Aggressive"),
			s("Pound", CRUSH, "Aggressive"), s("Block", SLASH, "Defensive")), "Multi-Melee");
		put(List.of(s("Stab", STAB, "Accurate"), s("Lunge", STAB, "Aggressive"),
			s("Pound", CRUSH, "Aggressive"), s("Block", STAB, "Defensive")), "Partisan");
		put(List.of(s("Spike", STAB, "Accurate"), s("Impale", STAB, "Aggressive"),
			s("Smash", CRUSH, "Aggressive"), s("Block", STAB, "Defensive")), "Pickaxe");
		put(List.of(s("Jab", STAB, "Controlled"), s("Swipe", SLASH, "Aggressive"),
			s("Fend", STAB, "Defensive")), "Polearm");
		put(List.of(s("Accurate", MAGIC, "Accurate"), s("Longrange", MAGIC, "Longrange")),
			"Powered Staff", "Powered Wand");
		put(List.of(s("Scorch", SLASH, "Aggressive"), s("Flare", RANGED, "Rapid"),
			s("Blaze", MAGIC, "Defensive")), "Salamander");
		put(List.of(s("Reap", SLASH, "Accurate"), s("Chop", SLASH, "Aggressive"),
			s("Jab", CRUSH, "Aggressive"), s("Block", SLASH, "Defensive")), "Scythe");
		put(List.of(s("Lunge", STAB, "Controlled"), s("Swipe", SLASH, "Controlled"),
			s("Pound", CRUSH, "Controlled"), s("Block", STAB, "Defensive")), "Spear");
		put(List.of(s("Bash", CRUSH, "Accurate"), s("Pound", CRUSH, "Aggressive"),
			s("Focus", CRUSH, "Defensive"), s("Spell", MAGIC, "Defensive Autocast"),
			s("Spell", MAGIC, "Autocast")), "Staff");
		put(List.of(s("Punch", CRUSH, "Accurate"), s("Kick", CRUSH, "Aggressive"),
			s("Block", CRUSH, "Defensive")), "Unarmed");
		put(List.of(s("Flick", SLASH, "Accurate"), s("Lash", SLASH, "Controlled"),
			s("Deflect", SLASH, "Defensive")), "Whip");
	}

	private WeaponStyles()
	{
	}

	public static List<CombatStyle> stylesFor(String category)
	{
		if (category == null)
		{
			return Collections.emptyList();
		}
		return TABLE.getOrDefault(category.toLowerCase(Locale.ROOT), Collections.emptyList());
	}

	/** The style to export for this category and attack type, preferring the offensive stance. */
	public static Optional<CombatStyle> bestStyleFor(String category, AttackType type)
	{
		CombatStyle first = null;
		for (CombatStyle style : stylesFor(category))
		{
			if (style.getType() != type)
			{
				continue;
			}
			if (first == null)
			{
				first = style;
			}
			if (isPreferredStance(type, style.getStance()))
			{
				return Optional.of(style);
			}
		}
		return Optional.ofNullable(first);
	}

	private static boolean isPreferredStance(AttackType type, String stance)
	{
		switch (type)
		{
			case RANGED:
				return "Rapid".equals(stance);
			case MAGIC:
				return "Autocast".equals(stance) || "Accurate".equals(stance);
			default:
				return "Aggressive".equals(stance);
		}
	}
}
