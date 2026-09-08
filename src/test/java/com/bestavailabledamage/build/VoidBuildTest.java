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
import com.bestavailabledamage.data.EquipmentEntry;
import com.bestavailabledamage.data.MonsterCatalogTest;
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.data.SpellCatalogTest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class VoidBuildTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final MonsterEntry abyssalDemon = MonsterCatalogTest.fixture().byId(415).get();
	private final LoadoutBuilder builder = new LoadoutBuilder(catalog, SpellCatalogTest.fixture(),
		new SpeedAdjustedRanker(), List.of(new VoidBuild()));
	private final Set<Integer> everything = catalog.all().stream().map(EquipmentEntry::getId).collect(Collectors.toSet());

	private static Optional<Loadout> voidBuild(List<Loadout> loadouts)
	{
		return loadouts.stream().filter(l -> l.getReason() != null && l.getReason().endsWith("Void")).findFirst();
	}

	@Test
	public void fullEliteSetOnTheTopMeleeBuild()
	{
		Loadout v = voidBuild(builder.build(everything, AttackType.STAB, abyssalDemon, 99, BuildOptions.defaults())).get();
		assertEquals(VoidBuild.ELITE_VOID, v.getReason());
		assertEquals("Inquisitor's mace (Elite Void)", v.getName());
		assertEquals("Void melee helm", v.getEquipment().get("head").getName());
		assertEquals("Elite void top", v.getEquipment().get("body").getName());
		assertEquals("Elite void robe", v.getEquipment().get("legs").getName());
		assertEquals("Void knight gloves", v.getEquipment().get("hands").getName());
		assertEquals("Amulet of torture", v.getEquipment().get("neck").getName());
	}

	@Test
	public void regularSetWhenEliteIsMissingAndRangedUsesTheRangerHelm()
	{
		Set<Integer> noElite = new HashSet<>(everything);
		noElite.remove(13072);
		noElite.remove(13073);
		Loadout v = voidBuild(builder.build(noElite, AttackType.RANGED, abyssalDemon, 99, BuildOptions.defaults())).get();
		assertEquals(VoidBuild.VOID, v.getReason());
		assertEquals("Void ranger helm", v.getEquipment().get("head").getName());
		assertEquals("Void knight top", v.getEquipment().get("body").getName());
		assertEquals("Void knight robe", v.getEquipment().get("legs").getName());
	}

	@Test
	public void eliteTopWithRegularRobeIsLabelledVoid()
	{
		Set<Integer> partialElite = new HashSet<>(everything);
		partialElite.remove(13073);
		Loadout v = voidBuild(builder.build(partialElite, AttackType.STAB, abyssalDemon, 99, BuildOptions.defaults())).get();
		assertEquals(VoidBuild.VOID, v.getReason());
		assertEquals("Elite void top", v.getEquipment().get("body").getName());
		assertEquals("Void knight robe", v.getEquipment().get("legs").getName());
	}

	@Test
	public void absentWithoutGlovesWithoutTheHelmForTheTypeOrWhenSwitchedOff()
	{
		Set<Integer> noGloves = new HashSet<>(everything);
		noGloves.remove(8842);
		assertFalse(voidBuild(builder.build(noGloves, AttackType.STAB, abyssalDemon, 99, BuildOptions.defaults())).isPresent());

		// no Void mage helm in the fixture
		assertFalse(voidBuild(builder.build(everything, AttackType.MAGIC, abyssalDemon, 99, BuildOptions.defaults())).isPresent());

		assertFalse(voidBuild(builder.build(everything, AttackType.STAB, abyssalDemon, 99,
			BuildOptions.builder().voidBuild(false).build())).isPresent());
	}
}
