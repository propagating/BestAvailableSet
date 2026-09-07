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

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class OwnedItemsStoreTest
{
	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private OwnedItemsStore store()
	{
		return new OwnedItemsStore(new Gson(), tmp.getRoot().toPath().resolve("bad"));
	}

	@Test
	public void roundTripsSnapshotsAndTimestamps() throws IOException
	{
		OwnedItemsStore store = store();
		OwnedItems owned = new OwnedItems(99L);
		Instant seen = Instant.parse("2026-09-07T10:00:00Z");
		owned.record(new Observation(99L, StorageType.BANK, Set.of(4151, 22324), seen));
		store.save(owned);

		assertTrue(Files.exists(store.fileFor(99L)));
		OwnedItems loaded = store.load(99L);
		assertEquals(99L, loaded.getAccountHash());
		assertEquals(Set.of(4151, 22324), loaded.allIds());
		assertEquals(seen, loaded.lastSeen(StorageType.BANK).get());
	}

	@Test
	public void missingFileLoadsEmpty()
	{
		OwnedItems loaded = store().load(5L);
		assertEquals(5L, loaded.getAccountHash());
		assertTrue(loaded.allIds().isEmpty());
	}

	@Test
	public void corruptFileIsQuarantinedAndLoadsEmpty() throws IOException
	{
		OwnedItemsStore store = store();
		Path file = store.fileFor(6L);
		Files.createDirectories(file.getParent());
		Files.write(file, "{not json".getBytes(StandardCharsets.UTF_8));

		OwnedItems loaded = store.load(6L);
		assertTrue(loaded.allIds().isEmpty());
		try (Stream<Path> files = Files.list(file.getParent()))
		{
			assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith("6.json.corrupt-")));
		}
	}
}
