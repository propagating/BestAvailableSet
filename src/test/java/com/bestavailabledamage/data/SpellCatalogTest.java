package com.bestavailabledamage.data;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class SpellCatalogTest
{
	public static SpellCatalog fixture()
	{
		Reader reader = new InputStreamReader(
			SpellCatalogTest.class.getResourceAsStream("/com/bestavailabledamage/spells-fixture.json"),
			StandardCharsets.UTF_8);
		return SpellCatalog.load(new Gson(), reader);
	}

	@Test
	public void strongestCastableSpellForElement()
	{
		SpellCatalog catalog = fixture();
		assertEquals("Fire Surge", catalog.strongest("fire", 99).get().getName());
		assertEquals("Fire Wave", catalog.strongest("fire", 94).get().getName());
		assertEquals("Fire Strike", catalog.strongest("fire", 13).get().getName());
		assertFalse(catalog.strongest("fire", 12).isPresent());
		assertFalse(catalog.strongest("water", 99).isPresent());
		assertEquals("Wind Surge", catalog.strongest("air", 81).get().getName());
	}
}
