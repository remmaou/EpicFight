package yesman.epicfight.nightfall;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.InPeriodEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.InTimeEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.SimpleEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.types.AirSlashAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.ComboAttackAnimation;
import yesman.epicfight.api.animation.types.DashAttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;

/// Animasi sabit (Scythe) Epic Fight Nightfall: idle/jalan/lari, combo 1-5, dash, air slash, block, dan skill Harvest.
/// Berkas animasi (.json) ada di assets/efn/animmodels/animations/biped/scythe/...
/// Event VFX (slash merah, Effekseer, camera shake, afterimage) belum dibawa; diganti suara + partikel debu merah.
public final class NightfallScytheAnimations {
	private NightfallScytheAnimations() {}
	
	public static AnimationAccessor<StaticAnimation> SCYTHE_IDLE_COMBAT;
	public static AnimationAccessor<MovementAnimation> SCYTHE_WALK_COMBAT;
	public static AnimationAccessor<MovementAnimation> SCYTHE_RUN_COMBAT;
	public static AnimationAccessor<StaticAnimation> SCYTHE_IDLE_NORMAL;
	public static AnimationAccessor<MovementAnimation> SCYTHE_WALK_NORMAL;
	public static AnimationAccessor<MovementAnimation> SCYTHE_RUN_NORMAL;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_AUTO1;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_AUTO2;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_AUTO3;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_AUTO4;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_AUTO5;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_RPG_AUTO1;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_RPG_AUTO2;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_RPG_WHIRL;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_RPG_LEAP;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_RPG_SLAM;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_SHADOW_AUTO1;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_SHADOW_AUTO2;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_SHADOW_WHIRL;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_SHADOW_LEAP;
	public static AnimationAccessor<ComboAttackAnimation> SCYTHE_SHADOW_SLAM;
	public static AnimationAccessor<yesman.epicfight.api.animation.types.DodgeAnimation> SHADOW_STEP_FORWARD;
	public static AnimationAccessor<yesman.epicfight.api.animation.types.DodgeAnimation> SHADOW_STEP_BACKWARD;
	public static AnimationAccessor<yesman.epicfight.api.animation.types.DodgeAnimation> SHADOW_STEP_LEFT;
	public static AnimationAccessor<yesman.epicfight.api.animation.types.DodgeAnimation> SHADOW_STEP_RIGHT;
	public static AnimationAccessor<AirSlashAnimation> SCYTHE_AIR_SLASH;
	public static AnimationAccessor<DashAttackAnimation> SCYTHE_DASH;
	public static AnimationAccessor<AttackAnimation> SCYTHE_HARVEST;
	public static AnimationAccessor<StaticAnimation> SCYTHE_BLOCK;
	public static AnimationAccessor<AttackAnimation> SCYTHE_SCARLET_END;
	public static AnimationAccessor<StaticAnimation> SCYTHE_SHEATHED;
	public static AnimationAccessor<StaticAnimation> SCYTHE_UNSHEATHED;
	
	// Event VFX (sisi klien): tebasan merah, getar kamera, afterimage. params: (roll derajat, ukuran) / (durasi tick, kekuatan)
	private static final AnimationEvent.E2<Float, Float> SPAWN_SLASH = (entitypatch, animation, params) -> NightfallVfx.spawnSlash(entitypatch.getOriginal(), params.first(), params.second());
	static final AnimationEvent.E2<Integer, Float> SHAKE = (entitypatch, animation, params) -> NightfallVfx.shakeCamera(entitypatch.getOriginal(), params.first(), params.second());
	static final AnimationEvent.E0 AFTERIMAGE = (entitypatch, animation, params) -> NightfallVfx.spawnAfterimage(entitypatch.getOriginal());
	
	private static final MultiOBBCollider SCYTHE_SKILL2_COLLIDER = new MultiOBBCollider(3, 0.5D, 0.5D, 5.0D, 0.0D, 0.0D, -4.0D);
	
	private static final MultiOBBCollider SCYTHE_COLLIDER = new MultiOBBCollider(
		new OBBCollider(0.25, 0.25, 1.3, 0.0, 0.0, 1.0),
		new OBBCollider(0.25, 0.25, 1.3, 0.0, 0.0, 1.0),
		new OBBCollider(0.7, 0.9, 0.7, 0.0, -0.5, -0.45),
		new OBBCollider(0.7, 0.9, 0.7, 0.0, -0.5, -0.45)
	);
	
	public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
		event.newBuilder(NightfallRegistries.MODID, NightfallScytheAnimations::build);
	}
	
	public static void build(AnimationBuilder builder) {
		Joint mainHand = Armatures.BIPED.get().toolR;
		Collider scytheCollider = SCYTHE_COLLIDER;
		
		// Kecepatan Harvest naik bersama efek Haste (efek Attack Speed Increase bawaan Nightfall belum dibawa).
		AnimationProperty.PlaybackSpeedModifier harvestSpeed = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
			LivingEntity entity = entitypatch.getOriginal();
			MobEffectInstance haste = entity.getEffect(MobEffects.DIG_SPEED);
			float bonus = haste == null ? 0.0F : (haste.getAmplifier() + 1) * 0.1F;
			
			return Math.min(1.0F + bonus, 1.5F);
		};
		
		SCYTHE_IDLE_COMBAT = builder.nextAccessor("biped/scythe/living/scythe_idle_combat", (accessor) -> new StaticAnimation(0.2F, true, accessor, Armatures.BIPED));
		SCYTHE_WALK_COMBAT = builder.nextAccessor("biped/scythe/living/scythe_walk_combat", (accessor) -> new MovementAnimation(0.2F, true, accessor, Armatures.BIPED));
		SCYTHE_RUN_COMBAT = builder.nextAccessor("biped/scythe/living/scythe_run_combat", (accessor) -> new MovementAnimation(0.2F, true, accessor, Armatures.BIPED));
		SCYTHE_IDLE_NORMAL = builder.nextAccessor("biped/scythe/living/scythe_idle_normal", (accessor) -> new StaticAnimation(0.2F, true, accessor, Armatures.BIPED));
		SCYTHE_WALK_NORMAL = builder.nextAccessor("biped/scythe/living/scythe_walk_normal", (accessor) -> new MovementAnimation(0.2F, true, accessor, Armatures.BIPED));
		SCYTHE_RUN_NORMAL = builder.nextAccessor("biped/scythe/living/scythe_run_normal", (accessor) -> new MovementAnimation(0.2F, true, accessor, Armatures.BIPED));
		
		SCYTHE_AUTO1 = builder.nextAccessor("biped/scythe/combat/scythe_auto1", (accessor) ->
			new ComboAttackAnimation(0.25F, 0.11666667F, 0.575F, 0.71F, 1.0F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_1.get())
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.735F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_AUTO2 = builder.nextAccessor("biped/scythe/combat/scythe_auto2", (accessor) ->
			new ComboAttackAnimation(0.16F, 0.11666667F, 0.58F, 0.7F, 1.3F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get())
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.725F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_AUTO3 = builder.nextAccessor("biped/scythe/combat/scythe_auto3", (accessor) ->
			new ComboAttackAnimation(0.16F, 0.11666667F, 0.58F, 0.75F, 1.33F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_4.get())
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.75F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_AUTO4 = builder.nextAccessor("biped/scythe/combat/scythe_auto4", (accessor) ->
			new ComboAttackAnimation(0.14F, 0.33333334F, 0.75F, 0.86F, 1.42F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get())
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.885F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_AUTO5 = builder.nextAccessor("biped/scythe/combat/scythe_auto5", (accessor) ->
			new ComboAttackAnimation(0.15F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 0.11666667F, 0.52F, 0.71F, 1.0F, 1.0F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get()),
				new AttackAnimation.Phase(1.0F, 1.14F, 1.31F, 2.3F, 2.3F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_3.get()))
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addEvents(
					InTimeEvent.create(0.71F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(-30.0F, 4.5F),
					InTimeEvent.create(1.31F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(35.0F, 5.5F),
					InTimeEvent.create(1.31F, SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.5F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 1.8F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		// ---- Combo RPG baru: lunge, spin 360, lompat, slam (gerakan diturunkan dari animasi asli + lapisan prosedural) ----
		SCYTHE_RPG_AUTO1 = builder.nextAccessor("biped/scythe/combat/scythe_rpg_auto1", (accessor) ->
			new ComboAttackAnimation(0.22F, 0.0972F, 0.479F, 0.592F, 0.833F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_1.get())
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.1F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.6125F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_RPG_AUTO2 = builder.nextAccessor("biped/scythe/combat/scythe_rpg_auto2", (accessor) ->
			new ComboAttackAnimation(0.16F, 0.106F, 0.527F, 0.636F, 1.182F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get())
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.15F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.659F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_RPG_WHIRL = builder.nextAccessor("biped/scythe/combat/scythe_rpg_whirl", (accessor) ->
			new ComboAttackAnimation(0.15F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 0.117F, 0.35F, 0.5F, 0.5F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get()),
				new AttackAnimation.Phase(0.5F, 0.58F, 0.78F, 0.95F, 1.33F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_4.get())
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.2F)))
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 3)
				.addEvents(InTimeEvent.create(0.78F, SHAKE, AnimationEvent.Side.CLIENT).params(8, 0.7F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 1.0F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_RPG_LEAP = builder.nextAccessor("biped/scythe/combat/scythe_rpg_leap", (accessor) ->
			new ComboAttackAnimation(0.12F, 0.33F, 0.72F, 0.86F, 1.42F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_3.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.25F))
				.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.5F))
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.8F))
				.addEvents(InTimeEvent.create(0.72F, SHAKE, AnimationEvent.Side.CLIENT).params(8, 0.8F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.885F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_RPG_SLAM = builder.nextAccessor("biped/scythe/combat/scythe_rpg_slam", (accessor) ->
			new ComboAttackAnimation(0.15F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 0.11666667F, 0.52F, 0.71F, 1.0F, 1.0F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get()),
				new AttackAnimation.Phase(1.0F, 1.14F, 1.31F, 2.3F, 2.3F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_3.get())
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.35F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.0F)))
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addEvents(
					InTimeEvent.create(0.6F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(0.9F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(1.31F, SHAKE, AnimationEvent.Side.CLIENT).params(12, 1.0F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 1.8F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		// ---- Shadow: combo gesit (blink-slash) + Shadow Step (dodge ala Genshin, kebal selama animasi) ----
		SCYTHE_SHADOW_AUTO1 = builder.nextAccessor("biped/scythe/combat/scythe_shadow_auto1", (accessor) ->
			new ComboAttackAnimation(0.12F, 0.0729F, 0.3594F, 0.4438F, 0.625F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_1.get())
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.95F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.addEvents(InTimeEvent.create(0.05F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.3F, AFTERIMAGE, AnimationEvent.Side.CLIENT))
				.newTimePair(0.0F, 0.4594F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_SHADOW_AUTO2 = builder.nextAccessor("biped/scythe/combat/scythe_shadow_auto2", (accessor) ->
			new ComboAttackAnimation(0.12F, 0.0752F, 0.3742F, 0.4516F, 0.8387F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get())
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.addEvents(InTimeEvent.create(0.05F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.32F, AFTERIMAGE, AnimationEvent.Side.CLIENT))
				.newTimePair(0.0F, 0.4677F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_SHADOW_WHIRL = builder.nextAccessor("biped/scythe/combat/scythe_shadow_whirl", (accessor) ->
			new ComboAttackAnimation(0.12F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 0.0807F, 0.2414F, 0.3448F, 0.3448F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get()),
				new AttackAnimation.Phase(0.3448F, 0.4F, 0.5379F, 0.6552F, 0.9172F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_4.get())
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.15F)))
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 3)
				.addEvents(InTimeEvent.create(0.1F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.3F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.538F, SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.6F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.6897F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_SHADOW_LEAP = builder.nextAccessor("biped/scythe/combat/scythe_shadow_leap", (accessor) ->
			new ComboAttackAnimation(0.1F, 0.2357F, 0.5143F, 0.6143F, 1.0143F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_3.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.2F))
				.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.5F))
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.571F))
				.addEvents(InTimeEvent.create(0.1F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.3F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.514F, SHAKE, AnimationEvent.Side.CLIENT).params(7, 0.8F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.632F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_SHADOW_SLAM = builder.nextAccessor("biped/scythe/combat/scythe_shadow_slam", (accessor) ->
			new ComboAttackAnimation(0.12F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 0.0897F, 0.4F, 0.5462F, 0.7692F, 0.7692F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get()),
				new AttackAnimation.Phase(0.7692F, 0.8769F, 1.0077F, 1.7692F, 1.7692F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_3.get())
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.35F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.0F)))
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addEvents(
					InTimeEvent.create(0.2F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(0.46F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(0.69F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(1.008F, SHAKE, AnimationEvent.Side.CLIENT).params(10, 1.0F))
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 1.385F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SHADOW_STEP_FORWARD = builder.nextAccessor("biped/scythe/dodge/shadow_step_forward", (accessor) ->
			new yesman.epicfight.api.animation.types.DodgeAnimation(0.08F, 0.44F, accessor, 0.6F, 1.65F, Armatures.BIPED)
				.addState(EntityState.LOOK_TARGET, true)
				.newTimePair(0.0F, 0.3F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
				.addEvents(InTimeEvent.create(0.0F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.12F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.24F, AFTERIMAGE, AnimationEvent.Side.CLIENT)));
		
		SHADOW_STEP_BACKWARD = builder.nextAccessor("biped/scythe/dodge/shadow_step_backward", (accessor) ->
			new yesman.epicfight.api.animation.types.DodgeAnimation(0.08F, 0.44F, accessor, 0.6F, 1.65F, Armatures.BIPED)
				.addState(EntityState.LOOK_TARGET, true)
				.newTimePair(0.0F, 0.3F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
				.addEvents(InTimeEvent.create(0.0F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.12F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.24F, AFTERIMAGE, AnimationEvent.Side.CLIENT)));
		
		SHADOW_STEP_LEFT = builder.nextAccessor("biped/scythe/dodge/shadow_step_left", (accessor) ->
			new yesman.epicfight.api.animation.types.DodgeAnimation(0.08F, 0.44F, accessor, 0.6F, 1.65F, Armatures.BIPED)
				.addState(EntityState.LOOK_TARGET, true)
				.newTimePair(0.0F, 0.3F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
				.addEvents(InTimeEvent.create(0.0F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.12F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.24F, AFTERIMAGE, AnimationEvent.Side.CLIENT)));
		
		SHADOW_STEP_RIGHT = builder.nextAccessor("biped/scythe/dodge/shadow_step_right", (accessor) ->
			new yesman.epicfight.api.animation.types.DodgeAnimation(0.08F, 0.44F, accessor, 0.6F, 1.65F, Armatures.BIPED)
				.addState(EntityState.LOOK_TARGET, true)
				.newTimePair(0.0F, 0.3F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
				.addEvents(InTimeEvent.create(0.0F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.12F, AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.24F, AFTERIMAGE, AnimationEvent.Side.CLIENT)));
		
		SCYTHE_AIR_SLASH = builder.nextAccessor("biped/scythe/combat/scythe_airslash", (accessor) ->
			new AirSlashAnimation(0.14F, 0.56F, 0.65F, 1.28F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_3.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.65F))
				.newTimePair(0.0F, 0.75F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		SCYTHE_DASH = builder.nextAccessor("biped/scythe/combat/scythe_dash", (accessor) ->
			new DashAttackAnimation(0.14F, 0.61F, 0.61F, 0.71F, 1.35F, scytheCollider, mainHand, accessor, Armatures.BIPED)
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get())
				.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
				.newTimePair(0.0F, 0.81F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false));
		
		// Skill Harvest: 3 tebasan memutar
		SCYTHE_HARVEST = builder.nextAccessor("biped/scythe/skill/scythe_harvest", (accessor) ->
			new AttackAnimation(0.14F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 0.66F, 0.81F, 0.81F, 0.81F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_4.get()),
				new AttackAnimation.Phase(0.81F, 0.81F, 1.0F, 1.0F, 1.0F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_4.get()),
				new AttackAnimation.Phase(1.0F, 1.05F, 1.21F, 2.13F, 2.13F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get()))
				.addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, harvestSpeed)
				.addEvents(
					InTimeEvent.create(0.81F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(20.0F, 4.5F),
					InTimeEvent.create(1.0F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(-20.0F, 4.5F),
					InTimeEvent.create(1.21F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(70.0F, 6.0F),
					InTimeEvent.create(1.21F, SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.5F))
				.newTimePair(0.0F, 1.63F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
				.newTimePair(0.0F, 1.83F)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false));
		

		SCYTHE_BLOCK = builder.nextAccessor("biped/scythe/skill/scythe_block", (accessor) -> new StaticAnimation(0.2F, true, accessor, Armatures.BIPED));

		SCYTHE_SHEATHED = builder.nextAccessor("biped/scythe/skill/scythe_sheathed", (accessor) ->
			new StaticAnimation(0.2F, false, accessor, Armatures.BIPED)
				.newTimePair(0.0F, 10.0F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
				.addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create((entitypatch, animation, params) -> setSheath(entitypatch, true), AnimationEvent.Side.SERVER)));
		
		SCYTHE_UNSHEATHED = builder.nextAccessor("biped/scythe/skill/scythe_unsheathed", (accessor) ->
			new StaticAnimation(0.2F, false, accessor, Armatures.BIPED)
				.newTimePair(0.0F, 10.0F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
				.addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create((entitypatch, animation, params) -> setSheath(entitypatch, false), AnimationEvent.Side.SERVER)));
		
		// Skill penutup Scarlet End (butuh Blood Power penuh + guard)
		AnimationProperty.PlaybackSpeedModifier finisherSpeed = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.0F;
		
		SCYTHE_SCARLET_END = builder.nextAccessor("biped/scythe/skill/scythe_scarlet_end", (accessor) ->
			new AttackAnimation(0.14F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 0.96F, 1.08F, 1.08F, 1.08F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(3.5F))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100.0F))
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(100.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG),
				new AttackAnimation.Phase(1.08F, 1.21F, 1.35F, 2.35F, 2.35F, mainHand, scytheCollider)
					.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(3.5F))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100.0F))
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(100.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG),
				new AttackAnimation.Phase(2.35F, 3.0F, 3.5F, 10.0F, 10.0F, Armatures.BIPED.get().rootJoint, SCYTHE_SKILL2_COLLIDER)
					.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.EVISCERATE.get())
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(3.0F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(5.0F))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100.0F))
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(100.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN))
				.addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, finisherSpeed)
				.newTimePair(0.96F, 10.0F)
					.addStateRemoveOld(EntityState.TURNING_LOCKED, true)
				.newTimePair(0.0F, 10.0F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
					.addStateRemoveOld(EntityState.ATTACK_RESULT, (damageSource) -> AttackResult.ResultType.BLOCKED)
				.addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create((entitypatch, animation, params) -> entitypatch.playSound(SoundEvents.WITHER_SPAWN, 0.0F, 0.0F), AnimationEvent.Side.CLIENT))
				.addEvents(
					InTimeEvent.create(0.26F, (entitypatch, animation, params) -> entitypatch.playSound(SoundEvents.ENDERMAN_STARE, 0.0F, 0.0F), AnimationEvent.Side.CLIENT),
					InTimeEvent.create(0.7F, (entitypatch, animation, params) -> entitypatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1, false, false, true)), AnimationEvent.Side.SERVER),
					InTimeEvent.create(0.7F, (entitypatch, animation, params) -> entitypatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 1, false, false, true)), AnimationEvent.Side.SERVER),
					InTimeEvent.create(1.0F, (entitypatch, animation, params) -> entitypatch.playSound(SoundEvents.WARDEN_SONIC_BOOM, 0.0F, 0.0F), AnimationEvent.Side.CLIENT),
					InTimeEvent.create(3.0F, (entitypatch, animation, params) -> entitypatch.playSound(SoundEvents.WITHER_BREAK_BLOCK, 0.0F, 0.0F), AnimationEvent.Side.SERVER),
					InTimeEvent.create(0.9F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(1.1F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(1.3F, AFTERIMAGE, AnimationEvent.Side.CLIENT),
					InTimeEvent.create(1.05F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(-35.0F, 5.0F),
					InTimeEvent.create(1.25F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(35.0F, 5.0F),
					InTimeEvent.create(1.05F, SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.5F),
					InTimeEvent.create(1.25F, SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.5F),
					InTimeEvent.create(3.0F, SPAWN_SLASH, AnimationEvent.Side.CLIENT).params(90.0F, 8.0F),
					InTimeEvent.create(3.0F, SHAKE, AnimationEvent.Side.CLIENT).params(16, 1.4F),
					InTimeEvent.create(3.05F, yesman.epicfight.gameasset.Animations.ReusableSources.FRACTURE_GROUND_SIMPLE, AnimationEvent.Side.CLIENT).params(new yesman.epicfight.api.utils.math.Vec3f(0.0F, -0.24F, -2.0F), Armatures.BIPED.get().toolR, 3.5D, 3.05F),
					InPeriodEvent.create(0.26F, 1.0F, (entitypatch, animation, params) -> {
						LivingEntity entity = entitypatch.getOriginal();
						
						if (entity.level() instanceof ServerLevel serverLevel && entity.tickCount % 3 == 0) {
							Vec3 pos = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D);
							serverLevel.sendParticles((entity.getMainHandItem().is(NightfallItems.SHADOW.get()) ? NightfallParticles.SHADOW_DROP : NightfallParticles.BLOOD_DROP).get(), pos.x, pos.y, pos.z, 3, 0.5D, 1.0D, 0.5D, 0.8D);
							serverLevel.sendParticles((entity.getMainHandItem().is(NightfallItems.SHADOW.get()) ? NightfallParticles.SHADOW_DROP : NightfallParticles.BLOOD_DROP).get(), pos.x, pos.y, pos.z, 3, 0.5D, 1.0D, 0.5D, 1.2D);
						}
					}, AnimationEvent.Side.SERVER)));
			}

	private static void setSheath(yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch<?> entitypatch, boolean sheath) {
		if (entitypatch instanceof ServerPlayerPatch playerpatch) {
			SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
			
			if (container != null && container.getSkill() instanceof ScytheSkill scytheSkill && !scytheSkill.isDisabled(container)) {
				container.getDataManager().setDataSync(NightfallSkillDataKeys.SCYTHE_SHEATH, sheath);
				playerpatch.modifyLivingMotionByCurrentItem();
			}
		}
	}
}
