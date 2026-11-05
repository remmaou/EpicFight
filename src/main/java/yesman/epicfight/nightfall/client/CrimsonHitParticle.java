package yesman.epicfight.nightfall.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import yesman.epicfight.nightfall.NightfallParticles;

/// Efek kena-serang Crimson Moon: satu tebasan merah (flipbook asli) + percikan darah kustom.
/// Menggantikan hit_blade bawaan (kilat kuning/oranye + tetes vanilla "drip_hang").
public class CrimsonHitParticle extends NoRenderParticle {
	public CrimsonHitParticle(ClientLevel level, double x, double y, double z, double width, double height) {
		super(level, x, y, z);
		
		this.x = x + (this.random.nextDouble() - 0.5D) * width;
		this.y = y + (this.random.nextDouble() + height) * 0.5D;
		this.z = z + (this.random.nextDouble() - 0.5D) * width;
		
		boolean shadow = level.getNearestPlayer(this.x, this.y, this.z, 8.0D, entity -> entity instanceof net.minecraft.world.entity.LivingEntity living && living.getMainHandItem().is(yesman.epicfight.nightfall.NightfallItems.SHADOW.get())) != null;
		
		level.addParticle((shadow ? NightfallParticles.SHADOW_SLASH : NightfallParticles.CRIMSON_SLASH).get(), this.x, this.y, this.z, this.random.nextDouble() * 360.0D, 1.1D + this.random.nextDouble() * 0.5D, 0.0D);
		
		for (int i = 0; i < 3; i++) {
			level.addParticle((shadow ? NightfallParticles.SHADOW_DROP : NightfallParticles.BLOOD_DROP).get(), this.x, this.y, this.z, (this.random.nextDouble() - 0.5D) * 0.5D, this.random.nextDouble() * 0.4D, (this.random.nextDouble() - 0.5D) * 0.5D);
		}
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new CrimsonHitParticle(level, x, y, z, xSpeed, ySpeed);
		}
	}
}
