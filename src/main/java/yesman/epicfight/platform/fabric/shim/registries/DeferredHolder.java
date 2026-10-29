package yesman.epicfight.platform.fabric.shim.registries;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/// Fabric stand-in for NeoForge's DeferredHolder.
/// Resolves lazily against the root registry, so it also works for mod-created registries.
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {
    protected final ResourceKey<R> key;
    @Nullable
    private Holder<R> holder;

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
        return new DeferredHolder<>(ResourceKey.create(registryKey, valueName));
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    protected DeferredHolder(ResourceKey<R> key) {
        this.key = Objects.requireNonNull(key);
    }

    @SuppressWarnings("unchecked")
    protected final void bind(boolean throwOnMissing) {
        if (this.holder != null) {
            return;
        }

        Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(this.key.registry());

        if (registry != null) {
            Optional<Holder.Reference<R>> found = registry.getHolder(this.key);

            if (found.isPresent()) {
                this.holder = found.get();
                return;
            }
        }

        if (throwOnMissing) {
            throw new NullPointerException("Trying to access unbound value: " + this.key);
        }
    }

    public ResourceKey<R> getKey() {
        return this.key;
    }

    public ResourceLocation getId() {
        return this.key.location();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        this.bind(true);
        return (T) this.holder.value();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T value() {
        this.bind(true);
        return (T) this.holder.value();
    }

    @Override
    public boolean isBound() {
        this.bind(false);
        return this.holder != null && this.holder.isBound();
    }

    public Holder<R> getDelegate() {
        this.bind(true);
        return this.holder;
    }

    @Override
    public boolean is(ResourceLocation id) {
        return id.equals(this.key.location());
    }

    @Override
    public boolean is(ResourceKey<R> key) {
        return key == this.key;
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(this.key);
    }

    @Override
    public boolean is(TagKey<R> tag) {
        this.bind(false);
        return this.holder != null && this.holder.is(tag);
    }

    @Override
    public boolean is(Holder<R> other) {
        this.bind(false);
        return this.holder != null && this.holder.is(other);
    }

    @Override
    public Stream<TagKey<R>> tags() {
        this.bind(false);
        return this.holder == null ? Stream.empty() : this.holder.tags();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        this.bind(false);
        return this.holder != null && this.holder.canSerializeIn(owner);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    /// NeoForge treats a DeferredHolder and the registry's own Holder.Reference for the same key as equal
    /// (see MixinHolderReference for the other direction), so both can be used interchangeably as map keys.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Holder<?> other && (Object)other.unwrapKey().orElse(null) == (Object)this.key;
    }

    @Override
    public String toString() {
        return String.format("DeferredHolder{%s}", this.key);
    }
}
