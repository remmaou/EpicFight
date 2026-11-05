package yesman.epicfight.mixin.client;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import java.util.ArrayList;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import yesman.epicfight.client.ClientEngine;

@Mixin(value = ParticleEngine.class)
public class MixinParticleEngine {
	@Shadow
	@Final
	private static List<ParticleRenderType> RENDER_ORDER;
	
	@Shadow
	@Mutable
	private Map<ParticleRenderType, Queue<Particle>> particles;
	
	@Inject(at = @At(value = "TAIL"), method = "<init>")
	private void epicfight$constructor(ClientLevel pLevel, TextureManager pTextureManager, CallbackInfo callbackInfo) {
		this.particles = Maps.newTreeMap(ClientEngine.makeCustomLowestParticleRenderTypeComparator(RENDER_ORDER));
	}
	
	/**
	 * Vanilla (and therefore Fabric) only renders the types listed in RENDER_ORDER; NeoForge patches the loop to render every
	 * key of the particle map. Without this, every Epic Fight particle that has its own ParticleRenderType (sword trails,
	 * afterimages, ...) is simulated but never drawn.
	 */
	@Inject(at = @At(value = "TAIL"), method = "render")
	private void epicfight$renderCustomTypes(LightTexture lightTexture, Camera camera, float partialTick, CallbackInfo callbackInfo) {
		List<ParticleRenderType> customTypes = null;
		
		for (ParticleRenderType type : this.particles.keySet()) {
			if (type != ParticleRenderType.NO_RENDER && !RENDER_ORDER.contains(type)) {
				if (customTypes == null) {
					customTypes = new ArrayList<> ();
				}
				
				customTypes.add(type);
			}
		}
		
		if (customTypes == null) {
			return;
		}
		
		lightTexture.turnOnLightLayer();
		RenderSystem.enableDepthTest();
		
		for (ParticleRenderType type : customTypes) {
			Queue<Particle> queue = this.particles.get(type);
			
			if (queue == null || queue.isEmpty()) {
				continue;
			}
			
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = type.begin(tesselator, Minecraft.getInstance().getTextureManager());
			
			if (bufferBuilder == null) {
				continue;
			}
			
			for (Particle particle : queue) {
				try {
					particle.render(bufferBuilder, camera, partialTick);
				} catch (Throwable t) {
					yesman.epicfight.main.EpicFightMod.LOGGER.error("Failed to render particle of type " + type, t);
					particle.remove();
				}
			}
			
			MeshData meshData = bufferBuilder.build();
			
			if (meshData != null) {
				BufferUploader.drawWithShader(meshData);
			}
		}
		
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		lightTexture.turnOffLightLayer();
	}
}
