package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;

public class S3EPacketTeams implements Packet<INetHandlerPlayClient> {
    private String name = "";
    private String displayName = "";
    private String prefix = "";
    private String suffix = "";
    private String nameTagVisibility;
    private int color;
    private final Collection<String> players;
    private int action;
    private int friendlyFlags;

    public S3EPacketTeams() {
        nameTagVisibility = Team.Visible.ALWAYS.internalName;
        color = -1;
        players = new ArrayList<>();
    }

    public S3EPacketTeams(ScorePlayerTeam teamIn, int actionIn) {
        nameTagVisibility = Team.Visible.ALWAYS.internalName;
        color = -1;
        players = new ArrayList<>();
        name = teamIn.getRegisteredName();
        action = actionIn;

        if (actionIn == 0 || actionIn == 2) {
            displayName = teamIn.getTeamName();
            prefix = teamIn.getColorPrefix();
            suffix = teamIn.getColorSuffix();
            friendlyFlags = teamIn.func_98299_i();
            nameTagVisibility = teamIn.getNameTagVisibility().internalName;
            color = teamIn.getChatFormat().getColorIndex();
        }

        if (actionIn == 0) {
            players.addAll(teamIn.getMembershipCollection());
        }
    }

    public S3EPacketTeams(ScorePlayerTeam teamIn, Collection<String> playersIn, int actionIn) {
        nameTagVisibility = Team.Visible.ALWAYS.internalName;
        color = -1;
        players = new ArrayList<>();

        if (actionIn != 3 && actionIn != 4) {
            throw new IllegalArgumentException("Method must be join or leave for player constructor");
        } else if (playersIn != null && !playersIn.isEmpty()) {
            action = actionIn;
            name = teamIn.getRegisteredName();
            players.addAll(playersIn);
        } else {
            throw new IllegalArgumentException("Players cannot be null/empty");
        }
    }

    public void readPacketData(PacketBuffer buf) {
        name = buf.readStringFromBuffer(16);
        action = buf.readByte();

        if (action == 0 || action == 2) {
            displayName = buf.readStringFromBuffer(32);
            prefix = buf.readStringFromBuffer(16);
            suffix = buf.readStringFromBuffer(16);
            friendlyFlags = buf.readByte();
            nameTagVisibility = buf.readStringFromBuffer(32);
            color = buf.readByte();
        }

        if (action == 0 || action == 3 || action == 4) {
            int i = buf.readVarIntFromBuffer();

            for (int j = 0; j < i; ++j) {
                players.add(buf.readStringFromBuffer(40));
            }
        }
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeByte(action);

        if (action == 0 || action == 2) {
            buf.writeString(displayName);
            buf.writeString(prefix);
            buf.writeString(suffix);
            buf.writeByte(friendlyFlags);
            buf.writeString(nameTagVisibility);
            buf.writeByte(color);
        }

        if (action == 0 || action == 3 || action == 4) {
            buf.writeVarIntToBuffer(players.size());

            for (String s : players) {
                buf.writeString(s);
            }
        }
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleTeams(this);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public Collection<String> getPlayers() {
        return players;
    }

    public int getAction() {
        return action;
    }

    public int getFriendlyFlags() {
        return friendlyFlags;
    }

    public int getColor() {
        return color;
    }

    public String getNameTagVisibility() {
        return nameTagVisibility;
    }
}
