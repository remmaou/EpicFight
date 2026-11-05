package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.platform.fabric.duck.EntityPatchHolder;
import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

@Mixin(Entity.class)
public abstract class MixinEntityPatchHolder implements EntityPatchHolder {
    @Unique
    private AttachmentEntityPatchProvider epicfight$patchProvider;

    @Override
    public AttachmentEntityPatchProvider epicfight$getPatchProvider() {
        // Lazily created on first access, like a NeoForge attachment
        if (this.epicfight$patchProvider == null) {
            this.epicfight$patchProvider = new AttachmentEntityPatchProvider((Entity)(Object)this);
        }

        return this.epicfight$patchProvider;
    }

    // Equivalent of EntityEvent.EntityConstructing
    @Inject(method = "<init>", at = @At("TAIL"))
    private void epicfight$onConstruct(EntityType<?> type, Level level, CallbackInfo ci) {
        VanillaEntityEventHooks.onConstruct((Entity)(Object)this);
    }
}
