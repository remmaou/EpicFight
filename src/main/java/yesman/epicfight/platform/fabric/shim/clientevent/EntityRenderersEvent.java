package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public abstract class EntityRenderersEvent extends ClientEvent {
    public static class AddLayers extends EntityRenderersEvent {
        private final EntityRendererProvider.Context context;

        public AddLayers(EntityRendererProvider.Context context) { this.context = context; }

        public EntityRendererProvider.Context getContext() { return this.context; }
    }
}
