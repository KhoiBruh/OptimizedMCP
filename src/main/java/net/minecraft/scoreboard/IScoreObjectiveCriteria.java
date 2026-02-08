package net.minecraft.scoreboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IScoreObjectiveCriteria {
    Map<String, IScoreObjectiveCriteria> INSTANCES = new HashMap<>();
    IScoreObjectiveCriteria DUMMY = new ScoreDummyCriteria("dummy");
    IScoreObjectiveCriteria TRIGGER = new ScoreDummyCriteria("trigger");
    IScoreObjectiveCriteria deathCount = new ScoreDummyCriteria("deathCount");
    IScoreObjectiveCriteria playerKillCount = new ScoreDummyCriteria("playerKillCount");
    IScoreObjectiveCriteria totalKillCount = new ScoreDummyCriteria("totalKillCount");
    IScoreObjectiveCriteria health = new ScoreHealthCriteria("health");
    IScoreObjectiveCriteria[] field_178792_h = new IScoreObjectiveCriteria[]{new GoalColor("teamkill.", ChatFormat.BLACK), new GoalColor("teamkill.", ChatFormat.DARK_BLUE), new GoalColor("teamkill.", ChatFormat.DARK_GREEN), new GoalColor("teamkill.", ChatFormat.DARK_AQUA), new GoalColor("teamkill.", ChatFormat.DARK_RED), new GoalColor("teamkill.", ChatFormat.DARK_PURPLE), new GoalColor("teamkill.", ChatFormat.GOLD), new GoalColor("teamkill.", ChatFormat.GRAY), new GoalColor("teamkill.", ChatFormat.DARK_GRAY), new GoalColor("teamkill.", ChatFormat.BLUE), new GoalColor("teamkill.", ChatFormat.GREEN), new GoalColor("teamkill.", ChatFormat.AQUA), new GoalColor("teamkill.", ChatFormat.RED), new GoalColor("teamkill.", ChatFormat.LIGHT_PURPLE), new GoalColor("teamkill.", ChatFormat.YELLOW), new GoalColor("teamkill.", ChatFormat.WHITE)};
    IScoreObjectiveCriteria[] field_178793_i = new IScoreObjectiveCriteria[]{new GoalColor("killedByTeam.", ChatFormat.BLACK), new GoalColor("killedByTeam.", ChatFormat.DARK_BLUE), new GoalColor("killedByTeam.", ChatFormat.DARK_GREEN), new GoalColor("killedByTeam.", ChatFormat.DARK_AQUA), new GoalColor("killedByTeam.", ChatFormat.DARK_RED), new GoalColor("killedByTeam.", ChatFormat.DARK_PURPLE), new GoalColor("killedByTeam.", ChatFormat.GOLD), new GoalColor("killedByTeam.", ChatFormat.GRAY), new GoalColor("killedByTeam.", ChatFormat.DARK_GRAY), new GoalColor("killedByTeam.", ChatFormat.BLUE), new GoalColor("killedByTeam.", ChatFormat.GREEN), new GoalColor("killedByTeam.", ChatFormat.AQUA), new GoalColor("killedByTeam.", ChatFormat.RED), new GoalColor("killedByTeam.", ChatFormat.LIGHT_PURPLE), new GoalColor("killedByTeam.", ChatFormat.YELLOW), new GoalColor("killedByTeam.", ChatFormat.WHITE)};

    String getName();

    int setScore(List<EntityPlayer> p_96635_1_);

    boolean isReadOnly();

    RenderType getRenderType();

    enum RenderType {
        INTEGER("integer"),
        HEARTS("hearts");

        private static final Map<String, RenderType> field_178801_c = new HashMap<>();

        static {
            for (RenderType iscoreobjectivecriteria$enumrendertype : values()) {
                field_178801_c.put(iscoreobjectivecriteria$enumrendertype.func_178796_a(), iscoreobjectivecriteria$enumrendertype);
            }
        }

        private final String field_178798_d;

        RenderType(String p_i45548_3_) {
            field_178798_d = p_i45548_3_;
        }

        public static RenderType func_178795_a(String p_178795_0_) {
            RenderType iscoreobjectivecriteria$enumrendertype = field_178801_c.get(p_178795_0_);
            return iscoreobjectivecriteria$enumrendertype == null ? INTEGER : iscoreobjectivecriteria$enumrendertype;
        }

        public String func_178796_a() {
            return field_178798_d;
        }
    }
}
