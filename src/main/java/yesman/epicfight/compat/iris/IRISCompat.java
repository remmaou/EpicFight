package yesman.epicfight.compat.iris;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.FakeBlockRenderer;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.platform.fabric.shim.bus.IEventBus;

public class IRISCompat implements ICompatModule {
    private static final Logger LOGGER = LoggerFactory.getLogger("epicfight/compat/iris");

    @Override public void onModEventBus(IEventBus eventBus) {}
    @Override public void onGameEventBus(IEventBus eventBus) {}

    @Override
    public void onModEventBusClient(IEventBus eventBus) {
        // Setara FMLClientSetupEvent: jalankan setelah klien siap
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ComputeShaderProvider.initIris();

            // Renderer blok khusus Sodium+Iris ada di source set compat "iris" (butuh library Sodium dan Iris saat build).
            // Dimuat lewat refleksi; bila tidak ikut ter-build, dipakai renderer vanilla.
            try {
                FakeBlockRenderer renderer = (FakeBlockRenderer) Class.forName("yesman.epicfight.compat.sodium.client.SodiumFakeBlockRenderer").getDeclaredConstructor().newInstance();
                RenderEngine.getInstance().reloadFakeBlockRenderer(renderer);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                LOGGER.info("SodiumFakeBlockRenderer tidak ikut ter-build, memakai renderer blok vanilla");
            } catch (Throwable t) {
                LOGGER.error("Gagal memasang SodiumFakeBlockRenderer, memakai renderer vanilla", t);
            }
        });
    }

    @Override public void onGameEventBusClient(IEventBus eventBus) {}
}
