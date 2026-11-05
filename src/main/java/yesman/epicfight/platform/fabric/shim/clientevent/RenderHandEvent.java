package yesman.epicfight.platform.fabric.shim.clientevent;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;

public class RenderHandEvent extends ClientEvent {
    private final InteractionHand hand;
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int packedLight;
    private final float partialTick;

    public RenderHandEvent(InteractionHand hand, PoseStack poseStack, MultiBufferSource buffers, int packedLight, float partialTick) {
        this.hand = hand; this.poseStack = poseStack; this.buffers = buffers; this.packedLight = packedLight; this.partialTick = partialTick;
    }

    public InteractionHand getHand() { return this.hand; }
    public PoseStack getPoseStack() { return this.poseStack; }
    public MultiBufferSource getMultiBufferSource() { return this.buffers; }
    public int getPackedLight() { return this.packedLight; }
    public float getPartialTick() { return this.partialTick; }
}
