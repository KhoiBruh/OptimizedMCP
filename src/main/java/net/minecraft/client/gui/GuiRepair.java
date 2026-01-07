package net.minecraft.client.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class GuiRepair extends GuiContainer implements ICrafting {
    private static final ResourceLocation anvilResource = new ResourceLocation("textures/gui/container/anvil.png");
    private final ContainerRepair anvil;
    private GuiTextField nameField;
    private final InventoryPlayer playerInventory;

    public GuiRepair(InventoryPlayer inventoryIn, World worldIn) {
        super(new ContainerRepair(inventoryIn, worldIn, Minecraft.getMinecraft().thePlayer));
        playerInventory = inventoryIn;
        anvil = (ContainerRepair) inventorySlots;
    }

    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;
        nameField = new GuiTextField(0, fontRendererObj, i + 62, j + 24, 103, 12);
        nameField.setTextColor(-1);
        nameField.setDisabledTextColour(-1);
        nameField.setEnableBackgroundDrawing(false);
        nameField.setMaxStringLength(30);
        inventorySlots.removeCraftingFromCrafters(this);
        inventorySlots.onCraftGuiOpened(this);
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        inventorySlots.removeCraftingFromCrafters(this);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        fontRendererObj.drawString(I18n.format("container.repair"), 60, 6, 4210752);

        if (anvil.maximumCost > 0) {
            int i = 8453920;
            boolean flag = true;
            String s = I18n.format("container.repair.cost", anvil.maximumCost);

            if (anvil.maximumCost >= 40 && !mc.thePlayer.capabilities.isCreativeMode) {
                s = I18n.format("container.repair.expensive");
                i = 16736352;
            } else if (!anvil.getSlot(2).getHasStack()) {
                flag = false;
            } else if (!anvil.getSlot(2).canTakeStack(playerInventory.player)) {
                i = 16736352;
            }

            if (flag) {
                int j = -16777216 | (i & 16579836) >> 2 | i & -16777216;
                int k = xSize - 8 - fontRendererObj.getStringWidth(s);
                int l = 67;

                if (fontRendererObj.getUnicodeFlag()) {
                    drawRect(k - 3, l - 2, xSize - 7, l + 10, -16777216);
                    drawRect(k - 2, l - 1, xSize - 8, l + 9, -12895429);
                } else {
                    fontRendererObj.drawString(s, k, l + 1, j);
                    fontRendererObj.drawString(s, k + 1, l, j);
                    fontRendererObj.drawString(s, k + 1, l + 1, j);
                }

                fontRendererObj.drawString(s, k, l, i);
            }
        }

        GlStateManager.enableLighting();
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.textboxKeyTyped(typedChar, keyCode)) {
            renameItem();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void renameItem() {
        String s = nameField.getText();
        Slot slot = anvil.getSlot(0);

        if (slot != null && slot.getHasStack() && !slot.getStack().hasDisplayName() && s.equals(slot.getStack().getDisplayName())) {
            s = "";
        }

        anvil.updateItemName(s);
        mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("MC|ItemName", (new PacketBuffer(Unpooled.buffer())).writeString(s)));
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        nameField.drawTextBox();
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(anvilResource);
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;
        drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
        drawTexturedModalRect(i + 59, j + 20, 0, ySize + (anvil.getSlot(0).getHasStack() ? 0 : 16), 110, 16);

        if ((anvil.getSlot(0).getHasStack() || anvil.getSlot(1).getHasStack()) && !anvil.getSlot(2).getHasStack()) {
            drawTexturedModalRect(i + 99, j + 45, xSize, 0, 28, 21);
        }
    }

    public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList) {
        sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
    }

    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if (slotInd == 0) {
            nameField.setText(stack == null ? "" : stack.getDisplayName());
            nameField.setEnabled(stack != null);

            if (stack != null) {
                renameItem();
            }
        }
    }

    public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue) {
    }

    public void sendAllWindowProperties(Container p_175173_1_, IInventory p_175173_2_) {
    }
}
