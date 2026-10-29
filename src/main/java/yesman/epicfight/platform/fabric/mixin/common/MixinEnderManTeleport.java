package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// EntityTeleportEvent.EnderEntity
@Mixin(EnderMan.class)
public abstract class MixinEnderManTeleport {
    @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void epicfight$teleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (VanillaEntityEventHooks.onEndermanTeleports((EnderMan)(Object)this)) {
            cir.setReturnValue(false);
        }
    }
}
