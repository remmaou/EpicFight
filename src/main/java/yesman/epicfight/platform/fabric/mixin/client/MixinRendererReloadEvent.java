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

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinRendererReloadEvent {
    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow @Final private BlockRenderDispatcher blockRenderDispatcher;
    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;
    @Shadow @Final private Font font;
    @Shadow @Final private EntityModelSet entityModels;

    // EntityRenderersEvent.AddLayers
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void epicfight$addLayers(ResourceManager resourceManager, CallbackInfo ci) {
        EntityRendererProvider.Context context = new EntityRendererProvider.Context((EntityRenderDispatcher)(Object)this, this.itemRenderer, this.blockRenderDispatcher, this.itemInHandRenderer, resourceManager, this.entityModels, this.font);
        ClientEventBridge.MOD_BUS.post(new EntityRenderersEvent.AddLayers(context));
    }
}
