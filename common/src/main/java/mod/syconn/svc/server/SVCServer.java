package mod.syconn.svc.server;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import mod.syconn.svc.server.savedData.HologramNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class SVCServer {

    public static void init() {
        TickEvent.SERVER_PRE.register(SVCServer::serverTickEvent);
        PlayerEvent.PLAYER_QUIT.register(SVCServer::playerLeftServer);
        PlayerEvent.PLAYER_JOIN.register(SVCServer::playerJoinedServer);
    }

    private static void serverTickEvent(MinecraftServer server) {
        HologramNetwork.get(server.overworld()).serverTick();
        ServerRenderSystem.get().tick(server);
    }

    private static void playerLeftServer(ServerPlayer player) {
        HologramNetwork.get(player.server.overworld()).playerLeftServer(player.getUUID());
    }

    private static void playerJoinedServer(ServerPlayer player) {
        HologramNetwork.get(player.server.overworld()).playerJoinedServer();
    }
}
