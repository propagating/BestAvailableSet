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
package com.bestavailabledamage.export;

import com.bestavailabledamage.build.Loadout;
import com.bestavailabledamage.build.LoadoutBuilder;
import com.bestavailabledamage.build.SlayerHelmBuild;
import com.bestavailabledamage.data.AttackType;
import com.bestavailabledamage.data.CombatStyle;
import com.bestavailabledamage.data.EquipmentCatalog;
import com.bestavailabledamage.data.EquipmentCatalogTest;
import com.bestavailabledamage.data.EquipmentEntry;
import com.bestavailabledamage.data.MonsterCatalogTest;
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.data.SpellEntry;
import com.bestavailabledamage.player.PlayerProfile;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SharePayloadTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();

	private Map<String, EquipmentEntry> gear(Object... slotIdPairs)
	{
		Map<String, EquipmentEntry> gear = new LinkedHashMap<>();
		for (String slot : LoadoutBuilder.SLOTS)
		{
			gear.put(slot, null);
		}
		for (int i = 0; i < slotIdPairs.length; i += 2)
		{
			gear.put((String) slotIdPairs[i], catalog.byId((Integer) slotIdPairs[i + 1]).get());
		}
		return gear;
	}

	@Test
	public void matchesGoldenFile()
	{
		Loadout rapier = new Loadout("Ghrazi rapier",
			new CombatStyle("Lunge", AttackType.STAB, "Aggressive"),
			gear("head", 26382, "cape", 21295, "neck", 19553, "weapon", 22324, "body", 11832,
				"shield", 22322, "legs", 11834, "hands", 22981, "feet", 13239, "ring", 28307),
			null, null, false, null);
		Loadout blowpipe = new Loadout("Toxic blowpipe",
			new CombatStyle("Rapid", AttackType.RANGED, "Rapid"),
			gear("weapon", 12926),
			catalog.byId(11230).get(),
			new SpellEntry("Fire Surge", 24, "fire", 95), false, null);
		Loadout slayer = new Loadout("Ghrazi rapier (Slayer helm, on task)",
			new CombatStyle("Lunge", AttackType.STAB, "Aggressive"),
			gear("head", 11865, "cape", 21295, "neck", 19553, "weapon", 22324, "body", 11832,
				"shield", 22322, "legs", 11834, "hands", 22981, "feet", 13239, "ring", 28307),
			null, null, true, SlayerHelmBuild.ON_TASK);
		MonsterEntry vorkath = MonsterCatalogTest.fixture().byId(8058).get();
		PlayerProfile profile = PlayerProfile.builder().attack(99).strength(99).defence(90).ranged(95)
			.magic(94).prayer(77).hitpoints(99).mining(85).herblore(80)
			.rigourUnlocked(true).auguryUnlocked(true).build();

		String json = new SharePayload(new Gson()).toJson(List.of(rapier, blowpipe, slayer), vorkath, profile, AttackType.STAB);

		JsonParser parser = new JsonParser();
		JsonElement expected = parser.parse(new InputStreamReader(
			getClass().getResourceAsStream("/com/bestavailabledamage/share-payload-golden.json"),
			StandardCharsets.UTF_8));
		assertEquals(expected, parser.parse(json));
	}
}
