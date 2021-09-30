//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity.data;

import noppes.npcs.scripted.IContainer;
import noppes.npcs.scripted.handler.data.IQuest;

public interface IPlayerMail {
    String getSender();

    void setSender(String var1);

    String getSubject();

    void setSubject(String var1);

    String[] getText();

    void setText(String[] var1);

    IQuest getQuest();

    void setQuest(int var1);

    IContainer getContainer();
}
