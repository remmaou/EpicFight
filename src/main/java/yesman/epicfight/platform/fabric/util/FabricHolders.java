package yesman.epicfight.platform.fabric.util;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import java.util.Optional;

/// Small replacements for helper methods that NeoForge added on vanilla classes.
public final class FabricHolders {
    private FabricHolders() {}

    // --- Level / RegistryAccess#holderOrThrow(Enchantment) ---
    public static Holder<Enchantment> enchantment(RegistryAccess access, ResourceKey<Enchantment> key) {
        return access.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }

    public static Holder<Enchantment> enchantment(Level level, ResourceKey<Enchantment> key) {
        return enchantment(level.registryAccess(), key);
    }

    public static Holder<Enchantment> enchantment(LevelReader level, ResourceKey<Enchantment> key) {
        return enchantment(level.registryAccess(), key);
    }

    // --- RegistryAccess#holder(ResourceKey) ---
    public static <T> Optional<Holder.Reference<T>> holder(RegistryAccess access, ResourceKey<T> key) {
        return access.registry(key.registryKey()).flatMap(registry -> registry.getHolder(key));
    }

    // --- ItemStack#getEnchantmentLevel(Holder<Enchantment>) (IItemStackExtension di NeoForge) ---
    public static int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    // --- ItemStack#getAttributeModifiers() ---
    public static ItemAttributeModifiers attributeModifiers(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    // --- AttributeSupplier.Builder(AttributeSupplier) ---
    public static AttributeSupplier.Builder copyBuilder(AttributeSupplier supplier) {
        AttributeSupplier.Builder builder = AttributeSupplier.builder();
        supplier.instances.forEach((attribute, instance) -> builder.add(attribute, instance.getBaseValue()));
        return builder;
    }

    // --- ArgumentTypeInfos.registerByClass ---
    public static <A extends ArgumentType<?>> void registerByClass(Class<A> clazz, ArgumentTypeInfo<?, ?> info) {
        ArgumentTypeInfos.BY_CLASS.put(clazz, info);
    }

    // --- GuiGraphics#drawString(Font, String, float, float, int, boolean) ---
    public static int drawString(GuiGraphics graphics, Font font, String text, float x, float y, int color, boolean dropShadow) {
        return graphics.drawString(font, text, Math.round(x), Math.round(y), color, dropShadow);
    }
}
