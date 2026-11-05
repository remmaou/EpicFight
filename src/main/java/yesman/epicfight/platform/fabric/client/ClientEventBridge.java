package yesman.epicfight.platform.fabric.client;

import yesman.epicfight.platform.fabric.shim.bus.SimpleEventBus;

/// The two client event buses that stand in for NeoForge's game bus and mod bus.
public final class ClientEventBridge {
    public static final SimpleEventBus GAME_BUS = new SimpleEventBus();
    public static final SimpleEventBus MOD_BUS = new SimpleEventBus();

    private ClientEventBridge() {}
}
