package yesman.epicfight.nightfall.client;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.client.particle.EntityAfterimageParticle;

/// Pembatas bayangan putih (afterimage) milik senjata efn supaya tidak bikin FPS jatuh / stutter.
/// Tiap bayangan merender seluruh tubuh pemain beberapa pass selama ~20 tick, dan satu serangan memicu 2-4 bayangan,
/// jadi dibatasi: jumlah bayangan yang hidup bersamaan, jarak antar-spawn per entity, dan umur bayangan, semuanya menyesuaikan FPS.
/// Efeknya tetap muncul (tidak dihapus); hanya dijarangkan dan dipercepat memudar bila perangkat sedang berat.
public final class NightfallAfterimageLimiter {
	private NightfallAfterimageLimiter() {}
	
	private static final int MAX_SLOTS = 6;
	private static final int FULL_LIFETIME = 20;
	/// Tick game saat tiap slot bayangan habis umurnya
	private static final long[] EXPIRY = new long[MAX_SLOTS];
	private static final Int2LongOpenHashMap LAST_SPAWN = new Int2LongOpenHashMap();
	private static float fpsEma = 60.0F;
	/// Tingkat kualitas senjata menurut FPS: 0 = penuh, 1 = tanpa glow + 85% segitiga, 2 = tanpa glow + 70% segitiga (segitiga terkecil dibuang dulu).
	/// Memakai histeresis (turun cepat, naik pelan) supaya glow tidak berkedip-kedip di sekitar ambang FPS.
	private static int quality = 0;
	private static boolean registered;
	
	public static void register() {
		if (registered) {
			return;
		}
		
		registered = true;
		ClientTickEvents.END_CLIENT_TICK.register(NightfallAfterimageLimiter::sample);
	}
	
	/// FPS dirata-rata halus (dari lama frame terakhir tiap tick), supaya satu frame lag tidak mengubah tingkat efek
	private static void sample(Minecraft minecraft) {
		float deltaTicks = minecraft.getTimer().getRealtimeDeltaTicks();
		
		if (deltaTicks > 1.0E-3F) {
			fpsEma += (20.0F / deltaTicks - fpsEma) * 0.15F;
		}
		
		if (fpsEma < 15.0F) {
			quality = 2;
		} else if (fpsEma < 24.0F) {
			quality = Math.max(quality, 1);
		} else if (fpsEma > 34.0F) {
			quality = 0;
		} else if (fpsEma > 22.0F && quality == 2) {
			quality = 1;
		}
	}
	
	/// 0 = kualitas penuh, 1 = hemat, 2 = sangat hemat (lihat komentar `quality`)
	public static int quality() {
		return quality;
	}
	
	public static float fps() {
		return fpsEma;
	}
	
	/// true bila bayangan boleh dimunculkan sekarang. Sekaligus mengatur umur bayangan putih menurut FPS.
	public static boolean allow(LivingEntity entity) {
		Minecraft minecraft = Minecraft.getInstance();
		
		if (minecraft.level == null) {
			return false;
		}
		
		// Entity jauh dari pemain: bayangannya hanya beberapa piksel, tidak perlu digambar
		if (minecraft.player != null && entity != minecraft.player && entity.distanceToSqr(minecraft.player) > 24.0D * 24.0D) {
			return false;
		}
		
		final int maxLive;
		final int gap;
		
		if (fpsEma < 10.0F) {
			EntityAfterimageParticle.whiteLifetime = 10;
			return false;
		} else if (fpsEma < 30.0F) {
			maxLive = 2;
			gap = 6;
			EntityAfterimageParticle.whiteLifetime = 12;
		} else if (fpsEma < 50.0F) {
			maxLive = 3;
			gap = 4;
			EntityAfterimageParticle.whiteLifetime = 16;
		} else {
			maxLive = 5;
			gap = 2;
			EntityAfterimageParticle.whiteLifetime = FULL_LIFETIME;
		}
		
		final long now = minecraft.level.getGameTime();
		final int id = entity.getId();
		final long last = LAST_SPAWN.getOrDefault(id, Long.MIN_VALUE);
		
		if (last != Long.MIN_VALUE && now >= last && now - last < gap) {
			return false;
		}
		
		int live = 0;
		int free = -1;
		
		for (int i = 0; i < MAX_SLOTS; i++) {
			// Slot yang waktunya jauh di depan = sisa dunia lain / reset waktu -> anggap kosong
			if (EXPIRY[i] > now && EXPIRY[i] - now <= FULL_LIFETIME) {
				live++;
			} else if (free < 0) {
				free = i;
			}
		}
		
		if (live >= maxLive || free < 0) {
			return false;
		}
		
		EXPIRY[free] = now + EntityAfterimageParticle.whiteLifetime;
		
		if (LAST_SPAWN.size() > 64) {
			LAST_SPAWN.clear();
		}
		
		LAST_SPAWN.put(id, now);
		
		return true;
	}
}
