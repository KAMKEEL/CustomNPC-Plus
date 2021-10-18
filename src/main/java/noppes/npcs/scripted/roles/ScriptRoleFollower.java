package noppes.npcs.scripted.roles;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.scripted.ScriptPlayer;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleFollower extends ScriptRoleInterface {
    private RoleFollower role;

    public ScriptRoleFollower(EntityNPCInterface npc) {
        super(npc);
        role = (RoleFollower) npc.roleInterface;
    }

    /**
     * @return Returns the followers owner. Returns null if he has no owner or the owner is offline
     * @since 1.7.10c
     */
    public ScriptPlayer getOwner() {
        if (role.owner == null)
            return null;

        return (ScriptPlayer) ScriptController.Instance.getScriptForEntity(role.owner);
    }

    /**
     * @param player Player who is set as the owner. If null given everything resets
     * @since 1.7.10c
     */
    public void setOwner(ScriptPlayer player) {
        if (player == null || player.getMinecraftEntity() == null) {
            role.setOwner(null);
            return;
        }
        EntityPlayer mcplayer = (EntityPlayer) player.getMinecraftEntity();
        role.setOwner(mcplayer);
    }

    /**
     * @return Returns whether or not the follower has an owner
     * @since 1.7.10c
     */
    public boolean hasOwner() {
        return role.owner != null;
    }

    /**
     * @return Returns days left
     * @since 1.7.10c
     */
    public int getDaysLeft() {
        return role.getDaysLeft();
    }

    /**
     * @param days The days you want to add to the days remaining
     * @since 1.7.10c
     */
    public void addDaysLeft(int days) {
        role.addDays(days);
    }

    /**
     * @return Returns whether or not the follower is set to infinite days
     * @since 1.7.10c
     */
    public boolean getInfiniteDays() {
        return role.infiniteDays;
    }

    /**
     * @param infinite Sets whether the days hired are infinite
     * @since 1.7.10c
     */
    public void setInfiniteDays(boolean infinite) {
        role.infiniteDays = infinite;
    }

    /**
     * @return Return whether the gui is disabled
     * @since 1.7.10c
     */
    public boolean getGuiDisabled() {
        return role.disableGui;
    }

    /**
     * @param disabled Set the gui to be disabled or not
     * @since 1.7.10c
     */
    public void setGuiDisabled(boolean disabled) {
        role.disableGui = disabled;
    }

    @Override
    public int getType() {
        return RoleType.FOLLOWER;
    }
}
