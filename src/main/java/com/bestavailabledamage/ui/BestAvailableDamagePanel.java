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
import com.bestavailabledamage.data.AttackType;
import com.bestavailabledamage.data.MonsterCatalog;
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.storage.StorageType;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class BestAvailableDamagePanel extends PluginPanel
{
	/** What the panel needs from the plugin. Every callback is invoked on the EDT. */
	public interface Actions
	{
		void build(AttackType type, MonsterEntry target, Consumer<List<Loadout>> onBuilt, Consumer<String> onStatus);

		void export(List<Loadout> loadouts, AttackType type, MonsterEntry target, Consumer<String> onStatus);

		boolean exportEnabled();
	}

	private static final int MAX_RESULTS = 15;
	public static final String EXPORT_DISABLED_TOOLTIP = "Enable 'Export to wiki calc' in the plugin settings";

	private final Actions actions;
	private final StorageHeader header = new StorageHeader();
	private final JComboBox<AttackType> attackType = new JComboBox<>(AttackType.values());
	private final JTextField search = new JTextField();
	private final DefaultListModel<MonsterEntry> resultsModel = new DefaultListModel<>();
	private final JList<MonsterEntry> results = new JList<>(resultsModel);
	private final JLabel target = new JLabel("Target: none");
	private final JButton buildButton = new JButton("Build loadouts");
	private final JPanel cards = new JPanel();
	private final JButton exportButton = new JButton("Open in wiki DPS calc");
	private final JLabel status = new JLabel(" ");

	private MonsterCatalog monsters;
	private MonsterEntry selected;
	private List<Loadout> loadouts = Collections.emptyList();

	public BestAvailableDamagePanel(Actions actions)
	{
		this.actions = actions;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.setBackground(ColorScheme.DARK_GRAY_COLOR);
		controls.setBorder(new EmptyBorder(6, 0, 6, 0));

		attackType.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(capitalise(((AttackType) value).calcName()));
				return this;
			}
		});
		controls.add(labelled("Attack type", attackType));

		search.setToolTipText("Type part of a monster name");
		search.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				refreshResults();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				refreshResults();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				refreshResults();
			}
		});
		controls.add(labelled("Target", search));

		results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		results.setVisibleRowCount(6);
		results.setCellRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				MonsterEntry m = (MonsterEntry) value;
				setText(m.displayName() + "  (lvl " + m.getLevel() + ")");
				setFont(FontManager.getRunescapeSmallFont());
				return this;
			}
		});
		results.addListSelectionListener(e ->
		{
			if (!e.getValueIsAdjusting() && results.getSelectedValue() != null)
			{
				selected = results.getSelectedValue();
				target.setText("Target: " + selected.displayName());
				buildButton.setEnabled(true);
			}
		});
		JScrollPane resultsScroll = new JScrollPane(results);
		resultsScroll.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 110));
		controls.add(resultsScroll);

		target.setFont(FontManager.getRunescapeSmallFont());
		target.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		controls.add(target);

		buildButton.setEnabled(false);
		buildButton.addActionListener(e -> build());
		controls.add(buildButton);

		JPanel top = new JPanel(new BorderLayout());
		top.setBackground(ColorScheme.DARK_GRAY_COLOR);
		top.add(header, BorderLayout.NORTH);
		top.add(controls, BorderLayout.CENTER);
		add(top, BorderLayout.NORTH);

		cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
		cards.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane cardScroll = new JScrollPane(cards);
		cardScroll.setBorder(null);
		add(cardScroll, BorderLayout.CENTER);

		JPanel footer = new JPanel();
		footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
		footer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		exportButton.addActionListener(e -> export());
		footer.add(exportButton);
		status.setFont(FontManager.getRunescapeSmallFont());
		status.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		footer.add(status);
		add(footer, BorderLayout.SOUTH);

		refreshExportState();
		setSearchEnabled(false, "Loading data...");
	}

	private static JPanel labelled(String text, Component field)
	{
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		row.add(label, BorderLayout.WEST);
		row.add(field, BorderLayout.CENTER);
		return row;
	}

	private static String capitalise(String s)
	{
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public void setMonsterCatalog(MonsterCatalog catalog)
	{
		this.monsters = catalog;
		setSearchEnabled(true, "");
		refreshResults();
	}

	public void setDataError(String message)
	{
		setSearchEnabled(false, message);
	}

	private void setSearchEnabled(boolean enabled, String message)
	{
		search.setEnabled(enabled);
		attackType.setEnabled(enabled);
		results.setEnabled(enabled);
		buildButton.setEnabled(enabled && selected != null);
		if (!message.isEmpty())
		{
			status.setText(message);
		}
	}

	public void setStorageStatus(Map<StorageType, Optional<Instant>> lastSeen)
	{
		header.update(lastSeen, Instant.now());
	}

	public void refreshExportState()
	{
		boolean enabled = actions.exportEnabled();
		exportButton.setEnabled(enabled && !loadouts.isEmpty());
		exportButton.setToolTipText(enabled ? "Create a share link and open it in your browser" : EXPORT_DISABLED_TOOLTIP);
	}

	private void refreshResults()
	{
		resultsModel.clear();
		if (monsters == null)
		{
			return;
		}
		for (MonsterEntry m : monsters.search(search.getText(), MAX_RESULTS))
		{
			resultsModel.addElement(m);
		}
	}

	private void build()
	{
		if (selected == null)
		{
			return;
		}
		AttackType type = (AttackType) attackType.getSelectedItem();
		buildButton.setEnabled(false);
		status.setText("Building...");
		actions.build(type, selected, built ->
		{
			loadouts = built;
			cards.removeAll();
			if (built.isEmpty())
			{
				JLabel none = new JLabel("No " + type.calcName() + " weapon found in your storage");
				none.setFont(FontManager.getRunescapeSmallFont());
				none.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				cards.add(none);
			}
			for (Loadout loadout : built)
			{
				cards.add(new LoadoutCard(loadout));
				cards.add(javax.swing.Box.createVerticalStrut(6));
			}
			cards.revalidate();
			cards.repaint();
			status.setText(built.isEmpty() ? " " : built.size() + " loadout(s) for " + selected.displayName());
			buildButton.setEnabled(true);
			refreshExportState();
		}, message ->
		{
			status.setText(message);
			buildButton.setEnabled(true);
		});
	}

	private void export()
	{
		if (loadouts.isEmpty() || selected == null)
		{
			return;
		}
		exportButton.setEnabled(false);
		status.setText("Creating share link...");
		actions.export(new ArrayList<>(loadouts), (AttackType) attackType.getSelectedItem(), selected, message ->
		{
			status.setText(message);
			refreshExportState();
		});
	}

	public void dispose()
	{
		cards.removeAll();
		resultsModel.clear();
		loadouts = Collections.emptyList();
	}
}
