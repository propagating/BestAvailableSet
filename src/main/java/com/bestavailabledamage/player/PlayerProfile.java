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

import com.bestavailabledamage.data.MonsterEntry;
import java.util.List;
import java.util.Locale;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/** The player's base levels, prayer unlocks and current slayer task, as the calculator wants them. */
@Slf4j
@Value
@Builder
public class PlayerProfile
{
	/** The slayer target id the game uses for "a boss of your choice" tasks (helper_slayer_current_assignment). */
	private static final int BOSS_TASK_ID = 98;

	int attack;
	int strength;
	int defence;
	int ranged;
	int magic;
	int prayer;
	int hitpoints;
	int mining;
	int herblore;
	boolean rigourUnlocked;
	boolean auguryUnlocked;
	/** The current slayer task's creature name as the game spells it ("Abyssal demons"), or null. */
	String slayerTask;

	/** Must run on the client thread. */
	public static PlayerProfile capture(Client client)
	{
		String task = null;
		try
		{
			task = currentSlayerTask(client);
		}
		catch (RuntimeException e)
		{
			log.debug("Could not read the slayer task", e);
		}
		return PlayerProfile.builder()
			.attack(client.getRealSkillLevel(Skill.ATTACK))
			.strength(client.getRealSkillLevel(Skill.STRENGTH))
			.defence(client.getRealSkillLevel(Skill.DEFENCE))
			.ranged(client.getRealSkillLevel(Skill.RANGED))
			.magic(client.getRealSkillLevel(Skill.MAGIC))
			.prayer(client.getRealSkillLevel(Skill.PRAYER))
			.hitpoints(client.getRealSkillLevel(Skill.HITPOINTS))
			.mining(client.getRealSkillLevel(Skill.MINING))
			.herblore(client.getRealSkillLevel(Skill.HERBLORE))
			.rigourUnlocked(client.getVarbitValue(VarbitID.PRAYER_RIGOUR_UNLOCKED) == 1)
			.auguryUnlocked(client.getVarbitValue(VarbitID.PRAYER_AUGURY_UNLOCKED) == 1)
			.slayerTask(task)
			.build();
	}

	/**
	 * The current task's creature name from the game's slayer tables, the same lookup the
	 * built-in Slayer plugin does. Client thread only. Null when there is no task.
	 */
	static String currentSlayerTask(Client client)
	{
		if (client.getVarpValue(VarPlayerID.SLAYER_COUNT) <= 0)
		{
			return null;
		}
		int taskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
		int row;
		if (taskId == BOSS_TASK_ID)
		{
			List<Integer> bossRows = client.getDBRowsByValue(DBTableID.SlayerTaskSublist.ID,
				DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID, 0,
				client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID));
			if (bossRows.isEmpty())
			{
				return null;
			}
			row = (Integer) client.getDBTableField(bossRows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0)[0];
		}
		else
		{
			List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
			if (rows.isEmpty())
			{
				return null;
			}
			row = rows.get(0);
		}
		return (String) client.getDBTableField(row, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0)[0];
	}

	/**
	 * Whether the current task is for this monster: the task name (singular or plural,
	 * case-insensitive) equals the monster's name or is a prefix of it. A miss never claims
	 * "on task".
	 */
	public boolean slayerTaskMatches(MonsterEntry target)
	{
		if (slayerTask == null || slayerTask.trim().isEmpty())
		{
			return false;
		}
		String task = slayerTask.trim().toLowerCase(Locale.ROOT);
		String singular = task.endsWith("s") ? task.substring(0, task.length() - 1) : task;
		String name = target.getName().toLowerCase(Locale.ROOT);
		return name.equals(task) || name.equals(singular) || name.startsWith(singular + " ");
	}
}
