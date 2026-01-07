package net.minecraft.tileentity;

import com.google.gson.JsonParseException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class TileEntitySign extends TileEntity {
    public final IChatComponent[] signText = new IChatComponent[]{new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};
    private final CommandResultStats stats = new CommandResultStats();
    public int lineBeingEdited = -1;
    private boolean isEditable = true;
    private EntityPlayer player;

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        for (int i = 0; i < 4; ++i) {
            String s = IChatComponent.Serializer.componentToJson(signText[i]);
            compound.setString("Text" + (i + 1), s);
        }

        stats.writeStatsToNBT(compound);
    }

    public void readFromNBT(NBTTagCompound compound) {
        isEditable = false;
        super.readFromNBT(compound);
        ICommandSender icommandsender = new ICommandSender() {
            public String getName() {
                return "Sign";
            }

            public IChatComponent getDisplayName() {
                return new ChatComponentText(getName());
            }

            public void addChatMessage(IChatComponent component) {
            }

            public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
                return true;
            }

            public BlockPos getPosition() {
                return pos;
            }

            public Vec3 getPositionVector() {
                return new Vec3((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
            }

            public World getEntityWorld() {
                return worldObj;
            }

            public Entity getCommandSenderEntity() {
                return null;
            }

            public boolean sendCommandFeedback() {
                return false;
            }

            public void setCommandStat(CommandResultStats.Type type, int amount) {
            }
        };

        for (int i = 0; i < 4; ++i) {
            String s = compound.getString("Text" + (i + 1));

            try {
                IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(s);

                try {
                    signText[i] = ChatComponentProcessor.processComponent(icommandsender, ichatcomponent, null);
                } catch (CommandException var7) {
                    signText[i] = ichatcomponent;
                }
            } catch (JsonParseException var8) {
                signText[i] = new ChatComponentText(s);
            }
        }

        stats.readStatsFromNBT(compound);
    }

    public Packet getDescriptionPacket() {
        IChatComponent[] aichatcomponent = new IChatComponent[4];
        System.arraycopy(signText, 0, aichatcomponent, 0, 4);
        return new S33PacketUpdateSign(worldObj, pos, aichatcomponent);
    }

    public boolean func_183000_F() {
        return true;
    }

    public boolean getIsEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditableIn) {
        isEditable = isEditableIn;

        if (!isEditableIn) {
            player = null;
        }
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void setPlayer(EntityPlayer playerIn) {
        player = playerIn;
    }

    public boolean executeCommand(final EntityPlayer playerIn) {
        ICommandSender icommandsender = new ICommandSender() {
            public String getName() {
                return playerIn.getName();
            }

            public IChatComponent getDisplayName() {
                return playerIn.getDisplayName();
            }

            public void addChatMessage(IChatComponent component) {
            }

            public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
                return permLevel <= 2;
            }

            public BlockPos getPosition() {
                return pos;
            }

            public Vec3 getPositionVector() {
                return new Vec3((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
            }

            public World getEntityWorld() {
                return playerIn.getEntityWorld();
            }

            public Entity getCommandSenderEntity() {
                return playerIn;
            }

            public boolean sendCommandFeedback() {
                return false;
            }

            public void setCommandStat(CommandResultStats.Type type, int amount) {
                stats.setCommandStatScore(this, type, amount);
            }
        };

        for (IChatComponent iChatComponent : signText) {
            ChatStyle chatstyle = iChatComponent == null ? null : iChatComponent.getChatStyle();

            if (chatstyle != null && chatstyle.getChatClickEvent() != null) {
                ClickEvent clickevent = chatstyle.getChatClickEvent();

                if (clickevent.action() == ClickEvent.Action.RUN_COMMAND) {
                    MinecraftServer.getServer().getCommandManager().executeCommand(icommandsender, clickevent.value());
                }
            }
        }

        return true;
    }

    public CommandResultStats getStats() {
        return stats;
    }
}
