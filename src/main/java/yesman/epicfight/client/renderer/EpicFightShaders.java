package yesman.epicfight.client.renderer;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightShaders {
	public static ShaderInstance positionColorNormalShader;

	@Nullable
	public static ShaderInstance getPositionColorNormalShader() {
		return positionColorNormalShader;
	}

	/// Dipanggil lewat `CoreShaderRegistrationCallback` (Fabric API), didaftarkan di `EpicFightFabricClient`.
	///
	/// Jangan memakai `new ShaderInstance(provider, "epicfight:solid_model", ...)` secara langsung: Minecraft 1.21.1
	/// membentuk path `shaders/core/epicfight:solid_model.json` di namespace `minecraft` (karakter ':' tidak valid) dan
	/// resource reload gagal. Fabric API hanya menulis ulang nama berawalan namespace untuk program yang dibuat
	/// lewat `CoreShaderRegistrationCallback`. Cara ini juga membuat vanilla menutup shader lama setiap reload
	/// (tidak ada kebocoran memori GPU).
	public static void registerCoreShaders(CoreShaderRegistrationCallback.RegistrationContext context) throws IOException {
		context.register(EpicFightMod.identifier("solid_model"), DefaultVertexFormat.POSITION_COLOR_NORMAL, reloadedShader -> {
			positionColorNormalShader = reloadedShader;
		});
	}
}
