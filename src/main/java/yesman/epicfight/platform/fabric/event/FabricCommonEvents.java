package yesman.epicfight.platform.fabric.event;

import yesman.epicfight.server.commands.AnimatorCommand;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.api.ex_cap.listeners.ConditionalReloadListener;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ItemPresetReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.ex_cap.listeners.MovesetReloadListener;
import yesman.epicfight.server.commands.PlayerModeCommand;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.server.commands.PlayerStaminaCommand;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.api.data.reloader.SkillReloadListener;
import yesman.epicfight.api.ex_cap.listeners.WeaponModifierReloadListener;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.api.event.impl.VanillaItemEventHooks;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.data.loot.OnSkillBookDroppedByEntity;
import yesman.epicfight.main.EpicFightExtensions;
import yesman.epicfight.platform.fabric.FabricReloadListener;
import yesman.epicfight.platform.fabric.shim.event.LootTableLoadEvent;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.EpicFightCreativeTabs;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;

/// Everything NeoForge delivered through its event bus on the server/common side, rebuilt on Fabric API events.
/// (Mixin-driven hooks such as damage and knockback live in platform.fabric.mixin.)
public final class FabricCommonEvents {
    private FabricCommonEvents() {}

    public static void register() {
        // LivingEquipmentChangeEvent
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) -> VanillaEntityEventHooks.onEquipmentChanged(entity, previous, current, slot));

        // PlayerEvent.StartTracking / StopTracking
        EntityTrackingEvents.START_TRACKING.register((tracked, player) -> VanillaPlayerEventHooks.onStartTracking(tracked, player));
        EntityTrackingEvents.STOP_TRACKING.register((tracked, player) -> VanillaPlayerEventHooks.onStopTracking(tracked, player));

        // PlayerEvent.Clone / PlayerChangedDimensionEvent
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> VanillaPlayerEventHooks.onCloned(oldPlayer, newPlayer, !alive));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> VanillaPlayerEventHooks.onChagneDimension(player));

        // PlayerInteractEvent.RightClickItem (server side; the client side hook is registered by the client initializer)
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (!level.isClientSide && VanillaPlayerEventHooks.onUseItemInServerSide(player)) {
                return InteractionResultHolder.fail(stack);
            }

            return InteractionResultHolder.pass(stack);
        });

        // ItemAttributeModifierEvent: Fabric API 1.21.1 tidak punya callback ini, dikerjakan oleh MixinItemStackModifiers

        // LootTableLoadEvent
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> EpicFightLootTables.onLootTableRegistry(new LootTableLoadEvent(key.location(), tableBuilder)));

        // OnDatapackSyncEvent (fires per player, on join and on /reload)
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> FabricWorldEvents.onDatapackSync(player));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> OnSkillBookDroppedByEntity.registerEntitySkillLootTable());

        // RegisterCommandsEvent
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayerModeCommand.register(dispatcher);
            PlayerSkillCommand.register(dispatcher);
            PlayerStaminaCommand.register(dispatcher);
            AnimatorCommand.register(dispatcher);
        });

        // AddReloadListenerEvent (animation manager first, like HIGHEST priority did)
        FabricReloadListener.register(PackType.SERVER_DATA, "animation_manager", AnimationManager.getInstance());
        FabricReloadListener.register(PackType.SERVER_DATA, "collider_preset", new ColliderPreset());
        FabricReloadListener.register(PackType.SERVER_DATA, "skill", new SkillReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "item_preset", new ItemPresetReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "weapon_modifier", new WeaponModifierReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "conditional", new ConditionalReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "moveset", new MovesetReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "weapon_type", new WeaponTypeReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "item_keyword", new ItemKeywordReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "item_capability", new ItemCapabilityReloadListener());
        FabricReloadListener.register(PackType.SERVER_DATA, "mob_patch", new MobPatchReloadListener());

        // BuildCreativeModeTabContentsEvent: skill books
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((tab, entries) -> {
            EpicFightRegistries.SKILL.keySet().stream().map(id -> id.getNamespace()).distinct().forEach(modid -> {
                EpicFightExtensions.get(modid).ifPresentOrElse(extension -> {
                    if (extension.skillBookCreativeTab().get() == tab) {
                        EpicFightRegistries.SKILL.holders()
                            .filter(skill -> skill.value().getCategory().learnable() && skill.value().getCreativeTab() == null && skill.value().getRegistryName().getNamespace().equals(modid))
                            .forEach(holder -> entries.accept(skillBook(holder)));
                    }
                }, () -> {
                    if (tab == EpicFightCreativeTabs.ITEMS.get()) {
                        EpicFightRegistries.SKILL.holders()
                            .filter(skill -> skill.value().getCategory().learnable() && skill.value().getCreativeTab() == null && skill.value().getRegistryName().getNamespace().equals(modid))
                            .forEach(holder -> entries.accept(skillBook(holder)));
                    }
                });
            });

            EpicFightRegistries.SKILL.holders()
                .filter(skill -> skill.value().getCategory().learnable() && skill.value().getCreativeTab() == tab)
                .forEach(holder -> entries.accept(skillBook(holder)));
        });
    }

    private static ItemStack skillBook(net.minecraft.core.Holder<yesman.epicfight.skill.Skill> holder) {
        ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
        SkillBookItem.setContainingSkill(holder, stack);
        return stack;
    }
}
