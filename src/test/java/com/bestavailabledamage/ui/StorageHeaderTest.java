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
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StorageHeaderTest
{
	private final Instant now = Instant.parse("2026-09-07T12:00:00Z");

	@Test
	public void describesAgeInHumanUnits()
	{
		assertEquals("Bank: never seen (open your bank once)",
			StorageHeader.describe(StorageType.BANK, Optional.empty(), now));
		assertEquals("Group storage: never seen",
			StorageHeader.describe(StorageType.GROUP_STORAGE, Optional.empty(), now));
		assertEquals("Inventory: just now",
			StorageHeader.describe(StorageType.INVENTORY, Optional.of(now.minusSeconds(30)), now));
		assertEquals("Bank: 4 min ago",
			StorageHeader.describe(StorageType.BANK, Optional.of(now.minus(Duration.ofMinutes(4))), now));
		assertEquals("Bank: 3 h ago",
			StorageHeader.describe(StorageType.BANK, Optional.of(now.minus(Duration.ofHours(3))), now));
		assertEquals("Equipment: 2 d ago",
			StorageHeader.describe(StorageType.EQUIPMENT, Optional.of(now.minus(Duration.ofDays(2))), now));
	}
}
