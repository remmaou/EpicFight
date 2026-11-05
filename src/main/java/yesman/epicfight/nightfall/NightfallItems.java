package yesman.epicfight.nightfall;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;

public final class NightfallItems {
	private NightfallItems() {}
	
	/// Memicu inisialisasi kelas (mendaftarkan holder).
	public static void init() {}
	
	public static final DeferredHolder<Item, ScytheItem> CRIMSON_MOON = NightfallRegistries.ITEMS.register("crimson_moon", () ->
		new ScytheItem(Tiers.NETHERITE, new Item.Properties()
			.rarity(Rarity.EPIC)
			.fireResistant()
			.component(DataComponents.UNBREAKABLE, new Unbreakable(false))
			.attributes(ScytheItem.createScytheAttributes(Tiers.NETHERITE)))
	);
	
	/// Varian ungu Crimson Moon (kemampuan sama, warna/glow ungu).
	public static final DeferredHolder<Item, ScytheItem> SHADOW = NightfallRegistries.ITEMS.register("shadow", () ->
		new ScytheItem(Tiers.NETHERITE, new Item.Properties()
			.rarity(Rarity.EPIC)
			.fireResistant()
			.component(DataComponents.UNBREAKABLE, new Unbreakable(false))
			.attributes(ScytheItem.createScytheAttributes(Tiers.NETHERITE)))
	);
	
	public static final DeferredHolder<Item, YamatoItem> YAMATO_DMC = NightfallRegistries.ITEMS.register("yamato_dmc", () ->
		new YamatoItem(Tiers.NETHERITE, new Item.Properties()
			.rarity(Rarity.EPIC)
			.fireResistant()
			.component(DataComponents.UNBREAKABLE, new Unbreakable(false))
			.attributes(YamatoItem.createYamatoAttributes(Tiers.NETHERITE)))
	);
	
	public static final DeferredHolder<Item, YamatoItem> YAMATO_DMC4 = NightfallRegistries.ITEMS.register("yamato_dmc4", () ->
		new YamatoItem(Tiers.NETHERITE, new Item.Properties()
			.rarity(Rarity.EPIC)
			.fireResistant()
			.component(DataComponents.UNBREAKABLE, new Unbreakable(false))
			.attributes(YamatoItem.createYamatoAttributes(Tiers.NETHERITE)))
	);
}
