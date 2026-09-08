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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The situational-item table applied to the target: weapon rows become weapon bases (a plain
 * build around that weapon, e.g. "vs demon" for Emberlight), other rows become slot overlays
 * (e.g. the Salve amulet in the neck for "vs undead"). Always on; no config toggle. On its own
 * (as a rule) it yields the bases as builds plus each overlay on the top plain weapon;
 * {@link LayeredBuilds} stacks the overlays onto every base.
 */
public class SituationalItemBuild implements GuaranteedBuild
{
	/** Applicable weapon rows, in table order, each as a plain build around the best usable owned weapon. */
	public List<WeaponBase> weaponBases(BuildContext ctx)
	{
		List<WeaponBase> out = new ArrayList<>();
		if (!ctx.getOptions().isSituationalBuilds())
		{
			return out;
		}
		for (SituationalItem row : SituationalItems.TABLE)
		{
			if (!row.getSlot().equals("weapon") || !row.getCondition().test(ctx.getTarget()))
			{
				continue;
			}
			firstUsableWeapon(row.bestPerPrefix(ctx.getFiller(), ctx.getType()), ctx)
				.ifPresent(l -> out.add(new WeaponBase(row.getReason(), l)));
		}
		return out;
	}

	/** Applicable non-weapon rows, in table order, each as a one-slot overlay with the preferred owned item. */
	public List<SlotOverlay> overlays(BuildContext ctx)
	{
		List<SlotOverlay> out = new ArrayList<>();
		if (!ctx.getOptions().isSituationalBuilds())
		{
			return out;
		}
		for (SituationalItem row : SituationalItems.TABLE)
		{
			if (row.getSlot().equals("weapon") || !row.getCondition().test(ctx.getTarget()))
			{
				continue;
			}
			List<EquipmentEntry> candidates = row.bestPerPrefix(ctx.getFiller(), ctx.getType());
			if (!candidates.isEmpty())
			{
				out.add(new SlotOverlay(row.getReason(), Map.of(row.getSlot(), candidates.get(0)), false));
			}
		}
		return out;
	}

	@Override
	public List<Loadout> make(BuildContext ctx)
	{
		List<Loadout> out = new ArrayList<>();
		for (SlotOverlay overlay : overlays(ctx))
		{
			out.add(overlay.applyTo(ctx.topPlain()).guaranteed(overlay.getReason()));
		}
		for (WeaponBase base : weaponBases(ctx))
		{
			out.add(base.getLoadout().guaranteed(base.getReason()));
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
}
