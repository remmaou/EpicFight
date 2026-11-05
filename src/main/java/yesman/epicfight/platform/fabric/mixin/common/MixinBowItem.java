package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;

/// ArrowLooseEvent
@Mixin(BowItem.class)
public abstract class MixinBowItem {
    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void epicfight$arrowLoose(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfo ci) {
        if (entity instanceof Player player) {
            VanillaPlayerEventHooks.onLooseArrow(player);
        }
    }
}
