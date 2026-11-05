package yesman.epicfight.platform.fabric.shim.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

/// Adapter for NeoForge's LootTableLoadEvent on top of Fabric's LootTableEvents.MODIFY.
public class LootTableLoadEvent {
    private final ResourceLocation name;
    private final LootTable.Builder builder;

    public LootTableLoadEvent(ResourceLocation name, LootTable.Builder builder) {
        this.name = name;
        this.builder = builder;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public TableAdapter getTable() {
        return new TableAdapter(this.builder);
    }

    public record TableAdapter(LootTable.Builder builder) {
        public void addPool(LootPool pool) {
            this.builder.pool(pool);
        }
    }
}
