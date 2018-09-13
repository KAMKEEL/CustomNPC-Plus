package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class ItemMusicViolin extends ItemMusic{

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.66f, 0.66f,0.66f);
        GL11.glRotatef(-90, 0F, 0, 1F);
        GL11.glRotatef(90, 0, 1, 0F);
        
        GL11.glTranslatef(0.2f, -0.9f, -0.52f);
	}    

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }
	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return super.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
    }
	@Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }
}
