package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.boss.wither.WitherBoss;

/// WitherBoss#performRangedAttack is overloaded. Mixin's annotation processor keys @Invoker mappings by name
/// per mixin class, so the second overload lives in its own mixin interface (see MixinWitherBossAccessor).
@Mixin(value = WitherBoss.class)
public interface MixinWitherBossRangedAccessor {
	@Invoker(value = "performRangedAttack")
	public void invoke_performRangedAttack(int head, double x, double y, double z, boolean isDangerous);
}
