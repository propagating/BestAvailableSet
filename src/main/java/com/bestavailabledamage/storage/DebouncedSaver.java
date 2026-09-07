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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Coalesces bursts of container changes (opening a bank fires many) into one write after the
 * burst settles. The executor is owned by the plugin, not by this class.
 */
@Slf4j
public class DebouncedSaver
{
	private final OwnedItemsStore store;
	private final ScheduledExecutorService executor;
	private final long delayMillis;
	private final Map<Long, OwnedItems> dirty = new ConcurrentHashMap<>();

	private ScheduledFuture<?> pending;

	public DebouncedSaver(OwnedItemsStore store, ScheduledExecutorService executor, long delayMillis)
	{
		this.store = store;
		this.executor = executor;
		this.delayMillis = delayMillis;
	}

	public synchronized void markDirty(OwnedItems owned)
	{
		dirty.put(owned.getAccountHash(), owned);
		if (pending != null && !pending.isDone())
		{
			pending.cancel(false);
		}
		try
		{
			pending = executor.schedule(this::writeDirty, delayMillis, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			log.debug("Saver is shutting down; dropping scheduled write");
		}
	}

	/**
	 * Cancels any pending write and flushes what is outstanding on the calling thread. Does
	 * not touch the executor - the caller owns it and is responsible for shutting it down.
	 */
	public synchronized void shutdown()
	{
		if (pending != null)
		{
			pending.cancel(false);
			pending = null;
		}
		writeDirty();
	}

	private void writeDirty()
	{
		for (Long accountHash : dirty.keySet())
		{
			OwnedItems owned = dirty.remove(accountHash);
			if (owned == null)
			{
				continue;
			}
			try
			{
				store.save(owned);
			}
			catch (RuntimeException e)
			{
				log.warn("Failed to save account {}", accountHash, e);
			}
		}
	}
}
