package yesman.epicfight.platform.fabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks;
import yesman.epicfight.platform.fabric.client.ClientEventBridge;
import yesman.epicfight.platform.fabric.shim.clientevent.EntityRenderersEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.InputEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.LivingJumpEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.MovementInputUpdateEvent;

@Mixin(KeyboardInput.class)
public abstract class MixinMoveInputEvent {
    // MovementInputUpdateEvent
    @Inject(method = "tick", at = @At("TAIL"))
    private void epicfight$movementInput(boolean isMovingSlowly, float sneakingSpeedBonus, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            ClientEventBridge.GAME_BUS.post(new MovementInputUpdateEvent(player, (KeyboardInput)(Object)this));
        }
    }
}
