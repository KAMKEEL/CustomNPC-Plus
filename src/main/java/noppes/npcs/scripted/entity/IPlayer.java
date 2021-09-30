//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.scripted.IContainer;
import noppes.npcs.scripted.ITimers;
import noppes.npcs.scripted.entity.data.IPlayerMail;
import noppes.npcs.scripted.gui.ICustomGui;
import noppes.npcs.scripted.handler.data.IQuest;
import noppes.npcs.scripted.item.IItemStack;

public interface IPlayer<T extends EntityPlayerMP> extends IEntityLivingBase<T> {
    String getDisplayName();

    boolean hasFinishedQuest(int var1);

    boolean hasActiveQuest(int var1);

    void startQuest(int var1);

    int factionStatus(int var1);

    void finishQuest(int var1);

    void stopQuest(int var1);

    void removeQuest(int var1);

    boolean hasReadDialog(int var1);

    void showDialog(int var1, String var2);

    void removeDialog(int var1);

    void addDialog(int var1);

    void addFactionPoints(int var1, int var2);

    int getFactionPoints(int var1);

    void message(String var1);

    int getGamemode();

    void setGamemode(int var1);

    /** @deprecated */
    int inventoryItemCount(IItemStack var1);

    /** @deprecated */
    int inventoryItemCount(String var1, int var2);

    IContainer getInventory();

    boolean removeItem(IItemStack var1, int var2);

    boolean removeItem(String var1, int var2, int var3);

    void removeAllItems(IItemStack var1);

    boolean giveItem(IItemStack var1);

    boolean giveItem(String var1, int var2, int var3);

    void setSpawnpoint(int var1, int var2, int var3);

    void resetSpawnpoint();

    boolean hasAchievement(String var1);

    int getExpLevel();

    void setExpLevel(int var1);

    boolean hasPermission(String var1);

    Object getPixelmonData();

    ITimers getTimers();

    void closeGui();

    T getMCEntity();

    int getHunger();

    void setHunger(int var1);

    void kick(String var1);

    void sendNotification(String var1, String var2, int var3);

    void sendMail(IPlayerMail var1);

    void clearData();

    IQuest[] getActiveQuests();

    IQuest[] getFinishedQuests();

    void updatePlayerInventory();

    void playSound(String var1, float var2, float var3);

    /** @deprecated */
    IContainer showChestGui(int var1);

    IContainer getOpenContainer();

    boolean canQuestBeAccepted(int var1);

    void showCustomGui(ICustomGui var1);

    ICustomGui getCustomGui();
}
