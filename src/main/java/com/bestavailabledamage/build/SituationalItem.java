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
import com.bestavailabledamage.data.MonsterEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import lombok.Value;

/** One row of the situational-item table: which items, which slot, and when they matter. */
@Value
public class SituationalItem
{
	String reason;
	String slot;
	List<String> namePrefixes;
	Predicate<MonsterEntry> condition;

	public boolean matches(EquipmentEntry e)
	{
		if (!e.getSlot().equals(slot))
		{
			return false;
		}
		String name = e.getName().toLowerCase(Locale.ROOT);
		for (String prefix : namePrefixes)
		{
			if (name.startsWith(prefix))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * The best owned item for each prefix that has one, in {@code namePrefixes} preference
	 * order (a prefix with no owned match is skipped, not left as a hole). A weapon row can
	 * have several usable prefixes: the caller tries each in turn since the first, most
	 * preferred one is not guaranteed to fit the attack type (e.g. a Dragon hunter lance has
	 * no ranged style, so a ranged build must fall through to the crossbow). Empty when
	 * nothing owned matches any prefix.
	 */
	public List<EquipmentEntry> bestPerPrefix(SlotFiller filler, AttackType type)
	{
		List<EquipmentEntry> items = new ArrayList<>();
		for (String prefix : namePrefixes)
		{
			EquipmentEntry item = filler.best(type, e -> e.getSlot().equals(slot)
				&& e.getName().toLowerCase(Locale.ROOT).startsWith(prefix));
			if (item != null)
			{
				items.add(item);
			}
		}
		return items;
	}
}
