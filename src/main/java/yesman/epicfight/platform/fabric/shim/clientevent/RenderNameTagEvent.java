package yesman.epicfight.platform.fabric.shim.clientevent;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class RenderNameTagEvent extends ClientEvent {
    private final Entity entity;
    private Component content;
    private TriState canRender = TriState.DEFAULT;

    public RenderNameTagEvent(Entity entity, Component content, EntityRenderer<?> renderer, PoseStack poseStack, MultiBufferSource buffers, int packedLight, float partialTick) {
        this.entity = entity;
        this.content = content;
    }

    public Entity getEntity() { return this.entity; }
    public Component getContent() { return this.content; }
    public void setContent(Component content) { this.content = content; }
    public TriState canRender() { return this.canRender; }
    public void setCanRender(TriState canRender) { this.canRender = canRender; }
}
