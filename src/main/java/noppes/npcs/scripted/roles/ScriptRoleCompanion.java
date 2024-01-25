package noppes.npcs.scripted.roles;

import noppes.npcs.api.roles.IRoleMailman;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleCompanion extends ScriptRoleInterface implements IRoleMailman {

	public ScriptRoleCompanion(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public int getType(){
		return RoleType.COMPANION;
	}
}
