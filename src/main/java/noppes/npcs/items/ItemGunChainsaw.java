package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class ItemGunChainsaw extends ItemNpcWeaponInterface{

	public ItemGunChainsaw(int par1, ToolMaterial tool) {
		super(par1, tool);
	}

    @Override
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving)
    {
    	if(par2EntityLiving.getHealth() <= 0)
    		return false;
        double x = par2EntityLiving.posX;
        double y = par2EntityLiving.posY+ par2EntityLiving.height / 2;
        double z = par2EntityLiving.posZ;
        
        par3EntityLiving.worldObj.playSoundEffect(x, y, z, "random.explode", 0.8F, (1.0F + (par3EntityLiving.worldObj.rand.nextFloat() - par3EntityLiving.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
        par3EntityLiving.worldObj.spawnParticle("largeexplode", x, y , z, 0.0D, 0.0D, 0.0D);
        return super.hitEntity(par1ItemStack, par2EntityLiving, par3EntityLiving);
    }    

	public void renderSpecial(){
		super.renderSpecial();
        GL11.glTranslatef(-0.1f, 0f, -0.16f);
        GL11.glRotatef(180, 0, 1, 0);
        GL11.glRotatef(-16, 0, 0, 1);
	}
}
