package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.constants.EnumNpcToolMaterial;

import org.lwjgl.opengl.GL11;

public class ItemShield extends ItemNpcInterface{
	public EnumNpcToolMaterial material;
	public ItemShield(int par1, EnumNpcToolMaterial material) {
		super(par1);
		this.material = material;
        this.setMaxDamage(material.getMaxUses());
        setCreativeTab(CustomItems.tabWeapon);
	}

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.6f, 0.6f,0.6f);
    	GL11.glTranslatef(0f, 0f, -0.2f);
        GL11.glRotatef(-6, 0, 1, 0);
	}	
	
	public EnumAction getItemUseAction(ItemStack par1ItemStack){
        return EnumAction.block;
    }
	
	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
        par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }
	
	@Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack){
        return 72000;
    }
}
