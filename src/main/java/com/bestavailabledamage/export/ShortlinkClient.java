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
package com.bestavailabledamage.export;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** Creates a share link on the wiki calculator. Callbacks run on the OkHttp thread pool. */
@Slf4j
public class ShortlinkClient
{
	public static final String DEFAULT_ENDPOINT = "https://tools.runescape.wiki/osrs-dps/shortlink";
	public static final String CALC_URL_PREFIX = "https://tools.runescape.wiki/osrs-dps/?id=";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private final OkHttpClient client;
	private final Gson gson;
	private final String endpoint;

	public ShortlinkClient(OkHttpClient client, Gson gson, String endpoint)
	{
		this.client = client;
		this.gson = gson;
		this.endpoint = endpoint;
	}

	public void create(String json, Consumer<String> onId, Consumer<String> onError)
	{
		Request request = new Request.Builder()
			.url(endpoint)
			.post(RequestBody.create(JSON, json))
			.build();
		client.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("shortlink request failed", e);
				onError.accept("Could not reach tools.runescape.wiki");
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (ResponseBody body = response.body())
				{
					if (!response.isSuccessful() || body == null)
					{
						onError.accept("Could not create share link (HTTP " + response.code() + ")");
						return;
					}
					JsonElement parsed = gson.fromJson(body.string(), JsonElement.class);
					if (parsed == null || !parsed.isJsonObject())
					{
						onError.accept("Could not create share link (unexpected response)");
						return;
					}
					JsonObject object = parsed.getAsJsonObject();
					JsonElement data = object.get("data");
					if (data == null || !data.isJsonPrimitive() || !data.getAsJsonPrimitive().isString()
						|| data.getAsString().isEmpty())
					{
						onError.accept("Could not create share link (unexpected response)");
						return;
					}
					onId.accept(data.getAsString());
				}
				catch (IOException | JsonParseException e)
				{
					log.debug("shortlink response unreadable", e);
					onError.accept("Could not create share link (unexpected response)");
				}
			}
		});
	}
}
