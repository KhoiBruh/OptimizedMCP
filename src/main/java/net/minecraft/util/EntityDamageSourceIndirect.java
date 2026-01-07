package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class EntityDamageSourceIndirect extends EntityDamageSource {
    private final Entity indirectEntity;

    public EntityDamageSourceIndirect(String damageTypeIn, Entity source, Entity indirectEntityIn) {
        super(damageTypeIn, source);
        indirectEntity = indirectEntityIn;
    }

    public Entity getSourceOfDamage() {
        return damageSourceEntity;
    }

    public Entity getEntity() {
        return indirectEntity;
    }

    public IChatComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
        IChatComponent ichatcomponent = indirectEntity == null ? damageSourceEntity.getDisplayName() : indirectEntity.getDisplayName();
        ItemStack itemstack = indirectEntity instanceof EntityLivingBase ? ((EntityLivingBase) indirectEntity).getHeldItem() : null;
        String s = "death.attack." + damageType;
        String s1 = s + ".item";
        return itemstack != null && itemstack.hasDisplayName() && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, entityLivingBaseIn.getDisplayName(), ichatcomponent, itemstack.getChatComponent()) : new ChatComponentTranslation(s, entityLivingBaseIn.getDisplayName(), ichatcomponent);
    }
}
