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
import com.bestavailabledamage.data.EquipmentEntry;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/** Fills every non-weapon slot with the best owned item for the attack type. */
public class SlotFiller
{
	static final String BLOWPIPE = "Toxic blowpipe";

	private final List<EquipmentEntry> owned;

	public SlotFiller(List<EquipmentEntry> owned)
	{
		this.owned = owned;
	}

	/**
	 * @param element the target's weakness element (lowercase, e.g. "fire") when this is an
	 *                elemental magic loadout, else null
	 */
	public Map<String, EquipmentEntry> fill(EquipmentEntry weapon, AttackType type, String element)
	{
		Map<String, EquipmentEntry> gear = new LinkedHashMap<>();
		for (String slot : LoadoutBuilder.SLOTS)
		{
			gear.put(slot, null);
		}
		gear.put("weapon", weapon);
		for (String slot : LoadoutBuilder.SLOTS)
		{
			if (slot.equals("weapon"))
			{
				continue;
			}
			if (slot.equals("shield") && weapon.isTwoHanded())
			{
				continue;
			}
			if (slot.equals("ammo"))
			{
				gear.put("ammo", ammoFor(weapon, type));
				continue;
			}
			if (slot.equals("shield") && element != null)
			{
				EquipmentEntry tome = best(type, e -> e.getSlot().equals("shield")
					&& e.getName().toLowerCase(Locale.ROOT).equals("tome of " + element));
				if (tome != null)
				{
					gear.put("shield", tome);
					continue;
				}
			}
			gear.put(slot, best(type, e -> e.getSlot().equals(slot)));
		}
		return gear;
	}

	/** Best owned dart for a blowpipe, or null. Darts are Thrown weapons whose name ends in "dart". */
	public EquipmentEntry bestDart()
	{
		return best(AttackType.RANGED, SlotFiller::isDart);
	}

	static boolean isDart(EquipmentEntry e)
	{
		return e.getSlot().equals("weapon") && e.getCategory().equalsIgnoreCase("Thrown")
			&& e.getName().toLowerCase(Locale.ROOT).endsWith("dart");
	}

	private EquipmentEntry ammoFor(EquipmentEntry weapon, AttackType type)
	{
		if (type != AttackType.RANGED)
		{
			return null;
		}
		String category = weapon.getCategory();
		if (category.equalsIgnoreCase("Bow"))
		{
			return best(type, e -> e.getSlot().equals("ammo")
				&& e.getName().toLowerCase(Locale.ROOT).matches(".*arrows?$"));
		}
		if (category.equalsIgnoreCase("Crossbow"))
		{
			return best(type, e -> e.getSlot().equals("ammo")
				&& e.getName().toLowerCase(Locale.ROOT).contains("bolt"));
		}
		return null;
	}

	/**
	 * Highest strength for the type, then highest accuracy, then alphabetical name, then the
	 * base item over an alias (so "Avernic defender" beats "Avernic defender (Locked)"), then
	 * the lower id. The order is total so results are deterministic.
	 */
	static Comparator<EquipmentEntry> preference(AttackType type)
	{
		return Comparator
			.comparingInt((EquipmentEntry e) -> type.strengthOf(e))
			.thenComparingInt(type::accuracyOf)
			.thenComparing(EquipmentEntry::getName, Comparator.reverseOrder())
			.thenComparingInt(e -> e.getId() == e.getBaseId() ? 1 : 0)
			.thenComparing(Comparator.comparingInt(EquipmentEntry::getId).reversed());
	}

	private EquipmentEntry best(AttackType type, Predicate<EquipmentEntry> filter)
	{
		Comparator<EquipmentEntry> order = preference(type);
		EquipmentEntry best = null;
		for (EquipmentEntry e : owned)
		{
			if (!filter.test(e))
			{
				continue;
			}
			if (best == null || order.compare(e, best) > 0)
			{
				best = e;
			}
		}
		return best;
	}
}
