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
package com.bestavailabledamage.player;

import com.bestavailabledamage.data.MonsterEntry;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerProfileTest
{
	private static MonsterEntry monster(String name)
	{
		return new MonsterEntry(1, name, "", 100, null, 0, List.of(), true, 1);
	}

	private static PlayerProfile withTask(String task)
	{
		return PlayerProfile.builder().slayerTask(task).build();
	}

	@Test
	public void taskMatchesSingularPluralAndPrefix()
	{
		assertTrue(withTask("Abyssal demons").slayerTaskMatches(monster("Abyssal demon")));
		assertTrue(withTask("ABYSSAL DEMONS").slayerTaskMatches(monster("Abyssal demon")));
		assertTrue(withTask("Kalphites").slayerTaskMatches(monster("Kalphite Queen")));
		assertTrue(withTask("Vorkath").slayerTaskMatches(monster("Vorkath")));
		assertFalse(withTask("Dragons").slayerTaskMatches(monster("Vorkath")));
		assertFalse(withTask(null).slayerTaskMatches(monster("Abyssal demon")));
		assertFalse(withTask("  ").slayerTaskMatches(monster("Abyssal demon")));
	}

	@Test
	public void readsTheTaskNameFromTheClientTables()
	{
		Client client = mock(Client.class);
		when(client.getVarpValue(VarPlayerID.SLAYER_COUNT)).thenReturn(12);
		when(client.getVarpValue(VarPlayerID.SLAYER_TARGET)).thenReturn(41);
		when(client.getDBRowsByValue(eq(DBTableID.SlayerTask.ID), eq(DBTableID.SlayerTask.COL_ID), eq(0), eq(41)))
			.thenReturn(List.of(7));
		when(client.getDBTableField(7, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0))
			.thenReturn(new Object[]{"Abyssal demons"});
		assertEquals("Abyssal demons", PlayerProfile.currentSlayerTask(client));
	}

	@Test
	public void noTaskOrMissingRowGivesNull()
	{
		Client none = mock(Client.class);
		when(none.getVarpValue(VarPlayerID.SLAYER_COUNT)).thenReturn(0);
		assertNull(PlayerProfile.currentSlayerTask(none));

		Client missing = mock(Client.class);
		when(missing.getVarpValue(VarPlayerID.SLAYER_COUNT)).thenReturn(3);
		when(missing.getVarpValue(VarPlayerID.SLAYER_TARGET)).thenReturn(5);
		when(missing.getDBRowsByValue(anyInt(), anyInt(), anyInt(), eq(5))).thenReturn(List.of());
		assertNull(PlayerProfile.currentSlayerTask(missing));
	}
}
