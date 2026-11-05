package yesman.epicfight.nightfall;

import java.util.List;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import yesman.epicfight.platform.fabric.shim.registries.DeferredRegister;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillDataKey;

/// Registry milik addon Nightfall (namespace "efn"). Didaftarkan bersama registry Epic Fight lain di EpicFightFabric.
public final class NightfallRegistries {
	private NightfallRegistries() {}
	
	public static final String MODID = "efn";
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
	public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(EpicFightRegistries.SKILL_DATA_KEY, MODID);
	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
	public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(EpicFightRegistries.Keys.SKILL, MODID);
	
	public static final List<DeferredRegister<?>> ALL = List.of(DATA_KEYS, EFFECTS, ITEMS, SOUNDS, PARTICLES, SKILLS);
}
