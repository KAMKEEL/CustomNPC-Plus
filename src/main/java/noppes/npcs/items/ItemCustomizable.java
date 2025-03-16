package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.EventHooks;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.ItemEvent;
import org.lwjgl.opengl.GL11;

public abstract class ItemCustomizable extends Item implements ItemRenderInterface {

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = Items.iron_pickaxe.getIconFromDamage(0);
    }

    @Override
    public int getColorFromItemStack(ItemStack itemStack, int par2) {
        return 0x8B4513;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLivingBase, ItemStack stack) {
        if (entityLivingBase.worldObj.isRemote) {
            return false;
        }

        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent((IItemCustomizable) istack, NpcAPI.Instance().getIEntity(entityLivingBase), 2, null);
        return EventHooks.onScriptItemAttack((IItemCustomizable) istack, eve);
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        IItemCustomizable customizable = (IItemCustomizable) NpcAPI.Instance().getIItemStack(stack);
        return customizable.getMaxItemUseDuration();
    }

    public boolean showDurabilityBar(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof IItemCustomizable && ((IItemCustomizable) istack).getDurabilityShow();
    }

    public double getDurabilityForDisplay(ItemStack stack) {

        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof IItemCustomizable ? 1.0D - ((IItemCustomizable) istack).getDurabilityValue() : 1.0D;
    }

    public int getItemStackLimit(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof IItemCustomizable ? istack.getMaxStackSize() : super.getItemStackLimit(stack);
    }

    public boolean isItemTool(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof IItemCustomizable ? ((IItemCustomizable) istack).isTool() : super.isItemTool(stack);
    }

    public float getDigSpeed(ItemStack stack, Block block, int metadata) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof IItemCustomizable ? ((IItemCustomizable) istack).getDigSpeed() : super.getDigSpeed(stack, block, metadata);
    }

    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        if (istack instanceof IItemCustomizable) {
            if (((IItemCustomizable) istack).getArmorType() == -1)
                return true;
            return armorType == ((IItemCustomizable) istack).getArmorType();
        }
        return super.isValidArmor(stack, armorType, entity);
    }

    public int getItemEnchantability(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof IItemCustomizable ? ((IItemCustomizable) istack).getEnchantability() : super.getItemEnchantability(stack);
    }

    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent((IItemCustomizable) istack, NpcAPI.Instance().getIEntity(attacker), 1, NpcAPI.Instance().getIEntity(target));
        return EventHooks.onScriptItemAttack((IItemCustomizable) istack, eve);
    }

    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public Item setUnlocalizedName(String name) {
        GameRegistry.registerItem(this, name);
        return super.setUnlocalizedName(name);
    }

    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }


    public EnumAction getItemUseAction(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        if (istack instanceof IItemCustomizable) {
            switch (((IItemCustomizable) istack).getItemUseAction()) {
                case 0:
                    return EnumAction.none;
                case 1:
                    return EnumAction.block;
                case 2:
                    return EnumAction.bow;
                case 3:
                    return EnumAction.eat;
                case 4:
                    return EnumAction.drink;
            }
        }
        return super.getItemUseAction(stack);
    }

    @Override
    public void renderSpecial() {
    }

    public void renderOffset(IItemCustomizable scriptCustomItem) {
        GL11.glTranslatef(0.135F * scriptCustomItem.getScaleX(), 0.2F * scriptCustomItem.getScaleY(), 0.07F * scriptCustomItem.getScaleZ());
    }
}
