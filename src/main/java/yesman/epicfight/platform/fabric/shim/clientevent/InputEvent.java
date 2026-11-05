package yesman.epicfight.platform.fabric.shim.clientevent;

public abstract class InputEvent extends ClientEvent {
    public static class MouseScrollingEvent extends InputEvent {
        private final double scrollDeltaX, scrollDeltaY;

        public MouseScrollingEvent(double scrollDeltaX, double scrollDeltaY) { this.scrollDeltaX = scrollDeltaX; this.scrollDeltaY = scrollDeltaY; }

        public double getScrollDeltaX() { return this.scrollDeltaX; }
        public double getScrollDeltaY() { return this.scrollDeltaY; }
    }
}
