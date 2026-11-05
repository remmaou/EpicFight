package yesman.epicfight.platform.fabric.util;

import com.mojang.blaze3d.platform.InputConstants;

/// Diimplementasikan oleh `MixinKeyMapping` pada `net.minecraft.client.KeyMapping`.
public interface KeyMappingAccess {
    InputConstants.Key epicfight$getBoundKey();

    void epicfight$addClick();
}
