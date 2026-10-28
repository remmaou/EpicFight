package yesman.epicfight.platform.fabric.shim.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/// Fabric stand-in for the PacketDistributor methods Epic Fight calls.
public final class PacketDistributor {
    private PacketDistributor() {}

    public static void sendToServer(CustomPacketPayload message, CustomPacketPayload... others) {
        ClientPlayNetworking.send(message);

        for (CustomPacketPayload other : others) {
            ClientPlayNetworking.send(other);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message, CustomPacketPayload... others) {
        // Saat login, player.connection masih null; paket tidak bisa dikirim dan akan dikirim ulang lewat sinkronisasi login.
        if (player.connection == null) {
            return;
        }

        ServerPlayNetworking.send(player, message);

        for (CustomPacketPayload other : others) {
            ServerPlayNetworking.send(player, other);
        }
    }

    public static void sendToAllPlayers(CustomPacketPayload message, CustomPacketPayload... others) {
        net.minecraft.server.MinecraftServer server = yesman.epicfight.platform.fabric.util.ServerHolder.get();


        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sendToPlayer(player, message, others);
            }
        }
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload message, CustomPacketPayload... others) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, message, others);
        }
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload message, CustomPacketPayload... others) {
        Set<ServerPlayer> targets = new LinkedHashSet<>(PlayerLookup.tracking(entity));

        if (entity instanceof ServerPlayer self) {
            targets.add(self);
        }

        for (ServerPlayer player : targets) {
            sendToPlayer(player, message, others);
        }
    }

    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload message, CustomPacketPayload... others) {
        Collection<ServerPlayer> players = PlayerLookup.tracking(level, chunkPos);

        for (ServerPlayer player : players) {
            sendToPlayer(player, message, others);
        }
    }
}
