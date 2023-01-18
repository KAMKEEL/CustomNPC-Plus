package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;


public class ItemTeleporter extends Item{
	
    public ItemTeleporter(){
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		if(!par2World.isRemote)
			return par1ItemStack;
    	CustomNpcs.proxy.openGui((EntityNPCInterface)null,EnumGuiType.NpcDimensions);
        return par1ItemStack;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase par3EntityPlayer, ItemStack stack){
    	if(par3EntityPlayer.worldObj.isRemote)
    		return false;
        float f = 1.0F;
        float f1 = par3EntityPlayer.prevRotationPitch + (par3EntityPlayer.rotationPitch - par3EntityPlayer.prevRotationPitch) * f;
        float f2 = par3EntityPlayer.prevRotationYaw + (par3EntityPlayer.rotationYaw - par3EntityPlayer.prevRotationYaw) * f;
        double d0 = par3EntityPlayer.prevPosX + (par3EntityPlayer.posX - par3EntityPlayer.prevPosX) * (double)f;
        double d1 = par3EntityPlayer.prevPosY + (par3EntityPlayer.posY - par3EntityPlayer.prevPosY) * (double)f + 1.62D - (double)par3EntityPlayer.yOffset;
        double d2 = par3EntityPlayer.prevPosZ + (par3EntityPlayer.posZ - par3EntityPlayer.prevPosZ) * (double)f;
        Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = 80.0D;
        Vec3 vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
        MovingObjectPosition movingobjectposition = par3EntityPlayer.worldObj.rayTraceBlocks(vec3, vec31, true);
        if (movingobjectposition == null)
            return false;
        
        Vec3 vec32 = par3EntityPlayer.getLook(f);
        boolean flag = false;
        float f9 = 1.0F;
        List list = par3EntityPlayer.worldObj.getEntitiesWithinAABBExcludingEntity(par3EntityPlayer, par3EntityPlayer.boundingBox.addCoord(vec32.xCoord * d3, vec32.yCoord * d3, vec32.zCoord * d3).expand((double)f9, (double)f9, (double)f9));

        for (int i = 0; i < list.size(); ++i){
            Entity entity = (Entity)list.get(i);

            if (entity.canBeCollidedWith()){
                float f10 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f10, (double)f10, (double)f10);

                if (axisalignedbb.isVecInside(vec3)){
                    flag = true;
                }
            }
        }

        if (flag)
            return false;
        
        if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
            int i = movingobjectposition.blockX;
            int j = movingobjectposition.blockY;
            int k = movingobjectposition.blockZ;
            
            while(par3EntityPlayer.worldObj.getBlock(i, j, k) != Blocks.air){
            	j++;
            }
            par3EntityPlayer.setPositionAndUpdate(i + 0.5F, j + 1.0F, k + 0.5F);
        }
        
    	
    	return true;
    }
//    protected static MovingObjectPosition rayTraceBlocks(EntityPlayer entityPlayer, int radius) {
//    	Vec3 vec3 = Vec3.createVectorHelper(entityPlayer.posX, entityPlayer.height * 0.8 + entityPlayer.boundingBox.minY, entityPlayer.posZ);
//    	Vec3 vec3a = entityPlayer.getLookVec();
//    	Vec3 vec3b = vec3.addVector(vec3a.xCoord * radius, vec3a.yCoord * radius, vec3a.zCoord * radius);
//
//    	return entityPlayer.worldObj.func_147447_a(vec3, vec3b, false, false, true);
//    }
//    	
//    protected static MovingObjectPosition getPointingEntity(EntityPlayer entityPlayer, int radius) {
//    	MovingObjectPosition block = rayTraceBlocks(entityPlayer, radius);
//    	Vec3 vec3 = Vec3.createVectorHelper(entityPlayer.posX, entityPlayer.height * 0.8 + entityPlayer.boundingBox.minY, entityPlayer.posZ);
//            
//    	Vec3 vec31 = entityPlayer.getLookVec();
//    	Vec3 vec32 = vec3.addVector(vec31.xCoord * radius, vec31.yCoord * radius, vec31.zCoord * radius);
//    	Vec3 vec33 = null;
//    	List<Entity> entities = entityPlayer.worldObj.getEntitiesWithinAABBExcludingEntity(entityPlayer, entityPlayer.boundingBox.addCoord(vec31.xCoord * radius, vec31.yCoord * radius, vec31.zCoord * radius).expand(1.0D, 1.0D, 1.0D));
//    	Entity pointedEntity = null;
//    	double d = radius;
//            
//    	for (Entity entity : entities) {
//    		if (entity.canBeCollidedWith()) {
//    			float f = entity.getCollisionBorderSize();
//    			AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f, (double)f, (double)f);
//    			MovingObjectPosition movingObjectPosition = axisalignedbb.calculateIntercept(vec3, vec32);
//
//    			if (axisalignedbb.isVecInside(vec3)) {
//    				if (0.0D < radius || radius == 0.0D) {
//    					pointedEntity = entity;
//    					vec33 = movingObjectPosition == null ? vec3 : movingObjectPosition.hitVec;
//    					d = 0.0D;
//    				}
//    			} else if (movingObjectPosition != null) {
//    				double d2 = vec3.distanceTo(movingObjectPosition.hitVec);
//
//    				if (d2 < radius || radius == 0.0D) {
//    					if (entity == entityPlayer.ridingEntity && !entity.canRiderInteract()) {
//    						if (radius == 0.0D) {
//    							pointedEntity = entity;
//    							vec33 = movingObjectPosition.hitVec;
//    						}
//    					} else {
//    						pointedEntity = entity;
//    						vec33 = movingObjectPosition.hitVec;
//    						d = d2;
//    					}
//    				}
//    			}
//    		}
//    	}
//            
//    	if (pointedEntity != null && (d < radius)) {
//    		return new MovingObjectPosition(pointedEntity, vec33);
//    	}
//            
//    	return null;
//    }
	
    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2){
		return 0x8B4513;
    }
    
    @Override
    public boolean requiresMultipleRenderPasses(){
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister){
        this.itemIcon = Items.feather.getIconFromDamage(0);
    }

    @Override
    public Item setUnlocalizedName(String name){
		GameRegistry.registerItem(this, name);
    	return super.setUnlocalizedName(name);
    }
}
