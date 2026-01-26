package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public final class ItemStack {
    public static final DecimalFormat DECIMALFORMAT = new DecimalFormat("#.###");
    public int stackSize;
    public int animationsToGo;
    private Item item;
    private NBTTagCompound stackTagCompound;
    private int itemDamage;
    private EntityItemFrame itemFrame;
    private Block canDestroyCacheBlock;
    private boolean canDestroyCacheResult;
    private Block canPlaceOnCacheBlock;
    private boolean canPlaceOnCacheResult;

    public ItemStack(Block blockIn) {
        this(blockIn, 1);
    }

    public ItemStack(Block blockIn, int amount) {
        this(blockIn, amount, 0);
    }

    public ItemStack(Block blockIn, int amount, int meta) {
        this(Item.getItemFromBlock(blockIn), amount, meta);
    }

    public ItemStack(Item itemIn) {
        this(itemIn, 1);
    }

    public ItemStack(Item itemIn, int amount) {
        this(itemIn, amount, 0);
    }

    public ItemStack(Item itemIn, int amount, int meta) {
        canDestroyCacheBlock = null;
        canDestroyCacheResult = false;
        canPlaceOnCacheBlock = null;
        canPlaceOnCacheResult = false;
        item = itemIn;
        stackSize = amount;
        itemDamage = meta;

        if (itemDamage < 0) {
            itemDamage = 0;
        }
    }

    private ItemStack() {
        canDestroyCacheBlock = null;
        canDestroyCacheResult = false;
        canPlaceOnCacheBlock = null;
        canPlaceOnCacheResult = false;
    }

    public static ItemStack loadItemStackFromNBT(NBTTagCompound nbt) {
        ItemStack itemstack = new ItemStack();
        itemstack.readFromNBT(nbt);
        return itemstack.item != null ? itemstack : null;
    }

    public static boolean areItemStackTagsEqual(ItemStack stackA, ItemStack stackB) {
        return stackA == null && stackB == null || (stackA != null && stackB != null && ((stackA.stackTagCompound != null || stackB.stackTagCompound == null) && (stackA.stackTagCompound == null || stackA.stackTagCompound.equals(stackB.stackTagCompound))));
    }

    public static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        return stackA == null && stackB == null || (stackA != null && stackB != null && stackA.isItemStackEqual(stackB));
    }

    public static boolean areItemsEqual(ItemStack stackA, ItemStack stackB) {
        return stackA == null && stackB == null || (stackA != null && stackA.isItemEqual(stackB));
    }

    public static ItemStack copyItemStack(ItemStack stack) {
        return stack == null ? null : stack.copy();
    }

    public ItemStack splitStack(int amount) {
        ItemStack itemstack = new ItemStack(item, amount, itemDamage);

        if (stackTagCompound != null) {
            itemstack.stackTagCompound = (NBTTagCompound) stackTagCompound.copy();
        }

        stackSize -= amount;
        return itemstack;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item newItem) {
        item = newItem;
    }

    public boolean onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        boolean flag = item.onItemUse(this, playerIn, worldIn, pos, side, hitX, hitY, hitZ);

        if (flag) {
            playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(item)]);
        }

        return flag;
    }

    public float getStrVsBlock(Block blockIn) {
        return item.getStrVsBlock(this, blockIn);
    }

    public ItemStack useItemRightClick(World worldIn, EntityPlayer playerIn) {
        return item.onItemRightClick(this, worldIn, playerIn);
    }

    public ItemStack onItemUseFinish(World worldIn, EntityPlayer playerIn) {
        return item.onItemUseFinish(this, worldIn, playerIn);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        ResourceLocation resourcelocation = Item.itemRegistry.getNameForObject(item);
        nbt.setString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
        nbt.setByte("Count", (byte) stackSize);
        nbt.setShort("Damage", (short) itemDamage);

        if (stackTagCompound != null) {
            nbt.setTag("tag", stackTagCompound);
        }

        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("id", 8)) {
            item = Item.getByNameOrId(nbt.getString("id"));
        } else {
            item = Item.getItemById(nbt.getShort("id"));
        }

        stackSize = nbt.getByte("Count");
        itemDamage = nbt.getShort("Damage");

        if (itemDamage < 0) {
            itemDamage = 0;
        }

        if (nbt.hasKey("tag", 10)) {
            stackTagCompound = nbt.getCompoundTag("tag");

            if (item != null) {
                item.updateItemStackNBT(stackTagCompound);
            }
        }
    }

    public int getMaxStackSize() {
        return item.getItemStackLimit();
    }

    public boolean isStackable() {
        return getMaxStackSize() > 1 && (!isItemStackDamageable() || !isItemDamaged());
    }

    public boolean isItemStackDamageable() {
        return item != null && (item.getMaxDamage() > 0 && (!hasTagCompound() || !stackTagCompound.getBoolean("Unbreakable")));
    }

    public boolean getHasSubtypes() {
        return item.getHasSubtypes();
    }

    public boolean isItemDamaged() {
        return isItemStackDamageable() && itemDamage > 0;
    }

    public int getItemDamage() {
        return itemDamage;
    }

    public void setItemDamage(int meta) {
        itemDamage = meta;

        if (itemDamage < 0) {
            itemDamage = 0;
        }
    }

    public int getMetadata() {
        return itemDamage;
    }

    public int getMaxDamage() {
        return item.getMaxDamage();
    }

    public boolean attemptDamageItem(int amount, Random rand) {
        if (!isItemStackDamageable()) {
            return false;
        } else {
            if (amount > 0) {
                int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, this);
                int j = 0;

                for (int k = 0; i > 0 && k < amount; ++k) {
                    if (EnchantmentDurability.negateDamage(this, i, rand)) {
                        ++j;
                    }
                }

                amount -= j;

                if (amount <= 0) {
                    return false;
                }
            }

            itemDamage += amount;
            return itemDamage > getMaxDamage();
        }
    }

    public void damageItem(int amount, EntityLivingBase entityIn) {
        if (!(entityIn instanceof EntityPlayer) || !((EntityPlayer) entityIn).capabilities.isCreativeMode) {
            if (isItemStackDamageable()) {
                if (attemptDamageItem(amount, entityIn.getRNG())) {
                    entityIn.renderBrokenItemStack(this);
                    --stackSize;

                    if (entityIn instanceof EntityPlayer entityplayer) {
                        entityplayer.triggerAchievement(StatList.objectBreakStats[Item.getIdFromItem(item)]);

                        if (stackSize == 0 && item instanceof ItemBow) {
                            entityplayer.destroyCurrentEquippedItem();
                        }
                    }

                    if (stackSize < 0) {
                        stackSize = 0;
                    }

                    itemDamage = 0;
                }
            }
        }
    }

    public void hitEntity(EntityLivingBase entityIn, EntityPlayer playerIn) {
        boolean flag = item.hitEntity(this, entityIn, playerIn);

        if (flag) {
            playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(item)]);
        }
    }

    public void onBlockDestroyed(World worldIn, Block blockIn, BlockPos pos, EntityPlayer playerIn) {
        boolean flag = item.onBlockDestroyed(this, worldIn, blockIn, pos, playerIn);

        if (flag) {
            playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(item)]);
        }
    }

    public boolean canHarvestBlock(Block blockIn) {
        return item.canHarvestBlock(blockIn);
    }

    public boolean interactWithEntity(EntityPlayer playerIn, EntityLivingBase entityIn) {
        return item.itemInteractionForEntity(this, playerIn, entityIn);
    }

    public ItemStack copy() {
        ItemStack itemstack = new ItemStack(item, stackSize, itemDamage);

        if (stackTagCompound != null) {
            itemstack.stackTagCompound = (NBTTagCompound) stackTagCompound.copy();
        }

        return itemstack;
    }

    private boolean isItemStackEqual(ItemStack other) {
        return stackSize == other.stackSize && (item == other.item && (itemDamage == other.itemDamage && ((stackTagCompound != null || other.stackTagCompound == null) && (stackTagCompound == null || stackTagCompound.equals(other.stackTagCompound)))));
    }

    public boolean isItemEqual(ItemStack other) {
        return other != null && item == other.item && itemDamage == other.itemDamage;
    }

    public String getUnlocalizedName() {
        return item.getUnlocalizedName(this);
    }

    public String toString() {
        return stackSize + "x" + item.getUnlocalizedName() + "@" + itemDamage;
    }

    public void updateAnimation(World worldIn, Entity entityIn, int inventorySlot, boolean isCurrentItem) {
        if (animationsToGo > 0) {
            --animationsToGo;
        }

        item.onUpdate(this, worldIn, entityIn, inventorySlot, isCurrentItem);
    }

    public void onCrafting(World worldIn, EntityPlayer playerIn, int amount) {
        playerIn.addStat(StatList.objectCraftStats[Item.getIdFromItem(item)], amount);
        item.onCreated(this, worldIn, playerIn);
    }

    public boolean getIsItemStackEqual(ItemStack p_179549_1_) {
        return isItemStackEqual(p_179549_1_);
    }

    public int getMaxItemUseDuration() {
        return item.getMaxItemUseDuration(this);
    }

    public EnumAction getItemUseAction() {
        return item.getItemUseAction(this);
    }

    public void onPlayerStoppedUsing(World worldIn, EntityPlayer playerIn, int timeLeft) {
        item.onPlayerStoppedUsing(this, worldIn, playerIn, timeLeft);
    }

    public boolean hasTagCompound() {
        return stackTagCompound != null;
    }

    public NBTTagCompound getTagCompound() {
        return stackTagCompound;
    }

    public void setTagCompound(NBTTagCompound nbt) {
        stackTagCompound = nbt;
    }

    public NBTTagCompound getSubCompound(String key, boolean create) {
        if (stackTagCompound != null && stackTagCompound.hasKey(key, 10)) {
            return stackTagCompound.getCompoundTag(key);
        } else if (create) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            setTagInfo(key, nbttagcompound);
            return nbttagcompound;
        } else {
            return null;
        }
    }

    public NBTTagList getEnchantmentTagList() {
        return stackTagCompound == null ? null : stackTagCompound.getTagList("ench", 10);
    }

    public String getDisplayName() {
        String s = item.getItemStackDisplayName(this);

        if (stackTagCompound != null && stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");

            if (nbttagcompound.hasKey("Name", 8)) {
                s = nbttagcompound.getString("Name");
            }
        }

        return s;
    }

    public ItemStack setStackDisplayName(String displayName) {
        if (stackTagCompound == null) {
            stackTagCompound = new NBTTagCompound();
        }

        if (!stackTagCompound.hasKey("display", 10)) {
            stackTagCompound.setTag("display", new NBTTagCompound());
        }

        stackTagCompound.getCompoundTag("display").setString("Name", displayName);
        return this;
    }

    public void clearCustomName() {
        if (stackTagCompound != null) {
            if (stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");
                nbttagcompound.removeTag("Name");

                if (nbttagcompound.hasNoTags()) {
                    stackTagCompound.removeTag("display");

                    if (stackTagCompound.hasNoTags()) {
                        stackTagCompound = null;
                    }
                }
            }
        }
    }

    public boolean hasDisplayName() {
        return stackTagCompound != null && (stackTagCompound.hasKey("display", 10) && stackTagCompound.getCompoundTag("display").hasKey("Name", 8));
    }

    public List<String> getTooltip(EntityPlayer playerIn, boolean advanced) {
        List<String> list = Lists.newArrayList();
        String s = getDisplayName();

        if (hasDisplayName()) {
            s = EnumChatFormatting.ITALIC + s;
        }

        s = s + EnumChatFormatting.RESET;

        if (advanced) {
            String s1 = "";

            if (!s.isEmpty()) {
                s = s + " (";
                s1 = ")";
            }

            int i = Item.getIdFromItem(item);

            if (getHasSubtypes()) {
                s = s + String.format("#%04d/%d%s", i, itemDamage, s1);
            } else {
                s = s + String.format("#%04d%s", i, s1);
            }
        } else if (!hasDisplayName() && item == Items.filled_map) {
            s = s + " #" + itemDamage;
        }

        list.add(s);
        int i1 = 0;

        if (hasTagCompound() && stackTagCompound.hasKey("HideFlags", 99)) {
            i1 = stackTagCompound.getInteger("HideFlags");
        }

        if ((i1 & 32) == 0) {
            item.addInformation(this, playerIn, list, advanced);
        }

        if (hasTagCompound()) {
            if ((i1 & 1) == 0) {
                NBTTagList nbttaglist = getEnchantmentTagList();

                if (nbttaglist != null) {
                    for (int j = 0; j < nbttaglist.tagCount(); ++j) {
                        int k = nbttaglist.getCompoundTagAt(j).getShort("id");
                        int l = nbttaglist.getCompoundTagAt(j).getShort("lvl");

                        if (Enchantment.getEnchantmentById(k) != null) {
                            list.add(Enchantment.getEnchantmentById(k).getTranslatedName(l));
                        }
                    }
                }
            }

            if (stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");

                if (nbttagcompound.hasKey("color", 3)) {
                    if (advanced) {
                        list.add("Color: #" + Integer.toHexString(nbttagcompound.getInteger("color")).toUpperCase());
                    } else {
                        list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.dyed"));
                    }
                }

                if (nbttagcompound.getTagId("Lore") == 9) {
                    NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

                    if (nbttaglist1.tagCount() > 0) {
                        for (int j1 = 0; j1 < nbttaglist1.tagCount(); ++j1) {
                            list.add(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + nbttaglist1.getStringTagAt(j1));
                        }
                    }
                }
            }
        }

        Multimap<String, AttributeModifier> multimap = getAttributeModifiers();

        if (!multimap.isEmpty() && (i1 & 2) == 0) {
            list.add("");

            for (Entry<String, AttributeModifier> entry : multimap.entries()) {
                AttributeModifier attributemodifier = entry.getValue();
                double d0 = attributemodifier.getAmount();

                if (attributemodifier.getID() == Item.itemModifierUUID) {
                    d0 += EnchantmentHelper.getModifierForCreature(this, EnumCreatureAttribute.UNDEFINED);
                }

                double d1;

                if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
                    d1 = d0;
                } else {
                    d1 = d0 * 100.0D;
                }

                if (d0 > 0.0D) {
                    list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier.getOperation(), new Object[]{DECIMALFORMAT.format(d1), StatCollector.translateToLocal("attribute.name." + entry.getKey())}));
                } else if (d0 < 0.0D) {
                    d1 = d1 * -1.0D;
                    list.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted("attribute.modifier.take." + attributemodifier.getOperation(), new Object[]{DECIMALFORMAT.format(d1), StatCollector.translateToLocal("attribute.name." + entry.getKey())}));
                }
            }
        }

        if (hasTagCompound() && stackTagCompound.getBoolean("Unbreakable") && (i1 & 4) == 0) {
            list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("item.unbreakable"));
        }

        if (hasTagCompound() && stackTagCompound.hasKey("CanDestroy", 9) && (i1 & 8) == 0) {
            NBTTagList nbttaglist2 = stackTagCompound.getTagList("CanDestroy", 8);

            if (nbttaglist2.tagCount() > 0) {
                list.add("");
                list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.canBreak"));

                for (int k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
                    Block block = Block.getBlockFromName(nbttaglist2.getStringTagAt(k1));

                    if (block != null) {
                        list.add(EnumChatFormatting.DARK_GRAY + block.getLocalizedName());
                    } else {
                        list.add(EnumChatFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (hasTagCompound() && stackTagCompound.hasKey("CanPlaceOn", 9) && (i1 & 16) == 0) {
            NBTTagList nbttaglist3 = stackTagCompound.getTagList("CanPlaceOn", 8);

            if (nbttaglist3.tagCount() > 0) {
                list.add("");
                list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.canPlace"));

                for (int l1 = 0; l1 < nbttaglist3.tagCount(); ++l1) {
                    Block block1 = Block.getBlockFromName(nbttaglist3.getStringTagAt(l1));

                    if (block1 != null) {
                        list.add(EnumChatFormatting.DARK_GRAY + block1.getLocalizedName());
                    } else {
                        list.add(EnumChatFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (advanced) {
            if (isItemDamaged()) {
                list.add("Durability: " + (getMaxDamage() - itemDamage) + " / " + getMaxDamage());
            }

            list.add(EnumChatFormatting.DARK_GRAY + Item.itemRegistry.getNameForObject(item).toString());

            if (hasTagCompound()) {
                list.add(EnumChatFormatting.DARK_GRAY + "NBT: " + stackTagCompound.getKeySet().size() + " tag(s)");
            }
        }

        return list;
    }

    public boolean hasEffect() {
        return item.hasEffect(this);
    }

    public EnumRarity getRarity() {
        return item.getRarity(this);
    }

    public boolean isItemEnchantable() {
        return item.isItemTool(this) && !isItemEnchanted();
    }

    public void addEnchantment(Enchantment ench, int level) {
        if (stackTagCompound == null) {
            stackTagCompound = new NBTTagCompound();
        }

        if (!stackTagCompound.hasKey("ench", 9)) {
            stackTagCompound.setTag("ench", new NBTTagList());
        }

        NBTTagList nbttaglist = stackTagCompound.getTagList("ench", 10);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setShort("id", (short) ench.effectId);
        nbttagcompound.setShort("lvl", (byte) level);
        nbttaglist.appendTag(nbttagcompound);
    }

    public boolean isItemEnchanted() {
        return stackTagCompound != null && stackTagCompound.hasKey("ench", 9);
    }

    public void setTagInfo(String key, NBTBase value) {
        if (stackTagCompound == null) {
            stackTagCompound = new NBTTagCompound();
        }

        stackTagCompound.setTag(key, value);
    }

    public boolean canEditBlocks() {
        return item.canItemEditBlocks();
    }

    public boolean isOnItemFrame() {
        return itemFrame != null;
    }

    public EntityItemFrame getItemFrame() {
        return itemFrame;
    }

    public void setItemFrame(EntityItemFrame frame) {
        itemFrame = frame;
    }

    public int getRepairCost() {
        return hasTagCompound() && stackTagCompound.hasKey("RepairCost", 3) ? stackTagCompound.getInteger("RepairCost") : 0;
    }

    public void setRepairCost(int cost) {
        if (!hasTagCompound()) {
            stackTagCompound = new NBTTagCompound();
        }

        stackTagCompound.setInteger("RepairCost", cost);
    }

    public Multimap<String, AttributeModifier> getAttributeModifiers() {
        Multimap<String, AttributeModifier> multimap;

        if (hasTagCompound() && stackTagCompound.hasKey("AttributeModifiers", 9)) {
            multimap = HashMultimap.create();
            NBTTagList nbttaglist = stackTagCompound.getTagList("AttributeModifiers", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);

                if (attributemodifier != null && attributemodifier.getID().getLeastSignificantBits() != 0L && attributemodifier.getID().getMostSignificantBits() != 0L) {
                    multimap.put(nbttagcompound.getString("AttributeName"), attributemodifier);
                }
            }
        } else {
            multimap = item.getItemAttributeModifiers();
        }

        return multimap;
    }

    public IChatComponent getChatComponent() {
        ChatComponentText chatcomponenttext = new ChatComponentText(getDisplayName());

        if (hasDisplayName()) {
            chatcomponenttext.getChatStyle().setItalic(Boolean.TRUE);
        }

        IChatComponent ichatcomponent = (new ChatComponentText("[")).appendSibling(chatcomponenttext).appendText("]");

        if (item != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            writeToNBT(nbttagcompound);
            ichatcomponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ChatComponentText(nbttagcompound.toString())));
            ichatcomponent.getChatStyle().setColor(getRarity().rarityColor);
        }

        return ichatcomponent;
    }

    public boolean canDestroy(Block blockIn) {
        if (blockIn == canDestroyCacheBlock) {
            return canDestroyCacheResult;
        } else {
            canDestroyCacheBlock = blockIn;

            if (hasTagCompound() && stackTagCompound.hasKey("CanDestroy", 9)) {
                NBTTagList nbttaglist = stackTagCompound.getTagList("CanDestroy", 8);

                for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                    Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));

                    if (block == blockIn) {
                        canDestroyCacheResult = true;
                        return true;
                    }
                }
            }

            canDestroyCacheResult = false;
            return false;
        }
    }

    public boolean canPlaceOn(Block blockIn) {
        if (blockIn == canPlaceOnCacheBlock) {
            return canPlaceOnCacheResult;
        } else {
            canPlaceOnCacheBlock = blockIn;

            if (hasTagCompound() && stackTagCompound.hasKey("CanPlaceOn", 9)) {
                NBTTagList nbttaglist = stackTagCompound.getTagList("CanPlaceOn", 8);

                for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                    Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));

                    if (block == blockIn) {
                        canPlaceOnCacheResult = true;
                        return true;
                    }
                }
            }

            canPlaceOnCacheResult = false;
            return false;
        }
    }
}
