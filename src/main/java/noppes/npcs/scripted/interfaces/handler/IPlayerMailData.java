package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.handler.data.IPlayerMail;

public interface IPlayerMailData {

    boolean hasMail();

    void addMail(IPlayerMail mail);

    void removeMail(IPlayerMail mail);

    boolean hasMail(IPlayerMail mail);

    IPlayerMail[] getAllMail();

    IPlayerMail[] getUnreadMail();

    IPlayerMail[] getReadMail();

    IPlayerMail[] getMailFrom(String sender);
}
