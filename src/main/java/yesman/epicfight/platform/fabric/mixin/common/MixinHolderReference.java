package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/// Makes the registry's Holder.Reference and Epic Fight's DeferredHolder interchangeable as hash-map keys,
/// like on NeoForge. Registry keys are interned, so comparing and hashing them by identity is cheap and exact.
///
/// Without this, lookups such as attributeMap.get(EpicFightAttributes.X) or skillDataManager.getDataValue(KEY)
/// miss entries that were stored under the registry's own holder (and vice versa).
@Mixin(Holder.Reference.class)
public abstract class MixinHolderReference<T> {
    @Shadow
    private ResourceKey<T> key;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return this.key != null && obj instanceof Holder<?> other && (Object)other.unwrapKey().orElse(null) == (Object)this.key;
    }

    @Override
    public int hashCode() {
        return this.key != null ? this.key.hashCode() : System.identityHashCode(this);
    }
}
