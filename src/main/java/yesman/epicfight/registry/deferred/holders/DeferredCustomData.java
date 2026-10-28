package yesman.epicfight.registry.deferred.holders;

import net.minecraft.resources.ResourceKey;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;
import yesman.epicfight.world.capabilities.item.custom.CustomData;

public class DeferredCustomData<T extends CustomData<?>> extends DeferredHolder<CustomData<?>, T> {
    public DeferredCustomData(ResourceKey<CustomData<?>> key) {
        super(key);
    }
}
