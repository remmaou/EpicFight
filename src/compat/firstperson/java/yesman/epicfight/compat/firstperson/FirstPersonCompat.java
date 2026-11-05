package yesman.epicfight.compat.firstperson;

import dev.tr7zw.firstperson.api.ActivationHandler;
import dev.tr7zw.firstperson.api.FirstPersonAPI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.platform.fabric.shim.bus.IEventBus;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class FirstPersonCompat implements ICompatModule {
    @Override
    public void onModEventBusClient(IEventBus eventBus) {
        ClientLifecycleEvents.CLIENT_STARTED.register(client ->
            FirstPersonAPI.getActivationHandlers().add(new ActivationHandler() {
                @Override
                public boolean preventFirstperson() {
                    PlayerPatch<?> playerpatch = EpicFightCapabilities.getCachedLocalPlayerPatch();

                    return playerpatch != null
                        && (playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT || !ClientConfig.enableOriginalModel)
                        && ClientConfig.enableAnimatedFirstPersonModel;
                }
            }));
    }

    @Override public void onGameEventBusClient(IEventBus eventBus) {}
    @Override public void onModEventBus(IEventBus eventBus) {}
    @Override public void onGameEventBus(IEventBus eventBus) {}
}
