package noppes.npcs.items;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.renderer.NpcItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemNpcInterface extends Item implements ItemRenderInterface{
    
	public ItemNpcInterface(int par1) {
		this();
	}
	public ItemNpcInterface() {
		setCreativeTab(CustomItems.tab);
		CustomNpcs.proxy.registerItem(this);
	}
	public void renderSpecial(){
        GL11.glScalef(0.66f, 0.66f,0.66f);
        GL11.glTranslatef(0, 0.3f, 0);
    };

    @Override
    public int getItemEnchantability()
    {
        //return this.toolMaterial.getEnchantability();
    	return super.getItemEnchantability();
    }

    @Override
    public Item setUnlocalizedName(String name){
    	super.setUnlocalizedName(name);
		GameRegistry.registerItem(this, name);
    	return this;
    }

    @Override
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving)
    {
    	if(par2EntityLiving.getHealth() <= 0)
    		return false;
        par1ItemStack.damageItem(1, par3EntityLiving);
        return true;
    }

	public boolean hasItem(EntityPlayer player, Item item) {
		return player.inventory.hasItem(item);
	}
	
	public boolean consumeItem(EntityPlayer player, Item item) {
		return player.inventory.consumeInventoryItem(item);
	}
}
