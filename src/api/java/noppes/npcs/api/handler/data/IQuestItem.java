package noppes.npcs.api.handler.data;

public interface IQuestItem extends IQuestInterface {
    void setLeaveItems(boolean leaveItems);
    boolean getLeaveItems();

    void setIgnoreDamage(boolean ignoreDamage);
    boolean getIgnoreDamage();

    void setIgnoreNbt(boolean ignoreNbt);
    boolean getIgnoreNbt();
}
