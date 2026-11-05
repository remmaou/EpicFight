package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// EntityJoinLevelEvent (server). Enderman spawns on the main End island can be cancelled during boss fights.
@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager {
    @Inject(method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z", at = @At("HEAD"), cancellable = true)
    private void epicfight$joinLevel(EntityAccess entityAccess, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (!(entityAccess instanceof Entity entity)) {
            return;
        }

        VanillaEntityEventHooks.onJoinLevel(entity, entity.level(), worldGenSpawned);

        if (entity.getType() == EntityType.ENDERMAN && VanillaEntityEventHooks.onEnderManSapwns((EnderMan)entity)) {
            cir.setReturnValue(false);
        }
    }
}
