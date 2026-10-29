package yesman.epicfight.platform.fabric.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/// Keeps a reference to the running server (integrated or dedicated) for code that has no level at hand.
public final class ServerHolder {
    private static volatile MinecraftServer server;

    private ServerHolder() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    public static MinecraftServer get() {
        return server;
    }
}
