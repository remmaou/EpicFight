package yesman.epicfight.nightfall.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/// Tetes darah Crimson Moon: tekstur sendiri (efn:blood_drop1/2), jatuh pelan lalu mengecil. Murah: tanpa fisika blok.
public class BloodDropParticle extends TextureSheetParticle {
	protected BloodDropParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
		super(level, x, y, z, xd, yd, zd);
		
		this.xd = xd * 0.35D;
		this.yd = yd * 0.35D + 0.05D;
		this.zd = zd * 0.35D;
		this.gravity = 0.5F;
		this.friction = 0.92F;
		this.hasPhysics = false;
		this.lifetime = 12 + this.random.nextInt(8);
		this.quadSize = 0.05F + this.random.nextFloat() * 0.05F;
		this.pickSprite(sprites);
	}
	
	@Override
	public void tick() {
		super.tick();
		this.quadSize *= 0.96F;
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;
		
		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}
		
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new BloodDropParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
		}
	}
}
