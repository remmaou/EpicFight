package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.fabric.client.model.SeparateTransformsModels;

/// Memilih model per konteks tampilan (ikon GUI, ground, fixed) seperti "neoforge:separate_transforms" di NeoForge.
@Mixin(ItemRenderer.class)
public abstract class MixinItemRendererPerspective {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void epicfight$selectPerspectiveModel(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        BakedModel resolved = SeparateTransformsModels.resolve(stack, model, context);

        if (resolved != model) {
            ((ItemRenderer) (Object) this).render(stack, context, leftHand, poseStack, buffer, light, overlay, resolved);
            ci.cancel();
        }
    }
}
