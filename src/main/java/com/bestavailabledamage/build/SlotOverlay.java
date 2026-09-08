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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Value;

/**
 * A swap of one or more slots that the target or the player's kit makes worth comparing
 * (the slayer helmet, a Salve amulet, a Void set). Overlays stack onto any base build as
 * long as they touch different slots.
 */
@Value
public class SlotOverlay
{
	String reason;
	/** Slot name to item; every slot listed is replaced. */
	Map<String, EquipmentEntry> items;
	/** Whether the calculator's on-task toggle must be set for the effect to apply. */
	boolean onSlayerTask;

	public boolean clashesWith(SlotOverlay other)
	{
		return !Collections.disjoint(items.keySet(), other.items.keySet());
	}

	/** The base with this overlay's slots replaced; the name and reason are left to the caller. */
	public Loadout applyTo(Loadout base)
	{
		Map<String, EquipmentEntry> gear = new LinkedHashMap<>(base.getEquipment());
		gear.putAll(items);
		return base.withEquipment(gear).withOnSlayerTask(base.isOnSlayerTask() || onSlayerTask);
	}
}
