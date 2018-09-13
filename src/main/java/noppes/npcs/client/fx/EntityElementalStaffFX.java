// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package noppes.npcs.client.fx;

import net.minecraft.client.particle.EntityPortalFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.MathHelper;


// Referenced classes of package net.minecraft.src:
//            EntityFX, World, Tessellator

public class EntityElementalStaffFX extends EntityPortalFX
{
	double x,y,z;
	EntityLivingBase player;
    public EntityElementalStaffFX(EntityLivingBase player, double d, double d1, double d2, 
            double f1, double f2, double f3, int color)
    {
        super(player.worldObj, player.posX + d, player.posY + d1, player.posZ + d2, f1, f2, f3);
        
        
        this.player = player;
        
        x = d;
        y = d1;
        z = d2;
        float[] colors;
        if(color <= 15)
	    	colors = EntitySheep.fleeceColorTable[color];
        else
        	colors = new float[]{((color>>16)&0xFF) / 255f, ((color>>8)&0xFF) / 255f,(color&0xFF) / 255f};
        particleRed = colors[0];
        particleGreen = colors[1];
        particleBlue = colors[2];
        particleMaxAge = (int)(16D / (Math.random() * 0.80000000000000004D + 0.20000000000000001D));
        noClip = false;
    }

    public void onUpdate()
    {
    	if(player.isDead){
    		this.setDead();
    		return;
    	}
    	
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        float var1 = (float)this.particleAge / (float)this.particleMaxAge;
        float var2 = var1;
        var1 = -var1 + var1 * var1 * 2.0F;
        var1 = 1.0F - var1;

    	double dx = -MathHelper.sin((float) ((player.rotationYaw / 180F) * Math.PI)) * MathHelper.cos((float) ((player.rotationPitch / 180F) * Math.PI));
    	double dz = MathHelper.cos((float) ((player.rotationYaw / 180F) * Math.PI)) * MathHelper.cos((float) ((player.rotationPitch / 180F) * Math.PI));
    	
        this.posX = player.posX + x + dx + this.motionX * (double)var1;
        this.posY = player.posY + y + this.motionY * (double)var1 + (double)(1.0F - var2)  - player.rotationPitch/40;
        this.posZ = player.posZ + z + dz + this.motionZ * (double)var1;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }
    }
    public void setDead(){
    	super.setDead();
    }
}
