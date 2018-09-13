package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;

public class ItemMusic extends ItemNpcInterface{
	
	private boolean shouldRotate = false;

	public ItemMusic() {
		setCreativeTab(CustomItems.tabMisc);		
	}
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer player)
    {
    	if(par2World.isRemote)
    		return par1ItemStack;
    	
    	int note = par2World.rand.nextInt(24);
        float var7 = (float)Math.pow(2.0D, (double)(note - 12) / 12.0D);

        String var8 = "harp";
        
    	par2World.playSoundEffect(player.posX, player.posY, player.posZ, "note." + var8, 3.0F, var7);
    	par2World.spawnParticle("note", player.posY, player.posY + 1.2D, player.posY, (double)note / 24.0D, 0.0D, 0.0D);
    	
    	return par1ItemStack;
    }
    
    public Item setRotated(){
    	shouldRotate = true;
    	return this;
    }
    
    public boolean shouldRotateAroundWhenRendering()
    {
        return shouldRotate;
    }

}
