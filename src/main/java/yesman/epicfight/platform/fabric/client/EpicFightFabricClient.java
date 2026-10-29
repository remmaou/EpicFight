package yesman.epicfight.platform.fabric.client;

import yesman.epicfight.client.particle.AirBurstParticle;
import yesman.epicfight.client.gui.widgets.AnchoredButton;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.particle.AshDirectionalParticle;
import yesman.epicfight.client.particle.BladeRushParticle;
import yesman.epicfight.client.particle.BloodParticle;
import yesman.epicfight.client.particle.CatharsisParticle;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.platform.fabric.shim.clientevent.ClientTickEvent;
import yesman.epicfight.client.gui.widgets.ColorDeterminator;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.client.particle.CutParticle;
import yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer;
import yesman.epicfight.client.particle.DustParticle;
import yesman.epicfight.client.particle.EnderParticle;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.registry.entries.EpicFightBlockEntities;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.client.renderer.EpicFightShaders;
import yesman.epicfight.client.particle.EviscerateParticle;
import yesman.epicfight.client.world.util.FakeLevel;
import yesman.epicfight.client.particle.FeatherParticle;
import yesman.epicfight.client.particle.ForceFieldEndParticle;
import yesman.epicfight.client.particle.ForceFieldParticle;
import yesman.epicfight.client.renderer.blockentity.FractureBlockRenderer;
import yesman.epicfight.client.particle.GroundSlamParticle;
import yesman.epicfight.client.particle.HitBluntParticle;
import yesman.epicfight.client.particle.HitCutParticle;
import yesman.epicfight.client.events.engine.IEventBasedEngine;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.platform.fabric.shim.clientevent.ItemTooltipEvent;
import yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.client.particle.LaserParticle;
import yesman.epicfight.platform.fabric.shim.clientevent.LevelTickEvent;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.client.particle.ProjectileTrailParticle;
import yesman.epicfight.platform.fabric.shim.clientevent.RegisterShadersEvent;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderHighlightEvent;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.platform.fabric.shim.clientevent.RenderLevelStageEvent;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.particle.TsunamiSplashParticle;
import yesman.epicfight.api.client.event.impl.VanillaGUIEventHooks;
import yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks;
import yesman.epicfight.client.gui.widgets.common.WidgetTheme;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.client.renderer.entity.WitherSkeletonMinionRenderer;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.config.ModConfig;
import yesman.epicfight.EpicFight;
import yesman.epicfight.EpicFightClient;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.platform.fabric.FabricReloadListener;
import yesman.epicfight.platform.fabric.shim.network.PayloadRegistrar;


/// Client entry point of Epic Fight on Fabric.
public class EpicFightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());

        // Model tambahan per konteks tampilan untuk senjata (pengganti neoforge:separate_transforms)
        net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(pluginContext ->
            pluginContext.addModels(yesman.epicfight.platform.fabric.client.model.SeparateTransformsModels.perspectiveModelIds())
        );
        yesman.epicfight.client.input.EpicFightKeyMappings.registerKeys();

        // listener "loading" harus dipasang sebelum register(): config dimuat langsung saat register()
        NeoForgeModConfigEvents.loading(EpicFight.MODID).register(ClientConfig::epicfight$modConfigLoading);
        NeoForgeConfigRegistry.INSTANCE.register(EpicFight.MODID, ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // Client-only enums
        yesman.epicfight.api.client.input.action.InputAction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, yesman.epicfight.api.client.input.action.EpicFightInputAction.class);
        yesman.epicfight.api.client.input.action.InputAction.ENUM_MANAGER.registerEnumCls("minecraft", yesman.epicfight.api.client.input.action.MinecraftInputAction.class);
        yesman.epicfight.client.gui.widgets.common.WidgetTheme.ENUM_MANAGER.registerEnumCls(yesman.epicfight.main.EpicFightMod.prefix("color_determinator_theme"), yesman.epicfight.client.gui.widgets.ColorDeterminator.Theme.class);
        yesman.epicfight.client.gui.widgets.common.WidgetTheme.ENUM_MANAGER.registerEnumCls(yesman.epicfight.main.EpicFightMod.prefix("anchored_button_built_in_theme"), yesman.epicfight.client.gui.widgets.AnchoredButton.BuiltInTheme.class);
        yesman.epicfight.client.gui.widgets.common.WidgetTheme.ENUM_MANAGER.loadEnum();
        yesman.epicfight.api.client.input.action.InputAction.ENUM_MANAGER.loadEnum();

        PayloadRegistrar.registerClientReceivers();

        // Engines listen on the typed buses
        yesman.epicfight.client.events.engine.IEventBasedEngine.init(ClientEventBridge.GAME_BUS, ClientEventBridge.MOD_BUS);
        // Core shader modded WAJIB lewat CoreShaderRegistrationCallback (Fabric API), bukan new ShaderInstance("epicfight:...")
        net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback.EVENT.register(EpicFightShaders::registerCoreShaders);
        ClientEventBridge.MOD_BUS.addListener(RegisterShadersEvent.class, yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider::epicfight$registerComputeShaders);

        registerParticles();
        registerRenderers();
        registerClientEvents();

        // Client reload listeners
        FabricReloadListener.register(PackType.CLIENT_RESOURCES, "joint_mask", new yesman.epicfight.api.client.animation.property.JointMaskReloadListener());
        FabricReloadListener.register(PackType.CLIENT_RESOURCES, "meshes", yesman.epicfight.api.client.model.Meshes.INSTANCE);
        FabricReloadListener.register(PackType.CLIENT_RESOURCES, "animation_manager_client", yesman.epicfight.api.animation.AnimationManager.getInstance());
        FabricReloadListener.register(PackType.CLIENT_RESOURCES, "item_skins", yesman.epicfight.api.client.model.ItemSkinsReloadListener.INSTANCE);

        // Built-in legacy resource pack (off by default, like the NeoForge pack finder)
        FabricLoader.getInstance().getModContainer(EpicFight.MODID).ifPresent(container ->
            ResourceManagerHelper.registerBuiltinResourcePack(EpicFight.identifier("epicfight_legacy"), container, Component.translatable("pack.epicfight_legacy.title"), ResourcePackActivationType.NORMAL));

        // FMLClientSetupEvent equivalent (needs a live GL context, so it runs once the client has started)
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider.checkIfSupports();
            yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider.ClientModule.registerClientPlayerPatches();
            yesman.epicfight.client.gui.screen.SkillBookScreen.registerIconItems();
            yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties.registerItemProperties();

            if (ClientConfig.combatCategorizedItems.isEmpty() && ClientConfig.miningCategorizedItems.isEmpty()) {
                yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen.resetItems();
            }
        });
    }

    private static void registerParticles() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        var particles = yesman.epicfight.registry.entries.EpicFightParticles.class;

        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.ENDERMAN_DEATH_EMIT.get(), yesman.epicfight.client.particle.EnderParticle.EndermanDeathEmitProvider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.HIT_BLUNT.get(), yesman.epicfight.client.particle.HitBluntParticle.Provider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.HIT_BLADE.get(), new yesman.epicfight.client.particle.HitCutParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.CUT.get(), yesman.epicfight.client.particle.CutParticle.Provider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.NORMAL_DUST.get(), yesman.epicfight.client.particle.DustParticle.NormalDustProvider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.DUST_EXPANSIVE.get(), yesman.epicfight.client.particle.DustParticle.ExpansiveDustProvider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.DUST_CONTRACTIVE.get(), yesman.epicfight.client.particle.DustParticle.ContractiveDustProvider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.EVISCERATE.get(), new yesman.epicfight.client.particle.EviscerateParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.BLOOD.get(), yesman.epicfight.client.particle.BloodParticle.Provider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.BLADE_RUSH_SKILL.get(), yesman.epicfight.client.particle.BladeRushParticle.Provider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.GROUND_SLAM.get(), new yesman.epicfight.client.particle.GroundSlamParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.GROUND_FRACTURE.get(), new yesman.epicfight.client.particle.GroundSlamParticle.BlockParticleProvider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.BREATH_FLAME.get(), yesman.epicfight.client.particle.EnderParticle.BreathFlameProvider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.FORCE_FIELD.get(), new yesman.epicfight.client.particle.ForceFieldParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.FORCE_FIELD_END.get(), new yesman.epicfight.client.particle.ForceFieldEndParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.ADRENALINE_PLAYER_BEATING.get(), new yesman.epicfight.client.particle.EntityAfterimageParticle.AdrenalineParticleProvider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.WHITE_AFTERIMAGE.get(), new yesman.epicfight.client.particle.EntityAfterimageParticle.WhiteAfterimageProvider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.LASER.get(), new yesman.epicfight.client.particle.LaserParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.NEUTRALIZE.get(), new yesman.epicfight.client.particle.DustParticle.ExpansiveMetaParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.BOSS_CASTING.get(), new yesman.epicfight.client.particle.DustParticle.ContractiveMetaParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.TSUNAMI_SPLASH.get(), yesman.epicfight.client.particle.TsunamiSplashParticle.Provider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.SWING_TRAIL.get(), new yesman.epicfight.client.particle.AnimationTrailParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.PROJECTILE_TRAIL.get(), new yesman.epicfight.client.particle.ProjectileTrailParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.FEATHER.get(), yesman.epicfight.client.particle.FeatherParticle.Provider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.AIR_BURST.get(), new yesman.epicfight.client.particle.AirBurstParticle.Provider());
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.ASH_DIRECTIONAL.get(), yesman.epicfight.client.particle.AshDirectionalParticle.Provider::new);
        registry.register(yesman.epicfight.registry.entries.EpicFightParticles.CATHARSIS.get(), yesman.epicfight.client.particle.CatharsisParticle.Provider::new);
    }

    private static void registerRenderers() {
        var types = yesman.epicfight.registry.entries.EpicFightEntityTypes.class;

        EntityRendererRegistry.register(yesman.epicfight.registry.entries.EpicFightEntityTypes.AREA_EFFECT_BREATH.get(), NoopRenderer::new);
        EntityRendererRegistry.register(yesman.epicfight.registry.entries.EpicFightEntityTypes.DROPPED_NETHER_STAR.get(), yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer::new);
        EntityRendererRegistry.register(yesman.epicfight.registry.entries.EpicFightEntityTypes.DEATH_HARVEST_ORB.get(), NoopRenderer::new);
        EntityRendererRegistry.register(yesman.epicfight.registry.entries.EpicFightEntityTypes.DODGE_LOCATION_INDICATOR.get(), NoopRenderer::new);
        EntityRendererRegistry.register(yesman.epicfight.registry.entries.EpicFightEntityTypes.WITHER_GHOST_CLONE.get(), yesman.epicfight.client.renderer.entity.WitherGhostRenderer::new);
        EntityRendererRegistry.register(yesman.epicfight.registry.entries.EpicFightEntityTypes.WITHER_SKELETON_MINION.get(), yesman.epicfight.client.renderer.entity.WitherSkeletonMinionRenderer::new);
        BlockEntityRenderers.register(yesman.epicfight.registry.entries.EpicFightBlockEntities.FRACTURE.get(), yesman.epicfight.client.renderer.blockentity.FractureBlockRenderer::new);
        yesman.epicfight.client.ClientEngine.getInstance().getAuthHelper().loadPlayerSkin();

        // Was RenderLevelStageEvent.RegisterStageEvent: item renderers need the game renderer to exist
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> yesman.epicfight.client.renderer.patched.item.RenderItemBase.initItemRenderers(Minecraft.getInstance()));

        // Modul compat (mod pihak ketiga yang terpasang)
        yesman.epicfight.compat.CompatLoader.loadClient(ClientEventBridge.MOD_BUS, ClientEventBridge.GAME_BUS);
    }

    private static void registerClientEvents() {
        // ClientTickEvent.Pre / Post, LevelTickEvent.Post
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientEventBridge.GAME_BUS.post(new ClientTickEvent.Pre()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientEventBridge.GAME_BUS.post(new ClientTickEvent.Post()));
        ClientTickEvents.END_WORLD_TICK.register(level -> ClientEventBridge.GAME_BUS.post(new LevelTickEvent.Post(level)));

        // ItemTooltipEvent
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> ClientEventBridge.GAME_BUS.post(new ItemTooltipEvent(stack, Minecraft.getInstance().player, lines)));

        // RenderLevelStageEvent (AFTER_TRIPWIRE_BLOCKS ~ after translucent terrain) and RenderHighlightEvent.Block
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> ClientEventBridge.GAME_BUS.post(new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS, context.matrixStack(), context.camera())));
        WorldRenderEvents.BLOCK_OUTLINE.register((context, outlineContext) -> !ClientEventBridge.GAME_BUS.post(new RenderHighlightEvent.Block()).isCanceled());

        // RegisterGuiLayersEvent
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            var engine = yesman.epicfight.client.events.engine.RenderEngine.getInstance();
            engine.battleModeHUD.renderStaminaBar(guiGraphics, deltaTracker);
            engine.battleModeHUD.renderNormalSkills(guiGraphics, deltaTracker);
            engine.battleModeHUD.renderWeaponInnateSkill(guiGraphics, deltaTracker);
            engine.battleModeHUD.renderChargingBar(guiGraphics, deltaTracker);
        });

        // ScreenEvent.MouseButtonPressed.Pre / MouseButtonReleased.Pre / KeyPressed.Pre
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> !yesman.epicfight.api.client.event.impl.VanillaGUIEventHooks.onMouseButtonPressedInScreen(s));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, mouseX, mouseY, button) -> !yesman.epicfight.api.client.event.impl.VanillaGUIEventHooks.onMouseButtonReleasedInScreen(s));
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, key, scancode, modifiers) -> !yesman.epicfight.api.client.event.impl.VanillaGUIEventHooks.onKeyboardPressedInScreen(s, key));
        });

        // PlayerInteractEvent.RightClickItem (client side)
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (level.isClientSide && yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks.onUseItemInClientSide(player, stack, hand)) {
                return InteractionResultHolder.fail(stack);
            }

            return InteractionResultHolder.pass(stack);
        });

        // ClientPlayerNetworkEvent.LoggingIn / LoggingOut, and LevelEvent.Load / Unload for the fake level
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks.onPlayerLoggedIn(client.player);

            if (client.level != null && !(client.level instanceof yesman.epicfight.client.world.util.FakeLevel)) {
                yesman.epicfight.client.world.util.FakeLevel.getFakeLevel(client.level);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.player != null) {
                yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks.onPlayerLoggedOut(client.player);
            }

            yesman.epicfight.client.world.util.FakeLevel.unloadFakeLevel();
        });
    }
}
