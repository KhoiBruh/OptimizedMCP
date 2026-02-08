package net.minecraft.item;

import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRecord extends Item {
    private static final Map<String, ItemRecord> RECORDS = new HashMap<>();
    public final String recordName;

    protected ItemRecord(String name) {
        recordName = name;
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
        RECORDS.put("records." + name, this);
    }

    public static ItemRecord getRecord(String name) {
        return RECORDS.get(name);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == Blocks.jukebox && !iblockstate.getValue(BlockJukebox.HAS_RECORD)) {
            if (worldIn.isRemote) {
                return true;
            } else {
                ((BlockJukebox) Blocks.jukebox).insertRecord(worldIn, pos, iblockstate, stack);
                worldIn.playAuxSFXAtEntity(null, 1005, pos, Item.getIdFromItem(this));
                --stack.stackSize;
                playerIn.triggerAchievement(StatList.field_181740_X);
                return true;
            }
        } else {
            return false;
        }
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(getRecordNameLocal());
    }

    public String getRecordNameLocal() {
        return StatCollector.translateToLocal("item.record." + recordName + ".desc");
    }

    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }
}
