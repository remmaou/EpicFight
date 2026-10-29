package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.util.function.Consumer;

/// Shaders created with the given provider are already loaded, so the "loaded" callback runs immediately.
public class RegisterShadersEvent extends ClientEvent {
    private final ResourceProvider provider;

    public RegisterShadersEvent(ResourceProvider provider) { this.provider = provider; }

    public ResourceProvider getResourceProvider() { return this.provider; }

    public void registerShader(ShaderInstance shader, Consumer<ShaderInstance> onLoaded) { onLoaded.accept(shader); }
}
