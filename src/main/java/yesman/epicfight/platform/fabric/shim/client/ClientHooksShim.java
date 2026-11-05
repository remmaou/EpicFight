package yesman.epicfight.platform.fabric.shim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;

/// The few NeoForge ClientHooks helpers Epic Fight relies on.
public final class ClientHooksShim {
    private ClientHooksShim() {}

    public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext context, boolean applyLeftHandTransform) {
        model.getTransforms().getTransform(context).apply(applyLeftHandTransform, poseStack);
        return model;
    }

    /// Sama seperti NeoForge: memilih model per konteks (separate_transforms), menerapkan transformnya, lalu mengembalikan model itu.
    public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext context, boolean applyLeftHandTransform, net.minecraft.world.item.ItemStack stack) {
        BakedModel resolved = yesman.epicfight.platform.fabric.client.model.SeparateTransformsModels.resolve(stack, model, context);
        resolved.getTransforms().getTransform(context).apply(applyLeftHandTransform, poseStack);
        return resolved;
    }

    public static float getGuiFarPlane() {
        return 21000.0F;
    }

    /// NeoForge's InputEvent.InteractionKeyMappingTriggered replacement. Nothing cancels it on Fabric.
    public static final class InteractionKeyMappingTriggered {
        private final int button;
        private final net.minecraft.client.KeyMapping keyMapping;
        private final net.minecraft.world.InteractionHand hand;
        private boolean canceled;
        private boolean swingHand = true;

        private InteractionKeyMappingTriggered(int button, net.minecraft.client.KeyMapping keyMapping, net.minecraft.world.InteractionHand hand) {
            this.button = button;
            this.keyMapping = keyMapping;
            this.hand = hand;
        }

        public boolean isCanceled() { return this.canceled; }
        public void setCanceled(boolean canceled) { this.canceled = canceled; }
        public boolean shouldSwingHand() { return this.swingHand; }
        public void setSwingHand(boolean swingHand) { this.swingHand = swingHand; }
        public int getButton() { return this.button; }
        public net.minecraft.client.KeyMapping getKeyMapping() { return this.keyMapping; }
        public net.minecraft.world.InteractionHand getHand() { return this.hand; }
        public boolean isAttack() { return this.button == 0; }
        public boolean isUseItem() { return this.button == 1; }
        public boolean isPickBlock() { return this.button == 2; }
    }

    public static InteractionKeyMappingTriggered onClickInput(int button, net.minecraft.client.KeyMapping keyBinding, net.minecraft.world.InteractionHand hand) {
        return new InteractionKeyMappingTriggered(button, keyBinding, hand);
    }
}
