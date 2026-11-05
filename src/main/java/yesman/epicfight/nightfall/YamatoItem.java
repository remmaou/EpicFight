package yesman.epicfight.nightfall;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.world.item.TieredWeaponItem;

/// Yamato (katana) dari Epic Fight Nightfall. Tidak bisa rusak.
/// Tahap 1: memakai moveset katana (uchigatana) bawaan Epic Fight. Animasi/mesh Yamato asli menyusul.
public class YamatoItem extends TieredWeaponItem {
	public static ItemAttributeModifiers createYamatoAttributes(Tier tier) {
		return TieredWeaponItem.createAttributes(tier, 3.0F, -1.8F, 0.0F);
	}
	
	public YamatoItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		tooltipComponents.add(Component.translatable("item.efn.yamato.description1"));
	}
	
	@Override
	public int getEnchantmentValue() {
		return 30;
	}
}
