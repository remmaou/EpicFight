package yesman.epicfight.nightfall;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.world.item.TieredWeaponItem;

/// Sabit (Scythe) dari Epic Fight Nightfall. Tidak bisa rusak (di aslinya isDamageable = false).
public class ScytheItem extends TieredWeaponItem {
	public static ItemAttributeModifiers createScytheAttributes(Tier tier) {
		return TieredWeaponItem.createAttributes(tier, 3.0F, -2.6F, 0.0F);
	}
	
	public ScytheItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		tooltipComponents.add(Component.translatable("item.efn.scythe.description1"));
	}
	
	@Override
	public int getEnchantmentValue() {
		return 30;
	}
}
