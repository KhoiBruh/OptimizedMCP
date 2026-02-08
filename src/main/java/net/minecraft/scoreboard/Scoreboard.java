package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatFormat;

import java.util.*;

public class Scoreboard {
    private static String[] field_178823_g = null;
    private final Map<String, ScoreObjective> scoreObjectives = new HashMap<>();
    private final Map<IScoreObjectiveCriteria, List<ScoreObjective>> scoreObjectiveCriterias = new HashMap<>();
    private final Map<String, Map<ScoreObjective, Score>> entitiesScoreObjectives = new HashMap<>();
    private final ScoreObjective[] objectiveDisplaySlots = new ScoreObjective[19];
    private final Map<String, ScorePlayerTeam> teams = new HashMap<>();
    private final Map<String, ScorePlayerTeam> teamMemberships = new HashMap<>();

    public static String getObjectiveDisplaySlot(int slot) {
        return switch (slot) {
            case 0 -> "list";
            case 1 -> "sidebar";
            case 2 -> "belowName";
            default -> {
                if (slot >= 3 && slot <= 18) {
                    ChatFormat chatFormat = ChatFormat.func_175744_a(slot - 3);

                    if (chatFormat != null && chatFormat != ChatFormat.RESET)
                        yield "sidebar.team." + chatFormat.getFriendlyName();
                }

                yield null;
            }
        };
    }

    public static int getObjectiveDisplaySlotNumber(String objective) {
        return switch (objective) {
            case "list" -> 0;
            case "sidebar" -> 1;
            case "belowName" -> 2;
            default -> {
                if (objective.startsWith("sidebar.team.")) {
                    String s = objective.substring("sidebar.team.".length());
                    ChatFormat chatFormat = ChatFormat.getValueByName(s);

                    if (chatFormat != null && chatFormat.getColorIndex() >= 0) yield chatFormat.getColorIndex() + 3;
                }

                yield  -1;
            }
        };
    }

    public static String[] getDisplaySlotStrings() {
        if (field_178823_g == null) {
            field_178823_g = new String[19];

            for (int i = 0; i < 19; ++i) {
                field_178823_g[i] = getObjectiveDisplaySlot(i);
            }
        }

        return field_178823_g;
    }

    public ScoreObjective getObjective(String name) {
        return scoreObjectives.get(name);
    }

    public ScoreObjective addScoreObjective(String name, IScoreObjectiveCriteria criteria) {
        if (name.length() <= 16) {
            ScoreObjective scoreobjective = getObjective(name);

            if (scoreobjective == null) {
                scoreobjective = new ScoreObjective(this, name, criteria);
                List<ScoreObjective> list = scoreObjectiveCriterias.computeIfAbsent(criteria, k -> new ArrayList<>());

                list.add(scoreobjective);
                scoreObjectives.put(name, scoreobjective);
                onScoreObjectiveAdded(scoreobjective);
                return scoreobjective;
            } else throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
        } else throw new IllegalArgumentException("The objective name '" + name + "' is too long!");
    }

    public Collection<ScoreObjective> getObjectivesFromCriteria(IScoreObjectiveCriteria criteria) {
        Collection<ScoreObjective> collection = scoreObjectiveCriterias.get(criteria);
        return collection == null ? new ArrayList<>() : Lists.newArrayList(collection);
    }

    public boolean entityHasObjective(String name, ScoreObjective p_178819_2_) {
        Map<ScoreObjective, Score> map = entitiesScoreObjectives.get(name);

        if (map != null) {
            Score score = map.get(p_178819_2_);
            return score != null;
        } else return false;
    }

    public Score getValueFromObjective(String name, ScoreObjective objective) {
        if (name.length() <= 40) {
            Map<ScoreObjective, Score> map = entitiesScoreObjectives.computeIfAbsent(name, k -> new HashMap<>());

            return map.computeIfAbsent(objective, o -> new Score(this, o, name));
        } else throw new IllegalArgumentException("The player name '" + name + "' is too long!");
    }

    public Collection<Score> getSortedScores(ScoreObjective objective) {
        List<Score> list = new ArrayList<>();

        for (Map<ScoreObjective, Score> map : entitiesScoreObjectives.values()) {
            Score score = map.get(objective);

            if (score != null) {
                list.add(score);
            }
        }

        list.sort(Score.scoreComparator);
        return list;
    }

    public Collection<ScoreObjective> getScoreObjectives() {
        return scoreObjectives.values();
    }

    public Collection<String> getObjectiveNames() {
        return entitiesScoreObjectives.keySet();
    }

    public void removeObjectiveFromEntity(String name, ScoreObjective objective) {
        if (objective == null) {
            Map<ScoreObjective, Score> map = entitiesScoreObjectives.remove(name);

            if (map != null) {
                func_96516_a(name);
            }
        } else {
            Map<ScoreObjective, Score> map2 = entitiesScoreObjectives.get(name);

            if (map2 != null) {
                Score score = map2.remove(objective);

                if (map2.isEmpty()) {
                    Map<ScoreObjective, Score> map1 = entitiesScoreObjectives.remove(name);

                    if (map1 != null) {
                        func_96516_a(name);
                    }
                } else if (score != null) {
                    func_178820_a(name, objective);
                }
            }
        }
    }

    public Collection<Score> getScores() {
        Collection<Map<ScoreObjective, Score>> collection = entitiesScoreObjectives.values();
        List<Score> list = new ArrayList<>();

        for (Map<ScoreObjective, Score> map : collection) {
            list.addAll(map.values());
        }

        return list;
    }

    public Map<ScoreObjective, Score> getObjectivesForEntity(String name) {
        Map<ScoreObjective, Score> map = entitiesScoreObjectives.get(name);

        if (map == null) map = new HashMap<>();

        return map;
    }

    public void removeObjective(ScoreObjective p_96519_1_) {
        scoreObjectives.remove(p_96519_1_.getName());

        for (int i = 0; i < 19; ++i) {
            if (getObjectiveInDisplaySlot(i) == p_96519_1_) setObjectiveInDisplaySlot(i, null);
        }

        List<ScoreObjective> list = scoreObjectiveCriterias.get(p_96519_1_.getCriteria());

        if (list != null) list.remove(p_96519_1_);

        for (Map<ScoreObjective, Score> map : entitiesScoreObjectives.values()) {
            map.remove(p_96519_1_);
        }

        onScoreObjectiveRemoved(p_96519_1_);
    }

    public void setObjectiveInDisplaySlot(int p_96530_1_, ScoreObjective p_96530_2_) {
        objectiveDisplaySlots[p_96530_1_] = p_96530_2_;
    }

    public ScoreObjective getObjectiveInDisplaySlot(int p_96539_1_) {
        return objectiveDisplaySlots[p_96539_1_];
    }

    public ScorePlayerTeam getTeam(String p_96508_1_) {
        return teams.get(p_96508_1_);
    }

    public ScorePlayerTeam createTeam(String name) {
        if (name.length() <= 16) {
            ScorePlayerTeam scoreplayerteam = getTeam(name);

            if (scoreplayerteam == null) {
                scoreplayerteam = new ScorePlayerTeam(this, name);
                teams.put(name, scoreplayerteam);
                broadcastTeamCreated(scoreplayerteam);
                return scoreplayerteam;
            } else throw new IllegalArgumentException("A team with the name '" + name + "' already exists!");
        } else throw new IllegalArgumentException("The team name '" + name + "' is too long!");
    }

    public void removeTeam(ScorePlayerTeam p_96511_1_) {
        if (p_96511_1_ == null) return;
        teams.remove(p_96511_1_.getRegisteredName());

        for (String s : p_96511_1_.getMembershipCollection()) {
            teamMemberships.remove(s);
        }

        func_96513_c(p_96511_1_);
    }

    public boolean addPlayerToTeam(String player, String newTeam) {
        if (player.length() > 40) {
            throw new IllegalArgumentException("The player name '" + player + "' is too long!");
        } else if (!teams.containsKey(newTeam)) {
            return false;
        } else {
            ScorePlayerTeam scoreplayerteam = getTeam(newTeam);

            if (getPlayersTeam(player) != null) removePlayerFromTeams(player);

            teamMemberships.put(player, scoreplayerteam);
            scoreplayerteam.getMembershipCollection().add(player);
            return true;
        }
    }

    public boolean removePlayerFromTeams(String p_96524_1_) {
        ScorePlayerTeam scoreplayerteam = getPlayersTeam(p_96524_1_);

        if (scoreplayerteam != null) {
            removePlayerFromTeam(p_96524_1_, scoreplayerteam);
            return true;
        } else return false;
    }

    public void removePlayerFromTeam(String p_96512_1_, ScorePlayerTeam p_96512_2_) {
        if (getPlayersTeam(p_96512_1_) == p_96512_2_) {
            teamMemberships.remove(p_96512_1_);
            p_96512_2_.getMembershipCollection().remove(p_96512_1_);
        } else
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + p_96512_2_.getRegisteredName() + "'.");
    }

    public Collection<String> getTeamNames() {
        return teams.keySet();
    }

    public Collection<ScorePlayerTeam> getTeams() {
        return teams.values();
    }

    public ScorePlayerTeam getPlayersTeam(String p_96509_1_) {
        return teamMemberships.get(p_96509_1_);
    }

    public void onScoreObjectiveAdded(ScoreObjective scoreObjectiveIn) {
    }

    public void onObjectiveDisplayNameChanged(ScoreObjective p_96532_1_) {
    }

    public void onScoreObjectiveRemoved(ScoreObjective p_96533_1_) {
    }

    public void func_96536_a(Score p_96536_1_) {
    }

    public void func_96516_a(String p_96516_1_) {
    }

    public void func_178820_a(String p_178820_1_, ScoreObjective p_178820_2_) {
    }

    public void broadcastTeamCreated(ScorePlayerTeam playerTeam) {
    }

    public void sendTeamUpdate(ScorePlayerTeam playerTeam) {
    }

    public void func_96513_c(ScorePlayerTeam playerTeam) {
    }

    public void func_181140_a(Entity p_181140_1_) {
        if (p_181140_1_ != null && !(p_181140_1_ instanceof EntityPlayer) && !p_181140_1_.isEntityAlive()) {
            String s = p_181140_1_.getUniqueID().toString();
            removeObjectiveFromEntity(s, null);
            removePlayerFromTeams(s);
        }
    }
}
