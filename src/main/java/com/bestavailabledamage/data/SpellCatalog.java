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
package com.bestavailabledamage.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

/** Standard-spellbook elemental spells, used for element-weak targets. */
public class SpellCatalog
{
	private final List<SpellEntry> spells;

	public SpellCatalog(List<SpellEntry> spells)
	{
		this.spells = spells;
	}

	public static SpellCatalog load(Gson gson, Reader reader)
	{
		List<SpellEntry> rows = gson.fromJson(reader, new TypeToken<List<SpellEntry>>()
		{
		}.getType());
		return new SpellCatalog(rows);
	}

	/** Highest max-hit spell of the element the player can cast, if any. */
	public Optional<SpellEntry> strongest(String element, int magicLevel)
	{
		SpellEntry best = null;
		for (SpellEntry s : spells)
		{
			if (!s.getElement().equals(element) || s.getLevel() > magicLevel)
			{
				continue;
			}
			if (best == null || s.getMaxHit() > best.getMaxHit())
			{
				best = s;
			}
		}
		return Optional.ofNullable(best);
	}
}
