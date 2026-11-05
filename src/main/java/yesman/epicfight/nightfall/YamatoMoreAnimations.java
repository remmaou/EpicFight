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
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.damagesource.StunType;

/// Animasi Yamato tambahan (Divorce, Crosscut, Orbit, Killer Bee, Stomp, Aerial Flush, Rapid Slash, Flare Cut, Upper Slash).
/// DIHASILKAN otomatis dari bytecode asli (waktu fase = frame / 30). Efek khusus (lava, tangkap entitas) belum dibawa.
public final class YamatoMoreAnimations {
	private YamatoMoreAnimations() {}
	
	public static AnimationAccessor<ComboAttackAnimation> DIVORCE_1;
	public static AnimationAccessor<ComboAttackAnimation> DIVORCE_2;
	public static AnimationAccessor<ComboAttackAnimation> DIVORCE_3;
	public static AnimationAccessor<ComboAttackAnimation> CROSSCUT_1;
	public static AnimationAccessor<ComboAttackAnimation> CROSSCUT_2;
	public static AnimationAccessor<ComboAttackAnimation> CROSSCUT_3;
	public static AnimationAccessor<ComboAttackAnimation> ORBIT_1;
	public static AnimationAccessor<ComboAttackAnimation> ORBIT_2;
	public static AnimationAccessor<ComboAttackAnimation> KILLERBEE;
	public static AnimationAccessor<ComboAttackAnimation> STOMP;
	public static AnimationAccessor<ComboAttackAnimation> AERIALFLUSH;
	public static AnimationAccessor<ComboAttackAnimation> RAPIDSLASH;
	public static AnimationAccessor<ComboAttackAnimation> FLARE_CUT;
	public static AnimationAccessor<ComboAttackAnimation> UPPERSLASH;
	
	private static final Collider AERIALRAVE_COLLIDER = new MultiOBBCollider(3, 0.8D, 0.8D, 1.5D, 0.0D, 0.0D, -0.95D);
	private static final Collider KILLERBEE_COLLIDER = new MultiOBBCollider(4, 1.0D, 1.0D, 1.0D, 0.0D, 0.9D, 0.0D);
	private static final Collider RAPIDSLASH_COLLIDER = new OBBCollider(1.5D, 1.5D, 1.5D, 0.0D, 1.0D, 0.0D);
	private static final Collider RAPIDSLASH_END_COLLIDER = new OBBCollider(2.0D, 2.0D, 2.0D, 0.0D, 1.0D, 0.0D);
	private static final Collider STOMP_COLLIDER = new OBBCollider(2.7D, 2.7D, 2.7D, 0.0D, 1.0D, 0.0D);
	private static final Collider JUDGEMENT_CUT_COLLIDER = new OBBCollider(1.5D, 1.5D, 3.0D, 0.0D, 0.8D, -3.0D);
	private static final Collider VOLCANO_COLLIDER = new OBBCollider(2.5D, 2.5D, 2.5D, 0.0D, 0.5D, 0.0D);
	
	public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
		event.newBuilder(NightfallRegistries.MODID, YamatoMoreAnimations::build);
	}
	
	public static void build(AnimationBuilder builder) {
		Joint toolR = Armatures.BIPED.get().toolR;
		Joint toolL = Armatures.BIPED.get().toolL;
		Joint root = Armatures.BIPED.get().rootJoint;
		Collider blade = ColliderPreset.UCHIGATANA;
		
		DIVORCE_1 = builder.nextAccessor("biped/yamato/dmcyamato_divorce_1", (accessor) ->
			new ComboAttackAnimation(0.05F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.8000F, 1.1000F, 1.2667F, 1.2667F, InteractionHand.MAIN_HAND, toolR, blade))
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_4.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
				.addProperty(AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(0.8000F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.7F),
					InTimeEvent.create(2.583F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		DIVORCE_2 = builder.nextAccessor("biped/yamato/dmcyamato_divorce_2", (accessor) ->
			new ComboAttackAnimation(0.05F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 1.2667F, 1.6667F, 1.8667F, 1.8667F, InteractionHand.MAIN_HAND, toolR, blade))
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_2.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(1.2667F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.7F),
					InTimeEvent.create(2.3F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		DIVORCE_3 = builder.nextAccessor("biped/yamato/dmcyamato_divorce_3", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 1.1667F, 1.4333F, 2.5333F, 2.5333F, InteractionHand.MAIN_HAND, toolR, blade))
				.addProperty(AttackPhaseProperty.SWING_SOUND, NightfallSounds.WHOOSH_HEAVY_3.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.PARTICLE, NightfallParticles.YAMATO_CUT)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(1.1667F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.7F),
					InTimeEvent.create(2.716F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		CROSSCUT_1 = builder.nextAccessor("biped/yamato/dmcyamato_slasher_crosscut_1", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.2333F, 0.4667F, 0.5000F, 0.5000F, InteractionHand.MAIN_HAND, toolR, blade),
				new AttackAnimation.Phase(0.5000F, 0.5667F, 0.8000F, 1.0000F, 1.0000F, InteractionHand.MAIN_HAND, toolR, blade))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(2.733F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		CROSSCUT_2 = builder.nextAccessor("biped/yamato/dmcyamato_slasher_crosscut_2", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.8333F, 1.0667F, 1.2667F, 1.2667F, InteractionHand.MAIN_HAND, toolR, blade),
				new AttackAnimation.Phase(1.2667F, 1.2667F, 1.5000F, 1.6667F, 1.6667F, InteractionHand.MAIN_HAND, toolR, blade))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(2.35F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		CROSSCUT_3 = builder.nextAccessor("biped/yamato/dmcyamato_slasher_crosscut_3", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.4333F, 0.6667F, 1.0000F, 1.0000F, InteractionHand.MAIN_HAND, toolR, blade))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.PARTICLE, NightfallParticles.YAMATO_CUT)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(0.4333F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.7F),
					InTimeEvent.create(2.6F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		ORBIT_1 = builder.nextAccessor("biped/yamato/dmcyamato_orbit_1", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.2333F, 0.7000F, 0.7000F, 0.7000F, InteractionHand.MAIN_HAND, toolR, AERIALRAVE_COLLIDER),
				new AttackAnimation.Phase(0.7000F, 0.7333F, 0.9333F, 0.9667F, 0.9667F, InteractionHand.MAIN_HAND, toolR, AERIALRAVE_COLLIDER))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.01F, 1.35F))
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(0.83F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		ORBIT_2 = builder.nextAccessor("biped/yamato/dmcyamato_orbit_2", (accessor) ->
			new ComboAttackAnimation(0.01F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.3333F, 0.9000F, 1.0333F, 1.0333F, InteractionHand.MAIN_HAND, toolR, AERIALRAVE_COLLIDER))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.01F, 1.0F))
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(1.116F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		KILLERBEE = builder.nextAccessor("biped/yamato/dmcyamato_killerbee", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.5667F, 0.8667F, 1.0333F, 1.0333F, InteractionHand.MAIN_HAND, toolR, KILLERBEE_COLLIDER),
				new AttackAnimation.Phase(1.0333F, 1.0333F, 1.1000F, 1.1000F, 1.1000F, InteractionHand.MAIN_HAND, root, RAPIDSLASH_COLLIDER))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_BIG.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.01F, 0.45F))
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH)));

		STOMP = builder.nextAccessor("biped/yamato/dmcyamato_stomp", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.7000F, 1.1000F, 1.5333F, 1.5333F, InteractionHand.MAIN_HAND, root, STOMP_COLLIDER))
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.7F))
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(2.0F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		AERIALFLUSH = builder.nextAccessor("biped/yamato/dmcyamato_aerialflush", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.4333F, 0.6000F, 0.6667F, 0.6667F, InteractionHand.MAIN_HAND, toolR, AERIALRAVE_COLLIDER))
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackAnimationProperty.EXTRA_COLLIDERS, 5)
				.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.01F, 1.0F))
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(0.99F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		RAPIDSLASH = builder.nextAccessor("biped/yamato/dmcyamato_rapidslash", (accessor) ->
			new ComboAttackAnimation(0.05F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.3333F, 1.1667F, 1.4333F, 1.4333F, InteractionHand.MAIN_HAND, root, RAPIDSLASH_COLLIDER),
				new AttackAnimation.Phase(1.4333F, 1.4667F, 1.6667F, 2.1000F, 2.1000F, InteractionHand.MAIN_HAND, root, RAPIDSLASH_END_COLLIDER)
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F)))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_ROD.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION)
				.addProperty(AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(0.3333F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.7F),
					InTimeEvent.create(1.4667F, NightfallScytheAnimations.SHAKE, AnimationEvent.Side.CLIENT).params(6, 0.7F),
					InTimeEvent.create(2.5F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		FLARE_CUT = builder.nextAccessor("biped/yamato/dmcyamato_flare_cut", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.7333F, 0.9333F, 0.9333F, 0.9333F, InteractionHand.MAIN_HAND, toolR, ColliderPreset.GREATSWORD),
				new AttackAnimation.Phase(0.9333F, 0.9333F, 1.2333F, 1.2667F, 1.2667F, InteractionHand.MAIN_HAND, toolR, ColliderPreset.GREATSWORD),
				new AttackAnimation.Phase(1.2667F, 1.4333F, 1.7667F, 2.0000F, 2.0000F, InteractionHand.MAIN_HAND, toolR, ColliderPreset.GREATSWORD))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_ROD.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(3.46F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));

		UPPERSLASH = builder.nextAccessor("biped/yamato/dmcyamato_upperslash", (accessor) ->
			new ComboAttackAnimation(0.1F, accessor, Armatures.BIPED,
				new AttackAnimation.Phase(0.0000F, 0.4333F, 0.6333F, 1.0000F, 1.0000F, InteractionHand.MAIN_HAND, toolR, AERIALRAVE_COLLIDER))
				.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
				.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
				.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
				.addProperty(AttackPhaseProperty.PARTICLE, NightfallParticles.YAMATO_CUT)
				.addEvents(InTimeEvent.create(0.0F, YamatoAnimations.YAMATO_OUT, AnimationEvent.Side.BOTH),
					InTimeEvent.create(2.066F, YamatoAnimations.YAMATO_IN, AnimationEvent.Side.BOTH)));
	}
}
