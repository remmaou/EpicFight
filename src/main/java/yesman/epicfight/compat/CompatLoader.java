package yesman.epicfight.compat;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yesman.epicfight.platform.fabric.shim.bus.IEventBus;

import java.util.List;

/// Memuat modul compat bila mod targetnya terpasang. Kelas modul dimuat lewat refleksi supaya build inti
/// tidak bergantung pada library pihak ketiga; modul yang tidak ikut ter-compile hanya dilewati.
public final class CompatLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("epicfight/compat");

    private record Entry(String modId, String className) {}

    private static final List<Entry> CLIENT_MODULES = List.of(
        new Entry("betterthirdperson", "yesman.epicfight.compat.betterthirdperson.BetterThirdPersonCompat"),
        new Entry("iris", "yesman.epicfight.compat.iris.IRISCompat"),
        new Entry("playeranimator", "yesman.epicfight.compat.playeranimator.PlayerAnimatorCompat"),
        new Entry("firstperson", "yesman.epicfight.compat.firstperson.FirstPersonCompat"),
        new Entry("skinlayers3d", "yesman.epicfight.compat.skinlayer3d.SkinLayer3DCompat")
    );

    private CompatLoader() {}

    public static void loadClient(IEventBus modBus, IEventBus gameBus) {
        for (Entry entry : CLIENT_MODULES) {
            if (!FabricLoader.getInstance().isModLoaded(entry.modId())) continue;

            try {
                ICompatModule module = (ICompatModule) Class.forName(entry.className()).getDeclaredConstructor().newInstance();
                module.onModEventBusClient(modBus);
                module.onGameEventBusClient(gameBus);
                LOGGER.info("Compat dimuat: {}", entry.modId());
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                LOGGER.warn("Mod {} terpasang, tapi modul compat-nya tidak ikut ter-build (lihat PORTING_STATUS.md)", entry.modId());
            } catch (Throwable t) {
                LOGGER.error("Gagal memuat compat {}", entry.modId(), t);
            }
        }
    }
}
