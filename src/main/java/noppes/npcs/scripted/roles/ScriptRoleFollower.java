package noppes.npcs.scripted.roles;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.scripted.ScriptPlayer;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleFollower extends ScriptRoleInterface{
	private RoleFollower role;
	public ScriptRoleFollower(EntityNPCInterface npc) {
		super(npc);
		role = (RoleFollower) npc.roleInterface;
	}
	
	/**
	 * @since 1.7.10c
	 * @param player Player who is set as the owner. If null given everything resets
	 */
	public void setOwner(ScriptPlayer player){
		if(player == null || player.getMinecraftEntity() == null){
			role.setOwner(null);
			return;
		}
		EntityPlayer mcplayer = (EntityPlayer) player.getMinecraftEntity();
		role.setOwner(mcplayer);
	}
	
	/**
	 * @since 1.7.10c
	 * @return Returns the followers owner. Returns null if he has no owner or the owner is offline
	 */
	public ScriptPlayer getOwner(){
		if(role.owner == null)
			return null;
		
		return (ScriptPlayer) ScriptController.Instance.getScriptForEntity(role.owner);
	}
	
	/**
	 * @since 1.7.10c
	 * @return Returns whether or not the follower has an owner
	 */
	public boolean hasOwner(){
		return role.owner != null;
	}
	
	/**
	 * @since 1.7.10c
	 * @return Returns days left
	 */
	public int getDaysLeft(){
		return role.getDaysLeft();
	}
	
	/**
	 * @since 1.7.10c
	 * @param days The days you want to add to the days remaining
	 */
	public void addDaysLeft(int days){
		role.addDays(days);
	}
	
	/**
	 * @since 1.7.10c
	 * @return Returns whether or not the follower is set to infinite days
	 */
	public boolean getInfiniteDays(){
		return role.infiniteDays;
	}
	
	/**
	 * @since 1.7.10c
	 * @param infinite Sets whether the days hired are infinite
	 */
	public void setInfiniteDays(boolean infinite){
		role.infiniteDays = infinite;
	}
	
	/**
	 * @since 1.7.10c
	 * @return Return whether the gui is disabled
	 */
	public boolean getGuiDisabled(){
		return role.disableGui;
	}
	
	/**
	 * @since 1.7.10c
	 * @param disabled Set the gui to be disabled or not
	 */
	public void setGuiDisabled(boolean disabled){
		role.disableGui = disabled;
	}

	@Override
	public int getType(){
		return RoleType.FOLLOWER;
	}
}
