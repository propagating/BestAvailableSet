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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Picks the strongest offensive prayers the player can use. Ids are the calculator's Prayer enum. */
public final class PrayerChooser
{
	public static final int BURST_OF_STRENGTH = 0;
	public static final int CLARITY_OF_THOUGHT = 1;
	public static final int SHARP_EYE = 2;
	public static final int MYSTIC_WILL = 3;
	public static final int SUPERHUMAN_STRENGTH = 4;
	public static final int IMPROVED_REFLEXES = 5;
	public static final int HAWK_EYE = 6;
	public static final int MYSTIC_LORE = 7;
	public static final int ULTIMATE_STRENGTH = 8;
	public static final int INCREDIBLE_REFLEXES = 9;
	public static final int EAGLE_EYE = 10;
	public static final int MYSTIC_MIGHT = 11;
	public static final int CHIVALRY = 12;
	public static final int PIETY = 13;
	public static final int RIGOUR = 14;
	public static final int AUGURY = 15;

	private PrayerChooser()
	{
	}

	public static List<Integer> forType(AttackType type, PlayerProfile profile)
	{
		int level = profile.getPrayer();
		switch (type)
		{
			case RANGED:
				return single(level, profile.isRigourUnlocked(), RIGOUR, 74, EAGLE_EYE, 44, HAWK_EYE, 26, SHARP_EYE, 8);
			case MAGIC:
				return single(level, profile.isAuguryUnlocked(), AUGURY, 77, MYSTIC_MIGHT, 45, MYSTIC_LORE, 27, MYSTIC_WILL, 9);
			default:
				return melee(level);
		}
	}

	private static List<Integer> single(int level, boolean topUnlocked, int top, int topLevel,
		int high, int highLevel, int mid, int midLevel, int low, int lowLevel)
	{
		if (topUnlocked && level >= topLevel)
		{
			return Collections.singletonList(top);
		}
		if (level >= highLevel)
		{
			return Collections.singletonList(high);
		}
		if (level >= midLevel)
		{
			return Collections.singletonList(mid);
		}
		if (level >= lowLevel)
		{
			return Collections.singletonList(low);
		}
		return Collections.emptyList();
	}

	private static List<Integer> melee(int level)
	{
		if (level >= 70)
		{
			return Collections.singletonList(PIETY);
		}
		if (level >= 60)
		{
			return Collections.singletonList(CHIVALRY);
		}
		List<Integer> prayers = new ArrayList<>();
		if (level >= 31)
		{
			prayers.add(ULTIMATE_STRENGTH);
		}
		else if (level >= 13)
		{
			prayers.add(SUPERHUMAN_STRENGTH);
		}
		else if (level >= 4)
		{
			prayers.add(BURST_OF_STRENGTH);
		}
		if (level >= 34)
		{
			prayers.add(INCREDIBLE_REFLEXES);
		}
		else if (level >= 16)
		{
			prayers.add(IMPROVED_REFLEXES);
		}
		else if (level >= 7)
		{
			prayers.add(CLARITY_OF_THOUGHT);
		}
		return prayers;
	}
}
