package yesman.epicfight.platform.fabric.shim.event;

import yesman.epicfight.platform.fabric.util.FabricHolders;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class EntityAttributeCreationEvent {
    public void put(EntityType<? extends LivingEntity> type, AttributeSupplier supplier) {
        FabricDefaultAttributeRegistry.register(type, FabricHolders.copyBuilder(supplier));
    }
}
