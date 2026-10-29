package yesman.epicfight.registry.entries;

import net.minecraft.world.entity.Entity;
import yesman.epicfight.platform.fabric.duck.EntityPatchHolder;
import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

/// Fabric port: NeoForge attachments are replaced by a field injected into Entity (see MixinEntity).
public final class EpicFightAttachmentTypes {
	private EpicFightAttachmentTypes() {}

	public static AttachmentEntityPatchProvider getPatchProvider(Entity entity) {
		return ((EntityPatchHolder)entity).epicfight$getPatchProvider();
	}
}
