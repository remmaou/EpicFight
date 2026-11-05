package yesman.epicfight.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.fabric.client.ClientEventBridge;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderLivingEvent;

/// RenderLivingEvent.Pre
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingRenderEvent<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void epicfight$renderLivingPre(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        RenderLivingEvent.Pre<T, M> event = new RenderLivingEvent.Pre<>(entity, (LivingEntityRenderer<T, M>)(Object)this, partialTicks, poseStack, buffer, packedLight);

        if (ClientEventBridge.GAME_BUS.post(event).isCanceled()) {
            ci.cancel();
        }
    }
}
