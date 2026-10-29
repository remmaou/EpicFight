package yesman.epicfight.platform.fabric.shim.event;

import yesman.epicfight.platform.fabric.util.FabricHolders;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Fabric stand-in for NeoForge's EntityAttributeModificationEvent: collects additions, then rebuilds each
/// affected entity type's default attribute supplier. Fire once after all registries are populated.
public class EntityAttributeModificationEvent {
    private record Entry(Holder<Attribute> attribute, Double value) {}

    private final Map<EntityType<? extends LivingEntity>, List<Entry>> pending = new HashMap<>();

    public void add(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute, double value) {
        this.pending.computeIfAbsent(type, k -> new ArrayList<>()).add(new Entry(attribute, value));
    }

    public void add(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute) {
        this.pending.computeIfAbsent(type, k -> new ArrayList<>()).add(new Entry(attribute, null));
    }

    public boolean has(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute) {
        AttributeSupplier supplier = DefaultAttributes.getSupplier(type);

        if (supplier != null && supplier.hasAttribute(attribute)) {
            return true;
        }

        return this.pending.getOrDefault(type, List.of()).stream().anyMatch(entry -> entry.attribute().equals(attribute));
    }

    /// Applies every collected modification through Fabric's default attribute registry.
    public void apply() {
        this.pending.forEach((type, entries) -> {
            AttributeSupplier current = DefaultAttributes.hasSupplier(type) ? DefaultAttributes.getSupplier(type) : null;
            AttributeSupplier.Builder builder = current != null ? FabricHolders.copyBuilder(current) : AttributeSupplier.builder();

            for (Entry entry : entries) {
                if (entry.value() == null) {
                    builder.add(entry.attribute());
                } else {
                    builder.add(entry.attribute(), entry.value());
                }
            }

            FabricDefaultAttributeRegistry.register(type, builder);
        });

        this.pending.clear();
    }
}
