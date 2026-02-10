package net.minecraft.entity;

import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public abstract class EntityLivingBase extends Entity {
    private static final UUID sprintingSpeedBoostModifierUUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier sprintingSpeedBoostModifier = (new AttributeModifier(sprintingSpeedBoostModifierUUID, "Sprinting speed boost", 0.30000001192092896D, 2)).setSaved(false);
    private final CombatTracker _combatTracker = new CombatTracker(this);
    private final Map<Integer, PotionEffect> activePotionsMap = new HashMap<>();
    private final ItemStack[] previousEquipment = new ItemStack[5];
    public boolean isSwingInProgress;
    public int swingProgressInt;
    public int arrowHitTimer;
    public int hurtTime;
    public int maxHurtTime;
    public float attackedAtYaw;
    public int deathTime;
    public float prevSwingProgress;
    public float swingProgress;
    public float prevLimbSwingAmount;
    public float limbSwingAmount;
    public float limbSwing;
    public int maxHurtResistantTime = 20;
    public float prevCameraPitch;
    public float cameraPitch;
    public float randomUnused2;
    public float randomUnused1;
    public float renderYawOffset;
    public float prevRenderYawOffset;
    public float rotationYawHead;
    public float prevRotationYawHead;
    public float jumpMovementFactor = 0.02F;
    public float moveStrafing;
    public float moveForward;
    protected EntityPlayer attackingPlayer;
    protected int recentlyHit;
    protected boolean dead;
    protected int entityAge;
    protected float prevOnGroundSpeedFactor;
    protected float onGroundSpeedFactor;
    protected float movedDistance;
    protected float prevMovedDistance;
    protected float unused180;
    protected int scoreValue;
    protected float lastDamage;
    protected boolean isJumping;
    protected float randomYawVelocity;
    protected int newPosRotationIncrements;
    protected double newPosX;
    protected double newPosY;
    protected double newPosZ;
    protected double newRotationYaw;
    protected double newRotationPitch;
    private BaseAttributeMap attributeMap;
    private boolean potionsNeedUpdate = true;
    private EntityLivingBase entityLivingToAttack;
    private int revengeTimer;
    private EntityLivingBase lastAttacker;
    private int lastAttackerTime;
    private float landMovementFactor;
    private int jumpTicks;
    private float absorptionAmount;

    public EntityLivingBase(World worldIn) {
        super(worldIn);
        applyEntityAttributes();
        setHealth(getMaxHealth());
        preventEntitySpawning = true;
        randomUnused1 = (float) ((Math.random() + 1.0D) * 0.009999999776482582D);
        setPosition(posX, posY, posZ);
        randomUnused2 = (float) Math.random() * 12398.0F;
        rotationYaw = (float) (Math.random() * Math.PI * 2.0D);
        rotationYawHead = rotationYaw;
        stepHeight = 0.6F;
    }

    public void onKillCommand() {
        attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
    }

    protected void entityInit() {
        dataWatcher.addObject(7, 0);
        dataWatcher.addObject(8, (byte) 0);
        dataWatcher.addObject(9, (byte) 0);
        dataWatcher.addObject(6, 1.0F);
    }

    protected void applyEntityAttributes() {
        getAttributeMap().registerAttribute(SharedMonsterAttributes.maxHealth);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.knockbackResistance);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.movementSpeed);
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos) {
        if (!isInWater()) {
            handleWaterMovement();
        }

        if (!worldObj.isRemote && fallDistance > 3.0F && onGroundIn) {
            IBlockState iblockstate = worldObj.getBlockState(pos);
            Block block = iblockstate.getBlock();
            float f = (float) MathHelper.ceil(fallDistance - 3.0F);

            if (block.getMaterial() != Material.air) {
                double d0 = Math.min(0.2F + f / 15.0F, 10.0F);

                if (d0 > 2.5D) {
                    d0 = 2.5D;
                }

                int i = (int) (150.0D * d0);
                ((WorldServer) worldObj).spawnParticle(ParticleTypes.BLOCK_DUST, posX, posY, posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(iblockstate));
            }
        }

        super.updateFallState(y, onGroundIn, blockIn, pos);
    }

    public boolean canBreatheUnderwater() {
        return false;
    }

    public void onEntityUpdate() {
        prevSwingProgress = swingProgress;
        super.onEntityUpdate();
        worldObj.theProfiler.startSection("livingEntityBaseTick");
        boolean flag = this instanceof EntityPlayer;

        if (isEntityAlive()) {
            if (isEntityInsideOpaqueBlock()) {
                attackEntityFrom(DamageSource.inWall, 1.0F);
            } else if (flag && !worldObj.getWorldBorder().contains(getEntityBoundingBox())) {
                double d0 = worldObj.getWorldBorder().getClosestDistance(this) + worldObj.getWorldBorder().getDamageBuffer();

                if (d0 < 0.0D) {
                    attackEntityFrom(DamageSource.inWall, (float) Math.max(1, MathHelper.floor(-d0 * worldObj.getWorldBorder().getDamageAmount())));
                }
            }
        }

        if (isImmuneToFire() || worldObj.isRemote) {
            extinguish();
        }

        boolean flag1 = flag && ((EntityPlayer) this).capabilities.disableDamage;

        if (isEntityAlive()) {
            if (isInsideOfMaterial(Material.water)) {
                if (!canBreatheUnderwater() && !isPotionActive(Potion.waterBreathing.id) && !flag1) {
                    setAir(decreaseAirSupply(getAir()));

                    if (getAir() == -20) {
                        setAir(0);

                        for (int i = 0; i < 8; ++i) {
                            float f = rand.nextFloat() - rand.nextFloat();
                            float f1 = rand.nextFloat() - rand.nextFloat();
                            float f2 = rand.nextFloat() - rand.nextFloat();
                            worldObj.spawnParticle(ParticleTypes.WATER_BUBBLE, posX + (double) f, posY + (double) f1, posZ + (double) f2, motionX, motionY, motionZ);
                        }

                        attackEntityFrom(DamageSource.drown, 2.0F);
                    }
                }

                if (!worldObj.isRemote && isRiding() && ridingEntity instanceof EntityLivingBase) {
                    mountEntity(null);
                }
            } else {
                setAir(300);
            }
        }

        if (isEntityAlive() && isWet()) {
            extinguish();
        }

        prevCameraPitch = cameraPitch;

        if (hurtTime > 0) {
            --hurtTime;
        }

        if (hurtResistantTime > 0 && !(this instanceof EntityPlayerMP)) {
            --hurtResistantTime;
        }

        if (getHealth() <= 0.0F) {
            onDeathUpdate();
        }

        if (recentlyHit > 0) {
            --recentlyHit;
        } else {
            attackingPlayer = null;
        }

        if (lastAttacker != null && !lastAttacker.isEntityAlive()) {
            lastAttacker = null;
        }

        if (entityLivingToAttack != null) {
            if (!entityLivingToAttack.isEntityAlive()) {
                setRevengeTarget(null);
            } else if (ticksExisted - revengeTimer > 100) {
                setRevengeTarget(null);
            }
        }

        updatePotionEffects();
        prevMovedDistance = movedDistance;
        prevRenderYawOffset = renderYawOffset;
        prevRotationYawHead = rotationYawHead;
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        worldObj.theProfiler.endSection();
    }

    public boolean isChild() {
        return false;
    }

    protected void onDeathUpdate() {
        ++deathTime;

        if (deathTime == 20) {
            if (!worldObj.isRemote && (recentlyHit > 0 || isPlayer()) && canDropLoot() && worldObj.getGameRules().getBoolean("doMobLoot")) {
                int i = getExperiencePoints(attackingPlayer);

                while (i > 0) {
                    int j = EntityXPOrb.getXPSplit(i);
                    i -= j;
                    worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, j));
                }
            }

            setDead();

            for (int k = 0; k < 20; ++k) {
                double d2 = rand.nextGaussian() * 0.02D;
                double d0 = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                worldObj.spawnParticle(ParticleTypes.EXPLOSION_NORMAL, posX + (double) (rand.nextFloat() * width * 2.0F) - (double) width, posY + (double) (rand.nextFloat() * height), posZ + (double) (rand.nextFloat() * width * 2.0F) - (double) width, d2, d0, d1);
            }
        }
    }

    protected boolean canDropLoot() {
        return !isChild();
    }

    protected int decreaseAirSupply(int p_70682_1_) {
        int i = EnchantmentHelper.getRespiration(this);
        return i > 0 && rand.nextInt(i + 1) > 0 ? p_70682_1_ : p_70682_1_ - 1;
    }

    protected int getExperiencePoints(EntityPlayer player) {
        return 0;
    }

    protected boolean isPlayer() {
        return false;
    }

    public Random getRNG() {
        return rand;
    }

    public EntityLivingBase getAITarget() {
        return entityLivingToAttack;
    }

    public int getRevengeTimer() {
        return revengeTimer;
    }

    public void setRevengeTarget(EntityLivingBase livingBase) {
        entityLivingToAttack = livingBase;
        revengeTimer = ticksExisted;
    }

    public EntityLivingBase getLastAttacker() {
        return lastAttacker;
    }

    public void setLastAttacker(Entity entityIn) {
        if (entityIn instanceof EntityLivingBase) {
            lastAttacker = (EntityLivingBase) entityIn;
        } else {
            lastAttacker = null;
        }

        lastAttackerTime = ticksExisted;
    }

    public int getLastAttackerTime() {
        return lastAttackerTime;
    }

    public int getAge() {
        return entityAge;
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setFloat("HealF", getHealth());
        tagCompound.setShort("Health", (short) ((int) Math.ceil(getHealth())));
        tagCompound.setShort("HurtTime", (short) hurtTime);
        tagCompound.setInteger("HurtByTimestamp", revengeTimer);
        tagCompound.setShort("DeathTime", (short) deathTime);
        tagCompound.setFloat("AbsorptionAmount", getAbsorptionAmount());

        for (ItemStack itemstack : getInventory()) {
            if (itemstack != null) {
                attributeMap.removeAttributeModifiers(itemstack.getAttributeModifiers());
            }
        }

        tagCompound.setTag("Attributes", SharedMonsterAttributes.writeBaseAttributeMapToNBT(getAttributeMap()));

        for (ItemStack itemstack1 : getInventory()) {
            if (itemstack1 != null) {
                attributeMap.applyAttributeModifiers(itemstack1.getAttributeModifiers());
            }
        }

        if (!activePotionsMap.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();

            for (PotionEffect potioneffect : activePotionsMap.values()) {
                nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
            }

            tagCompound.setTag("ActiveEffects", nbttaglist);
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        setAbsorptionAmount(tagCompund.getFloat("AbsorptionAmount"));

        if (tagCompund.hasKey("Attributes", 9) && worldObj != null && !worldObj.isRemote) {
            SharedMonsterAttributes.setAttributeModifiers(getAttributeMap(), tagCompund.getTagList("Attributes", 10));
        }

        if (tagCompund.hasKey("ActiveEffects", 9)) {
            NBTTagList nbttaglist = tagCompund.getTagList("ActiveEffects", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                PotionEffect potioneffect = PotionEffect.readCustomPotionEffectFromNBT(nbttagcompound);

                if (potioneffect != null) {
                    activePotionsMap.put(potioneffect.getPotionID(), potioneffect);
                }
            }
        }

        if (tagCompund.hasKey("HealF", 99)) {
            setHealth(tagCompund.getFloat("HealF"));
        } else {
            NBTBase nbtbase = tagCompund.getTag("Health");

            if (nbtbase == null) {
                setHealth(getMaxHealth());
            } else if (nbtbase.getId() == 5) {
                setHealth(((NBTTagFloat) nbtbase).getFloat());
            } else if (nbtbase.getId() == 2) {
                setHealth(((NBTTagShort) nbtbase).getShort());
            }
        }

        hurtTime = tagCompund.getShort("HurtTime");
        deathTime = tagCompund.getShort("DeathTime");
        revengeTimer = tagCompund.getInteger("HurtByTimestamp");
    }

    protected void updatePotionEffects() {
        Iterator<Integer> iterator = activePotionsMap.keySet().iterator();

        while (iterator.hasNext()) {
            Integer integer = iterator.next();
            PotionEffect potioneffect = activePotionsMap.get(integer);

            if (!potioneffect.onUpdate(this)) {
                if (!worldObj.isRemote) {
                    iterator.remove();
                    onFinishedPotionEffect(potioneffect);
                }
            } else if (potioneffect.getDuration() % 600 == 0) {
                onChangedPotionEffect(potioneffect, false);
            }
        }

        if (potionsNeedUpdate) {
            if (!worldObj.isRemote) {
                updatePotionMetadata();
            }

            potionsNeedUpdate = false;
        }

        int i = dataWatcher.getWatchableObjectInt(7);
        boolean flag1 = dataWatcher.getWatchableObjectByte(8) > 0;

        if (i > 0) {
            boolean flag;

            if (!isInvisible()) {
                flag = rand.nextBoolean();
            } else {
                flag = rand.nextInt(15) == 0;
            }

            if (flag1) {
                flag &= rand.nextInt(5) == 0;
            }

            if (flag) {
                double d0 = (double) (i >> 16 & 255) / 255.0D;
                double d1 = (double) (i >> 8 & 255) / 255.0D;
                double d2 = (double) (i & 255) / 255.0D;
                worldObj.spawnParticle(flag1 ? ParticleTypes.SPELL_MOB_AMBIENT : ParticleTypes.SPELL_MOB, posX + (rand.nextDouble() - 0.5D) * (double) width, posY + rand.nextDouble() * (double) height, posZ + (rand.nextDouble() - 0.5D) * (double) width, d0, d1, d2);
            }
        }
    }

    protected void updatePotionMetadata() {
        if (activePotionsMap.isEmpty()) {
            resetPotionEffectMetadata();
            setInvisible(false);
        } else {
            int i = PotionHelper.calcPotionLiquidColor(activePotionsMap.values());
            dataWatcher.updateObject(8, (byte) (PotionHelper.getAreAmbient(activePotionsMap.values()) ? 1 : 0));
            dataWatcher.updateObject(7, i);
            setInvisible(isPotionActive(Potion.invisibility.id));
        }
    }

    protected void resetPotionEffectMetadata() {
        dataWatcher.updateObject(8, (byte) 0);
        dataWatcher.updateObject(7, 0);
    }

    public void clearActivePotions() {
        Iterator<Integer> iterator = activePotionsMap.keySet().iterator();

        while (iterator.hasNext()) {
            Integer integer = iterator.next();
            PotionEffect potioneffect = activePotionsMap.get(integer);

            if (!worldObj.isRemote) {
                iterator.remove();
                onFinishedPotionEffect(potioneffect);
            }
        }
    }

    public Collection<PotionEffect> getActivePotionEffects() {
        return activePotionsMap.values();
    }

    public boolean isPotionActive(int potionId) {
        return activePotionsMap.containsKey(potionId);
    }

    public boolean isPotionActive(Potion potionIn) {
        return activePotionsMap.containsKey(potionIn.id);
    }

    public PotionEffect getActivePotionEffect(Potion potionIn) {
        return activePotionsMap.get(potionIn.id);
    }

    public void addPotionEffect(PotionEffect potioneffectIn) {
        if (isPotionApplicable(potioneffectIn)) {
            if (activePotionsMap.containsKey(potioneffectIn.getPotionID())) {
                activePotionsMap.get(potioneffectIn.getPotionID()).combine(potioneffectIn);
                onChangedPotionEffect(activePotionsMap.get(potioneffectIn.getPotionID()), true);
            } else {
                activePotionsMap.put(potioneffectIn.getPotionID(), potioneffectIn);
                onNewPotionEffect(potioneffectIn);
            }
        }
    }

    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        if (getCreatureAttribute() == CreatureAttribute.UNDEAD) {
            int i = potioneffectIn.getPotionID();

            return i != Potion.regeneration.id && i != Potion.poison.id;
        }

        return true;
    }

    public boolean isEntityUndead() {
        return getCreatureAttribute() == CreatureAttribute.UNDEAD;
    }

    public void removePotionEffectClient(int potionId) {
        activePotionsMap.remove(potionId);
    }

    public void removePotionEffect(int potionId) {
        PotionEffect potioneffect = activePotionsMap.remove(potionId);

        if (potioneffect != null) {
            onFinishedPotionEffect(potioneffect);
        }
    }

    protected void onNewPotionEffect(PotionEffect id) {
        potionsNeedUpdate = true;

        if (!worldObj.isRemote) {
            Potion.potionTypes[id.getPotionID()].applyAttributesModifiersToEntity(this, getAttributeMap(), id.getAmplifier());
        }
    }

    protected void onChangedPotionEffect(PotionEffect id, boolean p_70695_2_) {
        potionsNeedUpdate = true;

        if (p_70695_2_ && !worldObj.isRemote) {
            Potion.potionTypes[id.getPotionID()].removeAttributesModifiersFromEntity(this, getAttributeMap(), id.getAmplifier());
            Potion.potionTypes[id.getPotionID()].applyAttributesModifiersToEntity(this, getAttributeMap(), id.getAmplifier());
        }
    }

    protected void onFinishedPotionEffect(PotionEffect effect) {
        potionsNeedUpdate = true;

        if (!worldObj.isRemote) {
            Potion.potionTypes[effect.getPotionID()].removeAttributesModifiersFromEntity(this, getAttributeMap(), effect.getAmplifier());
        }
    }

    public void heal(float healAmount) {
        float f = getHealth();

        if (f > 0.0F) {
            setHealth(f + healAmount);
        }
    }

    public final float getHealth() {
        return dataWatcher.getWatchableObjectFloat(6);
    }

    public void setHealth(float health) {
        dataWatcher.updateObject(6, MathHelper.clamp(health, 0.0F, getMaxHealth()));
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else if (worldObj.isRemote) {
            return false;
        } else {
            entityAge = 0;

            if (getHealth() <= 0.0F) {
                return false;
            } else if (source.isFireDamage() && isPotionActive(Potion.fireResistance)) {
                return false;
            } else {
                if ((source == DamageSource.anvil || source == DamageSource.fallingBlock) && getEquipmentInSlot(4) != null) {
                    getEquipmentInSlot(4).damageItem((int) (amount * 4.0F + rand.nextFloat() * amount * 2.0F), this);
                    amount *= 0.75F;
                }

                limbSwingAmount = 1.5F;
                boolean flag = true;

                if ((float) hurtResistantTime > (float) maxHurtResistantTime / 2.0F) {
                    if (amount <= lastDamage) {
                        return false;
                    }

                    damageEntity(source, amount - lastDamage);
                    lastDamage = amount;
                    flag = false;
                } else {
                    lastDamage = amount;
                    hurtResistantTime = maxHurtResistantTime;
                    damageEntity(source, amount);
                    hurtTime = maxHurtTime = 10;
                }

                attackedAtYaw = 0.0F;
                Entity entity = source.getEntity();

                if (entity != null) {
                    if (entity instanceof EntityLivingBase) {
                        setRevengeTarget((EntityLivingBase) entity);
                    }

                    if (entity instanceof EntityPlayer) {
                        recentlyHit = 100;
                        attackingPlayer = (EntityPlayer) entity;
                    } else if (entity instanceof EntityWolf entitywolf) {

                        if (entitywolf.isTamed()) {
                            recentlyHit = 100;
                            attackingPlayer = null;
                        }
                    }
                }

                if (flag) {
                    worldObj.setEntityState(this, (byte) 2);

                    if (source != DamageSource.drown) {
                        setBeenAttacked();
                    }

                    if (entity != null) {
                        double d1 = entity.posX - posX;
                        double d0;

                        for (d0 = entity.posZ - posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        attackedAtYaw = (float) (MathHelper.atan2(d0, d1) * 180.0D / Math.PI - (double) rotationYaw);
                        knockBack(entity, amount, d1, d0);
                    } else {
                        attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                if (getHealth() <= 0.0F) {
                    String s = getDeathSound();

                    if (flag && s != null) {
                        playSound(s, getSoundVolume(), getSoundPitch());
                    }

                    onDeath(source);
                } else {
                    String s1 = getHurtSound();

                    if (flag && s1 != null) {
                        playSound(s1, getSoundVolume(), getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    public void renderBrokenItemStack(ItemStack stack) {
        playSound("random.break", 0.8F, 0.8F + worldObj.rand.nextFloat() * 0.4F);

        for (int i = 0; i < 5; ++i) {
            Vec3 vec3 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            vec3 = vec3.rotatePitch(-rotationPitch * (float) Math.PI / 180.0F);
            vec3 = vec3.rotateYaw(-rotationYaw * (float) Math.PI / 180.0F);
            double d0 = (double) (-rand.nextFloat()) * 0.6D - 0.3D;
            Vec3 vec31 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
            vec31 = vec31.rotatePitch(-rotationPitch * (float) Math.PI / 180.0F);
            vec31 = vec31.rotateYaw(-rotationYaw * (float) Math.PI / 180.0F);
            vec31 = vec31.addVector(posX, posY + (double) getEyeHeight(), posZ);
            worldObj.spawnParticle(ParticleTypes.ITEM_CRACK, vec31.xCoord(), vec31.yCoord(), vec31.zCoord(), vec3.xCoord(), vec3.yCoord() + 0.05D, vec3.zCoord(), Item.getIdFromItem(stack.getItem()));
        }
    }

    public void onDeath(DamageSource cause) {
        Entity entity = cause.getEntity();
        EntityLivingBase entitylivingbase = getAttackingEntity();

        if (scoreValue >= 0 && entitylivingbase != null) {
            entitylivingbase.addToPlayerScore(this, scoreValue);
        }

        if (entity != null) {
            entity.onKillEntity(this);
        }

        dead = true;
        _combatTracker.reset();

        if (!worldObj.isRemote) {
            int i = 0;

            if (entity instanceof EntityPlayer) {
                i = EnchantmentHelper.getLootingModifier((EntityLivingBase) entity);
            }

            if (canDropLoot() && worldObj.getGameRules().getBoolean("doMobLoot")) {
                dropFewItems(recentlyHit > 0, i);
                dropEquipment(recentlyHit > 0, i);

                if (recentlyHit > 0 && rand.nextFloat() < 0.025F + (float) i * 0.01F) {
                    addRandomDrop();
                }
            }
        }

        worldObj.setEntityState(this, (byte) 3);
    }

    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
    }

    public void knockBack(Entity entityIn, float p_70653_2_, double p_70653_3_, double p_70653_5_) {
        if (rand.nextDouble() >= getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue()) {
            isAirBorne = true;
            float f = MathHelper.sqrt(p_70653_3_ * p_70653_3_ + p_70653_5_ * p_70653_5_);
            float f1 = 0.4F;
            motionX /= 2.0D;
            motionY /= 2.0D;
            motionZ /= 2.0D;
            motionX -= p_70653_3_ / (double) f * (double) f1;
            motionY += f1;
            motionZ -= p_70653_5_ / (double) f * (double) f1;

            if (motionY > 0.4000000059604645D) {
                motionY = 0.4000000059604645D;
            }
        }
    }

    protected String getHurtSound() {
        return "game.neutral.hurt";
    }

    protected String getDeathSound() {
        return "game.neutral.die";
    }

    protected void addRandomDrop() {
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    }

    public boolean isOnLadder() {
        int i = MathHelper.floor(posX);
        int j = MathHelper.floor(getEntityBoundingBox().minY);
        int k = MathHelper.floor(posZ);
        Block block = worldObj.getBlockState(new BlockPos(i, j, k)).getBlock();
        return (block == Blocks.ladder || block == Blocks.vine) && (!(this instanceof EntityPlayer) || !((EntityPlayer) this).isSpectator());
    }

    public boolean isEntityAlive() {
        return !isDead && getHealth() > 0.0F;
    }

    public void fall(float distance, float damageMultiplier) {
        super.fall(distance, damageMultiplier);
        PotionEffect potioneffect = getActivePotionEffect(Potion.jump);
        float f = potioneffect != null ? (float) (potioneffect.getAmplifier() + 1) : 0.0F;
        int i = MathHelper.ceil((distance - 3.0F - f) * damageMultiplier);

        if (i > 0) {
            playSound(getFallSoundString(i), 1.0F, 1.0F);
            attackEntityFrom(DamageSource.fall, (float) i);
            int j = MathHelper.floor(posX);
            int k = MathHelper.floor(posY - 0.20000000298023224D);
            int l = MathHelper.floor(posZ);
            Block block = worldObj.getBlockState(new BlockPos(j, k, l)).getBlock();

            if (block.getMaterial() != Material.air) {
                Block.SoundType block$soundtype = block.stepSound;
                playSound(block$soundtype.getStepSound(), block$soundtype.getVolume() * 0.5F, block$soundtype.getFrequency() * 0.75F);
            }
        }
    }

    protected String getFallSoundString(int damageValue) {
        return damageValue > 4 ? "game.neutral.hurt.fall.big" : "game.neutral.hurt.fall.small";
    }

    public void performHurtAnimation() {
        hurtTime = maxHurtTime = 10;
        attackedAtYaw = 0.0F;
    }

    public int getTotalArmorValue() {
        int i = 0;

        for (ItemStack itemstack : getInventory()) {
            if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
                int j = ((ItemArmor) itemstack.getItem()).damageReduceAmount;
                i += j;
            }
        }

        return i;
    }

    protected void damageArmor(float p_70675_1_) {
    }

    protected float applyArmorCalculations(DamageSource source, float damage) {
        if (!source.isUnblockable()) {
            int i = 25 - getTotalArmorValue();
            float f = damage * (float) i;
            damageArmor(damage);
            damage = f / 25.0F;
        }

        return damage;
    }

    protected float applyPotionDamageCalculations(DamageSource source, float damage) {
        if (source.isDamageAbsolute()) {
            return damage;
        } else {
            if (isPotionActive(Potion.resistance) && source != DamageSource.outOfWorld) {
                int i = (getActivePotionEffect(Potion.resistance).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = damage * (float) j;
                damage = f / 25.0F;
            }

            if (damage <= 0.0F) {
                return 0.0F;
            } else {
                int k = EnchantmentHelper.getEnchantmentModifierDamage(getInventory(), source);

                if (k > 20) {
                    k = 20;
                }

                if (k > 0) {
                    int l = 25 - k;
                    float f1 = damage * (float) l;
                    damage = f1 / 25.0F;
                }

                return damage;
            }
        }
    }

    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        if (!isEntityInvulnerable(damageSrc)) {
            damageAmount = applyArmorCalculations(damageSrc, damageAmount);
            damageAmount = applyPotionDamageCalculations(damageSrc, damageAmount);
            float f = damageAmount;
            damageAmount = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
            setAbsorptionAmount(getAbsorptionAmount() - (f - damageAmount));

            if (damageAmount != 0.0F) {
                float f1 = getHealth();
                setHealth(f1 - damageAmount);
                _combatTracker.trackDamage(damageSrc, f1, damageAmount);
                setAbsorptionAmount(getAbsorptionAmount() - damageAmount);
            }
        }
    }

    public CombatTracker getCombatTracker() {
        return _combatTracker;
    }

    public EntityLivingBase getAttackingEntity() {
        return _combatTracker.func_94550_c() != null ? _combatTracker.func_94550_c() : (attackingPlayer != null ? attackingPlayer : (entityLivingToAttack != null ? entityLivingToAttack : null));
    }

    public final float getMaxHealth() {
        return (float) getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue();
    }

    public final int getArrowCountInEntity() {
        return dataWatcher.getWatchableObjectByte(9);
    }

    public final void setArrowCountInEntity(int count) {
        dataWatcher.updateObject(9, (byte) count);
    }

    private int getArmSwingAnimationEnd() {
        return isPotionActive(Potion.digSpeed) ? 6 - (1 + getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (isPotionActive(Potion.digSlowdown) ? 6 + (1 + getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
    }

    public void swingItem() {
        if (!isSwingInProgress || swingProgressInt >= getArmSwingAnimationEnd() / 2 || swingProgressInt < 0) {
            swingProgressInt = -1;
            isSwingInProgress = true;

            if (worldObj instanceof WorldServer) {
                ((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S0BPacketAnimation(this, 0));
            }
        }
    }

    public void handleStatusUpdate(byte id) {
        if (id == 2) {
            limbSwingAmount = 1.5F;
            hurtResistantTime = maxHurtResistantTime;
            hurtTime = maxHurtTime = 10;
            attackedAtYaw = 0.0F;
            String s = getHurtSound();

            if (s != null) {
                playSound(getHurtSound(), getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }

            attackEntityFrom(DamageSource.generic, 0.0F);
        } else if (id == 3) {
            String s1 = getDeathSound();

            if (s1 != null) {
                playSound(getDeathSound(), getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }

            setHealth(0.0F);
            onDeath(DamageSource.generic);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    protected void kill() {
        attackEntityFrom(DamageSource.outOfWorld, 4.0F);
    }

    protected void updateArmSwingProgress() {
        int i = getArmSwingAnimationEnd();

        if (isSwingInProgress) {
            ++swingProgressInt;

            if (swingProgressInt >= i) {
                swingProgressInt = 0;
                isSwingInProgress = false;
            }
        } else {
            swingProgressInt = 0;
        }

        swingProgress = (float) swingProgressInt / (float) i;
    }

    public IAttributeInstance getEntityAttribute(IAttribute attribute) {
        return getAttributeMap().getAttributeInstance(attribute);
    }

    public BaseAttributeMap getAttributeMap() {
        if (attributeMap == null) {
            attributeMap = new ServersideAttributeMap();
        }

        return attributeMap;
    }

    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEFINED;
    }

    public abstract ItemStack getHeldItem();

    public abstract ItemStack getEquipmentInSlot(int slotIn);

    public abstract ItemStack getCurrentArmor(int slotIn);

    public abstract void setCurrentItemOrArmor(int slotIn, ItemStack stack);

    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);

        if (iattributeinstance.getModifier(sprintingSpeedBoostModifierUUID) != null) {
            iattributeinstance.removeModifier(sprintingSpeedBoostModifier);
        }

        if (sprinting) {
            iattributeinstance.applyModifier(sprintingSpeedBoostModifier);
        }
    }

    public abstract ItemStack[] getInventory();

    protected float getSoundVolume() {
        return 1.0F;
    }

    protected float getSoundPitch() {
        return isChild() ? (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.5F : (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F;
    }

    protected boolean isMovementBlocked() {
        return getHealth() <= 0.0F;
    }

    public void dismountEntity(Entity entityIn) {
        double d0 = entityIn.posX;
        double d1 = entityIn.getEntityBoundingBox().minY + (double) entityIn.height;
        double d2 = entityIn.posZ;
        int i = 1;

        for (int j = -i; j <= i; ++j) {
            for (int k = -i; k < i; ++k) {
                if (j != 0 || k != 0) {
                    int l = (int) (posX + (double) j);
                    int i1 = (int) (posZ + (double) k);
                    AxisAlignedBB axisalignedbb = getEntityBoundingBox().offset(j, 1.0D, k);

                    if (worldObj.getCollisionBoxes(axisalignedbb).isEmpty()) {
                        if (World.doesBlockHaveSolidTopSurface(worldObj, new BlockPos(l, (int) posY, i1))) {
                            setPositionAndUpdate(posX + (double) j, posY + 1.0D, posZ + (double) k);
                            return;
                        }

                        if (World.doesBlockHaveSolidTopSurface(worldObj, new BlockPos(l, (int) posY - 1, i1)) || worldObj.getBlockState(new BlockPos(l, (int) posY - 1, i1)).getBlock().getMaterial() == Material.water) {
                            d0 = posX + (double) j;
                            d1 = posY + 1.0D;
                            d2 = posZ + (double) k;
                        }
                    }
                }
            }
        }

        setPositionAndUpdate(d0, d1, d2);
    }

    public boolean getAlwaysRenderNameTagForRender() {
        return false;
    }

    protected float getJumpUpwardsMotion() {
        return 0.42F;
    }

    protected void jump() {
        motionY = getJumpUpwardsMotion();

        if (isPotionActive(Potion.jump)) {
            motionY += (float) (getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }

        if (isSprinting()) {
            float f = rotationYaw * 0.017453292F;
            motionX -= MathHelper.sin(f) * 0.2F;
            motionZ += MathHelper.cos(f) * 0.2F;
        }

        isAirBorne = true;
    }

    protected void updateAITick() {
        motionY += 0.03999999910593033D;
    }

    protected void handleJumpLava() {
        motionY += 0.03999999910593033D;
    }

    public void moveEntityWithHeading(float strafe, float forward) {
        if (isServerWorld()) {
            if (!isInWater() || this instanceof EntityPlayer && ((EntityPlayer) this).capabilities.isFlying) {
                if (!isInLava() || this instanceof EntityPlayer && ((EntityPlayer) this).capabilities.isFlying) {
                    float f4 = 0.91F;

                    if (onGround) {
                        f4 = worldObj.getBlockState(new BlockPos(MathHelper.floor(posX), MathHelper.floor(getEntityBoundingBox().minY) - 1, MathHelper.floor(posZ))).getBlock().slipperiness * 0.91F;
                    }

                    float f = 0.16277136F / (f4 * f4 * f4);
                    float f5;

                    if (onGround) {
                        f5 = getAIMoveSpeed() * f;
                    } else {
                        f5 = jumpMovementFactor;
                    }

                    moveFlying(strafe, forward, f5);
                    f4 = 0.91F;

                    if (onGround) {
                        f4 = worldObj.getBlockState(new BlockPos(MathHelper.floor(posX), MathHelper.floor(getEntityBoundingBox().minY) - 1, MathHelper.floor(posZ))).getBlock().slipperiness * 0.91F;
                    }

                    if (isOnLadder()) {
                        float f6 = 0.15F;
                        motionX = MathHelper.clamp(motionX, -f6, f6);
                        motionZ = MathHelper.clamp(motionZ, -f6, f6);
                        fallDistance = 0.0F;

                        if (motionY < -0.15D) {
                            motionY = -0.15D;
                        }

                        boolean flag = isSneaking() && this instanceof EntityPlayer;

                        if (flag && motionY < 0.0D) {
                            motionY = 0.0D;
                        }
                    }

                    moveEntity(motionX, motionY, motionZ);

                    if (isCollidedHorizontally && isOnLadder()) {
                        motionY = 0.2D;
                    }

                    if (worldObj.isRemote && (!worldObj.isBlockLoaded(new BlockPos((int) posX, 0, (int) posZ)) || !worldObj.getChunkFromBlockCoords(new BlockPos((int) posX, 0, (int) posZ)).isLoaded())) {
                        if (posY > 0.0D) {
                            motionY = -0.1D;
                        } else {
                            motionY = 0.0D;
                        }
                    } else {
                        motionY -= 0.08D;
                    }

                    motionY *= 0.9800000190734863D;
                    motionX *= f4;
                    motionZ *= f4;
                } else {
                    double d1 = posY;
                    moveFlying(strafe, forward, 0.02F);
                    moveEntity(motionX, motionY, motionZ);
                    motionX *= 0.5D;
                    motionY *= 0.5D;
                    motionZ *= 0.5D;
                    motionY -= 0.02D;

                    if (isCollidedHorizontally && isOffsetPositionInLiquid(motionX, motionY + 0.6000000238418579D - posY + d1, motionZ)) {
                        motionY = 0.30000001192092896D;
                    }
                }
            } else {
                double d0 = posY;
                float f1 = 0.8F;
                float f2 = 0.02F;
                float f3 = (float) EnchantmentHelper.getDepthStriderModifier(this);

                if (f3 > 3.0F) {
                    f3 = 3.0F;
                }

                if (!onGround) {
                    f3 *= 0.5F;
                }

                if (f3 > 0.0F) {
                    f1 += (0.54600006F - f1) * f3 / 3.0F;
                    f2 += (getAIMoveSpeed() - f2) * f3 / 3.0F;
                }

                moveFlying(strafe, forward, f2);
                moveEntity(motionX, motionY, motionZ);
                motionX *= f1;
                motionY *= 0.800000011920929D;
                motionZ *= f1;
                motionY -= 0.02D;

                if (isCollidedHorizontally && isOffsetPositionInLiquid(motionX, motionY + 0.6000000238418579D - posY + d0, motionZ)) {
                    motionY = 0.30000001192092896D;
                }
            }
        }

        prevLimbSwingAmount = limbSwingAmount;
        double d2 = posX - prevPosX;
        double d3 = posZ - prevPosZ;
        float f7 = MathHelper.sqrt(d2 * d2 + d3 * d3) * 4.0F;

        if (f7 > 1.0F) {
            f7 = 1.0F;
        }

        limbSwingAmount += (f7 - limbSwingAmount) * 0.4F;
        limbSwing += limbSwingAmount;
    }

    public float getAIMoveSpeed() {
        return landMovementFactor;
    }

    public void setAIMoveSpeed(float speedIn) {
        landMovementFactor = speedIn;
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        setLastAttacker(entityIn);
        return false;
    }

    public boolean isPlayerSleeping() {
        return false;
    }

    public void onUpdate() {
        super.onUpdate();

        if (!worldObj.isRemote) {
            int i = getArrowCountInEntity();

            if (i > 0) {
                if (arrowHitTimer <= 0) {
                    arrowHitTimer = 20 * (30 - i);
                }

                --arrowHitTimer;

                if (arrowHitTimer <= 0) {
                    setArrowCountInEntity(i - 1);
                }
            }

            for (int j = 0; j < 5; ++j) {
                ItemStack itemstack = previousEquipment[j];
                ItemStack itemstack1 = getEquipmentInSlot(j);

                if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                    ((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S04PacketEntityEquipment(getEntityId(), j, itemstack1));

                    if (itemstack != null) {
                        attributeMap.removeAttributeModifiers(itemstack.getAttributeModifiers());
                    }

                    if (itemstack1 != null) {
                        attributeMap.applyAttributeModifiers(itemstack1.getAttributeModifiers());
                    }

                    previousEquipment[j] = itemstack1 == null ? null : itemstack1.copy();
                }
            }

            if (ticksExisted % 20 == 0) {
                _combatTracker.reset();
            }
        }

        onLivingUpdate();
        double d0 = posX - prevPosX;
        double d1 = posZ - prevPosZ;
        float f = (float) (d0 * d0 + d1 * d1);
        float f1 = renderYawOffset;
        float f2 = 0.0F;
        prevOnGroundSpeedFactor = onGroundSpeedFactor;
        float f3 = 0.0F;

        if (f > 0.0025000002F) {
            f3 = 1.0F;
            f2 = (float) Math.sqrt(f) * 3.0F;
            f1 = (float) MathHelper.atan2(d1, d0) * 180.0F / (float) Math.PI - 90.0F;
        }

        if (swingProgress > 0.0F) {
            f1 = rotationYaw;
        }

        if (!onGround) {
            f3 = 0.0F;
        }

        onGroundSpeedFactor += (f3 - onGroundSpeedFactor) * 0.3F;
        worldObj.theProfiler.startSection("headTurn");
        f2 = updateDistance(f1, f2);
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("rangeChecks");

        while (rotationYaw - prevRotationYaw < -180.0F) {
            prevRotationYaw -= 360.0F;
        }

        while (rotationYaw - prevRotationYaw >= 180.0F) {
            prevRotationYaw += 360.0F;
        }

        while (renderYawOffset - prevRenderYawOffset < -180.0F) {
            prevRenderYawOffset -= 360.0F;
        }

        while (renderYawOffset - prevRenderYawOffset >= 180.0F) {
            prevRenderYawOffset += 360.0F;
        }

        while (rotationPitch - prevRotationPitch < -180.0F) {
            prevRotationPitch -= 360.0F;
        }

        while (rotationPitch - prevRotationPitch >= 180.0F) {
            prevRotationPitch += 360.0F;
        }

        while (rotationYawHead - prevRotationYawHead < -180.0F) {
            prevRotationYawHead -= 360.0F;
        }

        while (rotationYawHead - prevRotationYawHead >= 180.0F) {
            prevRotationYawHead += 360.0F;
        }

        worldObj.theProfiler.endSection();
        movedDistance += f2;
    }

    protected float updateDistance(float p_110146_1_, float p_110146_2_) {
        float f = MathHelper.wrapAngle(p_110146_1_ - renderYawOffset);
        renderYawOffset += f * 0.3F;
        float f1 = MathHelper.wrapAngle(rotationYaw - renderYawOffset);
        boolean flag = f1 < -90.0F || f1 >= 90.0F;

        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        renderYawOffset = rotationYaw - f1;

        if (f1 * f1 > 2500.0F) {
            renderYawOffset += f1 * 0.2F;
        }

        if (flag) {
            p_110146_2_ *= -1.0F;
        }

        return p_110146_2_;
    }

    public void onLivingUpdate() {
        if (jumpTicks > 0) {
            --jumpTicks;
        }

        if (newPosRotationIncrements > 0) {
            double d0 = posX + (newPosX - posX) / (double) newPosRotationIncrements;
            double d1 = posY + (newPosY - posY) / (double) newPosRotationIncrements;
            double d2 = posZ + (newPosZ - posZ) / (double) newPosRotationIncrements;
            double d3 = MathHelper.wrapAngle(newRotationYaw - (double) rotationYaw);
            rotationYaw = (float) ((double) rotationYaw + d3 / (double) newPosRotationIncrements);
            rotationPitch = (float) ((double) rotationPitch + (newRotationPitch - (double) rotationPitch) / (double) newPosRotationIncrements);
            --newPosRotationIncrements;
            setPosition(d0, d1, d2);
            setRotation(rotationYaw, rotationPitch);
        } else if (!isServerWorld()) {
            motionX *= 0.98D;
            motionY *= 0.98D;
            motionZ *= 0.98D;
        }

        if (Math.abs(motionX) < 0.005D) {
            motionX = 0.0D;
        }

        if (Math.abs(motionY) < 0.005D) {
            motionY = 0.0D;
        }

        if (Math.abs(motionZ) < 0.005D) {
            motionZ = 0.0D;
        }

        worldObj.theProfiler.startSection("ai");

        if (isMovementBlocked()) {
            isJumping = false;
            moveStrafing = 0.0F;
            moveForward = 0.0F;
            randomYawVelocity = 0.0F;
        } else if (isServerWorld()) {
            worldObj.theProfiler.startSection("newAi");
            updateEntityActionState();
            worldObj.theProfiler.endSection();
        }

        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("jump");

        if (isJumping) {
            if (isInWater()) {
                updateAITick();
            } else if (isInLava()) {
                handleJumpLava();
            } else if (onGround && jumpTicks == 0) {
                jump();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }

        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("travel");
        moveStrafing *= 0.98F;
        moveForward *= 0.98F;
        randomYawVelocity *= 0.9F;
        moveEntityWithHeading(moveStrafing, moveForward);
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("push");

        if (!worldObj.isRemote) {
            collideWithNearbyEntities();
        }

        worldObj.theProfiler.endSection();
    }

    protected void updateEntityActionState() {
    }

    protected void collideWithNearbyEntities() {
        List<Entity> list = worldObj.getEntitiesInAABBexcluding(this, getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D), Predicates.and(EntitySelectors.NOT_SPECTATING, p_apply_1_ -> p_apply_1_.canBePushed()));

        if (!list.isEmpty()) {
            for (Entity entity : list) {
                collideWithEntity(entity);
            }
        }
    }

    protected void collideWithEntity(Entity entityIn) {
        entityIn.applyEntityCollision(this);
    }

    public void mountEntity(Entity entityIn) {
        if (ridingEntity != null && entityIn == null) {
            if (!worldObj.isRemote) {
                dismountEntity(ridingEntity);
            }

            if (ridingEntity != null) {
                ridingEntity.riddenByEntity = null;
            }

            ridingEntity = null;
        } else {
            super.mountEntity(entityIn);
        }
    }

    public void updateRidden() {
        super.updateRidden();
        prevOnGroundSpeedFactor = onGroundSpeedFactor;
        onGroundSpeedFactor = 0.0F;
        fallDistance = 0.0F;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
        newPosX = x;
        newPosY = y;
        newPosZ = z;
        newRotationYaw = yaw;
        newRotationPitch = pitch;
        newPosRotationIncrements = posRotationIncrements;
    }

    public void setJumping(boolean jumping) {
        isJumping = jumping;
    }

    public void onItemPickup(Entity p_71001_1_, int p_71001_2_) {
        if (!p_71001_1_.isDead && !worldObj.isRemote) {
            EntityTracker entitytracker = ((WorldServer) worldObj).getEntityTracker();

            if (p_71001_1_ instanceof EntityItem) {
                entitytracker.sendToAllTrackingEntity(p_71001_1_, new S0DPacketCollectItem(p_71001_1_.getEntityId(), getEntityId()));
            }

            if (p_71001_1_ instanceof EntityArrow) {
                entitytracker.sendToAllTrackingEntity(p_71001_1_, new S0DPacketCollectItem(p_71001_1_.getEntityId(), getEntityId()));
            }

            if (p_71001_1_ instanceof EntityXPOrb) {
                entitytracker.sendToAllTrackingEntity(p_71001_1_, new S0DPacketCollectItem(p_71001_1_.getEntityId(), getEntityId()));
            }
        }
    }

    public boolean canEntityBeSeen(Entity entityIn) {
        return worldObj.rayTraceBlocks(new Vec3(posX, posY + (double) getEyeHeight(), posZ), new Vec3(entityIn.posX, entityIn.posY + (double) entityIn.getEyeHeight(), entityIn.posZ)) == null;
    }

    public Vec3 getLookVec() {
        return getLook(1.0F);
    }

    public Vec3 getLook(float partialTicks) {
        if (partialTicks == 1.0F) {
            return getVectorForRotation(rotationPitch, rotationYawHead);
        } else {
            float f = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks;
            float f1 = prevRotationYawHead + (rotationYawHead - prevRotationYawHead) * partialTicks;
            return getVectorForRotation(f, f1);
        }
    }

    public float getSwingProgress(float partialTickTime) {
        float f = swingProgress - prevSwingProgress;

        if (f < 0.0F) {
            ++f;
        }

        return prevSwingProgress + f * partialTickTime;
    }

    public boolean isServerWorld() {
        return !worldObj.isRemote;
    }

    public boolean canBeCollidedWith() {
        return !isDead;
    }

    public boolean canBePushed() {
        return !isDead;
    }

    protected void setBeenAttacked() {
        velocityChanged = rand.nextDouble() >= getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue();
    }

    public float getRotationYawHead() {
        return rotationYawHead;
    }

    public void setRotationYawHead(float rotation) {
        rotationYawHead = rotation;
    }

    public void setRenderYawOffset(float offset) {
        renderYawOffset = offset;
    }

    public float getAbsorptionAmount() {
        return absorptionAmount;
    }

    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0F) {
            amount = 0.0F;
        }

        absorptionAmount = amount;
    }

    public Team getTeam() {
        return worldObj.getScoreboard().getPlayersTeam(getUniqueID().toString());
    }

    public boolean isOnSameTeam(EntityLivingBase otherEntity) {
        return isOnTeam(otherEntity.getTeam());
    }

    public boolean isOnTeam(Team teamIn) {
        return getTeam() != null && getTeam().isSameTeam(teamIn);
    }

    public void sendEnterCombat() {
    }

    public void sendEndCombat() {
    }

    protected void markPotionsDirty() {
        potionsNeedUpdate = true;
    }
}
