package yesman.epicfight.nightfall;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.registry.entries.EpicFightParticles;

/// Efek visual Crimson Moon yang aman dipanggil dari kode bersama (hanya memakai Level.addParticle, tidak melakukan apa pun di server).
public final class NightfallVfx {
	private NightfallVfx() {}
	
	/// Tebasan merah di depan pemain. rollDeg = kemiringan busur, scale = ukuran.
	public static void spawnSlash(LivingEntity entity, float rollDeg, float scale) {
		if (!entity.level().isClientSide()) {
			return;
		}
		
		Vec3 look = entity.getLookAngle();
		Vec3 pos = entity.position().add(0.0D, entity.getBbHeight() * 0.6D, 0.0D).add(look.x * 1.6D, 0.0D, look.z * 1.6D);
		
		entity.level().addParticle((entity.getMainHandItem().is(NightfallItems.SHADOW.get()) ? NightfallParticles.SHADOW_SLASH : NightfallParticles.CRIMSON_SLASH).get(), pos.x, pos.y, pos.z, rollDeg, isEnhanced(entity) ? scale * 1.3F : scale, 0.0D);
	}
	
	/// Apakah senjata di tangan utama memiliki enchant Scythe Enhance (efek tebasan lebih besar)
	public static boolean isEnhanced(LivingEntity entity) {
		return entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
			.getHolder(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "scythe_enhance")))
			.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, entity.getMainHandItem()) > 0)
			.orElse(false);
	}
	
	/// Bayangan putih Epic Fight (afterimage) dari pemain
	public static void spawnAfterimage(LivingEntity entity) {
		// Dibatasi menurut FPS (jumlah hidup, jarak antar-spawn, umur) supaya tidak lag; lihat NightfallAfterimageLimiter
		if (entity.level().isClientSide() && yesman.epicfight.nightfall.client.NightfallAfterimageLimiter.allow(entity)) {
			entity.level().addParticle(EpicFightParticles.WHITE_AFTERIMAGE.get(), entity.getX(), entity.getY(), entity.getZ(), Double.longBitsToDouble(entity.getId()), 0, 0);
		}
	}
	
	/// Getar kamera hanya untuk pemain lokal; dipanggil lewat kelas klien.
	public static void shakeCamera(LivingEntity entity, int ticks, float amplitude) {
		if (entity.level().isClientSide()) {
			yesman.epicfight.nightfall.client.NightfallCameraShake.start(entity, ticks, amplitude);
		}
	}
}
