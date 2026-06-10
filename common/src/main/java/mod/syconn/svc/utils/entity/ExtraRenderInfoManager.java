package mod.syconn.svc.utils.entity;

import mod.syconn.svc.utils.entity.extra.PlayerExtraRenderInfo;
import mod.syconn.svc.utils.interfaces.IExtraRenderInfo;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ExtraRenderInfoManager {

    private static final Map<String, Supplier<IExtraRenderInfo>> EXTRAS = new HashMap<>();

    public static void register(EntityType<?> type, Supplier<IExtraRenderInfo> extra) {
        EXTRAS.put(EntityType.getKey(type).toString(), extra);
    }

    @NotNull
    public static IExtraRenderInfo get(String entityTypeId) {
        return EXTRAS.getOrDefault(entityTypeId, IExtraRenderInfo.DefaultRenderInfo::new).get();
    }

    public static void register() {
        register(EntityType.PLAYER, PlayerExtraRenderInfo::new);
    }
}
