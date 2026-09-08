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

public class SituationalItemBuildTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final MonsterCatalog monsters = MonsterCatalogTest.fixture();
	private final LoadoutBuilder builder = new LoadoutBuilder(catalog, SpellCatalogTest.fixture(),
		new SpeedAdjustedRanker(), List.of(new SituationalItemBuild()));
	private final Set<Integer> everything = catalog.all().stream().map(EquipmentEntry::getId).collect(Collectors.toSet());

	private static Optional<Loadout> withReason(List<Loadout> loadouts, String reason)
	{
		return loadouts.stream().filter(l -> reason.equals(l.getReason())).findFirst();
	}

	@Test
	public void dragonAndUndeadTargetGetsTheLanceAndSalveBuilds()
	{
		// Vorkath: dragon + undead. The lance is also a plain stab weapon; the guaranteed copy
		// replaces the plain one rather than duplicating it.
		List<Loadout> loadouts = builder.build(everything, AttackType.STAB, monsters.byId(8058).get(), 99,
			BuildOptions.defaults());
		Loadout lance = withReason(loadouts, "vs dragon").get();
		assertEquals("Dragon hunter lance (vs dragon)", lance.getName());
		assertEquals("Dragon hunter lance", lance.weapon().getName());
		assertEquals(1, loadouts.stream().filter(l -> l.weapon().getId() == 22978).count());

		Loadout salve = withReason(loadouts, "vs undead").get();
		assertEquals("Inquisitor's mace", salve.weapon().getName());
		assertEquals("Salve amulet(ei)", salve.getEquipment().get("neck").getName());
		assertEquals("Inquisitor's mace (vs undead)", salve.getName());
		// table order: the undead row precedes the dragon row
		assertEquals("vs undead", loadouts.get(0).getReason());
		assertEquals("vs dragon", loadouts.get(1).getReason());
		// 2 guaranteed + mace, rapier, scimitar (the plain lance duplicates the guaranteed one)
		assertEquals(5, loadouts.size());
	}

	@Test
	public void weaponRowsOnlyApplyWhenTheWeaponCanUseTheAttackType()
	{
		// the lance has no ranged style, but the crossbow (also a "vs dragon" prefix) does
		List<Loadout> loadouts = builder.build(everything, AttackType.RANGED, monsters.byId(8058).get(), 99,
			BuildOptions.defaults());
		Loadout ranged = withReason(loadouts, "vs dragon").get();
		assertEquals("Dragon hunter crossbow", ranged.weapon().getName());
		// the crush style (Pound) exists, so the lance appears for crush
		List<Loadout> crush = builder.build(everything, AttackType.CRUSH, monsters.byId(8058).get(), 99,
			BuildOptions.defaults());
		assertTrue(withReason(crush, "vs dragon").isPresent());
	}

	@Test
	public void laterPrefixIsUsedWhenTheFirstOwnedItemCannotUseTheType()
	{
		// the lance is preferred but has no ranged style, so the build falls through to the
		// crossbow rather than skipping the "vs dragon" row entirely
		List<Loadout> ranged = builder.build(everything, AttackType.RANGED, monsters.byId(8058).get(), 99,
			BuildOptions.defaults());
		Loadout crossbow = withReason(ranged, "vs dragon").get();
		assertEquals("Dragon hunter crossbow", crossbow.weapon().getName());
		assertEquals("Ruby dragon bolts (e)", crossbow.getEquipment().get("ammo").getName());

		List<Loadout> stab = builder.build(everything, AttackType.STAB, monsters.byId(8058).get(), 99,
			BuildOptions.defaults());
		Loadout lance = withReason(stab, "vs dragon").get();
		assertEquals("Dragon hunter lance", lance.weapon().getName());
	}

	@Test
	public void twistedBowOnlyAgainstHighMagicLevels()
	{
		List<Loadout> zulrah = builder.build(everything, AttackType.RANGED, monsters.byId(2042).get(), 99,
			BuildOptions.defaults());
		Loadout tbow = withReason(zulrah, "vs high magic level").get();
		assertEquals("Twisted bow", tbow.weapon().getName());
		assertEquals("Dragon arrow", tbow.getEquipment().get("ammo").getName());

		List<Loadout> demon = builder.build(everything, AttackType.RANGED, monsters.byId(415).get(), 99,
			BuildOptions.defaults());
		assertFalse(withReason(demon, "vs high magic level").isPresent());
	}

	@Test
	public void nothingForTargetsWithoutMatchingAttributesOrWhenSwitchedOff()
	{
		List<Loadout> graardor = builder.build(everything, AttackType.STAB, monsters.byId(2215).get(), 99,
			BuildOptions.defaults());
		assertTrue(graardor.stream().allMatch(l -> l.getReason() == null));

		List<Loadout> off = builder.build(everything, AttackType.STAB, monsters.byId(8058).get(), 99,
			BuildOptions.builder().situationalBuilds(false).build());
		assertTrue(off.stream().allMatch(l -> l.getReason() == null));
	}

	@Test
	public void preferredSalveVariantWinsOverThePlainOne()
	{
		List<Loadout> both = builder.build(everything, AttackType.STAB, monsters.byId(8058).get(), 99,
			BuildOptions.defaults());
		Loadout salve = withReason(both, "vs undead").get();
		assertEquals("Salve amulet(ei)", salve.getEquipment().get("neck").getName());

		Set<Integer> onlyPlain = new HashSet<>(everything);
		onlyPlain.remove(12018);
		List<Loadout> plainOnly = builder.build(onlyPlain, AttackType.STAB, monsters.byId(8058).get(), 99,
			BuildOptions.defaults());
		Loadout salvePlain = withReason(plainOnly, "vs undead").get();
		assertEquals("Salve amulet", salvePlain.getEquipment().get("neck").getName());
	}

	@Test
	public void tableRowsMatchByNamePrefixCaseInsensitively()
	{
		SituationalItem salve = SituationalItems.TABLE.stream().filter(r -> r.getReason().equals("vs undead")).findFirst().get();
		assertTrue(salve.matches(catalog.byId(12018).get()));
		assertFalse(salve.matches(catalog.byId(19553).get()));
		assertTrue(SituationalItems.attribute("undead").test(monsters.byId(8058).get()));
		assertFalse(SituationalItems.attribute("undead").test(monsters.byId(415).get()));
	}
}
