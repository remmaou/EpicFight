package yesman.epicfight.nightfall.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/// Tebasan merah Crimson Moon: quad terang dengan animasi 7 frame (crimson_slash1..7).
public class CrimsonSlashParticle extends TextureSheetParticle {
	private final SpriteSet sprites;
	
	protected CrimsonSlashParticle(ClientLevel level, double x, double y, double z, double rollDeg, double scale, SpriteSet sprites) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		
		this.sprites = sprites;
		this.lifetime = 7;
		this.quadSize = (float)scale;
		this.hasPhysics = false;
		this.gravity = 0.0F;
		this.xd = 0.0D;
		this.yd = 0.0D;
		this.zd = 0.0D;
		this.roll = (float)Math.toRadians(rollDeg);
		this.oRoll = this.roll;
		this.setSpriteFromAge(sprites);
	}
	
	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.oRoll = this.roll;
		
		if (this.age++ >= this.lifetime) {
			this.remove();
			return;
		}
		
		this.setSpriteFromAge(this.sprites);
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	@Override
	protected int getLightColor(float partialTick) {
		return 15728880;
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;
		
		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}
		
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new CrimsonSlashParticle(level, x, y, z, xSpeed, ySpeed <= 0.0D ? 4.0D : ySpeed, this.sprites);
		}
	}
}
