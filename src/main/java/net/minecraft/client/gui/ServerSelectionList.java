package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;

import java.util.List;

public class ServerSelectionList extends GuiListExtended {
    private final GuiMultiplayer owner;
    private final List<ServerListEntryNormal> serverListInternet = Lists.newArrayList();
    private final List<ServerListEntryLanDetected> serverListLan = Lists.newArrayList();
    private final GuiListExtended.IGuiListEntry lanScanEntry = new ServerListEntryLanScan();
    private int selectedSlotIndex = -1;

    public ServerSelectionList(GuiMultiplayer ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        owner = ownerIn;
    }

    public GuiListExtended.IGuiListEntry getListEntry(int index) {
        if (index < serverListInternet.size()) {
            return serverListInternet.get(index);
        } else {
            index = index - serverListInternet.size();

            if (index == 0) {
                return lanScanEntry;
            } else {
                --index;
                return serverListLan.get(index);
            }
        }
    }

    protected int getSize() {
        return serverListInternet.size() + 1 + serverListLan.size();
    }

    public void setSelectedSlotIndex(int selectedSlotIndexIn) {
        selectedSlotIndex = selectedSlotIndexIn;
    }

    protected boolean isSelected(int slotIndex) {
        return slotIndex == selectedSlotIndex;
    }

    public int func_148193_k() {
        return selectedSlotIndex;
    }

    public void func_148195_a(ServerList p_148195_1_) {
        serverListInternet.clear();

        for (int i = 0; i < p_148195_1_.countServers(); ++i) {
            serverListInternet.add(new ServerListEntryNormal(owner, p_148195_1_.getServerData(i)));
        }
    }

    public void func_148194_a(List<LanServerDetector.LanServer> p_148194_1_) {
        serverListLan.clear();

        for (LanServerDetector.LanServer lanserverdetector$lanserver : p_148194_1_) {
            serverListLan.add(new ServerListEntryLanDetected(owner, lanserverdetector$lanserver));
        }
    }

    protected int getScrollBarX() {
        return super.getScrollBarX() + 30;
    }

    public int getListWidth() {
        return super.getListWidth() + 85;
    }
}
