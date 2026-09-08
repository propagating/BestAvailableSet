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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stacks the slot overlays (Salve, slayer helmet) onto every weapon base (each situational
 * weapon, then the top ranked weapon), adds a Void build on the first base (the first
 * situational weapon in table order, or the top ranked weapon when none applies) with
 * whatever overlays still fit, and finally the situational weapons on their own. The result is one
 * card per meaningful combination, e.g. "Emberlight (vs demon, Slayer helm, on task)",
 * rather than a card per rule that never combine.
 */
public class LayeredBuilds implements GuaranteedBuild
{
	private final SituationalItemBuild situational = new SituationalItemBuild();
	private final SlayerHelmBuild slayerHelm = new SlayerHelmBuild();
	private final VoidBuild voidSet = new VoidBuild();

	@Override
	public List<Loadout> make(BuildContext ctx)
	{
		List<WeaponBase> bases = new ArrayList<>(situational.weaponBases(ctx));
		Loadout top = ctx.topPlain();
		if (bases.stream().noneMatch(b -> b.getLoadout().weapon().getId() == top.weapon().getId()))
		{
			bases.add(new WeaponBase(null, top));
		}

		// stacking is greedy in list order, so an earlier overlay wins a slot clash: the table's
		// rows (none of which touch the head today) come first and the helm is appended last
		List<SlotOverlay> overlays = new ArrayList<>(situational.overlays(ctx));
		slayerHelm.overlay(ctx).ifPresent(overlays::add);
		Optional<SlotOverlay> voidOverlay = voidSet.overlay(ctx);

		List<Loadout> out = new ArrayList<>();
		List<SlotOverlay> stack = stackable(overlays, List.of());
		if (!stack.isEmpty())
		{
			for (WeaponBase base : bases)
			{
				out.add(compose(base, stack));
			}
		}
		if (voidOverlay.isPresent())
		{
			out.add(compose(bases.get(0), stackable(overlays, List.of(voidOverlay.get()))));
		}
		for (WeaponBase base : bases)
		{
			if (base.getReason() != null)
			{
				out.add(base.getLoadout().guaranteed(base.getReason()));
			}
		}
		return out;
	}

	/** {@code seed} first, then every overlay that does not clash with what is already stacked. */
	private static List<SlotOverlay> stackable(List<SlotOverlay> overlays, List<SlotOverlay> seed)
	{
		List<SlotOverlay> stack = new ArrayList<>(seed);
		for (SlotOverlay overlay : overlays)
		{
			if (stack.stream().noneMatch(overlay::clashesWith))
			{
				stack.add(overlay);
			}
		}
		return stack;
	}

	private static Loadout compose(WeaponBase base, List<SlotOverlay> stack)
	{
		Loadout loadout = base.getLoadout();
		List<String> reasons = new ArrayList<>();
		if (base.getReason() != null)
		{
			reasons.add(base.getReason());
		}
		for (SlotOverlay overlay : stack)
		{
			loadout = overlay.applyTo(loadout);
			reasons.add(overlay.getReason());
		}
		return loadout.guaranteed(String.join(", ", reasons));
	}
}
