package yesman.epicfight.platform.fabric.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.platform.client.ClientModPlatform;
import yesman.epicfight.platform.client.KeyMappingRegistrar;

public final class FabricClientModPlatform implements ClientModPlatform {
    private final KeyMappingRegistrar registrar = new KeyMappingRegistrar() {
        @Override
        public void registerKeyMapping(@NotNull KeyMapping keyMapping) {
            KeyBindingHelper.registerKeyBinding(keyMapping);
        }
    };

    @Override
    public @NotNull KeyMappingRegistrar keyMappingRegistrar() {
        return this.registrar;
    }
}
