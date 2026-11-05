package yesman.epicfight.nightfall;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;

/// Efek darah Crimson Moon. Efek ketahanan stun memakai Stun Immunity bawaan Epic Fight.
public final class NightfallMobEffects {
	private NightfallMobEffects() {}
	
	/// Memicu inisialisasi kelas (mendaftarkan holder).
	public static void init() {}
	
	/// Holder asli dari registry (bukan DeferredHolder). Wajib dipakai untuk MobEffectInstance, kalau tidak
	/// penyimpanan NBT entity crash: "Unregistered holder in ... mob_effect".
	@SuppressWarnings("unchecked")
	public static net.minecraft.core.Holder<MobEffect> real(net.minecraft.core.Holder<MobEffect> holder) {
		if (holder instanceof net.minecraft.core.Holder.Reference<?>) {
			return holder;
		}
		
		return holder.unwrapKey()
			.flatMap(key -> net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolder(key))
			.<net.minecraft.core.Holder<MobEffect>>map(ref -> (net.minecraft.core.Holder<MobEffect>)ref)
			.orElse(holder);
	}
	
	public static final DeferredHolder<MobEffect, MobEffect> CURSE_OF_BLOOD = NightfallRegistries.EFFECTS.register("curse_of_blood", CurseOfBloodEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> BLOOD_BLESSINGS = NightfallRegistries.EFFECTS.register("blood_blessing", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));
	public static final DeferredHolder<MobEffect, MobEffect> DAMAGE_REDUCTION = NightfallRegistries.EFFECTS.register("damage_reduction", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x7777FF));
	public static final DeferredHolder<MobEffect, MobEffect> ATTACK_DAMAGE_INCREASE = NightfallRegistries.EFFECTS.register("attack_damage_increase", () -> {
		SimpleEffect effect = new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xFFFF00);
		effect.addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "effect.attack_damage_increase"), 0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		return effect;
	});
	public static final DeferredHolder<MobEffect, MobEffect> ATTACK_SPEED_INCREASE = NightfallRegistries.EFFECTS.register("attack_speed_increase", () -> {
		SimpleEffect effect = new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xFF00FF);
		effect.addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "effect.attack_speed_increase"), 0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		return effect;
	});
	
	private static class SimpleEffect extends MobEffect {
		protected SimpleEffect(MobEffectCategory category, int color) {
			super(category, color);
		}
	}
	
	/// Kutukan Darah: luka 0.5 x (level+1) tiap detik.
	private static class CurseOfBloodEffect extends MobEffect {
		protected CurseOfBloodEffect() {
			super(MobEffectCategory.HARMFUL, 0x8B0000);
		}
		
		@Override
		public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
			livingEntity.hurt(livingEntity.damageSources().magic(), (amplifier + 1) * 0.5F);
			return true;
		}
		
		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
			return duration % 20 == 0;
		}
	}
}
