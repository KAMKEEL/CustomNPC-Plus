package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.item.ScriptCustomizableItem;

public abstract class ItemCustomizable extends Item implements ItemRenderInterface  {

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister){
        this.itemIcon = Items.iron_pickaxe.getIconFromDamage(0);
    }

    @Override
    public int getColorFromItemStack(ItemStack itemStack, int par2){
        return 0x8B4513;
    }

    @Override
    public boolean requiresMultipleRenderPasses(){
        return true;
    }

    @Override
    public Item setUnlocalizedName(String name){
        GameRegistry.registerItem(this, name);
        return super.setUnlocalizedName(name);
    }

    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }


    public EnumAction getItemUseAction(ItemStack stack)
    {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        if (istack instanceof ScriptCustomizableItem) {
            switch (((ScriptCustomizableItem) istack).getItemUseAction()) {
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
    public void renderSpecial() {}
}
