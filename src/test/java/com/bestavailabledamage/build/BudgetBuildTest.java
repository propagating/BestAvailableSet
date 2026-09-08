/*
 * Copyright (c) 2024, Ryan Richardson <rdrichardson@chapman.edu>
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
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.bestavailabledamage.build;

import com.bestavailabledamage.data.AttackType;
import com.bestavailabledamage.data.EquipmentCatalog;
import com.bestavailabledamage.data.EquipmentCatalogTest;
import com.bestavailabledamage.data.EquipmentEntry;
import com.bestavailabledamage.data.MonsterCatalogTest;
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.data.SpellCatalogTest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class BudgetBuildTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final MonsterEntry abyssalDemon = MonsterCatalogTest.fixture().byId(415).get();
	private final LoadoutBuilder builder = new LoadoutBuilder(catalog, SpellCatalogTest.fixture(),
		new SpeedAdjustedRanker(), List.of(new BudgetBuild()));
	private final Set<Integer> everything = catalog.all().stream().map(EquipmentEntry::getId).collect(Collectors.toSet());

	// everything costs 1m except a few cheap or untradeable items
	private final IntUnaryOperator prices = id -> Map.of(
		4587, 60_000,     // Dragon scimitar
		24271, 50_000,    // Neitiznot faceguard
		21295, 0,         // Infernal cape (untradeable)
		22947, 0          // Rada's blessing 4 (untradeable)
	).getOrDefault(id, 1_000_000);

	private static Optional<Loadout> budget(List<Loadout> loadouts)
	{
		return loadouts.stream().filter(l -> l.getReason() != null && l.getReason().startsWith("Budget")).findFirst();
	}

	@Test
	public void bestBuildFromItemsUnderTheCapWithUntradeablesCountingAsFree()
	{
		BuildOptions options = BuildOptions.builder().itemPrice(prices).build();
		List<Loadout> loadouts = builder.build(everything, AttackType.SLASH, abyssalDemon, 99, options);
		Loadout b = loadouts.get(0);
		assertEquals("Budget, under 100k", b.getReason());
		assertEquals("Dragon scimitar (Budget, under 100k)", b.getName());
		assertEquals("Neitiznot faceguard", b.getEquipment().get("head").getName());
		assertEquals("Infernal cape", b.getEquipment().get("cape").getName());
		assertEquals("Rada's blessing 4", b.getEquipment().get("ammo").getName());
		assertNull(b.getEquipment().get("neck"));
		assertNull(b.getEquipment().get("body"));
		// the top plain slash build is the rapier (stab swords have a Slash style; 39.25 beats the whip's 36.5)
		assertEquals("Ghrazi rapier", loadouts.get(1).getName());
	}

	@Test
	public void absentWhenNoWeaponIsUnderTheCapOrWhenSwitchedOff()
	{
		assertFalse(budget(builder.build(everything, AttackType.SLASH, abyssalDemon, 99,
			BuildOptions.builder().itemPrice(prices).budgetMaxPrice(10_000).build())).isPresent());
		assertFalse(budget(builder.build(everything, AttackType.SLASH, abyssalDemon, 99,
			BuildOptions.builder().itemPrice(prices).budgetBuild(false).build())).isPresent());
	}

	@Test
	public void priceLookupFailuresCountAsFree()
	{
		IntUnaryOperator broken = id ->
		{
			throw new IllegalStateException("no prices");
		};
		List<Loadout> loadouts = builder.build(everything, AttackType.SLASH, abyssalDemon, 99,
			BuildOptions.builder().itemPrice(broken).build());
		// every item is "free", so the budget build equals the top plain build (the rapier) and
		// the plain copy is de-duplicated; the whip follows
		assertEquals("Budget, under 100k", loadouts.get(0).getReason());
		assertEquals("Ghrazi rapier", loadouts.get(0).weapon().getName());
		assertEquals("Abyssal whip", loadouts.get(1).weapon().getName());
	}

	@Test
	public void formatsTheCap()
	{
		assertEquals("100k", BudgetBuild.formatGp(100_000));
		assertEquals("2m", BudgetBuild.formatGp(2_000_000));
		assertEquals("1500 gp", BudgetBuild.formatGp(1_500));
		assertEquals("2.5m", BudgetBuild.formatGp(2_500_000));
	}
}
