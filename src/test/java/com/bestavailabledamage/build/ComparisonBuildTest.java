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
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** "Compare styles": one card per attack type, then the strongest melee type's next cards. */
public class ComparisonBuildTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final MonsterEntry abyssalDemon = MonsterCatalogTest.fixture().byId(415).get();
	private final LoadoutBuilder builder = new LoadoutBuilder(catalog, SpellCatalogTest.fixture(), new SpeedAdjustedRanker());
	private final Set<Integer> everything = catalog.all().stream().map(EquipmentEntry::getId).collect(Collectors.toSet());

	private static List<AttackType> types(List<Loadout> cards)
	{
		return cards.stream().map(l -> l.getStyle().getType()).collect(Collectors.toList());
	}

	private static List<String> names(List<Loadout> cards)
	{
		return cards.stream().map(Loadout::getName).collect(Collectors.toList());
	}

	@Test
	public void oneCardPerAttackTypeThenTheStrongestMeleeTypesSecondCard()
	{
		List<Loadout> cards = builder.buildComparison(everything, abyssalDemon, 99, BuildOptions.defaults());
		assertEquals(6, cards.size());
		assertEquals(List.of(AttackType.STAB, AttackType.SLASH, AttackType.CRUSH, AttackType.RANGED,
			AttackType.MAGIC, AttackType.STAB), types(cards));
		assertEquals(List.of(
			"Emberlight (vs demon, Slayer helm, if on task)",
			"Emberlight (vs demon, Slayer helm, if on task)",
			"Inquisitor's mace (Slayer helm, if on task)",
			"Bow of Faerdhinen (Slayer helm, if on task)",
			"Kodai wand (Slayer helm, if on task)",
			// stab and crush tie on the top plain weapon (the mace, 40.0); stab comes first
			"Inquisitor's mace (Slayer helm, if on task)"), names(cards));
		assertEquals("Lunge", cards.get(0).getStyle().getName());
		assertEquals("Slash", cards.get(1).getStyle().getName());
		assertEquals("Spike", cards.get(5).getStyle().getName());
	}

	@Test
	public void typesWithoutAWeaponGiveTheirSlotsToTheOthers()
	{
		Set<Integer> meleeOnly = new HashSet<>(everything);
		// every ranged and magic weapon, plus the darts
		meleeOnly.removeAll(Set.of(20997, 861, 11785, 12926, 11230, 810, 23983, 25865, 21012,
			21006, 24422, 22323, 27275));
		List<Loadout> cards = builder.buildComparison(meleeOnly, abyssalDemon, 99, BuildOptions.defaults());
		assertEquals(6, cards.size());
		assertTrue(cards.stream().allMatch(l -> l.getStyle().getType().isMelee()));
		assertEquals(List.of(AttackType.STAB, AttackType.SLASH, AttackType.CRUSH), types(cards).subList(0, 3));
	}

	@Test
	public void sameGearWithTheSameStyleIsNotRepeated()
	{
		// only the whip: stab and crush have no card, slash has exactly one distinct card
		// per style even though the layered and plain lists overlap
		Set<Integer> whipOnly = new HashSet<>(Set.of(4151, 26382, 21295));
		List<Loadout> cards = builder.buildComparison(whipOnly, MonsterCatalogTest.fixture().byId(2042).get(),
			99, BuildOptions.builder().budgetBuild(false).build());
		assertEquals(1, cards.size());
		assertEquals("Abyssal whip", cards.get(0).getName());
	}
}
