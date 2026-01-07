package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.world.Explosion;

public class DamageSource {
    public static DamageSource inFire = (new DamageSource("inFire")).setFireDamage();
    public static DamageSource lightningBolt = new DamageSource("lightningBolt");
    public static DamageSource onFire = (new DamageSource("onFire")).setDamageBypassesArmor().setFireDamage();
    public static DamageSource lava = (new DamageSource("lava")).setFireDamage();
    public static DamageSource inWall = (new DamageSource("inWall")).setDamageBypassesArmor();
    public static DamageSource drown = (new DamageSource("drown")).setDamageBypassesArmor();
    public static DamageSource starve = (new DamageSource("starve")).setDamageBypassesArmor().setDamageIsAbsolute();
    public static DamageSource cactus = new DamageSource("cactus");
    public static DamageSource fall = (new DamageSource("fall")).setDamageBypassesArmor();
    public static DamageSource outOfWorld = (new DamageSource("outOfWorld")).setDamageBypassesArmor().setDamageAllowedInCreativeMode();
    public static DamageSource generic = (new DamageSource("generic")).setDamageBypassesArmor();
    public static DamageSource magic = (new DamageSource("magic")).setDamageBypassesArmor().setMagicDamage();
    public static DamageSource wither = (new DamageSource("wither")).setDamageBypassesArmor();
    public static DamageSource anvil = new DamageSource("anvil");
    public static DamageSource fallingBlock = new DamageSource("fallingBlock");
    public String damageType;
    private boolean isUnblockable;
    private boolean isDamageAllowedInCreativeMode;
    private boolean damageIsAbsolute;
    private float hungerDamage = 0.3F;
    private boolean fireDamage;
    private boolean projectile;
    private boolean difficultyScaled;
    private boolean magicDamage;
    private boolean explosion;

    protected DamageSource(String damageTypeIn) {
        damageType = damageTypeIn;
    }

    public static DamageSource causeMobDamage(EntityLivingBase mob) {
        return new EntityDamageSource("mob", mob);
    }

    public static DamageSource causePlayerDamage(EntityPlayer player) {
        return new EntityDamageSource("player", player);
    }

    public static DamageSource causeArrowDamage(EntityArrow arrow, Entity indirectEntityIn) {
        return (new EntityDamageSourceIndirect("arrow", arrow, indirectEntityIn)).setProjectile();
    }

    public static DamageSource causeFireballDamage(EntityFireball fireball, Entity indirectEntityIn) {
        return indirectEntityIn == null ? (new EntityDamageSourceIndirect("onFire", fireball, fireball)).setFireDamage().setProjectile() : (new EntityDamageSourceIndirect("fireball", fireball, indirectEntityIn)).setFireDamage().setProjectile();
    }

    public static DamageSource causeThrownDamage(Entity source, Entity indirectEntityIn) {
        return (new EntityDamageSourceIndirect("thrown", source, indirectEntityIn)).setProjectile();
    }

    public static DamageSource causeIndirectMagicDamage(Entity source, Entity indirectEntityIn) {
        return (new EntityDamageSourceIndirect("indirectMagic", source, indirectEntityIn)).setDamageBypassesArmor().setMagicDamage();
    }

    public static DamageSource causeThornsDamage(Entity source) {
        return (new EntityDamageSource("thorns", source)).setIsThornsDamage().setMagicDamage();
    }

    public static DamageSource setExplosionSource(Explosion explosionIn) {
        return explosionIn != null && explosionIn.getExplosivePlacedBy() != null ? (new EntityDamageSource("explosion.player", explosionIn.getExplosivePlacedBy())).setDifficultyScaled().setExplosion() : (new DamageSource("explosion")).setDifficultyScaled().setExplosion();
    }

    public boolean isProjectile() {
        return projectile;
    }

    public DamageSource setProjectile() {
        projectile = true;
        return this;
    }

    public boolean isExplosion() {
        return explosion;
    }

    public DamageSource setExplosion() {
        explosion = true;
        return this;
    }

    public boolean isUnblockable() {
        return isUnblockable;
    }

    public float getHungerDamage() {
        return hungerDamage;
    }

    public boolean canHarmInCreative() {
        return isDamageAllowedInCreativeMode;
    }

    public boolean isDamageAbsolute() {
        return damageIsAbsolute;
    }

    public Entity getSourceOfDamage() {
        return getEntity();
    }

    public Entity getEntity() {
        return null;
    }

    protected DamageSource setDamageBypassesArmor() {
        isUnblockable = true;
        hungerDamage = 0.0F;
        return this;
    }

    protected DamageSource setDamageAllowedInCreativeMode() {
        isDamageAllowedInCreativeMode = true;
        return this;
    }

    protected DamageSource setDamageIsAbsolute() {
        damageIsAbsolute = true;
        hungerDamage = 0.0F;
        return this;
    }

    protected DamageSource setFireDamage() {
        fireDamage = true;
        return this;
    }

    public IChatComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
        EntityLivingBase entitylivingbase = entityLivingBaseIn.getAttackingEntity();
        String s = "death.attack." + damageType;
        String s1 = s + ".player";
        return entitylivingbase != null && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, entityLivingBaseIn.getDisplayName(), entitylivingbase.getDisplayName()) : new ChatComponentTranslation(s, entityLivingBaseIn.getDisplayName());
    }

    public boolean isFireDamage() {
        return fireDamage;
    }

    public String getDamageType() {
        return damageType;
    }

    public DamageSource setDifficultyScaled() {
        difficultyScaled = true;
        return this;
    }

    public boolean isDifficultyScaled() {
        return difficultyScaled;
    }

    public boolean isMagicDamage() {
        return magicDamage;
    }

    public DamageSource setMagicDamage() {
        magicDamage = true;
        return this;
    }

    public boolean isCreativePlayer() {
        Entity entity = getEntity();
        return entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode;
    }
}
