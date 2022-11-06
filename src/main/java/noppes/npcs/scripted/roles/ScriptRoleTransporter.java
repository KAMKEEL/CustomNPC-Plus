package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.scripted.interfaces.roles.IRoleTransporter;

public class ScriptRoleTransporter extends ScriptRoleInterface implements IRoleTransporter {

	public ScriptRoleTransporter(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public int getType(){
		return RoleType.TRANSPORTER;
	}
}
