package mod.syconn.svc.client;

import com.mojang.authlib.GameProfile;
import mod.syconn.svc.utils.entity.HologramPlayer;
import mod.syconn.svc.utils.entity.RenderTargetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientRenderSystem {

    private static ClientRenderSystem INSTANCE;
    private static final long MAX_TARGET_AGE = 500;
    private final Map<UUID, RenderTargetInfo> targets = new HashMap<>();

    public void handleRenderPlayerPacket(RenderTargetInfo info) {
        var m = Minecraft.getInstance();
        if (m.level == null) return;
        if (!this.targets.containsKey(info.getUUID())) this.targets.put(info.getUUID(), info);
        this.targets.get(info.getUUID()).update(info, this);
    }

    public HologramPlayer getPlayer(UUID uuid) {
        var info = this.targets.get(uuid);
        if (info == null) return null;
        return (HologramPlayer) info.getFakeEntity(this);
    }

    public void tick() {
        var m = Minecraft.getInstance();
        if (m.level == null) return;
        var currentTime = System.currentTimeMillis();
        this.targets.entrySet().removeIf(entry -> (currentTime - entry.getValue().getLastUpdateTime()) > MAX_TARGET_AGE);
        this.targets.forEach((id, info) -> {
            Entity fake = info.getFakeEntity(this);
            if (fake != null) info.tickFakeEntity(fake);
        });
    }

    @Nullable
    public Entity createFakePlayer(RenderTargetInfo info) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var profile = new GameProfile(info.getUUID(), info.getName());
        var player = new HologramPlayer(level, profile);
        if (info.getExtraInfo() != null) info.getExtraInfo().setupEntityOnCreate(player);
        return player;
    }

    private static void init() {
        INSTANCE = new ClientRenderSystem();
    }

    public static ClientRenderSystem get() {
        if (INSTANCE == null) init();
        return INSTANCE;
    }
}
