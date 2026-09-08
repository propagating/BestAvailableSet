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

import com.bestavailabledamage.data.AttackType;
import com.bestavailabledamage.data.CombatStyle;
import com.bestavailabledamage.data.EquipmentCatalog;
import com.bestavailabledamage.data.EquipmentCatalogTest;
import com.bestavailabledamage.data.EquipmentEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LoadoutTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final CombatStyle style = new CombatStyle("Lunge", AttackType.STAB, "Aggressive");

	private Loadout loadout(String name, String reason)
	{
		EquipmentEntry rapier = catalog.byId(22324).get();
		Map<String, EquipmentEntry> gear = new LinkedHashMap<>();
		gear.put("weapon", rapier);
		return new Loadout(name, style, gear, null, null, false, reason);
	}

	@Test
	public void guaranteedAppendsTheReasonAndBaseNameStripsIt()
	{
		Loadout base = loadout("Ghrazi rapier", null);
		Loadout guaranteed = base.guaranteed("vs undead");
		assertEquals("vs undead", guaranteed.getReason());
		assertEquals("Ghrazi rapier (vs undead)", guaranteed.getName());
		assertEquals("Ghrazi rapier", guaranteed.baseName());
	}

	@Test
	public void baseNameLeavesAnUnexpectedNameAlone()
	{
		Loadout odd = loadout("Ghrazi rapier", null).withReason("x");
		assertEquals("Ghrazi rapier", odd.baseName());
	}
}
