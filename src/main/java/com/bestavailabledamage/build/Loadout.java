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

import com.bestavailabledamage.data.CombatStyle;
import com.bestavailabledamage.data.EquipmentEntry;
import com.bestavailabledamage.data.SpellEntry;
import java.util.Map;
import java.util.Objects;
import lombok.Value;
import lombok.With;

/**
 * One candidate gear set. {@code equipment} has every slot key; a null value means empty.
 * {@code reason} is null for a plain ranked build and the label of the rule that guaranteed
 * it otherwise (also appended to {@code name} in parentheses).
 */
@Value
@With
public class Loadout
{
	String name;
	CombatStyle style;
	Map<String, EquipmentEntry> equipment;
	EquipmentEntry blowpipeDart;
	SpellEntry spell;
	boolean onSlayerTask;
	String reason;

	public EquipmentEntry weapon()
	{
		return equipment.get("weapon");
	}

	/** Same items, dart, spell and calculator toggles; the name and reason do not matter. */
	public boolean sameGearAs(Loadout other)
	{
		return Objects.equals(equipment, other.equipment)
			&& Objects.equals(blowpipeDart, other.blowpipeDart)
			&& Objects.equals(spell, other.spell)
			&& onSlayerTask == other.onSlayerTask;
	}

	/**
	 * Marks this loadout as guaranteed by a rule: sets {@code reason} and appends it to the
	 * name in parentheses. The contract every caller (and {@link #baseName()}) relies on is
	 * that the result's name always ends with {@code " (" + reason + ")"}.
	 */
	public Loadout guaranteed(String reason)
	{
		return withReason(reason).withName(name + " (" + reason + ")");
	}

	/** The name without the {@link #guaranteed(String)} suffix, or the name unchanged without one. */
	public String baseName()
	{
		if (reason != null && name.endsWith(" (" + reason + ")"))
		{
			return name.substring(0, name.length() - reason.length() - 3);
		}
		return name;
	}
}
