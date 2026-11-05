package yesman.epicfight.platform.fabric.shim.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/// Fabric stand-in for NeoForge's DeferredRegister.
/// Nothing is registered until [#registerAll()] is called (once, from the mod initializer).
public class DeferredRegister<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private boolean registered;

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        return new DeferredRegister<>(registryKey, namespace);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.namespace = Objects.requireNonNull(namespace);
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return Collections.unmodifiableSet(this.entries.keySet());
    }

    public <I extends T> @NotNull DeferredHolder<T, I> register(@NotNull String name, @NotNull Supplier<? extends I> sup) {
        if (this.registered) {
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after registerAll() has been called.");
        }

        Objects.requireNonNull(name);
        Objects.requireNonNull(sup);

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.namespace, name);
        DeferredHolder<T, I> holder = this.createHolder(this.registryKey, id);

        if (this.entries.putIfAbsent(holder, sup) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }

        return holder;
    }

    public <I extends T> @NotNull DeferredHolder<T, I> register(@NotNull String name, @NotNull Function<ResourceLocation, ? extends I> func) {
        return this.register(name, () -> func.apply(ResourceLocation.fromNamespaceAndPath(this.namespace, name)));
    }

    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key) {
        return DeferredHolder.create(registryKey, key);
    }

    /// Registers every collected entry into the target registry. Call exactly once during mod initialization,
    /// after all custom registries have been created.
    @SuppressWarnings("unchecked")
    public void registerAll() {
        if (this.registered) {
            return;
        }

        this.registered = true;

        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(this.registryKey.location());

        if (registry == null) {
            throw new IllegalStateException("Registry not found: " + this.registryKey.location());
        }

        this.entries.forEach((holder, supplier) -> {
            Registry.registerForHolder(registry, holder.getKey(), supplier.get());
        });
    }
}
