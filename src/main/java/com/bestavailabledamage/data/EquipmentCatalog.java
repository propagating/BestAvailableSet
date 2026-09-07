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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Every piece of equipment the wiki calculator knows, keyed by item id. */
public class EquipmentCatalog
{
	private final List<EquipmentEntry> entries;
	private final Map<Integer, EquipmentEntry> byId = new HashMap<>();

	public EquipmentCatalog(List<EquipmentEntry> entries)
	{
		this.entries = Collections.unmodifiableList(entries);
		for (EquipmentEntry e : entries)
		{
			byId.put(e.getId(), e);
		}
	}

	public static EquipmentCatalog load(Gson gson, Reader reader)
	{
		List<EquipmentEntry> rows = gson.fromJson(reader, new TypeToken<List<EquipmentEntry>>()
		{
		}.getType());
		return new EquipmentCatalog(rows);
	}

	public Optional<EquipmentEntry> byId(int id)
	{
		return Optional.ofNullable(byId.get(id));
	}

	public List<EquipmentEntry> all()
	{
		return entries;
	}

	public int size()
	{
		return entries.size();
	}
}
