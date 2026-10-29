package yesman.epicfight.platform.fabric.shim.bus;

import java.util.function.Consumer;

/// Minimal typed event bus. Used for Epic Fight's client-side event listeners and as the surface compat modules compile against.
public interface IEventBus {
    <T> void addListener(Class<T> type, Consumer<T> listener);

    /// Legacy single-argument form (type unknown at runtime): kept so that unported compat modules still compile; never dispatched.
    default <T> void addListener(Consumer<T> listener) {}

    void register(Object target);

    <T> T post(T event);
}
