package yesman.epicfight.nightfall.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import yesman.epicfight.client.particle.HitParticle;

/// Percikan tebasan Yamato: HitParticle putih, flipbook yamato_cut.
public class YamatoCutParticle extends HitParticle {
	public YamatoCutParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
		super(level, x, y, z, sprites);
		this.rCol = 1.0F;
		this.gCol = 1.0F;
		this.bCol = 1.0F;
		this.quadSize = 2.4F;
		this.lifetime = 8;
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;
		
		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}
		
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new YamatoCutParticle(level, x, y, z, this.sprites);
		}
	}
}
