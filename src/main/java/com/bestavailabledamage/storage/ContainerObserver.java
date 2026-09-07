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
package com.bestavailabledamage.storage;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.game.ItemManager;

/** Translates {@link ItemContainerChanged} into {@link Observation}s. Holds no state. */
@Slf4j
public class ContainerObserver
{
	private final AccountContext context;
	private final ItemManager itemManager;
	private final Consumer<Observation> sink;

	public ContainerObserver(AccountContext context, ItemManager itemManager, Consumer<Observation> sink)
	{
		this.context = context;
		this.itemManager = itemManager;
		this.sink = sink;
	}

	public void onItemContainerChanged(ItemContainerChanged event)
	{
		try
		{
			if (!context.isObservable())
			{
				return;
			}
			Optional<StorageType> storage = StorageType.forContainer(event.getContainerId());
			ItemContainer container = event.getItemContainer();
			if (!storage.isPresent() || container == null)
			{
				return;
			}
			sink.accept(new Observation(context.accountHash(), storage.get(), idsOf(container), Instant.now()));
		}
		catch (RuntimeException e)
		{
			// never let our failure disrupt the event bus for other plugins
			log.warn("Failed to record container {}", event.getContainerId(), e);
		}
	}

	private Set<Integer> idsOf(ItemContainer container)
	{
		Set<Integer> ids = new HashSet<>();
		for (Item item : container.getItems())
		{
			// empty slot, or a bank placeholder (real id, quantity 0)
			if (item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			ids.add(itemManager.canonicalize(item.getId()));
		}
		return ids;
	}
}
