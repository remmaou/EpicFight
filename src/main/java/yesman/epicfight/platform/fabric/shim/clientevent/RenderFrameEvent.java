package yesman.epicfight.platform.fabric.shim.clientevent;

public abstract class RenderFrameEvent extends ClientEvent {
    public static class Pre extends RenderFrameEvent {}
    public static class Post extends RenderFrameEvent {}
}
