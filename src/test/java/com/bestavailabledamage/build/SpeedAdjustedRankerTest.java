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
import com.bestavailabledamage.data.EquipmentCatalog;
import com.bestavailabledamage.data.EquipmentCatalogTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SpeedAdjustedRankerTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final WeaponRanker ranker = new SpeedAdjustedRanker();

	@Test
	public void strengthPlus64OverSpeed()
	{
		// Ghrazi rapier: (93 + 64) / 4
		assertEquals(39.25, ranker.score(catalog.byId(22324).get(), AttackType.STAB), 1e-9);
		// Elder maul for crush: (147 + 64) / 6
		assertEquals(35.1666666, ranker.score(catalog.byId(21003).get(), AttackType.CRUSH), 1e-6);
	}

	@Test
	public void rapierOutranksSlowerHigherStrengthWeaponForStab()
	{
		double rapier = ranker.score(catalog.byId(22324).get(), AttackType.STAB);
		double maul = ranker.score(catalog.byId(21003).get(), AttackType.STAB);
		assertTrue(rapier > maul);
	}

	@Test
	public void rangedWeaponsUseRapidSpeed()
	{
		// Twisted bow: (20 + 64) / (6 - 1)
		assertEquals(16.8, ranker.score(catalog.byId(20997).get(), AttackType.RANGED), 1e-9);
	}
}
