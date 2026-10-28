package yesman.epicfight.platform.fabric;

import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightCommandArgumentTypes;
import yesman.epicfight.registry.entries.EpicFightCreativeTabs;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightPotions;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.config.ServerConfig;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.data.loot.OnSkillBookDroppedByEntity;
import yesman.epicfight.epicskins.animation.EpicSkinsAnimations;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightExtensibleEnums;
import yesman.epicfight.main.EpicFightExtensions;
import yesman.epicfight.network.EntityPairingPacketType;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.platform.ModPlatformProvider;
import yesman.epicfight.platform.fabric.event.FabricCommonEvents;
import yesman.epicfight.platform.fabric.shim.event.EntityAttributeCreationEvent;
import yesman.epicfight.platform.fabric.shim.event.EntityAttributeModificationEvent;
import yesman.epicfight.platform.fabric.shim.event.RegisterSpawnPlacementsEvent;
import yesman.epicfight.platform.fabric.shim.registries.DeferredRegister;
import yesman.epicfight.platform.fabric.shim.registries.RegistryBuilder;
import yesman.epicfight.platform.fabric.util.ServerHolder;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// Common (client + server) entry point of Epic Fight on Fabric.
/// Follows the same sequence NeoForge ran through its lifecycle events:
/// constructor -> FMLConstructModEvent -> registry events -> FMLCommonSetupEvent.
public class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModPlatformProvider.initialize(new FabricModPlatform());
        ServerHolder.init();

        // --- "constructor" ---
        yesman.epicfight.api.animation.LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.api.animation.LivingMotions.class);
        yesman.epicfight.skill.SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.skill.SkillCategories.class);
        yesman.epicfight.skill.SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.skill.SkillSlots.class);
        yesman.epicfight.world.capabilities.item.Style.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.world.capabilities.item.CapabilityItem.Styles.class);
        yesman.epicfight.world.capabilities.item.WeaponCategory.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories.class);
        yesman.epicfight.world.capabilities.entitypatch.Faction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.world.capabilities.entitypatch.Factions.class);
        EntityPairingPacketType.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.network.EntityPairingPacketTypes.class);

        EpicFightRegistries.init();

        // --- configs (NeoForge ModConfigSpec through Forge Config API Port) ---
        // Forge Config API Port memuat file config langsung saat register(), jadi listener "loading" harus dipasang LEBIH DULU
        NeoForgeModConfigEvents.loading(EpicFight.MODID).register(CommonConfig::onLoad);
        NeoForgeConfigRegistry.INSTANCE.register(EpicFight.MODID, ModConfig.Type.COMMON, CommonConfig.SPEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            NeoForgeModConfigEvents.loading(EpicFight.MODID).register(ServerConfig::onLoad);
            NeoForgeConfigRegistry.INSTANCE.register(EpicFight.MODID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        }

        // --- "FMLConstructModEvent" ---
        yesman.epicfight.api.animation.LivingMotion.ENUM_MANAGER.loadEnum();
        yesman.epicfight.skill.SkillCategory.ENUM_MANAGER.loadEnum();
        yesman.epicfight.skill.SkillSlot.ENUM_MANAGER.loadEnum();
        yesman.epicfight.world.capabilities.item.Style.ENUM_MANAGER.loadEnum();
        yesman.epicfight.world.capabilities.item.WeaponCategory.ENUM_MANAGER.loadEnum();
        yesman.epicfight.world.capabilities.entitypatch.Faction.ENUM_MANAGER.loadEnum();
        EntityPairingPacketType.ENUM_MANAGER.loadEnum();

        AnimationManager.addNoWarningModId(EpicFight.EPICSKINS_MODID);
        AnimationRegistryEvent animationRegistryEvent = new AnimationRegistryEvent();
        Animations.registerAnimations(animationRegistryEvent);
        EpicSkinsAnimations.registerAnimations(animationRegistryEvent);
        FabricLoader.getInstance().getEntrypoints("epicfight:animations", AnimationRegistryListener.class).forEach(listener -> listener.onRegisterAnimations(animationRegistryEvent));
        animationRegistryEvent.getBuilders().stream().sorted(Comparator.comparing(AnimationManager.AnimationBuilder::namespace)).forEach(builder -> builder.task().accept(builder));

        // --- registry events: vanilla registries in vanilla order (blocks before items, ...), then Epic Fight's own ---
        List<DeferredRegister<?>> registers = new ArrayList<>(EpicFightRegistries.DEFERRED_REGISTRIES);
        // keySet() berasal dari HashMap (urutan acak); iterasi registry mengikuti id = urutan pendaftaran vanilla (BLOCK sebelum BLOCK_ENTITY_TYPE dst.)
        List<ResourceLocation> vanillaOrder = new ArrayList<>();
        for (net.minecraft.core.Registry<?> vanillaRegistry : BuiltInRegistries.REGISTRY) {
            vanillaOrder.add(vanillaRegistry.key().location());
        }
        registers.sort(Comparator.comparingInt(register -> {
            int index = vanillaOrder.indexOf(register.getRegistryKey().location());
            return index < 0 ? Integer.MAX_VALUE : index;
        }));
        // Util.fetchChoiceType() mencatat "No data fixer registered for <id>" (level ERROR) untuk setiap EntityType/BlockEntityType
        // modded yang bisa disimpan. Itu hanya pengecekan skema datafixer milik developer vanilla dan tidak berpengaruh ke gameplay;
        // NeoForge juga tidak menampilkannya. Dimatikan selama registrasi Epic Fight saja, lalu dikembalikan.
        boolean checkDataFixerSchema = net.minecraft.SharedConstants.CHECK_DATA_FIXER_SCHEMA;
        net.minecraft.SharedConstants.CHECK_DATA_FIXER_SCHEMA = false;
        try {
            registers.forEach(DeferredRegister::registerAll);
        } finally {
            net.minecraft.SharedConstants.CHECK_DATA_FIXER_SCHEMA = checkDataFixerSchema;
        }
        RegistryBuilder.bakeAll();

        // datapack registry (Emote), synced to clients
        DynamicRegistries.registerSynced(EpicFightRegistries.Keys.EMOTE, yesman.epicfight.client.online.cosmetics.Emote.CODEC, yesman.epicfight.client.online.cosmetics.Emote.CODEC);

        // --- attributes (EntityAttributeCreationEvent / ModificationEvent) ---
        EpicFightAttributes.EventBus.entityAttributeCreationEvent(new EntityAttributeCreationEvent());
        EntityAttributeModificationEvent modification = new EntityAttributeModificationEvent();
        EpicFightAttributes.EventBus.entityAttributeModificationEvent(modification);
        modification.apply();

        EpicFightEntityTypes.registerSpawnPlacementsEvent(new RegisterSpawnPlacementsEvent());

        // --- "FMLCommonSetupEvent" ---
        Armatures.registerEntityTypes();
        EpicFightCommandArgumentTypes.registerArgumentTypes();
        EpicFightPotions.addRecipes();
        EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.registerWeaponTypesByClass();
        EpicFightCapabilities.ENTITY_PATCH_PROVIDER.registerVanillaEntityPatches();
        EpicFightGameRules.registerGameRules();
        WeaponTypeReloadListener.registerDefaultWeaponTypes();
        EpicFightMobEffects.addOffhandModifier();
        EpicFightExtensibleEnums.initExtensibleEnums();
        EpicFightEventHooks.Registry.SKILLBOOK_LOOT_TABLE.registerEvent(EpicFightLootTables::createSkillLootTable);
        EpicFightExtensions.register(EpicFight.MODID, new EpicFightExtensions(EpicFightCreativeTabs.ITEMS));

        EpicFightNetworkManager.register();
        FabricCommonEvents.register();
    }
}
