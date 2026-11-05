package yesman.epicfight.nightfall;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;

public final class NightfallParticles {
	private NightfallParticles() {}
	
	/// Memicu inisialisasi kelas (mendaftarkan holder).
	public static void init() {}
	
	/// Tebasan merah (flipbook 7 frame crimson_slash1..7). Parameter spawn: xSpeed = roll (derajat), ySpeed = ukuran.
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRIMSON_SLASH = NightfallRegistries.PARTICLES.register("crimson_slash", () -> new SimpleParticleType(true));
	
	/// Percikan darah Harvest/Scarlet End (asli Nightfall, flipbook blood_harvest1..6).
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_HARVEST = NightfallRegistries.PARTICLES.register("blood_harvest", () -> new SimpleParticleType(true));
	
	/// Tetes darah merah tua (pengganti DAMAGE_INDICATOR / dust vanilla). Kecepatan spawn = gerak partikel.
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_DROP = NightfallRegistries.PARTICLES.register("blood_drop", () -> new SimpleParticleType(true));
	
	/// Partikel kena-serang Crimson Moon (menggantikan hit_blade bawaan Epic Fight yang berkilat kuning/oranye).
	public static final DeferredHolder<ParticleType<?>, HitParticleType> CRIMSON_HIT = NightfallRegistries.PARTICLES.register("crimson_hit", () -> new HitParticleType(true, HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.ZERO));
	
	/// Varian ungu untuk Shadow.
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHADOW_SLASH = NightfallRegistries.PARTICLES.register("shadow_slash", () -> new SimpleParticleType(true));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHADOW_HARVEST = NightfallRegistries.PARTICLES.register("shadow_harvest", () -> new SimpleParticleType(true));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHADOW_DROP = NightfallRegistries.PARTICLES.register("shadow_drop", () -> new SimpleParticleType(true));
	
	/// Percikan tebasan Yamato (flipbook yamato_cut1..4).
	public static final DeferredHolder<ParticleType<?>, HitParticleType> YAMATO_CUT = NightfallRegistries.PARTICLES.register("yamato_cut", () -> new HitParticleType(true, HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.ZERO));
}
