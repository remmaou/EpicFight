package yesman.epicfight.platform.fabric.shim.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/// Fabric implementation of the subset of NeoForge's PayloadRegistrar used by Epic Fight.
/// Payload types are registered immediately; client receivers are deferred until [#registerClientReceivers()],
/// which the client initializer calls, so that no client class is touched on a dedicated server.
public final class PayloadRegistrar {
    private static final List<Runnable> CLIENT_RECEIVER_REGISTRATIONS = new ArrayList<>();

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        PayloadTypeRegistry.playS2C().register(type, codec);
        CLIENT_RECEIVER_REGISTRATIONS.add(() -> registerClientReceiver(type, handler));
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context::player));
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, DirectionalPayloadHandler<T> handler) {
        PayloadTypeRegistry.playS2C().register(type, codec);
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.serverSide().handle(payload, context::player));
        CLIENT_RECEIVER_REGISTRATIONS.add(() -> registerClientReceiver(type, handler.clientSide()));
        return this;
    }

    private static <T extends CustomPacketPayload> void registerClientReceiver(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context::player));
    }

    /// Call from the client mod initializer.
    public static void registerClientReceivers() {
        CLIENT_RECEIVER_REGISTRATIONS.forEach(Runnable::run);
        CLIENT_RECEIVER_REGISTRATIONS.clear();
    }
}
