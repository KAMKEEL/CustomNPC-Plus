package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleBank extends ScriptRoleInterface{

	public ScriptRoleBank(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public int getType(){
		return RoleType.BANK;
	}
}
