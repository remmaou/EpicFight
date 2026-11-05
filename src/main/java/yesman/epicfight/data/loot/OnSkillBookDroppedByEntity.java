package yesman.epicfight.data.loot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.RegisterMobSkillBookLootTableEvent;

/// Fabric port: NeoForge's global loot modifier "epicfight:skillbook_modifier" (condition: killed_by_player)
/// is applied directly from the entity death-drop hook instead.
public class OnSkillBookDroppedByEntity {
	private static final Map<EntityType<?>, LootTable> SKILL_LOOT_TABLE = Maps.newHashMap();

	public static void registerEntitySkillLootTable() {
		Map<EntityType<?>, LootTable.Builder> builders = new HashMap<> ();
		RegisterMobSkillBookLootTableEvent skillBookLootTableRegistryEvent = new RegisterMobSkillBookLootTableEvent(builders);
		EpicFightEventHooks.Registry.SKILLBOOK_LOOT_TABLE.post(skillBookLootTableRegistryEvent);
		SKILL_LOOT_TABLE.clear();

		builders.forEach((k, v) -> {
			SKILL_LOOT_TABLE.put(k, v.build());
		});
	}

	/// Rolls the mob's skill book table and appends the result to its (captured) drops.
	public static void applyOnDeath(LivingEntity entity, DamageSource source, ServerLevel level, List<ItemEntity> drops) {
		LootTable table = SKILL_LOOT_TABLE.get(entity.getType());

		if (table == null || !(source.getEntity() instanceof Player)) {
			return;
		}

		LootParams.Builder paramsBuilder = new LootParams.Builder(level)
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ORIGIN, entity.position())
			.withParameter(LootContextParams.DAMAGE_SOURCE, source)
			.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
			.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity())
			.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, (Player)source.getEntity());

		LootContext context = new LootContext.Builder(paramsBuilder.create(LootContextParamSets.ENTITY)).create(Optional.empty());

		// getRandomItemsRaw avoids recursion through other loot hooks
		table.getRandomItemsRaw(context, stack -> {
			ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
			itemEntity.setDefaultPickUpDelay();
			drops.add(itemEntity);
		});
	}
}
