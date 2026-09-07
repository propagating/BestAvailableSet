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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MonsterCatalog
{
	private final List<MonsterEntry> entries;
	private final Map<Integer, MonsterEntry> byId = new HashMap<>();

	public MonsterCatalog(List<MonsterEntry> entries)
	{
		List<MonsterEntry> sorted = new ArrayList<>(entries);
		sorted.sort(Comparator.comparing(MonsterEntry::getName, String.CASE_INSENSITIVE_ORDER)
			.thenComparing(MonsterEntry::getVersion, String.CASE_INSENSITIVE_ORDER));
		this.entries = sorted;
		for (MonsterEntry m : sorted)
		{
			byId.putIfAbsent(m.getId(), m);
		}
	}

	public static MonsterCatalog load(Gson gson, Reader reader)
	{
		List<MonsterEntry> rows = gson.fromJson(reader, new TypeToken<List<MonsterEntry>>()
		{
		}.getType());
		return new MonsterCatalog(rows);
	}

	/** Case-insensitive substring search on the name; blank queries match nothing. */
	public List<MonsterEntry> search(String query, int limit)
	{
		List<MonsterEntry> hits = new ArrayList<>();
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		if (needle.isEmpty())
		{
			return hits;
		}
		for (MonsterEntry m : entries)
		{
			if (m.getName().toLowerCase(Locale.ROOT).contains(needle))
			{
				hits.add(m);
				if (hits.size() >= limit)
				{
					break;
				}
			}
		}
		return hits;
	}

	public Optional<MonsterEntry> byId(int id)
	{
		return Optional.ofNullable(byId.get(id));
	}
}
