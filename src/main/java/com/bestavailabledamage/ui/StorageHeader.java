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

import com.bestavailabledamage.storage.StorageType;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/** One line per storage container saying when it was last seen. */
public class StorageHeader extends JPanel
{
	private final Map<StorageType, JLabel> labels = new EnumMap<>(StorageType.class);

	public StorageHeader()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(6, 8, 6, 8));
		for (StorageType type : StorageType.values())
		{
			JLabel label = new JLabel(describe(type, Optional.empty(), Instant.now()));
			label.setFont(FontManager.getRunescapeSmallFont());
			label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			labels.put(type, label);
			add(label);
		}
	}

	public void update(Map<StorageType, Optional<Instant>> lastSeen, Instant now)
	{
		for (StorageType type : StorageType.values())
		{
			Optional<Instant> seen = lastSeen.getOrDefault(type, Optional.empty());
			JLabel label = labels.get(type);
			label.setText(describe(type, seen, now));
			label.setForeground(seen.isPresent() ? ColorScheme.LIGHT_GRAY_COLOR : ColorScheme.PROGRESS_ERROR_COLOR);
		}
	}

	public static String describe(StorageType type, Optional<Instant> seen, Instant now)
	{
		if (!seen.isPresent())
		{
			return type.getDisplayName() + ": never seen"
				+ (type == StorageType.BANK ? " (open your bank once)" : "");
		}
		Duration age = Duration.between(seen.get(), now);
		String text;
		if (age.toMinutes() < 1)
		{
			text = "just now";
		}
		else if (age.toHours() < 1)
		{
			text = age.toMinutes() + " min ago";
		}
		else if (age.toDays() < 1)
		{
			text = age.toHours() + " h ago";
		}
		else
		{
			text = age.toDays() + " d ago";
		}
		return type.getDisplayName() + ": " + text;
	}
}
