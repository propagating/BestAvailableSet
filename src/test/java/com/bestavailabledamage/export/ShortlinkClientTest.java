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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ShortlinkClientTest
{
	private MockWebServer server;
	private ShortlinkClient client;

	@Before
	public void setUp() throws Exception
	{
		server = new MockWebServer();
		server.start();
		client = new ShortlinkClient(new OkHttpClient(), new Gson(), server.url("/shortlink").toString());
	}

	@After
	public void tearDown() throws Exception
	{
		server.shutdown();
	}

	private static class Outcome
	{
		final CompletableFuture<String> id = new CompletableFuture<>();
		final CompletableFuture<String> error = new CompletableFuture<>();
	}

	private Outcome call(String json) throws Exception
	{
		Outcome o = new Outcome();
		client.create(json, o.id::complete, o.error::complete);
		CompletableFuture.anyOf(o.id, o.error).get(5, TimeUnit.SECONDS);
		return o;
	}

	@Test
	public void postsJsonAndReturnsTheId() throws Exception
	{
		server.enqueue(new MockResponse().setBody("{\"data\":\"abc123\"}"));
		Outcome o = call("{\"loadouts\":[]}");
		assertEquals("abc123", o.id.get());
		RecordedRequest req = server.takeRequest();
		assertEquals("POST", req.getMethod());
		assertTrue(req.getHeader("Content-Type").startsWith("application/json"));
		assertEquals("{\"loadouts\":[]}", req.getBody().readUtf8());
	}

	@Test
	public void httpErrorReportsStatus() throws Exception
	{
		server.enqueue(new MockResponse().setResponseCode(503));
		Outcome o = call("{}");
		assertEquals("Could not create share link (HTTP 503)", o.error.get());
	}

	@Test
	public void malformedBodyReportsError() throws Exception
	{
		server.enqueue(new MockResponse().setBody("not json"));
		Outcome o = call("{}");
		assertEquals("Could not create share link (unexpected response)", o.error.get());
	}

	@Test
	public void unreachableHostReportsError() throws Exception
	{
		server.shutdown();
		Outcome o = call("{}");
		assertEquals("Could not reach tools.runescape.wiki", o.error.get());
	}
}
