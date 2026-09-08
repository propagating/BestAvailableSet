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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EquipmentCatalogTest
{
	public static EquipmentCatalog fixture()
	{
		Reader reader = new InputStreamReader(
			EquipmentCatalogTest.class.getResourceAsStream("/com/bestavailabledamage/equipment-fixture.json"),
			StandardCharsets.UTF_8);
		return EquipmentCatalog.load(new Gson(), reader);
	}

	@Test
	public void loadsRowsAndLooksUpById()
	{
		EquipmentCatalog catalog = fixture();
		assertEquals(48, catalog.size());
		assertEquals(2, catalog.byId(22947).get().getPrayer());
		EquipmentEntry rapier = catalog.byId(22324).get();
		assertEquals("Ghrazi rapier", rapier.getName());
		assertEquals("Stab Sword", rapier.getCategory());
		assertEquals(93, rapier.getStr());
		assertEquals(100, rapier.getStab());
		assertFalse(catalog.byId(1).isPresent());
	}

	@Test
	public void displayNameIncludesVersionOnlyWhenPresent()
	{
		EquipmentCatalog catalog = fixture();
		assertEquals("Ghrazi rapier", catalog.byId(22324).get().displayName());
		assertEquals("Toxic blowpipe (Charged)", catalog.byId(12926).get().displayName());
	}

	@Test
	public void attackTypeReadsMatchingStrengthAndAccuracy()
	{
		EquipmentCatalog catalog = fixture();
		EquipmentEntry rapier = catalog.byId(22324).get();
		EquipmentEntry tbow = catalog.byId(20997).get();
		EquipmentEntry kodai = catalog.byId(21006).get();
		assertEquals(93, AttackType.STAB.strengthOf(rapier));
		assertEquals(100, AttackType.STAB.accuracyOf(rapier));
		assertEquals(55, AttackType.SLASH.accuracyOf(rapier));
		assertEquals(20, AttackType.RANGED.strengthOf(tbow));
		assertEquals(70, AttackType.RANGED.accuracyOf(tbow));
		assertEquals(150, AttackType.MAGIC.strengthOf(kodai));
		assertEquals(28, AttackType.MAGIC.accuracyOf(kodai));
		assertTrue(AttackType.CRUSH.isMelee());
		assertFalse(AttackType.MAGIC.isMelee());
		assertEquals("stab", AttackType.STAB.calcName());
	}
}
