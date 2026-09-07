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
package com.bestavailabledamage.export;

import com.bestavailabledamage.build.Loadout;
import com.bestavailabledamage.build.LoadoutBuilder;
import com.bestavailabledamage.data.AttackType;
import com.bestavailabledamage.data.EquipmentEntry;
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.player.PlayerProfile;
import com.bestavailabledamage.player.PrayerChooser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * Builds the JSON the wiki calculator's shortlink endpoint stores. The site re-resolves
 * equipment by id and spells by name, and re-reads the monster by id and version, so only
 * ids, levels, prayers and the monster inputs travel.
 */
public class SharePayload
{
	/** Mirrors IMPORT_VERSION in the calculator's src/types/State.ts. */
	public static final int SERIALIZATION_VERSION = 10;

	private final Gson gson;

	public SharePayload(Gson gson)
	{
		// the golden payload needs explicit "null" for empty slots and an absent spell; the
		// injected Gson may not have serializeNulls() enabled, so derive one that does
		this.gson = gson.newBuilder().serializeNulls().create();
	}

	public String toJson(List<Loadout> loadouts, MonsterEntry target, PlayerProfile profile, AttackType type)
	{
		JsonObject root = new JsonObject();
		root.addProperty("serializationVersion", SERIALIZATION_VERSION);
		root.addProperty("selectedLoadout", 0);
		JsonArray array = new JsonArray();
		List<Integer> prayers = PrayerChooser.forType(type, profile);
		for (Loadout loadout : loadouts)
		{
			array.add(loadout(loadout, profile, prayers));
		}
		root.add("loadouts", array);
		root.add("monster", monster(target));
		return gson.toJson(root);
	}

	private JsonObject loadout(Loadout loadout, PlayerProfile profile, List<Integer> prayers)
	{
		JsonObject o = new JsonObject();
		o.addProperty("name", loadout.getName());

		JsonObject style = new JsonObject();
		style.addProperty("name", loadout.getStyle().getName());
		style.addProperty("type", loadout.getStyle().getType().calcName());
		style.addProperty("stance", loadout.getStyle().getStance());
		o.add("style", style);

		JsonObject skills = new JsonObject();
		skills.addProperty("atk", profile.getAttack());
		skills.addProperty("str", profile.getStrength());
		skills.addProperty("def", profile.getDefence());
		skills.addProperty("ranged", profile.getRanged());
		skills.addProperty("magic", profile.getMagic());
		skills.addProperty("prayer", profile.getPrayer());
		skills.addProperty("hp", profile.getHitpoints());
		skills.addProperty("mining", profile.getMining());
		skills.addProperty("herblore", profile.getHerblore());
		o.add("skills", skills);

		JsonArray prayerArray = new JsonArray();
		for (Integer p : prayers)
		{
			prayerArray.add(p);
		}
		o.add("prayers", prayerArray);

		JsonObject equipment = new JsonObject();
		for (String slot : LoadoutBuilder.SLOTS)
		{
			EquipmentEntry entry = loadout.getEquipment().get(slot);
			if (entry == null)
			{
				equipment.add(slot, JsonNull.INSTANCE);
				continue;
			}
			JsonObject piece = new JsonObject();
			piece.addProperty("id", entry.getId());
			if (slot.equals("weapon") && loadout.getBlowpipeDart() != null)
			{
				JsonObject vars = new JsonObject();
				vars.addProperty("blowpipeDartId", loadout.getBlowpipeDart().getId());
				vars.addProperty("blowpipeDartName", loadout.getBlowpipeDart().getName());
				piece.add("itemVars", vars);
			}
			equipment.add(slot, piece);
		}
		o.add("equipment", equipment);

		if (loadout.getSpell() == null)
		{
			o.add("spell", JsonNull.INSTANCE);
		}
		else
		{
			JsonObject spell = new JsonObject();
			spell.addProperty("name", loadout.getSpell().getName());
			o.add("spell", spell);
		}
		return o;
	}

	private static JsonObject monster(MonsterEntry target)
	{
		JsonObject m = new JsonObject();
		m.addProperty("id", target.getId());
		m.addProperty("name", target.getName());
		m.addProperty("version", target.getVersion());

		// the calculator's INITIAL_MONSTER_INPUTS; the import reads inputs.defenceReductions unconditionally
		JsonObject inputs = new JsonObject();
		inputs.addProperty("isFromCoxCm", false);
		inputs.addProperty("toaInvocationLevel", 0);
		inputs.addProperty("toaPathLevel", 0);
		inputs.addProperty("partyMaxCombatLevel", 126);
		inputs.addProperty("partySumMiningLevel", 99);
		inputs.addProperty("partyMaxHpLevel", 99);
		inputs.addProperty("partySize", 1);
		inputs.addProperty("monsterCurrentHp", 150);
		JsonObject reductions = new JsonObject();
		reductions.addProperty("vulnerability", false);
		reductions.addProperty("accursed", false);
		for (String key : new String[]{"elderMaul", "dwh", "arclight", "emberlight", "bgs", "tonalztic", "seercull", "ayak"})
		{
			reductions.addProperty(key, 0);
		}
		inputs.add("defenceReductions", reductions);
		m.add("inputs", inputs);
		return m;
	}
}
