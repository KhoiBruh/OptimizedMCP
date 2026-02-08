package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;

public class ItemFood extends Item {
    public final int itemUseDuration;
    private final int healAmount;
    private final float saturationModifier;
    private final boolean isWolfsFavoriteMeat;
    private boolean alwaysEdible;
    private int potionId;
    private int potionDuration;
    private int potionAmplifier;
    private float potionEffectProbability;

    public ItemFood(int amount, float saturation, boolean isWolfFood) {
        itemUseDuration = 32;
        healAmount = amount;
        isWolfsFavoriteMeat = isWolfFood;
        saturationModifier = saturation;
        setCreativeTab(CreativeTabs.tabFood);
    }

    public ItemFood(int amount, boolean isWolfFood) {
        this(amount, 0.6F, isWolfFood);
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        --stack.stackSize;
        playerIn.getFoodStats().addStats(this, stack);
        worldIn.playSoundAtEntity(playerIn, "random.burp", 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
        onFoodEaten(stack, worldIn, playerIn);
        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
        return stack;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote && potionId > 0 && worldIn.rand.nextFloat() < potionEffectProbability) {
            player.addPotionEffect(new PotionEffect(potionId, potionDuration * 20, potionAmplifier));
        }
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    public Action getItemUseAction(ItemStack stack) {
        return Action.EAT;
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
        if (playerIn.canEat(alwaysEdible)) {
            playerIn.setItemInUse(itemStackIn, getMaxItemUseDuration(itemStackIn));
        }

        return itemStackIn;
    }

    public int getHealAmount(ItemStack stack) {
        return healAmount;
    }

    public float getSaturationModifier(ItemStack stack) {
        return saturationModifier;
    }

    public boolean isWolfsFavoriteMeat() {
        return isWolfsFavoriteMeat;
    }

    public ItemFood setPotionEffect(int id, int duration, int amplifier, float probability) {
        potionId = id;
        potionDuration = duration;
        potionAmplifier = amplifier;
        potionEffectProbability = probability;
        return this;
    }

    public ItemFood setAlwaysEdible() {
        alwaysEdible = true;
        return this;
    }
}
