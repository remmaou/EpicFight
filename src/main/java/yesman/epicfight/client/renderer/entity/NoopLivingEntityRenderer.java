package yesman.epicfight.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderLivingEvent;
import yesman.epicfight.platform.fabric.client.ClientEventBridge;

public class NoopLivingEntityRenderer<T extends LivingEntity> extends LivingEntityRenderer<T, EntityModel<T>> {
	public NoopLivingEntityRenderer(Context context, float shadowRadius) {
		super(context, null, shadowRadius);
	}
	
	@Override
	public void render(LivingEntity livingEntity, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
		ClientEventBridge.GAME_BUS.post(new RenderLivingEvent.Pre<T, EntityModel<T>>(livingEntity, this, partialTicks, poseStack, multiBufferSource, packedLight));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return null;
	}
}