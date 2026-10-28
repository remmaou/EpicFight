package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// ProjectileImpactEvent (cancellable)
@Mixin(Projectile.class)
public abstract class MixinProjectileImpact {
    @Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true)
    private void epicfight$projectileImpact(HitResult hitResult, CallbackInfoReturnable<ProjectileDeflection> cir) {
        if (VanillaEntityEventHooks.onProjectileImpacts(hitResult, (Projectile)(Object)this)) {
            cir.setReturnValue(ProjectileDeflection.NONE);
        }
    }
}
