package yesman.epicfight.nightfall;

import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;

public final class NightfallSkills {
	private NightfallSkills() {}
	
	/// Memicu inisialisasi kelas (mendaftarkan holder).
	public static void init() {}
	
	/// Skill bawaan sabit Crimson Moon (Blood Power, Harvest, Scarlet End, mode sarung).
	public static final DeferredHolder<Skill, ScytheSkill> SCYTHE_SKILL = NightfallRegistries.SKILLS.register("scythe_skill", key ->
		WeaponInnateSkill.createWeaponInnateBuilder(ScytheSkill::new)
			.setResource(Skill.Resource.NONE)
			.build(key)
	);
	
	/// Dash menghindar Shadow (otomatis terpasang di slot dodge saat memegang Shadow).
	public static final DeferredHolder<Skill, ShadowStepSkill> SHADOW_STEP = NightfallRegistries.SKILLS.register("shadow_step", key ->
		yesman.epicfight.skill.dodge.DodgeSkill.createDodgeBuilder(ShadowStepSkill::new)
			.setAnimations(NightfallScytheAnimations.SHADOW_STEP_FORWARD, NightfallScytheAnimations.SHADOW_STEP_BACKWARD, NightfallScytheAnimations.SHADOW_STEP_LEFT, NightfallScytheAnimations.SHADOW_STEP_RIGHT)
			.build(key)
	);
	
	/// Skill bawaan Yamato.
	public static final DeferredHolder<Skill, YamatoSkill> YAMATO = NightfallRegistries.SKILLS.register("yamato", key ->
		WeaponInnateSkill.createWeaponInnateBuilder(YamatoSkill::new)
			.setResource(Skill.Resource.NONE)
			.build(key)
	);
	
	/// Dodge Yamato (animasi asli).
	public static final DeferredHolder<Skill, YamatoStepSkill> YAMATO_STEP = NightfallRegistries.SKILLS.register("yamato_step", key ->
		yesman.epicfight.skill.dodge.DodgeSkill.createDodgeBuilder(YamatoStepSkill::new)
			.setAnimations(YamatoAnimations.DODGE_F, YamatoAnimations.DODGE_B, YamatoAnimations.DODGE_L, YamatoAnimations.DODGE_R)
			.build(key)
	);
}
