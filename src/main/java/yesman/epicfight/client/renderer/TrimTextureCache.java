package yesman.epicfight.client.renderer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.main.EpicFightMod;

/// Menyalin sprite trim armor (sudah berwarna material) dari atlas `armor_trims` ke tekstur mandiri.
///
/// Mesh armor Epic Fight memakai UV yang tepat menyentuh tepi tekstur (u = 0.0 / v = 1.0). Di dalam atlas, tepi itu berbatasan
/// langsung dengan sprite trim lain (tanpa padding), sehingga sampling nearest di tepi mengambil piksel sprite TETANGGA
/// -> titik/garis warna acak (kuning, cyan, oranye, putih) di sekujur armor. Tekstur mandiri tidak punya tetangga, jadi
/// tepinya aman. Hasil di-cache per SpriteContents dan otomatis ikut diganti setelah resource reload.
public final class TrimTextureCache {
	private TrimTextureCache() {}
	
	private static final Map<SpriteContents, ResourceLocation> CACHE = new WeakHashMap<>();
	private static Field imageField;
	private static boolean failed;
	private static int counter;
	
	/// @return lokasi tekstur mandiri untuk sprite ini, atau null bila gagal (pemanggil jatuh balik ke atlas)
	@Nullable
	public static synchronized ResourceLocation get(TextureAtlasSprite sprite) {
		if (failed || sprite == null) {
			return null;
		}
		
		try {
			SpriteContents contents = sprite.contents();
			ResourceLocation cached = CACHE.get(contents);
			
			if (cached != null) {
				return cached;
			}
			
			NativeImage source = originalImage(contents);
			
			if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
				return null;
			}
			
			NativeImage copy = new NativeImage(source.getWidth(), source.getHeight(), false);
			copy.copyFrom(source);
			
			ResourceLocation location = EpicFightMod.identifier("dynamic/trim_" + (counter++));
			Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(copy));
			CACHE.put(contents, location);
			
			return location;
		} catch (Throwable t) {
			failed = true;
			EpicFightMod.LOGGER.error("Gagal membuat tekstur trim mandiri, pakai atlas", t);
			return null;
		}
	}
	
	/// Field gambar asli dicari lewat tipenya (NativeImage tunggal) agar tidak bergantung pada nama mapping.
	@Nullable
	private static NativeImage originalImage(SpriteContents contents) throws ReflectiveOperationException {
		if (imageField == null) {
			for (Field field : SpriteContents.class.getDeclaredFields()) {
				if (field.getType() == NativeImage.class) {
					field.setAccessible(true);
					imageField = field;
					break;
				}
			}
			
			if (imageField == null) {
				throw new NoSuchFieldException("SpriteContents NativeImage");
			}
		}
		
		return (NativeImage)imageField.get(contents);
	}
}
