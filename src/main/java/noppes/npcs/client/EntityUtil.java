package noppes.npcs.client;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcPony;

public class EntityUtil {

	public static void Copy(EntityLivingBase copied, EntityLivingBase entity){
		entity.worldObj = copied.worldObj;
		
		entity.deathTime = copied.deathTime;
		entity.distanceWalkedModified = copied.distanceWalkedModified;
		entity.prevDistanceWalkedModified = copied.distanceWalkedModified;
		entity.onGround = copied.onGround;
		entity.distanceWalkedOnStepModified = copied.distanceWalkedOnStepModified;

		entity.moveForward = copied.moveForward;
		entity.moveStrafing = copied.moveStrafing;
		
		entity.setPosition(copied.posX, copied.posY, copied.posZ);
		entity.boundingBox.setBB(copied.boundingBox);
		
		entity.prevPosX = copied.prevPosX;
		entity.prevPosY = copied.prevPosY;
		entity.prevPosZ = copied.prevPosZ;
		
		entity.motionX = copied.motionX;
		entity.motionY = copied.motionY;
		entity.motionZ = copied.motionZ;
		
		entity.rotationYaw = copied.rotationYaw;
		entity.rotationPitch = copied.rotationPitch;
		entity.prevRotationYaw = copied.prevRotationYaw;
		entity.prevRotationPitch = copied.prevRotationPitch;
		entity.rotationYawHead = copied.rotationYawHead;
		entity.prevRotationYawHead = copied.prevRotationYawHead;
		entity.prevRenderYawOffset = copied.prevRenderYawOffset;
		entity.cameraPitch = copied.cameraPitch;
		entity.prevCameraPitch = copied.prevCameraPitch;
		
		entity.renderYawOffset = copied.renderYawOffset;
				
		entity.lastTickPosX = copied.lastTickPosX;
		entity.lastTickPosY = copied.lastTickPosY;
		entity.lastTickPosZ = copied.lastTickPosZ;

		entity.limbSwingAmount = copied.limbSwingAmount;
		entity.prevLimbSwingAmount = copied.prevLimbSwingAmount;
		entity.limbSwing = copied.limbSwing;

		entity.swingProgress = copied.swingProgress;
		entity.prevSwingProgress = copied.prevSwingProgress;
		entity.isSwingInProgress = copied.isSwingInProgress;
		entity.swingProgressInt = copied.swingProgressInt;
		
		entity.ticksExisted = copied.ticksExisted;
		
		if(entity instanceof EntityPlayer && copied instanceof EntityPlayer){
			EntityPlayer ePlayer = (EntityPlayer) entity;
			EntityPlayer cPlayer = (EntityPlayer) copied;

			ePlayer.cameraYaw = cPlayer.cameraYaw;
			ePlayer.prevCameraYaw = cPlayer.prevCameraYaw;

			ePlayer.field_71091_bM = cPlayer.field_71091_bM;
			ePlayer.field_71096_bN = cPlayer.field_71096_bN;
			ePlayer.field_71097_bO = cPlayer.field_71097_bO;
			ePlayer.field_71094_bP = cPlayer.field_71094_bP;
			ePlayer.field_71095_bQ = cPlayer.field_71095_bQ;
			ePlayer.field_71085_bR = cPlayer.field_71085_bR;
		}
		
		if(entity instanceof EntityDragon){
			entity.rotationYaw += 180;
		}
		if(entity instanceof EntityChicken){
			((EntityChicken)entity).destPos = copied.onGround?0:1;
		}
		
		for(int i = 0; i < 5; i++){
			entity.setCurrentItemOrArmor(i, copied.getEquipmentInSlot(i));
		}
		
		if(copied instanceof EntityNPCInterface && entity instanceof EntityNPCInterface){
			EntityNPCInterface npc = (EntityNPCInterface) copied;
			EntityNPCInterface target = (EntityNPCInterface) entity;

			target.textureLocation = npc.textureLocation;
			target.textureGlowLocation = npc.textureGlowLocation;
			target.textureCloakLocation = npc.textureCloakLocation;
			target.display = npc.display;
			target.inventory = npc.inventory;
			
			target.currentAnimation = npc.currentAnimation;
			
			target.setDataWatcher(npc.getDataWatcher());
		}
	}
}
