package yesman.epicfight.platform.fabric.shim.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleEventBus implements IEventBus {
    private record Listener<T>(Class<T> type, Consumer<T> consumer) {}

    private final List<Listener<?>> listeners = new ArrayList<>();

    @Override
    public <T> void addListener(Class<T> type, Consumer<T> listener) {
        this.listeners.add(new Listener<>(type, listener));
    }

    @Override
    public void register(Object target) {}

    @SuppressWarnings("unchecked")
    @Override
    public <T> T post(T event) {
        for (Listener<?> listener : this.listeners) {
            if (listener.type().isInstance(event)) {
                ((Consumer<Object>)listener.consumer()).accept(event);
            }
        }

        return event;
    }
}
