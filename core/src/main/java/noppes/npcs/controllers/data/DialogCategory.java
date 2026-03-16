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
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DialogCategory implements IDialogCategory {

    public int id = -1;
    public String title = "";
    public HashMap<Integer, Dialog> dialogs;

    public DialogCategory() {
        dialogs = new HashMap<Integer, Dialog>();
    }

    public void readNBT(INbt compound) {
        id = compound.getInteger("Slot");
        title = compound.getString("Title");

        INbtList dialogsList = compound.getTagList("Dialogs", 10);
        if (dialogsList != null) {
            for (int ii = 0; ii < dialogsList.tagCount(); ii++) {
                Dialog dialog = new Dialog();
                dialog.category = this;
                INbt comp = dialogsList.getCompoundTagAt(ii);
                dialog.readNBT(comp);
                dialog.id = comp.getInteger("DialogId");
                dialogs.put(dialog.id, dialog);
            }
        }
    }

    public INbt writeNBT(INbt dialogCat) {
        dialogCat.setInteger("Slot", id);
        dialogCat.setString("Title", title);
        INbtList dialogs = new INbtList();
        for (Dialog dialog : this.dialogs.values()) {
            dialogs.appendTag(dialog.writeToNBT(new INbt()));
        }
        dialogCat.setTag("Dialogs", dialogs);
        return dialogCat;
    }

    public INbt writeSmallNBT(INbt dialogCat) {
        dialogCat.setInteger("Slot", id);
        dialogCat.setString("Title", title);
        return dialogCat;
    }

    public void readSmallNBT(INbt compound) {
        id = compound.getInteger("Slot");
        title = compound.getString("Title");
    }

    public List<IDialog> dialogs() {
        return new ArrayList(this.dialogs.values());
    }

    public String getName() {
        return this.title;
    }

    public IDialog create() {
        Dialog dialog = new Dialog();
        dialog.category = this;
        return dialog;
    }

    @Override
    public int getId() {
        return this.id;
    }
}
