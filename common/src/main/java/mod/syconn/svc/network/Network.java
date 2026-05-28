package mod.syconn.svc.network;

import dev.architectury.networking.NetworkChannel;
import dev.architectury.utils.GameInstance;
import mod.syconn.svc.network.packets.client.MessagePlayerPacket;
import mod.syconn.svc.network.packets.client.RequestedHologramPacket;
import mod.syconn.svc.network.packets.server.HoloCallPacket;
import mod.syconn.svc.network.packets.server.RequestHologramPacket;
import mod.syconn.svc.utils.Constants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Objects;

public class Network {

    public static NetworkChannel CHANNEL = NetworkChannel.create(Constants.withId("network"));

    public static void init() {
        CHANNEL.register(HoloCallPacket.class, HoloCallPacket::encode, HoloCallPacket::new, HoloCallPacket::apply);
        CHANNEL.register(RequestedHologramPacket.class, RequestedHologramPacket::encode, RequestedHologramPacket::new, RequestedHologramPacket::apply);
        CHANNEL.register(RequestHologramPacket.class, RequestHologramPacket::encode, RequestHologramPacket::new, RequestHologramPacket::apply);
        CHANNEL.register(MessagePlayerPacket.class, MessagePlayerPacket::encode, MessagePlayerPacket::new, MessagePlayerPacket::apply);
    }

    public static <T> void sendToNearby(ServerPlayer player, ResourceKey<Level> dimension, Vec3 pos, int radius, T message) {
        var playerlist = Objects.requireNonNull(GameInstance.getServer()).getPlayerList().getPlayers();
        for (ServerPlayer serverPlayer : playerlist) {
            if (serverPlayer != player && serverPlayer.level().dimension() == dimension) {
                double d = pos.x - serverPlayer.getX();
                double e = pos.y - serverPlayer.getY();
                double f = pos.z - serverPlayer.getZ();
                if (d * d + e * e + f * f < radius * radius) CHANNEL.sendToPlayer(serverPlayer, message);
            }
        }
    }

    public static Collection<ServerPlayer> tracking(ServerLevel world, ChunkPos pos) {
        Objects.requireNonNull(world, "The world cannot be null");
        Objects.requireNonNull(pos, "The chunk pos cannot be null");

        return world.getChunkSource().chunkMap.getPlayers(pos, false);
    }
}
