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
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.data.SpellCatalog;
import com.bestavailabledamage.data.SpellCatalogTest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class LoadoutBuilderTest
{
	private final EquipmentCatalog catalog = EquipmentCatalogTest.fixture();
	private final MonsterCatalog monsters = MonsterCatalogTest.fixture();
	private final SpellCatalog spells = SpellCatalogTest.fixture();
	private final LoadoutBuilder builder = new LoadoutBuilder(catalog, spells, new SpeedAdjustedRanker());

	private final MonsterEntry vorkath = monsters.byId(8058).get();       // fire weakness
	private final MonsterEntry abyssalDemon = monsters.byId(415).get();   // no weakness

	private static Set<Integer> everything(EquipmentCatalog catalog)
	{
		return catalog.all().stream().map(e -> e.getId()).collect(Collectors.toSet());
	}

	private static List<String> weaponNames(List<Loadout> loadouts)
	{
		return loadouts.stream().map(l -> l.weapon().getName()).collect(Collectors.toList());
	}

	@Test
	public void oneLoadoutPerStabWeaponRankedBestFirst()
	{
		List<Loadout> loadouts = builder.build(everything(catalog), AttackType.STAB, abyssalDemon, 99);
		// stab-capable in fixture: rapier (39.25), inquisitor's mace via Spike (40.0), lance (33.5),
		// dragon scimitar via Lunge (32.5)
		assertEquals(List.of("Inquisitor's mace", "Ghrazi rapier", "Dragon hunter lance", "Dragon scimitar"),
			weaponNames(loadouts));
		Loadout rapier = loadouts.get(1);
		assertEquals("Ghrazi rapier", rapier.getName());
		assertEquals("Lunge", rapier.getStyle().getName());
		assertEquals("Aggressive", rapier.getStyle().getStance());
		assertEquals("Torva full helm", rapier.getEquipment().get("head").getName());
		assertEquals("Infernal cape", rapier.getEquipment().get("cape").getName());
		assertEquals("Amulet of torture", rapier.getEquipment().get("neck").getName());
		assertEquals("Bandos chestplate", rapier.getEquipment().get("body").getName());
		assertEquals("Avernic defender", rapier.getEquipment().get("shield").getName());
		assertEquals(22322, rapier.getEquipment().get("shield").getId());
		assertEquals("Bandos tassets", rapier.getEquipment().get("legs").getName());
		assertEquals("Ferocious gloves", rapier.getEquipment().get("hands").getName());
		assertEquals("Primordial boots", rapier.getEquipment().get("feet").getName());
		assertEquals("Ultor ring", rapier.getEquipment().get("ring").getName());
		assertNull(rapier.getEquipment().get("ammo"));
		assertNull(rapier.getSpell());
	}

	@Test
	public void capsAtSixAndDedupesByBaseId()
	{
		Set<Integer> owned = everything(catalog);
		List<Loadout> loadouts = builder.build(owned, AttackType.CRUSH, abyssalDemon, 99);
		// crush-capable: inquisitor's, elder maul, dragon warhammer, rapier? no. lance (Pound), kodai/nightmare (Bash/Pound), dragon scimitar? no
		assertTrue(loadouts.size() <= LoadoutBuilder.MAX_LOADOUTS);
		assertEquals(6, loadouts.size());
	}

	@Test
	public void twoHandedWeaponClearsShield()
	{
		List<Loadout> loadouts = builder.build(everything(catalog), AttackType.CRUSH, abyssalDemon, 99);
		Loadout maul = loadouts.stream().filter(l -> l.weapon().getId() == 21003).findFirst().get();
		assertNull(maul.getEquipment().get("shield"));
	}

	@Test
	public void rangedAmmoFollowsWeaponKind()
	{
		List<Loadout> loadouts = builder.build(everything(catalog), AttackType.RANGED, abyssalDemon, 99);
		Loadout tbow = loadouts.stream().filter(l -> l.weapon().getId() == 20997).findFirst().get();
		assertEquals("Dragon arrow", tbow.getEquipment().get("ammo").getName());
		assertNull(tbow.getBlowpipeDart());

		Loadout acb = loadouts.stream().filter(l -> l.weapon().getId() == 11785).findFirst().get();
		assertEquals("Ruby dragon bolts (e)", acb.getEquipment().get("ammo").getName());
		// the defender has -4 ranged accuracy, so the neutral spirit shield wins the shield slot
		assertEquals("Elysian spirit shield", acb.getEquipment().get("shield").getName());

		Loadout blowpipe = loadouts.stream().filter(l -> l.weapon().getId() == 12926).findFirst().get();
		assertNull(blowpipe.getEquipment().get("ammo"));
		assertEquals("Dragon dart", blowpipe.getBlowpipeDart().getName());
		assertEquals("Masori body (f)", blowpipe.getEquipment().get("body").getName());
		assertEquals("Rapid", blowpipe.getStyle().getStance());
	}

	@Test
	public void dartsAreNotWeaponLoadoutsOfTheirOwn()
	{
		List<Loadout> loadouts = builder.build(everything(catalog), AttackType.RANGED, abyssalDemon, 99);
		assertTrue(weaponNames(loadouts).stream().noneMatch(n -> n.endsWith("dart")));
	}

	@Test
	public void magicWithoutWeaknessIsOneGroupOfPoweredAndUnpoweredStaves()
	{
		List<Loadout> loadouts = builder.build(everything(catalog), AttackType.MAGIC, abyssalDemon, 99);
		assertEquals(4, loadouts.size());
		assertTrue(loadouts.stream().allMatch(l -> l.getSpell() == null));
		Loadout kodai = loadouts.stream().filter(l -> l.weapon().getId() == 21006).findFirst().get();
		assertEquals("Ancestral hat", kodai.getEquipment().get("head").getName());
		assertEquals("Occult necklace", kodai.getEquipment().get("neck").getName());
		assertEquals("Tormented bracelet", kodai.getEquipment().get("hands").getName());
		assertEquals("Magus ring", kodai.getEquipment().get("ring").getName());
		assertEquals("Tome of Fire", kodai.getEquipment().get("shield").getName());
	}

	@Test
	public void magicWithFireWeaknessSplitsElementalAndPowered()
	{
		List<Loadout> loadouts = builder.build(everything(catalog), AttackType.MAGIC, vorkath, 99);
		assertEquals(4, loadouts.size());
		Loadout kodai = loadouts.get(0);
		assertEquals("Kodai wand", kodai.weapon().getName());
		assertEquals("Fire Surge", kodai.getSpell().getName());
		assertEquals("Kodai wand + Fire Surge", kodai.getName());
		assertEquals("Spell", kodai.getStyle().getName());
		assertEquals("Autocast", kodai.getStyle().getStance());
		assertEquals("Tome of Fire", kodai.getEquipment().get("shield").getName());
		assertEquals("Nightmare staff", loadouts.get(1).weapon().getName());
		assertNull(loadouts.get(2).getSpell());
		assertNull(loadouts.get(3).getSpell());
		assertEquals("Sanguinesti staff", loadouts.get(2).weapon().getName());
		assertEquals("Tumeken's shadow", loadouts.get(3).weapon().getName());
		assertNull(loadouts.get(3).getEquipment().get("shield"));
	}

	@Test
	public void elementalGroupIsEmptyWhenNoSpellIsCastable()
	{
		List<Loadout> loadouts = builder.build(everything(catalog), AttackType.MAGIC, vorkath, 12);
		assertEquals(4, loadouts.size());
		assertTrue(loadouts.stream().allMatch(l -> l.getSpell() == null));
	}

	@Test
	public void emptyWhenNoWeaponOfThatTypeIsOwned()
	{
		Set<Integer> owned = new HashSet<>(Set.of(26382, 19553));
		assertTrue(builder.build(owned, AttackType.SLASH, abyssalDemon, 99).isEmpty());
	}

	@Test
	public void unknownIdsAreIgnored()
	{
		Set<Integer> owned = new HashSet<>(Set.of(4151, 123456789));
		List<Loadout> loadouts = builder.build(owned, AttackType.SLASH, abyssalDemon, 99);
		assertEquals(1, loadouts.size());
		assertNull(loadouts.get(0).getEquipment().get("head"));
	}

	@Test
	public void salamanderIsExcludedFromElementalCasterPool()
	{
		// a Salamander offers a Magic style (Blaze) but cannot autocast a spell; it must not
		// be treated as an elemental caster even though it ranks above the real staves here.
		EquipmentEntry salamander = new EquipmentEntry(90210, 90210, "Test Salamander", null,
			"weapon", "Salamander", true, 1, 0, 0, 200, 0, 0, 0, 90, 0);
		List<EquipmentEntry> withSalamander = new ArrayList<>(catalog.all());
		withSalamander.add(salamander);
		EquipmentCatalog augmented = new EquipmentCatalog(withSalamander);
		LoadoutBuilder builderWithSalamander = new LoadoutBuilder(augmented, spells, new SpeedAdjustedRanker());

		Set<Integer> owned = everything(augmented);
		List<Loadout> loadouts = builderWithSalamander.build(owned, AttackType.MAGIC, vorkath, 99);
		assertTrue(weaponNames(loadouts).stream().noneMatch(n -> n.equals("Test Salamander")));

		// it is still eligible in the single-group path when the target has no weakness
		List<Loadout> noWeakness = builderWithSalamander.build(owned, AttackType.MAGIC, abyssalDemon, 99);
		assertTrue(weaponNames(noWeakness).stream().anyMatch(n -> n.equals("Test Salamander")));
	}
}
