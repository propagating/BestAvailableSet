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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContainerObserverTest
{
	private AccountContext context;
	private ItemManager itemManager;
	private List<Observation> sink;
	private ContainerObserver observer;

	@Before
	public void setUp()
	{
		context = mock(AccountContext.class);
		when(context.isObservable()).thenReturn(true);
		when(context.accountHash()).thenReturn(7L);
		itemManager = mock(ItemManager.class);
		when(itemManager.canonicalize(anyInt())).thenAnswer(inv -> inv.getArgument(0));
		sink = new ArrayList<>();
		observer = new ContainerObserver(context, itemManager, sink::add);
	}

	private static ItemContainerChanged event(int containerId, Item... items)
	{
		ItemContainer container = mock(ItemContainer.class);
		when(container.getItems()).thenReturn(items);
		return new ItemContainerChanged(containerId, container);
	}

	@Test
	public void bankEventBecomesObservationWithoutPlaceholdersOrEmptySlots()
	{
		observer.onItemContainerChanged(event(InventoryID.BANK,
			new Item(4151, 1), new Item(-1, 0), new Item(22324, 0), new Item(892, 500)));
		assertEquals(1, sink.size());
		Observation o = sink.get(0);
		assertEquals(StorageType.BANK, o.getStorage());
		assertEquals(7L, o.getAccountHash());
		assertEquals(Set.of(4151, 892), o.getItemIds());
	}

	@Test
	public void canonicalisesNotedIds()
	{
		when(itemManager.canonicalize(4152)).thenReturn(4151);
		observer.onItemContainerChanged(event(InventoryID.INV, new Item(4152, 3)));
		assertEquals(Set.of(4151), sink.get(0).getItemIds());
	}

	@Test
	public void ignoresOtherContainersAndUnobservableState()
	{
		observer.onItemContainerChanged(event(InventoryID.SEED_VAULT, new Item(1, 1)));
		assertTrue(sink.isEmpty());
		when(context.isObservable()).thenReturn(false);
		observer.onItemContainerChanged(event(InventoryID.BANK, new Item(1, 1)));
		assertTrue(sink.isEmpty());
	}
}
