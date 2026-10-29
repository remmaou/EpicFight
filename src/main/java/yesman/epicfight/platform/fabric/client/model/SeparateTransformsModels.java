package yesman.epicfight.platform.fabric.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Pengganti loader NeoForge "neoforge:separate_transforms" (model berbeda per konteks tampilan).
///
/// Fabric tidak mengenal loader itu: JSON lama tidak punya "parent" di level atas sehingga item tampil kosong (blank).
/// Sekarang tiap senjata punya model dasar vanilla (`models/item/<nama>.json`, dipakai di tangan) ditambah
/// model khusus per konteks di `models/item/perspective/<nama>_<konteks>.json` (ikon GUI 16x16, ground, fixed).
/// Model tambahan dimuat lewat Fabric Model Loading API dan dipilih di {@link #resolve}.
public final class SeparateTransformsModels {
    private static final String NAMESPACE = "epicfight";

    private static final String[] ITEMS = {
        "bokken",
        "diamond_greatsword",
        "diamond_longsword",
        "diamond_spear",
        "diamond_tachi",
        "golden_greatsword",
        "golden_longsword",
        "golden_spear",
        "golden_tachi",
        "iron_greatsword",
        "iron_longsword",
        "iron_spear",
        "iron_tachi",
        "netherite_greatsword",
        "netherite_longsword",
        "netherite_spear",
        "netherite_tachi",
        "stone_greatsword",
        "stone_longsword",
        "stone_spear",
        "stone_tachi",
        "uchigatana",
        "wooden_greatsword",
        "wooden_longsword",
        "wooden_spear",
        "wooden_tachi"
    };

    /// Nama konteks sama dengan ItemDisplayContext#getSerializedName (sama dengan kunci "perspectives" NeoForge).
    private static final String[] CONTEXTS = { "gui", "fixed", "ground" };

    private static final Map<ResourceLocation, Map<String, ResourceLocation>> DEFINITIONS = new HashMap<>();
    private static final Map<ResourceLocation, BakedModel> CACHE = new HashMap<>();
    private static BakedModel cacheEpoch;

    static {
        for (String item : ITEMS) {
            Map<String, ResourceLocation> perspectives = new HashMap<>();

            for (String context : CONTEXTS) {
                perspectives.put(context, ResourceLocation.fromNamespaceAndPath(NAMESPACE, "item/perspective/" + item + "_" + context));
            }

            DEFINITIONS.put(ResourceLocation.fromNamespaceAndPath(NAMESPACE, item), perspectives);
        }
    }

    private SeparateTransformsModels() {}

    /// Semua id model tambahan yang harus ikut dimuat.
    public static List<ResourceLocation> perspectiveModelIds() {
        List<ResourceLocation> ids = new ArrayList<>();

        for (Map<String, ResourceLocation> perspectives : DEFINITIONS.values()) {
            ids.addAll(perspectives.values());
        }

        return ids;
    }

    /// Mengembalikan model untuk konteks tampilan ini; bila item tidak punya model khusus, model asli dikembalikan.
    public static BakedModel resolve(ItemStack stack, BakedModel model, ItemDisplayContext context) {
        if (stack.isEmpty()) {
            return model;
        }

        Map<String, ResourceLocation> perspectives = DEFINITIONS.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));

        if (perspectives == null) {
            return model;
        }

        ResourceLocation perspectiveId = perspectives.get(context.getSerializedName());

        if (perspectiveId == null) {
            return model;
        }

        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        BakedModel missingModel = modelManager.getMissingModel();

        // Model dibuat ulang tiap reload resource; missingModel berganti instance, jadi dipakai sebagai penanda.
        if (cacheEpoch != missingModel) {
            CACHE.clear();
            cacheEpoch = missingModel;
        }

        BakedModel baked = CACHE.get(perspectiveId);

        if (baked == null) {
            baked = modelManager.getModel(perspectiveId);
            CACHE.put(perspectiveId, baked);
        }

        return baked == null || baked == missingModel ? model : baked;
    }
}
