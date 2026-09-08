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
import com.bestavailabledamage.data.MonsterCatalog;
import com.bestavailabledamage.data.MonsterCatalogTest;
import com.bestavailabledamage.data.SpellCatalogTest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SlayerHelmBuildTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final MonsterCatalog monsters = MonsterCatalogTest.fixture();
	private final LoadoutBuilder builder = new LoadoutBuilder(catalog, SpellCatalogTest.fixture(),
		new SpeedAdjustedRanker(), List.of(new SlayerHelmBuild()));
	private final Set<Integer> everything = catalog.all().stream().map(EquipmentEntry::getId).collect(Collectors.toSet());

	private static Optional<Loadout> slayerBuild(List<Loadout> loadouts)
	{
		return loadouts.stream().filter(l -> l.getReason() != null && l.getReason().startsWith("Slayer helm")).findFirst();
	}

	@Test
	public void slayerMonsterGetsOneHelmBuildFirstPlusThePlainBuilds()
	{
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, monsters.byId(415).get(), 99,
			BuildOptions.defaults());
		assertEquals(5, loadouts.size());
		Loadout helm = loadouts.get(0);
		assertEquals(SlayerHelmBuild.IF_ON_TASK, helm.getReason());
		assertEquals("Inquisitor's mace (Slayer helm, if on task)", helm.getName());
		assertEquals("Slayer helmet (i)", helm.getEquipment().get("head").getName());
		assertEquals("Inquisitor's mace", helm.weapon().getName());
		assertTrue(helm.isOnSlayerTask());
		assertEquals("Inquisitor's mace", loadouts.get(1).getName());
		assertFalse(loadouts.get(1).isOnSlayerTask());
	}

	@Test
	public void labelSaysOnTaskWhenTheClientConfirmsIt()
	{
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, monsters.byId(415).get(), 99,
			BuildOptions.builder().onSlayerTask(true).build());
		assertEquals(SlayerHelmBuild.ON_TASK, slayerBuild(loadouts).get().getReason());
	}

	@Test
	public void absentForNonSlayerTargetsWithoutAHelmOrWhenSwitchedOff()
	{
		assertFalse(slayerBuild(builder.build(everything, AttackType.STAB, monsters.byId(2042).get(), 99,
			BuildOptions.defaults())).isPresent());

		Set<Integer> noHelm = new HashSet<>(everything);
		noHelm.remove(11865);
		assertFalse(slayerBuild(builder.build(noHelm, AttackType.STAB, monsters.byId(415).get(), 99,
			BuildOptions.defaults())).isPresent());

		assertFalse(slayerBuild(builder.build(everything, AttackType.STAB, monsters.byId(415).get(), 99,
			BuildOptions.builder().slayerBuild(false).build())).isPresent());
	}

	@Test
	public void helmBuildIsKeptEvenWhenThePlainBuildAlreadyWearsTheHelm()
	{
		// for ranged the imbued helm's +3 accuracy makes it the plain head pick too; the two
		// differ only by the on-task toggle, and both must be exported
		List<Loadout> loadouts = builder.build(everything, AttackType.RANGED, monsters.byId(415).get(), 99,
			BuildOptions.defaults());
		Loadout helm = loadouts.get(0);
		assertTrue(helm.isOnSlayerTask());
		assertEquals("Slayer helmet (i)", helm.getEquipment().get("head").getName());
		assertEquals("Slayer helmet (i)", loadouts.get(1).getEquipment().get("head").getName());
		assertFalse(loadouts.get(1).isOnSlayerTask());
	}
}
