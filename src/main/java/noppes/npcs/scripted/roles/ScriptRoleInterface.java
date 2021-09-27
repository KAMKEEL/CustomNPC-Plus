package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleInterface {
    protected EntityNPCInterface npc;

    public ScriptRoleInterface(EntityNPCInterface npc) {
        this.npc = npc;
    }

    /**
     * @return The RoleType of this role
     * @see noppes.npcs.scripted.constants.RoleType
     */
    public int getType() {
        return RoleType.UNKNOWN;
    }
}
