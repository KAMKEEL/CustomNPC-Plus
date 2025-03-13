package noppes.npcs.scripted.roles;

import noppes.npcs.api.roles.IRoleMailman;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleMailman extends ScriptRoleInterface implements IRoleMailman {

    public ScriptRoleMailman(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public int getType() {
        return RoleType.POSTMAN;
    }
}
