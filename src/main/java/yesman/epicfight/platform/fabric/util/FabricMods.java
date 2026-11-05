package yesman.epicfight.platform.fabric.util;

import net.fabricmc.loader.api.FabricLoader;

public final class FabricMods {
    private FabricMods() {}

    public static boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static String version(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

    /// Replacement for NeoForge's EventHooks.canEntityGrief (mobGriefing game rule)
    public static boolean canEntityGrief(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity) {
        return level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING);
    }
}
