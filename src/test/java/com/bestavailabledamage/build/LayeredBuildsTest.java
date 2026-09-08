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
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** Situational weapons, the slayer helmet, Salve and Void stack into single builds. */
public class LayeredBuildsTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final MonsterCatalog monsters = MonsterCatalogTest.fixture();
	// the shipped rule set: layered builds then the budget build
	private final LoadoutBuilder builder = new LoadoutBuilder(catalog, SpellCatalogTest.fixture(), new SpeedAdjustedRanker());
	private final Set<Integer> everything = catalog.all().stream().map(EquipmentEntry::getId).collect(Collectors.toSet());

	private static List<String> names(List<Loadout> loadouts)
	{
		return loadouts.stream().map(Loadout::getName).collect(Collectors.toList());
	}

	@Test
	public void demonSlayerTaskStacksEmberlightWithTheSlayerHelm()
	{
		// Abyssal demon: demon attribute + slayer monster. Emberlight is the situational weapon,
		// the slayer helm is a slot overlay, the Void set is a separate overlay build; every
		// item is "free" so the budget build is the top plain build.
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, monsters.byId(415).get(), 99,
			BuildOptions.defaults());
		assertEquals(List.of(
			"Emberlight (vs demon, Slayer helm, if on task)",
			"Inquisitor's mace (Slayer helm, if on task)",
			"Emberlight (vs demon, Elite Void)",
			"Emberlight (vs demon)",
			"Inquisitor's mace (Budget, under 100k)",
			"Ghrazi rapier"), names(loadouts));

		Loadout combined = loadouts.get(0);
		assertEquals("Emberlight", combined.weapon().getName());
		assertEquals("Slayer helmet (i)", combined.getEquipment().get("head").getName());
		assertTrue(combined.isOnSlayerTask());
		assertEquals("vs demon, Slayer helm, if on task", combined.getReason());
		assertEquals("Emberlight", combined.baseName());

		Loadout voidBuild = loadouts.get(2);
		assertEquals("Void melee helm", voidBuild.getEquipment().get("head").getName());
		assertFalse(voidBuild.isOnSlayerTask());
		assertNull(loadouts.get(5).getReason());
	}

	@Test
	public void dragonAndUndeadTargetStacksLanceSalveAndHelm()
	{
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, monsters.byId(8058).get(), 99,
			BuildOptions.builder().onSlayerTask(true).build());
		Loadout first = loadouts.get(0);
		assertEquals("Dragon hunter lance (vs dragon, vs undead, Slayer helm, on task)", first.getName());
		assertEquals("Salve amulet(ei)", first.getEquipment().get("neck").getName());
		assertEquals("Slayer helmet (i)", first.getEquipment().get("head").getName());
		assertTrue(first.isOnSlayerTask());

		assertEquals("Inquisitor's mace (vs undead, Slayer helm, on task)", loadouts.get(1).getName());

		// Void takes the head, so the helm cannot stack with it, but Salve (neck) can
		Loadout voidBuild = loadouts.get(2);
		assertEquals("Dragon hunter lance (vs dragon, Elite Void, vs undead)", voidBuild.getName());
		assertEquals("Void melee helm", voidBuild.getEquipment().get("head").getName());
		assertEquals("Salve amulet(ei)", voidBuild.getEquipment().get("neck").getName());
		assertFalse(voidBuild.isOnSlayerTask());

		assertEquals("Dragon hunter lance (vs dragon)", loadouts.get(3).getName());
		assertEquals(LoadoutBuilder.MAX_LOADOUTS, loadouts.size());
	}

	@Test
	public void withoutOverlaysTheSituationalWeaponStillGetsItsOwnBuild()
	{
		BuildOptions off = BuildOptions.builder().slayerBuild(false).voidBuild(false).budgetBuild(false).build();
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, monsters.byId(415).get(), 99, off);
		assertEquals("Emberlight (vs demon)", loadouts.get(0).getName());
		assertEquals("Inquisitor's mace", loadouts.get(1).getName());
		assertTrue(loadouts.stream().skip(1).allMatch(l -> l.getReason() == null));
	}

	@Test
	public void overlaysAloneStackOntoTheTopPlainBuild()
	{
		// no Emberlight owned: the helm goes onto the top plain weapon only
		Set<Integer> noEmberlight = new HashSet<>(everything);
		noEmberlight.remove(29589);
		List<Loadout> loadouts = builder.build(noEmberlight, AttackType.STAB, monsters.byId(415).get(), 99,
			BuildOptions.builder().voidBuild(false).budgetBuild(false).build());
		assertEquals("Inquisitor's mace (Slayer helm, if on task)", loadouts.get(0).getName());
		assertEquals("Inquisitor's mace", loadouts.get(1).getName());
	}

	@Test
	public void nothingApplicableMeansPlainBuildsOnly()
	{
		// Zulrah: not a slayer monster, no attributes, magic 300 (Twisted bow only matters for ranged)
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, monsters.byId(2042).get(), 99,
			BuildOptions.builder().budgetBuild(false).build());
		assertEquals("Inquisitor's mace (Elite Void)", loadouts.get(0).getName());
		assertTrue(loadouts.stream().skip(1).allMatch(l -> l.getReason() == null));
	}
}
