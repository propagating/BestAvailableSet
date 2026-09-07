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
package com.bestavailabledamage.player;

import com.bestavailabledamage.data.AttackType;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PrayerChooserTest
{
	private static PlayerProfile prayer(int level, boolean rigour, boolean augury)
	{
		return PlayerProfile.builder().attack(99).strength(99).defence(99).ranged(99).magic(99)
			.prayer(level).hitpoints(99).mining(99).herblore(99)
			.rigourUnlocked(rigour).auguryUnlocked(augury).build();
	}

	@Test
	public void meleeLadder()
	{
		assertEquals(List.of(PrayerChooser.PIETY), PrayerChooser.forType(AttackType.STAB, prayer(70, false, false)));
		assertEquals(List.of(PrayerChooser.CHIVALRY), PrayerChooser.forType(AttackType.SLASH, prayer(69, false, false)));
		assertEquals(List.of(PrayerChooser.ULTIMATE_STRENGTH, PrayerChooser.INCREDIBLE_REFLEXES),
			PrayerChooser.forType(AttackType.CRUSH, prayer(34, false, false)));
		assertEquals(List.of(PrayerChooser.ULTIMATE_STRENGTH, PrayerChooser.IMPROVED_REFLEXES),
			PrayerChooser.forType(AttackType.CRUSH, prayer(31, false, false)));
		assertEquals(List.of(PrayerChooser.SUPERHUMAN_STRENGTH, PrayerChooser.IMPROVED_REFLEXES),
			PrayerChooser.forType(AttackType.CRUSH, prayer(16, false, false)));
		assertEquals(List.of(PrayerChooser.BURST_OF_STRENGTH, PrayerChooser.CLARITY_OF_THOUGHT),
			PrayerChooser.forType(AttackType.CRUSH, prayer(7, false, false)));
		assertEquals(List.of(PrayerChooser.BURST_OF_STRENGTH),
			PrayerChooser.forType(AttackType.CRUSH, prayer(4, false, false)));
		assertEquals(List.of(), PrayerChooser.forType(AttackType.CRUSH, prayer(3, false, false)));
	}

	@Test
	public void rangedLadderNeedsRigourUnlock()
	{
		assertEquals(List.of(PrayerChooser.RIGOUR), PrayerChooser.forType(AttackType.RANGED, prayer(74, true, false)));
		assertEquals(List.of(PrayerChooser.EAGLE_EYE), PrayerChooser.forType(AttackType.RANGED, prayer(74, false, false)));
		assertEquals(List.of(PrayerChooser.HAWK_EYE), PrayerChooser.forType(AttackType.RANGED, prayer(43, true, false)));
		assertEquals(List.of(PrayerChooser.SHARP_EYE), PrayerChooser.forType(AttackType.RANGED, prayer(25, true, false)));
		assertEquals(List.of(), PrayerChooser.forType(AttackType.RANGED, prayer(7, true, false)));
	}

	@Test
	public void magicLadderNeedsAuguryUnlock()
	{
		assertEquals(List.of(PrayerChooser.AUGURY), PrayerChooser.forType(AttackType.MAGIC, prayer(77, false, true)));
		assertEquals(List.of(PrayerChooser.MYSTIC_MIGHT), PrayerChooser.forType(AttackType.MAGIC, prayer(77, false, false)));
		assertEquals(List.of(PrayerChooser.MYSTIC_LORE), PrayerChooser.forType(AttackType.MAGIC, prayer(44, false, true)));
		assertEquals(List.of(PrayerChooser.MYSTIC_WILL), PrayerChooser.forType(AttackType.MAGIC, prayer(9, false, true)));
	}
}
