package yesman.epicfight.platform.fabric.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.LightningBolt;
import yesman.epicfight.platform.fabric.util.LightningBoltDamage;

@Mixin(LightningBolt.class)
public abstract class MixinLightningBolt implements LightningBoltDamage {
    @Unique
    private float epicfight$damage = 5.0F;

    @Override
    public void epicfight$setDamage(float damage) {
        this.epicfight$damage = damage;
    }

    @Override
    public float epicfight$getDamage() {
        return this.epicfight$damage;
    }
}
