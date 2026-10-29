package yesman.epicfight.platform.fabric.duck;

import yesman.epicfight.mixin.common.MixinEntity;

import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

/// Implemented onto net.minecraft.world.entity.Entity by MixinEntity. Replaces NeoForge's attachment holder.
public interface EntityPatchHolder {
    AttachmentEntityPatchProvider epicfight$getPatchProvider();
}
