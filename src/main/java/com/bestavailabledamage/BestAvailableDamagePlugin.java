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

import com.bestavailabledamage.build.Loadout;
import com.bestavailabledamage.build.LoadoutBuilder;
import com.bestavailabledamage.build.SpeedAdjustedRanker;
import com.bestavailabledamage.data.AttackType;
import com.bestavailabledamage.data.EquipmentCatalog;
import com.bestavailabledamage.data.MonsterCatalog;
import com.bestavailabledamage.data.MonsterEntry;
import com.bestavailabledamage.data.SpellCatalog;
import com.bestavailabledamage.export.SharePayload;
import com.bestavailabledamage.export.ShortlinkClient;
import com.bestavailabledamage.player.PlayerProfile;
import com.bestavailabledamage.storage.AccountContext;
import com.bestavailabledamage.storage.ContainerObserver;
import com.bestavailabledamage.storage.DebouncedSaver;
import com.bestavailabledamage.storage.Observation;
import com.bestavailabledamage.storage.OwnedItems;
import com.bestavailabledamage.storage.OwnedItemsStore;
import com.bestavailabledamage.storage.StorageType;
import com.bestavailabledamage.ui.BestAvailableDamagePanel;
import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDescriptor(
	name = "Best Available Damage",
	description = "Build loadouts from the gear you own for a chosen attack type and target, and open them in the OSRS Wiki DPS calculator",
	tags = {"dps", "gear", "loadout", "wiki"}
)
public class BestAvailableDamagePlugin extends Plugin
{
	private static final String RESOURCE_PREFIX = "/com/bestavailabledamage/";
	private static final long SAVE_DEBOUNCE_MILLIS = 2_000L;

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ItemManager itemManager;
	@Inject
	private Gson gson;
	@Inject
	private OkHttpClient okHttpClient;
	@Inject
	private BestAvailableDamageConfig config;

	private ScheduledExecutorService executor;
	private OwnedItemsStore store;
	private DebouncedSaver saver;
	private AccountContext accountContext;
	private ContainerObserver observer;
	private BestAvailableDamagePanel panel;
	private NavigationButton navButton;
	private ShortlinkClient shortlink;
	private SharePayload payload;

	// set on the executor once the bundled JSON is parsed; read on the client thread
	private volatile LoadoutBuilder builder;
	// the account currently logged in; written on the client thread, read on the EDT via allIds()
	private volatile OwnedItems owned;
	// captured on the client thread during build, used on the OkHttp thread during export
	private volatile PlayerProfile lastProfile;
	// account hash the store has already been loaded for this session; client thread only.
	// LOGGED_IN fires after every LOADING (teleports, region crossings), not just real logins,
	// so this avoids queuing a redundant disk load on each one.
	private Long loadedForAccount;

	@Provides
	BestAvailableDamageConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BestAvailableDamageConfig.class);
	}

	@Override
	protected void startUp()
	{
		executor = Executors.newSingleThreadScheduledExecutor();
		store = new OwnedItemsStore(gson,
			RuneLite.RUNELITE_DIR.toPath().resolve(OwnedItemsStore.DIRECTORY_NAME));
		saver = new DebouncedSaver(store, executor, SAVE_DEBOUNCE_MILLIS);
		accountContext = new AccountContext(client);
		observer = new ContainerObserver(accountContext, itemManager, this::onObservation);
		shortlink = new ShortlinkClient(okHttpClient, gson, ShortlinkClient.DEFAULT_ENDPOINT);
		payload = new SharePayload(gson);

		panel = new BestAvailableDamagePanel(new PanelActions());
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Best Available Damage")
			.icon(icon)
			.priority(8)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		final BestAvailableDamagePanel p = panel;
		executor.execute(() -> loadCatalogs(p));

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(this::onLogin);
		}
	}

	@Override
	protected void shutDown()
	{
		if (saver != null && executor != null)
		{
			final DebouncedSaver s = saver;
			try
			{
				// queue the flush on the executor rather than running it here: shutDown()
				// executes on the client thread, and writeDirty() does synchronous disk IO
				executor.execute(s::shutdown);
			}
			catch (RejectedExecutionException e)
			{
				log.warn("Could not queue final save on shutdown", e);
			}
		}
		saver = null;
		if (executor != null)
		{
			// not shutdownNow(): the queued flush above must still run
			executor.shutdown();
			executor = null;
		}
		loadedForAccount = null;
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}
		if (panel != null)
		{
			panel.dispose();
			panel = null;
		}
		builder = null;
		owned = null;
		lastProfile = null;
		observer = null;
		store = null;
	}

	private void loadCatalogs(BestAvailableDamagePanel target)
	{
		try
		{
			EquipmentCatalog equipment = EquipmentCatalog.load(gson, resource("equipment.json"));
			MonsterCatalog monsters = MonsterCatalog.load(gson, resource("monsters.json"));
			SpellCatalog spells = SpellCatalog.load(gson, resource("spells.json"));
			builder = new LoadoutBuilder(equipment, spells, new SpeedAdjustedRanker());
			SwingUtilities.invokeLater(() ->
			{
				if (panel == target)
				{
					target.setMonsterCatalog(monsters);
				}
			});
		}
		catch (Exception e)
		{
			log.error("bestavailabledamage: could not load bundled data", e);
			SwingUtilities.invokeLater(() ->
			{
				if (panel == target)
				{
					target.setDataError("Data files missing or unreadable; reinstall the plugin");
				}
			});
		}
	}

	private Reader resource(String name)
	{
		InputStream in = getClass().getResourceAsStream(RESOURCE_PREFIX + name);
		if (in == null)
		{
			throw new IllegalStateException("missing resource " + name);
		}
		return new InputStreamReader(in, StandardCharsets.UTF_8);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (observer != null)
		{
			observer.onItemContainerChanged(event);
		}
	}

	/** Client thread. */
	private void onObservation(Observation observation)
	{
		OwnedItems current = owned;
		if (current == null || current.getAccountHash() != observation.getAccountHash())
		{
			current = new OwnedItems(observation.getAccountHash());
			owned = current;
		}
		current.record(observation);
		saver.markDirty(current);
		pushStorageStatus(current);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			onLogin();
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (saver != null)
			{
				executor.execute(saver::shutdown);
			}
			owned = null;
			lastProfile = null;
			loadedForAccount = null;
			pushStorageStatus(null);
		}
	}

	/** Client thread. Starts an in-memory record for this account and merges the disk copy in. */
	private void onLogin()
	{
		if (!accountContext.isObservable())
		{
			return;
		}
		final long hash = accountContext.accountHash();
		OwnedItems current = owned;
		if (current == null || current.getAccountHash() != hash)
		{
			current = new OwnedItems(hash);
			owned = current;
		}
		if (loadedForAccount != null && loadedForAccount == hash)
		{
			// already loaded this account's store this session; LOGGED_IN also fires after
			// teleports and region crossings, not just real logins
			return;
		}
		loadedForAccount = hash;
		final OwnedItems live = current;
		final OwnedItemsStore s = store;
		executor.execute(() ->
		{
			OwnedItems loaded = s.load(hash);
			clientThread.invoke(() ->
			{
				if (owned == live)
				{
					live.mergeMissingFrom(loaded);
					pushStorageStatus(live);
				}
			});
		});
	}

	private void pushStorageStatus(OwnedItems current)
	{
		Map<StorageType, Optional<Instant>> seen = new EnumMap<>(StorageType.class);
		for (StorageType type : StorageType.values())
		{
			seen.put(type, current == null ? Optional.empty() : current.lastSeen(type));
		}
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.setStorageStatus(seen);
			}
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!BestAvailableDamageConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.refreshExportState();
			}
		});
	}

	private class PanelActions implements BestAvailableDamagePanel.Actions
	{
		@Override
		public void build(AttackType type, MonsterEntry target, Consumer<List<Loadout>> onBuilt, Consumer<String> onStatus)
		{
			final LoadoutBuilder b = builder;
			if (b == null)
			{
				onStatus.accept("Data still loading");
				return;
			}
			clientThread.invoke(() ->
			{
				try
				{
					if (client.getGameState() != GameState.LOGGED_IN)
					{
						SwingUtilities.invokeLater(() -> onStatus.accept("Log in first"));
						return;
					}
					PlayerProfile profile = PlayerProfile.capture(client);
					lastProfile = profile;
					OwnedItems current = owned;
					Set<Integer> ids = current == null ? Collections.emptySet() : current.allIds();
					List<Loadout> built = b.build(ids, type, target, profile.getMagic());
					SwingUtilities.invokeLater(() -> onBuilt.accept(built));
				}
				catch (RuntimeException e)
				{
					log.warn("Building loadouts failed", e);
					SwingUtilities.invokeLater(() -> onStatus.accept("Could not build loadouts"));
				}
			});
		}

		@Override
		public void export(List<Loadout> loadouts, AttackType type, MonsterEntry target, Consumer<String> onStatus)
		{
			PlayerProfile profile = lastProfile;
			if (!config.exportToWikiCalc())
			{
				onStatus.accept(BestAvailableDamagePanel.EXPORT_DISABLED_TOOLTIP);
				return;
			}
			if (profile == null)
			{
				onStatus.accept("Build loadouts again before exporting");
				return;
			}
			try
			{
				String json = payload.toJson(loadouts, target, profile, type);
				shortlink.create(json,
					id ->
					{
						try
						{
							LinkBrowser.browse(ShortlinkClient.CALC_URL_PREFIX + id);
							SwingUtilities.invokeLater(() -> onStatus.accept("Opened in your browser"));
						}
						catch (RuntimeException e)
						{
							log.warn("Opening the browser failed", e);
							SwingUtilities.invokeLater(() -> onStatus.accept("Could not open your browser"));
						}
					},
					message -> SwingUtilities.invokeLater(() -> onStatus.accept(message)));
			}
			catch (RuntimeException e)
			{
				log.warn("Creating share link failed", e);
				onStatus.accept("Could not create share link");
			}
		}

		@Override
		public boolean exportEnabled()
		{
			return config.exportToWikiCalc();
		}
	}
}
