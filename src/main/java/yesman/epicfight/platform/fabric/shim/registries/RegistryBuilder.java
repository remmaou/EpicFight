package yesman.epicfight.platform.fabric.shim.registries;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import yesman.epicfight.platform.fabric.shim.registries.callback.AddCallback;
import yesman.epicfight.platform.fabric.shim.registries.callback.BakeCallback;
import yesman.epicfight.platform.fabric.shim.registries.callback.ClearCallback;
import yesman.epicfight.platform.fabric.shim.registries.callback.RegistryCallback;

import java.util.ArrayList;
import java.util.List;


/// Fabric stand-in for NeoForge's RegistryBuilder (only the subset Epic Fight uses).
/// Registries are created and registered immediately in [#create()], so they must be built during mod initialization.
public class RegistryBuilder<T> {
    private static final List<Runnable> BAKE_TASKS = new ArrayList<>();

    private final ResourceKey<? extends Registry<T>> key;
    private boolean sync;
    private final List<RegistryCallback<T>> callbacks = new ArrayList<>();

    public RegistryBuilder(ResourceKey<? extends Registry<T>> key) {
        this.key = key;
    }

    public RegistryBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public RegistryBuilder<T> callback(RegistryCallback<T> callback) {
        this.callbacks.add(callback);
        return this;
    }

    @SuppressWarnings("unchecked")
    public Registry<T> create() {
        FabricRegistryBuilder<T, MappedRegistry<T>> builder = FabricRegistryBuilder.createSimple((ResourceKey<Registry<T>>)(ResourceKey<?>)this.key);

        if (this.sync) {
            builder.attribute(RegistryAttribute.SYNCED);
        }

        MappedRegistry<T> registry = builder.buildAndRegister();

        for (RegistryCallback<T> callback : this.callbacks) {
            if (callback instanceof AddCallback<T> addCallback) {
                RegistryEntryAddedCallback.event(registry).register((rawId, id, object) -> {
                    addCallback.onAdd(registry, rawId, ResourceKey.create((ResourceKey<? extends Registry<T>>)registry.key(), id), object);
                });
            }

            if (callback instanceof BakeCallback<T> bakeCallback) {
                BAKE_TASKS.add(() -> bakeCallback.onBake(registry));
            }

            // ClearCallback has no Fabric counterpart: registries are never cleared at runtime on Fabric.
        }

        return registry;
    }

    /// Runs every BakeCallback. Call once after all DeferredRegisters have registered their entries.
    public static void bakeAll() {
        BAKE_TASKS.forEach(Runnable::run);
        BAKE_TASKS.clear();
    }
}
