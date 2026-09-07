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
package com.bestavailabledamage.ui;

import com.bestavailabledamage.build.Loadout;
import com.bestavailabledamage.build.LoadoutBuilder;
import com.bestavailabledamage.data.EquipmentEntry;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/** A titled card listing every slot of one loadout. */
public class LoadoutCard extends JPanel
{
	public LoadoutCard(Loadout loadout)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(6, 8, 6, 8));

		JLabel title = new JLabel(loadout.getName());
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(ColorScheme.BRAND_ORANGE);
		add(title, BorderLayout.NORTH);

		JPanel rows = new JPanel(new GridLayout(0, 1, 0, 1));
		rows.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		for (String slot : LoadoutBuilder.SLOTS)
		{
			EquipmentEntry entry = loadout.getEquipment().get(slot);
			String value = entry == null ? "—" : entry.displayName();
			if (slot.equals("weapon") && loadout.getBlowpipeDart() != null)
			{
				value += " with " + loadout.getBlowpipeDart().getName();
			}
			JLabel row = new JLabel(capitalise(slot) + ": " + value);
			row.setFont(FontManager.getRunescapeSmallFont());
			row.setForeground(entry == null ? ColorScheme.MEDIUM_GRAY_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
			rows.add(row);
		}
		if (loadout.getSpell() != null)
		{
			JLabel spell = new JLabel("Spell: " + loadout.getSpell().getName());
			spell.setFont(FontManager.getRunescapeSmallFont());
			spell.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			rows.add(spell);
		}
		add(rows, BorderLayout.CENTER);
	}

	private static String capitalise(String s)
	{
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
