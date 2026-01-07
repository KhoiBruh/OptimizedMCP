package net.minecraft.command.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class CommandBlockLogic implements ICommandSender {
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss");
    private final CommandResultStats resultStats = new CommandResultStats();
    private int successCount;
    private boolean trackOutput = true;
    private IChatComponent lastOutput = null;
    private String commandStored = "";
    private String customName = "@";

    public int getSuccessCount() {
        return successCount;
    }

    public IChatComponent getLastOutput() {
        return lastOutput;
    }

    public void setLastOutput(IChatComponent lastOutputMessage) {
        lastOutput = lastOutputMessage;
    }

    public void writeDataToNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("Command", commandStored);
        tagCompound.setInteger("SuccessCount", successCount);
        tagCompound.setString("CustomName", customName);
        tagCompound.setBoolean("TrackOutput", trackOutput);

        if (lastOutput != null && trackOutput) {
            tagCompound.setString("LastOutput", IChatComponent.Serializer.componentToJson(lastOutput));
        }

        resultStats.writeStatsToNBT(tagCompound);
    }

    public void readDataFromNBT(NBTTagCompound nbt) {
        commandStored = nbt.getString("Command");
        successCount = nbt.getInteger("SuccessCount");

        if (nbt.hasKey("CustomName", 8)) {
            customName = nbt.getString("CustomName");
        }

        if (nbt.hasKey("TrackOutput", 1)) {
            trackOutput = nbt.getBoolean("TrackOutput");
        }

        if (nbt.hasKey("LastOutput", 8) && trackOutput) {
            lastOutput = IChatComponent.Serializer.jsonToComponent(nbt.getString("LastOutput"));
        }

        resultStats.readStatsFromNBT(nbt);
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return permLevel <= 2;
    }

    public String getCommand() {
        return commandStored;
    }

    public void setCommand(String command) {
        commandStored = command;
        successCount = 0;
    }

    public void trigger(World worldIn) {
        if (worldIn.isRemote) {
            successCount = 0;
        }

        MinecraftServer minecraftserver = MinecraftServer.getServer();

        if (minecraftserver != null && minecraftserver.isAnvilFileSet() && minecraftserver.isCommandBlockEnabled()) {
            ICommandManager icommandmanager = minecraftserver.getCommandManager();

            try {
                lastOutput = null;
                successCount = icommandmanager.executeCommand(this, commandStored);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Executing command block");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Command to be executed");
                crashreportcategory.addCrashSectionCallable("Command", this::getCommand);
                crashreportcategory.addCrashSectionCallable("Name", this::getName);
                throw new ReportedException(crashreport);
            }
        } else {
            successCount = 0;
        }
    }

    public String getName() {
        return customName;
    }

    public void setName(String p_145754_1_) {
        customName = p_145754_1_;
    }

    public IChatComponent getDisplayName() {
        return new ChatComponentText(customName);
    }

    public void addChatMessage(IChatComponent component) {
        if (trackOutput && getEntityWorld() != null && !getEntityWorld().isRemote) {
            lastOutput = (new ChatComponentText("[" + timestampFormat.format(new Date()) + "] ")).appendSibling(component);
            updateCommand();
        }
    }

    public boolean sendCommandFeedback() {
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        return minecraftserver == null || !minecraftserver.isAnvilFileSet() || minecraftserver.worldServers[0].getGameRules().getBoolean("commandBlockOutput");
    }

    public void setCommandStat(CommandResultStats.Type type, int amount) {
        resultStats.setCommandStatScore(this, type, amount);
    }

    public abstract void updateCommand();

    public abstract int func_145751_f();

    public abstract void func_145757_a(ByteBuf p_145757_1_);

    public void setTrackOutput(boolean shouldTrackOutput) {
        trackOutput = shouldTrackOutput;
    }

    public boolean shouldTrackOutput() {
        return trackOutput;
    }

    public boolean tryOpenEditCommandBlock(EntityPlayer playerIn) {
        if (!playerIn.capabilities.isCreativeMode) {
            return false;
        } else {
            if (playerIn.getEntityWorld().isRemote) {
                playerIn.openEditCommandBlock(this);
            }

            return true;
        }
    }

    public CommandResultStats getCommandResultStats() {
        return resultStats;
    }
}
