package yesman.epicfight.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.fabric.client.ClientEventBridge;
import yesman.epicfight.platform.fabric.shim.clientevent.CustomizeGuiOverlayEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderGuiEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderHandEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderLivingEvent;

@Mixin(Gui.class)
public abstract class MixinGuiRenderEvent {
    // RenderGuiEvent.Pre
    @Inject(method = "render", at = @At("HEAD"))
    private void epicfight$renderGuiPre(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientEventBridge.GAME_BUS.post(new RenderGuiEvent.Pre(guiGraphics));
    }
}
