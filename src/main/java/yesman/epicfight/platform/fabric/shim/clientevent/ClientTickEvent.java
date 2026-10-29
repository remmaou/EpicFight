package yesman.epicfight.platform.fabric.shim.clientevent;

public abstract class ClientTickEvent extends ClientEvent {
    public static class Pre extends ClientTickEvent {}
    public static class Post extends ClientTickEvent {}
}
