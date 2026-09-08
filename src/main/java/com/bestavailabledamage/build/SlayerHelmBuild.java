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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * One build wearing the best owned slayer helmet for every slayer monster, exported with the
 * calculator's on-task toggle so the site applies the bonus. The label says "on task" only
 * when the client confirmed the current task matches the target.
 */
public class SlayerHelmBuild implements GuaranteedBuild
{
	public static final String ON_TASK = "Slayer helm, on task";
	public static final String IF_ON_TASK = "Slayer helm, if on task";
	static final String NAME_PREFIX = "slayer helmet";

	@Override
	public List<Loadout> make(BuildContext ctx)
	{
		if (!ctx.getOptions().isSlayerBuild() || !ctx.getTarget().isSlayerMonster())
		{
			return List.of();
		}
		EquipmentEntry helm = ctx.getFiller().best(ctx.getType(), e -> e.getSlot().equals("head")
			&& e.getName().toLowerCase(Locale.ROOT).startsWith(NAME_PREFIX));
		if (helm == null)
		{
			return List.of();
		}
		Loadout base = ctx.topPlain();
		Map<String, EquipmentEntry> gear = new LinkedHashMap<>(base.getEquipment());
		gear.put("head", helm);
		String reason = ctx.getOptions().isOnSlayerTask() ? ON_TASK : IF_ON_TASK;
		return List.of(base.withEquipment(gear).withOnSlayerTask(true).withReason(reason)
			.withName(base.getName() + " (" + reason + ")"));
	}
}
