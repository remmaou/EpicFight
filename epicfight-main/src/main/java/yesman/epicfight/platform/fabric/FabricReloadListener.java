package yesman.epicfight.platform.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.EpicFight;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/// Wraps any vanilla reload listener as a Fabric identifiable one, chained so that listeners run in registration order
/// (NeoForge's AddReloadListenerEvent guarantees this order; Fabric needs explicit dependencies).
public final class FabricReloadListener implements IdentifiableResourceReloadListener {
    private static final Map<PackType, ResourceLocation> PREVIOUS = new EnumMap<>(PackType.class);

    private final ResourceLocation id;
    private final PreparableReloadListener delegate;
    private final Collection<ResourceLocation> dependencies;

    private FabricReloadListener(ResourceLocation id, PreparableReloadListener delegate, Collection<ResourceLocation> dependencies) {
        this.id = id;
        this.delegate = delegate;
        this.dependencies = dependencies;
    }

    public static void register(PackType type, String path, PreparableReloadListener listener) {
        ResourceLocation id = EpicFight.identifier(path);
        ResourceLocation previous = PREVIOUS.get(type);

        ResourceManagerHelper.get(type).registerReloadListener(new FabricReloadListener(id, listener, previous == null ? List.of() : List.of(previous)));
        PREVIOUS.put(type, id);
    }

    @Override
    public ResourceLocation getFabricId() {
        return this.id;
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return this.dependencies;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return this.delegate.reload(barrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }
}
