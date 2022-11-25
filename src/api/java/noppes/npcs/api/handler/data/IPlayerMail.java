package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

public interface IPlayerMail {

    void setPageText(String[] pages);

    String[] getPageText();

    int getPageCount();

    void setSender(String sender);

    String getSender();

    void setSubject(String subject);

    String getSubject();

    long getTimePast();

    long getTimeSent();

    boolean hasQuest();

    IQuest getQuest();

    IItemStack[] getItems();

    void setItems(IItemStack[] items);
}
