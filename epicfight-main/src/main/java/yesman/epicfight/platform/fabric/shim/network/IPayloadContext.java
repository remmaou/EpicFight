package yesman.epicfight.platform.fabric.shim.network;

import net.minecraft.world.entity.player.Player;

/// Fabric stand-in for NeoForge's IPayloadContext. Epic Fight's handlers only need the player.
/// Handlers already run on the game thread (Fabric networking dispatches there).
public interface IPayloadContext {
    Player player();
}
