package yesman.epicfight.platform.fabric.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import yesman.epicfight.platform.fabric.util.KeyMappingAccess;

/// Vanilla menyimpan SATU KeyMapping per tombol fisik (`KeyMapping.MAP`), sehingga mapping yang didaftarkan paling akhir
/// "mencuri" semua klik. Epic Fight mendaftarkan ATTACK / WEAPON_INNATE_SKILL (klik kiri), GUARD (klik kanan), MOVER_SKILL (spasi),
/// dll yang bentrok dengan tombol vanilla (serang/hancurkan, pakai/pasang, lompat) => klik kiri/kanan tidak berfungsi sama sekali.
/// NeoForge mengganti MAP dengan `KeyMappingLookup` yang memberi klik dan status tekan ke SEMUA mapping pada tombol itu.
/// Mixin ini meniru perilaku tersebut.
@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements KeyMappingAccess {
    @Shadow
    @Final
    private static Map<String, KeyMapping> ALL;

    @Shadow
    private InputConstants.Key key;

    @Shadow
    private int clickCount;

    @Override
    public InputConstants.Key epicfight$getBoundKey() {
        return this.key;
    }

    @Override
    public void epicfight$addClick() {
        ++this.clickCount;
    }

    @Inject(method = "click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", at = @At("HEAD"), cancellable = true)
    private static void epicfight$clickAll(InputConstants.Key key, CallbackInfo ci) {
        for (KeyMapping mapping : ALL.values()) {
            KeyMappingAccess access = (KeyMappingAccess) mapping;

            if (key.equals(access.epicfight$getBoundKey())) {
                access.epicfight$addClick();
            }
        }

        ci.cancel();
    }

    @Inject(method = "set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", at = @At("HEAD"), cancellable = true)
    private static void epicfight$setAll(InputConstants.Key key, boolean held, CallbackInfo ci) {
        for (KeyMapping mapping : ALL.values()) {
            KeyMappingAccess access = (KeyMappingAccess) mapping;

            if (key.equals(access.epicfight$getBoundKey())) {
                mapping.setDown(held);
            }
        }

        ci.cancel();
    }
}
