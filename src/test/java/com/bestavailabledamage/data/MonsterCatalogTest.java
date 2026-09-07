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
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class MonsterCatalogTest
{
	public static MonsterCatalog fixture()
	{
		Reader reader = new InputStreamReader(
			MonsterCatalogTest.class.getResourceAsStream("/com/bestavailabledamage/monsters-fixture.json"),
			StandardCharsets.UTF_8);
		return MonsterCatalog.load(new Gson(), reader);
	}

	@Test
	public void searchIsCaseInsensitiveSubstringSortedByNameThenVersion()
	{
		List<MonsterEntry> hits = fixture().search("VORK", 10);
		assertEquals(List.of("Vorkath (Dragon Slayer II)", "Vorkath (Post-quest)"),
			hits.stream().map(MonsterEntry::displayName).collect(Collectors.toList()));
	}

	@Test
	public void searchHonoursLimitAndBlankQueryReturnsNothing()
	{
		assertEquals(1, fixture().search("a", 1).size());
		assertEquals(0, fixture().search("   ", 10).size());
	}

	@Test
	public void elementWeaknessIsEmptyWhenNull()
	{
		MonsterCatalog catalog = fixture();
		assertEquals("fire", catalog.byId(8058).get().elementWeakness().get());
		assertFalse(catalog.byId(415).get().elementWeakness().isPresent());
		assertEquals("General Graardor", catalog.byId(2215).get().displayName());
	}
}
