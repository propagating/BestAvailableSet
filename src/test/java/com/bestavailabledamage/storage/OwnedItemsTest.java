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
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class OwnedItemsTest
{
	@Test
	public void recordReplacesTheContainerAndUnionsAcrossContainers()
	{
		OwnedItems owned = new OwnedItems(42L);
		Instant t1 = Instant.parse("2026-09-07T10:00:00Z");
		Instant t2 = Instant.parse("2026-09-07T10:05:00Z");
		owned.record(new Observation(42L, StorageType.BANK, Set.of(1, 2, 3), t1));
		owned.record(new Observation(42L, StorageType.INVENTORY, Set.of(3, 4), t1));
		assertEquals(Set.of(1, 2, 3, 4), owned.allIds());

		owned.record(new Observation(42L, StorageType.BANK, Set.of(1), t2));
		assertEquals(Set.of(1, 3, 4), owned.allIds());
		assertEquals(t2, owned.lastSeen(StorageType.BANK).get());
		assertEquals(t1, owned.lastSeen(StorageType.INVENTORY).get());
		assertFalse(owned.lastSeen(StorageType.EQUIPMENT).isPresent());
	}

	@Test
	public void copyIsIndependent()
	{
		OwnedItems owned = new OwnedItems(42L);
		owned.record(new Observation(42L, StorageType.BANK, Set.of(1), Instant.EPOCH));
		OwnedItems copy = owned.copy();
		owned.record(new Observation(42L, StorageType.BANK, Set.of(2), Instant.EPOCH));
		assertEquals(Set.of(1), copy.allIds());
		assertEquals(42L, copy.getAccountHash());
	}

	@Test
	public void storageTypeMapsContainerIds()
	{
		assertEquals(StorageType.BANK, StorageType.forContainer(net.runelite.api.gameval.InventoryID.BANK).get());
		assertEquals(StorageType.GROUP_STORAGE,
			StorageType.forContainer(net.runelite.api.gameval.InventoryID.INV_GROUP_TEMP).get());
		assertTrue(StorageType.forContainer(net.runelite.api.gameval.InventoryID.SEED_VAULT).isEmpty());
	}

	@Test
	public void mergeMissingFromKeepsLiveSnapshotsAndFillsGaps()
	{
		OwnedItems live = new OwnedItems(42L);
		live.record(new Observation(42L, StorageType.INVENTORY, Set.of(9), Instant.EPOCH));
		OwnedItems loaded = new OwnedItems(42L);
		loaded.record(new Observation(42L, StorageType.INVENTORY, Set.of(1), Instant.EPOCH));
		loaded.record(new Observation(42L, StorageType.BANK, Set.of(2, 3), Instant.EPOCH));

		live.mergeMissingFrom(loaded);
		assertEquals(Set.of(9, 2, 3), live.allIds());
	}
}
