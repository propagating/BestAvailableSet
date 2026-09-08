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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * A complete Void set for the attack type as a four-slot overlay: the matching helm, a top,
 * a robe and the gloves, Elite pieces preferred. The set bonus is not in any listed stat, so
 * the plain ranking never picks it; the calculator applies it from the item ids. On its own
 * (as a rule) it yields one build on the top plain weapon; {@link LayeredBuilds} puts it on
 * the best weapon base together with any overlay that does not clash.
 */
public class VoidBuild implements GuaranteedBuild
{
	public static final String VOID = "Void";
	public static final String ELITE_VOID = "Elite Void";

	public Optional<SlotOverlay> overlay(BuildContext ctx)
	{
		if (!ctx.getOptions().isVoidBuild())
		{
			return Optional.empty();
		}
		AttackType type = ctx.getType();
		EquipmentEntry helm = named(ctx, "head", helmName(type));
		EquipmentEntry eliteTop = named(ctx, "body", "elite void top");
		EquipmentEntry top = eliteTop != null ? eliteTop : named(ctx, "body", "void knight top");
		EquipmentEntry eliteRobe = named(ctx, "legs", "elite void robe");
		EquipmentEntry robe = eliteRobe != null ? eliteRobe : named(ctx, "legs", "void knight robe");
		EquipmentEntry gloves = named(ctx, "hands", "void knight gloves");
		if (helm == null || top == null || robe == null || gloves == null)
		{
			return Optional.empty();
		}
		String reason = eliteTop != null && eliteRobe != null ? ELITE_VOID : VOID;
		return Optional.of(new SlotOverlay(reason,
			Map.of("head", helm, "body", top, "legs", robe, "hands", gloves), false));
	}

	@Override
	public List<Loadout> make(BuildContext ctx)
	{
		return overlay(ctx)
			.map(o -> List.of(o.applyTo(ctx.topPlain()).guaranteed(o.getReason())))
			.orElse(List.of());
	}

	private static String helmName(AttackType type)
	{
		switch (type)
		{
			case RANGED:
				return "void ranger helm";
			case MAGIC:
				return "void mage helm";
			default:
				return "void melee helm";
		}
	}

	/** Best owned item in the slot whose name starts with the prefix (covers (or) and Locked variants). */
	private static EquipmentEntry named(BuildContext ctx, String slot, String prefix)
	{
		return ctx.getFiller().best(ctx.getType(), e -> e.getSlot().equals(slot)
			&& e.getName().toLowerCase(Locale.ROOT).startsWith(prefix));
	}
}
