package yesman.epicfight.nightfall.client;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.texture.OverlayTexture;
import yesman.epicfight.api.client.model.Meshes.MeshAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;

/// Mesh senjata yang titiknya terikat ke satu sendi per titik (mis. bilah di Tool_R, sarung di Tool_L).
/// Di-bake sekali ke koordinat lokal sendi masing-masing, lalu digambar tanpa skinning dengan pose sendi.
public final class BakedMultiMesh {
	private static final Map<String, BakedMultiMesh> CACHE = new HashMap<>();
	private static final float JOINT_STRIDE = 100.0F;
	
	private final float[] pos;
	private final float[] nrm;
	private final float[] uv;
	private final int[] joint;
	private final int count;
	
	private BakedMultiMesh(float[] pos, float[] nrm, float[] uv, int[] joint) {
		this.pos = pos;
		this.nrm = nrm;
		this.uv = uv;
		this.joint = joint;
		this.count = joint.length;
	}
	
	public static BakedMultiMesh get(String key, MeshAccessor<SkinnedMesh> accessor) {
		BakedMultiMesh cached = CACHE.get(key);
		
		if (cached != null) {
			return cached;
		}
		
		try {
			SkinnedMesh mesh = accessor.get();
			HumanoidArmature armature = Armatures.BIPED.get();
			
			if (mesh == null || armature == null) {
				return null;
			}
			
			// Tiap sendi diberi pose translasi unik (j * 100) agar sendi tiap titik bisa dibaca kembali dari posisinya.
			OpenMatrix4f[] poses = new OpenMatrix4f[armature.getJointNumber()];
			
			for (int i = 0; i < poses.length; i++) {
				poses[i] = new OpenMatrix4f();
				poses[i].translate(i * JOINT_STRIDE, 0.0F, 0.0F);
			}
			
			FloatArrayList p = new FloatArrayList();
			FloatArrayList n = new FloatArrayList();
			FloatArrayList t = new FloatArrayList();
			IntArrayList j = new IntArrayList();
			
			mesh.drawPosed(new PoseStack(), null, (consumer, px, py, pz, nx, ny, nz, light, r, g, b, a, u, v, overlay) -> {
				int id = Math.max(0, Math.min(poses.length - 1, Math.round(px / JOINT_STRIDE)));
				Vec4f position = new Vec4f(px - id * JOINT_STRIDE, py, pz, 1.0F);
				Vec4f normal = new Vec4f(nx, ny, nz, 0.0F);
				OpenMatrix4f toOrigin = armature.searchJointById(id).getToOrigin();
				OpenMatrix4f.transform(toOrigin, position, position);
				OpenMatrix4f.transform(toOrigin, normal, normal);
				p.add(position.x); p.add(position.y); p.add(position.z);
				n.add(normal.x); n.add(normal.y); n.add(normal.z);
				t.add(u); t.add(v);
				j.add(id);
			}, 0, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, null, poses);
			
			if (j.isEmpty()) {
				return null;
			}
			
			BakedMultiMesh baked = new BakedMultiMesh(p.toFloatArray(), n.toFloatArray(), t.toFloatArray(), j.toIntArray());
			CACHE.put(key, baked);
			
			return baked;
		} catch (Exception e) {
			EpicFightMod.LOGGER.error("Gagal bake mesh " + key, e);
			return null;
		}
	}
	
	public static void clearCache() {
		CACHE.clear();
	}
	
	/// Gambar semua titik; tiap sendi memakai pose-nya sendiri (poses = pose sendi dunia dari renderer item).
	public void draw(PoseStack poseStack, VertexConsumer consumer, int light, OpenMatrix4f[] poses) {
		Matrix4f[] models = new Matrix4f[poses.length];
		Matrix3f[] normals = new Matrix3f[poses.length];
		
		for (int tri = 0; tri < this.count / 3; tri++) {
			int first = tri * 3;
			int id = this.joint[first];
			
			if (id >= poses.length) {
				continue;
			}
			
			if (models[id] == null) {
				poseStack.pushPose();
				MathUtils.mulStack(poseStack, poses[id]);
				models[id] = new Matrix4f(poseStack.last().pose());
				normals[id] = new Matrix3f(poseStack.last().normal());
				poseStack.popPose();
			}
			
			Matrix4f m = models[id];
			Matrix3f nm = normals[id];
			
			// Buang sisi yang membelakangi kamera
			int k0 = first * 3;
			float x0 = this.pos[k0], y0 = this.pos[k0 + 1], z0 = this.pos[k0 + 2];
			float vx = m.m00() * x0 + m.m10() * y0 + m.m20() * z0 + m.m30();
			float vy = m.m01() * x0 + m.m11() * y0 + m.m21() * z0 + m.m31();
			float vz = m.m02() * x0 + m.m12() * y0 + m.m22() * z0 + m.m32();
			float nx0 = this.nrm[k0], ny0 = this.nrm[k0 + 1], nz0 = this.nrm[k0 + 2];
			float fx = nm.m00() * nx0 + nm.m10() * ny0 + nm.m20() * nz0;
			float fy = nm.m01() * nx0 + nm.m11() * ny0 + nm.m21() * nz0;
			float fz = nm.m02() * nx0 + nm.m12() * ny0 + nm.m22() * nz0;
			
			if (fx * vx + fy * vy + fz * vz >= 0.0F) {
				continue;
			}
			
			for (int i = first; i < first + 3; i++) {
				int k = i * 3;
				float x = this.pos[k], y = this.pos[k + 1], z = this.pos[k + 2];
				float nx = this.nrm[k], ny = this.nrm[k + 1], nz = this.nrm[k + 2];
				float tnx = nm.m00() * nx + nm.m10() * ny + nm.m20() * nz;
				float tny = nm.m01() * nx + nm.m11() * ny + nm.m21() * nz;
				float tnz = nm.m02() * nx + nm.m12() * ny + nm.m22() * nz;
				float len = (float)Math.sqrt(tnx * tnx + tny * tny + tnz * tnz);
				
				if (len > 1.0E-6F) {
					tnx /= len;
					tny /= len;
					tnz /= len;
				}
				
				consumer.addVertex(m.m00() * x + m.m10() * y + m.m20() * z + m.m30(), m.m01() * x + m.m11() * y + m.m21() * z + m.m31(), m.m02() * x + m.m12() * y + m.m22() * z + m.m32())
					.setColor(1.0F, 1.0F, 1.0F, 1.0F)
					.setUv(this.uv[i * 2], this.uv[i * 2 + 1])
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(tnx, tny, tnz);
			}
		}
	}
	
	/// Gambar hanya titik milik satu sendi dengan matriks poseStack apa adanya (untuk render tangan vanilla / kamera pertama).
	public void drawJoint(PoseStack poseStack, VertexConsumer consumer, int light, int jointId) {
		PoseStack.Pose last = poseStack.last();
		Matrix4f m = last.pose();
		Matrix3f nm = last.normal();
		
		for (int tri = 0; tri < this.count / 3; tri++) {
			int first = tri * 3;
			
			if (this.joint[first] != jointId) {
				continue;
			}
			
			int k0 = first * 3;
			float x0 = this.pos[k0], y0 = this.pos[k0 + 1], z0 = this.pos[k0 + 2];
			float vx = m.m00() * x0 + m.m10() * y0 + m.m20() * z0 + m.m30();
			float vy = m.m01() * x0 + m.m11() * y0 + m.m21() * z0 + m.m31();
			float vz = m.m02() * x0 + m.m12() * y0 + m.m22() * z0 + m.m32();
			float nx0 = this.nrm[k0], ny0 = this.nrm[k0 + 1], nz0 = this.nrm[k0 + 2];
			float fx = nm.m00() * nx0 + nm.m10() * ny0 + nm.m20() * nz0;
			float fy = nm.m01() * nx0 + nm.m11() * ny0 + nm.m21() * nz0;
			float fz = nm.m02() * nx0 + nm.m12() * ny0 + nm.m22() * nz0;
			
			if (fx * vx + fy * vy + fz * vz >= 0.0F) {
				continue;
			}
			
			for (int i = first; i < first + 3; i++) {
				int k = i * 3;
				float x = this.pos[k], y = this.pos[k + 1], z = this.pos[k + 2];
				float nx = this.nrm[k], ny = this.nrm[k + 1], nz = this.nrm[k + 2];
				float tnx = nm.m00() * nx + nm.m10() * ny + nm.m20() * nz;
				float tny = nm.m01() * nx + nm.m11() * ny + nm.m21() * nz;
				float tnz = nm.m02() * nx + nm.m12() * ny + nm.m22() * nz;
				float len = (float)Math.sqrt(tnx * tnx + tny * tny + tnz * tnz);
				
				if (len > 1.0E-6F) {
					tnx /= len;
					tny /= len;
					tnz /= len;
				}
				
				consumer.addVertex(m.m00() * x + m.m10() * y + m.m20() * z + m.m30(), m.m01() * x + m.m11() * y + m.m21() * z + m.m31(), m.m02() * x + m.m12() * y + m.m22() * z + m.m32())
					.setColor(1.0F, 1.0F, 1.0F, 1.0F)
					.setUv(this.uv[i * 2], this.uv[i * 2 + 1])
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(tnx, tny, tnz);
			}
		}
	}
}
