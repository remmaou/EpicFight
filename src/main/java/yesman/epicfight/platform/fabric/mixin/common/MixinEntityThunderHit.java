package yesman.epicfight.platform.fabric.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import yesman.epicfight.platform.fabric.util.LightningBoltDamage;

/// Vanilla: Entity#thunderHit selalu memanggil hurt(lightningBolt, 5.0F).
/// NeoForge: memakai lightning.getDamage(). Mixin ini meniru perilaku NeoForge.
@Mixin(Entity.class)
public abstract class MixinEntityThunderHit {
    @Unique
    private float epicfight$boltDamage = 5.0F;

    @Inject(method = "thunderHit", at = @At("HEAD"))
    private void epicfight$captureBoltDamage(ServerLevel level, LightningBolt lightning, CallbackInfo ci) {
        this.epicfight$boltDamage = ((LightningBoltDamage) lightning).epicfight$getDamage();
    }

    @ModifyConstant(method = "thunderHit", constant = @Constant(floatValue = 5.0F))
    private float epicfight$modifyBoltDamage(float original) {
        return this.epicfight$boltDamage;
    }
}
