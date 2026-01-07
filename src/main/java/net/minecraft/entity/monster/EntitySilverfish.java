package net.minecraft.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

public class EntitySilverfish extends EntityMob {
    private final EntitySilverfish.AISummonSilverfish summonSilverfish;

    public EntitySilverfish(World worldIn) {
        super(worldIn);
        setSize(0.4F, 0.3F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(3, summonSilverfish = new EntitySilverfish.AISummonSilverfish(this));
        tasks.addTask(4, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        tasks.addTask(5, new EntitySilverfish.AIHideInStone(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
    }

    public double getYOffset() {
        return 0.2D;
    }

    public float getEyeHeight() {
        return 0.1F;
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected String getLivingSound() {
        return "mob.silverfish.say";
    }

    protected String getHurtSound() {
        return "mob.silverfish.hit";
    }

    protected String getDeathSound() {
        return "mob.silverfish.kill";
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            if (source instanceof EntityDamageSource || source == DamageSource.magic) {
                summonSilverfish.func_179462_f();
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound("mob.silverfish.step", 0.15F, 1.0F);
    }

    protected Item getDropItem() {
        return null;
    }

    public void onUpdate() {
        renderYawOffset = rotationYaw;
        super.onUpdate();
    }

    public float getBlockPathWeight(BlockPos pos) {
        return worldObj.getBlockState(pos.down()).getBlock() == Blocks.stone ? 10.0F : super.getBlockPathWeight(pos);
    }

    protected boolean isValidLightLevel() {
        return true;
    }

    public boolean getCanSpawnHere() {
        if (super.getCanSpawnHere()) {
            EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, 5.0D);
            return entityplayer == null;
        } else {
            return false;
        }
    }

    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    static class AIHideInStone extends EntityAIWander {
        private final EntitySilverfish silverfish;
        private EnumFacing facing;
        private boolean field_179484_c;

        public AIHideInStone(EntitySilverfish silverfishIn) {
            super(silverfishIn, 1.0D, 10);
            silverfish = silverfishIn;
            setMutexBits(1);
        }

        public boolean shouldExecute() {
            if (silverfish.getAttackTarget() != null) {
                return false;
            } else if (!silverfish.getNavigator().noPath()) {
                return false;
            } else {
                Random random = silverfish.getRNG();

                if (random.nextInt(10) == 0) {
                    facing = EnumFacing.random(random);
                    BlockPos blockpos = (new BlockPos(silverfish.posX, silverfish.posY + 0.5D, silverfish.posZ)).offset(facing);
                    IBlockState iblockstate = silverfish.worldObj.getBlockState(blockpos);

                    if (BlockSilverfish.canContainSilverfish(iblockstate)) {
                        field_179484_c = true;
                        return true;
                    }
                }

                field_179484_c = false;
                return super.shouldExecute();
            }
        }

        public boolean continueExecuting() {
            return !field_179484_c && super.continueExecuting();
        }

        public void startExecuting() {
            if (!field_179484_c) {
                super.startExecuting();
            } else {
                World world = silverfish.worldObj;
                BlockPos blockpos = (new BlockPos(silverfish.posX, silverfish.posY + 0.5D, silverfish.posZ)).offset(facing);
                IBlockState iblockstate = world.getBlockState(blockpos);

                if (BlockSilverfish.canContainSilverfish(iblockstate)) {
                    world.setBlockState(blockpos, Blocks.monster_egg.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.forModelBlock(iblockstate)), 3);
                    silverfish.spawnExplosionParticle();
                    silverfish.setDead();
                }
            }
        }
    }

    static class AISummonSilverfish extends EntityAIBase {
        private final EntitySilverfish silverfish;
        private int field_179463_b;

        public AISummonSilverfish(EntitySilverfish silverfishIn) {
            silverfish = silverfishIn;
        }

        public void func_179462_f() {
            if (field_179463_b == 0) {
                field_179463_b = 20;
            }
        }

        public boolean shouldExecute() {
            return field_179463_b > 0;
        }

        public void updateTask() {
            --field_179463_b;

            if (field_179463_b <= 0) {
                World world = silverfish.worldObj;
                Random random = silverfish.getRNG();
                BlockPos blockpos = new BlockPos(silverfish);

                for (int i = 0; i <= 5 && i >= -5; i = i <= 0 ? 1 - i : -i) {
                    for (int j = 0; j <= 10 && j >= -10; j = j <= 0 ? 1 - j : -j) {
                        for (int k = 0; k <= 10 && k >= -10; k = k <= 0 ? 1 - k : -k) {
                            BlockPos blockpos1 = blockpos.add(j, i, k);
                            IBlockState iblockstate = world.getBlockState(blockpos1);

                            if (iblockstate.getBlock() == Blocks.monster_egg) {
                                if (world.getGameRules().getBoolean("mobGriefing")) {
                                    world.destroyBlock(blockpos1, true);
                                } else {
                                    world.setBlockState(blockpos1, iblockstate.getValue(BlockSilverfish.VARIANT).getModelBlock(), 3);
                                }

                                if (random.nextBoolean()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
