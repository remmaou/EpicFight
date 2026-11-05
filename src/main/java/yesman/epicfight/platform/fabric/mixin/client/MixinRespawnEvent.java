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

@Mixin(ClientPacketListener.class)
public abstract class MixinRespawnEvent {
    @Unique
    private LocalPlayer epicfight$oldPlayer;

    // ClientPlayerNetworkEvent.Clone
    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void epicfight$respawnHead(net.minecraft.network.protocol.game.ClientboundRespawnPacket packet, CallbackInfo ci) {
        this.epicfight$oldPlayer = Minecraft.getInstance().player;
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void epicfight$respawnTail(net.minecraft.network.protocol.game.ClientboundRespawnPacket packet, CallbackInfo ci) {
        if (this.epicfight$oldPlayer != null && Minecraft.getInstance().player != null) {
            VanillaGeneralClientEventHooks.onClonedInClient(this.epicfight$oldPlayer, Minecraft.getInstance().player);
        }
    }
}
