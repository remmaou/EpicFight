package yesman.epicfight.platform.fabric.shim.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class RegisterSpawnPlacementsEvent {
    public enum Operation { AND, OR, REPLACE }

    /// Operation is ignored on Fabric: the entry is registered as-is (these are always new entity types).
    public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
        SpawnPlacements.register(type, placementType, heightmap, predicate);
    }
}
