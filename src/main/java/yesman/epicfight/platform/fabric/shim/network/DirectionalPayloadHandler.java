package yesman.epicfight.platform.fabric.shim.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DirectionalPayloadHandler<T extends CustomPacketPayload>(IPayloadHandler<T> clientSide, IPayloadHandler<T> serverSide) {
}
