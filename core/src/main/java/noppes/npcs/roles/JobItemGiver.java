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
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.GlobalDataController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerItemGiverData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class JobItemGiver extends JobInterface {

    public int cooldownType = 0;
    public int givingMethod = 0;
    public int cooldown = 10;
    public NpcMiscInventory inventory;
    public int itemGiverId = 0;

    public List<String> lines = new ArrayList<String>();

    private int ticks = 10;

    private List<IPlayer> recentlyChecked = new ArrayList<IPlayer>();
    private List<IPlayer> toCheck;
    public Availability availability = new Availability();

    public JobItemGiver() {
        super(null);
        inventory = new NpcMiscInventory(9);
    }

    public JobItemGiver(EntityNPCInterface npc) {
        super(npc);
        inventory = new NpcMiscInventory(9);
        lines.add("Have these items {player}");
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setInteger("igCooldownType", cooldownType);
        INbt.setInteger("igGivingMethod", givingMethod);
        INbt.setInteger("igCooldown", cooldown);
        INbt.setInteger("ItemGiverId", itemGiverId);
        INbt.setTag("igLines", NBTTags.nbtStringList(lines));
        INbt.setTag("igJobInventory", inventory.getToNBT());
        INbt.setTag("igAvailability", availability.writeToNBT(new INbt()));
        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        itemGiverId = INbt.getInteger("ItemGiverId");
        cooldownType = INbt.getInteger("igCooldownType");
        givingMethod = INbt.getInteger("igGivingMethod");
        cooldown = INbt.getInteger("igCooldown");
        lines = NBTTags.getStringList(INbt.getTagList("igLines", 10));
        inventory.setFromNBT(INbt.getCompoundTag("igJobInventory"));

        if (itemGiverId == 0 && GlobalDataController.Instance != null)
            itemGiverId = GlobalDataController.Instance.incrementItemGiverId();

        availability.readFromNBT(INbt.getCompoundTag("igAvailability"));
    }

    public INbtList newHashMapNBTList(HashMap<String, Long> lines) {
        INbtList INbtList = new INbtList();
        HashMap<String, Long> lines2 = lines;
        for (String s : lines2.keySet()) {
            INbt INbt = new INbt();
            INbt.setString("Line", s);
            INbt.setLong("Time", lines.get(s));
            INbtList.appendTag(INbt);
        }
        return INbtList;
    }

    public HashMap<String, Long> getNBTLines(INbtList tagList) {
        HashMap<String, Long> map = new HashMap<String, Long>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            INbt INbt = tagList.getCompoundTagAt(i);
            String line = INbt.getString("Line");
            long time = INbt.getLong("Time");
            map.put(line, time);

        }
        return map;
    }

    public boolean giveItems(IPlayer player) {
        PlayerItemGiverData data = PlayerData.get(player).itemgiverData;
        if (!canPlayerInteract(data)) {
            return false;
        }

        Vector<IItemStack> items = new Vector<IItemStack>();
        Vector<IItemStack> toGive = new Vector<IItemStack>();

        for (IItemStack is : inventory.items.values())
            if (is != null)
                items.add(is.copy());
        if (items.isEmpty())
            return false;
        if (isAllGiver()) {
            toGive = items;
        } else if (isRemainingGiver()) {
            for (IItemStack is : items) {
                if (!playerHasItem(player, is.getItem()))
                    toGive.add(is);
            }
        } else if (isRandomGiver()) {
            toGive.add(items.get(npc.worldObj.rand.nextInt(items.size())).copy());
        } else if (isGiverWhenNotOwnedAny()) {
            boolean ownsItems = false;
            for (IItemStack is : items) {
                if (playerHasItem(player, is.getItem())) {
                    ownsItems = true;
                    break;
                }
            }
            if (!ownsItems) {
                toGive = items;
            } else
                return false;
        } else if (isChainedGiver()) {
            int itemIndex = data.getItemIndex(this);
            int i = 0;
            for (IItemStack item : inventory.items.values()) {
                if (i == itemIndex) {
                    toGive.add(item);
                    break;
                }
                i++;
            }
        }
        if (toGive.isEmpty())
            return false;
        if (givePlayerItems(player, toGive)) {
            if (!lines.isEmpty()) {
                npc.say(player, new Line(lines.get(npc.getRNG().nextInt(lines.size()))));
            }
            if (isDaily())
                data.setTime(this, getDay());
            else
                data.setTime(this, System.currentTimeMillis());
            if (isChainedGiver())
                data.setItemIndex(this, (data.getItemIndex(this) + 1) % inventory.items.size());
            return true;
        }
        return false;
    }

    private int getDay() {
        return (int) (npc.worldObj.getTotalWorldTime() / 24000L);
    }

    public boolean canPlayerInteract(PlayerItemGiverData data) {
        if (inventory.items.isEmpty())
            return false;
        if (isOnTimer()) {
            if (!data.hasInteractedBefore(this))
                return true;
            return data.getTime(this) + (cooldown * 1000) < System.currentTimeMillis();
        } else if (isGiveOnce()) {
            return !data.hasInteractedBefore(this);
        } else if (isDaily()) {
            if (!data.hasInteractedBefore(this))
                return true;
            return getDay() > data.getTime(this);
        }
        return false;
    }

    private boolean givePlayerItems(IPlayer player,
                                    Vector<IItemStack> toGive) {
        if (toGive.isEmpty())
            return false;
        if (freeInventorySlots(player) < toGive.size())
            return false;
        for (IItemStack is : toGive) {
            npc.givePlayerItem(player, is);
        }
        return true;
    }

    private boolean playerHasItem(IPlayer player, Item item) {
        for (IItemStack is : player.inventory.mainInventory) {
            if (is != null && is.getItem() == item)
                return true;
        }
        for (IItemStack is : player.inventory.armorInventory) {
            if (is != null && is.getItem() == item)
                return true;
        }
        return false;
    }

    private int freeInventorySlots(IPlayer player) {
        int i = 0;
        for (IItemStack is : player.inventory.mainInventory)
            if (is == null)
                i++;
        return i;
    }

    private boolean isRandomGiver() {
        return givingMethod == 0;
    }

    private boolean isAllGiver() {
        return givingMethod == 1;
    }

    private boolean isRemainingGiver() {
        return givingMethod == 2;
    }

    private boolean isGiverWhenNotOwnedAny() {
        return givingMethod == 3;
    }

    private boolean isChainedGiver() {
        return givingMethod == 4;
    }

    public boolean isOnTimer() {
        return cooldownType == 0;
    }

    private boolean isGiveOnce() {
        return cooldownType == 1;
    }

    private boolean isDaily() {
        return cooldownType == 2;
    }

    public boolean aiShouldExecute() {
        if (npc.isAttacking())
            return false;
        ticks--;
        if (ticks > 0)
            return false;
        ticks = 10;

        toCheck = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(3, 3, 3));
        toCheck.removeAll(recentlyChecked);

        List<IPlayer> listMax = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(10, 10, 10));
        recentlyChecked.retainAll(listMax);
        recentlyChecked.addAll(toCheck);
        return toCheck.size() > 0;
    }

    public void aiStartExecuting() {
        for (IPlayer player : toCheck) {
            if (npc.canSee(player) && availability.isAvailable(player)) {
                recentlyChecked.add(player);
                interact(player);
            }
        }
    }

    @Override
    public void killed() {


    }

    private boolean interact(IPlayer player) {
        if (!giveItems(player))
            npc.say(player, npc.advanced.getInteractLine());
        return true;
    }

    @Override
    public void delete() {


    }
}
