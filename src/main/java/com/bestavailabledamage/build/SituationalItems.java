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

import com.bestavailabledamage.data.MonsterEntry;
import java.util.List;
import java.util.function.Predicate;

/**
 * Items whose value depends on the target and is not in any listed bonus, so the plain
 * ranking would never choose them. Game facts, hand-maintained; the calculator applies the
 * actual effect from the item id and the target. A row's {@code namePrefixes} are in
 * preference order: the first prefix with any owned match wins over a later, better-ranked one
 * (e.g. Salve amulet's enchanted, imbued variants beat the plain amulet even though they carry
 * identical listed stats).
 */
public final class SituationalItems
{
	/** A target's magic level from which the Twisted bow's scaling is worth comparing. */
	static final int TWISTED_BOW_MAGIC_LEVEL = 200;

	public static final List<SituationalItem> TABLE = List.of(
		new SituationalItem("vs undead", "neck",
			List.of("salve amulet(ei)", "salve amulet(i)", "salve amulet (e)", "salve amulet"), attribute("undead")),
		new SituationalItem("vs dragon", "weapon",
			List.of("dragon hunter lance", "dragon hunter crossbow", "dragon hunter wand"), attribute("dragon")),
		new SituationalItem("vs kalphite", "weapon", List.of("keris"), attribute("kalphite")),
		new SituationalItem("vs demon", "weapon", List.of("emberlight", "arclight"), attribute("demon")),
		new SituationalItem("vs high magic level", "weapon", List.of("twisted bow"),
			m -> m.getMagicLevel() >= TWISTED_BOW_MAGIC_LEVEL)
	);

	public static Predicate<MonsterEntry> attribute(String attribute)
	{
		return m -> m.getAttributes() != null && m.getAttributes().contains(attribute);
	}

	private SituationalItems()
	{
	}
}
