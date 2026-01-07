package net.optifine.player;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.src.Config;

public class PlayerConfiguration {
    private PlayerItemModel[] playerItemModels = new PlayerItemModel[0];
    private boolean initialized = false;

    public void renderPlayerItems(ModelBiped modelBiped, AbstractClientPlayer player, float scale, float partialTicks) {
        if (initialized) {
            for (PlayerItemModel playeritemmodel : playerItemModels) {
                playeritemmodel.render(modelBiped, player, scale, partialTicks);
            }
        }
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public void addPlayerItemModel(PlayerItemModel playerItemModel) {
        playerItemModels = (PlayerItemModel[]) Config.addObjectToArray(playerItemModels, playerItemModel);
    }
}
