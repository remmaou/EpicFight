package yesman.epicfight.platform.fabric.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.fabric.client.ClientEventBridge;
import yesman.epicfight.platform.fabric.shim.clientevent.RegisterShadersEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderFrameEvent;
import yesman.epicfight.platform.fabric.shim.clientevent.ViewportEvent;

/// RenderFrameEvent.Pre/Post, RegisterShadersEvent and ViewportEvent.ComputeCameraAngles.
/// The roll component is applied by rotating the camera quaternion around its local Z axis (same as NeoForge's patch).
@Mixin(GameRenderer.class)
public abstract class MixinGameRendererEvents {
    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(method = "render", at = @At("HEAD"))
    private void epicfight$renderFramePre(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ClientEventBridge.GAME_BUS.post(new RenderFrameEvent.Pre());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void epicfight$renderFramePost(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ClientEventBridge.GAME_BUS.post(new RenderFrameEvent.Post());
    }

    @Inject(method = "reloadShaders", at = @At("TAIL"))
    private void epicfight$registerShaders(ResourceProvider provider, CallbackInfo ci) {
        ClientEventBridge.MOD_BUS.post(new RegisterShadersEvent(provider));
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.AFTER))
    private void epicfight$cameraAngles(DeltaTracker deltaTracker, CallbackInfo ci) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles(this.mainCamera, partialTick, this.mainCamera.getYRot(), this.mainCamera.getXRot(), 0.0F);

        ClientEventBridge.GAME_BUS.post(event);
        this.mainCamera.setRotation(event.getYaw(), event.getPitch());

        if (event.getRoll() != 0.0F) {
            this.mainCamera.rotation().rotateZ((float) Math.toRadians(event.getRoll()));
        }
    }
}
