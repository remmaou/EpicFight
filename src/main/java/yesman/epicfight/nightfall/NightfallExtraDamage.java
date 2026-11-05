package yesman.epicfight.nightfall;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;

public final class NightfallExtraDamage {
	private NightfallExtraDamage() {}
	
	/// Damage tambahan berdasarkan level & sisa durasi Curse of Blood (target) atau Blood Blessing (penyerang).
	/// params[0] = per level, params[1] = per tick sisa durasi
	public static final ExtraDamageInstance.ExtraDamage EX_DAMAGE_BY_COB_EFFECT = new ExtraDamageInstance.ExtraDamage((attacker, itemStack, target, baseDamage, params) -> {
		MobEffectInstance curse = target.getEffect(NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD));
		MobEffectInstance blessing = attacker.getEffect(NightfallMobEffects.real(NightfallMobEffects.BLOOD_BLESSINGS));
		MobEffectInstance used = curse != null ? curse : blessing;
		
		if (used == null) {
			return 0.0F;
		}
		
		int amplifier = used.getAmplifier() + 1;
		int duration = Mth.clamp(used.getDuration(), 0, 600);
		
		return baseDamage * (params[0] * amplifier + params[1] * duration);
	}, (levelReader, itemstack, tooltips, baseDamage, params) -> {});
}
