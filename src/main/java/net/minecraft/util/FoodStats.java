package net.minecraft.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.Difficulty;

public class FoodStats {
    private int foodLevel = 20;
    private float foodSaturationLevel = 5.0F;
    private float foodExhaustionLevel;
    private int foodTimer;
    private int prevFoodLevel = 20;

    public void addStats(int foodLevelIn, float foodSaturationModifier) {
        foodLevel = Math.min(foodLevelIn + foodLevel, 20);
        foodSaturationLevel = Math.min(foodSaturationLevel + (float) foodLevelIn * foodSaturationModifier * 2.0F, (float) foodLevel);
    }

    public void addStats(ItemFood foodItem, ItemStack p_151686_2_) {
        addStats(foodItem.getHealAmount(p_151686_2_), foodItem.getSaturationModifier(p_151686_2_));
    }

    public void onUpdate(EntityPlayer player) {
        Difficulty enumdifficulty = player.worldObj.getDifficulty();
        prevFoodLevel = foodLevel;

        if (foodExhaustionLevel > 4.0F) {
            foodExhaustionLevel -= 4.0F;

            if (foodSaturationLevel > 0.0F) {
                foodSaturationLevel = Math.max(foodSaturationLevel - 1.0F, 0.0F);
            } else if (enumdifficulty != Difficulty.PEACEFUL) {
                foodLevel = Math.max(foodLevel - 1, 0);
            }
        }

        if (player.worldObj.getGameRules().getBoolean("naturalRegeneration") && foodLevel >= 18 && player.shouldHeal()) {
            ++foodTimer;

            if (foodTimer >= 80) {
                player.heal(1.0F);
                addExhaustion(3.0F);
                foodTimer = 0;
            }
        } else if (foodLevel <= 0) {
            ++foodTimer;

            if (foodTimer >= 80) {
                if (player.getHealth() > 10.0F || enumdifficulty == Difficulty.HARD || player.getHealth() > 1.0F && enumdifficulty == Difficulty.NORMAL) {
                    player.attackEntityFrom(DamageSource.starve, 1.0F);
                }

                foodTimer = 0;
            }
        } else {
            foodTimer = 0;
        }
    }

    public void readNBT(NBTTagCompound p_75112_1_) {
        if (p_75112_1_.hasKey("foodLevel", 99)) {
            foodLevel = p_75112_1_.getInteger("foodLevel");
            foodTimer = p_75112_1_.getInteger("foodTickTimer");
            foodSaturationLevel = p_75112_1_.getFloat("foodSaturationLevel");
            foodExhaustionLevel = p_75112_1_.getFloat("foodExhaustionLevel");
        }
    }

    public void writeNBT(NBTTagCompound p_75117_1_) {
        p_75117_1_.setInteger("foodLevel", foodLevel);
        p_75117_1_.setInteger("foodTickTimer", foodTimer);
        p_75117_1_.setFloat("foodSaturationLevel", foodSaturationLevel);
        p_75117_1_.setFloat("foodExhaustionLevel", foodExhaustionLevel);
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(int foodLevelIn) {
        foodLevel = foodLevelIn;
    }

    public int getPrevFoodLevel() {
        return prevFoodLevel;
    }

    public boolean needFood() {
        return foodLevel < 20;
    }

    public void addExhaustion(float p_75113_1_) {
        foodExhaustionLevel = Math.min(foodExhaustionLevel + p_75113_1_, 40.0F);
    }

    public float getSaturationLevel() {
        return foodSaturationLevel;
    }

    public void setFoodSaturationLevel(float foodSaturationLevelIn) {
        foodSaturationLevel = foodSaturationLevelIn;
    }
}
