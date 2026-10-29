package yesman.epicfight.compat.betterthirdperson;

import yesman.epicfight.client.camera.EpicFightTpsCameraDisableState;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisabledReason;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.platform.fabric.shim.bus.IEventBus;

/// Mematikan perspektif TPS milik Epic Fight bila Better Third Person terpasang (keduanya mengubah kamera orang ketiga).
public final class BetterThirdPersonCompat implements ICompatModule {
    @Override public void onModEventBus(IEventBus eventBus) {}
    @Override public void onGameEventBus(IEventBus eventBus) {}
    @Override public void onModEventBusClient(IEventBus eventBus) {
        EpicFightTpsCameraDisableState.disable(EpicFightTpsCameraDisabledReason.BetterThirdPerson);
    }
    @Override public void onGameEventBusClient(IEventBus eventBus) {}
}
