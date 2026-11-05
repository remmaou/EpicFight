package yesman.epicfight.nightfall;

import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.InTimeEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.ComboAttackAnimation;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.StunType;

/// Animasi Yamato (DMC) asli Epic Fight Nightfall. Waktu fase = frame asli / 30.
/// Catatan: animasi stun kustom, efek summoned sword, dan combo-tree Invincible belum dibawa.
public final class YamatoAnimations {
	private YamatoAnimations() {}
	
	public static AnimationAccessor<StaticAnimation> IDLE;
	public static AnimationAccessor<StaticAnimation> KNEEL;
	public static AnimationAccessor<StaticAnimation> SNEAK;
	public static AnimationAccessor<StaticAnimation> JUMP;
	public static AnimationAccessor<MovementAnimation> WALK;
	public static AnimationAccessor<MovementAnimation> RUN;
	public static AnimationAccessor<ComboAttackAnimation> SLASHER_1;
	public static AnimationAccessor<ComboAttackAnimation> SLASHER_2;
	public static AnimationAccessor<ComboAttackAnimation> SLASHER_3;
	public static AnimationAccessor<ComboAttackAnimation> DRIVE;
	public static AnimationAccessor<ComboAttackAnimation> AERIALRAVE_1;
	public static AnimationAccessor<ComboAttackAnimation> AERIALRAVE_2;
	public static AnimationAccessor<ComboAttackAnimation> AERIALRAVE_3;
	public static AnimationAccessor<ComboAttackAnimation> HELMBREAKER;
	public static AnimationAccessor<ComboAttackAnimation> JUDGEMENTCUT;
	public static AnimationAccessor<DodgeAnimation> DODGE_F;
	public static AnimationAccessor<DodgeAnimation> DODGE_B;
	public static AnimationAccessor<DodgeAnimation> DODGE_L;
	public static AnimationAccessor<DodgeAnimation> DODGE_R;
	
	private static final Collider AERIALRAVE_COLLIDER = new MultiOBBCollider(3, 0.8D, 0.8D, 1.5D, 0.0D, 0.0D, -0.95D);
	private static final Collider JUDGEMENT_CUT_COLLIDER = new OBBCollider(1.5D, 1.5D, 3.0D, 0.0D, 0.8D, -3.0D);
	
	private static void setDrawn(yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch<?> entitypatch, boolean drawn) {
		if (entitypatch instanceof ServerPlayerPatch playerpatch) {
			var container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
			
			if (container != null && container.getSkill() instanceof YamatoSkill && container.getDataManager().getDataValue(NightfallSkillDataKeys.YAMATO_DRAWN) != drawn) {
				container.getDataManager().setDataSync(NightfallSkillDataKeys.YAMATO_DRAWN, drawn);
				var player = playerpatch.getOriginal();
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (drawn ? NightfallSounds.YAMATO : NightfallSounds.YAMATO_IN).get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
			}
		}
	}
	
	/// Menghunus pedang (bilah muncul di tangan kanan).
	static final AnimationEvent.E0 YAMATO_OUT = (entitypatch, animation, params) -> setDrawn(entitypatch, true);
	/// Menyarungkan pedang.
	static final AnimationEvent.E0 YAMATO_IN = (entitypatch, animation, params) -> setDrawn(entitypatch, false);
	
	private static final AnimationEvent.E0 JUDGEMENT_SOUND = (entitypatch, animation, params) -> {
		var entity = entitypatch.getOriginal();
		entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), NightfallSounds.JUDGEMENTCUT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
	};
	
	public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
		event.newBuilder(NightfallRegistries.MODID, YamatoAnimations::build);
	}
	
	public static void build(AnimationBuilder builder) {
		Joint toolR = Armatures.BIPED.get().toolR;
		Joint toolL = Armatures.BIPED.get().toolL;
		Joint root = Armatures.BIPED.get().rootJoint;
		Collider blade = ColliderPreset.UCHIGATANA;
		
		IDLE = builder.nextAccessor("biped/yamato/dmcyamato_idle", (accessor) -> new StaticAnimation(0.15F, true, accessor, Armatures.BIPED));
		KNEEL = builder.nextAccessor("biped/yamato/dmcyamato_kneel", (accessor) -> new StaticAnimation(0.15F, true, accessor, Armatures.BIPED));
		SNEAK = builder.nextAccessor("biped/yamato/dmcyamato_sneak", (accessor) -> new StaticAnimation(0.15F, true, accessor, Armatures.BIPED));
		JUMP = builder.nextAccessor("biped/yamato/dmcyamato_jump", (accessor) -> new StaticAnimation(0.15F, false, accessor, Armatures.BIPED));
		WALK = builder.nextAccessor("biped/yamato/dmcyamato_walk", (accessor) -> new MovementAnimation(0.15F, true, accessor, Armatures.BIPED));
		RUN = builder.nextAccessor("biped/yamato/dmcyamato_run", (accessor) -> new MovementAnimation(0.15F, true, accessor, Armatures.BIPED));
		
		// Slasher 1: tendangan/hantaman sarung (frame 10/15/18), mengunci ke target
		SLASHER_1 = builder.nextAccessor("biped/yamato/dmcyamato_slasher_1", (accessor) ->
			new ComboAttackAnimation(0.05F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 10 / 30.0F, 15 / 30.0F, 18 / 30.0F, 18 / 30.0F, InteractionHand.MAIN_HAND, toolL, blade))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_ROD.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
				.addProperty(AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
				.addEvents(InTimeEvent.create(0.0F, YAMATO_OUT, AnimationEvent.Side.BOTH), InTimeEvent.create(8 / 30.0F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.5F)));
		
		// Slasher 2 (frame 3/14/23)
		SLASHER_2 = builder.nextAccessor("biped/yamato/dmcyamato_slasher_2", (accessor) ->
			new ComboAttackAnimation(0.05F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 3 / 30.0F, 14 / 30.0F, 23 / 30.0F, 23 / 30.0F, InteractionHand.MAIN_HAND, toolL, blade))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.AIR_BURST)
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_ROD.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
				.addEvents(InTimeEvent.create(0.0F, YAMATO_OUT, AnimationEvent.Side.BOTH), InTimeEvent.create(3 / 30.0F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.5F)));
		
		// Slasher 3: tebasan penutup (frame 9/17/35), pedang disarungkan di akhir
		SLASHER_3 = builder.nextAccessor("biped/yamato/dmcyamato_slasher_3", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 9 / 30.0F, 17 / 30.0F, 35 / 30.0F, 35 / 30.0F, InteractionHand.MAIN_HAND, toolR, blade))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.PARTICLE, NightfallParticles.YAMATO_CUT)
				.addEvents(InTimeEvent.create(0.0F, YAMATO_OUT, AnimationEvent.Side.BOTH), InTimeEvent.create(9 / 30.0F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(8, 0.9F), InTimeEvent.create(2.483F, YAMATO_IN, AnimationEvent.Side.BOTH)));
		
		// Drive: serangan dash menusuk ke bawah (frame 11/16/20)
		DRIVE = builder.nextAccessor("biped/yamato/dmcyamato_drive", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 11 / 30.0F, 16 / 30.0F, 20 / 30.0F, 20 / 30.0F, InteractionHand.MAIN_HAND, toolR, AERIALRAVE_COLLIDER))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.01F, 1.0F))
				.addEvents(InTimeEvent.create(0.0F, YAMATO_OUT, AnimationEvent.Side.BOTH), InTimeEvent.create(0.5333F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(8, 0.9F), InTimeEvent.create(0.7667F, YAMATO_IN, AnimationEvent.Side.BOTH)));
		
		// Aerial Rave 1-3 (serangan udara)
		AERIALRAVE_1 = aerial(builder, "dmcyamato_aerialrave_1", 7, 14, 15, 0.76F, toolR);
		AERIALRAVE_2 = aerial(builder, "dmcyamato_aerialrave_2", 8, 14, 15, 0.83F, toolR);
		AERIALRAVE_3 = aerial(builder, "dmcyamato_aerialrave_3", 12, 18, 40, 1.2F, toolR);
		
		// Helm Breaker (frame 10/24/35)
		HELMBREAKER = builder.nextAccessor("biped/yamato/dmcyamato_helmbreaker", (accessor) ->
			new ComboAttackAnimation(0.05F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 10 / 30.0F, 24 / 30.0F, 35 / 30.0F, 35 / 30.0F, InteractionHand.MAIN_HAND, toolR, AERIALRAVE_COLLIDER))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F))
				.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.0F))
				.addEvents(InTimeEvent.create(0.0F, YAMATO_OUT, AnimationEvent.Side.BOTH), InTimeEvent.create(0.8F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(10, 1.0F)));
		
		// Judgement Cut: tebasan area panjang di depan (frame 5/13/19), pedang disarungkan di 2.78 dtk
		JUDGEMENTCUT = builder.nextAccessor("biped/yamato/dmcyamato_judgementcut", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, 5 / 30.0F, 13 / 30.0F, 19 / 30.0F, 19 / 30.0F, InteractionHand.MAIN_HAND, root, JUDGEMENT_CUT_COLLIDER))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.6F))
				.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.0F))
				.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(10))
				.addEvents(InTimeEvent.create(0.0F, YAMATO_OUT, AnimationEvent.Side.BOTH), InTimeEvent.create(0.0F, JUDGEMENT_SOUND, AnimationEvent.Side.SERVER), InTimeEvent.create(5 / 30.0F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(8, 1.0F), InTimeEvent.create(2.783F, YAMATO_IN, AnimationEvent.Side.BOTH)));
		
		// Dodge (animasi asli, kebal selama ~0.5 dtk lalu boleh dibatalkan ke serangan)
		DODGE_F = dodge(builder, "dmcyamato_dodge_f");
		DODGE_B = dodge(builder, "dmcyamato_dodge_b");
		DODGE_L = dodge(builder, "dmcyamato_dodge_l");
		DODGE_R = dodge(builder, "dmcyamato_dodge_r");
	}
	
	private static AnimationAccessor<ComboAttackAnimation> aerial(AnimationBuilder builder, String name, int antic, int contact, int end, float sheatheTime, Joint tool) {
		return builder.nextAccessor("biped/yamato/" + name, (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0F, antic / 30.0F, contact / 30.0F, end / 30.0F, end / 30.0F, InteractionHand.MAIN_HAND, tool, AERIALRAVE_COLLIDER))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 1.0F))
				.addEvents(InTimeEvent.create(0.0F, YAMATO_OUT, AnimationEvent.Side.BOTH), InTimeEvent.create(sheatheTime, YAMATO_IN, AnimationEvent.Side.BOTH))
				.newTimePair(0.0F, 0.25F)
					.addStateRemoveOld(EntityState.LOCKON_ROTATE, false));
	}
	
	private static AnimationAccessor<DodgeAnimation> dodge(AnimationBuilder builder, String name) {
		return builder.nextAccessor("biped/dodge/" + name, (accessor) ->
			new DodgeAnimation(0.05F, 0.5F, accessor, 0.6F, 1.65F, Armatures.BIPED)
				.addEvents(InTimeEvent.create(0.0F, NightfallScytheAnimations.AFTERIMAGE, AnimationEvent.Side.CLIENT), InTimeEvent.create(0.15F, NightfallScytheAnimations.AFTERIMAGE, AnimationEvent.Side.CLIENT))
				.newTimePair(0.0F, 0.6F)
					.addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
					.addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false));
	}
}
