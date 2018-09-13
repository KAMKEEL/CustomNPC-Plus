package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleTransporter extends ScriptRoleInterface{

	public ScriptRoleTransporter(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public int getType(){
		return RoleType.TRANSPORTER;
	}
}
