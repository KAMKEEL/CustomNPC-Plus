package noppes.npcs.scripted.roles;

import net.minecraft.entity.INpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.api.roles.IRole;

public class ScriptRoleInterface implements IRole {
	public final EntityNPCInterface npc;
	public final RoleInterface role;

	public ScriptRoleInterface(EntityNPCInterface npc){
		this.npc = npc;
		this.role = npc.roleInterface;
	}

	public INpc getNpc() {
		return (INpc) NpcAPI.Instance().getIEntity(npc);
	}

	/**
	 * @see noppes.npcs.scripted.constants.RoleType
	 * @return The RoleType of this role
	 */
	public int getType(){
		return RoleType.UNKNOWN;
	}
}
