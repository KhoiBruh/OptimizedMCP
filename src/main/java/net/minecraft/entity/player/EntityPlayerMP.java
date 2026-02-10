package net.minecraft.entity.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Action;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EntityPlayerMP extends EntityPlayer implements ICrafting {
    private static final Logger logger = LogManager.getLogger();
    public final MinecraftServer mcServer;
    public final ItemInWorldManager theItemInWorldManager;
    public final List<ChunkCoordIntPair> loadedChunks = Lists.newLinkedList();
    private final List<Integer> destroyedItemsNetCache = Lists.newLinkedList();
    private final StatisticsFile statsFile;
    public NetHandlerPlayServer playerNetServerHandler;
    public double managedPosX;
    public double managedPosZ;
    public boolean isChangingQuantityOnly;
    public int ping;
    public boolean playerConqueredTheEnd;
    private String translator = "en_US";
    private float combinedHealth = Float.MIN_VALUE;
    private float lastHealth = -1.0E8F;
    private int lastFoodLevel = -99999999;
    private boolean wasHungry = true;
    private int lastExperience = -99999999;
    private int respawnInvulnerabilityTicks = 60;
    private ChatVisibility chatVisibility;
    private boolean chatColours = true;
    private long playerLastActiveTime = System.currentTimeMillis();
    private Entity spectatingEntity = null;
    private int currentWindowId;

    public EntityPlayerMP(MinecraftServer server, WorldServer worldIn, GameProfile profile, ItemInWorldManager interactionManager) {
        super(worldIn, profile);
        interactionManager.thisPlayerMP = this;
        theItemInWorldManager = interactionManager;
        BlockPos blockpos = worldIn.getSpawnPoint();

        if (!worldIn.provider.getHasNoSky() && worldIn.getWorldInfo().getGameType() != WorldSettings.GameType.ADVENTURE) {
            int i = Math.max(5, server.getSpawnProtectionSize() - 6);
            int j = MathHelper.floor(worldIn.getWorldBorder().getClosestDistance(blockpos.getX(), blockpos.getZ()));

            if (j < i) {
                i = j;
            }

            if (j <= 1) {
                i = 1;
            }

            blockpos = worldIn.getTopSolidOrLiquidBlock(blockpos.add(rand.nextInt(i * 2) - i, 0, rand.nextInt(i * 2) - i));
        }

        mcServer = server;
        statsFile = server.getConfigurationManager().getPlayerStatsFile(this);
        stepHeight = 0.0F;
        moveToBlockPosAndAngles(blockpos, 0.0F, 0.0F);

        while (!worldIn.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty() && posY < 255.0D) {
            setPosition(posX, posY + 1.0D, posZ);
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("playerGameType", 99)) {
            if (MinecraftServer.getServer().getForceGamemode()) {
                theItemInWorldManager.setGameType(MinecraftServer.getServer().getGameType());
            } else {
                theItemInWorldManager.setGameType(WorldSettings.GameType.getByID(tagCompund.getInteger("playerGameType")));
            }
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("playerGameType", theItemInWorldManager.getGameType().getID());
    }

    public void addExperienceLevel(int levels) {
        super.addExperienceLevel(levels);
        lastExperience = -1;
    }

    public void removeExperienceLevel(int levels) {
        super.removeExperienceLevel(levels);
        lastExperience = -1;
    }

    public void addSelfToInternalCraftingInventory() {
        openContainer.onCraftGuiOpened(this);
    }

    public void sendEnterCombat() {
        super.sendEnterCombat();
        playerNetServerHandler.sendPacket(new S42PacketCombatEvent(getCombatTracker(), S42PacketCombatEvent.Event.ENTER_COMBAT));
    }

    public void sendEndCombat() {
        super.sendEndCombat();
        playerNetServerHandler.sendPacket(new S42PacketCombatEvent(getCombatTracker(), S42PacketCombatEvent.Event.END_COMBAT));
    }

    public void onUpdate() {
        theItemInWorldManager.updateBlockRemoving();
        --respawnInvulnerabilityTicks;

        if (hurtResistantTime > 0) {
            --hurtResistantTime;
        }

        openContainer.detectAndSendChanges();

        if (!worldObj.isRemote && !openContainer.canInteractWith(this)) {
            closeScreen();
            openContainer = inventoryContainer;
        }

        while (!destroyedItemsNetCache.isEmpty()) {
            int i = Math.min(destroyedItemsNetCache.size(), Integer.MAX_VALUE);
            int[] aint = new int[i];
            Iterator<Integer> iterator = destroyedItemsNetCache.iterator();
            int j = 0;

            while (iterator.hasNext() && j < i) {
                aint[j++] = iterator.next();
                iterator.remove();
            }

            playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(aint));
        }

        if (!loadedChunks.isEmpty()) {
            List<Chunk> list = new ArrayList<>();
            Iterator<ChunkCoordIntPair> iterator1 = loadedChunks.iterator();
            List<TileEntity> list1 = new ArrayList<>();

            while (iterator1.hasNext() && list.size() < 10) {
                ChunkCoordIntPair chunkcoordintpair = iterator1.next();

                if (chunkcoordintpair != null) {
                    if (worldObj.isBlockLoaded(new BlockPos(chunkcoordintpair.chunkXPos << 4, 0, chunkcoordintpair.chunkZPos << 4))) {
                        Chunk chunk = worldObj.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);

                        if (chunk.isPopulated()) {
                            list.add(chunk);
                            list1.addAll(((WorldServer) worldObj).getTileEntitiesIn(chunkcoordintpair.chunkXPos * 16, 0, chunkcoordintpair.chunkZPos * 16, chunkcoordintpair.chunkXPos * 16 + 16, 256, chunkcoordintpair.chunkZPos * 16 + 16));
                            iterator1.remove();
                        }
                    }
                } else {
                    iterator1.remove();
                }
            }

            if (!list.isEmpty()) {
                if (list.size() == 1) {
                    playerNetServerHandler.sendPacket(new S21PacketChunkData(list.getFirst(), true, 65535));
                } else {
                    playerNetServerHandler.sendPacket(new S26PacketMapChunkBulk(list));
                }

                for (TileEntity tileentity : list1) {
                    sendTileEntityUpdate(tileentity);
                }

                for (Chunk chunk1 : list) {
                    getServerForPlayer().getEntityTracker().func_85172_a(this, chunk1);
                }
            }
        }

        Entity entity = getSpectatingEntity();

        if (entity != this) {
            if (!entity.isEntityAlive()) {
                setSpectatingEntity(this);
            } else {
                setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                mcServer.getConfigurationManager().serverUpdateMountedMovingPlayer(this);

                if (isSneaking()) {
                    setSpectatingEntity(this);
                }
            }
        }
    }

    public void onUpdateEntity() {
        try {
            super.onUpdate();

            for (int i = 0; i < inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = inventory.getStackInSlot(i);

                if (itemstack != null && itemstack.getItem().isMap()) {
                    Packet packet = ((ItemMapBase) itemstack.getItem()).createMapDataPacket(itemstack, worldObj, this);

                    if (packet != null) {
                        playerNetServerHandler.sendPacket(packet);
                    }
                }
            }

            if (getHealth() != lastHealth || lastFoodLevel != foodStats.getFoodLevel() || foodStats.getSaturationLevel() == 0.0F != wasHungry) {
                playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(getHealth(), foodStats.getFoodLevel(), foodStats.getSaturationLevel()));
                lastHealth = getHealth();
                lastFoodLevel = foodStats.getFoodLevel();
                wasHungry = foodStats.getSaturationLevel() == 0.0F;
            }

            if (getHealth() + getAbsorptionAmount() != combinedHealth) {
                combinedHealth = getHealth() + getAbsorptionAmount();

                for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.health)) {
                    getWorldScoreboard().getValueFromObjective(getName(), scoreobjective).func_96651_a(List.of(this));
                }
            }

            if (experienceTotal != lastExperience) {
                lastExperience = experienceTotal;
                playerNetServerHandler.sendPacket(new S1FPacketSetExperience(experience, experienceTotal, experienceLevel));
            }

            if (ticksExisted % 20 * 5 == 0 && !statsFile.hasAchievementUnlocked(AchievementList.exploreAllBiomes)) {
                updateBiomesExplored();
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
            addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    protected void updateBiomesExplored() {
        BiomeGenBase biomegenbase = worldObj.getBiomeGenForCoords(new BlockPos(MathHelper.floor(posX), 0, MathHelper.floor(posZ)));
        String s = biomegenbase.biomeName;
        JsonSerializableSet jsonserializableset = statsFile.func_150870_b(AchievementList.exploreAllBiomes);

        if (jsonserializableset == null) {
            jsonserializableset = statsFile.func_150872_a(AchievementList.exploreAllBiomes, new JsonSerializableSet());
        }

        jsonserializableset.add(s);

        if (statsFile.canUnlockAchievement(AchievementList.exploreAllBiomes) && jsonserializableset.size() >= BiomeGenBase.explorationBiomesList.size()) {
            Set<BiomeGenBase> set = Sets.newHashSet(BiomeGenBase.explorationBiomesList);

            for (String s1 : jsonserializableset) {

                set.removeIf(biomegenbase1 -> biomegenbase1.biomeName.equals(s1));

                if (set.isEmpty()) {
                    break;
                }
            }

            if (set.isEmpty()) {
                triggerAchievement(AchievementList.exploreAllBiomes);
            }
        }
    }

    public void onDeath(DamageSource cause) {
        if (worldObj.getGameRules().getBoolean("showDeathMessages")) {
            Team team = getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS) {
                    mcServer.getConfigurationManager().sendMessageToAllTeamMembers(this, getCombatTracker().getDeathMessage());
                } else if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM) {
                    mcServer.getConfigurationManager().sendMessageToTeamOrEvryPlayer(this, getCombatTracker().getDeathMessage());
                }
            } else {
                mcServer.getConfigurationManager().sendChatMsg(getCombatTracker().getDeathMessage());
            }
        }

        if (!worldObj.getGameRules().getBoolean("keepInventory")) {
            inventory.dropAllItems();
        }

        for (ScoreObjective scoreobjective : worldObj.getScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.deathCount)) {
            Score score = getWorldScoreboard().getValueFromObjective(getName(), scoreobjective);
            score.func_96648_a();
        }

        EntityLivingBase entitylivingbase = getAttackingEntity();

        if (entitylivingbase != null) {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.entityEggs.get(EntityList.getEntityID(entitylivingbase));

            if (entitylist$entityegginfo != null) {
                triggerAchievement(entitylist$entityegginfo.field_151513_e);
            }

            entitylivingbase.addToPlayerScore(this, scoreValue);
        }

        triggerAchievement(StatList.deathsStat);
        func_175145_a(StatList.timeSinceDeathStat);
        getCombatTracker().reset();
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            boolean flag = mcServer.isDedicatedServer() && canPlayersAttack() && "fall".equals(source.damageType);

            if (!flag && respawnInvulnerabilityTicks > 0 && source != DamageSource.outOfWorld) {
                return false;
            } else {
                if (source instanceof EntityDamageSource) {
                    Entity entity = source.getEntity();

                    if (entity instanceof EntityPlayer && !canAttackPlayer((EntityPlayer) entity)) {
                        return false;
                    }

                    if (entity instanceof EntityArrow entityarrow) {

                        if (entityarrow.shootingEntity instanceof EntityPlayer && !canAttackPlayer((EntityPlayer) entityarrow.shootingEntity)) {
                            return false;
                        }
                    }
                }

                return super.attackEntityFrom(source, amount);
            }
        }
    }

    public boolean canAttackPlayer(EntityPlayer other) {
        return canPlayersAttack() && super.canAttackPlayer(other);
    }

    private boolean canPlayersAttack() {
        return mcServer.isPVPEnabled();
    }

    public void travelToDimension(int dimensionId) {
        if (dimension == 1 && dimensionId == 1) {
            triggerAchievement(AchievementList.theEnd2);
            worldObj.removeEntity(this);
            playerConqueredTheEnd = true;
            playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(4, 0.0F));
        } else {
            if (dimension == 0 && dimensionId == 1) {
                triggerAchievement(AchievementList.theEnd);
                BlockPos blockpos = mcServer.worldServerForDimension(dimensionId).getSpawnCoordinate();

                if (blockpos != null) {
                    playerNetServerHandler.setPlayerLocation(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 0.0F, 0.0F);
                }

                dimensionId = 1;
            } else {
                triggerAchievement(AchievementList.portal);
            }

            mcServer.getConfigurationManager().transferPlayerToDimension(this, dimensionId);
            lastExperience = -1;
            lastHealth = -1.0F;
            lastFoodLevel = -1;
        }
    }

    public boolean isSpectatedByPlayer(EntityPlayerMP player) {
        return player.isSpectator() ? getSpectatingEntity() == this : (!isSpectator() && super.isSpectatedByPlayer(player));
    }

    private void sendTileEntityUpdate(TileEntity p_147097_1_) {
        if (p_147097_1_ != null) {
            Packet packet = p_147097_1_.getDescriptionPacket();

            if (packet != null) {
                playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    public void onItemPickup(Entity p_71001_1_, int p_71001_2_) {
        super.onItemPickup(p_71001_1_, p_71001_2_);
        openContainer.detectAndSendChanges();
    }

    public Status trySleep(BlockPos bedLocation) {
        Status entityplayer$enumstatus = super.trySleep(bedLocation);

        if (entityplayer$enumstatus == Status.OK) {
            Packet<INetHandlerPlayClient> packet = new S0APacketUseBed(this, bedLocation);
            getServerForPlayer().getEntityTracker().sendToAllTrackingEntity(this, packet);
            playerNetServerHandler.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
            playerNetServerHandler.sendPacket(packet);
        }

        return entityplayer$enumstatus;
    }

    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        if (isPlayerSleeping()) {
            getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(this, 2));
        }

        super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);

        if (playerNetServerHandler != null) {
            playerNetServerHandler.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    public void mountEntity(Entity entityIn) {
        Entity entity = ridingEntity;
        super.mountEntity(entityIn);

        if (entityIn != entity) {
            playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(0, this, ridingEntity));
            playerNetServerHandler.setPlayerLocation(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos) {
    }

    public void handleFalling(double p_71122_1_, boolean p_71122_3_) {
        int i = MathHelper.floor(posX);
        int j = MathHelper.floor(posY - 0.20000000298023224D);
        int k = MathHelper.floor(posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        Block block = worldObj.getBlockState(blockpos).getBlock();

        if (block.getMaterial() == Material.air) {
            Block block1 = worldObj.getBlockState(blockpos.down()).getBlock();

            if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                blockpos = blockpos.down();
                block = worldObj.getBlockState(blockpos).getBlock();
            }
        }

        super.updateFallState(p_71122_1_, p_71122_3_, block, blockpos);
    }

    public void openEditSign(TileEntitySign signTile) {
        signTile.setPlayer(this);
        playerNetServerHandler.sendPacket(new S36PacketSignEditorOpen(signTile.getPos()));
    }

    private void getNextWindowId() {
        currentWindowId = currentWindowId % 100 + 1;
    }

    public void displayGui(IInteractionObject guiOwner) {
        getNextWindowId();
        playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(currentWindowId, guiOwner.getGuiID(), guiOwner.getDisplayName()));
        openContainer = guiOwner.createContainer(inventory, this);
        openContainer.windowId = currentWindowId;
        openContainer.onCraftGuiOpened(this);
    }

    public void displayGUIChest(IInventory chestInventory) {
        if (openContainer != inventoryContainer) {
            closeScreen();
        }

        if (chestInventory instanceof ILockableContainer ilockablecontainer) {

            if (ilockablecontainer.isLocked() && !canOpen(ilockablecontainer.getLockCode()) && !isSpectator()) {
                playerNetServerHandler.sendPacket(new S02PacketChat(new ChatComponentTranslation("container.isLocked", chestInventory.getDisplayName()), (byte) 2));
                playerNetServerHandler.sendPacket(new S29PacketSoundEffect("random.door_close", posX, posY, posZ, 1.0F, 1.0F));
                return;
            }
        }

        getNextWindowId();

        if (chestInventory instanceof IInteractionObject) {
            playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(currentWindowId, ((IInteractionObject) chestInventory).getGuiID(), chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
            openContainer = ((IInteractionObject) chestInventory).createContainer(inventory, this);
        } else {
            playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(currentWindowId, "minecraft:container", chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
            openContainer = new ContainerChest(inventory, chestInventory, this);
        }

        openContainer.windowId = currentWindowId;
        openContainer.onCraftGuiOpened(this);
    }

    public void displayVillagerTradeGui(IMerchant villager) {
        getNextWindowId();
        openContainer = new ContainerMerchant(inventory, villager, worldObj);
        openContainer.windowId = currentWindowId;
        openContainer.onCraftGuiOpened(this);
        IInventory iinventory = ((ContainerMerchant) openContainer).getMerchantInventory();
        IChatComponent ichatcomponent = villager.getDisplayName();
        playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(currentWindowId, "minecraft:villager", ichatcomponent, iinventory.getSizeInventory()));
        MerchantRecipeList merchantrecipelist = villager.getRecipes(this);

        if (merchantrecipelist != null) {
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(currentWindowId);
            merchantrecipelist.writeToBuf(packetbuffer);
            playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|TrList", packetbuffer));
        }
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory) {
        if (openContainer != inventoryContainer) {
            closeScreen();
        }

        getNextWindowId();
        playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(currentWindowId, "EntityHorse", horseInventory.getDisplayName(), horseInventory.getSizeInventory(), horse.getEntityId()));
        openContainer = new ContainerHorseInventory(inventory, horseInventory, horse, this);
        openContainer.windowId = currentWindowId;
        openContainer.onCraftGuiOpened(this);
    }

    public void displayGUIBook(ItemStack bookStack) {
        Item item = bookStack.getItem();

        if (item == Items.written_book) {
            playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|BOpen", new PacketBuffer(Unpooled.buffer())));
        }
    }

    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if (!(containerToSend.getSlot(slotInd) instanceof SlotCrafting)) {
            if (!isChangingQuantityOnly) {
                playerNetServerHandler.sendPacket(new S2FPacketSetSlot(containerToSend.windowId, slotInd, stack));
            }
        }
    }

    public void sendContainerToPlayer(Container p_71120_1_) {
        updateCraftingInventory(p_71120_1_, p_71120_1_.getInventory());
    }

    public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList) {
        playerNetServerHandler.sendPacket(new S30PacketWindowItems(containerToSend.windowId, itemsList));
        playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, inventory.getItemStack()));
    }

    public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue) {
        playerNetServerHandler.sendPacket(new S31PacketWindowProperty(containerIn.windowId, varToUpdate, newValue));
    }

    public void sendAllWindowProperties(Container p_175173_1_, IInventory p_175173_2_) {
        for (int i = 0; i < p_175173_2_.getFieldCount(); ++i) {
            playerNetServerHandler.sendPacket(new S31PacketWindowProperty(p_175173_1_.windowId, i, p_175173_2_.getField(i)));
        }
    }

    public void closeScreen() {
        playerNetServerHandler.sendPacket(new S2EPacketCloseWindow(openContainer.windowId));
        closeContainer();
    }

    public void updateHeldItem() {
        if (!isChangingQuantityOnly) {
            playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, inventory.getItemStack()));
        }
    }

    public void closeContainer() {
        openContainer.onContainerClosed(this);
        openContainer = inventoryContainer;
    }

    public void setEntityActionState(float p_110430_1_, float p_110430_2_, boolean p_110430_3_, boolean sneaking) {
        if (ridingEntity != null) {
            if (p_110430_1_ >= -1.0F && p_110430_1_ <= 1.0F) {
                moveStrafing = p_110430_1_;
            }

            if (p_110430_2_ >= -1.0F && p_110430_2_ <= 1.0F) {
                moveForward = p_110430_2_;
            }

            isJumping = p_110430_3_;
            setSneaking(sneaking);
        }
    }

    public void addStat(StatBase stat, int amount) {
        if (stat != null) {
            statsFile.increaseStat(this, stat, amount);

            for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(stat.getCriteria())) {
                getWorldScoreboard().getValueFromObjective(getName(), scoreobjective).increseScore(amount);
            }

            if (statsFile.func_150879_e()) {
                statsFile.func_150876_a(this);
            }
        }
    }

    public void func_175145_a(StatBase p_175145_1_) {
        if (p_175145_1_ != null) {
            statsFile.unlockAchievement(this, p_175145_1_, 0);

            for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(p_175145_1_.getCriteria())) {
                getWorldScoreboard().getValueFromObjective(getName(), scoreobjective).setScorePoints(0);
            }

            if (statsFile.func_150879_e()) {
                statsFile.func_150876_a(this);
            }
        }
    }

    public void mountEntityAndWakeUp() {
        if (riddenByEntity != null) {
            riddenByEntity.mountEntity(this);
        }

        if (sleeping) {
            wakeUpPlayer(true, false, false);
        }
    }

    public void setPlayerHealthUpdated() {
        lastHealth = -1.0E8F;
    }

    public void addChatComponentMessage(IChatComponent chatComponent) {
        playerNetServerHandler.sendPacket(new S02PacketChat(chatComponent));
    }

    protected void onItemUseFinish() {
        playerNetServerHandler.sendPacket(new S19PacketEntityStatus(this, (byte) 9));
        super.onItemUseFinish();
    }

    public void setItemInUse(ItemStack stack, int duration) {
        super.setItemInUse(stack, duration);

        if (stack != null && stack.getItem() != null && stack.getItem().getItemUseAction(stack) == Action.EAT) {
            getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(this, 3));
        }
    }

    public void clonePlayer(EntityPlayer oldPlayer, boolean respawnFromEnd) {
        super.clonePlayer(oldPlayer, respawnFromEnd);
        lastExperience = -1;
        lastHealth = -1.0F;
        lastFoodLevel = -1;
        destroyedItemsNetCache.addAll(((EntityPlayerMP) oldPlayer).destroyedItemsNetCache);
    }

    protected void onNewPotionEffect(PotionEffect id) {
        super.onNewPotionEffect(id);
        playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(getEntityId(), id));
    }

    protected void onChangedPotionEffect(PotionEffect id, boolean p_70695_2_) {
        super.onChangedPotionEffect(id, p_70695_2_);
        playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(getEntityId(), id));
    }

    protected void onFinishedPotionEffect(PotionEffect effect) {
        super.onFinishedPotionEffect(effect);
        playerNetServerHandler.sendPacket(new S1EPacketRemoveEntityEffect(getEntityId(), effect));
    }

    public void setPositionAndUpdate(double x, double y, double z) {
        playerNetServerHandler.setPlayerLocation(x, y, z, rotationYaw, rotationPitch);
    }

    public void onCriticalHit(Entity entityHit) {
        getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(entityHit, 4));
    }

    public void onEnchantmentCritical(Entity entityHit) {
        getServerForPlayer().getEntityTracker().func_151248_b(this, new S0BPacketAnimation(entityHit, 5));
    }

    public void sendPlayerAbilities() {
        if (playerNetServerHandler != null) {
            playerNetServerHandler.sendPacket(new S39PacketPlayerAbilities(capabilities));
            updatePotionMetadata();
        }
    }

    public WorldServer getServerForPlayer() {
        return (WorldServer) worldObj;
    }

    public void setGameType(WorldSettings.GameType gameType) {
        theItemInWorldManager.setGameType(gameType);
        playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(3, (float) gameType.getID()));

        if (gameType == WorldSettings.GameType.SPECTATOR) {
            mountEntity(null);
        } else {
            setSpectatingEntity(this);
        }

        sendPlayerAbilities();
        markPotionsDirty();
    }

    public boolean isSpectator() {
        return theItemInWorldManager.getGameType() == WorldSettings.GameType.SPECTATOR;
    }

    public void addChatMessage(IChatComponent component) {
        playerNetServerHandler.sendPacket(new S02PacketChat(component));
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        if ("seed".equals(commandName) && !mcServer.isDedicatedServer()) {
            return true;
        } else if (!"tell".equals(commandName) && !"help".equals(commandName) && !"me".equals(commandName) && !"trigger".equals(commandName)) {
            if (mcServer.getConfigurationManager().canSendCommands(getGameProfile())) {
                UserListOpsEntry userlistopsentry = mcServer.getConfigurationManager().getOppedPlayers().getEntry(getGameProfile());
                return userlistopsentry != null ? userlistopsentry.getPermissionLevel() >= permLevel : mcServer.getOpPermissionLevel() >= permLevel;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public String getPlayerIP() {
        String s = playerNetServerHandler.netManager.getRemoteAddress().toString();
        s = s.substring(s.indexOf('/') + 1);
        s = s.substring(0, s.indexOf(':'));
        return s;
    }

    public void handleClientSettings(C15PacketClientSettings packetIn) {
        translator = packetIn.getLang();
        chatVisibility = packetIn.getChatVisibility();
        chatColours = packetIn.isColorsEnabled();
        getDataWatcher().updateObject(10, (byte) packetIn.getModelPartFlags());
    }

    public ChatVisibility getChatVisibility() {
        return chatVisibility;
    }

    public void loadResourcePack(String url, String hash) {
        playerNetServerHandler.sendPacket(new S48PacketResourcePackSend(url, hash));
    }

    public void markPlayerActive() {
        playerLastActiveTime = MinecraftServer.getCurrentTimeMillis();
    }

    public StatisticsFile getStatFile() {
        return statsFile;
    }

    public void removeEntity(Entity p_152339_1_) {
        if (p_152339_1_ instanceof EntityPlayer) {
            playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(p_152339_1_.getEntityId()));
        } else {
            destroyedItemsNetCache.add(p_152339_1_.getEntityId());
        }
    }

    protected void updatePotionMetadata() {
        if (isSpectator()) {
            resetPotionEffectMetadata();
            setInvisible(true);
        } else {
            super.updatePotionMetadata();
        }

        getServerForPlayer().getEntityTracker().func_180245_a(this);
    }

    public Entity getSpectatingEntity() {
        return spectatingEntity == null ? this : spectatingEntity;
    }

    public void setSpectatingEntity(Entity entityToSpectate) {
        Entity entity = getSpectatingEntity();
        spectatingEntity = entityToSpectate == null ? this : entityToSpectate;

        if (entity != spectatingEntity) {
            playerNetServerHandler.sendPacket(new S43PacketCamera(spectatingEntity));
            setPositionAndUpdate(spectatingEntity.posX, spectatingEntity.posY, spectatingEntity.posZ);
        }
    }

    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        if (theItemInWorldManager.getGameType() == WorldSettings.GameType.SPECTATOR) {
            setSpectatingEntity(targetEntity);
        } else {
            super.attackTargetEntityWithCurrentItem(targetEntity);
        }
    }

    public long getLastActiveTime() {
        return playerLastActiveTime;
    }

    public IChatComponent getTabListDisplayName() {
        return null;
    }
}
