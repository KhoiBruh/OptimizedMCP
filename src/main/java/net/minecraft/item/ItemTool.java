package net.minecraft.item;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class ItemTool extends Item {
    protected float efficiencyOnProperMaterial = 4.0F;
    protected Item.ToolMaterial toolMaterial;
    private final Set<Block> effectiveBlocks;
    private final float damageVsEntity;

    protected ItemTool(float attackDamage, Item.ToolMaterial material, Set<Block> effectiveBlocks) {
        toolMaterial = material;
        this.effectiveBlocks = effectiveBlocks;
        maxStackSize = 1;
        setMaxDamage(material.getMaxUses());
        efficiencyOnProperMaterial = material.getEfficiencyOnProperMaterial();
        damageVsEntity = attackDamage + material.getDamageVsEntity();
        setCreativeTab(CreativeTabs.tabTools);
    }

    public float getStrVsBlock(ItemStack stack, Block state) {
        return effectiveBlocks.contains(state) ? efficiencyOnProperMaterial : 1.0F;
    }

    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(2, attacker);
        return true;
    }

    public boolean onBlockDestroyed(ItemStack stack, World worldIn, Block blockIn, BlockPos pos, EntityLivingBase playerIn) {
        if ((double) blockIn.getBlockHardness(worldIn, pos) != 0.0D) {
            stack.damageItem(1, playerIn);
        }

        return true;
    }

    public boolean isFull3D() {
        return true;
    }

    public Item.ToolMaterial getToolMaterial() {
        return toolMaterial;
    }

    public int getItemEnchantability() {
        return toolMaterial.getEnchantability();
    }

    public String getToolMaterialName() {
        return toolMaterial.toString();
    }

    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return toolMaterial.getRepairItem() == repair.getItem() || super.getIsRepairable(toRepair, repair);
    }

    public Multimap<String, AttributeModifier> getItemAttributeModifiers() {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Tool modifier", damageVsEntity, 0));
        return multimap;
    }
}
