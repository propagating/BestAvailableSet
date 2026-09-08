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
package com.bestavailabledamage.build;

import java.util.function.IntUnaryOperator;
import lombok.Builder;
import lombok.Value;

/** Which guaranteed builds to make and what they need to know. */
@Value
@Builder(toBuilder = true)
public class BuildOptions
{
	public static final int DEFAULT_BUDGET_MAX_PRICE = 100_000;

	/** Situational items (Salve, dragon hunter weapons, ...); no config toggle. */
	@Builder.Default
	boolean situationalBuilds = true;
	@Builder.Default
	boolean slayerBuild = true;
	@Builder.Default
	boolean voidBuild = true;
	@Builder.Default
	boolean budgetBuild = true;
	/** Per-item GE price cap for the budget build. */
	@Builder.Default
	int budgetMaxPrice = DEFAULT_BUDGET_MAX_PRICE;
	/** Item id to GE price; 0 means untradeable or unknown, which counts as within the cap. */
	@Builder.Default
	IntUnaryOperator itemPrice = id -> 0;
	/** True when the client confirms the current slayer task matches the target. */
	@Builder.Default
	boolean onSlayerTask = false;

	public static BuildOptions defaults()
	{
		return builder().build();
	}
}
