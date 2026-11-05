package yesman.epicfight.platform.fabric.shim.registries.callback;

import net.minecraft.core.Registry;

@FunctionalInterface
public interface BakeCallback<T> extends RegistryCallback<T> {
    void onBake(Registry<T> registry);
}
