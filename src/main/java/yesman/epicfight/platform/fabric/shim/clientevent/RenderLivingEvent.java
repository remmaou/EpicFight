package yesman.epicfight.platform.fabric.shim.clientevent;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public abstract class RenderLivingEvent<T extends LivingEntity, M extends EntityModel<T>> extends ClientEvent {
    private final LivingEntity entity;
    private final LivingEntityRenderer<T, M> renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int packedLight;

    protected RenderLivingEvent(LivingEntity entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        this.entity = entity; this.renderer = renderer; this.partialTick = partialTick; this.poseStack = poseStack; this.buffers = buffers; this.packedLight = packedLight;
    }

    public LivingEntity getEntity() { return this.entity; }
    public LivingEntityRenderer<T, M> getRenderer() { return this.renderer; }
    public float getPartialTick() { return this.partialTick; }
    public PoseStack getPoseStack() { return this.poseStack; }
    public MultiBufferSource getMultiBufferSource() { return this.buffers; }
    public int getPackedLight() { return this.packedLight; }

    public static class Pre<T extends LivingEntity, M extends EntityModel<T>> extends RenderLivingEvent<T, M> {
        public Pre(LivingEntity entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
            super(entity, renderer, partialTick, poseStack, buffers, packedLight);
        }
    }
}
