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

/** One of the five attack types the wiki calculator understands. */
public enum AttackType
{
	STAB("stab", true),
	SLASH("slash", true),
	CRUSH("crush", true),
	RANGED("ranged", false),
	MAGIC("magic", false);

	private final String calcName;
	private final boolean melee;

	AttackType(String calcName, boolean melee)
	{
		this.calcName = calcName;
		this.melee = melee;
	}

	/** The lowercase string the calculator uses for {@code style.type}. */
	public String calcName()
	{
		return calcName;
	}

	public boolean isMelee()
	{
		return melee;
	}

	/** Strength bonus relevant to this type: melee strength, ranged strength or magic damage. */
	public int strengthOf(EquipmentEntry e)
	{
		switch (this)
		{
			case RANGED:
				return e.getRangedStr();
			case MAGIC:
				return e.getMagicStr();
			default:
				return e.getStr();
		}
	}

	public int accuracyOf(EquipmentEntry e)
	{
		switch (this)
		{
			case STAB:
				return e.getStab();
			case SLASH:
				return e.getSlash();
			case CRUSH:
				return e.getCrush();
			case RANGED:
				return e.getRanged();
			default:
				return e.getMagic();
		}
	}
}
