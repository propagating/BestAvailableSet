/*
 * Copyright (c) 2024, Ryan Richardson <rdrichardson@chapman.edu>
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
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.bestavailabledamage.build;

import com.bestavailabledamage.data.EquipmentEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * The best plain build using only items at or under the configured GE price. A price of 0
 * (untradeable, or not priced yet this session) counts as within the cap.
 */
public class BudgetBuild implements GuaranteedBuild
{
	@Override
	public List<Loadout> make(BuildContext ctx)
	{
		BuildOptions options = ctx.getOptions();
		if (!options.isBudgetBuild())
		{
			return List.of();
		}
		int cap = options.getBudgetMaxPrice();
		List<EquipmentEntry> cheap = new ArrayList<>();
		for (EquipmentEntry e : ctx.getOwned())
		{
			if (price(options.getItemPrice(), e.getId()) <= cap)
			{
				cheap.add(e);
			}
		}
		List<Loadout> plain = ctx.getBuilder().plainLoadouts(cheap, ctx.getType(), ctx.getTarget(), ctx.getMagicLevel());
		if (plain.isEmpty())
		{
			return List.of();
		}
		Loadout base = plain.get(0);
		String reason = "Budget, under " + formatGp(cap);
		return List.of(base.withReason(reason).withName(base.getName() + " (" + reason + ")"));
	}

	private static int price(IntUnaryOperator lookup, int id)
	{
		try
		{
			return Math.max(0, lookup.applyAsInt(id));
		}
		catch (RuntimeException e)
		{
			return 0;
		}
	}

	/** 100000 → "100k", 2000000 → "2m", 2500000 → "2.5m", 1500 → "1500 gp". */
	static String formatGp(int gp)
	{
		if (gp >= 1_000_000 && gp % 100_000 == 0)
		{
			return gp % 1_000_000 == 0 ? (gp / 1_000_000) + "m" : String.format("%.1fm", gp / 1_000_000.0);
		}
		if (gp >= 1_000 && gp % 1_000 == 0)
		{
			return (gp / 1_000) + "k";
		}
		return gp + " gp";
	}
}
