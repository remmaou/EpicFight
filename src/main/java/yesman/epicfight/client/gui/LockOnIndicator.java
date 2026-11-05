package yesman.epicfight.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;

/// Lock-on icon drawn over the target while the camera is locked on to it. Features: tint color / alpha / size from config,
/// size scaling with the target's body size and distance, a short "snap in" animation when a target is acquired, and an
/// optional health arc under the icon. Edit `ingame.camera.lock_on_icon.*` in the client config to customise.
public class LockOnIndicator extends EntityUI {
	private static final ResourceLocation RING = EpicFightMod.identifier("textures/gui/lock_on/ring.png");
	private static final ResourceLocation CIRCLE = EpicFightMod.identifier("textures/gui/lock_on/circle.png");
	private static final ResourceLocation ARC = EpicFightMod.identifier("textures/gui/lock_on/arc.png");
	private static final ResourceLocation BRACKETS = EpicFightMod.identifier("textures/gui/lock_on/brackets.png");
	
	private static final long SNAP_IN_MILLIS = 260L;
	
	@Nullable
	private LivingEntity lastTarget;
	private long acquiredAt;
	
	public boolean isLockedOn(LivingEntity entity) {
		EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
		return cameraApi.isLockingOnTarget() && cameraApi.getFocusingEntity() == entity;
	}
	
	@Override
	public boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTick) {
		if (!ClientConfig.lockOnIcon || !this.isLockedOn(entity)) {
			return false;
		}
		
		return entity.isAlive() && !entity.isRemoved() && !entity.isInvisibleTo(Minecraft.getInstance().player);
	}
	
	@Override
	public void draw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTick) {
		long now = System.currentTimeMillis();
		
		if (this.lastTarget != entity) {
			this.lastTarget = entity;
			this.acquiredAt = now;
		}
		
		// Snap-in animation: starts large + rotated + transparent, settles to the final state (ease-out)
		float t = Mth.clamp((now - this.acquiredAt) / (float)SNAP_IN_MILLIS, 0.0F, 1.0F);
		float eased = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
		
		Minecraft mc = Minecraft.getInstance();
		double distance = Math.sqrt(entity.distanceToSqr(mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player));
		
		// Half size in blocks. Grows with the target's body and (mildly) with distance so it stays readable far away.
		float half = 0.35F;
		
		if (ClientConfig.lockOnIconScaleWithTarget) {
			half = Mth.clamp(Math.max(entity.getBbWidth(), entity.getBbHeight() * 0.6F) * 0.55F + 0.15F, 0.3F, 4.0F);
		}
		
		half *= ClientConfig.lockOnIconSize * (float)Mth.clamp(distance / 8.0D, 1.0D, 2.5D);
		
		int rgb = ClientConfig.lockOnIconColor;
		float r = ((rgb >> 16) & 0xFF) / 255.0F;
		float g = ((rgb >> 8) & 0xFF) / 255.0F;
		float b = (rgb & 0xFF) / 255.0F;
		float alpha = ClientConfig.lockOnIconAlpha * eased;
		
		poseStack.pushPose();
		setupPoseStack(poseStack, entity, 0.0F, entity.getBbHeight() * 0.55F, 0.0F, true, partialTick);
		
		if (ClientConfig.lockOnIconStyle == 1) {
			float size = half * (1.0F + (1.0F - eased) * 0.8F);
			poseStack.mulPose(Axis.ZP.rotationDegrees((1.0F - eased) * 45.0F));
			drawTinted(poseStack, buffers, BRACKETS, size, 0.0F, 1.0F, r, g, b, alpha);
		} else {
			// Outer ring: spins in
			poseStack.pushPose();
			poseStack.mulPose(Axis.ZP.rotationDegrees((1.0F - eased) * 120.0F));
			drawTinted(poseStack, buffers, RING, half * (1.0F + (1.0F - eased) * 1.2F), 0.0F, 1.0F, r, g, b, alpha);
			poseStack.popPose();
			
			// Inner circle: tint shifts toward white when the target is hurt, so damage reads at a glance
			float health = entity.getMaxHealth() > 0.0F ? Mth.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F) : 1.0F;
			float mix = 1.0F - health;
			drawTinted(poseStack, buffers, CIRCLE, half, 0.0F, 1.0F, Mth.lerp(mix, r, 1.0F), Mth.lerp(mix, g, 1.0F), Mth.lerp(mix, b, 1.0F), alpha);
			
			if (ClientConfig.lockOnHealthArc) {
				// Bottom arc is revealed from the left in proportion to remaining health
				drawTinted(poseStack, buffers, ARC, half, 0.0F, Math.max(health, 0.02F), r, g, b, alpha);
			}
		}
		
		poseStack.popPose();
	}
	
	/// Draws a square texture of half size [half] centered on the origin; only the left [widthRatio] of it is drawn when below 1.
	private static void drawTinted(PoseStack poseStack, MultiBufferSource buffers, ResourceLocation texture, float half, float minRatio, float widthRatio, float r, float g, float b, float a) {
		VertexConsumer consumer = buffers.getBuffer(EpicFightRenderTypes.entityUITextureTinted(texture));
		Matrix4f pose = poseStack.last().pose();
		
		float minX = -half;
		float maxX = -half + 2.0F * half * widthRatio;
		
		consumer.addVertex(pose, minX, -half, 0).setUv(minRatio, 1.0F).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, -half, 0).setUv(widthRatio, 1.0F).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, half, 0).setUv(widthRatio, 0.0F).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, half, 0).setUv(minRatio, 0.0F).setColor(r, g, b, a);
	}
}
