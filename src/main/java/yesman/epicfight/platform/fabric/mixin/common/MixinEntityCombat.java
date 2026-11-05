package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.platform.fabric.util.DropCapture;

@Mixin(Entity.class)
public abstract class MixinEntityCombat {
    // Captures item drops of the dying entity instead of spawning them right away (see MixinLivingEntityCombat)
    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void epicfight$captureDrop(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        DropCapture capture = DropCapture.current();
        Entity self = (Entity)(Object)this;

        if (capture != null && capture.owner == self && !stack.isEmpty() && !self.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(self.level(), self.getX(), self.getY() + (double)yOffset, self.getZ(), stack);
            itemEntity.setDefaultPickUpDelay();
            capture.drops.add(itemEntity);
            cir.setReturnValue(itemEntity);
        }
    }

    // EntityMountEvent (mounting)
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("RETURN"))
    private void epicfight$startRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            VanillaEntityEventHooks.onEntityMount((Entity)(Object)this, vehicle, true);
        }
    }

    // EntityMountEvent (dismounting)
    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void epicfight$removeVehicle(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        Entity vehicle = self.getVehicle();

        if (vehicle != null) {
            VanillaEntityEventHooks.onEntityMount(self, vehicle, false);
        }
    }

    // EntityEvent.Size: Epic Fight rescales the Ender Dragon's hitbox
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void epicfight$dimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        VanillaEntityEventHooks.onSizingEntity((Entity)(Object)this, cir::setReturnValue);
    }
}
