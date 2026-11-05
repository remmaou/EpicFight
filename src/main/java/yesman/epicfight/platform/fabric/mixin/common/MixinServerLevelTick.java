package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// EntityTickEvent.Pre / Post. Hooked around the entity.tick() call in tickNonPassenger, exactly like NeoForge,
/// so that passengers ticked by their vehicle are not reported twice.
@Mixin(ServerLevel.class)
public abstract class MixinServerLevelTick {
    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void epicfight$preTick(Entity entity, CallbackInfo ci) {
        VanillaEntityEventHooks.preTick(entity);
    }

    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
    private void epicfight$postTick(Entity entity, CallbackInfo ci) {
        VanillaEntityEventHooks.postTick(entity);
    }
}
