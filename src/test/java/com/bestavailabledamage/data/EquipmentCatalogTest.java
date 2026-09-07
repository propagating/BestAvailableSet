package com.bestavailabledamage.data;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EquipmentCatalogTest
{
	public static EquipmentCatalog fixture()
	{
		Reader reader = new InputStreamReader(
			EquipmentCatalogTest.class.getResourceAsStream("/com/bestavailabledamage/equipment-fixture.json"),
			StandardCharsets.UTF_8);
		return EquipmentCatalog.load(new Gson(), reader);
	}

	@Test
	public void loadsRowsAndLooksUpById()
	{
		EquipmentCatalog catalog = fixture();
		assertEquals(43, catalog.size());
		EquipmentEntry rapier = catalog.byId(22324).get();
		assertEquals("Ghrazi rapier", rapier.getName());
		assertEquals("Stab Sword", rapier.getCategory());
		assertEquals(93, rapier.getStr());
		assertEquals(100, rapier.getStab());
		assertFalse(catalog.byId(1).isPresent());
	}

	@Test
	public void displayNameIncludesVersionOnlyWhenPresent()
	{
		EquipmentCatalog catalog = fixture();
		assertEquals("Ghrazi rapier", catalog.byId(22324).get().displayName());
		assertEquals("Toxic blowpipe (Charged)", catalog.byId(12926).get().displayName());
	}

	@Test
	public void attackTypeReadsMatchingStrengthAndAccuracy()
	{
		EquipmentCatalog catalog = fixture();
		EquipmentEntry rapier = catalog.byId(22324).get();
		EquipmentEntry tbow = catalog.byId(20997).get();
		EquipmentEntry kodai = catalog.byId(21006).get();
		assertEquals(93, AttackType.STAB.strengthOf(rapier));
		assertEquals(100, AttackType.STAB.accuracyOf(rapier));
		assertEquals(55, AttackType.SLASH.accuracyOf(rapier));
		assertEquals(20, AttackType.RANGED.strengthOf(tbow));
		assertEquals(70, AttackType.RANGED.accuracyOf(tbow));
		assertEquals(150, AttackType.MAGIC.strengthOf(kodai));
		assertEquals(28, AttackType.MAGIC.accuracyOf(kodai));
		assertTrue(AttackType.CRUSH.isMelee());
		assertFalse(AttackType.MAGIC.isMelee());
		assertEquals("stab", AttackType.STAB.calcName());
	}
}
