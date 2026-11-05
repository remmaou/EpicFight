package yesman.epicfight.platform.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import yesman.epicfight.client.gui.screen.config.EpicFightSettingScreen;

/// Menampilkan layar setting Epic Fight (tab UI, grafis, model, kontrol, kamera) dari tombol Config di Mod Menu.
public class EpicFightModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new EpicFightSettingScreen(null, parent);
    }
}
