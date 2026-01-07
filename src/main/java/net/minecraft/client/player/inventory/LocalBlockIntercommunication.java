package net.minecraft.client.player.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;

public class LocalBlockIntercommunication implements IInteractionObject {
    private final String guiID;
    private final IChatComponent displayName;

    public LocalBlockIntercommunication(String guiIdIn, IChatComponent displayNameIn) {
        guiID = guiIdIn;
        displayName = displayNameIn;
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return displayName.getUnformattedText();
    }

    public boolean hasCustomName() {
        return true;
    }

    public String getGuiID() {
        return guiID;
    }

    public IChatComponent getDisplayName() {
        return displayName;
    }
}
