package noppes.npcs.items;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.NoppesUtil;

public class ItemMoneyBag extends Item{
	
    public ItemMoneyBag(int i)
    {
        //super(i - 26700 + CustomNpcs.ItemStartId);
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
    }
	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
		if(par2World.isRemote) //Client check. Only server may create the moneybagcontents
			return par1ItemStack;
		
		if(par1ItemStack.stackTagCompound == null) //You use the par1ItemStack.stackTagCompound to store your data in the item
			par1ItemStack.stackTagCompound = new NBTTagCompound();
		
		MoneyBagContents contents = new MoneyBagContents(par3EntityPlayer);
		
		NoppesUtil.openGUI(par3EntityPlayer, new GuiScreen());
		
		return par1ItemStack;
    }
}