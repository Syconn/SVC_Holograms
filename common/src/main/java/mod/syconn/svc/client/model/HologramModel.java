package mod.syconn.svc.client.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;

public class HologramModel extends PlayerModel<AbstractClientPlayer> {

    public HologramModel(ModelPart root, boolean slim) {
        super(root, slim);
    }
}
