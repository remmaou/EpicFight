package yesman.epicfight.nightfall.client;

import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.Meshes.MeshAccessor;
import yesman.epicfight.nightfall.NightfallRegistries;

/// Mesh 3D senjata Nightfall (assets/efn/animmodels/weapon/*.json)
public final class NightfallMeshes {
	private NightfallMeshes() {}
	
	public static final MeshAccessor<SkinnedMesh> CRIMSON_MOON = MeshAccessor.create(NightfallRegistries.MODID, "weapon/crimson_moon", (jsonModelLoader) -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new));
}
