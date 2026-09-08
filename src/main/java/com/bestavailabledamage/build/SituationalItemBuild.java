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

import com.bestavailabledamage.data.EquipmentEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * For every table row the target satisfies and the player owns an item for: a weapon row
 * becomes a plain build around that weapon; any other slot is swapped into the top plain
 * build. Always on; no config toggle.
 */
public class SituationalItemBuild implements GuaranteedBuild
{
	@Override
	public List<Loadout> make(BuildContext ctx)
	{
		if (!ctx.getOptions().isSituationalBuilds())
		{
			return List.of();
		}
		List<Loadout> out = new ArrayList<>();
		for (SituationalItem row : SituationalItems.TABLE)
		{
			if (!row.getCondition().test(ctx.getTarget()))
			{
				continue;
			}
			List<EquipmentEntry> candidates = row.bestPerPrefix(ctx.getFiller(), ctx.getType());
			if (candidates.isEmpty())
			{
				continue;
			}
			Optional<Loadout> built = row.getSlot().equals("weapon")
				? firstUsableWeapon(candidates, ctx)
				: Optional.of(swap(ctx.topPlain(), row.getSlot(), candidates.get(0)));
			built.ifPresent(l -> out.add(l.guaranteed(row.getReason())));
		}
		return out;
	}

	/**
	 * The first candidate, in preference order, whose weapon style fits the attack type: a
	 * more preferred item (e.g. a Dragon hunter lance) can lack a style for this type, in
	 * which case the next-preferred owned item (e.g. the crossbow) is tried instead.
	 */
	private static Optional<Loadout> firstUsableWeapon(List<EquipmentEntry> candidates, BuildContext ctx)
	{
		for (EquipmentEntry item : candidates)
		{
			Optional<Loadout> built = ctx.getBuilder().loadoutFor(item, ctx);
			if (built.isPresent())
			{
				return built;
			}
		}
		return Optional.empty();
	}

	private static Loadout swap(Loadout base, String slot, EquipmentEntry item)
	{
		Map<String, EquipmentEntry> gear = new LinkedHashMap<>(base.getEquipment());
		gear.put(slot, item);
		return base.withEquipment(gear);
	}
}
