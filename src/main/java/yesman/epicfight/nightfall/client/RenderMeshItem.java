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
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Renderer item skin "efn:mesh_item": mesh 3D asli Crimson Moon (sudah di-bake, lihat BakedScythe).
/// Kunci JSON item_skins (semua opsional, bisa disetel tanpa build ulang lalu F3+T):
///   "texture", "texture_l"            : tekstur utama dan lapisan menyala
///   "mesh_offset"/"mesh_rotation"/"mesh_scale" : tambahan di sendi (tangan atau punggung)
///   "fp_offset"/"fp_rotation"/"fp_scale"       : tambahan khusus kamera pertama / render vanilla
public class RenderMeshItem extends RenderItemBase {
	/// Tampilan per item (tekstur, glow, ikon, setelan kamera pertama). Kunci = nama item ("crimson_moon", "shadow").
	public static final class Look {
		public final ResourceLocation texture;
		public final ResourceLocation glow;
		public final ResourceLocation icon;
		public final float[] fpOffset;
		public final float[] fpRotation;
		public final float fpScale;
		
		public Look(ResourceLocation texture, ResourceLocation glow, ResourceLocation icon, float[] fpOffset, float[] fpRotation, float fpScale) {
			this.texture = texture;
			this.glow = glow;
			this.icon = icon;
			this.fpOffset = fpOffset;
			this.fpRotation = fpRotation;
			this.fpScale = fpScale;
		}
	}
	
	private static final java.util.Map<String, Look> LOOKS = new java.util.concurrent.ConcurrentHashMap<>();
	private static final java.util.Map<ResourceLocation, RenderType> TYPE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
	
	public static Look look(String key) {
		return LOOKS.computeIfAbsent(key, k -> {
			String tex = k.equals("shadow") ? "shadow" : "crimson_moon";
			return new Look(ResourceLocation.parse("efn:textures/item/weapon/" + tex + ".png"), ResourceLocation.parse("efn:textures/item/weapon/" + tex + "_light.png"),
				ResourceLocation.parse("efn:textures/item/" + (k.equals("shadow") ? "shadow" : "crimson_moon") + ".png"), new float[3], new float[3], 1.0F);
		});
	}
	
	private final float[] offset;
	private final float[] rotation;
	private final float scale;
	private final Look look;
	
	public RenderMeshItem(JsonElement jsonElement) {
		super(jsonElement);
		
		JsonObject json = jsonElement.getAsJsonObject();
		this.offset = readVector(json, "mesh_offset");
		this.rotation = readVector(json, "mesh_rotation");
		this.scale = GsonHelper.getAsFloat(json, "mesh_scale", 1.0F);
		
		String key = GsonHelper.getAsString(json, "key", "crimson_moon");
		Look defaults = look(key);
		ResourceLocation texture = json.has("texture") ? ResourceLocation.parse(GsonHelper.getAsString(json, "texture")) : defaults.texture;
		ResourceLocation glow = json.has("texture_l") ? ResourceLocation.parse(GsonHelper.getAsString(json, "texture_l")) : defaults.glow;
		this.look = new Look(texture, glow, defaults.icon, readVector(json, "fp_offset"), readVector(json, "fp_rotation"), GsonHelper.getAsFloat(json, "fp_scale", 1.0F));
		LOOKS.put(key, this.look);
	}
	
	private static float[] readVector(JsonObject json, String key) {
		float[] result = new float[3];
		
		if (json.has(key)) {
			var array = json.getAsJsonArray(key);
			
			for (int i = 0; i < 3 && i < array.size(); i++) {
				result[i] = array.get(i).getAsFloat();
			}
		}
		
		return result;
	}
	
	private static OpenMatrix4f handCorrection() {
		return GLOBAL_MAINHAND_ITEM_TRANSFORMS.get("Tool_R");
	}
	
	/// item-space (kerangka item vanilla) <- lokal Tool_R. Dipakai render vanilla / kamera pertama.
	public static OpenMatrix4f handInverse() {
		return new OpenMatrix4f().load(handCorrection()).invert();
	}
	
	/// Kerangka sendi punggung <- lokal Tool_R: pakai koreksi punggung bawaan Epic Fight untuk item di punggung.
	private static OpenMatrix4f backMatrix() {
		return new OpenMatrix4f().load(GLOBAL_MAINHAND_ITEM_TRANSFORMS.get("Chest")).mulBack(handInverse());
	}
	
	public static void applyTuning(OpenMatrix4f m, float[] offset, float[] rotation, float scale) {
		m.translate(offset[0], offset[1], offset[2]);
		
		if (rotation[0] != 0.0F) {
			m.rotateDeg(rotation[0], Vec3f.X_AXIS);
		}
		
		if (rotation[1] != 0.0F) {
			m.rotateDeg(rotation[1], Vec3f.Y_AXIS);
		}
		
		if (rotation[2] != 0.0F) {
			m.rotateDeg(rotation[2], Vec3f.Z_AXIS);
		}
		
		if (scale != 1.0F) {
			m.scale(scale, scale, scale);
		}
	}
	
	/// Jarak (blok) di mana lapisan menyala (glow) berhenti digambar: di sejauh ini lapisan itu hanya beberapa piksel
	private static final float GLOW_MAX_DISTANCE_SQR = 20.0F * 20.0F;
	/// Di atas ini senjata tidak digambar sama sekali (di luar jarak render entity biasa)
	private static final float MESH_MAX_DISTANCE_SQR = 100.0F * 100.0F;
	
	public static void drawMesh(PoseStack poseStack, MultiBufferSource buffer, int light, Look look) {
		float distanceSqr = BakedScythe.distanceSqr(poseStack);
		
		if (distanceSqr > MESH_MAX_DISTANCE_SQR) {
			return;
		}
		
		// RenderType di-cache supaya tidak dibuat ulang tiap frame
		RenderType main = TYPE_CACHE.computeIfAbsent(look.texture, t -> EpicFightRenderTypes.getTriangulated(RenderType.entityCutout(t)));
		// Kualitas adaptif menurut FPS (NightfallAfterimageLimiter.quality): 0 penuh; 1 hemat; 2 sangat hemat (tanpa glow, segitiga terkecil dibuang)
		final int quality = NightfallAfterimageLimiter.quality();
		BakedScythe.draw(poseStack, buffer.getBuffer(main), light, 1.0F, 1.0F, 1.0F, 1.0F, quality == 0 ? 1.0F : quality == 1 ? 0.85F : 0.7F);
		
		if (quality == 0 && look.glow != null && distanceSqr <= GLOW_MAX_DISTANCE_SQR) {
			RenderType glow = TYPE_CACHE.computeIfAbsent(look.glow, t -> EpicFightRenderTypes.getTriangulated(RenderType.eyes(t)));
			BakedScythe.draw(poseStack, buffer.getBuffer(glow), 15728880, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		Joint parentJoint = entitypatch.getParentJointOfHand(hand);
		
		if (parentJoint == null || !BakedScythe.ensure()) {
			super.renderItemInHand(stack, entitypatch, hand, poses, buffer, poseStack, packedLight, partialTicks);
			return;
		}
		
		OpenMatrix4f matrix = new OpenMatrix4f().load(poses[parentJoint.getId()]);
		
		if (!parentJoint.getName().startsWith("Tool_")) {
			matrix.mulBack(backMatrix());
		}
		
		applyTuning(matrix, this.offset, this.rotation, this.scale);
		
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, matrix);
		drawMesh(poseStack, buffer, packedLight, this.look);
		poseStack.popPose();
	}
}
