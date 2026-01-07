package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EntityDamageSource extends DamageSource {
    protected Entity damageSourceEntity;
    private boolean isThornsDamage = false;

    public EntityDamageSource(String damageTypeIn, Entity damageSourceEntityIn) {
        super(damageTypeIn);
        damageSourceEntity = damageSourceEntityIn;
    }

    public EntityDamageSource setIsThornsDamage() {
        isThornsDamage = true;
        return this;
    }

    public boolean getIsThornsDamage() {
        return isThornsDamage;
    }

    public Entity getEntity() {
        return damageSourceEntity;
    }

    public IChatComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
        ItemStack itemstack = damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase) damageSourceEntity).getHeldItem() : null;
        String s = "death.attack." + damageType;
        String s1 = s + ".item";
        return itemstack != null && itemstack.hasDisplayName() && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, entityLivingBaseIn.getDisplayName(), damageSourceEntity.getDisplayName(), itemstack.getChatComponent()) : new ChatComponentTranslation(s, entityLivingBaseIn.getDisplayName(), damageSourceEntity.getDisplayName());
    }

    public boolean isDifficultyScaled() {
        return damageSourceEntity != null && damageSourceEntity instanceof EntityLivingBase && !(damageSourceEntity instanceof EntityPlayer);
    }
}
