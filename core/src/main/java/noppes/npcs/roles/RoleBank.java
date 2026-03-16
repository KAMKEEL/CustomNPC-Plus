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
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.BankData;
public class RoleBank extends RoleInterface {

    public int bankId = -1;

    public RoleBank(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setInteger("RoleBankID", bankId);
        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        bankId = INbt.getInteger("RoleBankID");
    }

    @Override
    public void interact(IPlayer player) {
        BankData data = PlayerDataController.Instance.getBankData(player, bankId).getBankOrDefault(bankId);
        data.openBankGui(player, npc, bankId, 0);
        npc.say(player, npc.advanced.getInteractLine());
    }

    public Bank getBank() {
        Bank bank = BankController.getInstance().banks.get(bankId);
        if (bank != null)
            return bank;
        return BankController.getInstance().banks.values().iterator().next();
    }
}
