package noppes.npcs.api.handler.data;

public interface IQuestKill extends IQuestInterface {
    void setTargetType(int type);
    int getTargetType();
}
