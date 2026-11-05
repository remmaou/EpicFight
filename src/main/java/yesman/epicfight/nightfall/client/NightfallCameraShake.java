package yesman.epicfight.nightfall.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/// Getar kamera sederhana: menggeser pandangan pemain lokal sedikit-sedikit lalu mengembalikannya (tanpa drift).
public final class NightfallCameraShake {
	private NightfallCameraShake() {}
	
	private static int remaining = 0;
	private static int total = 0;
	private static float amplitude = 0.0F;
	private static float lastX = 0.0F;
	private static float lastY = 0.0F;
	private static boolean registered = false;
	
	public static void register() {
		if (registered) {
			return;
		}
		
		registered = true;
		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> tick(minecraft));
	}
	
	public static void start(LivingEntity entity, int ticks, float strength) {
		Minecraft minecraft = Minecraft.getInstance();
		
		if (minecraft.player == null || entity != minecraft.player) {
			return;
		}
		
		// Getaran yang lebih kuat menggantikan yang lebih lemah
		if (strength >= amplitude * remaining / Math.max(total, 1)) {
			remaining = ticks;
			total = ticks;
			amplitude = strength;
		}
	}
	
	private static void tick(Minecraft minecraft) {
		Player player = minecraft.player;
		
		if (player == null) {
			remaining = 0;
			lastX = 0.0F;
			lastY = 0.0F;
			return;
		}
		
		// Kembalikan offset tick sebelumnya
		player.setXRot(player.getXRot() - lastX);
		player.setYRot(player.getYRot() - lastY);
		lastX = 0.0F;
		lastY = 0.0F;
		
		if (remaining > 0) {
			float decay = (float)remaining / (float)Math.max(total, 1);
			lastX = (player.getRandom().nextFloat() - 0.5F) * 2.0F * amplitude * decay;
			lastY = (player.getRandom().nextFloat() - 0.5F) * 2.0F * amplitude * decay;
			player.setXRot(player.getXRot() + lastX);
			player.setYRot(player.getYRot() + lastY);
			remaining--;
		}
	}
}
