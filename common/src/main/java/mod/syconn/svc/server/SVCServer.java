package mod.syconn.svc.server;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import mod.syconn.svc.server.savedData.HologramNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class SVCServer {

    public static void init() {
        PlayerEvent.PLAYER_QUIT.register(SVCServer::playerLeaveServer);
        TickEvent.SERVER_PRE.register(SVCServer::serverTick);
    }

    public static void playerLeaveServer(ServerPlayer player) {
        HologramNetwork.get(player.server.overworld()).playerLeave(player);
    }

    public static void serverTick(MinecraftServer server){
        HologramNetwork.get(server.overworld()).serverTick(server.overworld());
    }
}
