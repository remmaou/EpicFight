package yesman.epicfight.compat;

import yesman.epicfight.platform.fabric.shim.bus.IEventBus;

/// Kept so that compatibility modules can be ported one at a time (see _pending/compat and PORTING_STATUS.md).
public interface ICompatModule {
	void onModEventBus(IEventBus eventBus);

	void onGameEventBus(IEventBus eventBus);

	void onModEventBusClient(IEventBus eventBus);

	void onGameEventBusClient(IEventBus eventBus);
}
