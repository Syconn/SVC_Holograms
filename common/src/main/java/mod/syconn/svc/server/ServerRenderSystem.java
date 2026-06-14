package mod.syconn.svc.server;

import dev.architectury.networking.NetworkManager;
import mod.syconn.svc.network.Network;
import mod.syconn.svc.network.packets.client.RenderTargetPacket;
import mod.syconn.svc.server.savedData.HologramNetwork;
import mod.syconn.svc.utils.entity.RenderTargetInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ServerRenderSystem {

    private static ServerRenderSystem INSTANCE;

    private void sendPayload(@NotNull ServerPlayer player, @NotNull Entity target) {
        NetworkManager.sendToPlayer(player, new RenderTargetPacket(new RenderTargetInfo(target)));
    }

    private void sendPayloads(MinecraftServer server) {
        var players = server.getPlayerList().getPlayers();
        var cache = HologramNetwork.get(server.overworld()).getRenderCache();
        for (ServerPlayer player : players) {
            for (var uuid : cache) {
                if (uuid == null) continue;
                var target = server.getPlayerList().getPlayer(uuid);
                if (target == null) continue;
                sendPayload(player, target);
            }
        }
    }

    public void tick(MinecraftServer server) {
        sendPayloads(server);
    }

    private static void init() {
        INSTANCE = new ServerRenderSystem();
    }

    public static ServerRenderSystem get() {
        if (INSTANCE == null) init();
        return INSTANCE;
    }
}
