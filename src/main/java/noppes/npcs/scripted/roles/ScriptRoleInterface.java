package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.scripted.interfaces.roles.IRole;

public class ScriptRoleInterface implements IRole {
	public final EntityNPCInterface npc;
	public final RoleInterface role;

	public ScriptRoleInterface(EntityNPCInterface npc){
		this.npc = npc;
		this.role = npc.roleInterface;
	}

	/**
	 * @see noppes.npcs.scripted.constants.RoleType
	 * @return The RoleType of this role
	 */
	public int getType(){
		return RoleType.UNKNOWN;
	}
}
