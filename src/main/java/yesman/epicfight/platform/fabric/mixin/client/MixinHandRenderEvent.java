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

@Mixin(ItemInHandRenderer.class)
public abstract class MixinHandRenderEvent {
    // RenderHandEvent
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void epicfight$renderHand(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (ClientEventBridge.GAME_BUS.post(new RenderHandEvent(hand, poseStack, buffer, packedLight, partialTicks)).isCanceled()) {
            ci.cancel();
        }
    }
}
