package mod.syconn.svc.server;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.client.render.debug.HoloProjectorDebugRenderer;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.server.savedData.HologramNetwork;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SVCServer {

    public static void init() {
        TickEvent.SERVER_PRE.register(SVCServer::serverTickEvent);
        PlayerEvent.PLAYER_QUIT.register(SVCServer::playerLeftServer);
        PlayerEvent.PLAYER_JOIN.register(SVCServer::playerJoinedServer);
        PlayerEvent.CHANGE_DIMENSION.register(SVCServer::playerChangedDimension);
    }

    private static void serverTickEvent(MinecraftServer server) {
        HologramNetwork.get(server.overworld()).serverTick();
        ServerRenderSystem.get().tick(server);
    }

    private static void playerLeftServer(ServerPlayer player) {
        HologramNetwork.get(player.server.overworld()).playerLeftServer(player.getUUID());
        NetworkManager.sendToPlayer(player, HoloProjectorDebugRenderer.playerLeftServer());
    }

    private static void playerJoinedServer(ServerPlayer player) {
        HologramNetwork.get(player.server.overworld()).playerJoinedServer();
        NetworkManager.sendToPlayer(player, HoloProjectorDebugRenderer.playerJoinedServer(player));
    }

    private static void playerChangedDimension(ServerPlayer player, ResourceKey<Level> oldLevel, ResourceKey<Level> newLevel) {
        NetworkManager.sendToPlayer(player, HoloProjectorDebugRenderer.playerChangedDimension(player));
    }
}
