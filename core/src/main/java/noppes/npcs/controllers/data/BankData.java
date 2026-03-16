package noppes.npcs.controllers.data;

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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.BankController;
import java.util.HashMap;

public class BankData {
    public HashMap<Integer, NpcMiscInventory> itemSlots;
    public HashMap<Integer, Boolean> upgradedSlots;
    public int unlockedSlots = 0;
    public int bankId = -1;

    public BankData() {
        itemSlots = new HashMap<Integer, NpcMiscInventory>();
        upgradedSlots = new HashMap<Integer, Boolean>();

        for (int i = 0; i < 6; i++) {
            itemSlots.put(i, new NpcMiscInventory(54));
            upgradedSlots.put(i, false);
        }
    }

    public void readNBT(INbt INbt) {
        bankId = INbt.getInteger("DataBankId");
        unlockedSlots = INbt.getInteger("UnlockedSlots");
        itemSlots = getItemSlots(INbt.getTagList("BankInv", 10));
        upgradedSlots = NBTTags.getBooleanList(INbt.getTagList("UpdatedSlots", 10));
    }

    private HashMap<Integer, NpcMiscInventory> getItemSlots(INbtList tagList) {
        HashMap<Integer, NpcMiscInventory> list = new HashMap<Integer, NpcMiscInventory>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            INbt INbt = tagList.getCompoundTagAt(i);
            int slot = INbt.getInteger("Slot");
            NpcMiscInventory inv = new NpcMiscInventory(54);
            inv.setFromNBT(INbt.getCompoundTag("BankItems"));
            list.put(slot, inv);
        }
        return list;
    }

    public void writeNBT(INbt INbt) {
        INbt.setInteger("DataBankId", bankId);
        INbt.setInteger("UnlockedSlots", unlockedSlots);
        INbt.setTag("UpdatedSlots", NBTTags.nbtBooleanList(upgradedSlots));
        INbt.setTag("BankInv", nbtItemSlots(itemSlots));
    }

    private INbtList nbtItemSlots(HashMap<Integer, NpcMiscInventory> items) {
        INbtList list = new INbtList();
        for (int slot : items.keySet()) {
            INbt INbt = new INbt();
            INbt.setInteger("Slot", slot);

            INbt.setTag("BankItems", items.get(slot).getToNBT());
            list.appendTag(INbt);
        }
        return list;
    }

    public boolean isUpgraded(Bank bank, int slot) {
        if (bank.isUpgraded(slot))
            return true;
        return bank.canBeUpgraded(slot) && upgradedSlots.get(slot);
    }

    public void openBankGui(final IPlayer player, EntityNPCInterface npc, int bankId, int slot) {
        final Bank bank = BankController.getInstance().getBank(bankId);

        if (bank.getMaxSlots() <= slot)
            return;

        if (bank.startSlots > unlockedSlots)
            unlockedSlots = bank.startSlots;

        IItemStack currency = null;
        if (unlockedSlots <= slot) {
            currency = bank.currencyInventory.getStackInSlot(slot);
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUnlock, npc, slot, bank.id, 0);
        } else if (isUpgraded(bank, slot)) {
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankLarge, npc, slot, bank.id, 0);
        } else if (bank.canBeUpgraded(slot)) {
            currency = bank.upgradeInventory.getStackInSlot(slot);
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUprade, npc, slot, bank.id, 0);
        } else {
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankSmall, npc, slot, bank.id, 0);
        }
        final IItemStack item = currency;
        INbt compound = new INbt();
        compound.setInteger("MaxSlots", bank.getMaxSlots());
        compound.setInteger("UnlockedSlots", unlockedSlots);
        if (item != null) {
            compound.setTag("Currency", NoppesUtilServer.writeItem(item, new INbt()));
            ContainerNPCBankInterface container = getContainer(player);
            if (container != null)
                container.setCurrency(item);
        }

        GuiDataPacket.sendGuiData((IPlayer) player, compound);
    }

    private ContainerNPCBankInterface getContainer(IPlayer player) {
        Container con = player.openContainer;
        if (con == null || !(con instanceof ContainerNPCBankInterface))
            return null;

        return (ContainerNPCBankInterface) con;
    }
}
