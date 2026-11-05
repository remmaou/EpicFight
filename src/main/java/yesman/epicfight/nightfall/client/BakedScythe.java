package yesman.epicfight.nightfall.client;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.texture.OverlayTexture;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;

/// Mesh Crimson Moon yang di-bake SEKALI ke koordinat lokal sendi Tool_R (semua titik hanya terikat ke satu sendi),
/// lalu digambar tanpa skinning per frame -> jauh lebih ringan dari SkinnedMesh/compute shader.
public final class BakedScythe {
	private BakedScythe() {}
	
	private static float[] pos;
	private static float[] nrm;
	private static float[] uv;
	private static int count;
	private static boolean failed;
	/// Culling sisi belakang di CPU. DIMATIKAN: versi lama (yang mulus) tidak punya ini; semua segitiga dikirim dan GPU (RenderType entityCutout)
	/// yang membuang sisi belakang berdasarkan urutan titik, jadi tidak mungkin salah buang. Nyalakan hanya bila perlu hemat vertex di HP lemah.
	private static final boolean CPU_CULLING = false;
	/// Normal pencahayaan: false = sama persis seperti versi lama (pakai matriks normal PoseStack apa adanya); true = diputar mengikuti pose (lebih akurat)
	private static final boolean ROTATED_LIGHTING_NORMALS = false;
	/// Matriks normal sementara (dipakai hanya di render thread, tanpa alokasi per frame)
	private static final Matrix3f NORMAL_SCRATCH = new Matrix3f();
	
	public static boolean ensure() {
		if (pos != null) {
			return true;
		}
		
		if (failed) {
			return false;
		}
		
		try {
			SkinnedMesh mesh = NightfallMeshes.CRIMSON_MOON.get();
			HumanoidArmature armature = Armatures.BIPED.get();
			
			if (mesh == null || armature == null) {
				return false;
			}
			
			OpenMatrix4f[] identity = new OpenMatrix4f[armature.getJointNumber()];
			
			for (int i = 0; i < identity.length; i++) {
				identity[i] = new OpenMatrix4f();
			}
			
			OpenMatrix4f toOrigin = armature.toolR.getToOrigin();
			FloatArrayList p = new FloatArrayList();
			FloatArrayList n = new FloatArrayList();
			FloatArrayList t = new FloatArrayList();
			
			mesh.drawPosed(new PoseStack(), null, (consumer, px, py, pz, nx, ny, nz, light, r, g, b, a, u, v, overlay) -> {
				Vec4f position = new Vec4f(px, py, pz, 1.0F);
				Vec4f normal = new Vec4f(nx, ny, nz, 0.0F);
				OpenMatrix4f.transform(toOrigin, position, position);
				OpenMatrix4f.transform(toOrigin, normal, normal);
				p.add(position.x); p.add(position.y); p.add(position.z);
				n.add(normal.x); n.add(normal.y); n.add(normal.z);
				t.add(u); t.add(v);
			}, 0, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, null, identity);
			
			float[] allPos = p.toFloatArray();
			float[] allNrm = n.toFloatArray();
			float[] allUv = t.toFloatArray();
			
			// Buang segitiga kembar persis (posisi + UV sama): tidak terlihat bedanya, hanya menghemat vertex
			int triangles = allPos.length / 9;
			java.util.HashSet<String> seen = new java.util.HashSet<>();
			boolean[] keep = new boolean[triangles];
			int kept = 0;
			
			for (int tri = 0; tri < triangles; tri++) {
				String[] keys = new String[3];
				
				for (int v = 0; v < 3; v++) {
					int vi = tri * 3 + v;
					keys[v] = Math.round(allPos[vi * 3] * 10000.0F) + "," + Math.round(allPos[vi * 3 + 1] * 10000.0F) + "," + Math.round(allPos[vi * 3 + 2] * 10000.0F)
						+ "," + Math.round(allUv[vi * 2] * 10000.0F) + "," + Math.round(allUv[vi * 2 + 1] * 10000.0F);
				}
				
				java.util.Arrays.sort(keys);
				
				if (seen.add(keys[0] + "|" + keys[1] + "|" + keys[2])) {
					keep[tri] = true;
					kept++;
				}
			}
			
			pos = new float[kept * 9];
			nrm = new float[kept * 9];
			uv = new float[kept * 6];
			int out = 0;
			
			for (int tri = 0; tri < triangles; tri++) {
				if (!keep[tri]) {
					continue;
				}
				
				System.arraycopy(allPos, tri * 9, pos, out * 9, 9);
				System.arraycopy(allNrm, tri * 9, nrm, out * 9, 9);
				System.arraycopy(allUv, tri * 6, uv, out * 6, 6);
				out++;
			}
			
			// Urutkan segitiga dari yang terbesar: saat FPS rendah cukup menggambar N pertama (segitiga kecil/ujung duri dibuang dulu, bentuk utama tetap utuh).
			// Urutan gambar tidak memengaruhi hasil (depth test), jadi tampilan penuh tetap identik.
			final float[] area = new float[kept];
			final Integer[] order = new Integer[kept];
			
			for (int i = 0; i < kept; i++) {
				final int o = i * 9;
				final float ax = pos[o + 3] - pos[o], ay = pos[o + 4] - pos[o + 1], az = pos[o + 5] - pos[o + 2];
				final float bx = pos[o + 6] - pos[o], by = pos[o + 7] - pos[o + 1], bz = pos[o + 8] - pos[o + 2];
				final float cx = ay * bz - az * by, cy = az * bx - ax * bz, cz = ax * by - ay * bx;
				area[i] = cx * cx + cy * cy + cz * cz;
				order[i] = i;
			}
			
			java.util.Arrays.sort(order, (x, y) -> Float.compare(area[y], area[x]));
			float[] sortedPos = new float[kept * 9];
			float[] sortedNrm = new float[kept * 9];
			float[] sortedUv = new float[kept * 6];
			
			for (int i = 0; i < kept; i++) {
				final int from = order[i];
				System.arraycopy(pos, from * 9, sortedPos, i * 9, 9);
				System.arraycopy(nrm, from * 9, sortedNrm, i * 9, 9);
				System.arraycopy(uv, from * 6, sortedUv, i * 6, 6);
			}
			
			pos = sortedPos;
			nrm = sortedNrm;
			uv = sortedUv;
			count = kept * 3;
			
			return count > 0;
		} catch (Exception e) {
			failed = true;
			EpicFightMod.LOGGER.error("Gagal bake mesh Crimson Moon", e);
			return false;
		}
	}
	
	/// Gambar mesh (triangle mode, pakai EpicFightRenderTypes.getTriangulated).
	/// Tanpa alokasi per vertex: transformasi posisi/normal dihitung langsung (dulu addVertex(Matrix4f) + setNormal(Pose) membuat
	/// 2 objek Vector3f per vertex = puluhan ribu sampah per frame, penyebab stutter di HP RAM kecil). Hasil gambar identik.
	public static void draw(PoseStack poseStack, VertexConsumer consumer, int light, float r, float g, float b, float a) {
		draw(poseStack, consumer, light, r, g, b, a, 1.0F);
	}
	
	/// detail 0..1 = porsi segitiga yang digambar (terbesar dulu). 1.0 = seluruh mesh.
	public static void draw(PoseStack poseStack, VertexConsumer consumer, int light, float r, float g, float b, float a, float detail) {
		if (!ensure()) {
			return;
		}
		
		PoseStack.Pose last = poseStack.last();
		Matrix4f m = last.pose();
		
		// Catatan: last.normal() tidak ikut berputar (MathUtils.mulStack tidak mengisi buffer 3x3). Untuk culling CPU / normal akurat, hitung dari matriks pose.
		Matrix3f nm;
		
		if (CPU_CULLING || ROTATED_LIGHTING_NORMALS) {
			nm = NORMAL_SCRATCH.set(m);
			
			if (Math.abs(nm.determinant()) < 1.0E-12F) {
				return;
			}
			
			nm.invert().transpose();
		} else {
			nm = last.normal();
		}
		
		final float m00 = m.m00(), m01 = m.m01(), m02 = m.m02();
		final float m10 = m.m10(), m11 = m.m11(), m12 = m.m12();
		final float m20 = m.m20(), m21 = m.m21(), m22 = m.m22();
		final float m30 = m.m30(), m31 = m.m31(), m32 = m.m32();
		final float n00 = nm.m00(), n01 = nm.m01(), n02 = nm.m02();
		final float n10 = nm.m10(), n11 = nm.m11(), n12 = nm.m12();
		final float n20 = nm.m20(), n21 = nm.m21(), n22 = nm.m22();
		
		// Normal sudah satuan bila matriks normal ortonormal (kasus umum) -> lewati normalisasi seperti yang dilakukan vanilla
		final boolean unitNormals = near1(n00 * n00 + n01 * n01 + n02 * n02) && near1(n10 * n10 + n11 * n11 + n12 * n12) && near1(n20 * n20 + n21 * n21 + n22 * n22);
		final float[] pos = BakedScythe.pos, nrm = BakedScythe.nrm, uv = BakedScythe.uv;
		final int total = count;
		
		final int triangles = detail >= 1.0F ? total / 3 : Math.max(1, Math.min(total / 3, Math.round(total / 3 * detail)));
		
		for (int t = 0; t < triangles; t++) {
			final int first = t * 3;
			
			if (CPU_CULLING) {
				// Buang sisi yang membelakangi kamera (kamera = titik asal ruang pandang). Normal mesh sudah dicek 100% searah winding.
				{
					final int k0 = first * 3;
					final float x = pos[k0], y = pos[k0 + 1], z = pos[k0 + 2];
					final float vx = m00 * x + m10 * y + m20 * z + m30;
					final float vy = m01 * x + m11 * y + m21 * z + m31;
					final float vz = m02 * x + m12 * y + m22 * z + m32;
					final float nx0 = nrm[k0], ny0 = nrm[k0 + 1], nz0 = nrm[k0 + 2];
					final float fx = n00 * nx0 + n10 * ny0 + n20 * nz0;
					final float fy = n01 * nx0 + n11 * ny0 + n21 * nz0;
					final float fz = n02 * nx0 + n12 * ny0 + n22 * nz0;
				
					if (fx * vx + fy * vy + fz * vz >= 0.0F) {
						continue;
					}
				}
			
			}
			
			for (int i = first; i < first + 3; i++) {
				final int k = i * 3;
				final float x = pos[k], y = pos[k + 1], z = pos[k + 2];
				float nx = nrm[k], ny = nrm[k + 1], nz = nrm[k + 2];
				float tnx = n00 * nx + n10 * ny + n20 * nz;
				float tny = n01 * nx + n11 * ny + n21 * nz;
				float tnz = n02 * nx + n12 * ny + n22 * nz;
				
				if (!unitNormals) {
					float len = (float)Math.sqrt(tnx * tnx + tny * tny + tnz * tnz);
					
					if (len > 1.0E-6F) {
						float inv = 1.0F / len;
						tnx *= inv;
						tny *= inv;
						tnz *= inv;
					}
				}
				
				consumer.addVertex(m00 * x + m10 * y + m20 * z + m30, m01 * x + m11 * y + m21 * z + m31, m02 * x + m12 * y + m22 * z + m32)
					.setColor(r, g, b, a)
					.setUv(uv[i * 2], uv[i * 2 + 1])
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(tnx, tny, tnz);
			}
		}
	}
	
	private static boolean near1(float lengthSqr) {
		return lengthSqr > 0.995F && lengthSqr < 1.005F;
	}
	
	/// Jarak (blok) dari kamera ke titik asal pose saat ini. Ruang pose = relatif kamera, jadi cukup panjang translasi.
	public static float distanceSqr(PoseStack poseStack) {
		Matrix4f m = poseStack.last().pose();
		
		return m.m30() * m.m30() + m.m31() * m.m31() + m.m32() * m.m32();
	}
	
	/// Sprite 2D datar (ikon inventori / di tanah / item frame). Pakai RenderType mode QUADS.
	public static void drawSprite(PoseStack poseStack, VertexConsumer consumer, int light) {
		PoseStack.Pose last = poseStack.last();
		Matrix4f matrix = last.pose();
		float[][] v = { {0, 0, 0, 1}, {1, 0, 1, 1}, {1, 1, 1, 0}, {0, 1, 0, 0} };
		
		for (float[] c : v) {
			consumer.addVertex(matrix, c[0], c[1], 0.5F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(c[2], c[3])
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last, 0.0F, 0.0F, 1.0F);
		}
	}
}
