package noppes.npcs.items;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemSoulstoneEmpty extends Item {
	public ItemSoulstoneEmpty(){
		this.setMaxStackSize(64);
	}
    @Override
    public Item setUnlocalizedName(String name){
    	super.setUnlocalizedName(name);
		GameRegistry.registerItem(this, name);
    	return this;
    }
    
	public boolean store(EntityLivingBase entity, ItemStack stack, EntityPlayer player) {
		if(!hasPermission(entity, player) || entity instanceof EntityPlayer)
			return false;
		ItemStack stone = new ItemStack(CustomItems.soulstoneFull);
		NBTTagCompound compound = new NBTTagCompound();
		if(!entity.writeToNBTOptional(compound))
			return false;
		ServerCloneController.Instance.cleanTags(compound);
		if(stone.stackTagCompound == null)
			stone.stackTagCompound = new NBTTagCompound();
		stone.stackTagCompound.setTag("Entity", compound);
		
        String name = EntityList.getEntityString(entity);
        if (name == null)
        	name = "generic";
		stone.stackTagCompound.setString("Name", "entity." + name + ".name");
        if(entity instanceof EntityNPCInterface){
        	EntityNPCInterface npc = (EntityNPCInterface) entity;
    		stone.stackTagCompound.setString("DisplayName", entity.getCommandSenderName());
    		if(npc.advanced.role == EnumRoleType.Companion){
    			RoleCompanion role = (RoleCompanion) npc.roleInterface;
        		stone.stackTagCompound.setString("ExtraText", "companion.stage,: ," + role.stage.name);
    		}
        }
        else if(entity instanceof EntityLiving && ((EntityLiving)entity).hasCustomNameTag())
    		stone.stackTagCompound.setString("DisplayName", ((EntityLiving)entity).getCustomNameTag());
		NoppesUtilServer.GivePlayerItem(player, player, stone);
		
		if(!player.capabilities.isCreativeMode){
			stack.splitStack(1);
			if(stack.stackSize <= 0)
				player.destroyCurrentEquippedItem();
		}
		
		entity.isDead = true;
		return true;
	}
	
	public boolean hasPermission(EntityLivingBase entity, EntityPlayer player){
		if(NoppesUtilServer.isOp(player) && player.capabilities.isCreativeMode)
			return true;
		if(CustomNpcsPermissions.enabled() && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.SOULSTONE_ALL))
			return true;
		if(entity instanceof EntityNPCInterface){
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			if(npc.advanced.role == EnumRoleType.Companion){
				RoleCompanion role = (RoleCompanion) npc.roleInterface;
				if(role.getOwner() == player)
					return true;
			}
			if(npc.advanced.role == EnumRoleType.Follower){
				RoleFollower role = (RoleFollower) npc.roleInterface;
				if(role.getOwner() == player)
					return !role.refuseSoulStone;
			}
			return CustomNpcs.SoulStoneNPCs;
		}
		if(entity instanceof EntityAnimal)
			return CustomNpcs.SoulStoneAnimals;
		
		return false;
	}
}
