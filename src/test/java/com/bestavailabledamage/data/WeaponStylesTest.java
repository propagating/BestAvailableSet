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

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class WeaponStylesTest
{
	@Test
	public void everyCalcCategoryHasStyles()
	{
		List<String> categories = List.of("2h Sword", "Axe", "Banner", "Bladed Staff", "Blaster",
			"Bludgeon", "Blunt", "Bow", "Bulwark", "Chinchompas", "Claw", "Crossbow", "Dagger",
			"Flail", "Gun", "Multi-Melee", "Partisan", "Pickaxe", "Polearm", "Polestaff",
			"Powered Staff", "Powered Wand", "Salamander", "Scythe", "Slash Sword", "Spear",
			"Spiked", "Stab Sword", "Staff", "Thrown", "Unarmed", "Whip");
		for (String c : categories)
		{
			assertFalse("no styles for " + c, WeaponStyles.stylesFor(c).isEmpty());
		}
		assertTrue(WeaponStyles.stylesFor("").isEmpty());
		assertTrue(WeaponStyles.stylesFor("Nonsense").isEmpty());
	}

	@Test
	public void bestStylePrefersTheOffensiveStance()
	{
		CombatStyle stab = WeaponStyles.bestStyleFor("Stab Sword", AttackType.STAB).get();
		assertEquals("Lunge", stab.getName());
		assertEquals("Aggressive", stab.getStance());

		CombatStyle slashLunge = WeaponStyles.bestStyleFor("Slash Sword", AttackType.STAB).get();
		assertEquals("Lunge", slashLunge.getName());
		assertEquals("Controlled", slashLunge.getStance());

		CombatStyle rapid = WeaponStyles.bestStyleFor("Bow", AttackType.RANGED).get();
		assertEquals("Rapid", rapid.getStance());

		CombatStyle autocast = WeaponStyles.bestStyleFor("Staff", AttackType.MAGIC).get();
		assertEquals("Spell", autocast.getName());
		assertEquals("Autocast", autocast.getStance());

		CombatStyle powered = WeaponStyles.bestStyleFor("Powered Staff", AttackType.MAGIC).get();
		assertEquals("Accurate", powered.getStance());

		assertFalse(WeaponStyles.bestStyleFor("Blunt", AttackType.SLASH).isPresent());
		assertFalse(WeaponStyles.bestStyleFor("Whip", AttackType.STAB).isPresent());
	}

	@Test
	public void lookupIgnoresCase()
	{
		assertEquals(WeaponStyles.stylesFor("Blunt"), WeaponStyles.stylesFor("blunt"));
	}
}
