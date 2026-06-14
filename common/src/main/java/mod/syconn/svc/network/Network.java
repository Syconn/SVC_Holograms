package mod.syconn.svc.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import mod.syconn.svc.network.packets.client.MessagePlayerPacket;
import mod.syconn.svc.network.packets.client.RenderTargetPacket;
import mod.syconn.svc.network.packets.client.RequestedHologramPacket;
import mod.syconn.svc.network.packets.client.UpdateProjectorCachePacket;
import mod.syconn.svc.network.packets.server.HoloCallPacket;
import mod.syconn.svc.network.packets.server.RequestHologramPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class Network { // TODO LET HELL BREAK LOSE

    public static void init() {
        // C2S Packets
        NetworkManager.registerReceiver(NetworkManager.c2s(), RequestedHologramPacket.TYPE, RequestedHologramPacket.STREAM_CODEC, RequestedHologramPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.c2s(), MessagePlayerPacket.TYPE, MessagePlayerPacket.STREAM_CODEC, MessagePlayerPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.c2s(), RenderTargetPacket.TYPE, RenderTargetPacket.STREAM_CODEC, RenderTargetPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.c2s(), UpdateProjectorCachePacket.TYPE, UpdateProjectorCachePacket.STREAM_CODEC, UpdateProjectorCachePacket::handle);

        // S2C Packets
        registerS2CPacket(RequestHologramPacket.TYPE, RequestHologramPacket.STREAM_CODEC, RequestHologramPacket::handle);
        registerS2CPacket(HoloCallPacket.TYPE, HoloCallPacket.STREAM_CODEC, HoloCallPacket::handle);
        registerS2CPacket(HoloCallPacket.TYPE, HoloCallPacket.STREAM_CODEC, HoloCallPacket::handle);
    }

    private static <T extends CustomPacketPayload> void registerS2CPacket(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, NetworkManager.NetworkReceiver<T> receiver) {
        if (Platform.getEnvironment() == Env.CLIENT) NetworkManager.registerReceiver(NetworkManager.s2c(), id, codec, receiver);
        else NetworkManager.registerS2CPayloadType(id, codec);
    }
}
