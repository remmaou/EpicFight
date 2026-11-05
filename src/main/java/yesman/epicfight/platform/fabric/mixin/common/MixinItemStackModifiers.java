package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaItemEventHooks;

import java.util.function.BiConsumer;

/// Pengganti ItemAttributeModifierEvent NeoForge (Fabric API 1.21.1 tidak punya callback-nya).
/// Atribut Epic Fight (armor negation, impact, dst.) ditambahkan ke slot MAINHAND.
@Mixin(ItemStack.class)
public abstract class MixinItemStackModifiers {
    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void epicfight$modifiersBySlot(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        VanillaItemEventHooks.onModifyItemAttribute((ItemStack) (Object) this, (attribute, modifier, slotGroup) -> {
            if (slotGroup.test(slot)) {
                consumer.accept(attribute, modifier);
            }
        });
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void epicfight$modifiersByGroup(EquipmentSlotGroup group, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        VanillaItemEventHooks.onModifyItemAttribute((ItemStack) (Object) this, (attribute, modifier, slotGroup) -> {
            if (slotGroup == group) {
                consumer.accept(attribute, modifier);
            }
        });
    }
}
