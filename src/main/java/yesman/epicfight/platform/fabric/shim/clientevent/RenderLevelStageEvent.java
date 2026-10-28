package yesman.epicfight.platform.fabric.shim.clientevent;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

public class RenderLevelStageEvent extends ClientEvent {
    public enum Stage { AFTER_TRIPWIRE_BLOCKS }

    private final Stage stage;
    private final PoseStack poseStack;
    private final Camera camera;

    public RenderLevelStageEvent(Stage stage, PoseStack poseStack, Camera camera) { this.stage = stage; this.poseStack = poseStack; this.camera = camera; }

    public Stage getStage() { return this.stage; }
    public PoseStack getPoseStack() { return this.poseStack; }
    public Camera getCamera() { return this.camera; }
}
