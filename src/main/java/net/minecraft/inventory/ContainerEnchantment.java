package net.minecraft.inventory;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ContainerEnchantment extends Container {
    public IInventory tableInventory;
    public int xpSeed;
    public int[] enchantLevels;
    public int[] enchantmentIds;
    private final World worldPointer;
    private final BlockPos position;
    private final Random rand;

    public ContainerEnchantment(InventoryPlayer playerInv, World worldIn) {
        this(playerInv, worldIn, BlockPos.ORIGIN);
    }

    public ContainerEnchantment(InventoryPlayer playerInv, World worldIn, BlockPos pos) {
        tableInventory = new InventoryBasic("Enchant", true, 2) {
            public int getInventoryStackLimit() {
                return 64;
            }

            public void markDirty() {
                super.markDirty();
                onCraftMatrixChanged(this);
            }
        };
        rand = new Random();
        enchantLevels = new int[3];
        enchantmentIds = new int[]{-1, -1, -1};
        worldPointer = worldIn;
        position = pos;
        xpSeed = playerInv.player.getXPSeed();
        addSlotToContainer(new Slot(tableInventory, 0, 15, 47) {
            public boolean isItemValid(ItemStack stack) {
                return true;
            }

            public int getSlotStackLimit() {
                return 1;
            }
        });
        addSlotToContainer(new Slot(tableInventory, 1, 35, 47) {
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.dye && EnumDyeColor.byDyeDamage(stack.getMetadata()) == EnumDyeColor.BLUE;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    public void onCraftGuiOpened(ICrafting listener) {
        super.onCraftGuiOpened(listener);
        listener.sendProgressBarUpdate(this, 0, enchantLevels[0]);
        listener.sendProgressBarUpdate(this, 1, enchantLevels[1]);
        listener.sendProgressBarUpdate(this, 2, enchantLevels[2]);
        listener.sendProgressBarUpdate(this, 3, xpSeed & -16);
        listener.sendProgressBarUpdate(this, 4, enchantmentIds[0]);
        listener.sendProgressBarUpdate(this, 5, enchantmentIds[1]);
        listener.sendProgressBarUpdate(this, 6, enchantmentIds[2]);
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (ICrafting icrafting : crafters) {
            icrafting.sendProgressBarUpdate(this, 0, enchantLevels[0]);
            icrafting.sendProgressBarUpdate(this, 1, enchantLevels[1]);
            icrafting.sendProgressBarUpdate(this, 2, enchantLevels[2]);
            icrafting.sendProgressBarUpdate(this, 3, xpSeed & -16);
            icrafting.sendProgressBarUpdate(this, 4, enchantmentIds[0]);
            icrafting.sendProgressBarUpdate(this, 5, enchantmentIds[1]);
            icrafting.sendProgressBarUpdate(this, 6, enchantmentIds[2]);
        }
    }

    public void updateProgressBar(int id, int data) {
        if (id >= 0 && id <= 2) {
            enchantLevels[id] = data;
        } else if (id == 3) {
            xpSeed = data;
        } else if (id >= 4 && id <= 6) {
            enchantmentIds[id - 4] = data;
        } else {
            super.updateProgressBar(id, data);
        }
    }

    public void onCraftMatrixChanged(IInventory inventoryIn) {
        if (inventoryIn == tableInventory) {
            ItemStack itemstack = inventoryIn.getStackInSlot(0);

            if (itemstack != null && itemstack.isItemEnchantable()) {
                if (!worldPointer.isRemote) {
                    int l = 0;

                    for (int j = -1; j <= 1; ++j) {
                        for (int k = -1; k <= 1; ++k) {
                            if ((j != 0 || k != 0) && worldPointer.isAirBlock(position.add(k, 0, j)) && worldPointer.isAirBlock(position.add(k, 1, j))) {
                                if (worldPointer.getBlockState(position.add(k * 2, 0, j * 2)).getBlock() == Blocks.bookshelf) {
                                    ++l;
                                }

                                if (worldPointer.getBlockState(position.add(k * 2, 1, j * 2)).getBlock() == Blocks.bookshelf) {
                                    ++l;
                                }

                                if (k != 0 && j != 0) {
                                    if (worldPointer.getBlockState(position.add(k * 2, 0, j)).getBlock() == Blocks.bookshelf) {
                                        ++l;
                                    }

                                    if (worldPointer.getBlockState(position.add(k * 2, 1, j)).getBlock() == Blocks.bookshelf) {
                                        ++l;
                                    }

                                    if (worldPointer.getBlockState(position.add(k, 0, j * 2)).getBlock() == Blocks.bookshelf) {
                                        ++l;
                                    }

                                    if (worldPointer.getBlockState(position.add(k, 1, j * 2)).getBlock() == Blocks.bookshelf) {
                                        ++l;
                                    }
                                }
                            }
                        }
                    }

                    rand.setSeed(xpSeed);

                    for (int i1 = 0; i1 < 3; ++i1) {
                        enchantLevels[i1] = EnchantmentHelper.calcItemStackEnchantability(rand, i1, l, itemstack);
                        enchantmentIds[i1] = -1;

                        if (enchantLevels[i1] < i1 + 1) {
                            enchantLevels[i1] = 0;
                        }
                    }

                    for (int j1 = 0; j1 < 3; ++j1) {
                        if (enchantLevels[j1] > 0) {
                            List<EnchantmentData> list = func_178148_a(itemstack, j1, enchantLevels[j1]);

                            if (list != null && !list.isEmpty()) {
                                EnchantmentData enchantmentdata = list.get(rand.nextInt(list.size()));
                                enchantmentIds[j1] = enchantmentdata.enchantmentobj.effectId | enchantmentdata.enchantmentLevel << 8;
                            }
                        }
                    }

                    detectAndSendChanges();
                }
            } else {
                for (int i = 0; i < 3; ++i) {
                    enchantLevels[i] = 0;
                    enchantmentIds[i] = -1;
                }
            }
        }
    }

    public boolean enchantItem(EntityPlayer playerIn, int id) {
        ItemStack itemstack = tableInventory.getStackInSlot(0);
        ItemStack itemstack1 = tableInventory.getStackInSlot(1);
        int i = id + 1;

        if ((itemstack1 == null || itemstack1.stackSize < i) && !playerIn.capabilities.isCreativeMode) {
            return false;
        } else if (enchantLevels[id] > 0 && itemstack != null && (playerIn.experienceLevel >= i && playerIn.experienceLevel >= enchantLevels[id] || playerIn.capabilities.isCreativeMode)) {
            if (!worldPointer.isRemote) {
                List<EnchantmentData> list = func_178148_a(itemstack, id, enchantLevels[id]);
                boolean flag = itemstack.getItem() == Items.book;

                if (list != null) {
                    playerIn.removeExperienceLevel(i);

                    if (flag) {
                        itemstack.setItem(Items.enchanted_book);
                    }

                    for (EnchantmentData enchantmentdata : list) {
                        if (flag) {
                            Items.enchanted_book.addEnchantment(itemstack, enchantmentdata);
                        } else {
                            itemstack.addEnchantment(enchantmentdata.enchantmentobj, enchantmentdata.enchantmentLevel);
                        }
                    }

                    if (!playerIn.capabilities.isCreativeMode) {
                        itemstack1.stackSize -= i;

                        if (itemstack1.stackSize <= 0) {
                            tableInventory.setInventorySlotContents(1, null);
                        }
                    }

                    playerIn.triggerAchievement(StatList.field_181739_W);
                    tableInventory.markDirty();
                    xpSeed = playerIn.getXPSeed();
                    onCraftMatrixChanged(tableInventory);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private List<EnchantmentData> func_178148_a(ItemStack stack, int p_178148_2_, int p_178148_3_) {
        rand.setSeed(xpSeed + p_178148_2_);
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(rand, stack, p_178148_3_);

        if (stack.getItem() == Items.book && list != null && list.size() > 1) {
            list.remove(rand.nextInt(list.size()));
        }

        return list;
    }

    public int getLapisAmount() {
        ItemStack itemstack = tableInventory.getStackInSlot(1);
        return itemstack == null ? 0 : itemstack.stackSize;
    }

    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        if (!worldPointer.isRemote) {
            for (int i = 0; i < tableInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = tableInventory.removeStackFromSlot(i);

                if (itemstack != null) {
                    playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        return worldPointer.getBlockState(position).getBlock() == Blocks.enchanting_table && playerIn.getDistanceSq((double) position.getX() + 0.5D, (double) position.getY() + 0.5D, (double) position.getZ() + 0.5D) <= 64.0D;
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0) {
                if (!mergeItemStack(itemstack1, 2, 38, true)) {
                    return null;
                }
            } else if (index == 1) {
                if (!mergeItemStack(itemstack1, 2, 38, true)) {
                    return null;
                }
            } else if (itemstack1.getItem() == Items.dye && EnumDyeColor.byDyeDamage(itemstack1.getMetadata()) == EnumDyeColor.BLUE) {
                if (!mergeItemStack(itemstack1, 1, 2, true)) {
                    return null;
                }
            } else {
                if (inventorySlots.getFirst().getHasStack() || !inventorySlots.getFirst().isItemValid(itemstack1)) {
                    return null;
                }

                if (itemstack1.hasTagCompound() && itemstack1.stackSize == 1) {
                    inventorySlots.getFirst().putStack(itemstack1.copy());
                    itemstack1.stackSize = 0;
                } else if (itemstack1.stackSize >= 1) {
                    inventorySlots.getFirst().putStack(new ItemStack(itemstack1.getItem(), 1, itemstack1.getMetadata()));
                    --itemstack1.stackSize;
                }
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }
}
