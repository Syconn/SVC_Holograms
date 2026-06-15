package mod.syconn.svc.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import mod.syconn.svc.network.packets.s2c.MessagePlayerPacket;
import mod.syconn.svc.network.packets.s2c.RenderTargetPacket;
import mod.syconn.svc.network.packets.s2c.RequestedHologramPacket;
import mod.syconn.svc.network.packets.c2s.HoloCallPacket;
import mod.syconn.svc.network.packets.c2s.RenderHoloPlayerPacket;
import mod.syconn.svc.network.packets.c2s.RequestHologramPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class Network {

    public static void init() {
        // S2C Packets
        registerS2C(RequestedHologramPacket.TYPE, RequestedHologramPacket.STREAM_CODEC, RequestedHologramPacket::handle);
        registerS2C(MessagePlayerPacket.TYPE, MessagePlayerPacket.STREAM_CODEC, MessagePlayerPacket::handle);
        registerS2C(RenderTargetPacket.TYPE, RenderTargetPacket.STREAM_CODEC, RenderTargetPacket::handle);

        // C2S Packets
        registerC2S(RequestHologramPacket.TYPE, RequestHologramPacket.STREAM_CODEC, RequestHologramPacket::handle);
        registerC2S(RenderHoloPlayerPacket.TYPE, RenderHoloPlayerPacket.STREAM_CODEC, RenderHoloPlayerPacket::handle);
        registerC2S(HoloCallPacket.TYPE, HoloCallPacket.STREAM_CODEC, HoloCallPacket::handle);
    }

    private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, NetworkManager.NetworkReceiver<T> receiver) {
        NetworkManager.registerReceiver(NetworkManager.c2s(), id, codec, receiver);
    }

    private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, NetworkManager.NetworkReceiver<T> receiver) {
        if (Platform.getEnvironment() == Env.CLIENT) NetworkManager.registerReceiver(NetworkManager.s2c(), id, codec, receiver);
        else NetworkManager.registerS2CPayloadType(id, codec);
    }
}
