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
import com.bestavailabledamage.data.SpellCatalog;
import com.bestavailabledamage.data.SpellCatalogTest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** The orchestration: order, de-duplication, the cap and the surviving plain build. */
public class GuaranteedBuildsTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final SpellCatalog spells = SpellCatalogTest.fixture();
	private final MonsterEntry abyssalDemon = MonsterCatalogTest.fixture().byId(415).get();
	private final Set<Integer> everything = catalog.all().stream().map(EquipmentEntry::getId).collect(Collectors.toSet());

	/** Swaps the head of the top plain loadout for the given item and labels it. */
	private static GuaranteedBuild headSwap(String reason, int headId, EquipmentCatalog catalog)
	{
		return ctx ->
		{
			Loadout base = ctx.topPlain();
			Map<String, EquipmentEntry> gear = new LinkedHashMap<>(base.getEquipment());
			gear.put("head", catalog.byId(headId).get());
			return List.of(base.withEquipment(gear).withReason(reason).withName(base.getName() + " (" + reason + ")"));
		};
	}

	private static List<String> reasons(List<Loadout> loadouts)
	{
		return loadouts.stream().map(Loadout::getReason).collect(Collectors.toList());
	}

	@Test
	public void guaranteedBuildsComeFirstInRegistrationOrderThenPlainFillsUp()
	{
		LoadoutBuilder builder = new LoadoutBuilder(catalog, spells, new SpeedAdjustedRanker(),
			List.of(headSwap("first", 24271, catalog), headSwap("second", 11665, catalog)));
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, abyssalDemon, 99, BuildOptions.defaults());
		// 4 plain stab loadouts exist; 2 guaranteed + 4 plain = 6
		assertEquals(6, loadouts.size());
		assertEquals("first", loadouts.get(0).getReason());
		assertEquals("Neitiznot faceguard", loadouts.get(0).getEquipment().get("head").getName());
		assertEquals("Inquisitor's mace (first)", loadouts.get(0).getName());
		assertEquals("second", loadouts.get(1).getReason());
		assertNull(loadouts.get(2).getReason());
		assertEquals("Inquisitor's mace", loadouts.get(2).getName());
	}

	@Test
	public void plainOnlyOverloadIgnoresGuaranteedBuilds()
	{
		LoadoutBuilder builder = new LoadoutBuilder(catalog, spells, new SpeedAdjustedRanker(),
			List.of(headSwap("first", 24271, catalog)));
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, abyssalDemon, 99);
		assertEquals(5, loadouts.size());
		assertTrue(loadouts.stream().allMatch(l -> l.getReason() == null));
	}

	@Test
	public void duplicateGearIsDroppedKeepingTheGuaranteedCopy()
	{
		// a rule that returns the top plain loadout unchanged apart from its label
		GuaranteedBuild same = ctx -> List.of(ctx.topPlain().withReason("same").withName("same"));
		LoadoutBuilder builder = new LoadoutBuilder(catalog, spells, new SpeedAdjustedRanker(), List.of(same));
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, abyssalDemon, 99, BuildOptions.defaults());
		assertEquals(5, loadouts.size());
		assertEquals("same", loadouts.get(0).getReason());
		assertEquals("Ghrazi rapier", loadouts.get(1).getName());
	}

	@Test
	public void atMostFiveGuaranteedSoOnePlainBuildAlwaysSurvives()
	{
		List<GuaranteedBuild> rules = new ArrayList<>();
		// five distinct heads, then two repeats: r5 duplicates r0's gear and r6 duplicates r3's,
		// so both are dropped; the plain top build (Torva head) is distinct and survives
		int[] heads = {24271, 11665, 11664, 21018, 11865, 24271, 21018};
		for (int i = 0; i < heads.length; i++)
		{
			rules.add(headSwap("r" + i, heads[i], catalog));
		}
		LoadoutBuilder builder = new LoadoutBuilder(catalog, spells, new SpeedAdjustedRanker(), rules);
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, abyssalDemon, 99, BuildOptions.defaults());
		assertEquals(LoadoutBuilder.MAX_LOADOUTS, loadouts.size());
		assertEquals(List.of("r0", "r1", "r2", "r3", "r4"), reasons(loadouts.subList(0, 5)));
		assertNull(loadouts.get(5).getReason());
		assertEquals("Inquisitor's mace", loadouts.get(5).getName());
	}

	@Test
	public void noWeaponMeansNoLoadoutsEvenWithRules()
	{
		LoadoutBuilder builder = new LoadoutBuilder(catalog, spells, new SpeedAdjustedRanker(),
			List.of(headSwap("first", 24271, catalog)));
		assertTrue(builder.build(Set.of(26382, 24271), AttackType.STAB, abyssalDemon, 99, BuildOptions.defaults()).isEmpty());
	}

	@Test
	public void sameGearAsIgnoresNameAndReason()
	{
		LoadoutBuilder builder = new LoadoutBuilder(catalog, spells, new SpeedAdjustedRanker());
		Loadout a = builder.build(everything, AttackType.STAB, abyssalDemon, 99).get(0);
		assertTrue(a.sameGearAs(a.withName("x").withReason("y")));
		assertFalse(a.sameGearAs(a.withOnSlayerTask(true)));
	}
}
