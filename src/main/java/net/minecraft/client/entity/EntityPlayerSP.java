package net.minecraft.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

public class EntityPlayerSP extends AbstractClientPlayer {
    public final NetHandlerPlayClient sendQueue;
    private final StatFileWriter statWriter;
    public MovementInput movementInput;
    public int sprintingTicksLeft;
    public float renderArmYaw;
    public float renderArmPitch;
    public float prevRenderArmYaw;
    public float prevRenderArmPitch;
    public float timeInPortal;
    public float prevTimeInPortal;
    protected Minecraft mc;
    protected int sprintToggleTimer;
    private double lastReportedPosX;
    private double lastReportedPosY;
    private double lastReportedPosZ;
    private float lastReportedYaw;
    private float lastReportedPitch;
    private boolean serverSneakState;
    private boolean serverSprintState;
    private int positionUpdateTicks;
    private boolean hasValidHealth;
    private String clientBrand;
    private int horseJumpPowerCounter;
    private float horseJumpPower;

    public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatFileWriter statFile) {
        super(worldIn, netHandler.getGameProfile());
        sendQueue = netHandler;
        statWriter = statFile;
        mc = mcIn;
        dimension = 0;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    public void heal(float healAmount) {
    }

    public void mountEntity(Entity entityIn) {
        super.mountEntity(entityIn);

        if (entityIn instanceof EntityMinecart) {
            mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart) entityIn));
        }
    }

    public void onUpdate() {
        if (worldObj.isBlockLoaded(new BlockPos(posX, 0.0D, posZ))) {
            super.onUpdate();

            if (isRiding()) {
                sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(rotationYaw, rotationPitch, onGround));
                sendQueue.addToSendQueue(new C0CPacketInput(moveStrafing, moveForward, movementInput.jump, movementInput.sneak));
            } else {
                onUpdateWalkingPlayer();
            }
        }
    }

    public void onUpdateWalkingPlayer() {
        boolean flag = isSprinting();

        if (flag != serverSprintState) {
            if (flag) {
                sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
            } else {
                sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            serverSprintState = flag;
        }

        boolean flag1 = isSneaking();

        if (flag1 != serverSneakState) {
            if (flag1) {
                sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SNEAKING));
            } else {
                sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

            serverSneakState = flag1;
        }

        if (isCurrentViewEntity()) {
            double d0 = posX - lastReportedPosX;
            double d1 = getEntityBoundingBox().minY - lastReportedPosY;
            double d2 = posZ - lastReportedPosZ;
            double d3 = rotationYaw - lastReportedYaw;
            double d4 = rotationPitch - lastReportedPitch;
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || positionUpdateTicks >= 20;
            boolean flag3 = d3 != 0.0D || d4 != 0.0D;

            if (ridingEntity == null) {
                if (flag2 && flag3) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(posX, getEntityBoundingBox().minY, posZ, rotationYaw, rotationPitch, onGround));
                } else if (flag2) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, getEntityBoundingBox().minY, posZ, onGround));
                } else if (flag3) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(rotationYaw, rotationPitch, onGround));
                } else {
                    sendQueue.addToSendQueue(new C03PacketPlayer(onGround));
                }
            } else {
                sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(motionX, -999.0D, motionZ, rotationYaw, rotationPitch, onGround));
                flag2 = false;
            }

            ++positionUpdateTicks;

            if (flag2) {
                lastReportedPosX = posX;
                lastReportedPosY = getEntityBoundingBox().minY;
                lastReportedPosZ = posZ;
                positionUpdateTicks = 0;
            }

            if (flag3) {
                lastReportedYaw = rotationYaw;
                lastReportedPitch = rotationPitch;
            }
        }
    }

    public EntityItem dropOneItem(boolean dropAll) {
        C07PacketPlayerDigging.Action c07packetplayerdigging$action = dropAll ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
        sendQueue.addToSendQueue(new C07PacketPlayerDigging(c07packetplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
        return null;
    }

    protected void joinEntityItemWithWorld(EntityItem itemIn) {
    }

    public void sendChatMessage(String message) {
        sendQueue.addToSendQueue(new C01PacketChatMessage(message));
    }

    public void swingItem() {
        super.swingItem();
        sendQueue.addToSendQueue(new C0APacketAnimation());
    }

    public void respawnPlayer() {
        sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
    }

    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        if (!isEntityInvulnerable(damageSrc)) {
            setHealth(getHealth() - damageAmount);
        }
    }

    public void closeScreen() {
        sendQueue.addToSendQueue(new C0DPacketCloseWindow(openContainer.windowId));
        closeScreenAndDropStack();
    }

    public void closeScreenAndDropStack() {
        inventory.setItemStack(null);
        super.closeScreen();
        mc.displayGuiScreen(null);
    }

    public void setPlayerSPHealth(float health) {
        if (hasValidHealth) {
            float f = getHealth() - health;

            if (f <= 0.0F) {
                setHealth(health);

                if (f < 0.0F) {
                    hurtResistantTime = maxHurtResistantTime / 2;
                }
            } else {
                lastDamage = f;
                setHealth(getHealth());
                hurtResistantTime = maxHurtResistantTime;
                damageEntity(DamageSource.generic, f);
                hurtTime = maxHurtTime = 10;
            }
        } else {
            setHealth(health);
            hasValidHealth = true;
        }
    }

    public void addStat(StatBase stat, int amount) {
        if (stat != null) {
            if (stat.isIndependent) {
                super.addStat(stat, amount);
            }
        }
    }

    public void sendPlayerAbilities() {
        sendQueue.addToSendQueue(new C13PacketPlayerAbilities(capabilities));
    }

    public boolean isUser() {
        return true;
    }

    protected void sendHorseJump() {
        sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.RIDING_JUMP, (int) (horseJumpPower * 100.0F)));
    }

    public void sendHorseInventory() {
        sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.OPEN_INVENTORY));
    }

    public String getClientBrand() {
        return clientBrand;
    }

    public void setClientBrand(String brand) {
        clientBrand = brand;
    }

    public StatFileWriter getStatFileWriter() {
        return statWriter;
    }

    public void addChatComponentMessage(IChatComponent chatComponent) {
        mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
    }

    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if (noClip) {
            return false;
        } else {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double) blockpos.getX();
            double d1 = z - (double) blockpos.getZ();

            if (!isOpenBlockSpace(blockpos)) {
                int i = -1;
                double d2 = 9999.0D;

                if (isOpenBlockSpace(blockpos.west()) && d0 < d2) {
                    d2 = d0;
                    i = 0;
                }

                if (isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2) {
                    d2 = 1.0D - d0;
                    i = 1;
                }

                if (isOpenBlockSpace(blockpos.north()) && d1 < d2) {
                    d2 = d1;
                    i = 4;
                }

                if (isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2) {
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0) {
                    motionX = -f;
                }

                if (i == 1) {
                    motionX = f;
                }

                if (i == 4) {
                    motionZ = -f;
                }

                if (i == 5) {
                    motionZ = f;
                }
            }

            return false;
        }
    }

    private boolean isOpenBlockSpace(BlockPos pos) {
        return !worldObj.getBlockState(pos).getBlock().isNormalCube() && !worldObj.getBlockState(pos.up()).getBlock().isNormalCube();
    }

    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        sprintingTicksLeft = sprinting ? 600 : 0;
    }

    public void setXPStats(float currentXP, int maxXP, int level) {
        experience = currentXP;
        experienceTotal = maxXP;
        experienceLevel = level;
    }

    public void addChatMessage(IChatComponent component) {
        mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return permLevel <= 0;
    }

    public BlockPos getPosition() {
        return new BlockPos(posX + 0.5D, posY + 0.5D, posZ + 0.5D);
    }

    public void playSound(String name, float volume, float pitch) {
        worldObj.playSound(posX, posY, posZ, name, volume, pitch, false);
    }

    public boolean isServerWorld() {
        return true;
    }

    public boolean isRidingHorse() {
        return ridingEntity != null && ridingEntity instanceof EntityHorse && ((EntityHorse) ridingEntity).isHorseSaddled();
    }

    public float getHorseJumpPower() {
        return horseJumpPower;
    }

    public void openEditSign(TileEntitySign signTile) {
        mc.displayGuiScreen(new GuiEditSign(signTile));
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic) {
        mc.displayGuiScreen(new GuiCommandBlock(cmdBlockLogic));
    }

    public void displayGUIBook(ItemStack bookStack) {
        Item item = bookStack.getItem();

        if (item == Items.writable_book) {
            mc.displayGuiScreen(new GuiScreenBook(this, bookStack, true));
        }
    }

    public void displayGUIChest(IInventory chestInventory) {
        String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject) chestInventory).getGuiID() : "minecraft:container";

        if ("minecraft:chest".equals(s)) {
            mc.displayGuiScreen(new GuiChest(inventory, chestInventory));
        } else if ("minecraft:hopper".equals(s)) {
            mc.displayGuiScreen(new GuiHopper(inventory, chestInventory));
        } else if ("minecraft:furnace".equals(s)) {
            mc.displayGuiScreen(new GuiFurnace(inventory, chestInventory));
        } else if ("minecraft:brewing_stand".equals(s)) {
            mc.displayGuiScreen(new GuiBrewingStand(inventory, chestInventory));
        } else if ("minecraft:beacon".equals(s)) {
            mc.displayGuiScreen(new GuiBeacon(inventory, chestInventory));
        } else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s)) {
            mc.displayGuiScreen(new GuiChest(inventory, chestInventory));
        } else {
            mc.displayGuiScreen(new GuiDispenser(inventory, chestInventory));
        }
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory) {
        mc.displayGuiScreen(new GuiScreenHorseInventory(inventory, horseInventory, horse));
    }

    public void displayGui(IInteractionObject guiOwner) {
        String s = guiOwner.getGuiID();

        if ("minecraft:crafting_table".equals(s)) {
            mc.displayGuiScreen(new GuiCrafting(inventory, worldObj));
        } else if ("minecraft:enchanting_table".equals(s)) {
            mc.displayGuiScreen(new GuiEnchantment(inventory, worldObj, guiOwner));
        } else if ("minecraft:anvil".equals(s)) {
            mc.displayGuiScreen(new GuiRepair(inventory, worldObj));
        }
    }

    public void displayVillagerTradeGui(IMerchant villager) {
        mc.displayGuiScreen(new GuiMerchant(inventory, villager, worldObj));
    }

    public void onCriticalHit(Entity entityHit) {
        mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
    }

    public void onEnchantmentCritical(Entity entityHit) {
        mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
    }

    public boolean isSneaking() {
        boolean flag = movementInput != null && movementInput.sneak;
        return flag && !sleeping;
    }

    public void updateEntityActionState() {
        super.updateEntityActionState();

        if (isCurrentViewEntity()) {
            moveStrafing = movementInput.moveStrafe;
            moveForward = movementInput.moveForward;
            isJumping = movementInput.jump;
            prevRenderArmYaw = renderArmYaw;
            prevRenderArmPitch = renderArmPitch;
            renderArmPitch = (float) ((double) renderArmPitch + (double) (rotationPitch - renderArmPitch) * 0.5D);
            renderArmYaw = (float) ((double) renderArmYaw + (double) (rotationYaw - renderArmYaw) * 0.5D);
        }
    }

    protected boolean isCurrentViewEntity() {
        return mc.getRenderViewEntity() == this;
    }

    public void onLivingUpdate() {
        if (sprintingTicksLeft > 0) {
            --sprintingTicksLeft;

            if (sprintingTicksLeft == 0) {
                setSprinting(false);
            }
        }

        if (sprintToggleTimer > 0) {
            --sprintToggleTimer;
        }

        prevTimeInPortal = timeInPortal;

        if (inPortal) {
            if (mc.currentScreen != null && !mc.currentScreen.doesGuiPauseGame()) {
                mc.displayGuiScreen(null);
            }

            if (timeInPortal == 0.0F) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), rand.nextFloat() * 0.4F + 0.8F));
            }

            timeInPortal += 0.0125F;

            if (timeInPortal >= 1.0F) {
                timeInPortal = 1.0F;
            }

            inPortal = false;
        } else if (isPotionActive(Potion.confusion) && getActivePotionEffect(Potion.confusion).getDuration() > 60) {
            timeInPortal += 0.006666667F;

            if (timeInPortal > 1.0F) {
                timeInPortal = 1.0F;
            }
        } else {
            if (timeInPortal > 0.0F) {
                timeInPortal -= 0.05F;
            }

            if (timeInPortal < 0.0F) {
                timeInPortal = 0.0F;
            }
        }

        if (timeUntilPortal > 0) {
            --timeUntilPortal;
        }

        boolean flag = movementInput.jump;
        boolean flag1 = movementInput.sneak;
        float f = 0.8F;
        boolean flag2 = movementInput.moveForward >= f;
        movementInput.updatePlayerMoveState();

        if (isUsingItem() && !isRiding()) {
            movementInput.moveStrafe *= 0.2F;
            movementInput.moveForward *= 0.2F;
            sprintToggleTimer = 0;
        }

        pushOutOfBlocks(posX - (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ + (double) width * 0.35D);
        pushOutOfBlocks(posX - (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ - (double) width * 0.35D);
        pushOutOfBlocks(posX + (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ - (double) width * 0.35D);
        pushOutOfBlocks(posX + (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ + (double) width * 0.35D);
        boolean flag3 = (float) getFoodStats().getFoodLevel() > 6.0F || capabilities.allowFlying;

        if (onGround && !flag1 && !flag2 && movementInput.moveForward >= f && !isSprinting() && flag3 && !isUsingItem() && !isPotionActive(Potion.blindness)) {
            if (sprintToggleTimer <= 0 && !mc.gameSettings.keyBindSprint.isKeyDown()) {
                sprintToggleTimer = 7;
            } else {
                setSprinting(true);
            }
        }

        if (!isSprinting() && movementInput.moveForward >= f && flag3 && !isUsingItem() && !isPotionActive(Potion.blindness) && mc.gameSettings.keyBindSprint.isKeyDown()) {
            setSprinting(true);
        }

        if (isSprinting() && (movementInput.moveForward < f || isCollidedHorizontally || !flag3)) {
            setSprinting(false);
        }

        if (capabilities.allowFlying) {
            if (mc.playerController.isSpectatorMode()) {
                if (!capabilities.isFlying) {
                    capabilities.isFlying = true;
                    sendPlayerAbilities();
                }
            } else if (!flag && movementInput.jump) {
                if (flyToggleTimer == 0) {
                    flyToggleTimer = 7;
                } else {
                    capabilities.isFlying = !capabilities.isFlying;
                    sendPlayerAbilities();
                    flyToggleTimer = 0;
                }
            }
        }

        if (capabilities.isFlying && isCurrentViewEntity()) {
            if (movementInput.sneak) {
                motionY -= capabilities.getFlySpeed() * 3.0F;
            }

            if (movementInput.jump) {
                motionY += capabilities.getFlySpeed() * 3.0F;
            }
        }

        if (isRidingHorse()) {
            if (horseJumpPowerCounter < 0) {
                ++horseJumpPowerCounter;

                if (horseJumpPowerCounter == 0) {
                    horseJumpPower = 0.0F;
                }
            }

            if (flag && !movementInput.jump) {
                horseJumpPowerCounter = -10;
                sendHorseJump();
            } else if (!flag && movementInput.jump) {
                horseJumpPowerCounter = 0;
                horseJumpPower = 0.0F;
            } else if (flag) {
                ++horseJumpPowerCounter;

                if (horseJumpPowerCounter < 10) {
                    horseJumpPower = (float) horseJumpPowerCounter * 0.1F;
                } else {
                    horseJumpPower = 0.8F + 2.0F / (float) (horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();

        if (onGround && capabilities.isFlying && !mc.playerController.isSpectatorMode()) {
            capabilities.isFlying = false;
            sendPlayerAbilities();
        }
    }
}
