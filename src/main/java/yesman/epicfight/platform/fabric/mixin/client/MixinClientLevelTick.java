package yesman.epicfight.platform.fabric.mixin.client;

import yesman.epicfight.platform.fabric.mixin.common.MixinServerLevelTick;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// Client counterpart of MixinServerLevelTick, plus the client side of EntityJoinLevelEvent.
@Mixin(ClientLevel.class)
public abstract class MixinClientLevelTick {
    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void epicfight$preTick(Entity entity, CallbackInfo ci) {
        VanillaEntityEventHooks.preTick(entity);
    }

    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
    private void epicfight$postTick(Entity entity, CallbackInfo ci) {
        VanillaEntityEventHooks.postTick(entity);
    }

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void epicfight$joinLevel(Entity entity, CallbackInfo ci) {
        VanillaEntityEventHooks.onJoinLevel(entity, (ClientLevel)(Object)this, false);
    }
}
