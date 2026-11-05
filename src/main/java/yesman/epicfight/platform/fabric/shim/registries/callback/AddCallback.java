package yesman.epicfight.platform.fabric.shim.registries.callback;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@FunctionalInterface
public interface AddCallback<T> extends RegistryCallback<T> {
    void onAdd(Registry<T> registry, int id, ResourceKey<T> key, T value);
}
