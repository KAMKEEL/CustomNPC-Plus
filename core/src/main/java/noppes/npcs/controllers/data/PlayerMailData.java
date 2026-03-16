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
import noppes.npcs.api.handler.IPlayerMailData;
import noppes.npcs.api.handler.data.IPlayerMail;

import java.util.ArrayList;

public class PlayerMailData implements IPlayerMailData {
    private final PlayerData parent;
    public ArrayList<PlayerMail> playermail = new ArrayList<PlayerMail>();

    public PlayerMailData() {
        this.parent = null;
    }

    public PlayerMailData(PlayerData parent) {
        this.parent = parent;
    }

    public void loadNBTData(INbt compound) {
        ArrayList<PlayerMail> newmail = new ArrayList<PlayerMail>();
        INbtList list = compound.getTagList("MailData", 10);
        if (list == null)
            return;

        for (int i = 0; i < list.tagCount(); i++) {
            PlayerMail mail = new PlayerMail();
            mail.readNBT(list.getCompoundTagAt(i));
            newmail.add(mail);
        }
        playermail = newmail;
    }

    public INbt saveNBTData(INbt compound) {
        INbtList list = new INbtList();

        for (PlayerMail mail : playermail) {
            list.appendTag(mail.writeNBT());
        }

        compound.setTag("MailData", list);
        return compound;
    }

    public boolean hasMail() {
        for (PlayerMail mail : playermail)
            if (!mail.beenRead)
                return true;
        return false;
    }

    public void addMail(IPlayerMail mail) {
        playermail.add((PlayerMail) mail);
    }

    public void removeMail(IPlayerMail mail) {
        playermail.remove((PlayerMail) mail);
    }

    public boolean hasMail(IPlayerMail mail) {
        return playermail.contains((PlayerMail) mail);
    }

    public IPlayerMail[] getAllMail() {
        return playermail.toArray(new IPlayerMail[0]);
    }

    public IPlayerMail[] getUnreadMail() {
        ArrayList<IPlayerMail> mails = new ArrayList<>();

        for (PlayerMail mail : playermail) {
            if (!mail.beenRead) {
                mails.add(mail);
            }
        }

        return mails.toArray(new IPlayerMail[0]);
    }

    public IPlayerMail[] getReadMail() {
        ArrayList<IPlayerMail> mails = new ArrayList<>();

        for (PlayerMail mail : playermail) {
            if (mail.beenRead) {
                mails.add(mail);
            }
        }

        return mails.toArray(new IPlayerMail[0]);
    }

    public IPlayerMail[] getMailFrom(String sender) {
        ArrayList<IPlayerMail> mails = new ArrayList<>();

        for (PlayerMail mail : playermail) {
            if (mail.sender.equals(sender)) {
                mails.add(mail);
            }
        }

        return mails.toArray(new IPlayerMail[0]);
    }
}
