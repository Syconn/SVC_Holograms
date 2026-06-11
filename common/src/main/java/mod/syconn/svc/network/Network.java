package mod.syconn.svc.network;

import dev.architectury.networking.NetworkChannel;
import mod.syconn.svc.network.packets.client.MessagePlayerPacket;
import mod.syconn.svc.network.packets.client.RenderTargetPacket;
import mod.syconn.svc.network.packets.client.RequestedHologramPacket;
import mod.syconn.svc.network.packets.client.UpdateProjectorCachePacket;
import mod.syconn.svc.network.packets.server.HoloCallPacket;
import mod.syconn.svc.network.packets.server.RenderHoloPlayerPacket;
import mod.syconn.svc.network.packets.server.RequestHologramPacket;
import mod.syconn.svc.utils.Constants;

public class Network {

    public static final NetworkChannel CHANNEL = NetworkChannel.create(Constants.withId("network"));

    public static void init() {
        CHANNEL.register(HoloCallPacket.class, HoloCallPacket::encode, HoloCallPacket::new, HoloCallPacket::apply);
        CHANNEL.register(RequestedHologramPacket.class, RequestedHologramPacket::encode, RequestedHologramPacket::new, RequestedHologramPacket::apply);
        CHANNEL.register(RequestHologramPacket.class, RequestHologramPacket::encode, RequestHologramPacket::new, RequestHologramPacket::apply);
        CHANNEL.register(MessagePlayerPacket.class, MessagePlayerPacket::encode, MessagePlayerPacket::new, MessagePlayerPacket::apply);
        CHANNEL.register(UpdateProjectorCachePacket.class, UpdateProjectorCachePacket::encode, UpdateProjectorCachePacket::new, UpdateProjectorCachePacket::apply);
        CHANNEL.register(RenderHoloPlayerPacket.class, RenderHoloPlayerPacket::encode, RenderHoloPlayerPacket::new, RenderHoloPlayerPacket::apply);
        CHANNEL.register(RenderTargetPacket.class, RenderTargetPacket::encode, RenderTargetPacket::new, RenderTargetPacket::apply);
    }
}
