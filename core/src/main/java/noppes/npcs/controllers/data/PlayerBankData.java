package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.api.handler.IPlayerBankData;
import noppes.npcs.controllers.BankController;

import java.util.HashMap;

public class PlayerBankData implements IPlayerBankData {
    private final PlayerData parent;
    public HashMap<Integer, BankData> banks;

    public PlayerBankData(PlayerData parent) {
        banks = new HashMap<Integer, BankData>();
        this.parent = parent;
    }

    public void loadNBTData(INbt compound) {
        HashMap<Integer, BankData> banks = new HashMap<Integer, BankData>();
        INbtList list = compound.getTagList("BankData", 10);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.tagCount(); i++) {
            INbt INbt = list.getCompoundTagAt(i);
            BankData data = new BankData();
            data.readNBT(INbt);
            banks.put(data.bankId, data);
        }
        this.banks = banks;
    }

    public void saveNBTData(INbt playerData) {
        INbtList list = new INbtList();
        for (BankData data : banks.values()) {
            INbt INbt = new INbt();
            data.writeNBT(INbt);
            list.appendTag(INbt);
        }
        playerData.setTag("BankData", list);
    }


    public BankData getBank(int bankId) {
        return banks.get(bankId);
    }

    public BankData getBankOrDefault(int bankId) {
        BankData data = banks.get(bankId);
        if (data != null)
            return data;
        Bank bank = BankController.getInstance().getBank(bankId);
        return banks.get(bank.id);
    }

    public boolean hasBank(int bank) {
        return banks.containsKey(bank);
    }

    public void loadNew(int bank) {
        BankData data = new BankData();
        data.bankId = bank;
        banks.put(bank, data);

    }
}
