package net.minecraft.world;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;
import java.util.TreeMap;

public class GameRules {
    private final TreeMap<String, GameRules.Value> theGameRules = new TreeMap<>();

    public GameRules() {
        addGameRule("doFireTick", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("mobGriefing", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("keepInventory", "false", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("doMobSpawning", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("doMobLoot", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("doTileDrops", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("doEntityDrops", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("commandBlockOutput", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("naturalRegeneration", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("doDaylightCycle", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("logAdminCommands", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("showDeathMessages", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("randomTickSpeed", "3", GameRules.ValueType.NUMERICAL_VALUE);
        addGameRule("sendCommandFeedback", "true", GameRules.ValueType.BOOLEAN_VALUE);
        addGameRule("reducedDebugInfo", "false", GameRules.ValueType.BOOLEAN_VALUE);
    }

    public void addGameRule(String key, String value, GameRules.ValueType type) {
        theGameRules.put(key, new GameRules.Value(value, type));
    }

    public void setOrCreateGameRule(String key, String ruleValue) {
        GameRules.Value gamerules$value = theGameRules.get(key);

        if (gamerules$value != null) {
            gamerules$value.setValue(ruleValue);
        } else {
            addGameRule(key, ruleValue, GameRules.ValueType.ANY_VALUE);
        }
    }

    public String getString(String name) {
        GameRules.Value gamerules$value = theGameRules.get(name);
        return gamerules$value != null ? gamerules$value.getString() : "";
    }

    public boolean getBoolean(String name) {
        GameRules.Value gamerules$value = theGameRules.get(name);
        return gamerules$value != null && gamerules$value.getBoolean();
    }

    public int getInt(String name) {
        GameRules.Value gamerules$value = theGameRules.get(name);
        return gamerules$value != null ? gamerules$value.getInt() : 0;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        for (String s : theGameRules.keySet()) {
            GameRules.Value gamerules$value = theGameRules.get(s);
            nbttagcompound.setString(s, gamerules$value.getString());
        }

        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        for (String s : nbt.getKeySet()) {
            String s1 = nbt.getString(s);
            setOrCreateGameRule(s, s1);
        }
    }

    public String[] getRules() {
        Set<String> set = theGameRules.keySet();
        return set.toArray(new String[0]);
    }

    public boolean hasRule(String name) {
        return theGameRules.containsKey(name);
    }

    public boolean areSameType(String key, GameRules.ValueType otherValue) {
        GameRules.Value gamerules$value = theGameRules.get(key);
        return gamerules$value != null && (gamerules$value.getType() == otherValue || otherValue == GameRules.ValueType.ANY_VALUE);
    }

    public enum ValueType {
        ANY_VALUE,
        BOOLEAN_VALUE,
        NUMERICAL_VALUE
    }

    static class Value {
        private final GameRules.ValueType type;
        private String valueString;
        private boolean valueBoolean;
        private int valueInteger;
        private double valueDouble;

        public Value(String value, GameRules.ValueType type) {
            this.type = type;
            setValue(value);
        }

        public void setValue(String value) {
            valueString = value;

            if (value != null) {
                if (value.equals("false")) {
                    valueBoolean = false;
                    return;
                }

                if (value.equals("true")) {
                    valueBoolean = true;
                    return;
                }
            }

            valueBoolean = Boolean.parseBoolean(value);
            valueInteger = valueBoolean ? 1 : 0;

            try {
                valueInteger = Integer.parseInt(value);
            } catch (NumberFormatException var4) {
            }

            try {
                valueDouble = Double.parseDouble(value);
            } catch (NumberFormatException var3) {
            }
        }

        public String getString() {
            return valueString;
        }

        public boolean getBoolean() {
            return valueBoolean;
        }

        public int getInt() {
            return valueInteger;
        }

        public GameRules.ValueType getType() {
            return type;
        }
    }
}
