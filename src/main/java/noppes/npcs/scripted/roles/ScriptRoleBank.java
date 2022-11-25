package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.api.roles.IRoleBank;

public class ScriptRoleBank extends ScriptRoleInterface implements IRoleBank {

	public ScriptRoleBank(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public int getType(){
		return RoleType.BANK;
	}
}
