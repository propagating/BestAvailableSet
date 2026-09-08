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
package com.bestavailabledamage;

import com.bestavailabledamage.build.BuildOptions;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(BestAvailableDamageConfig.GROUP)
public interface BestAvailableDamageConfig extends Config
{
	String GROUP = "bestavailabledamage";

	@ConfigItem(
		keyName = "exportToWikiCalc",
		name = "Export to wiki calc",
		description = "Allow the 'Open in wiki DPS calc' button to create a share link on tools.runescape.wiki",
		warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers",
		position = 1
	)
	default boolean exportToWikiCalc()
	{
		return false;
	}

	@ConfigSection(
		name = "Guaranteed builds",
		description = "Builds that always get one of the six loadout slots when they can be made",
		position = 2
	)
	String guaranteedSection = "guaranteed";

	@ConfigItem(
		keyName = "guaranteedSlayerBuild",
		name = "Slayer helmet build",
		description = "For slayer monsters, one loadout wears your slayer helmet, exported with the calculator's on-task toggle",
		section = guaranteedSection,
		position = 1
	)
	default boolean guaranteedSlayerBuild()
	{
		return true;
	}

	@ConfigItem(
		keyName = "guaranteedVoidBuild",
		name = "Void build",
		description = "One loadout in a full Void or Elite Void set when you own it",
		section = guaranteedSection,
		position = 2
	)
	default boolean guaranteedVoidBuild()
	{
		return true;
	}

	@ConfigItem(
		keyName = "guaranteedBudgetBuild",
		name = "Budget build",
		description = "One loadout using only items at or under the price below (untradeables count as free)",
		section = guaranteedSection,
		position = 3
	)
	default boolean guaranteedBudgetBuild()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "budgetMaxPrice",
		name = "Budget max item price",
		description = "Grand Exchange price per item for the budget build",
		section = guaranteedSection,
		position = 4
	)
	default int budgetMaxPrice()
	{
		return BuildOptions.DEFAULT_BUDGET_MAX_PRICE;
	}
}
