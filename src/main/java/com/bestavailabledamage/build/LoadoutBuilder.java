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
package com.bestavailabledamage.build;

import com.bestavailabledamage.data.AttackType;
import com.bestavailabledamage.data.CombatStyle;
import com.bestavailabledamage.data.EquipmentCatalog;
import com.bestavailabledamage.data.EquipmentEntry;
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.data.SpellCatalog;
import com.bestavailabledamage.data.SpellEntry;
import com.bestavailabledamage.data.WeaponStyles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Turns "what I own" plus an attack type and a target into up to six loadouts: first the
 * builds guaranteed by the registered rules, then plain ranked builds. Pure.
 */
public class LoadoutBuilder
{
	public static final int MAX_LOADOUTS = 6;
	public static final List<String> SLOTS = List.of(
		"head", "cape", "neck", "ammo", "weapon", "body", "shield", "legs", "hands", "feet", "ring");

	private final EquipmentCatalog catalog;
	private final SpellCatalog spells;
	private final WeaponRanker ranker;
	private final List<GuaranteedBuild> guaranteedBuilds;

	public LoadoutBuilder(EquipmentCatalog catalog, SpellCatalog spells, WeaponRanker ranker)
	{
		this(catalog, spells, ranker, standardBuilds());
	}

	public LoadoutBuilder(EquipmentCatalog catalog, SpellCatalog spells, WeaponRanker ranker,
		List<GuaranteedBuild> guaranteedBuilds)
	{
		this.catalog = catalog;
		this.spells = spells;
		this.ranker = ranker;
		this.guaranteedBuilds = List.copyOf(guaranteedBuilds);
	}

	/** The shipped rules, in the order their builds appear: situational, slayer, Void, budget. */
	public static List<GuaranteedBuild> standardBuilds()
	{
		return List.of(new SituationalItemBuild(), new SlayerHelmBuild(), new VoidBuild(), new BudgetBuild());
	}

	/** Plain ranked builds only; registered guaranteed builds are not consulted at all. */
	public List<Loadout> build(Set<Integer> ownedIds, AttackType type, MonsterEntry target, int magicLevel)
	{
		List<EquipmentEntry> owned = new ArrayList<>();
		for (Integer id : ownedIds)
		{
			catalog.byId(id).ifPresent(owned::add);
		}
		return plainLoadouts(owned, type, target, magicLevel);
	}

	public List<Loadout> build(Set<Integer> ownedIds, AttackType type, MonsterEntry target, int magicLevel,
		BuildOptions options)
	{
		List<EquipmentEntry> owned = new ArrayList<>();
		for (Integer id : ownedIds)
		{
			catalog.byId(id).ifPresent(owned::add);
		}
		List<Loadout> plain = plainLoadouts(owned, type, target, magicLevel);
		if (plain.isEmpty())
		{
			return plain;
		}
		BuildContext ctx = new BuildContext(owned, type, target, magicLevel, options, plain,
			new SlotFiller(owned), this);

		List<Loadout> out = new ArrayList<>();
		for (GuaranteedBuild rule : guaranteedBuilds)
		{
			for (Loadout candidate : rule.make(ctx))
			{
				if (out.size() < MAX_LOADOUTS - 1 && out.stream().noneMatch(candidate::sameGearAs))
				{
					out.add(candidate);
				}
			}
		}
		// re-ranked without the cap that dropped `plain` at MAX_LOADOUTS, so a plain build
		// that turns out to duplicate a guaranteed one does not cost the fill a slot
		List<Loadout> fill = plainLoadouts(owned, type, target, magicLevel, MAX_LOADOUTS + out.size());
		for (Loadout loadout : fill)
		{
			if (out.size() >= MAX_LOADOUTS)
			{
				break;
			}
			if (out.stream().noneMatch(loadout::sameGearAs))
			{
				out.add(loadout);
			}
		}
		return out;
	}

	/** The v1 algorithm: ranked weapons, one loadout each, magic split by element weakness. */
	public List<Loadout> plainLoadouts(List<EquipmentEntry> owned, AttackType type, MonsterEntry target, int magicLevel)
	{
		return plainLoadouts(owned, type, target, magicLevel, MAX_LOADOUTS);
	}

	/** As above, capped at {@code limit} loadouts instead of always {@link #MAX_LOADOUTS}. */
	public List<Loadout> plainLoadouts(List<EquipmentEntry> owned, AttackType type, MonsterEntry target,
		int magicLevel, int limit)
	{
		SlotFiller filler = new SlotFiller(owned);
		List<EquipmentEntry> weapons = rankedWeapons(owned, type);

		Optional<String> element = type == AttackType.MAGIC ? target.elementWeakness() : Optional.empty();
		Optional<SpellEntry> spell = element.flatMap(el -> spells.strongest(el, magicLevel));

		List<Loadout> out = new ArrayList<>();
		if (!spell.isPresent())
		{
			for (EquipmentEntry weapon : weapons)
			{
				if (out.size() >= limit)
				{
					break;
				}
				out.add(loadout(weapon, type, filler, null, null));
			}
			return out;
		}

		// elemental group on castable staves, then powered staves, half and half (spill over)
		List<EquipmentEntry> casters = new ArrayList<>();
		List<EquipmentEntry> powered = new ArrayList<>();
		for (EquipmentEntry w : weapons)
		{
			if (isPoweredStaff(w))
			{
				powered.add(w);
			}
			else if (isElementalCaster(w))
			{
				casters.add(w);
			}
			// other non-powered magic-capable weapons (e.g. Salamanders) offer a magic style
			// but cannot autocast a spell, so they are excluded from the elemental split here;
			// they still appear in the single-group path above when there is no weakness.
		}
		int half = limit / 2;
		int casterCount = Math.min(casters.size(), Math.max(half, limit - powered.size()));
		int poweredCount = Math.min(powered.size(), limit - casterCount);
		for (int i = 0; i < casterCount; i++)
		{
			out.add(loadout(casters.get(i), type, filler, element.get(), spell.get()));
		}
		for (int i = 0; i < poweredCount; i++)
		{
			out.add(loadout(powered.get(i), type, filler, null, null));
		}
		return out;
	}

	/**
	 * A plain loadout built around one specific owned weapon (for rules that guarantee a
	 * weapon), with the same spell logic as {@link #plainLoadouts}. Empty when the weapon
	 * cannot use the attack type.
	 */
	public Optional<Loadout> loadoutFor(EquipmentEntry weapon, BuildContext ctx)
	{
		if (!weapon.getSlot().equals("weapon") || cannotAttack(weapon)
			|| !WeaponStyles.bestStyleFor(weapon.getCategory(), ctx.getType()).isPresent())
		{
			return Optional.empty();
		}
		Optional<String> element = ctx.getType() == AttackType.MAGIC && isElementalCaster(weapon)
			? ctx.getTarget().elementWeakness() : Optional.empty();
		Optional<SpellEntry> spell = element.flatMap(el -> spells.strongest(el, ctx.getMagicLevel()));
		return Optional.of(loadout(weapon, ctx.getType(), ctx.getFiller(),
			spell.isPresent() ? element.get() : null, spell.orElse(null)));
	}

	private static boolean isPoweredStaff(EquipmentEntry weapon)
	{
		String c = weapon.getCategory();
		return c.equalsIgnoreCase("Powered Staff") || c.equalsIgnoreCase("Powered Wand");
	}

	/** Pool A of the elemental split: weapons that can attach a standard-spellbook spell. */
	private static boolean isElementalCaster(EquipmentEntry weapon)
	{
		String c = weapon.getCategory();
		return c.equalsIgnoreCase("Staff") || c.equalsIgnoreCase("Bladed Staff");
	}

	/**
	 * Weapon-slot items that cannot attack: the calculator's list carries every equippable
	 * item (greegrees, tools, cosmetics) under "Unarmed" with a speed of 0 or -1.
	 */
	static boolean cannotAttack(EquipmentEntry weapon)
	{
		return weapon.getCategory().equalsIgnoreCase("Unarmed") || weapon.getSpeed() < 1;
	}

	/** Owned weapons that can attack with the type, best first, one per base item, no darts. */
	private List<EquipmentEntry> rankedWeapons(List<EquipmentEntry> owned, AttackType type)
	{
		Map<Integer, EquipmentEntry> byBase = new HashMap<>();
		Comparator<EquipmentEntry> order = Comparator
			.comparingDouble((EquipmentEntry w) -> ranker.score(w, type))
			.thenComparing(SlotFiller.preference(type));
		for (EquipmentEntry w : owned)
		{
			if (!w.getSlot().equals("weapon") || SlotFiller.isDart(w) || cannotAttack(w)
				|| !WeaponStyles.bestStyleFor(w.getCategory(), type).isPresent())
			{
				continue;
			}
			EquipmentEntry current = byBase.get(w.getBaseId());
			if (current == null || order.compare(w, current) > 0)
			{
				byBase.put(w.getBaseId(), w);
			}
		}
		List<EquipmentEntry> ranked = new ArrayList<>(byBase.values());
		ranked.sort(order.reversed());
		return ranked;
	}

	private Loadout loadout(EquipmentEntry weapon, AttackType type, SlotFiller filler,
		String element, SpellEntry spell)
	{
		CombatStyle style = WeaponStyles.bestStyleFor(weapon.getCategory(), type).get();
		Map<String, EquipmentEntry> gear = filler.fill(weapon, type, element);
		EquipmentEntry dart = weapon.getName().startsWith(SlotFiller.BLOWPIPE) ? filler.bestDart() : null;
		String name = spell == null ? weapon.getName() : weapon.getName() + " + " + spell.getName();
		return new Loadout(name, style, gear, dart, spell, false, null);
	}
}
