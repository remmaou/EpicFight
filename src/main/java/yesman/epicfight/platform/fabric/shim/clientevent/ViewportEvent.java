package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.client.Camera;

public abstract class ViewportEvent extends ClientEvent {
    private final Camera camera;
    private final double partialTick;

    protected ViewportEvent(Camera camera, double partialTick) { this.camera = camera; this.partialTick = partialTick; }

    public Camera getCamera() { return this.camera; }
    public double getPartialTick() { return this.partialTick; }

    public static class ComputeCameraAngles extends ViewportEvent {
        private float yaw, pitch, roll;

        public ComputeCameraAngles(Camera camera, double partialTick, float yaw, float pitch, float roll) {
            super(camera, partialTick);
            this.yaw = yaw; this.pitch = pitch; this.roll = roll;
        }

        public float getYaw() { return this.yaw; }
        public void setYaw(float yaw) { this.yaw = yaw; }
        public float getPitch() { return this.pitch; }
        public void setPitch(float pitch) { this.pitch = pitch; }
        public float getRoll() { return this.roll; }
        public void setRoll(float roll) { this.roll = roll; }
    }
}
