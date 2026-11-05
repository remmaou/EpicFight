package yesman.epicfight.nightfall.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.client.model.Meshes.MeshAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.nightfall.YamatoSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Renderer item skin "efn:yamato_mesh": mesh asli Yamato. Terhunus = bilah di tangan kanan + sarung di tangan kiri;
/// tersarung = seluruh pedang di dalam sarung (tangan kiri). Item skin: mesh_drawn, mesh_sheathed, texture.
public class RenderYamatoItem extends RenderItemBase {
	private final String keyDrawn;
	private final String keySheathed;
	private final MeshAccessor<SkinnedMesh> drawn;
	private final MeshAccessor<SkinnedMesh> sheathed;
	private final RenderType type;
	
	public RenderYamatoItem(JsonElement jsonElement) {
		super(jsonElement);
		
		JsonObject json = jsonElement.getAsJsonObject();
		ResourceLocation drawnLoc = ResourceLocation.parse(GsonHelper.getAsString(json, "mesh_drawn"));
		ResourceLocation sheathedLoc = ResourceLocation.parse(GsonHelper.getAsString(json, "mesh_sheathed", GsonHelper.getAsString(json, "mesh_drawn")));
		ResourceLocation texture = ResourceLocation.parse(GsonHelper.getAsString(json, "texture"));
		
		this.keyDrawn = drawnLoc.toString();
		this.keySheathed = sheathedLoc.toString();
		this.drawn = MeshAccessor.create(drawnLoc.getNamespace(), drawnLoc.getPath(), (loader) -> loader.loadSkinnedMesh(SkinnedMesh::new));
		this.sheathed = MeshAccessor.create(sheathedLoc.getNamespace(), sheathedLoc.getPath(), (loader) -> loader.loadSkinnedMesh(SkinnedMesh::new));
		this.type = EpicFightRenderTypes.getTriangulated(RenderType.entityCutout(texture));
		BakedMultiMesh.clearCache();
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		boolean isDrawn = YamatoSkill.isDrawn(entitypatch);
		BakedMultiMesh mesh = isDrawn ? BakedMultiMesh.get(this.keyDrawn, this.drawn) : BakedMultiMesh.get(this.keySheathed, this.sheathed);
		
		if (mesh == null) {
			super.renderItemInHand(stack, entitypatch, hand, poses, buffer, poseStack, packedLight, partialTicks);
			return;
		}
		
		mesh.draw(poseStack, buffer.getBuffer(this.type), packedLight, poses);
	}
}
