package noppes.npcs.scripted.roles;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.roles.IRoleFollower;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleFollower extends ScriptRoleInterface implements IRoleFollower {
    private final RoleFollower role;

    public ScriptRoleFollower(EntityNPCInterface npc) {
        super(npc);
        role = (RoleFollower) npc.roleInterface;
    }

    public void setOwner(IPlayer player) {
        if (player == null || player.getMCEntity() == null) {
            role.setOwner(null);
            return;
        }
        EntityPlayer mcplayer = (EntityPlayer) player.getMCEntity();
        role.setOwner(mcplayer);
    }

    public IPlayer getOwner() {
        if (role.owner == null)
            return null;

        return (IPlayer) NpcAPI.Instance().getIEntity(role.owner);
    }

    public boolean hasOwner() {
        return role.owner != null;
    }

    public boolean isFollowing() {
        return role.isFollowing();
    }

    public void setIsFollowing(boolean following) {
        if (role.owner != null && role.getDaysLeft() > 0) role.isFollowing = following;
    }

    public int getDaysLeft() {
        return role.getDaysLeft();
    }

    public void addDaysLeft(int days) {
        role.addDays(days);
    }

    public boolean getInfiniteDays() {
        return role.infiniteDays;
    }

    public void setInfiniteDays(boolean infinite) {
        role.infiniteDays = infinite;
    }

    public boolean getGuiDisabled() {
        return role.disableGui;
    }

    public void setGuiDisabled(boolean disabled) {
        role.disableGui = disabled;
    }


    /**
     * @param index  Index of Rate [0 - 2]
     * @param amount Amount hired for
     */
    public void setRate(int index, int amount) {
        role.setRate(index, amount);
    }

    /**
     * @param index Index of Rate [0 - 2]
     * @return amount fired for
     */
    public int getRate(int index) {
        return role.getRate(index);
    }

    /**
     * @param dialogHire New dialog hire string
     */
    public void setDialogHire(String dialogHire) {
        role.setDialogHire(dialogHire);
    }

    /**
     * @return dialog hire string
     */
    public String getDialogHire() {
        return role.getDialogHire();
    }

    /**
     * @param dialogFarewell New dialog farewell string
     */
    public void setDialogFarewell(String dialogFarewell) {
        role.setDialogFarewell(dialogFarewell);
    }

    /**
     * @return dialog farewell string
     */
    public String getDialogFarewell() {
        return role.getDialogFarewell();
    }

    @Override
    public int getType() {
        return RoleType.FOLLOWER;
    }
}
