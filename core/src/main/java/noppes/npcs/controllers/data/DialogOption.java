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
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.DialogController;

public class DialogOption {
    public int dialogId = -1;
    public String title = "Talk";
    public EnumOptionType optionType = EnumOptionType.DialogOption;
    public int optionColor = 0xe0e0e0;
    public String command = "";

    public void readNBT(INbt compound) {
        if (compound == null)
            return;
        title = compound.getString("Title");
        dialogId = compound.getInteger("Dialog");
        optionColor = compound.getInteger("DialogColor");
        optionType = EnumOptionType.values()[compound.getInteger("OptionType")];
        command = compound.getString("DialogCommand");
        if (optionColor == 0) {
            optionColor = 0xe0e0e0;
        }
    }

    public INbt writeNBT() {
        INbt compound = new INbt();
        compound.setString("Title", title);
        compound.setInteger("OptionType", optionType.ordinal());
        compound.setInteger("Dialog", dialogId);
        compound.setInteger("DialogColor", optionColor);
        compound.setString("DialogCommand", command);
        return compound;
    }


    public boolean hasDialog() {
        if (dialogId <= 0)
            return false;
        if (!DialogController.Instance.hasDialog(dialogId)) {
            dialogId = -1;
            return false;
        }
        return true;
    }

    public Dialog getDialog() {
        if (!hasDialog())
            return null;
        return DialogController.Instance.dialogs.get(dialogId);
    }

    public boolean isAvailable(IPlayer player) {
        Dialog dialog = getDialog();
        if (dialog == null)
            return false;

        return dialog.availability.isAvailable(player);
    }
}
