package noppes.npcs.roles;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumJobType;
import java.util.HashMap;
import java.util.UUID;

public class RoleFollower extends RoleInterface {

    private String ownerUUID;
    public boolean isFollowing = true;
    public HashMap<Integer, Integer> rates;
    public NpcMiscInventory inventory;
    public String dialogHire = PlatformServiceHolder.get().translateToLocal("follower.hireText") + " {days} " + PlatformServiceHolder.get().translateToLocal("follower.days");
    public String dialogFarewell = PlatformServiceHolder.get().translateToLocal("follower.farewellText") + " {player}";
    public int daysHired;
    public long hiredTime;
    public boolean disableGui = false;
    public boolean infiniteDays = false;
    public boolean refuseSoulStone = false;

    public IPlayer owner = null;

    public RoleFollower(EntityNPCInterface npc) {
        super(npc);
        inventory = new NpcMiscInventory(3);
        rates = new HashMap<Integer, Integer>();
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setInteger("MercenaryDaysHired", daysHired);
        INbt.setLong("MercenaryHiredTime", hiredTime);
        INbt.setString("MercenaryDialogHired", dialogHire);
        INbt.setString("MercenaryDialogFarewell", dialogFarewell);
        if (hasOwner())
            INbt.setString("MercenaryOwner", ownerUUID);
        INbt.setTag("MercenaryDayRates", NBTTags.nbtIntegerIntegerMap(rates));
        INbt.setTag("MercenaryInv", inventory.getToNBT());
        INbt.setBoolean("MercenaryIsFollowing", isFollowing);
        INbt.setBoolean("MercenaryDisableGui", disableGui);
        INbt.setBoolean("MercenaryInfiniteDays", infiniteDays);
        INbt.setBoolean("MercenaryRefuseSoulstone", refuseSoulStone);
        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        ownerUUID = INbt.getString("MercenaryOwner");
        daysHired = INbt.getInteger("MercenaryDaysHired");
        hiredTime = INbt.getLong("MercenaryHiredTime");
        dialogHire = INbt.getString("MercenaryDialogHired");
        dialogFarewell = INbt.getString("MercenaryDialogFarewell");
        rates = NBTTags.getIntegerIntegerMap(INbt.getTagList("MercenaryDayRates", 10));
        inventory.setFromNBT(INbt.getCompoundTag("MercenaryInv"));
        isFollowing = INbt.getBoolean("MercenaryIsFollowing");
        disableGui = INbt.getBoolean("MercenaryDisableGui");
        infiniteDays = INbt.getBoolean("MercenaryInfiniteDays");
        refuseSoulStone = INbt.getBoolean("MercenaryRefuseSoulstone");
    }

    @Override
    public boolean aiShouldExecute() {
        owner = getOwner();
        if (!infiniteDays && owner != null && getDaysLeft() <= 0) {
            owner.addChatMessage(new ITextComponent(NoppesStringUtils.formatText(dialogFarewell, owner, npc)));
            killed();
        }
        return false;
    }

    public IPlayer getOwner() {
        if (ownerUUID == null || ownerUUID.isEmpty())
            return null;
        try {
            UUID uuid = UUID.fromString(ownerUUID);
            if (uuid != null)
                return npc.worldObj.func_152378_a(uuid);
        } catch (IllegalArgumentException ex) {

        }

        return npc.worldObj.getPlayerEntityByName(ownerUUID);
    }

    public boolean hasOwner() {
        if (!infiniteDays && daysHired <= 0)
            return false;
        return ownerUUID != null && !ownerUUID.isEmpty();
    }

    @Override
    public void killed() {
        ownerUUID = null;
        daysHired = 0;
        hiredTime = 0;
        isFollowing = true;
        npc.stats.canDespawn = npc.stats.playerSetCanDespawn;
    }

    public int getDaysLeft() {
        if (infiniteDays)
            return 100;
        if (daysHired <= 0)
            return 0;
        int days = (int) ((npc.worldObj.getTotalWorldTime() - hiredTime) / 24000);
        return daysHired - days;
    }

    public void addDays(int days) {
        daysHired = days + getDaysLeft();
        hiredTime = npc.worldObj.getTotalWorldTime();
    }

    @Override
    public void interact(IPlayer player) {
        if (ownerUUID == null || ownerUUID.isEmpty()) {
            npc.say(player, npc.advanced.getInteractLine());
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollowerHire, npc);
        } else if (player == owner && !disableGui) {
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollower, npc);
        }
    }

    @Override
    public boolean defendOwner() {
        return isFollowing() && npc.advanced.job == EnumJobType.Guard;
    }

    @Override
    public void delete() {

    }

    public boolean isFollowing() {
        return owner != null && isFollowing && getDaysLeft() > 0;
    }

    public void setOwner(IPlayer player) {
        UUID id = player.getUniqueID();
        if (ownerUUID == null || id == null || !ownerUUID.equals(id))
            killed();
        ownerUUID = id.toString();
        npc.stats.canDespawn = false;
    }

    public void setRate(int index, int amount) {
        if (index > 2 || index < 0)
            return;

        if (amount < 1)
            return;

        rates.put(index, amount);
    }

    public int getRate(int index) {
        if (index > 2 || index < 0)
            return -1;

        if (!rates.containsKey(index))
            return -1;

        return rates.get(index);
    }

    public void setDialogHire(String dialogHire) {
        this.dialogHire = dialogHire;
    }

    public String getDialogHire() {
        return dialogHire;
    }

    public void setDialogFarewell(String dialogFarewell) {
        this.dialogFarewell = dialogFarewell;
    }

    public String getDialogFarewell() {
        return this.dialogFarewell;
    }
}
