package com.bestavailabledamage.data;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class MonsterCatalogTest
{
	public static MonsterCatalog fixture()
	{
		Reader reader = new InputStreamReader(
			MonsterCatalogTest.class.getResourceAsStream("/com/bestavailabledamage/monsters-fixture.json"),
			StandardCharsets.UTF_8);
		return MonsterCatalog.load(new Gson(), reader);
	}

	@Test
	public void searchIsCaseInsensitiveSubstringSortedByNameThenVersion()
	{
		List<MonsterEntry> hits = fixture().search("VORK", 10);
		assertEquals(List.of("Vorkath (Dragon Slayer II)", "Vorkath (Post-quest)"),
			hits.stream().map(MonsterEntry::displayName).collect(Collectors.toList()));
	}

	@Test
	public void searchHonoursLimitAndBlankQueryReturnsNothing()
	{
		assertEquals(1, fixture().search("a", 1).size());
		assertEquals(0, fixture().search("   ", 10).size());
	}

	@Test
	public void elementWeaknessIsEmptyWhenNull()
	{
		MonsterCatalog catalog = fixture();
		assertEquals("fire", catalog.byId(8058).get().elementWeakness().get());
		assertFalse(catalog.byId(415).get().elementWeakness().isPresent());
		assertEquals("General Graardor", catalog.byId(2215).get().displayName());
	}
}
