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
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * One JSON file per account under {@code .runelite/best-available-damage/}. Writes go to a
 * sibling .tmp file and are renamed into place so a crash never leaves a torn file.
 */
@Slf4j
public class OwnedItemsStore
{
	public static final String DIRECTORY_NAME = "best-available-damage";

	private final Gson gson;
	private final Path root;

	public OwnedItemsStore(Gson gson, Path root)
	{
		this.gson = gson;
		this.root = root;
	}

	public Path fileFor(long accountHash)
	{
		return root.resolve(accountHash + ".json");
	}

	public OwnedItems load(long accountHash)
	{
		Path file = fileFor(accountHash);
		if (!Files.exists(file))
		{
			return new OwnedItems(accountHash);
		}
		try
		{
			String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
			OwnedItems loaded = gson.fromJson(json, OwnedItems.class);
			if (loaded == null || loaded.getSnapshots() == null)
			{
				throw new JsonParseException("empty document");
			}
			return loaded;
		}
		catch (IOException e)
		{
			log.warn("Could not read {}", file, e);
			return new OwnedItems(accountHash);
		}
		catch (JsonParseException e)
		{
			quarantine(file);
			return new OwnedItems(accountHash);
		}
	}

	public void save(OwnedItems owned)
	{
		Path file = fileFor(owned.getAccountHash());
		Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
		String json = gson.toJson(owned.copy());
		try
		{
			Files.createDirectories(root);
			Files.write(tmp, json.getBytes(StandardCharsets.UTF_8));
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e)
		{
			log.warn("Could not save {}; keeping data in memory", file, e);
		}
	}

	private void quarantine(Path file)
	{
		Path aside = file.resolveSibling(file.getFileName() + ".corrupt-" + Instant.now().toEpochMilli());
		try
		{
			Files.move(file, aside, StandardCopyOption.REPLACE_EXISTING);
			log.warn("Unreadable store {} moved to {}", file, aside);
		}
		catch (IOException e)
		{
			log.warn("Unreadable store {} could not be moved aside", file, e);
		}
	}
}
