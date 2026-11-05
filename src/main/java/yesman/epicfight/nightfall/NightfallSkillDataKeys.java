package yesman.epicfight.nightfall;

import net.minecraft.network.codec.ByteBufCodecs;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;
import yesman.epicfight.skill.SkillDataKey;

public final class NightfallSkillDataKeys {
	private NightfallSkillDataKeys() {}
	
	/// Memicu inisialisasi kelas (mendaftarkan holder).
	public static void init() {}
	
	/// Sabit sedang disarungkan (gaya SHEATH)
	public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> SCYTHE_SHEATH = NightfallRegistries.DATA_KEYS.register("scythe_sheath", () ->
		SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, false, ScytheSkill.class));
	
	/// Energi darah 0..1000
	public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Float>> SCYTHE_BLOOD_POWER = NightfallRegistries.DATA_KEYS.register("scythe_blood_power", () ->
		SkillDataKey.createSkillDataKey(ByteBufCodecs.FLOAT, 0.0F, false, ScytheSkill.class));
	
	/// Yamato: true = pedang terhunus (bilah di tangan kanan), false = di dalam sarung.
	public static final DeferredHolder<SkillDataKey<?>, SkillDataKey<Boolean>> YAMATO_DRAWN = NightfallRegistries.SKILL_DATA_KEYS.register("yamato_drawn", () ->
		SkillDataKey.createSkillDataKey(ByteBufCodecs.BOOL, false, true, YamatoSkill.class)
	);
}
