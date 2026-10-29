package yesman.epicfight.platform.fabric.shim.registries.callback;

import net.minecraft.core.Registry;

@FunctionalInterface
public interface ClearCallback<T> extends RegistryCallback<T> {
    void onClear(Registry<T> registry, boolean full);
}
