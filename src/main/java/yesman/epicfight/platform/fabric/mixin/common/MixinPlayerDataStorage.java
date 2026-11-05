package yesman.epicfight.platform.fabric.mixin.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;

import java.io.File;
import java.util.Optional;

/// PlayerEvent.LoadFromFile
@Mixin(PlayerDataStorage.class)
public abstract class MixinPlayerDataStorage {
    @Shadow
    @Final
    private File playerDir;

    @Inject(method = "load(Lnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;", at = @At("RETURN"))
    private void epicfight$load(Player player, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        VanillaPlayerEventHooks.onLoad(player, this.playerDir, player.getStringUUID());
    }
}
